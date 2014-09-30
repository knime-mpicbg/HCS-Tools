package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.hcs.base.utils.ExcelUtils;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.knime.knutils.ui.FileSelectPanel;
import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.util.StringTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
import java.util.*;
import java.util.List;


/**
 * This is the model implementation of GeniusPro-file reader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class GeniusProFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();
    public static final String[] GENIUSPRO_FILE_SUFFIX = new String[]{"xlsx", "xls"};


    public GeniusProFileReader() {
        super(0, 1);

        addSetting(propInputDir);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        List<File> geniusProFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), GENIUSPRO_FILE_SUFFIX);

        List<Attribute> attributes = compileAttributes(geniusProFiles);

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(attributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int fileCounter = 0;

        int tableRowCounter = 0;


        for (File inputFile : geniusProFiles) {
            logger.info("reading file " + inputFile);


            String fileName = inputFile.getName();
            String barcode = fileName.endsWith("x") ? fileName.replace(".xlsx", "") : fileName.replace(".xls", "");


            Map<String, StringTable> data = readData(inputFile);


            // iterate over all wells in the opera-data-strucuture

            if (data.isEmpty()) {
                throw new RuntimeException("Could not read data form " + inputFile);
            }

            StringTable firstTable = data.values().iterator().next();


            for (int rowIndex = 1; rowIndex < firstTable.getHeight(); rowIndex++) {
                for (int colIndex = 1; colIndex < firstTable.getWidth(); colIndex++) {

                    DataCell[] knimeRow = new DataCell[3 + data.size()]; // +3 because of barcode and row/col positions


                    // start with the barcode column
                    knimeRow[0] = attributes.get(0).createCell(barcode);

                    knimeRow[1] = attributes.get(1).createCell(rowIndex);
                    knimeRow[2] = attributes.get(2).createCell(colIndex);

                    // now add the actual readout
                    int featureCounter = 0;
                    for (String s : data.keySet()) {
                        StringTable readoutTable = data.get(s);

                        knimeRow[3 + featureCounter] = attributes.get(3 + featureCounter).createCell(readoutTable.get(rowIndex, colIndex));

                        featureCounter++;
                    }

                    DataRow tableRow = new DefaultRow(new RowKey("" + tableRowCounter++), knimeRow);
                    container.addRowToTable(tableRow);
                }
            }

            BufTableUtils.updateProgress(exec, fileCounter++, geniusProFiles.size());
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};

    }


    private TreeMap<String, StringTable> readData(File inputFile) {
        TreeMap<String, StringTable> tables = new TreeMap<String, StringTable>();

        int startSearchInRow = 0;

        int featureCounter = 1;

        Sheet sheet = ExcelUtils.openWorkSheet(inputFile, 0);

        while (true) {
            Point nextPlatePosition = StringTable.findNextPlatePosition(sheet, new Point(1, startSearchInRow));
            if (nextPlatePosition == null)
                break;

            Rectangle plateDim = StringTable.guessPlateBounds(sheet, nextPlatePosition);
            if (plateDim == null)
                break;

            StringTable readoutTable = StringTable.readStringGridFromExcel(plateDim, sheet);

            tables.put("Feature " + featureCounter++, readoutTable);

            startSearchInRow = (int) (plateDim.getY() + plateDim.getHeight() - 3);
        }

        return tables;
    }


    private List<Attribute> compileAttributes(List<File> geniusProFiles) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
        attributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));

        attributes.addAll(getFeatureAttributes(geniusProFiles.get(0)));

        return attributes;
    }


    private List<Attribute> getFeatureAttributes(File file) {
        TreeMap<String, StringTable> map = readData(file);


        List<Attribute> featureAttrs = new ArrayList<Attribute>();
        for (int i = 0; i < map.size(); i++) {
            featureAttrs.add(new Attribute("Feature " + (i + 1), DoubleCell.TYPE));

        }


        return featureAttrs;
    }


    private Attribute getAquisitionModeAttribute(List<File> genProFiles) {
        Set<String> acModes = new HashSet<String>();


        for (File file : genProFiles) {
            Sheet sheet = ExcelUtils.openWorkSheet(file, 0);

            String measModePrefix = "Measurement mode: ";

            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().startsWith(measModePrefix)) {
                    acModes.add(cell.getStringCellValue().replace(measModePrefix, ""));
                }
            }
        }

        if (acModes.isEmpty()) {
            throw new RuntimeException("Could not determine acquisition mode from input files");
        }

        if (acModes.size() > 1) {
            throw new RuntimeException("Inconsistent acquisition modes in input files");
        }

        return new Attribute(acModes.iterator().next(), DoubleCell.TYPE);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<File> geniusProFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), GENIUSPRO_FILE_SUFFIX);
        if (geniusProFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }


        List<Attribute> colAttributes = compileAttributes(geniusProFiles);

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }
}
