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

import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import de.mpicbg.tds.knime.hcstools.utils.MadStatistic;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class HCSToolsPreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String MIN_SAMPLE_NUMBER_FOR_MEANS = "min.samples.means";
    public static final String MIN_SAMPLE_NUMBER_FOR_DISPERSION = "min.samples.dispesion";
    public static final String MAD_SCALING_FACTOR = "mad.scaling.factor";

    public static final String BARCODE_PATTERNS = "barcode.patterns";


    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = HCSToolsBundleActivator.getDefault().getPreferenceStore();

        store.setDefault(MIN_SAMPLE_NUMBER_FOR_MEANS, 5);
        store.setDefault(MIN_SAMPLE_NUMBER_FOR_DISPERSION, 8);
        store.setDefault(MAD_SCALING_FACTOR, MadStatistic.MAD_GAUSS_FACTOR);

        store.setDefault(BARCODE_PATTERNS, BarcodeParserFactory.ASSAY_PLATE_PATTERN + ";" + BarcodeParserFactory.LIB_PLATE_BARCODE_PATTERN + ";" + BarcodeParserFactory.ASSAY_PLATE_PATTERN_OLD);
    }
}