package de.mpicbg.knime.hcs2.base.utils.exceptions;

import org.knime.core.node.InvalidSettingsException;

@SuppressWarnings("serial")
public class InvalidSettingsWrongDataTypeException extends InvalidSettingsException {

	public InvalidSettingsWrongDataTypeException(String column) {
		super("Data type of input column '" + column + "' is not compatible.");
	}

}
