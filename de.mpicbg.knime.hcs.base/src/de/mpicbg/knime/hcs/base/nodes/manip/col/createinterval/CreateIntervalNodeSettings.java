package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

public class CreateIntervalNodeSettings extends SettingsModel {
	
	private final String m_configName;
	private static final String SM_ID = "SMID_NODE_CREATE_INTERVAL";
	
	private String CFG_LEFTBOUND = "left.bound";
	private String CFG_RIGHTBOUND = "right.bound";
	
	private SettingsModelString m_leftBoundSM;
	private SettingsModelString m_rightBoundSM;
	
	public CreateIntervalNodeSettings(String configName) {
		m_configName = configName;
		
		initDefaultSettings();
	}

	private void initDefaultSettings() {
		m_leftBoundSM = new SettingsModelString(CFG_LEFTBOUND, null);
		m_rightBoundSM = new SettingsModelString(CFG_RIGHTBOUND, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CreateIntervalNodeSettings createClone() {
		CreateIntervalNodeSettings clonedSM = new CreateIntervalNodeSettings(m_configName);
		clonedSM.setLeftBoundColumn(m_leftBoundSM.getStringValue());
		clonedSM.setRightBoundColumn(m_rightBoundSM.getStringValue());
		
		return clonedSM;
	}

	public void setRightBoundColumn(String rightBoundColumnName) {
		m_rightBoundSM.setStringValue(rightBoundColumnName);
	}

	public void setLeftBoundColumn(String leftBoundColumnName) {
		m_leftBoundSM.setStringValue(leftBoundColumnName);
	}

	@Override
	protected String getModelTypeID() {
		return SM_ID;
	}

	@Override
	protected String getConfigName() {
		return m_configName;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			m_leftBoundSM.loadSettingsFrom(settings);
			m_rightBoundSM.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		m_leftBoundSM.saveSettingsTo(settings);
		m_rightBoundSM.saveSettingsTo(settings);
		
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * get name of the column with left bound values
	 * 
	 * @return column name (can be null)
	 */
	public String getLeftBoundColumn() {		
		return m_leftBoundSM.getStringValue();
	}

	/**
	 * get name of the column with right bound values
	 * 
	 * @return column name (can be null)
	 */
	public String getRightBoundColumn() {
		return m_rightBoundSM.getStringValue();
	}

}
