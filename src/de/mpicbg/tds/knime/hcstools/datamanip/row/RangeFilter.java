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

package de.mpicbg.tds.knime.hcstools.datamanip.row;


import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import java.util.ArrayList;
import java.util.List;


/**
 * Removes outlier rows from a table.
 *
 * @author Felix Meyenhofer
 */


public class RangeFilter extends AbstractNodeModel {


    private SettingsModelFilterString parameterNames = RangeFilterFactory.createParameterFilterSetting();
    private SettingsModelDouble lowerBoundSetting = RangeFilterFactory.createLowerBoundSetting();
    private SettingsModelDouble upperBoundSetting = RangeFilterFactory.createUpperBoundSetting();
    private SettingsModelBoolean rule = RangeFilterFactory.createRuleSetting();


    public RangeFilter() {
        super(1, 2);
        addSetting(rule);
        addSetting(lowerBoundSetting);
        addSetting(upperBoundSetting);
        addSetting(parameterNames);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0], inSpecs[0]};
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];
        DataTableSpec inputSpec = input.getDataTableSpec();

        // Get the parameter and make sure there all double value columns
        List<Attribute> parameter = new ArrayList<Attribute>();
        for (String item : parameterNames.getIncludeList()) {
            Attribute attribute = new InputTableAttribute(item, input);
            if (attribute.getType().isCompatible(DoubleValue.class)) {
                parameter.add(attribute);
            } else {
                logger.warn("The parameter '" + attribute.getName() + "' will not be considered for outlier removal, since it is not a DoubleCell type.");
            }
        }

        // Initialize
        BufferedDataContainer retained = exec.createDataContainer(inputSpec);
        BufferedDataContainer discated = exec.createDataContainer(inputSpec);
        Double lowerBound = lowerBoundSetting.getDoubleValue();
        Double upperBound = upperBoundSetting.getDoubleValue();
        int N = parameter.size();
        int S = input.getRowCount();
        int s = 1;

        // Filter rows
        for (DataRow row : input) {

            if (rule.getBooleanValue()) {    // The row is only discarted if the row is an outlier in all parameter.
                int c = 0;
                for (Attribute column : parameter) {
                    Double value = (Double) column.getValue(row);
                    if ((value != null) && (lowerBound < value) && (value < upperBound)) {
                        break;
                    } else {
                        c++;
                    }
                }
                if (c != N) {
                    retained.addRowToTable(row);
                } else {
                    discated.addRowToTable(row);
                }

            } else {                         // The row is discarted if it has a outlier for at least one parameter.
                int c = 0;
                for (Attribute column : parameter) {
                    Double value = (Double) column.getValue(row);
                    if ((value != null) && (lowerBound < value) && (value < upperBound)) {
                        c++;
                    } else {
                        break;
                    }
                }
                if (c == N) {
                    retained.addRowToTable(row);
                } else {
                    discated.addRowToTable(row);
                }
            }

            BufTableUtils.updateProgress(exec, s++, S);

        }

        retained.close();
        discated.close();
        return new BufferedDataTable[]{retained.getTable(), discated.getTable()};
    }


}