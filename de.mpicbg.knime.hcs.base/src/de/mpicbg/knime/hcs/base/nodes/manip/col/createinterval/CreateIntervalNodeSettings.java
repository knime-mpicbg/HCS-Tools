package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.apache.commons.lang3.EnumUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

import de.mpicbg.knime.hcs.core.math.Interval;
import de.mpicbg.knime.hcs.core.math.Interval.Mode;

public class CreateIntervalNodeSettings extends SettingsModel {
	
	private final String m_configName;
	private static final String SM_ID = "SMID_NODE_CREATE_INTERVAL";
	
	// settings 1 and 2
	private final String CFG_LEFTBOUND = "left.bound";
	private final String CFG_RIGHTBOUND = "right.bound";
	private String m_leftBoundSM;
	private String m_rightBoundSM;
	
	// settings 3 and 4
	private final String CFG_LEFTMODE = "left.mode.column";
	private final String CFG_RIGHTMODE = "right.mode.column";
	private String m_leftModeColumnSM;
	private String m_rightModeColumnSM;
	
	// setting 5
	private final String CFG_USE_MODECOLUMN = "use.mode.columns";	
	public final static boolean CFG_USE_MODECOLUMN_DFT = false;
	private boolean m_useModeColumnsSM;
	
	// setting 6
	private final String CFG_CREATE_NEW_COLUMN = "create.new.column";	
	public final static boolean CFG_CREATE_NEW_COLUMN_DFT = true;
	private boolean m_createNewColumnSM;
	
	// setting 7
	private final String CFG_OUT_COLUMN_NAME = "out.column.name";
	public final static String CFG_OUT_COLUMN_NAME_DFT = "Interval";
	private String m_outColumnSM;
	
	// setting 8
	private final String CFG_FIXED_MODE = "fixed.mode";
	private final Mode CFG_FIXED_MODE_DFT = Interval.Mode.INCL_LEFT;
	private String m_fixedModeSM;
	
	
	public CreateIntervalNodeSettings(String configName) {
		m_configName = configName;
		
		initDefaultSettings();
	}

	private void initDefaultSettings() {
		m_leftBoundSM = null;
		m_rightBoundSM = null;
		
		m_leftModeColumnSM = null;
		m_rightModeColumnSM = null;
		
		m_outColumnSM = CFG_OUT_COLUMN_NAME_DFT;
		
		m_useModeColumnsSM = CFG_USE_MODECOLUMN_DFT;
		m_createNewColumnSM = CFG_CREATE_NEW_COLUMN_DFT;
		
		m_fixedModeSM = CFG_FIXED_MODE_DFT.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CreateIntervalNodeSettings createClone() {
		CreateIntervalNodeSettings clonedSM = new CreateIntervalNodeSettings(m_configName);
		
		clonedSM.setLeftBoundColumn(m_leftBoundSM);
		clonedSM.setRightBoundColumn(m_rightBoundSM);
		clonedSM.setLeftModeColumn(m_leftModeColumnSM);
		clonedSM.setRightModeColumn(m_rightModeColumnSM);
		clonedSM.setCreateColumnFlag(m_createNewColumnSM);
		clonedSM.setModeColumnsFlag(m_useModeColumnsSM);
		clonedSM.setFixedMode(m_fixedModeSM);
		clonedSM.setOutColumnName(m_outColumnSM);
		
		return clonedSM;
	}

	public void setRightBoundColumn(String rightBoundColumnName) {
		m_rightBoundSM = rightBoundColumnName;
	}

	public void setLeftBoundColumn(String leftBoundColumnName) {
		m_leftBoundSM = leftBoundColumnName;
	}
	
	public void setLeftModeColumn(String leftModeColumnName) {
		m_leftModeColumnSM = leftModeColumnName;
	}
	
	public void setRightModeColumn(String rightModeColumnName) {
		m_rightModeColumnSM = rightModeColumnName;
	}
	
	public void setFixedMode(String mode) 
			throws IllegalArgumentException {
		if(!EnumUtils.isValidEnum(Mode.class, mode))
			throw new IllegalArgumentException("Fixed mode cannot be set to \""
					+ mode + "\" which is not a value of {" + Mode.values() + "}");
		m_fixedModeSM = mode;
	}
	
	public void setFixedMode(Mode mode) {
		m_fixedModeSM = mode.toString();
	}
	
	/**
	 * set flag whether mode columns are used
	 * 
	 * @param enabled	true, if mode columns are required
	 */
	public void setModeColumnsFlag(boolean enabled) {
		m_useModeColumnsSM = enabled;
	}
	
	public void setCreateColumnFlag(boolean create) {
		m_createNewColumnSM = create;
	}
	
	public void setOutColumnName(String columnName) {
		m_outColumnSM = columnName;
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
		// try to load from settings, ignore if missing in 'settings'
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(m_configName);
			m_leftBoundSM = mySettings.getString(CFG_LEFTBOUND);
			m_rightBoundSM = mySettings.getString(CFG_RIGHTBOUND);
			m_leftModeColumnSM = mySettings.getString(CFG_LEFTMODE);
			m_rightModeColumnSM = mySettings.getString(CFG_RIGHTMODE);
			m_useModeColumnsSM = mySettings.getBoolean(CFG_USE_MODECOLUMN);
			m_createNewColumnSM = mySettings.getBoolean(CFG_CREATE_NEW_COLUMN);
			m_outColumnSM = mySettings.getString(CFG_OUT_COLUMN_NAME);
			setFixedMode(mySettings.getString(CFG_FIXED_MODE));
		} catch (InvalidSettingsException e) {
			// ignore
		} catch (IllegalArgumentException e)  {
			// ignore
		}	
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		saveSettingsForDialog(settings);
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		String mode;
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(m_configName);
			mySettings.getString(CFG_LEFTBOUND);
			mySettings.getString(CFG_RIGHTBOUND);
			mySettings.getString(CFG_LEFTMODE);
			mySettings.getString(CFG_RIGHTMODE);
			mySettings.getBoolean(CFG_USE_MODECOLUMN);
			mySettings.getBoolean(CFG_CREATE_NEW_COLUMN);
			mySettings.getString(CFG_OUT_COLUMN_NAME);
			mode = mySettings.getString(CFG_FIXED_MODE);
		} catch (InvalidSettingsException ise) {
			throw new InvalidSettingsException(getClass().getSimpleName()
                    + " - " + m_configName + ": " +  ise.getMessage());
		} 

