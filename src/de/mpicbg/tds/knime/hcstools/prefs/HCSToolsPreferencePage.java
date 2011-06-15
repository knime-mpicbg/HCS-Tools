/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ------------------------------------------------------------------------
 * 
 * History
 *   19.09.2007 (thiel): created
 */
package de.mpicbg.tds.knime.hcstools.prefs;

import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * @author Holger Brandl
 */
public class HCSToolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * Creates a new preference page.
     */
    public HCSToolsPreferencePage() {
        super(GRID);

        setPreferenceStore(HCSToolsBundleActivator.getDefault().getPreferenceStore());
    }


    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        addField(new IntegerFieldEditor(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_MEANS, "Minimal number of samples required to calculate mean or median.", parent));
        addField(new IntegerFieldEditor(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_DISPERSION, "Minimal number of samples required to calculate variance or MAD.", parent));
        addField(new DoubleFieldEditor(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR, "Scaling factor for MAD-statistic", parent));
        addField(new StringFieldEditor(HCSToolsPreferenceInitializer.BARCODE_PATTERNS, "Barcode patterns", parent));
    }


    public void init(final IWorkbench workbench) {
        // nothing to do
    }
}