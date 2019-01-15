package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

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
		
		comp_mainPanel.add(northPanel, BorderLayout.NORTH);
		//comp_mainPanel.add(comp_rightBoundColumn, BorderLayout.NORTH);
		
		this.addTab("General Settings", comp_mainPanel);
	}
}
