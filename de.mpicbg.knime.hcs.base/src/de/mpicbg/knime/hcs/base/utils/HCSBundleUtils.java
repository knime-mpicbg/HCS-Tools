/**
 * 
 */
package de.mpicbg.knime.hcs.base.utils;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import de.mpicbg.knime.hcs.base.HCSToolsBundleActivator;
import de.mpicbg.knime.hcs.base.prefs.BarcodePatternsEditor;
import de.mpicbg.knime.hcs.base.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParserFactory;

/**
 * @author Antje Janosch
 * put here public static methods which are plugin/bundle dependent
 *
 */
public class HCSBundleUtils {
	
	/**
	 * retrieves a barcode parser factory from preference settings
	 * @return
	 */
	public static BarcodeParserFactory loadFactory() {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();

        List<String> patterns = BarcodePatternsEditor.getPatternList(prefStore.getString(HCSToolsPreferenceInitializer.BARCODE_PATTERNS));
        return new BarcodeParserFactory(patterns);
    }
}
