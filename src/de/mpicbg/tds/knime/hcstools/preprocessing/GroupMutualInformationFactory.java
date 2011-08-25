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

import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumbericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Arrays;


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
//                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createGroupBy(), "Group by", 0, StringValue.class));

                // Parameter for the mutual information algorithm
                addDialogComponent(new DialogComponentStringSelection(createMethodSelection(), "Method", createMethodUsageOptions()));
                addDialogComponent(new DialogComponentNumberEdit(createLogBase(), "Logarithmic base"));
                addDialogComponent(new DialogComponentNumberEdit(createBinning(), "Binning"));
                addDialogComponent(new DialogComponentNumberEdit(createThrehold(), "Threshold"));

                // Group pselection
                DialogComponentStringListSelection multiPosCtrlsProperty =
                        new DialogComponentStringListSelection(createMultiCtls(de.mpicbg.tds.knime.hcstools.preprocessing.GroupMutualInformation.REFERENCE_SETTINGS_NAME), "Reference", new ArrayList<String>(), false, 1);
                DialogComponentStringListSelection multiNegCtrlProperty =
                        new DialogComponentStringListSelection(createMultiCtls(de.mpicbg.tds.knime.hcstools.preprocessing.GroupMutualInformation.LIBRARY_SETTINGS_NAME), "Library", new ArrayList<String>(), false, 1);
                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(multiPosCtrlsProperty, multiNegCtrlProperty));
                addDialogComponent(multiPosCtrlsProperty);
                addDialogComponent(multiNegCtrlProperty);

//                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
//                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumbericFilter()));

                // Parameter selection
                addDialogComponent(new DialogComponentColumnFilter(ParameterMutualInformationFactory.createParameterFilterSetting(), 0, true, new TdsNumbericFilter()));

            }
        };
    }

//    static SettingsModelDouble createLogBase() {
//        double factor = (double) 2;
//        return new SettingsModelDouble("FactorSetting", factor);
//    }
//
//    static SettingsModelDouble createThrehold() {
//        double factor = (double) 2;
//        return new SettingsModelDouble("ThresholdSetting", factor);
//    }
//
//    static SettingsModelInteger createBinning() {
//        int factor = 0;
//        return new SettingsModelInteger("BinningSetting", factor);
//    }
//
//    static SettingsModelString createMethodSelection() {
//        return new SettingsModelString("MethodSetting", "unbiased");
//    }
//
//    static Collection<String> createMethodUsageOptions() {
//        Collection<String> options = new ArrayList<String>();
//        options.add("unbiased");
//        options.add("biased");
//        options.add("mmse");
//        return options;
//    }

//    static SettingsModelFilterString createParameterFilterSetting() {
//        return new SettingsModelFilterString("ParameterSetting");
//    }


    static SettingsModelStringArray createMultiCtls(String propName) {
        return new SettingsModelStringArray(propName, new String[8]);
    }

}