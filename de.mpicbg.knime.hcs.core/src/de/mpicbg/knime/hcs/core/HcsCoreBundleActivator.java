package de.mpicbg.knime.hcs.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;


public class HcsCoreBundleActivator extends AbstractUIPlugin {
	
	/** Make sure that this *always* matches the ID in plugin.xml */
	public static final String PLUGIN_ID = "de.mpicbg.knime.hcs.core";
	/** Shared instance */
	private static HcsCoreBundleActivator plugin;

	/**
	 * Constructor
	 */
	public HcsCoreBundleActivator() {
		plugin = this;
	}
	
	/**
     * Returns the shared instance.
     *
     * @return Singleton instance of the Plugin
     */
    public static HcsCoreBundleActivator getDefault() {
        return plugin;
    }

}
