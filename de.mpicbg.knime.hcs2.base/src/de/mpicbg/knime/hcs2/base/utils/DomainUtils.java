package de.mpicbg.knime.hcs2.base.utils;

import org.knime.core.data.DataColumnSpec;

public final class DomainUtils {
	
	public static boolean hasDomainValues(DataColumnSpec cspec) {
		return cspec.getDomain().hasBounds() || cspec.getDomain().hasValues();
	}

}
