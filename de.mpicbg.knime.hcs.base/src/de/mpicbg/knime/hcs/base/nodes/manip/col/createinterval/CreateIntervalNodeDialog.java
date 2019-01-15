package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

public class CreateIntervalNodeDialog extends NodeDialogPane {
	
	// panel for "General Settings" tab
	private final JPanel comp_mainPanel;
	
	private CreateIntervalNodeSettings m_settings = null;
	
	/*
	 * combobox to select the left bound column and its model
	 */
	private final JComboBox<String> comp_leftBoundColumn;
	private DefaultComboBoxModel<String> m_leftBoundColumnModel;
	
	/*
	 * combobox to select the right bound column and its model
	 */
	private final JComboBox<String> comp_rightBoundColumn;
	private DefaultComboBoxModel<String> m_rightBoundColumnModel;
	
	private JRadioButton comp_useFixedModes;
	private JRadioButton comp_useFlexibleModes;
	
	
	/**
	 * constructor
	 * inits GUI
	 */
	public CreateIntervalNodeDialog() {
		super();
		
		m_settings = new CreateIntervalNodeSettings(CreateIntervalNodeModel.CFG_KEY);
		
		// main panel
		comp_mainPanel = new JPanel(new BorderLayout());
		
		// init left bound column combobox
		m_leftBoundColumnModel = new DefaultComboBoxModel<String>();
		comp_leftBoundColumn = new JComboBox<String>(m_leftBoundColumnModel);
		
		// init left bound column combobox
		m_rightBoundColumnModel = new DefaultComboBoxModel<String>();
		comp_rightBoundColumn = new JComboBox<String>(m_rightBoundColumnModel);
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
		northPanel.add(comp_leftBoundColumn);
		northPanel.add(comp_rightBoundColumn);
		
		JPanel centerPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		comp_useFixedModes = new JRadioButton("set include/exclude flags manually");	
		centerPanel.add(comp_useFixedModes, c);
		
		c.gridx = 1;
		
		comp_useFlexibleModes = new JRadioButton("use columns for include/exclude flags");
		centerPanel.add(comp_useFlexibleModes, c);
		
		ButtonGroup group = new ButtonGroup();
		group.add(comp_useFlexibleModes);
		group.add(comp_useFixedModes);
		group.setSelected(comp_useFixedModes.getModel(), true);
		
		JPanel flexibleModesPanel = new JPanel();
		flexibleModesPanel.setLayout(new BoxLayout(flexibleModesPanel, BoxLayout.Y_AXIS));
		flexibleModesPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		flexibleModesPanel.add(new JLabel("test"));
		
		JPanel fixedModesPanel = new JPanel();
		fixedModesPanel.setLayout(new BoxLayout(fixedModesPanel, BoxLayout.Y_AXIS));
		fixedModesPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		fixedModesPanel.add(new JLabel("test"));
		
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		centerPanel.add(fixedModesPanel, c);
		
		c.gridx = 1;
		centerPanel.add(flexibleModesPanel, c);
		
		comp_mainPanel.add(centerPanel, BorderLayout.CENTER);
		comp_mainPanel.add(northPanel, BorderLayout.NORTH);
		
		
		comp_useFixedModes.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
			        fixedModesPanel.setEnabled(false);
			        for (Component cp : fixedModesPanel.getComponents() ){
			        	cp.setEnabled(false);
			        }
			    }
				if (e.getStateChange() == ItemEvent.SELECTED) {
			        fixedModesPanel.setEnabled(true);
			        for (Component cp : fixedModesPanel.getComponents() ){
			        	cp.setEnabled(true);
			        }
			    }
			}
		});
		
		comp_useFlexibleModes.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
			        flexibleModesPanel.setEnabled(false);
			        for (Component cp : flexibleModesPanel.getComponents() ){
			        	cp.setEnabled(false);
			        }
			    }
				if (e.getStateChange() == ItemEvent.SELECTED) {
			        flexibleModesPanel.setEnabled(true);
			        for (Component cp : flexibleModesPanel.getComponents() ){
			        	cp.setEnabled(true);
			        }
			    }
			}
		});
		
		
		this.addTab("General Settings", comp_mainPanel);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		
		// check configurability
		
		m_settings.loadSettingsForDialog(settings, specs);
				
		// update components
		updateComponents(specs[0]);
	}

	private void updateComponents(DataTableSpec spec) {
		// update left/right bound column combobox
		m_leftBoundColumnModel.removeAllElements();
		m_rightBoundColumnModel.removeAllElements();
		
		for (DataColumnSpec colSpec : spec) {
			if (colSpec.getType().isCompatible(DoubleValue.class)) {
            	String columnName = colSpec.getName();
            	m_leftBoundColumnModel.addElement(columnName);
            	m_rightBoundColumnModel.addElement(columnName);
            	if(columnName.equals(m_settings.getLeftBoundColumn()))
            		m_leftBoundColumnModel.setSelectedItem(columnName);
            	if(columnName.equals(m_settings.getRightBoundColumn()))
            		m_rightBoundColumnModel.setSelectedItem(columnName);
			}
		}
		
		// select first column if no column is set by model
		if(m_leftBoundColumnModel.getSelectedItem() == null)
			m_leftBoundColumnModel.setSelectedItem(m_leftBoundColumnModel.getElementAt(0));
		// select first column if no column is set by model
		if(m_rightBoundColumnModel.getSelectedItem() == null)
			m_rightBoundColumnModel.setSelectedItem(m_rightBoundColumnModel.getElementAt(0));
	}


}
