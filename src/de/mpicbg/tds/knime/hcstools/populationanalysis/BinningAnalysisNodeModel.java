package de.mpicbg.tds.knime.hcstools.populationanalysis;

import de.mpicbg.tds.core.math.BinningAnalysis;
import de.mpicbg.tds.core.math.BinningData;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import org.knime.core.data.*;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This is the model implementation of BinningAnalysis.
 * will be done later
 *
 * @author MPI-CBG
 */
public class BinningAnalysisNodeModel extends AbstractNodeModel {

    public static final String CFG_AGGR = "groupBy";
    //private static final String CFG_AGGR_DFT = TdsUtils.SCREEN_MODEL_WELL;  TODO: use this as soon asthe hcscore was compiled again
    private static final String CFG_AGGR_DFT = "well";

    public static final String CFG_COLUMN = "selectedCols";

    public static final String CFG_BIN = "nBins";
    private static final Integer CFG_BIN_DFT = 5;
    private static final Integer CFG_BIN_MIN = 2;
    private static final Integer CFG_BIN_MAX = Integer.MAX_VALUE;

    public static final String CFG_REFCOLUMN = "refCol";

    public static final String CFG_REFSTRING = "refString";

    // provides the row id for the output table
    private int rowCount;

    /**
     * Constructor for the node model.
     */
    protected BinningAnalysisNodeModel() {
        super(1, 1, true);
        initializeSettings();
    }

    private void initializeSettings() {
        this.addModelSetting(CFG_COLUMN, createColumnSelectionModel());
        this.addModelSetting(CFG_BIN, createBinSelectionModel());
        this.addModelSetting(CFG_REFCOLUMN, createRefColumnSelectionModel());
        this.addModelSetting(CFG_REFSTRING, createRefStringSelectionModel());
        this.addModelSetting(CFG_AGGR, createAggregationSelectionModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
                                          final ExecutionContext exec) throws Exception {

        BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getSpec();

        // read current settings
        String aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        List<String> selColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN)).getIncludeList();
        boolean hasRefColumn = true;
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        if (refColumn == null) hasRefColumn = false;
        String refString = null;
        if (hasRefColumn) refString = ((SettingsModelString) getModelSetting(CFG_REFSTRING)).getStringValue();
        int nBins = ((SettingsModelInteger) getModelSetting(CFG_BIN)).getIntValue();

        int aggIdx = inSpec.findColumnIndex(aggColumn);
        int refIdx = -1;
        if (hasRefColumn) refIdx = inSpec.findColumnIndex(refColumn);

        List<Double> doubleList;

        //ExecutionContext binExec = exec.createSubExecutionContext(0.5);
        int i = 1;
        int n = selColumns.size();
        double progress = 0.0;
        int countMissing;

        // new data table
        BufferedDataContainer con = exec.createDataContainer(createOutSpec(inTable.getSpec()));
        rowCount = 0;

