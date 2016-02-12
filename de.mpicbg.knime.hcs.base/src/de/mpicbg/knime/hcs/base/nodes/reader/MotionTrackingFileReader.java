package de.mpicbg.knime.hcs.base.nodes.reader;

import au.com.bytecode.opencsv.CSVReader;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.knime.knutils.ui.FileSelectPanel;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParser;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParserFactory;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;
import de.mpicbg.knime.hcs.core.TdsUtils;
import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the model implementation of MotionTracking file reader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class MotionTrackingFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();
    public SettingsModelString propSuffixPattern = createSuffixPatternProperty();
    public SettingsModelString fileExtension = MotionTrackingFileReaderFactory.createFileExtensionSelection();
    public SettingsModelString columnSeperatorSetting = MotionTrackingFileReaderFactory.createColumnSeperatorSelection();


    public MotionTrackingFileReader() {
        super(0, 1);

        addSetting(propInputDir);
        addSetting(propSuffixPattern);
        addSetting(fileExtension);
        addSetting(columnSeperatorSetting);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), fileExtension.getStringValue());
        List<Attribute> colAttributes = getMotionTrackingColModel(inputFiles.get(0));


        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(colAttributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);


        int rowCounter = 0;

        for (File inputFile : inputFiles) {

            logger.info("reading file " + inputFile);

//            BufferedReader reader = new BufferedReader();
            char columnSeperator = columnSeperatorSetting.getStringValue().toCharArray()[0];
            CSVReader reader = new CSVReader(new FileReader(inputFile), columnSeperator, '\"');

            BarcodeParserFactory barcodeParserFactory = buildParserFactory();

            List<Attribute> curColumnModel = getMotionTrackingColModel(inputFiles.get(0));

            if (!curColumnModel.equals(colAttributes)) {
                throw new RuntimeException("Column model not consistent in input-files: Number of expected columns " + colAttributes.size() + " not present in file " + inputFile);
            }


            // skip the column header
            reader.readNext();
            exec.setProgress("reading file: " + inputFile.getName());

            String[] line;
            int errCounter = 0;

            while ((line = reader.readNext()) != null) {

                // read the data
                try {

                    // post process line to remove training commas (note: motion tracking adds them occasionally)
                    if (line.length == 0) {
                        break;
                    }

                    DataCell[] knimeRow = new DataCell[colAttributes.size()];


                    // try to build a parser for it
                    BarcodeParser parser = buildFileNameParser(line[0], barcodeParserFactory);


                    // now add the actual contents
                    int colIndex = 0;
                    knimeRow[colIndex++] = new StringCell(parser.getGroup(GROUP_BARCODE));
                    knimeRow[colIndex++] = new IntCell(Integer.parseInt(parser.getGroup(GROUP_ROW)));
                    knimeRow[colIndex++] = new IntCell(Integer.parseInt(parser.getGroup(GROUP_COLUMN)));

                    if (AttributeUtils.contains(colAttributes, GROUP_WELL_FIELD_DESC)) {
                        knimeRow[colIndex++] = new IntCell(Integer.parseInt(parser.getGroup(GROUP_WELL_FIELD)));
                    }

                    if (AttributeUtils.contains(colAttributes, GROUP_TIMEPOINT_DESC)) {
                        knimeRow[colIndex++] = new IntCell(Integer.parseInt(parser.getGroup(BarcodeParser.GROUP_TIMEPOINT)));
                    }

                    if (AttributeUtils.contains(colAttributes, BarcodeParser.GROUP_FRAME)) {
                        knimeRow[colIndex++] = new IntCell(Integer.parseInt(parser.getGroup(BarcodeParser.GROUP_FRAME)));
                    }

                    int dataColIndex = 2;
                    for (int i = colIndex; i < colAttributes.size(); i++) {
                        String colValue = line[dataColIndex++].trim().toLowerCase();


                        Double readout = null;

                        try {
                            readout = Double.parseDouble(colValue);
                        } catch (NumberFormatException e) {
                        }

                        knimeRow[i] = colAttributes.get(i).createCell(readout);
                    }

                    DataRow tableRow = new DefaultRow(new RowKey("" + rowCounter++), knimeRow);
                    container.addRowToTable(tableRow);

                } catch (Throwable t) {
                    logger.warn(t.toString());
                    logger.error("Could not parse line beginning with " + line[0]);

                    if (errCounter++ > 100) {
                        throw new RuntimeException("More than 100s lines could not be parsed correctly. The remainder of the file is thus skipped!\n" +
                                "It's likely thar your suffix- and barcode-pattern do not match to your files");
                    }

                }

                exec.checkCanceled();
//                AbstractScreenTrafoModel.updateProgress(exec, (int) (rowCounter/(double)inputFiles.size()), inputFiles.size());
            }
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};

    }


    private BarcodeParser buildFileNameParser(String fileName, BarcodeParserFactory bpf) {
        BarcodeParser barcodeParser = bpf.getParser(fileName);

        if (barcodeParser == null) {
            logger.error("Could not match file-name against registered any registered barcodepattern: \n");
            ArrayList<NamedPattern> patterns = bpf.getPatterns();
            for (int i = 0; i < patterns.size(); i++) {
                NamedPattern pattern = patterns.get(i);
                logger.error("pattern " + i + ": " + pattern);

            }
            throw new IllegalArgumentException();
        }

        List<String> availGroups = barcodeParser.getAvailableGroups();

        if (!availGroups.contains(GROUP_ROW) || !availGroups.contains(GROUP_COLUMN)) {
            throw new RuntimeException("suffix pattern MUST contain named groups for row and column position named 'row' and 'column' respectively");
        }

        return barcodeParser;
    }


    private BarcodeParserFactory buildParserFactory() {
        String[] suffixPatterns = propSuffixPattern.getStringValue().split(";");

        // expand all combinations and try to find a matching one
        BarcodeParserFactory bpf = new BarcodeParserFactory();

        for (String suffixPattern : suffixPatterns) {
            bpf.registerPattern(suffixPattern);
        }

        return bpf;
    }


    public static final String GROUP_WELL_FIELD = "wellfield";
    public static final String GROUP_WELL_FIELD_DESC = "Well Field";
    public static final String GROUP_BARCODE = "barcode";
    public static final String GROUP_ROW = "row";
    public static final String GROUP_COLUMN = "column";
    public static final String GROUP_TIMEPOINT_DESC = "Timepoint";


    public static SettingsModelString createSuffixPatternProperty() {
        return new SettingsModelString("mtffile.nonbarcode.suffix", "(?<barcode>.*)Meas.*__(?<row>[0-9]{3})(?<column>[0-9]{3})(?<frame>[0-9]{3})_(?<timepoint>[0-9]{1})(?<wellfield>[0-9]{3})[.]mtf");
    }


    private List<Attribute> getMotionTrackingColModel(File inputFile) {
        try {

            char columnSeperator = columnSeperatorSetting.getStringValue().toCharArray()[0];
            CSVReader reader = new CSVReader(new FileReader(inputFile), columnSeperator, '\"');
            String[] columns = reader.readNext();

            List<Attribute> colAttributes = new ArrayList<Attribute>();

            colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
            colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
            colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));

            BarcodeParser barcodeParser = buildFileNameParser(reader.readNext()[0], buildParserFactory());
            if (barcodeParser.getAvailableGroups().contains(GROUP_WELL_FIELD)) {
                colAttributes.add(new Attribute(GROUP_WELL_FIELD_DESC, IntCell.TYPE));
            }

            if (barcodeParser.getAvailableGroups().contains(BarcodeParser.GROUP_TIMEPOINT)) {
                colAttributes.add(new Attribute(GROUP_TIMEPOINT_DESC, IntCell.TYPE));
            }

            if (barcodeParser.getAvailableGroups().contains(BarcodeParser.GROUP_FRAME)) {
                colAttributes.add(new Attribute(BarcodeParser.GROUP_FRAME, IntCell.TYPE));
            }


            for (int i = 2, columnsLength = columns.length; i < columnsLength; i++) {
                String colName = columns[i].trim();

                if (StringUtils.isNotBlank(colName)) {
                    colAttributes.add(new Attribute(colName, DoubleCell.TYPE));
                }
            }

            return colAttributes;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {

        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), fileExtension.getStringValue());
        if (inputFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }

        List<Attribute> colAttributes = getMotionTrackingColModel(inputFiles.get(0));

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }


    public static void main(String[] args) {
//        String pattern = "(?<barcode>(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*))14__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{3})(?<wellfield>[0-9]{1})[.]mtf";
        String pattern = "(?<barcode>.*)__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{1})(?<wellfield>[0-9]{3})[.]mtf";
//        String inputString = "001lk100528a-___rtchm\\001LK100528A-___RTCHM_Meas_01_2010-06-02_12-48-14__003003000_0000.mtf";
//        String inputString = "001MB100630A-MSD-AHe_Meas_01_2010-07-02_20-28-33__001002000_0017.mtf";
        String inputString = "900ec100510a-dms-hel900EC100510A-DMS-HEL_Meas_01_2010-05-11_19-05-00__001023000_0004.mtf";

        BarcodeParser parser = new BarcodeParser(inputString, NamedPattern.compile(pattern));
        String barcode = parser.getGroup(GROUP_BARCODE);

        System.err.println("the barcode is " + barcode);
    }
}