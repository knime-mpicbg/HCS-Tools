package de.mpicbg.tds.knime.hcstools.normalization.bycolumn.node_poc;

import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import de.mpicbg.tds.knime.hcstools.normalization.bycolumn.AbstractNormNodeModel;
import de.mpicbg.tds.knime.hcstools.normalization.bycolumn.NormalizationStats;
import de.mpicbg.tds.knime.hcstools.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.tds.knime.hcstools.utils.ExtDescriptiveStats;
import de.mpicbg.tds.knime.hcstools.utils.MadStatistic;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.*;
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
public class PocNormalizerNodeModel extends AbstractNormNodeModel {
	
	public static final String CFG_SUFFIX_DFT = ".poc";

    /**
     * constructor with two ouput ports
     */
    public PocNormalizerNodeModel() {
        super(2);
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
        addModelSetting(AbstractNormNodeModel.CFG_REFSTRING, AbstractNormNodeModel.createRefStringSM(CFG_REFSTRING));
        addModelSetting(AbstractNormNodeModel.CFG_REPLACE_VALUES, AbstractNormNodeModel.createReplaceValuesSM());
        addModelSetting(AbstractNormNodeModel.CFG_ROBUST_STATS, AbstractNormNodeModel.createRobustStatsSM());
        addModelSetting(AbstractNormNodeModel.CFG_SUFFIX, AbstractNormNodeModel.createSuffixSM(getColumnSuffix()));
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

        columnSpecList.add(uniqueNames.newCreator("n", IntCell.TYPE).createSpec());
        columnSpecList.add(uniqueNames.newCreator("n missing", IntCell.TYPE).createSpec());

        DataColumnSpec[] columnSpecArray = new DataColumnSpec[columnSpecList.size()];
        columnSpecArray = columnSpecList.toArray(columnSpecArray);

        return new DataTableSpec("POC Statistics", columnSpecArray);
    }

    /**
     * @param inSpecs
     * @return
     * @throws org.knime.core.node.InvalidSettingsException
     *
     */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inSpec = inSpecs[0];

        checkForNumericColumns(inSpec);

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
        return new NormalizerCellFactory(cSpecArray);
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

        hasReferenceData = false;

        createColumnList(inSpec, inclColumns, useOpt, procOpt);

        // collect statistic data
        statisticTable = new HashMap<String, HashMap<String, NormalizationStats>>();

        // iterate on column hashmap to collect data of multiple columns at once
        for (HashMap<String, Integer> curList : columnList) {
            HashMap<String, HashMap<String, List<Double>>> refData = extractReferenceData(inTable, refString, hasAggColumn, hasRefColumn, curList);
            calculateStatistics(refData, useRobustStats);
            exec.checkCanceled();
        }

        BufferedDataContainer statContainer = createNodeStatisticTable(exec, inSpec, hasAggColumn, hasRefColumn);


        // create KNIME table with normalized values
        ColumnRearranger columnRearranger = createColumnRearranger(inSpec);
        BufferedDataTable pocTable = exec.createColumnRearrangeTable(inTable, columnRearranger, exec);

        if (!hasReferenceData)
            logger.error("input table does not contain any reference data or reference data contains missing values only.");

        return new BufferedDataTable[]{pocTable, statContainer.getTable()};
    }

    /**
     * for each group and each column the statistics of the reference data is calculated and stored
     *
     * @param data
     * @param useRobustStats
     * @throws de.mpicbg.tds.knime.hcstools.utils.MadStatistic.IllegalMadFactorException
     *
     */
    private void calculateStatistics(HashMap<String, HashMap<String, List<Double>>> data, boolean useRobustStats) throws MadStatistic.IllegalMadFactorException {

        // get preference data
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        int minSamplesMean = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_MEANS);
        double madScalingFactor = prefStore.getDouble(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR);

        // group, list of warnings
        HashMap<String, StringBuilder> meanWarnings = new HashMap<String, StringBuilder>();

        //iterate over groups
        for (String curGroup : data.keySet()) {

            if (!statisticTable.containsKey(curGroup))
                statisticTable.put(curGroup, new HashMap<String, NormalizationStats>());
            long n = 0;

            //iterate over columns
            for (String curColumn : data.get(curGroup).keySet()) {
                NormalizationStats stats = new NormalizationStats().init(data.get(curGroup).get(curColumn), madScalingFactor, useRobustStats);
                n = stats.getnSamples();

                // ensure sample size settings
                if (!stats.hasEnoughSamples(minSamplesMean)) {
                    if (!meanWarnings.containsKey(curGroup))
                        meanWarnings.put(curGroup, new StringBuilder(curColumn + "(" + n + ")"));
                    else meanWarnings.get(curGroup).append(", " + curColumn + "(" + n + ")");
                }

                statisticTable.get(curGroup).put(curColumn, stats);
            }

            // throw warnings
            if (meanWarnings.containsKey(curGroup))
                createWarning(curGroup, ExtDescriptiveStats.WARN_NOT_ENOUGH_MEAN_SAMPLES, meanWarnings.get(curGroup), minSamplesMean, n);
        }
    }


    /**
     * calculate the poc by using the previously calculated statistic
     *
     * @param value
     * @param aggString
     * @param column
     * @return poc
     */
    @Override
    protected double evaluate(Double value, String aggString, String column) {
        double poc = Double.NaN;
        if (statisticTable.containsKey(aggString))
            if (statisticTable.get(aggString).containsKey(column)) {
                NormalizationStats stats = statisticTable.get(aggString).get(column);
                poc = value / stats.getMean_median() * 100.0;
            }
        return poc;
    }

    /**
     * creates the statistic output table of the node
     *
     * @param exec
     * @param inSpec
     * @param hasAggColumn
     * @param hasRefColumn
     * @return
     */
    @Override
    protected BufferedDataContainer createNodeStatisticTable(ExecutionContext exec, DataTableSpec inSpec, boolean hasAggColumn, boolean hasRefColumn) {
        // create KNIME table of statistics
        String refString = ((SettingsModelString) getModelSetting(CFG_REFSTRING)).getStringValue();
        BufferedDataContainer statContainer = exec.createDataContainer(createOutSpecStats(inSpec), true);
        int curRowIdx = 1;
        for (String curGroup : statisticTable.keySet()) {
            HashMap<String, NormalizationStats> stat = statisticTable.get(curGroup);
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

                cellList.add(new IntCell(stat.get(curColumn).getnSamples()));

                cellList.add(new IntCell(stat.get(curColumn).getnMissing()));

                DataRow row = new DefaultRow(RowKey.createRowKey(curRowIdx), cellList);
                statContainer.addRowToTable(row);
                curRowIdx++;
            }
        }
        statContainer.close();
        return statContainer;
    }

	@Override
	public String getColumnSuffix() {
		return CFG_SUFFIX_DFT;
	}
}
