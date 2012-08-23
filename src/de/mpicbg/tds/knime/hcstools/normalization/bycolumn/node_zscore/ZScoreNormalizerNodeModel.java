package de.mpicbg.tds.knime.hcstools.normalization.bycolumn.node_zscore;

import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import de.mpicbg.tds.knime.hcstools.normalization.bycolumn.AbstractNormNodeModel;
import de.mpicbg.tds.knime.hcstools.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.tds.knime.hcstools.utils.ExtDescriptiveStats;
import de.mpicbg.tds.knime.hcstools.utils.MadStatistic;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.*;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.UniqueNameGenerator;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 11:39 AM
 */
public class ZScoreNormalizerNodeModel extends AbstractNormNodeModel {

    /**
     * stores z-score statistik for each group; <group, <parameter, statistic>>
     */
    HashMap<String, HashMap<String, ZScoreStats>> statisticTable;

    // index of aggregation column and reference column
    int aggIdx;
    int refIdx;

    /**
     * list of parameter sets (see processing options) and their column index
     */
    List<HashMap<String, Integer>> columnList;

    /**
     * constructor with two ouput ports
     */
    public ZScoreNormalizerNodeModel() {
        super(2);
        CFG_SUFFIX_DFT = ".(z-score)";
        initializeSettings();
    }

    /**
     * add model settings
     */
    @Override
    protected void initializeSettings() {
        addModelSetting(AbstractNormNodeModel.CFG_AGGR, AbstractNormNodeModel.createAggregationSM());
        addModelSetting(AbstractNormNodeModel.CFG_COLUMN_SELECTION, AbstractNormNodeModel.createColumnFilterSM());
        addModelSetting(AbstractNormNodeModel.CFG_REFCOLUMN, AbstractNormNodeModel.createRefColumnSM());
        addModelSetting(AbstractNormNodeModel.CFG_REFSTRING, AbstractNormNodeModel.createRefStringSM());
        addModelSetting(AbstractNormNodeModel.CFG_REPLACE_VALUES, AbstractNormNodeModel.createReplaceValuesSM());
        addModelSetting(AbstractNormNodeModel.CFG_ROBUST_STATS, AbstractNormNodeModel.createRobustStatsSM());
        addModelSetting(AbstractNormNodeModel.CFG_SUFFIX, AbstractNormNodeModel.createSuffixSM());
        addModelSetting(AbstractNormNodeModel.CFG_USEOPT, AbstractNormNodeModel.createUseProcessingOptionsSM());
        addModelSetting(AbstractNormNodeModel.CFG_OPT, AbstractNormNodeModel.createProcessingOptionsSM());
    }

    /**
     * statistic table contains group (optional), reference (optional), column name, mean/median, sd/mad, n, n missing
     *
     * @param inSpec
     * @return table specs for statistic table
     */
    @Override
    protected DataTableSpec createOutSpecStats(DataTableSpec inSpec) {
        String aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        boolean useRobustStats = ((SettingsModelBoolean) getModelSetting(CFG_ROBUST_STATS)).getBooleanValue();

        List<DataColumnSpec> columnSpecList = new ArrayList<DataColumnSpec>();
        Set<String> reservedNames = new HashSet<String>();

        if (aggColumn != null) {
            columnSpecList.add(inSpec.getColumnSpec(aggColumn));
            reservedNames.add(aggColumn);
        }
        if (refColumn != null) {
            columnSpecList.add(inSpec.getColumnSpec(refColumn));
            reservedNames.add(refColumn);
        }

        UniqueNameGenerator uniqueNames = new UniqueNameGenerator(reservedNames);

        columnSpecList.add(uniqueNames.newCreator("column name", StringCell.TYPE).createSpec());
        columnSpecList.add(uniqueNames.newCreator(useRobustStats ? "median" : "mean", DoubleCell.TYPE).createSpec());
        columnSpecList.add(uniqueNames.newCreator(useRobustStats ? "median absolute deviation" : "standard deviation", DoubleCell.TYPE).createSpec());
        columnSpecList.add(uniqueNames.newCreator("n", IntCell.TYPE).createSpec());
        columnSpecList.add(uniqueNames.newCreator("n missing", IntCell.TYPE).createSpec());

        DataColumnSpec[] columnSpecArray = new DataColumnSpec[columnSpecList.size()];
        columnSpecArray = columnSpecList.toArray(columnSpecArray);

        return new DataTableSpec("Z-score Statistics", columnSpecArray);
    }

    /**
     * @param inSpecs
     * @return
     * @throws InvalidSettingsException
     */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inSpec = inSpecs[0];

