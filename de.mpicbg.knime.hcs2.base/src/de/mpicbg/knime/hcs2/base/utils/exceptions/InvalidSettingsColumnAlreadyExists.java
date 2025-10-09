package de.mpicbg.knime.hcs2.base.utils.exceptions;

import org.knime.core.node.InvalidSettingsException;

@SuppressWarnings("serial")
public class InvalidSettingsColumnAlreadyExists extends InvalidSettingsException {

	public InvalidSettingsColumnAlreadyExists(final String columnName) {
		super("Column with name: '" + columnName + "' already exists.");
	}

}
