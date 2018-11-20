package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLong;

public class BinningApplyNodeDialog extends DefaultNodeSettingsPane {
	
	public BinningApplyNodeDialog() {
		super();
		createControls();
	}

	protected void createControls() {
		
		this.removeTab("Grouping Options");
		
		DialogComponentColumnFilter2 groupsDialog = new DialogComponentColumnFilter2(BinningApplyNodeModel.createGroupFilterModel(), 0);
		addDialogComponent(groupsDialog);
		this.setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(BinningApplyNodeModel.createIgnoreMissingSettingsModel(), "Ignore missing columns"));
		addDialogComponent(new DialogComponentBoolean(BinningApplyNodeModel.createIgnoreIncompleteSettingsModel(), "Ignore columns with less bins"));
				
		createNewTab("Sampling Options");
		
		
		SettingsModelBoolean sm_useSampling = BinningApplyNodeModel.createUseSamplingSettingsModel();
		SettingsModelBoolean sm_useSeed = BinningApplyNodeModel.createUseSeedSettingsModel();
		
		SettingsModelIntegerBounded sm_sampleSize = BinningApplyNodeModel.createSampleSizeSettingsModel();
		SettingsModelLong sm_seedValue = BinningApplyNodeModel.createSeedValueSettingsModel();
			
		sm_useSampling.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean enabled = ((SettingsModelBoolean)e.getSource()).getBooleanValue();
				
				sm_sampleSize.setEnabled(enabled);
				sm_useSeed.setEnabled(enabled);
				sm_seedValue.setEnabled(enabled);
			}
		});
		
		this.setHorizontalPlacement(false);
		DialogComponentBoolean useSampling = new DialogComponentBoolean(sm_useSampling, "Enable sampling");
		addDialogComponent(useSampling);
		
		DialogComponentNumberEdit sampleSize = new DialogComponentNumberEdit(sm_sampleSize, "Sample size");
		addDialogComponent(sampleSize);
		
		this.createNewGroup("");
		this.setHorizontalPlacement(true);
		
		DialogComponentBoolean useSeed = new DialogComponentBoolean(sm_useSeed, "Use random seed");
		addDialogComponent(useSeed);
		
		DialogComponentNumberEdit seedValue = new DialogComponentNumberEdit(sm_seedValue, "Seed value");
		addDialogComponent(seedValue);
		
		this.closeCurrentGroup();
	
	}


	
	

}
