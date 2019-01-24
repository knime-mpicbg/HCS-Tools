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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.DataValueColumnFilter;

import de.mpicbg.knime.hcs.core.math.Interval.Mode;

/**
 * node dialog of Create Interval node
 * 
 * @author Antje Janosch
 *
 */
public class CreateIntervalNodeDialog extends NodeDialogPane {
	

	// node settings
	private CreateIntervalNodeSettings m_settings = null;
	
	/**
	 * Dialog components which are important to set and get node settings
	 */
	
	// selection of left/right bound (comboboxes)
	private final ColumnSelectionPanel comp_leftBoundColumn;
	private final ColumnSelectionPanel comp_rightBoundColumn;
	
	// selection of left/right mode (comboboxes)
	private final ColumnSelectionPanel comp_leftModeColumn;
	private final ColumnSelectionPanel comp_rightModeColumn;
	
	// selection of column to bereplaced (combobox)
	private final ColumnSelectionPanel comp_replaceColumnPanel;
	// new column name
	private final JTextField comp_newColumnName;
	
	// radio buttons to decide beween fixed modes and modes by column
	private JRadioButton comp_useFixedModes;
	private JRadioButton comp_useFlexibleModes;
	
	// radio buttons to decide whether to replace a column or append a new one
	private final JRadioButton comp_replaceColumnRadio;
	private final JRadioButton comp_appendColumnRadio;
	
