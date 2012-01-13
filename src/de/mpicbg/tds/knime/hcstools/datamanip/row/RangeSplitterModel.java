package de.mpicbg.tds.knime.hcstools.datamanip.row;

/**
 *  Filters table rows by applying a range filter to one or multiple numeric columns (integer, double)
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/12/11
 * Time: 1:30 PM
 */

import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import de.mpicbg.tds.knime.knutils.RangeRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.*;
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

public class RangeSplitterModel extends AbstractNodeModel {

    private SettingsModelFilterString parameterNames = RangeFilterV2Factory.createParameterFilterSetting();
    private SettingsModelDouble lowerBoundSetting = RangeFilterV2Factory.createLowerBoundSetting();
    private SettingsModelDouble upperBoundSetting = RangeFilterV2Factory.createUpperBoundSetting();
    private SettingsModelString filterRuleMatch = RangeFilterV2Factory.createFilterRuleMatchSetting();

    static final String[] FILTER_RULE_MATCHALL = {"values are in range for all columns", "at least one value is in range"};

    public RangeSplitterModel() {
        super(1, 2);
        addSetting(lowerBoundSetting);
        addSetting(upperBoundSetting);
        addSetting(filterRuleMatch);
        addSetting(parameterNames);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0], inSpecs[0]};    //To change body of overridden methods use File | Settings | File Templates.
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
        BufferedDataContainer discard = exec.createDataContainer(inputSpec);

        // set booleans for filter rules
        boolean retainIfAllMatch = false;
        if (filterRuleMatch.getStringValue().equals(FILTER_RULE_MATCHALL[0])) retainIfAllMatch = true;

        // apply range filters
        exec.setMessage("Searching first matching row...");

        // number of rows to process
        int nRows = input.getRowCount();
        int currentRow = 0;

        for (RowIterator it = input.iterator(); it.hasNext(); ) {
            DataRow row = it.next();
            currentRow++;

            // check if execution has been canceled
            exec.checkCanceled();
            // set progress
            exec.setProgress((double) currentRow / nRows, " processing row " + currentRow);

            // consult the filters whether to include this row
            int matchCount = 0;
            for (RowFilter curFilter : rangeFilterList) {
                if (curFilter.matches(row, currentRow)) matchCount++;
            }

            boolean rowMatches = false;
            // all filters are matched
            if (retainIfAllMatch && matchCount == rangeFilterList.size()) rowMatches = true;
            // at least one filter matches
            if (!retainIfAllMatch && matchCount > 0) rowMatches = true;

            if (rowMatches) retain.addRowToTable(row);
            else discard.addRowToTable(row);

        }

        retain.close();
        discard.close();
        return new BufferedDataTable[]{retain.getTable(), discard.getTable()};
    }
}
