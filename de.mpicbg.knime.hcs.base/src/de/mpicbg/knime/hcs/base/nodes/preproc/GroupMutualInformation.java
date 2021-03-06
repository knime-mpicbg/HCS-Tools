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

package de.mpicbg.knime.hcs.base.nodes.preproc;


import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel;
import de.mpicbg.knime.hcs.base.utils.MutualInformation;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.List;
import java.util.Map;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.createTreatmentAttributeSelector;
import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.createTreatmentSelector;


/**
 * Calculate mutual information between parameters.
 *
 * @author Felix Meyenhofer
 *         <p/>
 */


public class GroupMutualInformation extends ParameterMutualInformation {

    public static final String LIBRARY_SETTINGS_NAME = "ref.ctrl";
    public static final String REFERENCE_SETTINGS_NAME = "lib.ctrl";

    private SettingsModelString library = createTreatmentSelector(LIBRARY_SETTINGS_NAME);
    private SettingsModelString reference = createTreatmentSelector(REFERENCE_SETTINGS_NAME);
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    // Constructor
    public GroupMutualInformation() {
        super(1, 1);
        addSetting(reference);
        addSetting(library);
        addSetting(treatmentAttribute);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataColumnSpec[] listSpecs = getListSpec();
        return new DataTableSpec[]{new DataTableSpec(listSpecs)};
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];

        // Get the condition attribute
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        // Get the library and reference condition names
        String libraryName = AbstractScreenTrafoModel.getAndValidateTreatment(reference);
        String referenceName = AbstractScreenTrafoModel.getAndValidateTreatment(library);

        // Get the parameter and make sure there all double value columns
        List<Attribute> parameters = getParameterList(input);

        // Split the columns according to groups contained in the condition column
        Map<String, List<DataRow>> groupedRows = AttributeUtils.splitRows(input, treatmentAttribute);
        List<DataRow> libraryRows = groupedRows.get(libraryName);
        List<DataRow> referenceRows = groupedRows.get(referenceName);

        int progress = parameters.size();
        BufTableUtils.updateProgress(exec, progress / 2, progress);

        // Initialize
        BufferedDataContainer container = exec.createDataContainer(new DataTableSpec(getListSpec()));
        MutualInformation mutualinfo = new MutualInformation();
        mutualinfo.set_base(logbase.getDoubleValue());
        mutualinfo.set_method(method.getStringValue());
        mutualinfo.set_axeslinking(linkaxes.getBooleanValue());

        DataCell[] cells = new DataCell[container.getTableSpec().getNumColumns()];
        int p = 0;

        // Calculate mutual information
        for (Attribute parameter : parameters) {

            Double[] x = getDataVec(libraryRows, parameter);
            Double[] y = getDataVec(referenceRows, parameter);
            mutualinfo.set_vectors(x, y);

            if (binning.getIntValue() == 0) {
                mutualinfo.set_binning();
            } else {
                mutualinfo.set_binning(binning.getIntValue());
            }
            int[] bins = mutualinfo.get_binning();

            Double[] res = mutualinfo.calculate();

            cells[0] = new StringCell(parameter.getName());
            cells[1] = new DoubleCell(res[0]);
            cells[2] = new DoubleCell(res[1]);
            cells[3] = new DoubleCell(res[2]);
            cells[4] = new IntCell(bins[0]);
            cells[5] = new IntCell(bins[1]);
            cells[6] = new DoubleCell(mutualinfo.get_logbase());
            cells[7] = new StringCell(mutualinfo.get_method());

            container.addRowToTable(new DefaultRow("row" + p, cells));

            BufTableUtils.updateProgress(exec, (progress + p++) / 2, progress);
            exec.checkCanceled();
        }

        container.close();
        return new BufferedDataTable[]{container.getTable()};
    }


    private DataColumnSpec[] getListSpec() {
        DataColumnSpec[] listSpecs = new DataColumnSpec[8];
        String columnName = "mutual info. " + reference.getStringValue() + " - " + library.getStringValue();
        listSpecs[0] = new DataColumnSpecCreator("Parameter name", StringCell.TYPE).createSpec();
        listSpecs[1] = new DataColumnSpecCreator(columnName, DoubleCell.TYPE).createSpec();
        listSpecs[2] = new DataColumnSpecCreator("sigma", DoubleCell.TYPE).createSpec();
        listSpecs[3] = new DataColumnSpecCreator("bias", DoubleCell.TYPE).createSpec();
        listSpecs[4] = new DataColumnSpecCreator("N bins x", DoubleCell.TYPE).createSpec();
        listSpecs[5] = new DataColumnSpecCreator("N bins y", DoubleCell.TYPE).createSpec();
        listSpecs[6] = new DataColumnSpecCreator("log-base", DoubleCell.TYPE).createSpec();
        listSpecs[7] = new DataColumnSpecCreator("method", StringCell.TYPE).createSpec();
        return listSpecs;
    }


    private Double[] getDataVec(List<DataRow> rows, Attribute param) {
        Double[] vect = new Double[rows.size()];
        int n = 0;
        for (DataRow row : rows) {
            vect[n] = param.getDoubleAttribute(row);
            n++;
        }
        return vect;
    }


}