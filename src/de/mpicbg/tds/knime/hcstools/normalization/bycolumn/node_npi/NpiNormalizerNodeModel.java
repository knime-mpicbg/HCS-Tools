package de.mpicbg.tds.knime.hcstools.normalization.bycolumn.node_npi;

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
public class NpiNormalizerNodeModel extends AbstractNormNodeModel {

    // additional settings
    public static final String CFG_REFSTRINGPOS = "refStringPos";

    private HashMap<String, HashMap<String, NormalizationStats>> statisticTablePos;

    /**
     * constructor with two ouput ports
     */
    public NpiNormalizerNodeModel() {
        super(2);
        CFG_SUFFIX_DFT = ".npi";
        initializeSettings();
    }

    /**
     * add model settings
     */
    @Override
    protected void initializeSettings() {
        addModelSetting(CFG_AGGR, createAggregationSM());
        addModelSetting(CFG_COLUMN_SELECTION, createColumnFilterSM());
        addModelSetting(CFG_REFCOLUMN, createRefColumnSM());
        addModelSetting(CFG_REFSTRING, createRefStringSM(CFG_REFSTRING));
        addModelSetting(CFG_REFSTRINGPOS, createRefStringSM(CFG_REFSTRINGPOS));
        addModelSetting(CFG_REPLACE_VALUES, createReplaceValuesSM());
        addModelSetting(CFG_ROBUST_STATS, createRobustStatsSM());
        addModelSetting(CFG_SUFFIX, createSuffixSM());
        addModelSetting(CFG_USEOPT, createUseProcessingOptionsSM());
        addModelSetting(CFG_OPT, createProcessingOptionsSM());
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

        return new DataTableSpec("NPI Statistics", columnSpecArray);
    }

    /**
     * checks if input specs do contain nominal value columns
     *
     * @param inSpec
     * @throws InvalidSettingsException
     */
    protected void checkForNominalColumns(DataTableSpec inSpec) throws InvalidSettingsException {
        //check whether the table contains a nominal value column
        if (!inSpec.containsCompatibleType(NominalValue.class))
            throw new InvalidSettingsException("input table requires " +
                    "at least one column with nominal values (String) and a domain of at least two values");
        //check if at least one nominal value column has a domain with at least two domain values
        boolean noNominalColumn = (getPossibleReferenceColumn(inSpec, null) == null);
        if (noNominalColumn)
            throw new InvalidSettingsException("input table requires " +
                    "at least one column with nominal values (String) and a domain of at least two values");
    }

    /**
     * search in the table specs for a nominal column with at least two domain values, which is different from the given column
     *
     * @param inSpec
     * @param columnName
     * @return
     */
    private String getPossibleReferenceColumn(DataTableSpec inSpec, String columnName) {
        Iterator<DataColumnSpec> iterator = inSpec.iterator();
        String nominalColumn = null;
        while (iterator.hasNext() && nominalColumn == null) {
            DataColumnSpec cSpec = iterator.next();
            if (cSpec.getDomain().hasValues())
                if (cSpec.getDomain().getValues().size() >= 2) {
                    if (columnName == null) nominalColumn = cSpec.getName();
                    else if (!cSpec.getName().equals(columnName)) nominalColumn = cSpec.getName();
                }
        }
        return nominalColumn;
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
        checkForNominalColumns(inSpec);

        runAutoGuessing(inSpec);

        // throw an exception if selected columns are identical (except if both are set to <none>)
        String aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue();
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        if (refColumn.equals(aggColumn))
            throw new InvalidSettingsException("aggregation column and reference column must not be the same");

        String refStringNeg = ((SettingsModelString) getModelSetting(CFG_REFSTRING)).getStringValue();
        String refStringPos = ((SettingsModelString) getModelSetting(CFG_REFSTRINGPOS)).getStringValue();

        if (refStringNeg.equals(refStringPos))
            throw new InvalidSettingsException("positive and negative controls must not be the same");

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
        autoGuessReferenceColumnNoNone(inSpec);
        autoGuessColumnSelection(inSpec);
    }

