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

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;


/**
 * @author Felix Meyenhofer (MPI-CBG)
 * @deprecated
 */


public class RangeFilterFactory extends NodeFactory<RangeFilter> {


    @Override
    public RangeFilter createNodeModel() {
        return new RangeFilter();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<RangeFilter> createNodeView(final int viewIndex, final RangeFilter nodeModel) {
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

                addDialogComponent(new DialogComponentNumberEdit(createLowerBoundSetting(), "Lower Bound"));

                addDialogComponent(new DialogComponentNumberEdit(createUpperBoundSetting(), "Upper Bound"));

                addDialogComponent(new DialogComponentColumnFilter(createParameterFilterSetting(), 0, true, new TdsNumericFilter()));

                addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean("Rule", Boolean.FALSE), "All Parameter"));
            }
        };
    }

    static SettingsModelBoolean createRuleSetting() {
        return new SettingsModelBoolean("Rule", Boolean.FALSE);
    }

    static SettingsModelDouble createLowerBoundSetting() {
        return new SettingsModelDouble("LowerBoundSetting", Double.NEGATIVE_INFINITY);
    }

    static SettingsModelDouble createUpperBoundSetting() {
        return new SettingsModelDouble("UpperBoundSetting", Double.POSITIVE_INFINITY);
    }

    static SettingsModelFilterString createParameterFilterSetting() {
        return new SettingsModelFilterString("ParameterSetting");
    }


}