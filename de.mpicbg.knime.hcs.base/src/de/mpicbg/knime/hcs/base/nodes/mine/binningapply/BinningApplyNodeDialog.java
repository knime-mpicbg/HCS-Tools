package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

public class BinningApplyNodeDialog extends AbstractConfigDialog {
	
	
	@Override
	protected void createControls() {
		DialogComponentColumnFilter2 groupsDialog = new DialogComponentColumnFilter2(BinningApplyNodeModel.createGroupFilterModel(), 0);
		addDialogComponent(groupsDialog);
		this.setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(BinningApplyNodeModel.createIgnoreMissingSettingsModel(), "Ignore missing columns"));
		addDialogComponent(new DialogComponentBoolean(BinningApplyNodeModel.createIgnoreIncompleteSettingsModel(), "Ignore columns with less bins"));
	}

}
