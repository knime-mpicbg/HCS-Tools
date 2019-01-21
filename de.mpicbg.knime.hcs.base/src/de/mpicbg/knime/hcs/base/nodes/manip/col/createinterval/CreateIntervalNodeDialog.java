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

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.core.node.util.ColumnFilterPanel.ValueClassFilter;

import de.mpicbg.knime.hcs.core.math.Interval.Mode;

public class CreateIntervalNodeDialog extends NodeDialogPane {
	
	// panel for "General Settings" tab
	private final JPanel comp_mainPanel;
	
	private CreateIntervalNodeSettings m_settings = null;
	
	private final ColumnSelectionPanel comp_leftBoundColumn;
	private final ColumnSelectionPanel comp_rightBoundColumn;
	
	private final ColumnSelectionPanel comp_leftModeColumn;
	private final ColumnSelectionPanel comp_rightModeColumn;
	
	private JRadioButton comp_useFixedModes;
	private JRadioButton comp_useFlexibleModes;
	
	private ButtonGroup comp_fixedModesSelection;
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
		comp_mainPanel = new JPanel(new BorderLayout());
		
		// init left bound column combobox
		comp_leftBoundColumn =new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), DoubleValue.class);
		
		// init right bound column combobox
		comp_rightBoundColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), DoubleValue.class);
		
		DataValueColumnFilter columnFilter = new DataValueColumnFilter(BooleanValue.class, IntValue.class);
		
		// init left mode column combobox
		comp_leftModeColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), columnFilter, true);

		// init right mode column combobox
		comp_rightModeColumn = new ColumnSelectionPanel(BorderFactory.createEmptyBorder(), columnFilter, true);
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
				
		JPanel centerPanel = new JPanel(new GridBagLayout());
			
		comp_useFixedModes = new JRadioButton("set include/exclude flags manually");			
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
		
		comp_fixedModesSelection = new ButtonGroup();
		comp_fixedModesSelection.add(comp_inclBoth);
		comp_fixedModesSelection.add(comp_inclLeft);
		comp_fixedModesSelection.add(comp_inclRight);
		comp_fixedModesSelection.add(comp_inclNone);
		
		comp_useFixedModes.getModel().addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
					enablePanel(fixedModesPanel, e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		comp_useFlexibleModes.getModel().addItemListener(new ItemListener() {	
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
		
		/** LAYOUT COMPONENTS **/
		
		northPanel.add(comp_leftBoundColumn);
		northPanel.add(comp_rightBoundColumn);
		
		GridBagConstraints c = new GridBagConstraints();
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
		
		comp_mainPanel.add(centerPanel, BorderLayout.CENTER);
		comp_mainPanel.add(northPanel, BorderLayout.NORTH);
				
		this.addTab("General Settings", comp_mainPanel);
		
		group.setSelected(comp_useFixedModes.getModel(), true);
		enablePanel(flexibleModesPanel, false);
	}
	
	private void enablePanel(JPanel panel, boolean enable) {
        panel.setEnabled(enable);
        for (Component cp : panel.getComponents() ){
        	cp.setEnabled(enable);
        }
	}

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
		
	
		// might besave settings for model?
		m_settings.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		
		// check configurability
		
		m_settings.loadSettingsForDialog(settings, specs);
				
		// update components
		updateComponents(specs[0]);
	}

	private void updateComponents(DataTableSpec spec) throws NotConfigurableException {
		// update left/right bound column combobox	
		String leftBoundSelected = null;
		String rightBoundSelected = null;
		String leftModeColumnSelected = null;
		String rightModeColumnSelected = null;
		
		for (DataColumnSpec colSpec : spec) {
			DataType dType = colSpec.getType();
			String columnName = colSpec.getName();
			if (dType.isCompatible(DoubleValue.class)) {          	
            	if(columnName.equals(m_settings.getLeftBoundColumn()))
            		leftBoundSelected = columnName;
            	if(columnName.equals(m_settings.getRightBoundColumn()))
            		rightBoundSelected = columnName;
			}
			if(dType.isCompatible(BooleanValue.class) || dType.isCompatible(IntValue.class)) {
				if(columnName.equals(m_settings.getLeftModeColumn()))
            		leftModeColumnSelected = columnName;
            	if(columnName.equals(m_settings.getRightModeColumn()))
            		rightModeColumnSelected = columnName;
			}
		}
		
		comp_leftBoundColumn.update(spec, leftBoundSelected);
		comp_rightBoundColumn.update(spec, rightBoundSelected);;
		comp_leftModeColumn.update(spec, leftModeColumnSelected);
		comp_rightModeColumn.update(spec, rightModeColumnSelected);

	}

}
