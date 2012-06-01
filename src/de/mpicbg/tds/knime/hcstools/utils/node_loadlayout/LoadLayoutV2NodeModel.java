package de.mpicbg.tds.knime.hcstools.utils.node_loadlayout;

import de.mpicbg.tds.core.ExcelLayout;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Utils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is the model implementation of LoadLayoutV2.
 *
 * @author
 */
public class LoadLayoutV2NodeModel extends AbstractNodeModel {

    public static final String CFG_FILE = "layoutfile";

    public static final String CFG_SHEET = "layoutsheet";

    private ExcelLayout excelLayout = null;

    /**
     * Constructor for the node model.
     */
    protected LoadLayoutV2NodeModel() {

        super(0, 1, true);
        initializeSettings();
    }

    private void initializeSettings() {
        this.addModelSetting(CFG_FILE, createLayoutFileSelectionModel());
        this.addModelSetting(CFG_SHEET, createLayoutSheetSelectionModel());
    }

    public static SettingsModelString createLayoutSheetSelectionModel() {
        return new SettingsModelString(CFG_SHEET, null);
    }

    public static SettingsModelString createLayoutFileSelectionModel() {
        return new SettingsModelString(CFG_FILE, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
                                          final ExecutionContext exec) throws Exception {

        validateExcelFile();

        BufferedDataContainer dataCon = exec.createDataContainer(createOutSpec());

        LinkedHashMap<String, Class<?>> layoutLabel = excelLayout.getLabels();

        HashMap<Integer, HashMap<Integer, HashMap<String, String>>> layoutMap = excelLayout.getLayout();

        int rowIdx = 0;

        for (Integer curRow : layoutMap.keySet()) {
            for (Integer curCol : layoutMap.keySet()) {
                DataCell[] rowCells = new DataCell[layoutLabel.size() + 2];

                rowCells[0] = new IntCell(curRow);
                rowCells[1] = new IntCell(curCol);

                int i = 2;

                HashMap<String, String> wellMap = layoutMap.get(curRow).get(curCol);

                for (String curLabel : layoutLabel.keySet()) {
                    String value = wellMap.get(curLabel);

                    // get data type
                    Class curType = layoutLabel.get(curLabel);
                    DataType dType = Utils.mapType(curType);

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
                    rowCells[i] = cell;
                    i++;
                }
                dataCon.addRowToTable(new DefaultRow(RowKey.createRowKey(rowIdx), rowCells));
                rowIdx++;
            }
        }
        dataCon.close();

        return new BufferedDataTable[]{dataCon.getTable()};
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

        // filename of the excel layout file
        String filename = ((SettingsModelString) getModelSetting(CFG_FILE)).getStringValue();
        // sheet name of the layout
        String sheet = ((SettingsModelString) getModelSetting(CFG_SHEET)).getStringValue();

        // not yet fully configured
        if (filename == null || sheet == null)
            throw new InvalidSettingsException("No Excel file has been selected.");

        //
        if (excelLayout == null) loadExcelSheet(filename, sheet);

        DataTableSpec outSpec = createOutSpec();

        return new DataTableSpec[]{outSpec};
    }

    private DataTableSpec createOutSpec() {
        // retrieve layout
        LinkedHashMap<String, Class<?>> layoutLabel = excelLayout.getLabels();

        String[] columnNames = new String[layoutLabel.size() + 2];
        DataType[] columnDataTypes = new DataType[layoutLabel.size() + 2];

        columnNames[0] = "plateRow";
        columnDataTypes[0] = IntCell.TYPE;

        columnNames[1] = "plateColumn";
        columnDataTypes[1] = IntCell.TYPE;

        int i = 2;

        // add all layout columns (later containing the layout data)
        for (Map.Entry<String, Class<?>> curLayout : layoutLabel.entrySet()) {
            Class curType = curLayout.getValue();
            DataType dType = Utils.mapType(curType);

            columnNames[i] = curLayout.getKey();
            columnDataTypes[i] = dType;

            i++;
        }

        return new DataTableSpec("Layout table", columnNames, columnDataTypes);
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
        /// super method
        super.validateSettings(settings);

        // test if filename and sheet name are set and can be loaded without errors
        if (settings.containsKey(CFG_FILE) && settings.containsKey(CFG_SHEET)) {
            String filename = settings.getString(CFG_FILE);
            String sheet = settings.getString(CFG_SHEET);

            loadExcelSheet(filename, sheet);

        } else
            throw new InvalidSettingsException("No Excel file has been selected.");

    }

}

