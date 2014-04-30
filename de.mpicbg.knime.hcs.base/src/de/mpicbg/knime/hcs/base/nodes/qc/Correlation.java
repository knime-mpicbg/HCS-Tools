package de.mpicbg.knime.hcs.base.nodes.qc;


import de.mpicbg.knime.hcs.base.HCSSettingsFactory;
import de.mpicbg.knime.knutils.*;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.*;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.*;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * Caluclates correlation in many misterious ways.
 *
 * @author Felix Meyenhofer
 *         <p/>
 *         initial set.
 * @deprecated
 */

public class Correlation extends AbstractNodeModel {


    public static final String subset1ColumnName = "Subset_1";
    public static final String subset2ColumnName = "Subset_2";
    public static final String corrColumnName = "CorrCoeff";

    private SettingsModelString correlationMethod = CorrelationFactory.createCorrelationMethodSelection();
    private SettingsModelString columnFilterUsage = CorrelationFactory.createColumnFilterUsageSelection();
    private SettingsModelFilterString constrainingColumnNames = CorrelationFactory.createConstraintsSelection();
    private SettingsModelFilterString parameterNames = createPropReadoutSelection();
    private SettingsModelString subsetColumnName = HCSSettingsFactory.createGroupBy();


    public Correlation() {
        addSetting(correlationMethod);
        addSetting(columnFilterUsage);
        addSetting(subsetColumnName);
        addSetting(constrainingColumnNames);
        addSetting(parameterNames);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];

        // Get the grouping of the measurements
        List<String> subsetColumns = new ArrayList<String>();
        subsetColumns.add(subsetColumnName.getStringValue());
        Map<String, ArrayList<String>> subsets = getNominalValues(input, subsetColumns);

        // Get the parameter
        List<Attribute> readouts = new ArrayList<Attribute>();
        for (String parameter : parameterNames.getIncludeList()) {
            readouts.add(new InputTableAttribute(parameter, input));
        }

        // Get the groups defined by the serveral nominal columns.
        List<String> constraintColumnNames = constrainingColumnNames.getIncludeList();
        Map<String, ArrayList<String>> constraints = getNominalValues(input, constraintColumnNames);

        // Initialize the output table.
        BufferedDataTable correlationTable = prepareCorrelationTable(exec, subsets.keySet().toArray(), constraints);
        TableUpdateCache updateCache = new TableUpdateCache(correlationTable.getDataTableSpec());

        // Loop through the lines of the correlation table.
        int index = 0;
        for (DataRow tableRow : correlationTable) {
            exec.checkCanceled();

            // Get a subset of the variable.
            BufferedDataTable conditionSubset;
            if ((constraintColumnNames.size() > 0) && columnFilterUsage.getStringValue().equals("batch-processing")) {
                List<String> constraintValues = new ArrayList<String>();
                for (String constrainingColumn : constraintColumnNames) {
                    Attribute attr = new InputTableAttribute(constrainingColumn, correlationTable);
                    constraintValues.add(attr.getNominalAttribute(tableRow));
                }
                conditionSubset = getTableSubset(exec, input, constraintColumnNames, constraintValues);
            } else {
                conditionSubset = input;
            }

            // Get the subsets.
            Attribute subset1Attibute = new InputTableAttribute(subset1ColumnName, correlationTable);
            Attribute subset2Attibute = new InputTableAttribute(subset2ColumnName, correlationTable);
            String subset1Name = subset1Attibute.getNominalAttribute(tableRow);
            String subset2Name = subset2Attibute.getNominalAttribute(tableRow);
            BufferedDataTable subset1 = getTableSubset(exec, conditionSubset, subsetColumns, subsets.get(subset1Name));
            BufferedDataTable subset2 = getTableSubset(exec, conditionSubset, subsetColumns, subsets.get(subset2Name));

            // Apply the constraintf for the measurement association.
            if ((constraintColumnNames.size() > 0) && columnFilterUsage.getStringValue().equals("measurement-association")) {
                BufferedDataTable[] output = assotiateSubsetMeasurements(exec, subset1, subset2, constraints);
                subset1 = output[0];
                subset2 = output[1];
            }

            // Assemble the correlation matrix depending on the number of parameter.
            double correlation = 0;
            if (readouts.size() == 1) {
                correlation = monoParametricCorrelation(subset1, subset2, readouts, correlationMethod.getStringValue());

            } else {
                if (correlationMethod.getStringValue().equals("Pearson")) {
                    correlation = mulitparametricPearsonCorrelation(subset1, subset2, readouts);
                } else if (correlationMethod.getStringValue().equals("Spearman")) {
                    correlation = mulitparametricSpearmanCorrelation(subset1, subset2, readouts);
                }
            }

            // parse the values in the ouput table.
            Attribute attribute = new Attribute(corrColumnName, DoubleCell.TYPE);
            updateCache.add(tableRow, attribute, isValidNumber(correlation) ? new DoubleCell(correlation) : DataType.getMissingCell());

            BufTableUtils.updateProgress(exec, index++, subsets.size());
        }

        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(correlationTable, c, exec);

