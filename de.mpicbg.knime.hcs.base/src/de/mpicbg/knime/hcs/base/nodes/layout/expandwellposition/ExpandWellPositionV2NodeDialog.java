package de.mpicbg.knime.hcs.base.nodes.layout.expandwellposition;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

public class ExpandWellPositionV2NodeDialog extends DefaultNodeSettingsPane {

	@SuppressWarnings("unchecked")
	public ExpandWellPositionV2NodeDialog() {
		this.addDialogComponent(
				new DialogComponentColumnNameSelection(
						ExpandWellPositionV2NodeModel.createWellColumnSettingsModel(), 
						"Well Position", 0, 
						StringValue.class));
		this.addDialogComponent(
				new DialogComponentBoolean(
						ExpandWellPositionV2NodeModel.createConvertRowValuesSettingsModel(),
						"Convert Row Characters"));
		this.addDialogComponent(
				new DialogComponentBoolean(
						ExpandWellPositionV2NodeModel.createDeleteSourceSettingsModel(),
						"Delete Source Column"));
	}
	
}
