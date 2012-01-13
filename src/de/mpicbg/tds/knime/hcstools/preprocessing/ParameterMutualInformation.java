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


import de.mpicbg.tds.knime.hcstools.utils.MutualInformation;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Calculate mutual information between parameters.
 *
 * @author Felix Meyenhofer
 *         <p/>
 *         TODO: Add the view from the "Linear Correlation" Node for matrix visualization.
 */


public class ParameterMutualInformation extends AbstractNodeModel {


    public static final SettingsModelString method = ParameterMutualInformationFactory.createMethodSelection();
    public static final SettingsModelFilterString parameterNames = ParameterMutualInformationFactory.createParameterFilterSetting();
    public static final SettingsModelDouble logbase = ParameterMutualInformationFactory.createLogBase();
    public static final SettingsModelDouble threshold = ParameterMutualInformationFactory.createThrehold();
    public static final SettingsModelInteger binning = ParameterMutualInformationFactory.createBinning();
    public static final SettingsModelBoolean linkaxes = ParameterMutualInformationFactory.createAxesSettings();


    // Constructor
    public ParameterMutualInformation() {
        this(1, 2);
    }

    public ParameterMutualInformation(int portIn, int portOut) {
        super(portIn, portOut);
        addSetting(method);
        addSetting(logbase);
        addSetting(parameterNames);
        addSetting(threshold);
        addSetting(binning);
        addSetting(linkaxes);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        // Parameter list
        DataColumnSpec[] listSpecs = getListSpec();
        // Parameter matrix
        DataColumnSpec[] matrixSpecs = getMatrixSpec();

        return new DataTableSpec[]{new DataTableSpec(listSpecs), new DataTableSpec(matrixSpecs)};
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];

        // Get the parameter and make sure there all double value columns
        List<Attribute> parameters = getParameterList(input);

        // Initialize
        int Np = parameters.size();
        int iterations = Np * 2;
        double[][] mutmatrix = new double[Np][Np];
        double[][] sigmatrix = new double[Np][Np];
        ;
        double[][] biamatrix = new double[Np][Np];
        ;

        MutualInformation mutualinfo = new MutualInformation();
        mutualinfo.set_base(logbase.getDoubleValue());
        mutualinfo.set_method(method.getStringValue());
        mutualinfo.set_axeslinking(linkaxes.getBooleanValue());
        if (binning.getIntValue() > 0)
            mutualinfo.set_binning(binning.getIntValue());

        // Load data.
        Double[][] table = new Double[Np][input.getRowCount()];
        int j = 0;
        for (DataRow row : input) {
            int i = 0;
            for (Attribute param : parameters) {
                table[i][j] = param.getDoubleAttribute(row);
                i++;
            }
            j++;
            exec.checkCanceled();
        }
        BufTableUtils.updateProgress(exec, Np, iterations);

        // Calculate mutual information
        for (int a = 0; a < Np; a++) {
            mutualinfo.set_xvector(table[a]);

            for (int b = a; b < Np; b++) {
                mutualinfo.set_yvector(table[b]);

                // Calculate the mutual info.
                Double[] res = mutualinfo.calculate();

                // Put it into the output matrix
                mutmatrix[a][b] = res[0];
                mutmatrix[b][a] = res[0];
                sigmatrix[a][b] = res[1];
                sigmatrix[b][a] = res[1];
                biamatrix[a][b] = res[2];
                biamatrix[b][a] = res[2];
            }
            BufTableUtils.updateProgress(exec, Np + a, iterations);
            exec.checkCanceled();
        }

        // Create Tables
        BufferedDataContainer matrixContainer = exec.createDataContainer(new DataTableSpec(getMatrixSpec()));
        BufferedDataContainer listContainer = exec.createDataContainer(new DataTableSpec(getListSpec()));
        double thresh = threshold.getDoubleValue();
        int numListCol = listContainer.getTableSpec().getNumColumns();

        for (int a = 0; a < Np; a++) {

            // Initialize
            DataCell[] matrixCells = new DataCell[Np];
            DataCell[] listCells = new DataCell[numListCol];
            String similars = "";
            DescriptiveStatistics mutstats = new DescriptiveStatistics();
            DescriptiveStatistics sigstats = new DescriptiveStatistics();
            ;
            DescriptiveStatistics biastats = new DescriptiveStatistics();
            ;

            // Create matrix rows and collect values for statistics.
            for (int b = 0; b < Np; b++) {
                matrixCells[b] = new DoubleCell(mutmatrix[a][b]);
                if (a != b) {
                    mutstats.addValue(mutmatrix[a][b]);
                    sigstats.addValue(sigmatrix[a][b]);
                    biastats.addValue(biamatrix[a][b]);
                    if (mutmatrix[a][b] > thresh) {
                        similars += parameters.get(b).getName() + ",";
                    }
                }
            }

            // Create matrix row
            DataRow matrixRow = new DefaultRow(parameters.get(a).getName(), matrixCells);
            matrixContainer.addRowToTable(matrixRow);

            // Create list row
            listCells[0] = new StringCell(parameters.get(a).getName());
            listCells[1] = new DoubleCell(mutstats.getMin());
            listCells[2] = new DoubleCell(mutstats.getMean());
            listCells[3] = new DoubleCell(mutstats.getMax());
            listCells[4] = new DoubleCell(sigstats.getGeometricMean());
            listCells[5] = new DoubleCell(biastats.getMean());
            listCells[6] = new StringCell(similars);
            DataRow listRow = new DefaultRow("row" + a, listCells);
            listContainer.addRowToTable(listRow);

            exec.checkCanceled();
        }

        matrixContainer.close();
        listContainer.close();
        return new BufferedDataTable[]{listContainer.getTable(), matrixContainer.getTable()};
    }


    private DataColumnSpec[] getListSpec() {
        DataColumnSpec[] listSpecs = new DataColumnSpec[7];
        listSpecs[0] = new DataColumnSpecCreator("Parameter name", StringCell.TYPE).createSpec();
        listSpecs[1] = new DataColumnSpecCreator("Min mutual info.", DoubleCell.TYPE).createSpec();
        listSpecs[2] = new DataColumnSpecCreator("Mean mutual info.", DoubleCell.TYPE).createSpec();
        listSpecs[3] = new DataColumnSpecCreator("Max mutual info.", DoubleCell.TYPE).createSpec();
        listSpecs[4] = new DataColumnSpecCreator("Geo.mean sigma", DoubleCell.TYPE).createSpec();
        listSpecs[5] = new DataColumnSpecCreator("Mean bias.", DoubleCell.TYPE).createSpec();
        listSpecs[6] = new DataColumnSpecCreator("Similar parameter", StringCell.TYPE).createSpec();
        return listSpecs;
    }


    private DataColumnSpec[] getMatrixSpec() {
        List<String> params = parameterNames.getIncludeList();
        DataColumnSpec[] matrixSpecs = new DataColumnSpec[params.size()];
        int n = 0;
        for (String parameter : params) {
            matrixSpecs[n] = new DataColumnSpecCreator(parameter, DoubleCell.TYPE).createSpec();
            n += 1;
        }
        return matrixSpecs;
    }


    public List<Attribute> getParameterList(BufferedDataTable table) {
        List<Attribute> parameters = new ArrayList<Attribute>();
        for (String item : parameterNames.getIncludeList()) {
            Attribute attribute = new InputTableAttribute(item, table);
            if (attribute.getType().isCompatible(DoubleValue.class)) {
                parameters.add(attribute);
            } else {
                logger.warn("The parameters '" + attribute.getName() + "' will not be considered, since it is not a DoubleCell type.");
            }
        }
        return parameters;
    }


}