        checkForNumericColumns(inSpec);
        //TODO: neccessary?
        checkForNominalColumns(inSpec);

        runAutoGuessing(inSpec);

        // throw an exception if selected columns are identical (except if both are set to <none>)
        String aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        if (aggColumn != null && aggColumn.equals(refColumn))
            throw new InvalidSettingsException("aggregation column and reference column must not be the same");

        // create ouput specs
        DataTableSpec outSpec1 = createOutSpec(inSpec);
        DataTableSpec outSpec2 = createOutSpecStats(inSpec);

        return new DataTableSpec[]{outSpec1, outSpec2};    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * @param cSpecArray
     * @return
     */
    @Override
    protected CellFactory getCellFactory(DataColumnSpec[] cSpecArray) {
        return new ZScoreNormalizerCellFactory(cSpecArray);
    }

    /**
     * auto guess aggregation column, reference column and data columns
     *
     * @param inSpec
     */
    private void runAutoGuessing(DataTableSpec inSpec) {
        autoGuessAggreagtionColumn(inSpec);
        autoGuessReferenceColumn(inSpec);
        autoGuessColumnSelection(inSpec);
    }

    /**
     * @param inData
     * @param exec
     * @return
     * @throws Exception
     */
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getSpec();

        // get settings
        String aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        String refString = ((SettingsModelString) getModelSetting(CFG_REFSTRING)).getStringValue();
        List<String> inclColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN_SELECTION)).getIncludeList();
        boolean useRobustStats = ((SettingsModelBoolean) getModelSetting(CFG_ROBUST_STATS)).getBooleanValue();
        boolean useOpt = ((SettingsModelBoolean) getModelSetting(CFG_USEOPT)).getBooleanValue();
        int procOpt = ((SettingsModelIntegerBounded) getModelSetting(CFG_OPT)).getIntValue();

        aggIdx = inSpec.findColumnIndex(aggColumn);
        refIdx = inSpec.findColumnIndex(refColumn);
        boolean hasAggColumn = (aggColumn != null);
        boolean hasRefColumn = (refColumn != null);

        //put parameters and their index to process at once into a list of hashmaps
        columnList = new ArrayList<HashMap<String, Integer>>();
        HashMap<String, Integer> subList = new HashMap<String, Integer>();
        if (useOpt) {
            for (int i = 0; i < inclColumns.size(); i++) {
                String col = inclColumns.get(i);
                int colIdx = inSpec.findColumnIndex(col);
                if (i % procOpt == 0 && i > 0) {
                    columnList.add(subList);
                    subList = new HashMap<String, Integer>();
                    subList.put(col, colIdx);
                } else {
                    subList.put(col, colIdx);
                }
            }
            columnList.add(subList); // add last list (might contain less entries)
        } else {
            for (String col : inclColumns) {
                subList.put(col, inSpec.findColumnIndex(col));
            }
            columnList.add(subList);
        }

        // collect statistic data
        statisticTable = new HashMap<String, HashMap<String, ZScoreStats>>();

        // iterate on column hashmap to collect data of multiple columns at once
        for (HashMap<String, Integer> curList : columnList) {
            // reference data: group, parameter, values
            HashMap<String, HashMap<String, List<Double>>> refData = new HashMap<String, HashMap<String, List<Double>>>();

            // iterate over the table
            for (DataRow row : inTable) {
                DataCell aggCell = null;
                DataCell refCell = null;

                //current group string
                String curGroup = null;
                boolean isReference = false;

                if (hasAggColumn) {
                    aggCell = row.getCell(aggIdx);
                    curGroup = (aggCell.isMissing()) ? null : ((StringCell) aggCell).getStringValue();
                }
                if (hasRefColumn) {
                    refCell = row.getCell(refIdx);
                    isReference = (!refCell.isMissing() && ((StringValue) refCell).getStringValue().equals(refString));
                }

                // iterate over columns
                for (String curColumn : curList.keySet()) {

                    if (!refData.containsKey(curGroup)) {
                        HashMap<String, List<Double>> subData = new HashMap<String, List<Double>>();
                        refData.put(curGroup, subData);
                    }
                    if (!refData.get(curGroup).containsKey(curColumn)) {
                        List<Double> doubleList = new ArrayList<Double>();
                        refData.get(curGroup).put(curColumn, doubleList);
                    }

                    DataCell valueCell = row.getCell(curList.get(curColumn));
                    if (hasRefColumn && isReference || !hasRefColumn) {
                        Double value = valueCell.isMissing() ? Double.NaN : ((DoubleValue) valueCell).getDoubleValue();
                        refData.get(curGroup).get(curColumn).add(value);
                    }

                }
            } // finish iteration over the table
            calculateStatistics(refData, useRobustStats);
        }

        // create KNIME table of statistics
        BufferedDataContainer statContainer = exec.createDataContainer(createOutSpecStats(inSpec), true);
        int curRowIdx = 1;
        for (String curGroup : statisticTable.keySet()) {
            HashMap<String, ZScoreStats> stat = statisticTable.get(curGroup);
            for (String curColumn : stat.keySet()) {
                List<DataCell> cellList = new ArrayList<DataCell>();

                if (hasAggColumn) {
                    if (curGroup != null) cellList.add(new StringCell(curGroup));
                    else cellList.add(DataType.getMissingCell());
                }

                if (hasRefColumn) cellList.add(new StringCell(refString));
                cellList.add(new StringCell(curColumn));

                double mean = stat.get(curColumn).getMean_median();
                if (Double.isNaN(mean)) cellList.add(DataType.getMissingCell());
                else cellList.add(new DoubleCell(mean));

                double sd = stat.get(curColumn).getSd_mad();
                if (Double.isNaN(sd)) cellList.add(DataType.getMissingCell());
                else cellList.add(new DoubleCell(stat.get(curColumn).getSd_mad()));

                cellList.add(new IntCell(stat.get(curColumn).getnSamples()));

                cellList.add(new IntCell(stat.get(curColumn).getnMissing()));

                DataRow row = new DefaultRow(RowKey.createRowKey(curRowIdx), cellList);
                statContainer.addRowToTable(row);
                curRowIdx++;
            }
        }
        statContainer.close();

        // create KNIME table with normalized values
        ColumnRearranger columnRearranger = createColumnRearranger(inSpec);
        BufferedDataTable zScoreTable = exec.createColumnRearrangeTable(inTable, columnRearranger, exec);

        return new BufferedDataTable[]{zScoreTable, statContainer.getTable()};
    }

    /**
     * for each group and each column the statistics of the reference data is calculated and stored
     *
     * @param data
     * @param useRobustStats
     * @throws MadStatistic.IllegalMadFactorException
     *
     */
    private void calculateStatistics(HashMap<String, HashMap<String, List<Double>>> data, boolean useRobustStats) throws MadStatistic.IllegalMadFactorException {

        // get preference data
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        int minSamplesMean = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_MEANS);
        int minSamplesSd = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_DISPERSION);
        double madScalingFactor = prefStore.getDouble(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR);

        // group, list of warnings
        HashMap<String, StringBuilder> meanWarnings = new HashMap<String, StringBuilder>();
        HashMap<String, StringBuilder> sdWarnings = new HashMap<String, StringBuilder>();

        //iterate over groups
        for (String curGroup : data.keySet()) {

            if (!statisticTable.containsKey(curGroup)) statisticTable.put(curGroup, new HashMap<String, ZScoreStats>());
            long n = 0;

            //iterate over columns
            for (String curColumn : data.get(curGroup).keySet()) {
                ZScoreStats stats = new ZScoreStats().init(data.get(curGroup).get(curColumn), madScalingFactor, useRobustStats);
                n = stats.getnSamples();

                // ensure sample size settings
                if (!stats.hasEnoughSamples(minSamplesMean)) {
                    if (!meanWarnings.containsKey(curGroup))
                        meanWarnings.put(curGroup, new StringBuilder(curColumn + "(" + n + ")"));
                    else meanWarnings.get(curGroup).append(", " + curColumn + "(" + n + ")");
                }
                if (!stats.hasEnoughSamples(minSamplesSd)) {
                    if (!sdWarnings.containsKey(curGroup))
                        sdWarnings.put(curGroup, new StringBuilder(curColumn + "(" + n + ")"));
                    else sdWarnings.get(curGroup).append(", " + curColumn + "(" + n + ")");
                }

                statisticTable.get(curGroup).put(curColumn, stats);
            }

            // throw warnings
            if (meanWarnings.containsKey(curGroup))
                createWarning(curGroup, ExtDescriptiveStats.WARN_NOT_ENOUGH_MEAN_SAMPLES, meanWarnings.get(curGroup), minSamplesMean, n);
            if (sdWarnings.containsKey(curGroup))
                createWarning(curGroup, ExtDescriptiveStats.WARN_NOT_ENOUGH_SD_SAMPLES, sdWarnings.get(curGroup), minSamplesSd, n);
        }
    }

    /**
     * throws a warning if the number of data points is less then required for statistics calculation (set in HCSTools preferences)
     *
     * @param group
     * @param message
     * @param columns
     * @param minSamples
     * @param n
     */
    private void createWarning(String group, String message, StringBuilder columns, int minSamples, long n) {
        //logger.warn("Group \"" + group + "\" (Columns - " + columns + ")\n " + message + " (required: " + minSamples + ")");
        logger.warn("Group \"" + group + "\": " + message + "\nrequired: " + minSamples + ", available: " + columns);
    }

    /**
     * cell factory class for table with normalized values
     */
    private class ZScoreNormalizerCellFactory extends AbstractCellFactory {
        public ZScoreNormalizerCellFactory(DataColumnSpec[] cSpecArray) {
            super(cSpecArray);
        }

        @Override
        public DataCell[] getCells(DataRow dataRow) {

            // get settings
            List<String> inclColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN_SELECTION)).getIncludeList();
            boolean hasAggColumn = (aggIdx >= 0);

            List<DataCell> cellList = new ArrayList<DataCell>();

            // create a single column map from list of column maps
            HashMap<String, Integer> colList = new HashMap<String, Integer>();
            for (HashMap<String, Integer> subList : columnList) {
                colList.putAll(subList);
            }

            // iterate over columns
            for (String curColumn : inclColumns) {
                // get the value cell and the aggregation cell (if set)
                DataCell valueCell = dataRow.getCell(colList.get(curColumn));
                DataCell aggCell = null;
                // if value cell is missing, then the normalized value is also missing
                if (valueCell.isMissing()) cellList.add(DataType.getMissingCell());
                else {
                    String aggString = null;
                    if (hasAggColumn) {
                        aggCell = dataRow.getCell(aggIdx);
                        if (!aggCell.isMissing()) aggString = ((StringValue) aggCell).getStringValue();
                    }
                    Double value = ((DoubleValue) valueCell).getDoubleValue();
                    Double normValue = evaluateZScore(value, aggString, curColumn);
                    // return a missing value if the normalization returned NaN (sd = 0 or n(ref) = 0)
                    if (normValue.isNaN()) cellList.add(DataType.getMissingCell());
                    else cellList.add(new DoubleCell(normValue));
                }
            }

            DataCell[] cellArray = new DataCell[cellList.size()];
            cellArray = cellList.toArray(cellArray);

            return cellArray;
        }
    }

    /**
     * calculate the z-score by using the previously calculated statistic
     *
     * @param value
     * @param aggString
     * @param column
     * @return zscore
     */
    private double evaluateZScore(Double value, String aggString, String column) {
        double zscore = Double.NaN;
        if (statisticTable.containsKey(aggString))
            if (statisticTable.get(aggString).containsKey(column)) {
                ZScoreStats stats = statisticTable.get(aggString).get(column);
                zscore = (value - stats.getMean_median()) / stats.getSd_mad();
            }
        return zscore;
    }

    /**
     * class to hold statistic data
     */
    private class ZScoreStats {
        // hold mean or median of the values
        private double mean_median;
        // holds standard deviation or median absolute deviation of the values
        private double sd_mad;
        // holds the total number of values
        private long nSamples;
        // holds the number of missing values
        private int nMissing;

        /**
         * constructor
         */
        public ZScoreStats() {
            this.mean_median = Double.NaN;
            this.sd_mad = Double.NaN;
            this.nSamples = 0;
            this.nMissing = 0;
        }

        public double getMean_median() {
            return mean_median;
        }

        public double getSd_mad() {
            return sd_mad;
        }

        public int getnSamples() {
            return (new Long(nSamples).intValue());
        }

        public boolean hasEnoughSamples(int minSamples) {
            return nSamples >= minSamples;
        }

        public int getnMissing() {
            return nMissing;
        }

        /**
         * set class members by using the extended statistic class
         *
         * @param values
         * @param madScalingFactor
         * @param useRobustStats
         * @return
         * @throws MadStatistic.IllegalMadFactorException
         *
         */
        public ZScoreStats init(List<Double> values, double madScalingFactor, boolean useRobustStats) throws MadStatistic.IllegalMadFactorException {
            ExtDescriptiveStats stats = new ExtDescriptiveStats();
            stats.setMadImpl(new MadStatistic(madScalingFactor));

            // fill data
            for (double val : values) {
                if (Double.isNaN(val)) nMissing++;
                else stats.addValue(val);
            }

            // sample size
            nSamples = values.size();
            mean_median = (useRobustStats) ? stats.getMedian() : stats.getMean();
            sd_mad = (useRobustStats) ? stats.getMad() : stats.getStandardDeviation();
            return this;
        }
    }
}
