package de.mpicbg.knime.hcs2.base.utils.exceptions;

import org.knime.core.node.InvalidSettingsException;

@SuppressWarnings("serial")
public class InvalidSettingsMissingSettingException extends InvalidSettingsException {

	public InvalidSettingsMissingSettingException(String setting) {
		super("Setting value not available for '" + setting + "'. Please check the configuration.");
	}

}
