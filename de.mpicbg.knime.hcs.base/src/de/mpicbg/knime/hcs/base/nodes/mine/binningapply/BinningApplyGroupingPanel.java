package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

@SuppressWarnings("serial")
public class BinningApplyGroupingPanel extends JPanel {
	
	DataColumnSpecFilterPanel comp_columnFilterPanel;
	JCheckBox comp_ignoreMissing = new JCheckBox("Ignore missing columns");
	JCheckBox comp_dismissIncomplete = new JCheckBox("Dismiss incomplete binning models");
	JCheckBox comp_alreadySorted = new JCheckBox("Input is already sorted by group column(s)");
	
	public BinningApplyGroupingPanel() {
		super(new BorderLayout());
		
		comp_columnFilterPanel = new DataColumnSpecFilterPanel();
		add(comp_columnFilterPanel, BorderLayout.CENTER);
		
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		comp_dismissIncomplete.setAlignmentX(Component.LEFT_ALIGNMENT);
		subPanel.add(comp_dismissIncomplete);
		comp_ignoreMissing.setAlignmentX(Component.LEFT_ALIGNMENT);
		subPanel.add(comp_ignoreMissing);
		comp_alreadySorted.setAlignmentX(Component.LEFT_ALIGNMENT);
		subPanel.add(comp_alreadySorted);
		add(subPanel, BorderLayout.SOUTH);
	}

	public void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec dataTableSpec) throws InvalidSettingsException {
		boolean sm_ignoreMissing = settings.getBoolean(BinningApplyNodeModel.CFG_MISSING);
		boolean sm_dismissIncomplete = settings.getBoolean(BinningApplyNodeModel.CFG_INCOMPLETE);
		boolean sm_alreadySorted = settings.getBoolean(BinningApplyNodeModel.CFG_SORTED);
		
		SettingsModelColumnFilter2 sm_columnFilter = new SettingsModelColumnFilter2(BinningApplyNodeModel.CFG_GROUPS);
		sm_columnFilter.loadSettingsFrom(settings);
		DataColumnSpecFilterConfiguration filterSpec = new DataColumnSpecFilterConfiguration(BinningApplyNodeModel.CFG_GROUPS);
		filterSpec.loadConfigurationInDialog(settings, dataTableSpec);
		
		comp_ignoreMissing.setSelected(sm_ignoreMissing);
		comp_dismissIncomplete.setSelected(sm_dismissIncomplete);
		comp_columnFilterPanel.loadConfiguration(filterSpec, dataTableSpec);
		comp_alreadySorted.setSelected(sm_alreadySorted);
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		boolean ignoreMissing = comp_ignoreMissing.isSelected();
		boolean dismissIncomplete = comp_dismissIncomplete.isSelected();
		boolean alreadySorted = comp_alreadySorted.isSelected();
		
		settings.addBoolean(BinningApplyNodeModel.CFG_INCOMPLETE, dismissIncomplete);
		settings.addBoolean(BinningApplyNodeModel.CFG_MISSING, ignoreMissing);
		settings.addBoolean(BinningApplyNodeModel.CFG_SORTED, alreadySorted);
		
		NameFilterConfiguration nfc = new NameFilterConfiguration(BinningApplyNodeModel.CFG_GROUPS);
		comp_columnFilterPanel.saveConfiguration(nfc);
		nfc.saveConfiguration(settings);		
	}

}