        // iterate for each parameter through the table and feed the hash maps
        for (String col : selColumns) {
            int colIdx = inSpec.findColumnIndex(col);
            countMissing = 0;

            // parameter / aggregation string / values
            HashMap<Object, List<Double>> refData = new HashMap<Object, List<Double>>();
            HashMap<Object, List<Double>> allData = new HashMap<Object, List<Double>>();

            for (DataRow row : inTable) {

                DataCell aggCell = row.getCell(aggIdx);
                String aggString = null;
                if (!aggCell.isMissing()) aggString = ((StringCell) aggCell).getStringValue();

                String label = null;
                if (hasRefColumn) {
                    DataCell labelCell = row.getCell(refIdx);
                    if (!aggCell.isMissing()) label = ((StringCell) labelCell).getStringValue();
                }

                DataCell valueCell = row.getCell(colIdx);
                Double value = null;
                if (!valueCell.isMissing()) value = ((DoubleCell) valueCell).getDoubleValue();

                // skip data row if
                // - aggregation information is not available
                // - data value is missing
                // - reference column was selected but reference label is not available
                if (aggString == null || value == null || (hasRefColumn && label == null)) {
                    countMissing++;
                    continue;
                }

                // fill list of reference data
                if (hasRefColumn) {

                    if (label.equals(refString)) {

                        if (refData.containsKey(aggString)) doubleList = refData.get(aggString);
                        else doubleList = new ArrayList<Double>();

                        doubleList.add(value);
                        refData.put(aggString, doubleList);
                    }
                }

                // fill list with all data
                if (allData.containsKey(aggString)) doubleList = allData.get(aggString);
                else doubleList = new ArrayList<Double>();

                doubleList.add(value);
                allData.put(aggString, doubleList);

                // check whether execution was canceled
                exec.checkCanceled();
            }

            // if there is no reference column selected, take the whole dataset as reference
            if (!hasRefColumn) {
                refData = allData;
            }
            // if the reference label does not contain any data (domain available though no data in the table
            if (refData.isEmpty()) {
                setWarningMessage("there is no reference data available for " + col + " (" + countMissing + " rows were skipped because of missing values)");
                continue;
            }

            // do the binning analysis on the collected data
            BinningAnalysis binAnalysis = new BinningAnalysis(refData, 10, col);
            HashMap<Object, List<BinningData>> ret = binAnalysis.getZscore(allData);

            // fill binning data into the new table
            for (Object aggLabel : ret.keySet()) {
                DataRow[] newRows = createDataRow(col, (String) aggLabel, ret.get(aggLabel));
                for (DataRow row : newRows) {
                    con.addRowToTable(row);
                }
            }

            // set progress
            progress = progress + 1.0 / n;
            i++;
            exec.setProgress(progress, "Binning done for parameter " + col + " (" + i + "/" + n + ")");
        }

        con.close();

