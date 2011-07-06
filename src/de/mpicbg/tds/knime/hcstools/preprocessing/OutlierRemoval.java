/*
 * Module Name: hcstools
 * This module is a plugin for the KNIME platform <http://www.knime.org/>
 *
 * Copyright (c) 2011.
 * Max Planck Institute of Molecular Cell Biology and Genetics, Dresden
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Detailed terms and conditions are described in the license.txt.
 *     also see <http://www.gnu.org/licenses/>.
 */

package de.mpicbg.tds.knime.hcstools.preprocessing;


import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.mpicbg.tds.knime.hcstools.utils.Table2Matrix.extractMatrix;


/**
 * Removes outlier rows from a table.
 *
 * @author Felix Meyenhofer
 */


public class OutlierRemoval extends AbstractScreenTrafoModel {


    private SettingsModelString method = OutlierRemovalFactory.createMethodSelection();
    public SettingsModelString groupingColumn = OutlierRemovalFactory.createGrouping();
    private SettingsModelFilterString parameterNames = createPropReadoutSelection();
    private SettingsModelDouble factor = OutlierRemovalFactory.createFactor();
    private SettingsModelBoolean rule = OutlierRemovalFactory.createRule();


    public OutlierRemoval() {
        addSetting(method);
        addSetting(rule);
        addSetting(factor);
        addSetting(groupingColumn);
        addSetting(parameterNames);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return inSpecs;
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];
        DataTableSpec inputSpec = input.getDataTableSpec();

        // Get the parameter and make sure there all double value columns
        List<Attribute> parameter = new ArrayList<Attribute>();
        for (String item : parameterNames.getIncludeList()) {
            Attribute attribute = new InputTableAttribute(item, input);
            if (attribute.getType().equals(DoubleCell.TYPE)) {
                parameter.add(attribute);
            } else {
                logger.warn("The parameter '" + attribute.getName() + "' will not be considered for outlier removal, since it is not a DoubleCell type.");
            }
        }

        // Get the groups defined by the nominal column.
        Attribute groupingAttribute = new InputTableAttribute(this.groupingColumn.getStringValue(), input);
        Map<Object, List<DataRow>> subsets = AttributeUtils.splitRowsGeneric(input, groupingAttribute);

        // Initialize
        BufferedDataContainer container = exec.createDataContainer(inputSpec);
        int S = subsets.size();
        int s = 1;

        // Outlier analysis for each subset
        for (Object key : subsets.keySet()) {

            // Get the subset having all constraints in common
            List<DataRow> rowSubset = subsets.get(key);

            // Get the valid values
            RealMatrix data = extractMatrix(rowSubset, parameter);

            int N = data.getColumnDimension();
            int M = data.getRowDimension();
            if (M == 0) {
                logger.warn("The group '" + key + "' has no valid values and will be removed entirely'");
            } else {

                // Determine upper and lower outlier bounds
                double[] lowerBound = new double[N];
                double[] upperBound = new double[N];
                if (method.getStringValue().equals("Boxplot")) {
                    for (int c = 0; c < N; ++c) {
                        RealVector vect = data.getColumnVector(c);
                        DescriptiveStatistics stats = new DescriptiveStatistics();
                        for (double value : vect.getData()) {
                            stats.addValue(value);
                        }
                        double lowerQuantile = stats.getPercentile(25);
                        double upperQuantile = stats.getPercentile(85);
                        double whisker = factor.getDoubleValue() * Math.abs(lowerQuantile - upperQuantile);
                        lowerBound[c] = lowerQuantile - whisker;
                        upperBound[c] = upperQuantile + whisker;
                    }
                } else {
                    for (int c = 0; c < N; ++c) {
                        RealVector vect = data.getColumnVector(c);
                        double mean = StatUtils.mean(vect.getData());
                        double sd = Math.sqrt(StatUtils.variance(vect.getData()));
                        lowerBound[c] = mean - factor.getDoubleValue() * sd;
                        upperBound[c] = mean + factor.getDoubleValue() * sd;
                    }
                }

                // Remove The outlier
                if (rule.getBooleanValue()) {    // The row is only discarted if the row is an outlier in all parameter.
                    for (DataRow row : rowSubset) {
                        int c = 0;
                        for (Attribute column : parameter) {
                            Double value = (Double) column.getValue(row);
                            if ((value != null) && (lowerBound[c] <= value) && (value <= upperBound[c])) {
                                break;
                            } else {
                                c++;
                            }
                        }
                        if (c != N) {
                            container.addRowToTable(row);
                        }
                    }
                } else {                         // The row is discarted if it has a outlier for at least one parameter.
                    for (DataRow row : rowSubset) {
                        int c = 0;
                        for (Attribute column : parameter) {
                            Double value = (Double) column.getValue(row);
                            if ((value != null) && (lowerBound[c] <= value) && (value <= upperBound[c])) {
                                c++;
                            } else {
                                break;
                            }
                        }
                        if (c == N) {
                            container.addRowToTable(row);
                        }
                    }
                }
            }

            BufTableUtils.updateProgress(exec, s++, S);

        }

        container.close();
        return new BufferedDataTable[]{container.getTable()};
    }


    @Override
    protected String getAppendSuffix() {
        return "";
    }


//    protected RealMatrix extractMatrix(List<DataRow> rows, List<Attribute> params) {
//        double[][] matrix = new double[rows.size()][params.size()];
//        int nbparams = params.size();
//        int m = 0;
//        for (DataRow row : rows) {
//            int n = 0;
//            for (Attribute readout : params) {
//                Double val = readout.getDoubleAttribute(row);
//                if ((val == null) || !isValidNumber(val)) {
//                    break;
//                }
//                matrix[m][n] = val;
//                n += 1;
//            }
//            if (n == nbparams) {
//                m += 1;
//            }
//        }
//        // remove the unused rows.
//        RealMatrix rmatrix = new Array2DRowRealMatrix(matrix);
//        if (m > 0) {
//            rmatrix = rmatrix.getSubMatrix(0, m - 1, 0, nbparams - 1);
//        }
//        return rmatrix;
//    }


    private boolean isValidNumber(double nb) {
        return !(Double.isInfinite(nb) || Double.isNaN(nb));
    }


}