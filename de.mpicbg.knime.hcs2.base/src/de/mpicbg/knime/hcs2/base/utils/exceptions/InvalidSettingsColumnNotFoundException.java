package de.mpicbg.knime.hcs2.base.utils.exceptions;

import org.knime.core.node.InvalidSettingsException;

@SuppressWarnings("serial")
public class InvalidSettingsColumnNotFoundException extends InvalidSettingsException {

	public InvalidSettingsColumnNotFoundException(String missingColumn) {
		super("Column '" + missingColumn + "' could not be found in input table.");
	}

}
