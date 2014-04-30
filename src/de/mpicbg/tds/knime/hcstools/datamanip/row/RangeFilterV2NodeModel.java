package de.mpicbg.tds.knime.hcstools.datamanip.row;

/**
 * Filters table rows by applying a range filter to one or multiple numeric columns (integer, double)
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/1/11
 * Time: 2:17 PM
 */

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.RangeRowFilter;
import de.mpicbg.knime.knutils.RowMultiFilterIterator;

import org.knime.base.node.preproc.filter.row.RowFilterIterator;
import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.*;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RangeFilterV2NodeModel extends AbstractNodeModel {

    static final String[] FILTER_RULE_INCLUDE = {"include", "exclude"};
    static final String[] FILTER_RULE_MATCHALL = {"values are in range for all columns", "at least one value is in range"};
    static final String[] FILTER_RULE_MISSING = {"yes", "no"};

    // configuration keys and default values
    public static final String CFG_PARAMS = "ParameterSetting";

    public static final String CFG_LOWER = "LowerBoundSetting";
    public static final Double CFG_LOWER_DFT = Double.NEGATIVE_INFINITY;

    public static final String CFG_UPPER = "UpperBoundSetting";
    public static final Double CFG_UPPER_DFT = Double.POSITIVE_INFINITY;

    public static final String CFG_INCLUDE = "Filter Rule Include";
    public static final String CFG_INCLUDE_DFT = FILTER_RULE_INCLUDE[0];

    public static final String CFG_MATCH = "Filter Rule Match";
    public static final String CFG_MATCH_DFT = FILTER_RULE_MATCHALL[0];

    public static final String CFG_MISSING = "Filter Rule Missing";
    public static final String CFG_MISSING_DFT = FILTER_RULE_MISSING[0];

    public RangeFilterV2NodeModel(int outPorts) {
        super(1, outPorts, true);
        initializeSettings(outPorts);
    }

    private void initializeSettings(int outPorts) {
        this.addModelSetting(CFG_PARAMS, createParameterFilterSetting());
        this.addModelSetting(CFG_LOWER, createLowerBoundSetting());
        this.addModelSetting(CFG_UPPER, createUpperBoundSetting());
        this.addModelSetting(CFG_MATCH, createFilterRuleMatchSetting());
        // only add this setting for the range filter but not for the range splitter
        if (outPorts == 1) this.addModelSetting(CFG_INCLUDE, createFilterRuleIncludeSetting());
        this.addModelSetting(CFG_MISSING, createFilterRuleMissingSetting());
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        //check whether there are any numeric columns available (boolean columns are not allowed)
        boolean numericColumns = false;
        Iterator i = inSpecs[0].iterator();
        while (!numericColumns && i.hasNext()) {
            DataColumnSpec cspec = (DataColumnSpec) i.next();
            DataType type = cspec.getType();
            if (type.isCompatible(DoubleValue.class) && !type.isCompatible(BooleanValue.class)) numericColumns = true;
        }
        if (!numericColumns)
            throw new InvalidSettingsException("input table requires at least one numeric column (Double or Integer)");

        return new DataTableSpec[]{inSpecs[0]};
    }

    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        if (settings.containsKey(CFG_PARAMS)) {
            String[] selectedColumns = settings.getNodeSettings(CFG_PARAMS).getStringArray("InclList");
            if (selectedColumns.length < 1)
                throw new InvalidSettingsException("at least one numeric column has to be selected");
        }
        if (settings.containsKey(CFG_UPPER) && settings.containsKey(CFG_LOWER)) {
            Double lowerBound = settings.getDouble(CFG_LOWER);
            Double upperBound = settings.getDouble(CFG_UPPER);

            if (!(lowerBound < upperBound))
                throw new InvalidSettingsException("lower bound cannot be equal to or bigger than upper bound");
        }
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inTable = inData[0];
        DataTableSpec inputSpec = inTable.getDataTableSpec();

        // Initialize
        BufferedDataContainer retain = exec.createDataContainer(inputSpec);

        // set booleans for filter rules
        String match = ((SettingsModelString) getModelSetting(CFG_MATCH)).getStringValue();
        boolean retainIfAllMatch = match.equals(FILTER_RULE_MATCHALL[0]);
        String include = ((SettingsModelString) getModelSetting(CFG_INCLUDE)).getStringValue();
        boolean retainMatchingRows = include.equals(FILTER_RULE_INCLUDE[0]);
        String missing = ((SettingsModelString) getModelSetting(CFG_MISSING)).getStringValue();
        boolean missingValuesMatch = missing.equals(FILTER_RULE_MISSING[0]);
        List<String> selColumns = ((SettingsModelFilterString) getModelSetting(CFG_PARAMS)).getIncludeList();

        Double minRange = ((SettingsModelDouble) getModelSetting(CFG_LOWER)).getDoubleValue();
        Double maxRange = ((SettingsModelDouble) getModelSetting(CFG_UPPER)).getDoubleValue();


        // initialize range filters
        List<RowFilter> rangeFilterList = initRangeFilters(inputSpec, selColumns, minRange, maxRange, missingValuesMatch);

        exec.setMessage("Searching first matching row...");
        try {
            RowMultiFilterIterator rowIterator = new RowMultiFilterIterator(inTable, rangeFilterList, exec, retainIfAllMatch, retainMatchingRows);
            while (rowIterator.hasNext()) {
                DataRow row = rowIterator.next();
                retain.addRowToTable(row);
                exec.setMessage("Added row " + row.getKey().getString());
            }
        } catch (RowFilterIterator.RuntimeCanceledExecutionException rce) {
            throw rce.getCause();
        }

        retain.close();

        return new BufferedDataTable[]{retain.getTable()};
    }

    protected List<RowFilter> initRangeFilters(DataTableSpec inputSpec, List<String> selColumns, Double minRange, Double maxRange, boolean missingValuesMatch)
            throws InvalidSettingsException {
        List<RowFilter> rangeFilterList = new ArrayList<RowFilter>();
        for (String col : selColumns) {

            DataCell minRangeCell = null;
            DataCell maxRangeCell = null;
            DataType type = inputSpec.getColumnSpec(col).getType();

            if (type.equals(IntCell.TYPE)) {
                minRangeCell = new IntCell((int) Math.ceil(minRange));
                maxRangeCell = new IntCell((int) Math.floor(maxRange));
            }
            if (type.equals(LongCell.TYPE)) {
                minRangeCell = new LongCell((long) Math.ceil(minRange));
                maxRangeCell = new LongCell((long) Math.floor(maxRange));
            }
            if (type.equals(DoubleCell.TYPE)) {
                minRangeCell = new DoubleCell(minRange);
                maxRangeCell = new DoubleCell(maxRange);
            }
            if (type.equals(BooleanCell.TYPE))
                throw new InvalidSettingsException("No support for boolean columns");
            RangeRowFilter rowFilter = new RangeRowFilter(col, minRangeCell, maxRangeCell, missingValuesMatch);
            rowFilter.configure(inputSpec);
            rangeFilterList.add(rowFilter);
        }
        return rangeFilterList;
    }

    static SettingsModelNumber createLowerBoundSetting() {
        return new SettingsModelDouble(CFG_LOWER, CFG_LOWER_DFT);
    }

    static SettingsModelNumber createUpperBoundSetting() {
        return new SettingsModelDouble(CFG_UPPER, CFG_UPPER_DFT);
    }

    static SettingsModelFilterString createParameterFilterSetting() {
        return new SettingsModelFilterString(CFG_PARAMS);
    }

    static SettingsModelString createFilterRuleMatchSetting() {
        return new SettingsModelString(CFG_MATCH, CFG_MATCH_DFT);
    }

    static SettingsModelString createFilterRuleIncludeSetting() {
        return new SettingsModelString(CFG_INCLUDE, CFG_INCLUDE_DFT);
    }

    static SettingsModelString createFilterRuleMissingSetting() {
        return new SettingsModelString(CFG_MISSING, CFG_MISSING_DFT);
    }
}