        return new BufferedDataTable[]{out};
    }

    protected double mulitparametricPearsonCorrelation(BufferedDataTable sbs1, BufferedDataTable sbs2, List<Attribute> param) {
        DescriptiveStatistics corr = new DescriptiveStatistics();
        for (DataRow row1 : sbs1) {
            double[] v1 = getMeasurementTrace(row1, param);

            for (DataRow row2 : sbs2) {
                double[] v2 = getMeasurementTrace(row2, param);
                corr.addValue(new PearsonsCorrelation().correlation(v1, v2));
            }
        }
        return corr.getMean();
    }

    protected double mulitparametricSpearmanCorrelation(BufferedDataTable sbs1, BufferedDataTable sbs2, List<Attribute> param) {
        DescriptiveStatistics corr = new DescriptiveStatistics();
        for (DataRow row1 : sbs1) {
            double[] v1 = getMeasurementTrace(row1, param);

            for (DataRow row2 : sbs2) {
                double[] v2 = getMeasurementTrace(row2, param);
                corr.addValue(new SpearmansCorrelation().correlation(v1, v2));
            }
        }
        return corr.getMean();
    }


    protected double[] getMeasurementTrace(DataRow row, List<Attribute> params) {
        double[] trace = new double[params.size()];
        int index = 0;
        for (Attribute param : params) {
            Double val = param.getDoubleAttribute(row);
            if (isValidNumber(val)) {
                trace[index++] = val;
            } else {
                trace[index++] = Double.NaN;
            }
        }
        return trace;
    }


    protected double monoParametricCorrelation(BufferedDataTable sbs1, BufferedDataTable sbs2, List<Attribute> param, String method) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.addValue(sbs1.getRowCount());
        stats.addValue(sbs2.getRowCount());
        double numberOfRows = stats.getMin();

        RealVector v1 = getMeasurmentsVector(sbs1, param.get(0));
        v1.getSubVector(0, (int) numberOfRows);
        RealVector v2 = getMeasurmentsVector(sbs2, param.get(0));
        v2.getSubVector(0, (int) numberOfRows);

        double corr = 0;
        if (method.equals("Pearson")) {
            corr = new PearsonsCorrelation().correlation(v1.getData(), v2.getData());
        } else if (method.equals("Spearman")) {
            corr = new SpearmansCorrelation().correlation(v1.getData(), v2.getData());
        }

        return corr;
    }


    protected RealVector getMeasurmentsVector(BufferedDataTable inputTable, Attribute param) {
        double[] vector = new double[inputTable.getRowCount()];
        int m = 0;
        for (DataRow row : inputTable) {
            Double val = param.getDoubleAttribute(row);
            if (isValidNumber(val)) {
                vector[m++] = val;
            }
        }
        return new ArrayRealVector(vector);
    }


    protected static BufferedDataTable getTableSubset(ExecutionContext exec, BufferedDataTable table, List<String> conditionNames, List<String> conditionValues) {
        DataTableSpec tableSpec = table.getDataTableSpec();
        BufferedDataContainer subset = exec.createDataContainer(tableSpec);
        for (DataRow dataRow : table) {
            // Check if the conditions match
            boolean allMatched = true;
            for (int i = 0; i < tableSpec.getNumColumns(); i++) {
                DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
                if (conditionNames.contains(columnSpec.getName())) {
                    DataCell dataCell = dataRow.getCell(i);
                    if (!conditionValues.contains(dataCell.toString())) {
                        allMatched = false;
                        break;
                    }
                }
            }
            if (allMatched) {
                subset.addRowToTable(dataRow);
            }
        }
        subset.close();
        return subset.getTable();
    }


    private List<List<DataCell>> matchCondition(ExecutionContext exec, BufferedDataTable table, List<String> columnNames, ArrayList<String> columnValues) {
        DataTableSpec tableSpec = table.getDataTableSpec();
        List<List<DataCell>> subset = new ArrayList<List<DataCell>>();
        for (DataRow dataRow : table) {
            // Check if the conditions match
            boolean allMatched = true;
            List<DataCell> cells = new ArrayList<DataCell>();
            for (int i = 0; i < tableSpec.getNumColumns(); i++) {
                DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
                cells.add(dataRow.getCell(i));
                if (columnNames.contains(columnSpec.getName())) {
                    if (!columnValues.contains(dataRow.getCell(i).toString())) {
                        allMatched = false;
                        break;
                    }
                }
            }
            if (allMatched) {
                subset.add(cells);
            }
        }
        return subset;
    }


    private BufferedDataTable[] assotiateSubsetMeasurements(ExecutionContext exec, BufferedDataTable sbs1in, BufferedDataTable sbs2in, Map<String, ArrayList<String>> conds) {
        DataTableSpec tableSpec = sbs1in.getDataTableSpec();
        BufferedDataContainer sbs1out = exec.createDataContainer(tableSpec);
        BufferedDataContainer sbs2out = exec.createDataContainer(tableSpec);
        int index = 0;
        for (String cond : conds.keySet()) {
            List<String> columnNames = Arrays.asList(cond.split("_"));
            ArrayList<String> columnValues = conds.get(cond);
            List<List<DataCell>> match1 = matchCondition(exec, sbs1in, columnNames, columnValues);
            List<List<DataCell>> match2 = matchCondition(exec, sbs2in, columnNames, columnValues);

            //TODO bootstrap the conditions, for better correlation estimation.
            int minSize = Math.min(match1.size(), match2.size());
            for (int r = 1; r < minSize; r++) {
                DataRow row1 = new DefaultRow(new RowKey("Row " + index++), match1.get(r));
                sbs1out.addRowToTable(row1);
                DataRow row2 = new DefaultRow(new RowKey("Row " + index++), match2.get(r));
                sbs2out.addRowToTable(row2);
            }
        }
        sbs1out.close();
        sbs2out.close();
        BufferedDataTable[] output = new BufferedDataTable[2];
        output[0] = sbs1out.getTable();
        output[1] = sbs2out.getTable();

        return output;
    }


    protected static Map<String, ArrayList<String>> getNominalValues(BufferedDataTable table, List<String> columns) {
        Map<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
        DataTableSpec tableSpec = table.getDataTableSpec();
        for (DataRow dataRow : table) {

            String groupName = "";
            ArrayList<String> groupValues = new ArrayList<String>();
            for (int i = 0; i < tableSpec.getNumColumns(); i++) {
                DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
                if (columns.contains(columnSpec.getName())) {
                    DataCell dataCell = dataRow.getCell(i);
                    groupName += "_" + dataCell.toString();
                    groupValues.add(dataCell.toString());
                }
            }
            groups.put(groupName, groupValues);
        }
        return groups;
    }


    private boolean isValidNumber(double nb) {
        return !(Double.isInfinite(nb) || Double.isNaN(nb));
    }


    private DataTableSpec prepareTableSpecification(String option) {
        int nbCol = 2;
        if (option.equals("init")) {
            nbCol = 3;
        }
        DataColumnSpec[] allColSpecs;
        List<String> constrCols = constrainingColumnNames.getIncludeList();

        if (columnFilterUsage.getStringValue().equals("batch-processing") && (constrCols.size() > 0)) {
            allColSpecs = new DataColumnSpec[nbCol + constrCols.size()];
            int index = 0;
            for (String constrCol : constrCols) {
                allColSpecs[2 + index++] = new DataColumnSpecCreator(constrCol, StringCell.TYPE).createSpec();
            }
        } else {
            allColSpecs = new DataColumnSpec[nbCol];
        }

        allColSpecs[0] = new DataColumnSpecCreator(subset1ColumnName, StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(subset2ColumnName, StringCell.TYPE).createSpec();
        if (option.equals("init")) {
            allColSpecs[allColSpecs.length - 1] = new DataColumnSpecCreator(corrColumnName, StringCell.TYPE).createSpec();
        }

        return new DataTableSpec(allColSpecs);
    }


    private BufferedDataTable prepareCorrelationTable(ExecutionContext exec, Object[] subsetNames, Map<String, ArrayList<String>> columnSets) {
        // the table will have the following columns:
        DataTableSpec outputSpec = prepareTableSpecification("pre-fill");
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        // Get the names of the constraining columns.
        Set<String> columnSetNames = columnSets.keySet();

        // fill in the first three columns.
        int index = 0;
        for (int m = 0; m < subsetNames.length - 1; m++) {
            Object subsetName1 = subsetNames[m];

            for (int n = m + 1; n < subsetNames.length; n++) {
                Object subsetName2 = subsetNames[n];

                if (columnFilterUsage.getStringValue().equals("batch-processing") && (columnSets.size() > 0)) {

                    for (String key : columnSetNames) {
                        ArrayList<String> columnValues = columnSets.get(key);

                        List<DataCell> cells = new ArrayList<DataCell>();
                        cells.add(new StringCell(subsetName1.toString()));
                        cells.add(new StringCell(subsetName2.toString()));
                        for (String columnValue : columnValues) {
                            cells.add(new StringCell(columnValue));
                        }

                        DataRow row = new DefaultRow(new RowKey("Row " + index++), cells);
                        container.addRowToTable(row);
                    }
                } else {
                    DataCell[] cells = new DataCell[]{new StringCell(subsetName1.toString()), new StringCell(subsetName2.toString())};
                    DataRow row = new DefaultRow(new RowKey("Row " + index++), cells);
                    container.addRowToTable(row);

                }
            }
        }
        // once we are done, we close the container and return its table
        container.close();
        return container.getTable();
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{prepareTableSpecification("init")};
    }


}