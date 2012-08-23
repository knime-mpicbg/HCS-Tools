package de.mpicbg.tds.knime.hcstools.normalization.bycolumn;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import org.knime.core.data.*;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.*;
import org.knime.core.util.UniqueNameGenerator;

import java.util.ArrayList;
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
    protected static String CFG_SUFFIX_DFT;

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

    /**
     * checks if input specs do contain nominal value columns
     *
     * @param inSpec
     * @throws InvalidSettingsException
     */
    protected void checkForNominalColumns(DataTableSpec inSpec) throws InvalidSettingsException {
        if (!inSpec.containsCompatibleType(NominalValue.class))
            throw new InvalidSettingsException("input table requires " +
                    "at least one column with nominal values (String)");
    }

    /**
     * if no valid model setting value is available for the aggregation, it will be guessed from spec and setting will be updated
     *
     * @param inSpec
     */
    protected void autoGuessAggreagtionColumn(DataTableSpec inSpec) {
        // auto guess aggregation column if default not available
        SettingsModelString aggColumn = ((SettingsModelString) getModelSetting(CFG_AGGR));
        if (aggColumn.getStringValue() != null && !inSpec.containsName(aggColumn.getStringValue())) {
            Iterator<DataColumnSpec> it = inSpec.iterator();
            while (it.hasNext()) {
                DataColumnSpec cSpec = it.next();
                if (cSpec.getType().isCompatible(StringValue.class)) {
                    aggColumn.setStringValue(cSpec.getName());
                    addModelSetting(CFG_AGGR, aggColumn);
                    setWarningMessage("Auto-Guessing aggregation column. Please check configuration settings before execution");
                    return;
                }
            }
        }
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
                if (cSpec.getType().isCompatible(NominalValue.class) && cSpec.getDomain().hasValues()) return;
            }
            refColumnSM.setStringValue(null);
            addModelSetting(CFG_REFCOLUMN, refColumnSM);
            setWarningMessage("Auto-Guessing reference column. Please check configuration settings before execution");
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
        String suffix = ((SettingsModelOptionalString) getModelSetting(CFG_SUFFIX)).getStringValue();
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
    public static SettingsModelString createRefStringSM() {
        return new SettingsModelString(CFG_REFSTRING, null);
    }

    /**
     * @return settings model for column suffix
     */
    public static SettingsModelOptionalString createSuffixSM() {
        return new SettingsModelOptionalString(CFG_SUFFIX, CFG_SUFFIX_DFT, false);
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
}