		if(!EnumUtils.isValidEnum(Mode.class, mode))
			throw new InvalidSettingsException("Fixed mode cannot be set to \""
					+ mode + "\" which is not a value of {" + Mode.values() + "}");
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(m_configName);
			m_leftBoundSM = mySettings.getString(CFG_LEFTBOUND);
			m_rightBoundSM = mySettings.getString(CFG_RIGHTBOUND);
			m_leftModeColumnSM = mySettings.getString(CFG_LEFTMODE);
			m_rightModeColumnSM = mySettings.getString(CFG_RIGHTMODE);
			m_useModeColumnsSM = mySettings.getBoolean(CFG_USE_MODECOLUMN);
			setFixedMode(mySettings.getString(CFG_FIXED_MODE));
			m_createNewColumnSM = mySettings.getBoolean(CFG_CREATE_NEW_COLUMN);
			m_outColumnSM = mySettings.getString(CFG_OUT_COLUMN_NAME);
		} catch (InvalidSettingsException ise) {
			throw new InvalidSettingsException(getClass().getSimpleName()
                    + " - " + m_configName + ": " + ise.getMessage());
		} catch (IllegalArgumentException iae)  {
			throw new InvalidSettingsException(getClass().getSimpleName()
                    + " - " + m_configName + ": " + iae.getMessage());
		}
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		
		NodeSettingsWO mySettings = settings.addNodeSettings(m_configName);
		
		mySettings.addString(CFG_LEFTBOUND, m_leftBoundSM);
		mySettings.addString(CFG_RIGHTBOUND, m_rightBoundSM);
		mySettings.addString(CFG_LEFTMODE, m_leftModeColumnSM);
		mySettings.addString(CFG_RIGHTMODE, m_rightModeColumnSM);
		mySettings.addBoolean(CFG_USE_MODECOLUMN, m_useModeColumnsSM);
		mySettings.addString(CFG_FIXED_MODE, m_fixedModeSM);
		mySettings.addBoolean(CFG_CREATE_NEW_COLUMN, m_createNewColumnSM);
		mySettings.addString(CFG_OUT_COLUMN_NAME, m_outColumnSM);
	}

	@Override
	public String toString() {		
		return this.getClass().getSimpleName()  + " ('" + m_configName + "')";
	}

	/**
	 * get name of the column with left bound values
	 * 
	 * @return column name (can be null)
	 */
	public String getLeftBoundColumn() {		
		return m_leftBoundSM;
	}

	/**
	 * get name of the column with right bound values
	 * 
	 * @return column name (can be null)
	 */
	public String getRightBoundColumn() {
		return m_rightBoundSM;
	}
	
	/**
	 * get name of column with left mode values
	 * 
	 * @return column name (can be null)
	 */
	public String getLeftModeColumn() {
		return m_leftModeColumnSM;
	}
	
	/**
	 * get name of column with right mode values
	 * 
	 * @return column name (can be null)
	 */
	public String getRightModeColumn() {
		return m_rightModeColumnSM;
	}

	/**
	 * @return true, if mode columns are required
	 */
	public boolean useModeColumns() {
		return m_useModeColumnsSM;
	}
	

	
	/**
	 * @return Mode for interval bounds; string representation of {@link Interval.Mode}
	 */
	public String getFixedMode() {
		return m_fixedModeSM;
	}
	
	/**
	 * @return true, if a new column should be created
	 * false in case of replacement of an old column
	 */
	public boolean createNewColumn() {
		return m_createNewColumnSM;
	}
	
	/**
	 * @return name of the interval column <br/>
	 * It's either the name for the new column or the name of the column to be replaced
	 */
	public String getOutColumnName() {
		return m_outColumnSM;
	}
	
}
