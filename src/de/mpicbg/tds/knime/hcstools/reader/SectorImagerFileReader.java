package de.mpicbg.tds.knime.hcstools.reader;

import au.com.bytecode.opencsv.CSVReader;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.util.StringArrayDummySheet;
import de.mpicbg.tds.core.util.StringTable;
import de.mpicbg.tds.knime.hcstools.utils.ExcelUtils;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.tds.knime.knutils.ui.FileSelectPanel;
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

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * This is the model implementation of SectorImager-file reader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class SectorImagerFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();


    public SectorImagerFileReader() {
        super(0, 1);

        addSetting(propInputDir);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {


        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), SectorImagerFileReaderFactory.SECTORIMAGER_FILE_SUFFIXES);

        if (inputFiles.isEmpty()) {
            throw new RuntimeException("No files selected");
        }

        List<Attribute> attributes = compileAttributes(inputFiles);

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(attributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int fileCounter = 0;
        int tableRowCounter = 0;


        for (File inputFile : inputFiles) {
            logger.info("reading file " + inputFile);


            String fileName = inputFile.getName();
            String barcode = fileName.endsWith("x") ? fileName.replace(".xlsx", "") : fileName.replace(".xls", "");


            Map<String, TreeMap<String, StringTable>> filePlateCache = parseSecImFile(inputFile);


            int numFeatures;
            try {
                numFeatures = filePlateCache.values().iterator().next().size();
            } catch (Throwable t) {
                throw new RuntimeException("Could not guess the number of features from file " + inputFile, t);
            }

            // iterate over all plates in the file
            for (String plateBarcode : filePlateCache.keySet()) {

                TreeMap<String, StringTable> plateData = filePlateCache.get(plateBarcode);

                StringTable firstTable = plateData.values().iterator().next();

                // iterate over all well positions in the file
                for (int rowIndex = 0; rowIndex < firstTable.getHeight(); rowIndex++) {
                    for (int colIndex = 0; colIndex < firstTable.getWidth(); colIndex++) {

                        DataCell[] knimeRow = new DataCell[3 + numFeatures]; // +3 because of barcode and row/col positions


                        // start with the barcode column
                        knimeRow[0] = attributes.get(0).createCell(plateBarcode);

                        knimeRow[1] = attributes.get(1).createCell(rowIndex + 1);
                        knimeRow[2] = attributes.get(2).createCell(colIndex + 1);

                        // now add the actual readout
                        int featureCounter = 0;
                        for (String featureName : plateData.keySet()) {
                            StringTable readoutTable = plateData.get(featureName);

                            knimeRow[3 + featureCounter] = attributes.get(3 + featureCounter).createCell(readoutTable.get(rowIndex, colIndex));

                            featureCounter++;
                        }

                        DataRow tableRow = new DefaultRow(new RowKey("" + tableRowCounter++), knimeRow);
                        container.addRowToTable(tableRow);
                    }
                }
            }

            BufTableUtils.updateProgress(exec, fileCounter++, inputFiles.size());
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }


    public static void main(String[] args) throws IOException {
//        Map<String, TreeMap<String, StringTable>> stringTreeMapMap = readDataCSV(new File("/Users/brandl/projects/knime/hcstools/misc/MSD Vector Imager 96example.txt"));
        Map<String, TreeMap<String, StringTable>> stringTreeMapMap = parseSecImFile(new File("/Users/brandl/projects/knime/hcstools/misc/MSD Vector Imager 384example.txt"));

        System.err.println("parsing sucessful!");

        for (String barcode : stringTreeMapMap.keySet()) {
            TreeMap<String, StringTable> plateFeatures = stringTreeMapMap.get(barcode);

            System.err.println(barcode + ": " + plateFeatures.toString());
        }
    }


    /**
     * @return a map with barcodes as keys, and maps of ordered feature-tables as values.
     */
    public static Map<String, TreeMap<String, StringTable>> parseSecImFile(File inputFile) {
        Map<String, TreeMap<String, StringTable>> platesData = new TreeMap<String, TreeMap<String, StringTable>>();

        try {

            CSVReader reader = new CSVReader(new FileReader(inputFile), '\t', '\"');

            List<String[]> csvLines = reader.readAll();

            StringArrayDummySheet sheet = new StringArrayDummySheet(csvLines);

            String curBarcode = null;
            TreeMap<String, StringTable> curReadoutMap = null;

            for (int curRowNum = 0; curRowNum < csvLines.size(); curRowNum++) {
                // find barcode 1 field and read barcode
                String value = sheet.getRow(curRowNum).getCell(0).getStringCellValue();
                if (value.startsWith("FileName :")) {
                    // we've found another plate let's read it!
//                    curBarcode = sheet.getRow(curRowNum).getCell(1).getStringCellValue().replace("<*", "").replace("*>", "");
                    curBarcode = sheet.getRow(curRowNum).getCell(1).getStringCellValue().replace(".txt", "");

                    curReadoutMap = new TreeMap<String, StringTable>();
                    platesData.put(curBarcode, curReadoutMap);
                }

                if (curBarcode == null) {
                    continue;
                }

                Point nextPlatePosition = StringTable.findNextPlatePosition(sheet, new Point(1, curRowNum + 1));
                if (nextPlatePosition == null)
                    break;

                // detect the number of spots
                int numSpots = 1;
                while (numSpots < 10) { // they should never be more
                    int rowIndex = (int) (nextPlatePosition.getY() + numSpots);
                    int columnIndex = (int) (nextPlatePosition.getX() - 1);

                    if (!ExcelUtils.getCellContents(sheet, rowIndex, columnIndex).isEmpty()) {
                        break;
                    }

                    numSpots++;
                }

                Rectangle plateDim = StringTable.guessPlateBounds(sheet, nextPlatePosition, numSpots);
                if (plateDim == null)
                    break;

                //            if(curReadoutMap == null) {
                //                throw new RuntimeException("File '" + inputFile + "' does not comply with expected Vector Imager file format");
                //            }

                for (int spotOffset = 0; spotOffset < numSpots; spotOffset++) {
                    Rectangle offsetPlateDim = new Rectangle();
                    offsetPlateDim.setRect(plateDim.getX() + 1, plateDim.getY() + spotOffset + 1, plateDim.getWidth() - 1, plateDim.getHeight() - 1);

                    StringTable readoutTable = StringTable.readStringGridFromExcel(offsetPlateDim, sheet, numSpots);

                    curReadoutMap.put("Feature " + (spotOffset + 1), readoutTable);
                }

                break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Parsing of file Sector Imager result file '" + inputFile + "' failed");
        }

        if (platesData.isEmpty()) {
            throw new RuntimeException("Sector Imager result file '" + inputFile + "' seems to contain no valid readout tables.");
        }

        return platesData;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<File> sectorImagesFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), SectorImagerFileReaderFactory.SECTORIMAGER_FILE_SUFFIXES);
        if (sectorImagesFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }


        List<Attribute> colAttributes = compileAttributes(sectorImagesFiles);

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }


    private List<Attribute> compileAttributes(List<File> sectorImagesFiles) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));

        attributes.addAll(getFeatureAttributes(sectorImagesFiles.get(0)));

        return attributes;
    }


    private List<Attribute> getFeatureAttributes(File file) {
        Map<String, TreeMap<String, StringTable>> map = parseSecImFile(file);


        List<Attribute> featureAttrs = new ArrayList<Attribute>();
        TreeMap<String, StringTable> firstPlateDataTable = map.values().iterator().next();

        for (int i = 0; i < firstPlateDataTable.size(); i++) {
            featureAttrs.add(new Attribute("Feature " + (i + 1), DoubleCell.TYPE));
        }

        return featureAttrs;
    }

}
