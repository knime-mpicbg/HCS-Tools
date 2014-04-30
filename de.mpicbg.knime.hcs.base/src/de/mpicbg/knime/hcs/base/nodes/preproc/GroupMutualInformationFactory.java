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

import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoDialog;
import de.mpicbg.knime.hcs.base.utils.TdsNumericFilter;
import de.mpicbg.knime.knutils.AbstractConfigDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.Arrays;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.SELECT_TREATMENT_ADVICE;


/**
 * @author Felix Meyenhofer (MPI-CBG)
 */
public class GroupMutualInformationFactory extends ParameterMutualInformationFactory {


    @Override
    public ParameterMutualInformation createNodeModel() {
        return new GroupMutualInformation();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<ParameterMutualInformation> createNodeView(final int viewIndex, final ParameterMutualInformation nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractConfigDialog() {


            @Override
            protected void createControls() {

                // Parameter for the mutual information algorithm
                setHorizontalPlacement(true);
                createNewGroup("Mutual information algorithm settings");
                addDialogComponent(new DialogComponentStringSelection(createMethodSelection(), "Method", createMethodUsageOptions()));
                addDialogComponent(new DialogComponentNumberEdit(createLogBase(), "Logarithmic base"));
                addDialogComponent(new DialogComponentNumberEdit(createBinning(), "Binning"));
                addDialogComponent(new DialogComponentNumberEdit(createThrehold(), "Threshold"));
                addDialogComponent(new DialogComponentBoolean(createAxesSettings(), "Axes linkage"));

                // Group pselection
                createNewGroup("Data grouping");
                DialogComponentStringSelection referenceGroupName = new DialogComponentStringSelection(
                        createGroupSelector(GroupMutualInformation.REFERENCE_SETTINGS_NAME), "Reference", Arrays.asList(SELECT_TREATMENT_ADVICE), true);
                DialogComponentStringSelection libraryGroupName = new DialogComponentStringSelection(
                        createGroupSelector(GroupMutualInformation.LIBRARY_SETTINGS_NAME), "Library", Arrays.asList(SELECT_TREATMENT_ADVICE), true);
                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(referenceGroupName, libraryGroupName));
                addDialogComponent(referenceGroupName);
                addDialogComponent(libraryGroupName);

                // Parameter selection
                setHorizontalPlacement(false);
                createNewGroup("Paramter subset");
                addDialogComponent(new DialogComponentColumnFilter(ParameterMutualInformationFactory.createParameterFilterSetting(), 0, true, new TdsNumericFilter()));

            }
        };
    }


    public static SettingsModelString createGroupSelector(String setting) {
        return new SettingsModelString(setting, "group id");
    }


}