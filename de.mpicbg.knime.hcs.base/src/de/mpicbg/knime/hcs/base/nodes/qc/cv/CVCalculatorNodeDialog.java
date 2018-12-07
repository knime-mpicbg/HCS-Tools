package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;
import org.knime.core.node.util.filter.column.DataTypeColumnFilter;
import org.knime.core.node.util.filter.nominal.NominalValueFilterConfiguration;

public class CVCalculatorNodeDialog extends NodeDialogPane {
	
	private final SubsetValueFilterPanel comp_valueFilter;
	private SettingsModelValueFilter sm_subsetSelection;
	
	private final JPanel comp_subsetPanel;
	private final JPanel comp_mainPanel;
	
	private final JComboBox<String> comp_groupColumn;
	private DefaultComboBoxModel<String> m_groupColumnModel;
	
	private final JComboBox<String> comp_subsetColumn;
	private DefaultComboBoxModel<String> m_subsetColumnModel;
	
	
	
	private final DataColumnSpecFilterPanel comp_columnFilterPanel;
	
	private final JCheckBox comp_useRobustStats;
	private final JCheckBox comp_useSuffix;
	private final JTextField comp_suffix;
	
	private final Map<String, Set<DataCell>> m_colAttributes;
	
	public CVCalculatorNodeDialog() {
		super();
		
		m_colAttributes = new LinkedHashMap<String, Set<DataCell>>();
		
		// main panel
		comp_mainPanel = new JPanel(new BorderLayout());
		
		// init subset column combobox
		m_subsetColumnModel = new DefaultComboBoxModel<String>();
		comp_subsetColumn = new JComboBox<String>(m_subsetColumnModel);
		comp_subsetColumn.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) 
				{
					String selectedColumn = (String)e.getItem();
					updateValueFiler(selectedColumn);
				}
			}
		});
		
		// init group column combobox
		m_groupColumnModel = new DefaultComboBoxModel<String>();
		comp_groupColumn = new JComboBox<String>(m_groupColumnModel);
		
		// put comboboxes + labels to NORTH-panel
		JPanel northPanel = new JPanel(new GridLayout(2,2));
		JLabel groupLabel = new JLabel("Group by");
		groupLabel.setHorizontalAlignment(JLabel.RIGHT);
		northPanel.add(groupLabel);
		northPanel.add(comp_groupColumn);
		JLabel subsetLabel = new JLabel("Subset by");
		subsetLabel.setHorizontalAlignment(JLabel.RIGHT);
		northPanel.add(subsetLabel);
		northPanel.add(comp_subsetColumn);
		
		comp_mainPanel.add(northPanel, BorderLayout.NORTH);
		
		// init parameter selection panel and add to CENTER
		comp_columnFilterPanel = new DataColumnSpecFilterPanel();
		TitledBorder title;
		title = BorderFactory.createTitledBorder("Calculate Coefficient of Variation for");
		comp_columnFilterPanel.setBorder(title);		
		comp_mainPanel.add(comp_columnFilterPanel, BorderLayout.CENTER);
		
		// init checkboxes and textfield and create SOUTH panel
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		comp_useRobustStats = new JCheckBox("Use robust statistics (median / mad)");
		comp_useSuffix = new JCheckBox("column suffix");
		comp_useSuffix.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					comp_suffix.setEnabled(true);
				else
					comp_suffix.setEnabled(false);
			}
		});
		comp_suffix = new JTextField(".cv");
		comp_suffix.setEnabled(false);
		
		southPanel.add(comp_useRobustStats);
		southPanel.add(comp_useSuffix);
		southPanel.add(comp_suffix);
		
		comp_mainPanel.add(southPanel, BorderLayout.SOUTH);
		
		// create subset panel for additional tab
		comp_subsetPanel = new JPanel(new BorderLayout());
		comp_valueFilter = new SubsetValueFilterPanel();	
		comp_subsetPanel.add(comp_valueFilter, BorderLayout.CENTER);
		
		// add mainPanel and subsetPanel to tabs
		this.addTab("General Settings", comp_mainPanel);
		this.addTab("Subset Filter",comp_subsetPanel);
	}
	
	
	private void updateValueFiler(String selectedColumn) {
			
		if(m_colAttributes.containsKey(selectedColumn)) {
			
			Set<DataCell> domainValues = m_colAttributes.get(selectedColumn);
			String[] dVals = new String[domainValues.size()];
			int i = 0;
			for(DataCell value : domainValues) {
				dVals[i] = ((StringValue)value).getStringValue();
				i++;
			}
						
			NominalValueFilterConfiguration config = new NominalValueFilterConfiguration(CVCalculatorNodeModel.CFG_SUBSET_SEL);
            config.loadDefaults(dVals, null, EnforceOption.EnforceExclusion);
            comp_valueFilter.loadConfiguration(config, dVals);
		} else {
			// if <none> is selected
			String[] dVals = new String[0];
			NominalValueFilterConfiguration config = new NominalValueFilterConfiguration(CVCalculatorNodeModel.CFG_SUBSET_SEL);
            config.loadDefaults(null, null, EnforceOption.EnforceExclusion);
            comp_valueFilter.loadConfiguration(config, dVals);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		settings.addBoolean(CVCalculatorNodeModel.CFG_USE_ROBUST, comp_useRobustStats.isSelected());
		settings.addBoolean(CVCalculatorNodeModel.CFG_USE_SUFFIX, comp_useSuffix.isSelected());
		settings.addString(CVCalculatorNodeModel.CFG_SUFFIX, comp_suffix.getText());
		settings.addString(CVCalculatorNodeModel.CFG_GROUP, (String)m_groupColumnModel.getSelectedItem());
		String selectedColumn = (String)m_subsetColumnModel.getSelectedItem();
		settings.addString(CVCalculatorNodeModel.CFG_SUBSET_COL, selectedColumn);

		NominalValueFilterConfiguration nfc = new NominalValueFilterConfiguration(CVCalculatorNodeModel.CFG_SUBSET_SEL);
		comp_valueFilter.saveConfiguration(nfc);
		sm_subsetSelection.updateSettings(nfc, selectedColumn);
		sm_subsetSelection.saveSettingsTo(settings);

		DataColumnSpecFilterConfiguration filterSpec = new DataColumnSpecFilterConfiguration(CVCalculatorNodeModel.CFG_PARAMETERS, new DataTypeColumnFilter(DoubleValue.class), NameFilterConfiguration.FILTER_BY_NAMEPATTERN);
		comp_columnFilterPanel.saveConfiguration(filterSpec);
		filterSpec.saveConfiguration(settings);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) 
			throws NotConfigurableException {
		
		String selectedColumn = settings.getString(CVCalculatorNodeModel.CFG_SUBSET_COL, CVCalculatorNodeModel.CFG_SUBSET_COL_DFT);
		String groupColumn = settings.getString(CVCalculatorNodeModel.CFG_GROUP, CVCalculatorNodeModel.CFG_GROUP_DFT);
		
		// update subset column
		m_colAttributes.clear();
		m_subsetColumnModel.removeAllElements();
		m_subsetColumnModel.addElement(CVCalculatorNodeModel.CFG_SUBSET_COL_DFT);
		m_subsetColumnModel.setSelectedItem(CVCalculatorNodeModel.CFG_SUBSET_COL_DFT);
		
		// update groupby column
		m_groupColumnModel.removeAllElements();
		m_groupColumnModel.addElement(CVCalculatorNodeModel.CFG_GROUP_DFT);
		m_groupColumnModel.setSelectedItem(CVCalculatorNodeModel.CFG_GROUP_DFT);
		
		// add columns via specs to both and set selected column
		for (DataColumnSpec colSpec : specs[0]) {
            if (colSpec.getType().isCompatible(NominalValue.class) && colSpec.getDomain().hasValues()) {
                String columnName = colSpec.getName();
            	m_colAttributes.put(columnName, colSpec.getDomain().getValues());
                m_subsetColumnModel.addElement(columnName);
                if(columnName.equals(selectedColumn)) {
                	m_subsetColumnModel.setSelectedItem(columnName);
                }
            }
            if (colSpec.getType().isCompatible(StringValue.class)) {
            	String columnName = colSpec.getName();
            	m_groupColumnModel.addElement(columnName);
            	if(columnName.equals(groupColumn))
            		m_groupColumnModel.setSelectedItem(columnName);
            }
        }
				
		// update subset filter panel
		sm_subsetSelection = new SettingsModelValueFilter(CVCalculatorNodeModel.CFG_SUBSET_SEL, selectedColumn);
		sm_subsetSelection.loadSettingsForDialog(settings, specs);
		comp_valueFilter.loadConfiguration(sm_subsetSelection.getFilterConfig(), sm_subsetSelection.getDomainValues());
		
		// update column filter panel
		DataColumnSpecFilterConfiguration filterSpec = new DataColumnSpecFilterConfiguration(CVCalculatorNodeModel.CFG_PARAMETERS, new DataTypeColumnFilter(DoubleValue.class), NameFilterConfiguration.FILTER_BY_NAMEPATTERN);
		filterSpec.loadConfigurationInDialog(settings, specs[0]);		
		comp_columnFilterPanel.loadConfiguration(filterSpec, specs[0]);
		
		boolean useRobust = settings.getBoolean(CVCalculatorNodeModel.CFG_USE_ROBUST, CVCalculatorNodeModel.CFG_USE_ROBUST_DFT);
		boolean useSuffix = settings.getBoolean(CVCalculatorNodeModel.CFG_USE_SUFFIX, CVCalculatorNodeModel.CFG_USE_SUFFIX_DFT);
		String suffix = settings.getString(CVCalculatorNodeModel.CFG_SUFFIX, CVCalculatorNodeModel.CFG_SUFFIX_DFT);
		
		comp_useRobustStats.setSelected(useRobust);
		comp_useSuffix.setSelected(useSuffix);
		comp_suffix.setText(suffix);
	}

}
