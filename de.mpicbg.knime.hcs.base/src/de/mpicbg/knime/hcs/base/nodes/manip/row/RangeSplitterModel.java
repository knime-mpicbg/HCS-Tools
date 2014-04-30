package de.mpicbg.knime.hcs.base.nodes.manip.row;

/**
 *  Filters table rows by applying a range filter to one or multiple numeric columns (integer, double)
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/12/11
 * Time: 1:30 PM
 */

import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.List;

public class RangeSplitterModel extends RangeFilterV2NodeModel {


    public RangeSplitterModel() {
        super(2);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0], inSpecs[0]};    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inTable = inData[0];
        DataTableSpec inputSpec = inTable.getDataTableSpec();

        // Initialize
        BufferedDataContainer retain = exec.createDataContainer(inputSpec);
        BufferedDataContainer discard = exec.createDataContainer(inputSpec);

        // set booleans for filter rules
        String match = ((SettingsModelString) getModelSetting(CFG_MATCH)).getStringValue();
        boolean retainIfAllMatch = match.equals(FILTER_RULE_MATCHALL[0]);
        String missing = ((SettingsModelString) getModelSetting(CFG_MISSING)).getStringValue();
        boolean missingValuesMatch = missing.equals(FILTER_RULE_MISSING[0]);
        List<String> selColumns = ((SettingsModelFilterString) getModelSetting(CFG_PARAMS)).getIncludeList();
        Double minRange = ((SettingsModelDouble) getModelSetting(CFG_LOWER)).getDoubleValue();
        Double maxRange = ((SettingsModelDouble) getModelSetting(CFG_UPPER)).getDoubleValue();

        // initialize range filters
        List<RowFilter> rangeFilterList = initRangeFilters(inputSpec, selColumns, minRange, maxRange, missingValuesMatch);

        exec.setMessage("Searching first matching row...");

        // number of rows to process
        int nRows = inTable.getRowCount();
        int currentRow = 0;

        for (DataRow row : inTable) {
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
