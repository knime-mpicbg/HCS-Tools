package de.mpicbg.knime.hcs.base.nodes.preproc; /**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 1/13/12
 * Time: 9:03 AM
 */

import de.mpicbg.knime.knutils.AbstractNodeModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

public class OutlierFilterModel extends AbstractNodeModel {

    public OutlierFilterModel() {
        //TODO: adapt numer of in and out ports
        super(1, 1);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return super.configure(inSpecs);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];
        DataTableSpec inputSpec = input.getDataTableSpec();

        /*HashMap<String, Integer> verifiedColumns = new HashMap<String, Integer>();
        for (String item : parameterNames.getIncludeList()) {
            DataColumnSpec cSpec = inputSpec.getColumnSpec(item);
            if( cSpec.getType().isCompatible(DoubleValue.class)) {
                verifiedColumns.put(item, inputSpec.findColumnIndex(item));
            }
            else {
                logger.warn("The parameter '" + cSpec.getName() + "' will not be considered for outlier removal, since it is not a double compatible type.");
            }
        }

        // Get the groups defined by the nominal column.
        //Attribute groupingAttribute = new InputTableAttribute(this.groupingColumn.getStringValue(), input);
        //Map<Object, List<DataRow>> subsets = AttributeUtils.splitRowsGeneric(input, groupingAttribute);

        // sort table by grouping column
        final ExecutionContext sortExec = exec.createSubExecutionContext(0.6);
        exec.setMessage("Sorting input table...");
        final BufferedDataTable sortedTable;
        List<String> groupBy = new ArrayList<String>();
        groupBy.add(groupingColumn.getStringValue());
        sortedTable = sortTable(sortExec, input,groupBy, true);
        sortExec.setProgress(1.0);

        // execution context for removing outliers
        final ExecutionContext groupExec = exec.createSubExecutionContext(0.4);   */

        // Initialize
        BufferedDataContainer keepContainer = exec.createDataContainer(inputSpec);
        BufferedDataContainer discartContainer = exec.createDataContainer(inputSpec);

        /*String selectedMethod = method.getStringValue();
        double selectedFactor = factor.getDoubleValue();
        // set booleans for filter rules
        boolean retainIfAllMatch = rule.getBooleanValue();

        // calculate statistics (contains one group with all columns)
        HashMap<Integer, DescriptiveStatistics> statistics = new HashMap<Integer, DescriptiveStatistics>();
        BufferedDataContainer tempContainer = exec.createDataContainer(inputSpec);
        boolean flagNewGroup = true;
        String lastGroupValue = null;
        String currentGroupValue = null;
        int groupColumnIndex = sortedTable.getDataTableSpec().findColumnIndex(groupBy.get(1));

        for (final DataRow row : sortedTable) {

            currentGroupValue = ((StringValue)row.getCell(groupColumnIndex)).getStringValue();

            // if a new group starts
            if(!currentGroupValue.equals(lastGroupValue)) {

                // calculate bounds for all columns
                List<RowFilter> rangeFilterList = new ArrayList<RowFilter>();
                for(Integer currentColumnIndex : verifiedColumns.values()){
                    DescriptiveStatistics stats = statistics.get(currentColumnIndex);
                    boolean doubleCell = inputSpec.getColumnSpec(currentColumnIndex).getType().isCompatible(DoubleCell.class);

                    // calculate upper and lower bounds
                    double lBound;
                    double uBound;
                    if(selectedMethod.equals("Boxplot")) {
                        // implement calculation of boxplot whiskers
                        lBound = stats.getPercentile(25);
                        uBound = stats.getPercentile(75);    //TODO: ask Felix why he used 85 instead of 75!
                        double iqr = uBound - lBound;
                        lBound = lBound - selectedFactor * iqr;
                        uBound = uBound + selectedFactor * iqr;
                    } else {
                        // implement mean +- factor * SD here
                        double mean = stats.getMean();
                        double sd = stats.getStandardDeviation();
                        lBound = mean - selectedFactor * sd;
                        uBound = mean + selectedFactor * sd;
                    }

                    RangeRowFilter rowFilter;
                    String columnName = inputSpec.getColumnSpec(currentColumnIndex).getName();
                    if(doubleCell) {
                        DoubleCell lBoundCell = new DoubleCell(lBound);
                        DoubleCell uBoundCell = new DoubleCell(uBound);
                        rowFilter = new RangeRowFilter(columnName, lBoundCell, uBoundCell);
                    } else {
                        IntCell lBoundCell = new IntCell((int) Math.ceil(lBound));
                        IntCell uBoundCell = new IntCell((int) Math.floor(uBound));
                        rowFilter = new RangeRowFilter(columnName, lBoundCell, uBoundCell);
                    }
                    rowFilter.configure(inputSpec);
                    rangeFilterList.add(rowFilter);

                    for(DataRow tempRow : tempContainer.getTable()) {
                        double rowValue = ((DoubleCell)tempRow.getCell(currentColumnIndex)).getDoubleValue();

                    }
                }

                tempContainer = exec.createDataContainer(inputSpec);
                flagNewGroup = true;
            } else flagNewGroup = false;

            tempContainer.addRowToTable(row);
            // iterate on columns
            for(Integer currentColumnIndex : verifiedColumns.values()){
                final DoubleCell cell = (DoubleCell)row.getCell(currentColumnIndex);
                // create an new statistics entry if a new group starts
                if(flagNewGroup){
                    DescriptiveStatistics stat = new DescriptiveStatistics();
                    stat.addValue(cell.getDoubleValue());
                    statistics.put(currentColumnIndex, stat);
                } else {
                    statistics.get(currentColumnIndex).addValue(cell.getDoubleValue());
                }
            }


            //TODO: Continue here !!!
        } */

        keepContainer.close();
        discartContainer.close();
        return new BufferedDataTable[]{keepContainer.getTable(), discartContainer.getTable()};
    }
}
