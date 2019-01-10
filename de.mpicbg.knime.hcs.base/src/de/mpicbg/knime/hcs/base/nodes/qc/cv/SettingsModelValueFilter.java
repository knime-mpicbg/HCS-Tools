package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.NominalValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.filter.nominal.NominalValueFilterConfiguration;

/**
 * wrap {@link NominalValueFilterConfiguration} as SettingsModel
 * 
 * @author Antje Janosch
 *
 */
public class SettingsModelValueFilter extends SettingsModel {
	
	private static final String CFG_COLUMN = "selected.column";
	private static final String CFG_COLUMN_DFT = "";
	
	private NominalValueFilterConfiguration m_nfc;
	private String m_selectedColumn;
	private Map<String, Set<DataCell>> m_domainValues;
	
	/**
	 * 
	 * @param key				key for settings model
	 * @param selectedColumn	column for subset selection
	 */
	public SettingsModelValueFilter(String key, String selectedColumn) {
		m_nfc = new NominalValueFilterConfiguration(key);
		m_selectedColumn = selectedColumn;
		m_domainValues = new LinkedHashMap<String, Set<DataCell>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelValueFilter createClone() {
		// TODO Auto-generated method stub
		return new SettingsModelValueFilter(m_nfc.getConfigRootName(), m_selectedColumn);
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_filterValue";
	}

	@Override
	protected String getConfigName() {
		return m_nfc.getConfigRootName();
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		
		if(!(specs[0] instanceof DataTableSpec) && this.isEnabled())
			throw new NotConfigurableException("Port 1 needs to be a DataTableSpec");
		
		m_domainValues.clear();
	
		for (DataColumnSpec colSpec : (DataTableSpec)specs[0]) {
			if (colSpec.getType().isCompatible(NominalValue.class) && colSpec.getDomain().hasValues()) {
				String columnName = colSpec.getName();
            	m_domainValues.put(columnName, colSpec.getDomain().getValues());
			}
		}
		
		Set<DataCell> domain = m_domainValues.get(m_selectedColumn);
		m_nfc.loadConfigurationInDialog(settings, domain);
		m_selectedColumn = settings.getString(CFG_COLUMN, CFG_COLUMN_DFT);

	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		m_nfc.saveConfiguration(settings);
		settings.addString(CFG_COLUMN, m_selectedColumn);
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// NOTHING TO DO HERE?
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		m_nfc.loadConfigurationInModel(settings);
		m_selectedColumn = settings.getString(CFG_COLUMN);
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		m_nfc.saveConfiguration(settings);
		settings.addString(CFG_COLUMN, m_selectedColumn);
	}

	@Override
	public String toString() {
		return m_nfc.toString();
	}

	/**
	 * @return domain values of subset column
	 */
	public Set<DataCell> getDomainValues() {
		return m_domainValues.get(m_selectedColumn);
	}

	/**
	 * @return filter configuration
	 */
	public NominalValueFilterConfiguration getFilterConfig() {
		return m_nfc;
	}

	/**
	 * 
	 * @param nfc				new filter configuration		
	 * @param selectedColumn	subset column name
	 * @throws InvalidSettingsException		if key string of the new config differs from the old
	 */
	public void updateSettings(NominalValueFilterConfiguration nfc, String selectedColumn) 
			throws InvalidSettingsException {
		if(nfc.getConfigRootName().equals(m_nfc.getConfigRootName())) {
			m_nfc = nfc;
			m_selectedColumn = selectedColumn;
		}
		else
			throw new InvalidSettingsException("Keys for 'value filter configuration' differ");
	}
	
	/**
	 * @return subset column name
	 */
	public String getSelectedColumn() {
		return m_selectedColumn;
	}

}
