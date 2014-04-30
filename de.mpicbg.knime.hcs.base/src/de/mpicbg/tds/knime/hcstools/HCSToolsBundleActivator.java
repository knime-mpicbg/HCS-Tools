/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package de.mpicbg.tds.knime.hcstools;

import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * This is the eclipse bundle activator. Note: KNIME node developers probably won't have to do anything in here, as this
 * class is only needed by the eclipse platform/plugin mechanism. If you want to move/rename this file, make sure to
 * change the plugin.xml file in the project root directory accordingly.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class HCSToolsBundleActivator extends AbstractUIPlugin {

    /**
     * Make sure that this *always* matches the ID in plugin.xml.
     */
    public static final String PLUGIN_ID = "de.mpicbg.tds.knime.hcstools";

    // The shared instance.
    private static HCSToolsBundleActivator plugin;


    /**
     * The constructor.
     */
    public HCSToolsBundleActivator() {
        plugin = this;
    }


    /**
     * Returns the shared instance.
     *
     * @return Singleton instance of the Plugin
     */
    public static HCSToolsBundleActivator getDefault() {
        return plugin;
    }

}

