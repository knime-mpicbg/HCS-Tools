package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.core.node.util.filter.nominal.NominalValueFilterConfiguration;
import org.knime.core.node.util.filter.nominal.NominalValueFilterConfiguration.NominalValueFilterResult;

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
	
	public SettingsModelValueFilter createModelSettingSubsetSelection() {
		return new SettingsModelValueFilter(CFG_SUBSET_SEL, null);
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
		
		DataTableSpec inSpec = inSpecs[0];
		
		String groupColumn = ((SettingsModelString)this.getModelSetting(CFG_GROUP)).getStringValue();
		String subsetColumn = ((SettingsModelString)this.getModelSetting(CFG_SUBSET_COL)).getStringValue();
		
		// group column should not be the same like subset column
		if(groupColumn.equals(subsetColumn) && !groupColumn.equals(CFG_GROUP_DFT))
			throw new InvalidSettingsException("Group column and subset column should differ. Please reconfigure the node.");
		
		// if group column not <none>, check if column is available
		if(!groupColumn.equals(CFG_SUBSET_COL_DFT) && !inSpec.containsName(groupColumn))
			throw new InvalidSettingsException("Incoming data table does miss the grouping column \"" + groupColumn + "\"");
		
		// if subset column not <none>
		if(!subsetColumn.equals(CFG_SUBSET_COL_DFT)) {
			SettingsModelValueFilter smvf = ((SettingsModelValueFilter)this.getModelSetting(CFG_SUBSET_SEL));
			
			// check if selected column name is the same like the name of the value filter column
			if(!smvf.getSelectedColumn().equals(subsetColumn))
				throw new InvalidSettingsException("Model setting inconsistency. Column name of subset column is different from domain value filter column name");
		
			// check column for availability
			if(!inSpec.containsName(smvf.getSelectedColumn())) {
				throw new InvalidSettingsException("Incoming data table does miss the subset column \"" + smvf.getSelectedColumn() + "\"");
			}
			else {
				/*Set<DataCell> domain = inSpec.getColumnSpec(subsetColumn).getDomain().getValues();
				NominalValueFilterConfiguration ncfg = smvf.getFilterConfig();
				NominalValueFilterResult filterResult = ncfg.applyTo(domain);
				
				String[] incl = filterResult.getIncludes();
				
				// check if at least one subset value has been included 
				if(incl.length == 0)
					throw new InvalidSettingsException("Subset selection does not include any possible values. Please reconfigure the node.");*/
			}			
		}
		
		// suffix can be empty string
		// booleans do not matter
		
		// get grouping columns and deliver specs to output table spec
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_PARAMETERS)).applyTo(inSpec);
		String[] parameterColumns = filter.getIncludes();
		
		checkColumnsForAvailability(inSpec, parameterColumns, true, false);
		
		//DataTableSpec outSpec = createOutputSpecs();

		return new DataTableSpec[]{null};
	}
	


	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}
	
	

}