    /**
     * try to find a reference column (nominal with at least two domain values different from the aggregation column)
     * autoguess and update the selected reference strings
     *
     * @param inSpec
     */
    private void autoGuessReferenceColumnNoNone(DataTableSpec inSpec) {
        SettingsModelString refColumnSM = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN));
        String refColumn = refColumnSM.getStringValue();

        //no autoguessing if the column exists, if it is of type nominal and has at least two domain values
        if (inSpec.containsName(refColumn)) {
            DataColumnSpec cSpec = inSpec.getColumnSpec(refColumn);
            if (cSpec.getType().isCompatible(NominalValue.class) && cSpec.getDomain().hasValues()) {
                if (cSpec.getDomain().getValues().size() >= 2) return;
            }
        }

        // autoguess
        // try to find a nominal column with at least two domain values
        String guessedColumn = getPossibleReferenceColumn(inSpec, ((SettingsModelString) getModelSetting(CFG_AGGR)).getStringValue());

        refColumnSM.setStringValue(guessedColumn);
        addModelSetting(CFG_REFCOLUMN, refColumnSM);
        setWarningMessage("Auto-Guessing reference column. Please check configuration settings before execution");

        SettingsModelString refStringSM = ((SettingsModelString) getModelSetting(CFG_REFSTRING));
        SettingsModelString refStringPosSM = ((SettingsModelString) getModelSetting(CFG_REFSTRINGPOS));

        String[] values = new String[2];
        Arrays.fill(values, null);

        if (guessedColumn != null) {
            Set<DataCell> domainValues = inSpec.getColumnSpec(guessedColumn).getDomain().getValues();
            Iterator it = domainValues.iterator();
            int i = 0;
            while (it.hasNext() && i < values.length) {
                values[i] = ((StringCell) it.next()).getStringValue();
                i++;
            }
        }

        refStringSM.setStringValue(values[0]);
        addModelSetting(CFG_REFSTRING, refStringSM);
        refStringPosSM.setStringValue(values[1]);
        addModelSetting(CFG_REFSTRINGPOS, refStringPosSM);
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
        String refStringPos = ((SettingsModelString) getModelSetting(CFG_REFSTRINGPOS)).getStringValue();
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
        statisticTablePos = new HashMap<String, HashMap<String, NormalizationStats>>();

        // iterate on column hashmap to collect data of multiple columns at once
        for (HashMap<String, Integer> curList : columnList) {
            HashMap<String, HashMap<String, List<Double>>> refData = extractReferenceData(inTable, refString, hasAggColumn, hasRefColumn, curList);
            HashMap<String, HashMap<String, List<Double>>> refDataPos = extractReferenceData(inTable, refStringPos, hasAggColumn, hasRefColumn, curList);
            calculateStatistics(refData, useRobustStats, statisticTable);
            calculateStatistics(refDataPos, useRobustStats, statisticTablePos);
            exec.checkCanceled();
        }

        BufferedDataContainer statContainer = createNodeStatisticTable(exec, inSpec, hasAggColumn, hasRefColumn);


        // create KNIME table with normalized values
        ColumnRearranger columnRearranger = createColumnRearranger(inSpec);
        BufferedDataTable npiTable = exec.createColumnRearrangeTable(inTable, columnRearranger, exec);

        if (!hasReferenceData)
            logger.error("input table does not contain any reference data or reference data contains missing values only.");

        return new BufferedDataTable[]{npiTable, statContainer.getTable()};
    }

    /**
     * for each group and each column the statistics of the reference data is calculated and stored
     *
     * @param data
     * @param useRobustStats
     * @param statTable
     * @throws de.mpicbg.tds.knime.hcstools.utils.MadStatistic.IllegalMadFactorException
     *
     */
    private void calculateStatistics(HashMap<String, HashMap<String, List<Double>>> data, boolean useRobustStats, HashMap<String, HashMap<String, NormalizationStats>> statTable) throws MadStatistic.IllegalMadFactorException {

        // get preference data
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        int minSamplesMean = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_MEANS);
        double madScalingFactor = prefStore.getDouble(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR);

        // group, list of warnings
        HashMap<String, StringBuilder> meanWarnings = new HashMap<String, StringBuilder>();

        //iterate over groups
        for (String curGroup : data.keySet()) {

            if (!statTable.containsKey(curGroup))
                statTable.put(curGroup, new HashMap<String, NormalizationStats>());
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

                statTable.get(curGroup).put(curColumn, stats);
            }

            // throw warnings
            if (meanWarnings.containsKey(curGroup))
                createWarning(curGroup, ExtDescriptiveStats.WARN_NOT_ENOUGH_MEAN_SAMPLES, meanWarnings.get(curGroup), minSamplesMean, n);
        }
    }


    /**
     * calculate the npi by using the previously calculated statistic
     *
     * @param value
     * @param aggString
     * @param column
     * @return npi
     */
    @Override
    protected double evaluate(Double value, String aggString, String column) {
        double npi = Double.NaN;
        if (statisticTable.containsKey(aggString) && statisticTablePos.containsKey(aggString))
            if (statisticTable.get(aggString).containsKey(column) && statisticTablePos.get(aggString).containsKey(column)) {
                NormalizationStats stats = statisticTable.get(aggString).get(column);
                NormalizationStats statsPos = statisticTablePos.get(aggString).get(column);
                double diff = (statsPos.getMean_median() - stats.getMean_median());
                if (diff != 0)
                    npi = (statsPos.getMean_median() - value) / diff * 100.0;
            }
        return npi;
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
        String refStringPos = ((SettingsModelString) getModelSetting(CFG_REFSTRINGPOS)).getStringValue();

        BufferedDataContainer statContainer = exec.createDataContainer(createOutSpecStats(inSpec), true);
        int curRowIdx = 1;
        curRowIdx = fillContainer(refString, hasAggColumn, hasRefColumn, statContainer, curRowIdx, statisticTable);
        fillContainer(refStringPos, hasAggColumn, hasRefColumn, statContainer, curRowIdx, statisticTablePos);
        statContainer.close();
        return statContainer;
    }

    /**
     * statistic table is filled with the statistic of the given statisticTable
     *
     * @param refString
     * @param hasAggColumn
     * @param hasRefColumn
     * @param statContainer
     * @param curRowIdx
     * @param statTable
     * @return
     */
    private int fillContainer(String refString, boolean hasAggColumn, boolean hasRefColumn, BufferedDataContainer statContainer, int curRowIdx, HashMap<String, HashMap<String, NormalizationStats>> statTable) {
        for (String curGroup : statTable.keySet()) {
            HashMap<String, NormalizationStats> stat = statTable.get(curGroup);
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
        return curRowIdx;
    }
}
