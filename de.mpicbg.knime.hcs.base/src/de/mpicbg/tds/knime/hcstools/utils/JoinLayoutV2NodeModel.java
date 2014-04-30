package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Utils;
import de.mpicbg.tds.core.ExcelLayout;
import de.mpicbg.tds.core.TdsUtils;
import org.knime.core.data.*;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.UniqueNameGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the model implementation of JoinLayoutV2.
 *
 * @author MPI-CBG
 */
public class JoinLayoutV2NodeModel extends AbstractNodeModel {

    public static final String CFG_COL = "column";
    private static final String CFG_COL_DFT = TdsUtils.SCREEN_MODEL_WELL_COLUMN;

    public static final String CFG_ROW = "row";
    private static final String CFG_ROW_DFT = TdsUtils.SCREEN_MODEL_WELL_ROW;

    public static final String CFG_FILE = "layoutfile";

    public static final String CFG_SHEET = "layoutsheet";

    private ExcelLayout excelLayout = null;

    //test column rearranger
    private int plateRowIdx;
    private int plateColumnIdx;

    /**
     * Constructor for the node model.
     */
    protected JoinLayoutV2NodeModel() {

        super(1, 1, true);
        initializeSettings();
    }

    private void initializeSettings() {
        this.addModelSetting(CFG_COL, createPlateColumnSelectionModel());
        this.addModelSetting(CFG_ROW, createPlateRowSelectionModel());
        this.addModelSetting(CFG_FILE, createLayoutFileSelectionModel());
        this.addModelSetting(CFG_SHEET, createLayoutSheetSelectionModel());
    }

    public static SettingsModelString createLayoutSheetSelectionModel() {
        return new SettingsModelString(CFG_SHEET, null);
    }

    public static SettingsModelString createLayoutFileSelectionModel() {
        return new SettingsModelString(CFG_FILE, null);
    }

    public static SettingsModelString createPlateRowSelectionModel() {
        return new SettingsModelString(CFG_ROW, CFG_ROW_DFT);
    }

    public static SettingsModelString createPlateColumnSelectionModel() {
        return new SettingsModelString(CFG_COL, CFG_COL_DFT);
    }

    private DataTableSpec createOutSpec(DataTableSpec inSpec) {

        ColumnRearranger columnRearranger = createColumnRearranger(inSpec);

        return columnRearranger.createSpec();
    }

    /**
     * creates a column rearranger
     *
     * @param inSpec
     * @return
     */
    private ColumnRearranger createColumnRearranger(DataTableSpec inSpec) {

        ColumnRearranger columnRearranger = new ColumnRearranger(inSpec);

        List<DataColumnSpec> cSpecs = new ArrayList<DataColumnSpec>();

        // retrieve layout
        LinkedHashMap<String, Class<?>> layoutLabel = excelLayout.getLabels();

        UniqueNameGenerator uniqueNames = new UniqueNameGenerator(inSpec);

        // add all layout columns (later containing the layout data)
        for (Map.Entry<String, Class<?>> curLayout : layoutLabel.entrySet()) {
            Class curType = curLayout.getValue();
            DataType dType = Utils.mapType(curType);
            // to ensure unique names
            DataColumnSpecCreator dcsc = uniqueNames.newCreator(curLayout.getKey(), dType);
            cSpecs.add(dcsc.createSpec());
        }

        DataColumnSpec[] cSpecArray = new DataColumnSpec[cSpecs.size()];
        cSpecs.toArray(cSpecArray);
        JoinLayoutCellFactory factory = new JoinLayoutCellFactory(cSpecArray);

        columnRearranger.append(factory);
        return columnRearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
                                          final ExecutionContext exec) throws Exception {
        // input table + specs
        BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getDataTableSpec();

        plateRowIdx = inSpec.findColumnIndex(((SettingsModelString) getModelSetting(CFG_ROW)).getStringValue());
        plateColumnIdx = inSpec.findColumnIndex(((SettingsModelString) getModelSetting(CFG_COL)).getStringValue());

        validateExcelFile();

        ColumnRearranger columnRearranger = createColumnRearranger(inSpec);
        BufferedDataTable out = exec.createColumnRearrangeTable(inTable, columnRearranger, exec);

        return new BufferedDataTable[]{out};
    }

