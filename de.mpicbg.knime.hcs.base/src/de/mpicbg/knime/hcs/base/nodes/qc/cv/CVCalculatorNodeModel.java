package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;

import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of the new CV node.
 * 
 * @author Antje Janosch
 *
 */
public class CVCalculatorNodeModel extends AbstractNodeModel {
	
	public static final String CFG_GROUP = "group.by";
	public static final String CFG_GROUP_DFT = "<none>";
	
	public static final String CFG_SUBSET_COL = "subset.column";
	public static final String CFG_SUBSET_COL_DFT = "<none>";
	
	public static final String CFG_SUBSET_SEL = "subset.selection";
	
	public static final String CFG_PARAMETERS = "parameters";
	
	public static final String CFG_USE_ROBUST = "use.robust.statistics";
	public static final boolean CFG_USE_ROBUST_DFT = false;
	
	public static final String CFG_USE_SUFFIX = "use.suffix";
	public static final boolean CFG_USE_SUFFIX_DFT = false;
	
	public static final String CFG_SUFFIX = "suffix";
	public static final String CFG_SUFFIX_DFT = ".cv";
	
	/**
	 * constructor
	 */
	protected CVCalculatorNodeModel() {
		super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	private void initializeSettings() {
		this.addModelSetting(CFG_GROUP, createModelSettingGroup());
		this.addModelSetting(CFG_SUBSET_COL, createModelSettingSubset());
		this.addModelSetting(CFG_PARAMETERS, createModelSettingParameterSelection());
		this.addModelSetting(CFG_SUBSET_SEL, createModelSettingSubsetSelection());
		this.addModelSetting(CFG_USE_ROBUST, createModelSettingUseRobustStatistics());
		this.addModelSetting(CFG_USE_SUFFIX, createModelSettingUseSuffix());
		this.addModelSetting(CFG_SUFFIX, createModelSettingSuffix());
	}

	public SettingsModelString createModelSettingGroup() {
		return new SettingsModelString(CFG_GROUP, CFG_GROUP_DFT);
	}
	
	public SettingsModelString createModelSettingSubset() {
		return new SettingsModelString(CFG_SUBSET_COL, CFG_SUBSET_COL_DFT);
	}
	
	@SuppressWarnings("unchecked")
	public SettingsModelColumnFilter2 createModelSettingParameterSelection() {
		return new SettingsModelColumnFilter2(CFG_PARAMETERS, DoubleValue.class);
	}
	
	public SettingsModelFilterString createModelSettingSubsetSelection() {
		return new SettingsModelFilterString(CFG_SUBSET_SEL);
	}
	
	public SettingsModelBoolean createModelSettingUseSuffix() {
		return new SettingsModelBoolean(CFG_USE_SUFFIX, CFG_USE_SUFFIX_DFT);
	}
	
	public SettingsModelBoolean createModelSettingUseRobustStatistics() {
		return new SettingsModelBoolean(CFG_USE_ROBUST, CFG_USE_ROBUST_DFT);
	}
	
	public SettingsModelString createModelSettingSuffix() {
		return new SettingsModelString(CFG_SUFFIX, CFG_SUFFIX_DFT);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		return super.configure(inSpecs);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}
	
	

}