	// radio buttons for fixed incl/excl modes
	private JRadioButton comp_inclBoth = new JRadioButton("[a;b]");
	private JRadioButton comp_inclLeft = new JRadioButton("[a;b)");
	private JRadioButton comp_inclRight = new JRadioButton("(a;b]");
	private JRadioButton comp_inclNone = new JRadioButton("(a;b)");
	
	
	/**
	 * constructor
	 * inits GUI
	 */
	@SuppressWarnings("unchecked")
	public CreateIntervalNodeDialog() {
		super();
		
		m_settings = new CreateIntervalNodeSettings(CreateIntervalNodeModel.CFG_KEY);
		
		/** INIT COMPONENTS **/
		
		// main panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel northPanel = new JPanel(new GridBagLayout());
		JPanel southPanel = new JPanel(new GridBagLayout());
		JPanel centerPanel = new JPanel(new GridBagLayout());
		
		// init left bound column combobox
		comp_leftBoundColumn =new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), DoubleValue.class);
		
		// init right bound column combobox
		comp_rightBoundColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), DoubleValue.class);
		
		// init left mode column combobox
		comp_leftModeColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), BooleanValue.class);
		comp_leftModeColumn.setRequired(false);
		
		// init right mode column combobox
		comp_rightModeColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), BooleanValue.class);
		comp_rightModeColumn.setRequired(false);	
		
		comp_useFixedModes = new JRadioButton("set fixed include/exclude flags");			
		comp_useFlexibleModes = new JRadioButton("use columns for include/exclude flags");
				
		ButtonGroup group = new ButtonGroup();
		group.add(comp_useFlexibleModes);
		group.add(comp_useFixedModes);
		
		JPanel flexibleModesPanel = new JPanel();
		flexibleModesPanel.setLayout(new BoxLayout(flexibleModesPanel, BoxLayout.Y_AXIS));
		flexibleModesPanel.setBorder(BorderFactory.createTitledBorder(""));
				
		JPanel fixedModesPanel = new JPanel();
		fixedModesPanel.setLayout(new BoxLayout(fixedModesPanel, BoxLayout.Y_AXIS));
		fixedModesPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		ButtonGroup comp_fixedModesSelection = new ButtonGroup();
		comp_fixedModesSelection.add(comp_inclBoth);
		comp_fixedModesSelection.add(comp_inclLeft);
		comp_fixedModesSelection.add(comp_inclRight);
		comp_fixedModesSelection.add(comp_inclNone);
		
		comp_useFixedModes.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
					enablePanel(fixedModesPanel, e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		comp_useFlexibleModes.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				enablePanel(flexibleModesPanel, e.getStateChange() == ItemEvent.SELECTED);
			}

		});
		comp_leftBoundColumn.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				m_settings.setLeftBoundColumn(comp_leftBoundColumn.getSelectedColumn());
			}
		});
		comp_rightBoundColumn.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				m_settings.setRightBoundColumn(comp_rightBoundColumn.getSelectedColumn());
			}
		});
		comp_leftModeColumn.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				m_settings.setLeftModeColumn(comp_leftModeColumn.getSelectedColumn());
			}
		});
		comp_rightModeColumn.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				m_settings.setRightModeColumn(comp_rightModeColumn.getSelectedColumn());
			}
		});
		
		comp_fixedModesSelection.setSelected(comp_inclLeft.getModel(), true);
		comp_useFixedModes.setSelected(true);
		
		// components for south panel
		
		ButtonGroup bg = new ButtonGroup();
        comp_replaceColumnRadio = new JRadioButton("Replace Column");
        comp_appendColumnRadio = new JRadioButton("Append Column");
        bg.add(comp_replaceColumnRadio);
        bg.add(comp_appendColumnRadio);
        
        comp_replaceColumnPanel = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), DataValue.class);
        comp_replaceColumnPanel.setRequired(false);
        comp_newColumnName = new JTextField();
        comp_newColumnName.setPreferredSize(comp_replaceColumnPanel.getPreferredSize());
        comp_newColumnName.setText(CreateIntervalNodeSettings.CFG_OUT_COLUMN_NAME_DFT);
  
        comp_replaceColumnRadio.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				comp_replaceColumnPanel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
        
        comp_appendColumnRadio.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				comp_newColumnName.setEnabled(e.getStateChange() == ItemEvent.SELECTED);				
			}
		});
        
        bg.setSelected(comp_appendColumnRadio.getModel(), true);
        comp_appendColumnRadio.setSelected(true);
        // initial selection / deselection (as no event is fired for first set selected)
        comp_replaceColumnPanel.setEnabled(false);
        comp_newColumnName.setEnabled(true);

		
		/** LAYOUT COMPONENTS **/
        
        // north panel
        GridBagConstraints c = new GridBagConstraints();
        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        northPanel.add(new JLabel("Left Bound"), c);

        c.gridx = 1;
        c.weightx = 3;
        northPanel.add(comp_leftBoundColumn, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        northPanel.add(new JLabel("Right Bound"),c);

        c.gridx = 1;
        c.weightx = 3;
        northPanel.add(comp_rightBoundColumn, c);
		
		// center panel
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		centerPanel.add(comp_useFixedModes, c);
		
		c.gridx = 1;
		
		centerPanel.add(comp_useFlexibleModes, c);
		
		fixedModesPanel.add(comp_inclBoth);
		fixedModesPanel.add(comp_inclLeft);
		fixedModesPanel.add(comp_inclRight);
		fixedModesPanel.add(comp_inclNone);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		centerPanel.add(fixedModesPanel, c);
		
		flexibleModesPanel.add(new JLabel("include left bound?"));
		flexibleModesPanel.add(comp_leftModeColumn);
		flexibleModesPanel.add(new JLabel("include right bound?"));
		flexibleModesPanel.add(comp_rightModeColumn);
		
		c.gridx = 1;
		centerPanel.add(flexibleModesPanel, c);
		
		// south panel
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		southPanel.add(comp_replaceColumnRadio, c);
		
		c.gridx = 1;
		c.weightx = 3;
		southPanel.add(comp_replaceColumnPanel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		southPanel.add(comp_appendColumnRadio,c);
		
		c.gridx = 1;
		c.weightx = 3;
		southPanel.add(comp_newColumnName, c);
		
		mainPanel.add(northPanel, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);	
		mainPanel.add(southPanel, BorderLayout.SOUTH);
				
		this.addTab("General Settings", mainPanel);
			
		// initial selection / deselection (as no event is fired for first set selected)
		enablePanel(flexibleModesPanel, false);
		enablePanel(fixedModesPanel, true);
	}
	
	/**
	 * enables or disables the panel and all subcomponents (not for sub-panels)
	 * 
	 * @param panel
	 * @param enable
	 */
	private void enablePanel(JPanel panel, boolean enable) {
        panel.setEnabled(enable);
        for (Component cp : panel.getComponents() ){
        	cp.setEnabled(enable);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		
		// sync GUI-settings to model
		m_settings.setModeColumnsFlag(comp_useFlexibleModes.isSelected());
		
		if(comp_inclBoth.isSelected())
			m_settings.setFixedMode(Mode.INCL_BOTH);
		if(comp_inclLeft.isSelected())
			m_settings.setFixedMode(Mode.INCL_LEFT);
		if(comp_inclRight.isSelected())
			m_settings.setFixedMode(Mode.INCL_RIGHT);
		if(comp_inclNone.isSelected())
			m_settings.setFixedMode(Mode.INCL_NONE);
		
		if(m_settings.useModeColumns()) {
			if(m_settings.getLeftModeColumn() == null || m_settings.getRightModeColumn() == null)
				throw new InvalidSettingsException("No mode columns selected.\nEnable fixed mode usage if no mode columns are available, otherwise select valid columns");
		}
		
		boolean appendColumn = comp_newColumnName.isEnabled();
		m_settings.setCreateColumnFlag(appendColumn);
		if(appendColumn)
			m_settings.setOutColumnName(comp_newColumnName.getText());
		else
			m_settings.setOutColumnName(comp_replaceColumnPanel.getSelectedColumn());
				
		// might be save settings for model?
		m_settings.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
	
		m_settings.loadSettingsForDialog(settings, specs);
				
		// update components
		updateComponents(specs[0]);
	}

	/**
	 * update components with loaded settings
	 * 
	 * @param spec							input table specs
	 * @throws NotConfigurableException
	 */
	private void updateComponents(DataTableSpec spec) throws NotConfigurableException {
		// update left/right bound column combobox			
		comp_leftBoundColumn.update(spec, m_settings.getLeftBoundColumn(), false, true);
		comp_rightBoundColumn.update(spec, m_settings.getRightBoundColumn(), false, true);
		comp_leftModeColumn.update(spec, m_settings.getLeftModeColumn(), false, true);
		comp_rightModeColumn.update(spec, m_settings.getRightModeColumn(), false, true);
		
		boolean appendColumn = m_settings.createNewColumn();
		String appendColumnName = appendColumn ? 
				m_settings.getOutColumnName() : CreateIntervalNodeSettings.CFG_OUT_COLUMN_NAME_DFT;
		comp_newColumnName.setText(appendColumnName);
		String replaceColumnName = appendColumn ? null : m_settings.getOutColumnName();
		comp_replaceColumnPanel.update(spec, replaceColumnName, false, true);
		
		if(appendColumn)
			comp_appendColumnRadio.setSelected(true);
		else
			comp_replaceColumnRadio.setSelected(true);
		
		boolean useModeColumns = m_settings.useModeColumns();
		
		if(useModeColumns)
			comp_useFlexibleModes.setSelected(true);
		else
			comp_useFixedModes.setSelected(true);
	}



}
