package de.mpicbg.knime.hcs.base.nodes.manip.col.splitinterval;

import org.knime.core.data.IntervalValue;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

public class SplitIntervalNodeDialog extends AbstractConfigDialog {

	@SuppressWarnings("unchecked")
	@Override
	protected void createControls() {
		
		this.addDialogComponent(new DialogComponentColumnNameSelection(
				SplitIntervalNodeModel.createIntervalColumnnModel(), 
				"Select Interval Column", 
				0, true, IntervalValue.class));
		this.addDialogComponent(new DialogComponentBoolean(
				SplitIntervalNodeModel.createIncludeModeModel(), 
				"Include Mode?"));

	}

}
