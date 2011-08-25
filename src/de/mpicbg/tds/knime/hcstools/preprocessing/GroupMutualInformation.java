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
import de.mpicbg.tds.knime.hcstools.utils.MutualInformation;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.List;
import java.util.Map;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createTreatmentAttributeSelector;
import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createTreatmentSelector;


/**
 * Calculate mutual information between parameters.
 *
 * @author Felix Meyenhofer
 *         <p/>
 */


public class GroupMutualInformation extends ParameterMutualInformation {

    public static final String LIBRARY_SETTINGS_NAME = "pos.ctrl";
    public static final String REFERENCE_SETTINGS_NAME = "neg.ctrl";

    private SettingsModelString library = createTreatmentSelector(LIBRARY_SETTINGS_NAME);
    private SettingsModelString reference = createTreatmentSelector(REFERENCE_SETTINGS_NAME);
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    // Constructor
    public GroupMutualInformation() {
        super(1, 1);
        addSetting(reference);
        addSetting(library);
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
        int progress = parameters.size();
        BufTableUtils.updateProgress(exec, progress, progress / 2);

        // Split the columns according to groups contained in the condition column
        Map<String, List<DataRow>> groupedRows = AttributeUtils.splitRows(input, treatmentAttribute);
        List<DataRow> libraryRows = groupedRows.get(libraryName);
        List<DataRow> referenceRows = groupedRows.get(referenceName);

        // Initialize
        BufferedDataContainer container = exec.createDataContainer(new DataTableSpec(getListSpec()));
        MutualInformation mutualinfo = new MutualInformation(method.getStringValue(), binning.getIntValue(), logbase.getDoubleValue());
        DataCell[] cells = new DataCell[4];
        int p = 0;

        // Calculate mutual information
        for (Attribute parameter : parameters) {

            Double[] x = getDataVec(libraryRows, parameter);
            Double[] y = getDataVec(referenceRows, parameter);
            mutualinfo.set_vectors(x, y);
            Double[] res = mutualinfo.calculate();

            cells[0] = new StringCell(parameter.getName());
            cells[1] = new DoubleCell(res[0]);
            cells[2] = new DoubleCell(res[1]);
            cells[3] = new DoubleCell(res[2]);
            container.addRowToTable(new DefaultRow("row" + p, cells));

            BufTableUtils.updateProgress(exec, progress, (progress + p++) / 2);
            exec.checkCanceled();
        }

        container.close();
        return new BufferedDataTable[]{container.getTable()};
    }


    private DataColumnSpec[] getListSpec() {
        DataColumnSpec[] listSpecs = new DataColumnSpec[4];
        String columnName = "mutual info. " + reference.getStringValue() + " - " + library.getStringValue();
        listSpecs[0] = new DataColumnSpecCreator("Parameter name", StringCell.TYPE).createSpec();
        listSpecs[1] = new DataColumnSpecCreator(columnName, DoubleCell.TYPE).createSpec();
        listSpecs[2] = new DataColumnSpecCreator("sigma", DoubleCell.TYPE).createSpec();
        listSpecs[3] = new DataColumnSpecCreator("bias", StringCell.TYPE).createSpec();
        return listSpecs;
    }


    private Double[] getDataVec(List<DataRow> rows, Attribute param) {
        Double[] vect = new Double[rows.size()];
        int n = 0;
        for (DataRow row : rows) {
            vect[n] = param.getDoubleAttribute(row);
        }
        return vect;
    }


}