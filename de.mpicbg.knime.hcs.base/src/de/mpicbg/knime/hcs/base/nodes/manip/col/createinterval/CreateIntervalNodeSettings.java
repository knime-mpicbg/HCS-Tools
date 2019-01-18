package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import java.util.LinkedList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import de.mpicbg.knime.hcs.core.math.Interval;
import de.mpicbg.knime.hcs.core.math.Interval.Mode;

public class CreateIntervalNodeSettings extends SettingsModel {
	
	private final String m_configName;
	private static final String SM_ID = "SMID_NODE_CREATE_INTERVAL";
	
	private final String CFG_LEFTBOUND = "left.bound";
	private final String CFG_RIGHTBOUND = "right.bound";
	private SettingsModelString m_leftBoundSM;
	private SettingsModelString m_rightBoundSM;
	
	private final String CFG_LEFTMODE = "left.mode.column";
	private final String CFG_RIGHTMODE = "right.mode.column";
	private SettingsModelOptionalString m_leftModeColumnSM;
	private SettingsModelOptionalString m_rightModeColumnSM;
	
	private final String CFG_USE_MODECOLUMN = "use.mode.columns";	
	private final boolean CFG_USE_MODECOLUMN_DFT = false;
	private SettingsModelBoolean m_useModeColumnsSM;
	
	private final String CFG_FIXED_MODE = "fixed.mode";
	private final Mode CFG_FIXED_MODE_DFT = Interval.Mode.INCL_LEFT;
	private SettingsModelString m_fixedModeSM;
	
	
	public CreateIntervalNodeSettings(String configName) {
		m_configName = configName;
		
		initDefaultSettings();
	}

	private void initDefaultSettings() {
		m_leftBoundSM = new SettingsModelString(CFG_LEFTBOUND, null);
		m_rightBoundSM = new SettingsModelString(CFG_RIGHTBOUND, null);
		
		m_leftModeColumnSM = new SettingsModelOptionalString(CFG_LEFTMODE, null, false);
		m_rightModeColumnSM = new SettingsModelOptionalString(CFG_RIGHTMODE, null, false);
		
		m_useModeColumnsSM = new SettingsModelBoolean(CFG_USE_MODECOLUMN, CFG_USE_MODECOLUMN_DFT);
		
		m_fixedModeSM = new SettingsModelString(CFG_FIXED_MODE, CFG_FIXED_MODE_DFT.toString());
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
	
	public void setLeftModeColumn(String leftModeColumnName) {
		m_leftModeColumnSM.setStringValue(leftModeColumnName);
	}
	
	public void setRightModeColumn(String rightModeColumnName) {
		m_leftModeColumnSM.setStringValue(rightModeColumnName);
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
		System.out.println("load setttings for dialog");
		// try to load from settings, ignore if missing in 'settings'
		try {
			m_leftBoundSM.setStringValue(settings.getString(CFG_LEFTBOUND));
			m_rightBoundSM.setStringValue(settings.getString(CFG_RIGHTBOUND));
			m_leftModeColumnSM.setStringValue(settings.getString(CFG_LEFTMODE));
			m_leftModeColumnSM.setIsActive(settings.getBoolean(CFG_LEFTMODE + "_BOOL"));
			m_rightModeColumnSM.setStringValue(settings.getString(CFG_RIGHTMODE));
			m_rightModeColumnSM.setIsActive(settings.getBoolean(CFG_RIGHTMODE + "_BOOL"));
			m_useModeColumnsSM.setBooleanValue(settings.getBoolean(CFG_USE_MODECOLUMN));
			m_fixedModeSM.setStringValue(settings.getString(CFG_FIXED_MODE));
		} catch (InvalidSettingsException e) {
			// do nothing
		}
		
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		System.out.println("save setttings for dialog");
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		System.out.println("validate settings for model");
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		System.out.println("load setttings for model");
		
		m_leftBoundSM.setStringValue(settings.getString(CFG_LEFTBOUND));
		m_rightBoundSM.setStringValue(settings.getString(CFG_RIGHTBOUND));
		m_leftModeColumnSM.setStringValue(settings.getString(CFG_LEFTMODE, null));
		m_rightModeColumnSM.setStringValue(settings.getString(CFG_RIGHTMODE, null));
		m_fixedModeSM.setStringValue(settings.getString(CFG_FIXED_MODE, CFG_FIXED_MODE_DFT.toString()));
		boolean useModeColumns = settings.getBoolean(CFG_USE_MODECOLUMN, CFG_USE_MODECOLUMN_DFT);
		m_useModeColumnsSM.setBooleanValue(useModeColumns);
		m_leftModeColumnSM.setIsActive(useModeColumns);
		m_rightModeColumnSM.setIsActive(useModeColumns);
		
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		System.out.println("save setttings for mode");
		
		m_leftBoundSM.saveSettingsTo(settings);
		m_rightBoundSM.saveSettingsTo(settings);
		m_leftModeColumnSM.saveSettingsTo(settings);
		m_rightModeColumnSM.saveSettingsTo(settings);
		m_useModeColumnsSM.saveSettingsTo(settings);
		m_fixedModeSM.saveSettingsTo(settings);
	}

	@Override
	public String toString() {
		List<String> settingsStrings = new LinkedList<String>();
		
		settingsStrings.add(this.getClass().getSimpleName()  + " ('" + m_configName + "')");
		settingsStrings.add(m_leftBoundSM.getClass().getSimpleName() + " ('" + CFG_LEFTBOUND + "')");
		settingsStrings.add(m_rightBoundSM.getClass().getSimpleName() + " ('" + CFG_RIGHTBOUND + "')");
		settingsStrings.add(m_leftModeColumnSM.getClass().getSimpleName() + " ('" + CFG_LEFTMODE + "')");
		settingsStrings.add(m_rightModeColumnSM.getClass().getSimpleName() + " ('" + CFG_RIGHTMODE + "')");
		settingsStrings.add(m_useModeColumnsSM.getClass().getSimpleName() + " ('" + CFG_USE_MODECOLUMN + "')");
		settingsStrings.add(m_fixedModeSM.getClass().getSimpleName() + " ('" + CFG_FIXED_MODE + "')");
		
		return String.join("\n", settingsStrings);
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
	
	/**
	 * get name of column with left mode values
	 * 
	 * @return column name (can be null)
	 */
	public String getLeftModeColumn() {
		return m_leftModeColumnSM.getStringValue();
	}
	
	/**
	 * get name of column with right mode values
	 * 
	 * @return column name (can be null)
	 */
	public String getRightModeColumn() {
		return m_rightModeColumnSM.getStringValue();
	}

	/**
	 * @return true, if mode columns are required
	 */
	public boolean useModeColumns() {
		return m_useModeColumnsSM.getBooleanValue();
	}
	
	/**
	 * @return Mode for interval bounds; string representation of {@link Interval.Mode}
	 */
	public String getFixedMode() {
		return m_fixedModeSM.getStringValue();
	}
}
