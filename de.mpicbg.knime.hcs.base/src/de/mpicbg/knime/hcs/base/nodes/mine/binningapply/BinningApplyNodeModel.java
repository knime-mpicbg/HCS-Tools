package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

import de.mpicbg.knime.hcs.base.node.port.binning.BinningPortObject;
import de.mpicbg.knime.hcs.base.node.port.binning.BinningPortObjectSpec;
import de.mpicbg.knime.hcs.core.math.BinningAnalysisModel;
import de.mpicbg.knime.hcs.core.math.Interval;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of BinningApply.
 * 
 * @author Antje Janosch
 *
 */
public class BinningApplyNodeModel extends AbstractNodeModel {
	
	public static final String CFG_GROUPS = "group.by";
	
	public static final String CFG_MISSING = "ignore.missing";
	public static final boolean CFG_MISSING_DFT = true;
	
	public static final String CFG_INCOMPLETE = "ignore.incomplete";
	public static final boolean CFG_INCOMPLETE_DFT = true;

	/**
	 * constructor
	 */
	protected BinningApplyNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE, BinningPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	private void initializeSettings() {
		this.addModelSetting(CFG_GROUPS, (SettingsModel) createGroupFilterModel());
		this.addModelSetting(CFG_MISSING, (SettingsModel) createIgnoreMissingSettingsModel()); 
		this.addModelSetting(CFG_INCOMPLETE, (SettingsModel) createIgnoreIncompleteSettingsModel()); 
	}

	public static SettingsModelBoolean createIgnoreMissingSettingsModel() {
		return new SettingsModelBoolean(CFG_MISSING, CFG_MISSING_DFT);
	}
	
	public static SettingsModelBoolean createIgnoreIncompleteSettingsModel() {
		return new SettingsModelBoolean(CFG_INCOMPLETE, CFG_INCOMPLETE_DFT);
	}

	public static SettingsModelColumnFilter2 createGroupFilterModel() {
		return new SettingsModelColumnFilter2(CFG_GROUPS);
	}

	@Override
	protected DataTableSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		
		DataTableSpec inSpec = (DataTableSpec) inSpecs[0];
		BinningPortObjectSpec modelSpec = (BinningPortObjectSpec) inSpecs[1];
		
		boolean ignoreMissing = ((SettingsModelBoolean) this.getModelSetting(CFG_MISSING)).getBooleanValue();
				
		assert modelSpec != null;
		
		String[] modelColumns = modelSpec.getColumnNames();
		
		assert modelColumns.length > 0;
		
		// collect columns which are not available in input spec
		List<String> missingColumns = new ArrayList<String>();
		for(String col : modelColumns) {
			if(!inSpec.containsName(col))
				missingColumns.add(col);
		}		
		if(!missingColumns.isEmpty()) {
			if(ignoreMissing)
				setWarningMessage("Input table is missing the following columns for processing (will be ignored): " + String.join(",", missingColumns));
			else 
				throw new InvalidSettingsException("Input table is missing the following columns for processing: " + String.join(",", missingColumns));
		}
		
		// get grouping columns and deliver specs to output table spec
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_GROUPS)).applyTo(inSpec);
		String[] groupingColumns = filter.getIncludes();
		
		List<DataColumnSpec> groupingSpecs = getGroupColumnSpecs(inSpec, groupingColumns);
		
		return new DataTableSpec[]{createCountDataTableSpec(groupingSpecs)};
	}

	private List<DataColumnSpec> getGroupColumnSpecs(DataTableSpec inSpec, String[] groupingColumns) {
		List<DataColumnSpec> groupingSpecs = new LinkedList<DataColumnSpec>();
		for(String col : groupingColumns) {
			groupingSpecs.add(inSpec.getColumnSpec(col));
		}
		return groupingSpecs;
	}

	/**
	 * create the table spec for the output data containing count data
	 * @param groupingSpecs 
	 * 
	 * @return {@link DataTableSpec}
	 */
	private DataTableSpec createCountDataTableSpec(List<DataColumnSpec> groupingSpecs) {
		DataTableSpecCreator dtsc = new DataTableSpecCreator();
		
		// add column specs of grouping columns
		for(DataColumnSpec dcs : groupingSpecs)
			dtsc.addColumns(dcs);
		
		return dtsc.createSpec();
	}

	@Override
	protected BufferedDataTable[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		BufferedDataTable inData = (BufferedDataTable) inObjects[0];
		DataTableSpec inSpec = inData.getDataTableSpec();
		BinningPortObject inModel = (BinningPortObject) inObjects[1];
		
		// retrieve node settings
		//boolean ignoreMissing = ((SettingsModelBoolean) this.getModelSetting(CFG_MISSING)).getBooleanValue();
		boolean ignoreIncomplete = ((SettingsModelBoolean) this.getModelSetting(CFG_INCOMPLETE)).getBooleanValue();
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_GROUPS)).applyTo(inSpec);
		String[] groupingColumns = filter.getIncludes();
		
		// load model
		BinningAnalysisModel model = inModel.getBinningModel();
		Map<String, LinkedList<Interval>>binMap = model.getModel();
		int nBins = model.getNBins();
		
		// check if
		// - column is available in input column
		// - interval map is available
		// - if 'ignoreIncomplete'; models with fewer bins are allowed
		// - otherwise allow only complete models
		// => list with columns which can and should be binned
		List<String> columnsToProcess = new LinkedList<String>();
		for(String col : model.getColumns()) {			
			if(inSpec.containsName(col)) {
				if(binMap.containsKey(col)) {
					LinkedList<Interval> ivList = (LinkedList<Interval>) binMap.get(col);
					if(ivList.size() < nBins && ignoreIncomplete)
						columnsToProcess.add(col);
					if(ivList.size() == nBins)
						columnsToProcess.add(col);
				}
			} 
		}
		
		BufferedDataContainer buf = exec.createDataContainer(createCountDataTableSpec(getGroupColumnSpecs(inSpec, groupingColumns)));
		buf.close();
		
		// TODO Auto-generated method stub
		return new BufferedDataTable[] {buf.getTable()};
	}
	
	
}
