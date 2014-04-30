package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.knime.knutils.ui.FileSelectPanel;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.util.ScreenImportUtils;
import de.mpicbg.tds.core.util.StringTable;
import de.mpicbg.tds.knime.hcstools.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;


/**
 * This is the model implementation of ExcelReader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class EnvisionFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();
    public SettingsModelInteger propTableIndex = EnvisionFileReaderFactory.createTableIndex();


    public EnvisionFileReader() {
        super(0, 1);

        addSetting(propInputDir);
        addSetting(propTableIndex);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), getAllowedFileExtensions());

        if (inputFiles.isEmpty()) {
            throw new RuntimeException("No files selected");
        }

        // first group files into plate-groups
        Map<String, List<File>> plateFiles = splitFilesIntoPlates(inputFiles);


        if (inputFiles.isEmpty()) {
            throw new RuntimeException("No valid envision-files in selection " + inputFiles);
        }


        // split files
        List<String> allAttributes = mergeAttributes(plateFiles);
        List<Attribute> colAttributes = compileColumnModel(allAttributes);

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(colAttributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        // populate the table
        int fileCounter = 0, rowCounter = 0;
        for (String barcode : plateFiles.keySet()) {

            logger.info("Processing plate " + barcode);


            Plate plate = new Plate();

            // invalidate plate-dims as these become fixed in the loop
            plate.setNumColumns(-1);
            plate.setNumRows(-1);

            for (File file : plateFiles.get(barcode)) {
                String attributeName = getAttributeNameOfEnvisionFile(file);
                parseFile(plate, attributeName, file);

                BufTableUtils.updateProgress(exec, fileCounter++, inputFiles.size());
            }


            // now create the data-rows for this table
            for (Well well : plate.getWells()) {
                if (well.getReadOutNames().isEmpty()) {
                    continue;
                }

                DataCell[] knimeRow = new DataCell[colAttributes.size()];


                // first add the barcode-column
                knimeRow[0] = new StringCell(barcode);

                knimeRow[1] = colAttributes.get(1).createCell(well.getPlateRow());
                knimeRow[2] = colAttributes.get(2).createCell(well.getPlateColumn());


                for (String attributeName : allAttributes) {
                    int rowIndex = allAttributes.indexOf(attributeName);
                    Double value = well.getReadout(attributeName);

                    if (value != null) {
                        knimeRow[3 + rowIndex] = new DoubleCell(value);
                    } else {
                        knimeRow[3 + rowIndex] = DataType.getMissingCell();
                    }
                }

                DataRow tableRow = new DefaultRow(new RowKey("" + rowCounter++), knimeRow);
                container.addRowToTable(tableRow);
            }
        }


        container.close();

        return new BufferedDataTable[]{container.getTable()};

    }


    public static String[] getAllowedFileExtensions() {
        return new String[]{"xls", "xlsx"};
    }


    private List<String> mergeAttributes(Map<String, List<File>> inputFiles) {
        Set<String> mergedAttributes = new HashSet<String>();
        for (List<File> plateFiles : inputFiles.values()) {
            mergedAttributes.addAll(getAssayParamNames(plateFiles));

        }

        for (String barcode : inputFiles.keySet()) {
            List<File> plateFiles = inputFiles.get(barcode);

            HashSet<String> curSetAttribtues = new HashSet<String>(getAssayParamNames(plateFiles));

            if (!mergedAttributes.equals(curSetAttribtues)) {
                logger.error("The attributes '" + curSetAttribtues + "'of plate '" + barcode + "' are not the same as the collected ones: '" + mergedAttributes + "'");
            }
        }


        return new ArrayList<String>(mergedAttributes);
    }


    private Map<String, List<File>> splitFilesIntoPlates(List<File> inputFiles) {
        Map<String, List<File>> plateFiles = new HashMap<String, List<File>>();

        for (File inputFile : inputFiles) {
            String fileName = inputFile.getName();

            String attributeName = getAttributeNameOfEnvisionFile(inputFile);
            if (attributeName == null) {
                logger.error("File " + inputFile + " seems not to be a valid envision file");
                continue;
            }

            String barcode = getBardoceOfInvisionFile(inputFile);

            if (!plateFiles.containsKey(barcode)) {
                plateFiles.put(barcode, new ArrayList<File>());
            }

            plateFiles.get(barcode).add(inputFile);
        }

        return plateFiles;
    }


    private List<String> getAssayParamNames(List<File> files) {
        List<String> parNames = new ArrayList<String>();

        for (File file : files) {
            parNames.add(getAttributeNameOfEnvisionFile(file));
        }

        return parNames;
    }


    private String getAttributeNameOfEnvisionFile(File inputFile) {
        String fileName2 = inputFile.getName();
        String[] splitName = fileName2.split("__");

        if (splitName.length != 2) {
            return "Envision Readout";
        }

        return ExcelUtils.removeExcelSuffix(splitName[splitName.length - 1]);
    }


    private String getBardoceOfInvisionFile(File inputFile) {
        String inputFileName = inputFile.getName();
        inputFileName = ExcelUtils.removeExcelSuffix(inputFileName);

        return inputFileName.replace(getAttributeNameOfEnvisionFile(inputFile), "");
    }


    public void parseFile(Plate plate, String attributeName, File envisionFile) {

        Sheet sheet = StringTable.openWorkSheet(envisionFile).getSheetAt(0);

        Point tableTopLeftPos = StringTable.findNextPlatePosition(sheet, new Point(1, 1));

        int numSkipTables = propTableIndex.getIntValue() - 1;
        while (tableTopLeftPos != null && numSkipTables > 0) {
            tableTopLeftPos = StringTable.findNextPlatePosition(sheet, new Point((int) tableTopLeftPos.getX(), (int) (tableTopLeftPos.getY() + 1)));
            numSkipTables--;
        }

        if (tableTopLeftPos == null) {
            throw new RuntimeException("Could not find readout-table in file " + envisionFile);
        }

        Rectangle tableBounds = StringTable.guessPlateBounds(sheet, tableTopLeftPos);
        StringTable envisionTable = StringTable.readStringGridFromExcel(tableBounds, sheet);

        // either set the plate dimensions or validate them
        if (plate.getNumRows() < 0) {
            plate.setNumColumns(envisionTable.getWidth() - 1);
            plate.setNumRows(envisionTable.getHeight() - 1);
        } else {
            assert envisionTable.getWidth() - 1 == plate.getNumColumns();
            assert envisionTable.getHeight() - 1 == plate.getNumRows();
        }

        for (int colIndex = 0; colIndex < plate.getNumColumns(); colIndex++) {
            for (int rowIndex = 0; rowIndex < plate.getNumRows(); rowIndex++) {
                int plateRow = rowIndex + 1;      // this inversion looks weired but it is correct
                int plateColumn = colIndex + 1;

                Well well = plate.getWell(plateColumn, plateRow);
                if (well == null) {
                    well = new Well();

                    well.setPlateRow(plateRow);
                    well.setPlateColumn(plateColumn);
                    well.setPlate(plate);

                    plate.addWell(well);
                }

                Double readout = ScreenImportUtils.parseDouble(envisionTable.get(plateRow, plateColumn));
                if (readout != null) {
                    well.getWellStatistics().put(attributeName, readout);
                }
            }
        }
    }


    private List<Attribute> compileColumnModel(Collection<String> attributeNames) {
        List<Attribute> colAttributes = new ArrayList<Attribute>();


        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));


        for (String attributeName : attributeNames) {
            colAttributes.add(new Attribute(attributeName, DoubleCell.TYPE));
        }

        return colAttributes;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {

        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), getAllowedFileExtensions());
        if (inputFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }

        // first group files into plate-groups
        Map<String, List<File>> plateFiles = splitFilesIntoPlates(inputFiles);
        if (inputFiles.isEmpty()) {
            throw new RuntimeException("No valid envision-files in selection " + inputFiles);
        }

        // compile the column-model
        List<Attribute> colAttributes = compileColumnModel(mergeAttributes(plateFiles));

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }
}