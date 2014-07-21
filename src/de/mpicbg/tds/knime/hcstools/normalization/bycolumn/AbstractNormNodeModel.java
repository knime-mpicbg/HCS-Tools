package de.mpicbg.tds.knime.hcstools.normalization.bycolumn;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import org.knime.core.data.*;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.*;
import org.knime.core.util.UniqueNameGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class provides methods for normalization nodes model classes
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 10:18 AM
 */
public abstract class AbstractNormNodeModel extends AbstractNodeModel {

    // model settings and their default values
    public static final String CFG_REPLACE_VALUES = "replace";
    public static final boolean CFG_REPLACE_VALUES_DFT = false;

    public static final String CFG_COLUMN_SELECTION = "selected columns";

    public static final String CFG_ROBUST_STATS = "use robust statistics";
    public static final boolean CFG_ROBUST_STATS_DFT = false;

    public static final String CFG_AGGR = "groupBy";
    public static final String CFG_AGGR_DFT = TdsUtils.SCREEN_MODEL_BARCODE;

    public static final String CFG_REFCOLUMN = "refCol";

    public static final String CFG_REFSTRING = "refString";

    public static final String CFG_SUFFIX = "suffix";

    public static final String CFG_OPT = "processing option";
    public static final int CFG_OPT_DFT = 5;

    public static final String CFG_USEOPT = "use processing option";
    public static final boolean CFG_USEOPT_DFT = false;

    // model setting default which is set in each implementation class
    //protected static String CFG_SUFFIX_DFT;

    protected boolean hasReferenceData;

    /**
     * stores statistik for each group; <group, <parameter, statistic>>
     */
    protected HashMap<String, HashMap<String, NormalizationStats>> statisticTable;

    // index of aggregation column and reference column
    protected int aggIdx;
    protected int refIdx;
    /**
     * list of parameter sets (see processing options) and their column index
     */
    protected List<HashMap<String, Integer>> columnList;

    /**
     * constructor (allows one input port but multiple ouput ports)
     *
     * @param outPorts
     */
    public AbstractNormNodeModel(int outPorts) {
        super(1, outPorts, true);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * abstract method to add model settings
     */
    protected abstract void initializeSettings();

    /**
     * abstract method for execution
     *
     * @param inData
     * @param exec
     * @return
     * @throws Exception
     */
    protected abstract BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception;

    /**
     * abstract method for configuration
     *
     * @param inSpecs
     * @return
     * @throws InvalidSettingsException
     */
    protected abstract DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException;

    /**
     * abstract method to retrieve the cell factory to normalized values
     *
     * @param cSpecArray
     * @return
     */
    protected abstract CellFactory getCellFactory(DataColumnSpec[] cSpecArray);

    /**
     * abstract method to return a table spec for a second statistic output
     *
     * @param inSpec
     * @return
     */
    protected abstract DataTableSpec createOutSpecStats(DataTableSpec inSpec);

    protected abstract double evaluate(Double value, String aggString, String column);

    /**
     * checks, if input specs do contain numeric columns (all columns compatible to double except boolean
     *
     * @param inSpec
     * @throws InvalidSettingsException
     */
    protected void checkForNumericColumns(DataTableSpec inSpec) throws InvalidSettingsException {
        boolean numericColumns = false;
        Iterator i = inSpec.iterator();
        //iterate through all columns as long as no numeric column occur
        while (!numericColumns && i.hasNext()) {
            DataColumnSpec cspec = (DataColumnSpec) i.next();
            DataType type = cspec.getType();
            if (type.isCompatible(DoubleValue.class) && !type.isCompatible(BooleanValue.class)) numericColumns = true;
        }
        if (!numericColumns)
            throw new InvalidSettingsException("input table requires at least one numeric column (Double or Integer)");
    }


    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        //super.validateSettings(settings);
        // check that at least one numeric column has been selected
        if (settings.containsKey(CFG_COLUMN_SELECTION)) {
            String[] selectedColumns = settings.getNodeSettings(CFG_COLUMN_SELECTION).getStringArray("InclList");
            if (selectedColumns.length < 1)
                throw new InvalidSettingsException("at least one numeric column has to be selected");
        }
        // check reference string (should be available if a reference column has been selected)
        if (settings.containsKey(CFG_REFCOLUMN)) {
            if (settings.getString(CFG_REFCOLUMN) != null) {
                if (!settings.containsKey(CFG_REFSTRING))
                    throw new InvalidSettingsException("cannot find any reference string setting for selected reference column");
                if (settings.getString(CFG_REFSTRING) == null)
                    throw new InvalidSettingsException("reference string is required if reference column is set");
            }
        }
    }

