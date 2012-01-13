package de.mpicbg.tds.knime.hcstools.datamanip.row;

/**
 * Filters table rows by applying a range filter to one or multiple numeric columns (integer, double)
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/1/11
 * Time: 2:17 PM
 */

import de.mpicbg.tds.knime.knutils.*;
import org.knime.base.node.preproc.filter.row.RowFilterIterator;
import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;

public class RangeFilterV2Model extends AbstractNodeModel {

    private SettingsModelFilterString parameterNames = RangeFilterV2Factory.createParameterFilterSetting();
    private SettingsModelDouble lowerBoundSetting = RangeFilterV2Factory.createLowerBoundSetting();
    private SettingsModelDouble upperBoundSetting = RangeFilterV2Factory.createUpperBoundSetting();
    private SettingsModelString filterRuleInclude = RangeFilterV2Factory.createFilterRuleIncludeSetting();
    private SettingsModelString filterRuleMatch = RangeFilterV2Factory.createFilterRuleMatchSetting();

    static final String[] FILTER_RULE_INCLUDE = {"include", "exclude"};
    static final String[] FILTER_RULE_MATCHALL = {"values are in range for all columns", "at least one value is in range"};

    public RangeFilterV2Model() {
        super(1, 1);
        addSetting(lowerBoundSetting);
        addSetting(upperBoundSetting);
        addSetting(filterRuleInclude);
        addSetting(filterRuleMatch);
        addSetting(parameterNames);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0]};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];
        DataTableSpec inputSpec = input.getDataTableSpec();


        // Get the parameter and make sure there all double value columns
        List<Attribute> parameter = new ArrayList<Attribute>();
        List<RowFilter> rangeFilterList = new ArrayList<RowFilter>();
        for (String item : parameterNames.getIncludeList()) {
            Attribute attribute = new InputTableAttribute(item, input);
            if (attribute.getType().isCompatible(DoubleValue.class)) {
                parameter.add(attribute);
                DoubleCell lowerBound = new DoubleCell(lowerBoundSetting.getDoubleValue());
                DoubleCell upperBound = new DoubleCell(upperBoundSetting.getDoubleValue());
                RangeRowFilter rowFilter = new RangeRowFilter(attribute.getName(), lowerBound, upperBound);
                rowFilter.configure(inputSpec);
                rangeFilterList.add(rowFilter);
            } else {
                if (attribute.getType().isCompatible(IntValue.class)) {
                    parameter.add(attribute);
                    IntCell lowerBound = new IntCell((int) Math.ceil(lowerBoundSetting.getDoubleValue()));
                    IntCell upperBound = new IntCell((int) Math.floor(upperBoundSetting.getDoubleValue()));
                    RangeRowFilter rowFilter = new RangeRowFilter(attribute.getName(), lowerBound, upperBound);
                    rowFilter.configure(inputSpec);
                    rangeFilterList.add(rowFilter);
                } else {
                    logger.warn("The parameter '" + attribute.getName() + "' will not be considered for outlier removal, since it is not a DoubleCell type.");
                }
            }
        }

        // Initialize
        BufferedDataContainer retain = exec.createDataContainer(inputSpec);

        // set booleans for filter rules
        boolean retainIfAllMatch = false;
        if (filterRuleMatch.getStringValue().equals(FILTER_RULE_MATCHALL[0])) retainIfAllMatch = true;

        boolean retainMatchingRows = true;
        if (filterRuleInclude.getStringValue().equals(FILTER_RULE_INCLUDE[1])) retainMatchingRows = false;

        // apply range filters
        exec.setMessage("Searching first matching row...");
        try {
            int count = 0;
            RowMultiFilterIterator rowIterator = new RowMultiFilterIterator(input, rangeFilterList, exec, retainIfAllMatch, retainMatchingRows);
            while (rowIterator.hasNext()) {
                DataRow row = rowIterator.next();
                count++;
                retain.addRowToTable(row);
                exec.setMessage("Added row " + count + " (\""
                        + row.getKey() + "\")");
            }
        } catch (RowFilterIterator.RuntimeCanceledExecutionException rce) {
            throw rce.getCause();
        } finally {
            retain.close();
        }

        retain.close();
        //return new BufferedDataTable[]{retain.getTable(), discard.getTable()};
        return new BufferedDataTable[]{retain.getTable()};
    }
}
