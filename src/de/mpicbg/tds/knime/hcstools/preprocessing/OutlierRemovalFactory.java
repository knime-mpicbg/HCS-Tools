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
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Collection;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * @author Felix Meyenhofer (MPI-CBG)
 */
public class OutlierRemovalFactory extends NodeFactory<OutlierRemoval> {


    @Override
    public OutlierRemoval createNodeModel() {
        return new OutlierRemoval();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<OutlierRemoval> createNodeView(final int viewIndex, final OutlierRemoval nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractScreenTrafoDialog() {

            @Override
            protected void createControls() {

                addDialogComponent(new DialogComponentStringSelection(createMethodSelection(), "Method", createMethodUsageOptions()));

                addDialogComponent(new DialogComponentNumberEdit(createFactor(), "Factor"));

                addDialogComponent(new DialogComponentColumnNameSelection(createGrouping(), "Group measurments by", 0,
                        new Class[]{StringValue.class, IntValue.class, DateAndTimeValue.class}));

                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumbericFilter()));

                addDialogComponent(new DialogComponentBoolean(createRule(), "All Parameter"));
            }
        };
    }

    static SettingsModelBoolean createRule() {
        return new SettingsModelBoolean("Rule", Boolean.FALSE);
    }

    static SettingsModelDouble createFactor() {
        double factor = (double) 3;
        return new SettingsModelDouble("FactorSetting", factor);
    }

    static SettingsModelString createMethodSelection() {
        return new SettingsModelString("MethodSetting", "Mean +- SD");
    }

    static Collection<String> createMethodUsageOptions() {
        Collection<String> options = new ArrayList<String>();
        options.add("Mean +- SD");
        options.add("Boxplot");
        return options;
    }

    public static SettingsModelString createGrouping() {
        return new SettingsModelString("GroupingSetting", "Controls");
    }


}