        return new BufferedDataTable[]{con.getTable()};
    }

    /**
     * returns an array with table rows created from binning data
     *
     * @param col
     * @param aggLabel
     * @param binningDatas
     * @return
     */
    private DataRow[] createDataRow(String col, String aggLabel, List<BinningData> binningDatas) {
        DataRow[] newRows = new DataRow[binningDatas.size()];

        int i = 0;
        for (BinningData binData : binningDatas) {
            List<DataCell> cells = new ArrayList<DataCell>();

            cells.add(new StringCell(col));
            cells.add(new StringCell(aggLabel));
            cells.add(new StringCell(binData.getInterval().getLabel()));
            cells.add(new DoubleCell(binData.getPercentage()));
            double zval = binData.getZscore();
            if (Double.isNaN(zval)) cells.add(DataType.getMissingCell());
            else cells.add(new DoubleCell(zval));
            cells.add(new IntCell((int) binData.getCount()));

            DataRow currentRow = new DefaultRow(new RowKey(Integer.toString(rowCount)), cells);
            newRows[i] = currentRow;
            i++;
            rowCount++;
        }
        return newRows;
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
        DataTableSpec inSpec = inSpecs[0];

        // check if the input table contains numeric columns and string columns
        if (!inSpec.containsCompatibleType(DoubleValue.class))
            throw new InvalidSettingsException("input table requires at least one numeric column (Double or Integer)");
        if (!inSpec.containsCompatibleType(StringValue.class))
            throw new InvalidSettingsException("input table requires at least one column with nominal values (String)");

        runAutoGuessing(inSpec);

        DataTableSpec outSpec = createOutSpec(inSpec);

        return new DataTableSpec[]{outSpec};
    }

    /**
     * Auto guess aggregation column as well as colun selection
     *
     * @param inSpec
     */
    private void runAutoGuessing(DataTableSpec inSpec) {

        // auto guess aggregation column if default not available
        SettingsModelString aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR));
        if (!inSpec.containsName(aggColumn.getStringValue())) {
            Iterator<DataColumnSpec> it = inSpec.iterator();
            while (it.hasNext()) {
                DataColumnSpec cSpec = it.next();
                if (cSpec.getType().isCompatible(StringValue.class)) {
                    aggColumn.setStringValue(cSpec.getName());
                    addModelSetting(CFG_AGGR, aggColumn);
                    setWarningMessage("Auto-Guessing aggregation column. Please check configuration settings before execution");
                }
            }
        }

        // auto guess a column selection if no setting available
        SettingsModelFilterString selColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN));
        if (selColumns.getIncludeList().size() == 0) {
            List<String> inclColumns = new ArrayList<String>();
            List<String> exclColumns = new ArrayList<String>();
            for (DataColumnSpec cSpec : inSpec) {
                if (cSpec.getType().isCompatible(DoubleValue.class)) inclColumns.add(cSpec.getName());
            }
            selColumns.setNewValues(inclColumns, exclColumns, false);
            addModelSetting(CFG_COLUMN, selColumns);
            setWarningMessage("Auto-Guessing column selection. Please check configuration settings before execution.");
        }
    }

    /**
     * generates the table specs for the ouput table
     *
     * @param inSpec
     * @return new specs
     */
    private DataTableSpec createOutSpec(DataTableSpec inSpec) {
        DataColumnSpec[] columnArray = new DataColumnSpec[6];

        DataColumnSpecCreator colCreator;

        colCreator = new DataColumnSpecCreator("parameter", StringCell.TYPE);
        columnArray[0] = colCreator.createSpec();

        String aggrColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        colCreator = new DataColumnSpecCreator(inSpec.getColumnSpec(aggrColumn));
        columnArray[1] = colCreator.createSpec();

        colCreator = new DataColumnSpecCreator("interval", StringCell.TYPE);
        columnArray[2] = colCreator.createSpec();

        colCreator = new DataColumnSpecCreator("percentage", DoubleCell.TYPE);
        columnArray[3] = colCreator.createSpec();

        colCreator = new DataColumnSpecCreator("z-score", DoubleCell.TYPE);
        columnArray[4] = colCreator.createSpec();

        colCreator = new DataColumnSpecCreator("count", IntCell.TYPE);
        columnArray[5] = colCreator.createSpec();

        return new DataTableSpec("binned Data", columnArray);  //To change body of created methods use File | Settings | File Templates.
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // check that at least one numeric column has been selected
        if (settings.containsKey(CFG_COLUMN)) {
            String[] selectedColumns = settings.getNodeSettings(CFG_COLUMN).getStringArray("InclList");
            if (selectedColumns.length < 1)
                throw new InvalidSettingsException("at least one numeric column has to be selected");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
                                 final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
                                 final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save
    }

    /**
     * selection model for numeric columns
     *
     * @return selection model
     */
    public static SettingsModelFilterString createColumnSelectionModel() {

        return new SettingsModelFilterString(CFG_COLUMN);
    }

    /**
     * selection model for number of bins
     *
     * @return selection model
     */
    public static SettingsModelNumber createBinSelectionModel() {

        return new SettingsModelIntegerBounded(CFG_BIN, CFG_BIN_DFT, CFG_BIN_MIN, CFG_BIN_MAX);
    }

    /**
     * selection model for reference column
     *
     * @return selection model
     */
    public static SettingsModelString createRefColumnSelectionModel() {

        return new SettingsModelString(CFG_REFCOLUMN, null);
    }

    /**
     * selection model for reference label
     *
     * @return selection model
     */
    public static SettingsModelString createRefStringSelectionModel() {

        return new SettingsModelString(CFG_REFSTRING, null);
    }

    /**
     * selection model for aggregating object based data
     *
     * @return selection model
     */
    public static SettingsModelString createAggregationSelectionModel() {

        return new SettingsModelString(CFG_AGGR, CFG_AGGR_DFT);
    }
}