    /**
     * if no valid model setting value is available for the aggregation, it will be guessed from spec and setting will be updated
     *
     * @param inSpec
     */
    protected void autoGuessAggreagtionColumn(DataTableSpec inSpec) {

        SettingsModelString aggColumnSM = ((SettingsModelString) getModelSetting(CFG_AGGR));
        String aggColumn = aggColumnSM.getStringValue();

        // no autoguessing ... if no aggregation column has been selected
        if (aggColumn == null) return;
        if (inSpec.containsName(aggColumn)) {
            // ... if aggregation column exists and is of type nominal
            if (inSpec.getColumnSpec(aggColumn).getType().isCompatible(NominalValue.class)) return;
        }

        // autoguess
        Iterator<DataColumnSpec> it = inSpec.iterator();
        String guessedColumn = null;
        while (it.hasNext() && guessedColumn == null) {
            DataColumnSpec cSpec = it.next();
            if (cSpec.getType().isCompatible(NominalValue.class)) {
                guessedColumn = cSpec.getName();
            }
        }

        aggColumnSM.setStringValue(guessedColumn);
        addModelSetting(CFG_AGGR, aggColumnSM);
        setWarningMessage("Auto-Guessing aggregation column. Please check configuration settings before execution");
    }

    /**
     * if no valid model setting value is available for the reference, it will be guessed from spec and setting will be updated
     *
     * @param inSpec
     */
    protected void autoGuessReferenceColumn(DataTableSpec inSpec) {
        SettingsModelString refColumnSM = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN));
        String refColumn = refColumnSM.getStringValue();
        if (refColumn != null) {
            if (inSpec.containsName(refColumn)) {
                DataColumnSpec cSpec = inSpec.getColumnSpec(refColumn);
                if (cSpec.getType().isCompatible(NominalValue.class)) return;
                // if column type is not a nominal column, reset the reference column to <none>
                refColumnSM.setStringValue(null);
                addModelSetting(CFG_REFCOLUMN, refColumnSM);
                setWarningMessage("Auto-Guessing reference column. Please check configuration settings before execution");
            }
        }
    }

    /**
     * if no valid model setting value is available for column selection, it will be guessed from spec and setting will be updated
     *
     * @param inSpec
     */
    protected void autoGuessColumnSelection(DataTableSpec inSpec) {
        // auto guess a column selection if no setting available
        SettingsModelFilterString selColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN_SELECTION));
        if (selColumns.getIncludeList().size() == 0) {
            List<String> inclColumns = new ArrayList<String>();
            List<String> exclColumns = new ArrayList<String>();
            for (DataColumnSpec cSpec : inSpec) {
                if (cSpec.getType().isCompatible(DoubleValue.class)) inclColumns.add(cSpec.getName());
            }
            selColumns.setNewValues(inclColumns, exclColumns, false);
            addModelSetting(CFG_COLUMN_SELECTION, selColumns);
            setWarningMessage("Auto-Guessing column selection. Please check configuration settings before execution.");
        }
    }

    /**
     * @param inSpec
     * @return outSpec
     */
    protected DataTableSpec createOutSpec(DataTableSpec inSpec) {
        return createColumnRearranger(inSpec).createSpec();
    }

    /**
     * column rearranger adds new columns with suffix or replaces the old ones
     *
     * @param inSpec
     * @return
     */
    protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec) {
        ColumnRearranger columnRearranger = new ColumnRearranger(inSpec);

        List<DataColumnSpec> cSpecs = new ArrayList<DataColumnSpec>();

        List<String> inclColumns = ((SettingsModelFilterString) getModelSetting(CFG_COLUMN_SELECTION)).getIncludeList();

        SettingsModelOptionalString suffixSM = ((SettingsModelOptionalString) getModelSetting(CFG_SUFFIX));
        String suffix = getColumnSuffix();
        if (suffixSM.isActive()) suffix = suffixSM.getStringValue();
        boolean replaceValues = ((SettingsModelBoolean) getModelSetting(CFG_REPLACE_VALUES)).getBooleanValue();

        UniqueNameGenerator uniqueNames = new UniqueNameGenerator(inSpec);
        int[] colIdx = new int[inclColumns.size()];
        int i = 0;

        for (String curColumn : inclColumns) {
            if (replaceValues) {
                colIdx[i] = inSpec.findColumnIndex(curColumn);
                i++;
                // create a new spec because the data type has to be double
                cSpecs.add(new DataColumnSpecCreator(curColumn, DoubleCell.TYPE).createSpec());
            } else {
                // to ensure unique names
                DataColumnSpecCreator dcsc = uniqueNames.newCreator(curColumn + suffix, DoubleCell.TYPE);
                cSpecs.add(dcsc.createSpec());
            }
        }

        DataColumnSpec[] cSpecArray = new DataColumnSpec[cSpecs.size()];
        cSpecs.toArray(cSpecArray);

        if (replaceValues) columnRearranger.replace(getCellFactory(cSpecArray), colIdx);
        else columnRearranger.append(getCellFactory(cSpecArray));

        return columnRearranger;
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
    protected void createWarning(String group, String message, StringBuilder columns, int minSamples, long n) {
        //logger.warn("Group \"" + group + "\" (Columns - " + columns + ")\n " + message + " (required: " + minSamples + ")");
        logger.warn("Group \"" + group + "\": " + message + "\nrequired: " + minSamples + ", available: " + columns);
    }

    /**
     * @return settings model for replacing values
     */
    public static SettingsModelBoolean createReplaceValuesSM() {
        return new SettingsModelBoolean(CFG_REPLACE_VALUES, CFG_REPLACE_VALUES_DFT);
    }

    /**
     * @return settings model for numeric columns
     */
    public static SettingsModelFilterString createColumnFilterSM() {
        return new SettingsModelFilterString(CFG_COLUMN_SELECTION);
    }

    /**
     * @return settings model for using robust statistics
     */
    public static SettingsModelBoolean createRobustStatsSM() {
        return new SettingsModelBoolean(CFG_ROBUST_STATS, CFG_ROBUST_STATS_DFT);
    }

    /**
     * @return settings model for aggregation column
     */
    public static SettingsModelString createAggregationSM() {
        return new SettingsModelString(CFG_AGGR, CFG_AGGR_DFT);
    }

    /**
     * @return settings model for reference population column
     */
    public static SettingsModelString createRefColumnSM() {
        return new SettingsModelString(CFG_REFCOLUMN, null);
    }

    /**
     * @return settings model for reference population string
     */
    public static SettingsModelString createRefStringSM(String key) {
        return new SettingsModelString(key, null);
    }

    /**
     * @return settings model for column suffix
     */
    public static SettingsModelOptionalString createSuffixSM(String cgfSuffixDft) {
        return new SettingsModelOptionalString(CFG_SUFFIX, cgfSuffixDft, false);
    }

    /**
     * @return settings model for number of columns which are processed at once
     */
    public static SettingsModelNumber createProcessingOptionsSM() {
        return new SettingsModelIntegerBounded(CFG_OPT, CFG_OPT_DFT, 1, 100);
    }

    /**
     * @return settings model for using processing options
     */
    public static SettingsModelBoolean createUseProcessingOptionsSM() {
        return new SettingsModelBoolean(CFG_USEOPT, CFG_USEOPT_DFT);
    }

    /**
     * populates the columnLists (selected columns and their position within the given table)
     * each list is a set of columns (not more than given in the processing options, if they are used)
     *
     * @param inSpec
     * @param inclColumns
     * @param useOpt
     * @param procOpt
     */
    protected void createColumnList(DataTableSpec inSpec, List<String> inclColumns, boolean useOpt, int procOpt) {
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
    }
    
    public abstract String getColumnSuffix();

    protected abstract BufferedDataContainer createNodeStatisticTable(ExecutionContext exec, DataTableSpec inSpec, boolean hasAggColumn, boolean hasRefColumn);

    /**
     * retrieve reference data from the input table
     *
     * @param inTable
     * @param refString
     * @param hasAggColumn
     * @param hasRefColumn
     * @param curList
     * @return Hashmap with Key 'group' and entry another Hashmap with key 'parameter' and a list of double values as entry
     */
    protected HashMap<String, HashMap<String, List<Double>>> extractReferenceData(BufferedDataTable inTable, String refString, boolean hasAggColumn, boolean hasRefColumn, HashMap<String, Integer> curList) {
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

                    //set flag that at least one row with reference data has been detected
                    if (!Double.isNaN(value)) hasReferenceData = true;
                }

            }
        } // finish iteration over the table
        return refData;
    }

    /**
     * cell factory class for table with normalized values
     */
    protected class NormalizerCellFactory extends AbstractCellFactory {
        public NormalizerCellFactory(DataColumnSpec[] cSpecArray) {
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
                    Double normValue = evaluate(value, aggString, curColumn);
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
}
