package de.mpicbg.knime.hcs2.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class HCSCoreBundleActivator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		HCSCoreBundleActivator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		HCSCoreBundleActivator.context = null;
	}

}