    private void validateExcelFile() throws Exception {
        if (excelLayout.hasChanged()) {
            String filename = ((SettingsModelString) getModelSetting(CFG_FILE)).getStringValue();
            String sheet = ((SettingsModelString) getModelSetting(CFG_SHEET)).getStringValue();
            try {
                loadExcelSheet(filename, sheet);
            } catch (InvalidSettingsException e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // input table specs
        DataTableSpec inspec = inSpecs[0];

        // check if the table contains numeric columns which can describe plateRow and plate Column
        if (!inspec.containsCompatibleType(DoubleValue.class))
            throw new InvalidSettingsException("Input table requires at least one column with integer values");

        // no autguessing needed

        // filename of the excel layout file
        String filename = ((SettingsModelString) getModelSetting(CFG_FILE)).getStringValue();
        // sheet name of the layout
        String sheet = ((SettingsModelString) getModelSetting(CFG_SHEET)).getStringValue();

        // not yet fully configured
        if (filename == null || sheet == null)
            throw new InvalidSettingsException("No Excel file has been selected.");

        //
        if (excelLayout == null) loadExcelSheet(filename, sheet);

        DataTableSpec outSpec = createOutSpec(inspec);

        return new DataTableSpec[]{outSpec};
    }

    private void loadExcelSheet(String filename, String sheet) throws InvalidSettingsException {
        try {
            // open excel file
            excelLayout = new ExcelLayout(filename);
            // try to set the sheet
            excelLayout.setSheetName(sheet);
            // parse the sheet for layout labels
            excelLayout.parseLayoutLabels();
            // parse each layout to guess its data type
            excelLayout.parseLayoutContent();
        } catch (IOException e) {
            excelLayout = null;
            throw new InvalidSettingsException(e.getMessage());
        } catch (ExcelLayout.ExcelLayoutException e) {
            excelLayout = null;
            throw new InvalidSettingsException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // super method
        super.validateSettings(settings);

        // check whether the columns containing plate row and plate column identifier are different
        if (settings.containsKey(CFG_ROW) && settings.containsKey(CFG_COL)) {
            if (settings.getString(CFG_ROW).equals(settings.getString(CFG_COL)))
                throw new InvalidSettingsException("Plate row and plate column identifier cannot be in the same column");
        }

        // test if filename and sheet name are set and can be loaded without errors
        if (settings.containsKey(CFG_FILE) && settings.containsKey(CFG_SHEET)) {
            String filename = settings.getString(CFG_FILE);
            String sheet = settings.getString(CFG_SHEET);

            loadExcelSheet(filename, sheet);

        } else
            throw new InvalidSettingsException("No Excel file has been selected.");

    }

    /**
     * method returns a data cell with the layout value of a given layout and a given well position
     * it's used by the JoinLayoutCellFactory
     *
     * @param label
     * @param plateRow    a double value which represents the row of a well position. make sure before that it can be cast to int
     * @param plateColumn a double value which represents the column of a well position. make sure before that it can be cast to int
     * @return
     */
    private DataCell getLayoutCell(Map.Entry<String, Class<?>> label, double plateRow, double plateColumn) {

        // get data type
        Class curType = label.getValue();
        DataType dType = Utils.mapType(curType);

        // retrieve the value of this position (or null otherwise)
        String value = excelLayout.getLayoutValue(label.getKey(), (int) plateRow, (int) plateColumn);
        DataCell cell = null;

        // create a new data cell with appropriate data type
        // casts should work as each value already has been testet if it fits to this data type
        if (dType.equals(StringCell.TYPE)) {
            cell = (value != null) ? new StringCell(value) : DataType.getMissingCell();
        } else {
            if (value == null || value == "") cell = DataType.getMissingCell();
            else {
                double numericValue = Double.valueOf(value);
                if (dType.equals(DoubleCell.TYPE)) cell = new DoubleCell(numericValue);
                if (dType.equals(IntCell.TYPE)) cell = new IntCell((int) numericValue);
            }
        }

        return cell;
    }

    /**
     * Inner class to provide the cell factory for ColumnRearranger
     */
    private class JoinLayoutCellFactory extends AbstractCellFactory {
        public JoinLayoutCellFactory(DataColumnSpec[] columnSpecs) {
            super(columnSpecs);
        }

        @Override
        public DataCell[] getCells(DataRow dataRow) {
            LinkedHashMap<String, Class<?>> labels = excelLayout.getLabels();

            DataRow curRow = dataRow;

            // retrieve well position cells
            DataCell plateRowCell = curRow.getCell(plateRowIdx);
            DataCell plateColumnCell = curRow.getCell(plateColumnIdx);

            // do not skip the rows with missing well identifier, but create missing layout cells instead
            double plateRow = -1;
            double plateColumn = -1;
            if (!(plateRowCell.isMissing() || plateColumnCell.isMissing())) {
                // retrieve well position
                plateRow = ((DoubleValue) plateRowCell).getDoubleValue();
                plateColumn = ((DoubleValue) plateColumnCell).getDoubleValue();
            }

            // check whether plateRow and plateColumn have integer values (missing values will be created n this case
            String warnMessage = null;

            if (Math.floor(plateColumn) != plateColumn)
                warnMessage = "Plate column identifier in table row " + curRow.getKey().getString() + "(" + plateColumn + ") is not whole-number.";
            if (Math.floor(plateRow) != plateRow)
                warnMessage = "Plate row identifier in table row " + curRow.getKey().getString() + "(" + plateRow + ") is not whole-number.";

            // empty vector of data cells
            DataCell[] appendedCells = new DataCell[labels.size()];

            // return missing cell if a warning has been generated for this row otherwise return data cells with layout values
            if (warnMessage != null) {
                setWarningMessage(warnMessage);
                for (int i = 0; i < appendedCells.length; i++) appendedCells[i] = DataType.getMissingCell();
            } else {
                // retrieve the layout values for this position
                int pos = 0;
                for (Map.Entry<String, Class<?>> label : labels.entrySet()) {
                    appendedCells[pos] = getLayoutCell(label, plateRow, plateColumn);
                    pos++;
                }
            }

            return appendedCells;
        }
    }


}

