package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.base.node.preproc.groupby.GroupByTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.util.MutableInteger;

import de.mpicbg.knime.hcs.base.nodes.mine.binningapply.BinningApplyNodeModel;
import de.mpicbg.knime.hcs.base.utils.ExtDescriptiveStats;
import de.mpicbg.knime.hcs.base.utils.MadStatistic.IllegalMadFactorException;
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
	
	public static final String CFG_CHANGE_SUFFIX = "change.suffix";
	public static final boolean CFG_CHANGE_SUFFIX_DFT = false;
	
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
		this.addModelSetting(CFG_CHANGE_SUFFIX, createModelSettingChangeSuffix());
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
	
	public SettingsModelBoolean createModelSettingChangeSuffix() {
		return new SettingsModelBoolean(CFG_CHANGE_SUFFIX, CFG_CHANGE_SUFFIX_DFT);
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
			
		checkGroupColumnSettingAgainstInSpec(inSpec, groupColumn);
				
		checkSubsetColumnSettingAgainstInSpec(inSpec, subsetColumn);
		
		// suffix can be empty string
		// booleans do not matter
		
		// get grouping columns and deliver specs to output table spec
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_PARAMETERS)).applyTo(inSpec);
		String[] parameterColumns = filter.getIncludes();
		
		checkColumnsForAvailability(inSpec, parameterColumns, DoubleValue.class, true, false);
		
		
		boolean changeSuffix = ((SettingsModelBoolean) this.getModelSetting(CFG_CHANGE_SUFFIX)).getBooleanValue();
		String suffix = ((SettingsModelString) this.getModelSetting(CFG_SUFFIX)).getStringValue();
		suffix = changeSuffix ? suffix : CFG_SUFFIX_DFT;
			
		DataTableSpec outSpec = createOutputSpecs(inSpec, groupColumn, subsetColumn, parameterColumns, suffix);

		return new DataTableSpec[]{outSpec};
	}

	private void checkSubsetColumnSettingAgainstInSpec(DataTableSpec inSpec, String subsetColumn)
			throws InvalidSettingsException {
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
				// check for expected data type and the presence of domain values
				DataColumnSpec spec = inSpec.getColumnSpec(subsetColumn);
				if(!(spec.getType().isCompatible(StringValue.class) && spec.getDomain().hasValues()))
					throw new InvalidSettingsException("Subset column \"" + subsetColumn + "\" is either not of type String or does not contain domain values.");
				
				Set<DataCell> domain = inSpec.getColumnSpec(subsetColumn).getDomain().getValues();
				NominalValueFilterConfiguration ncfg = smvf.getFilterConfig();
				NominalValueFilterResult filterResult = ncfg.applyTo(domain);
				
				String[] incl = filterResult.getIncludes();
				String[] formerIncl = filterResult.getRemovedFromIncludes();
				
				// check if at least one subset value has been included 
				if(incl.length == 0)
					this.setWarningMessage("All subsets are excluded. Node will produce an empty table");
				if(formerIncl.length > 0) {
					String subsets = String.join(", ",formerIncl);
					this.setWarningMessage("The following subsets are not present anymore in the incoming data table: " + subsets);
				}
			}			
		}
	}

	private void checkGroupColumnSettingAgainstInSpec(DataTableSpec inSpec, String groupColumn)
			throws InvalidSettingsException {
		// if group column not <none>, check if column is available
		if(!groupColumn.equals(CFG_SUBSET_COL_DFT)) {
			if(!inSpec.containsName(groupColumn))
				throw new InvalidSettingsException("Incoming data table does miss the grouping column \"" + groupColumn + "\"");
			else
				if(!inSpec.getColumnSpec(groupColumn).getType().isCompatible(StringValue.class))
					throw new InvalidSettingsException("The data type of the group column \"" + groupColumn + "\" is not string.");
		} else {
			// check that incoming table doe not contain a column named <none>
			if(inSpec.containsName(groupColumn))
				throw new InvalidSettingsException("Incoming data table should not contain a column named \"" + CFG_GROUP_DFT + "\" as it a node settings default to not group a t all.");
		}
	}
	


	private DataTableSpec createOutputSpecs(DataTableSpec inSpec, String groupColumn, String subsetColumn, String[] parameterColumns,
			String suffix) {
		
		DataTableSpecCreator dtsc = new DataTableSpecCreator();
		
		if(!groupColumn.equals(CFG_GROUP_DFT)) {
			DataColumnSpec cSpec = inSpec.getColumnSpec(groupColumn);
			dtsc.addColumns(cSpec);
		}
		if(!subsetColumn.equals(CFG_SUBSET_COL_DFT)) {
			DataColumnSpec cSpec = inSpec.getColumnSpec(subsetColumn);
			dtsc.addColumns(cSpec);
		}
		
		for(String parameter : parameterColumns) {
			DataColumnSpecCreator colCreator;
			String newColumnName = parameter + suffix;
			colCreator = new DataColumnSpecCreator(newColumnName,DoubleCell.TYPE);
			dtsc.addColumns(colCreator.createSpec());
		}
		
		return dtsc.createSpec();
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		
		BufferedDataTable inTable = inData[0];
		DataTableSpec inSpec = inTable.getDataTableSpec();
		
		CVNodeSettings settings = new CVNodeSettings();
		
		// get node settings		
		String groupColumn = ((SettingsModelString)this.getModelSetting(CFG_GROUP)).getStringValue();
		String subsetColumn = ((SettingsModelString)this.getModelSetting(CFG_SUBSET_COL)).getStringValue();
		
		settings.setGroupColumn(groupColumn);
		settings.setSubsetColumn(subsetColumn);
		
		// get grouping columns and deliver specs to output table spec
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_PARAMETERS)).applyTo(inTable.getDataTableSpec());
		String[] parameterColumns = filter.getIncludes();
		List<String> paramColumnList = new LinkedList<String>(Arrays.asList(parameterColumns));
		
		settings.setParameterColumns(parameterColumns);
		
		boolean useRobustStats = ((SettingsModelBoolean) this.getModelSetting(CFG_USE_ROBUST)).getBooleanValue();		
		boolean changeSuffix = ((SettingsModelBoolean) this.getModelSetting(CFG_CHANGE_SUFFIX)).getBooleanValue();
		String suffix = ((SettingsModelString) this.getModelSetting(CFG_SUFFIX)).getStringValue();
		
		settings.setRobustStatisticsFlag(useRobustStats);
		settings.setSuffixFlag(changeSuffix);
		if(changeSuffix)
			settings.setSuffix(suffix);
		else
			settings.setSuffix(CFG_SUFFIX_DFT);
		
		SettingsModelValueFilter smvf = ((SettingsModelValueFilter) this.getModelSetting(CFG_SUBSET_SEL));
		String[] subsetSelection = getIncludedSubsets(smvf, inSpec);
		
		settings.setSubsetSelection(subsetSelection);
		
		// sort input table		
		HashMap<String, String> groupMap = new LinkedHashMap<String, String>();
		if(!groupColumn.equals(CFG_GROUP_DFT))
			groupMap.put(CFG_GROUP, groupColumn);
		if(!subsetColumn.equals(CFG_SUBSET_COL_DFT))
			groupMap.put(CFG_SUBSET_COL, subsetColumn);
		
		List<String> columnsToGroup = new ArrayList<String>(groupMap.values());
		
        exec.createSubExecutionContext(0.5);
        exec.setMessage("Sorting input table...");
        
        final BufferedDataTable sortedTable;
        final ExecutionContext sortExec = exec.createSubExecutionContext(0.5);
        sortedTable = GroupByTable.sortTable(sortExec, inTable, columnsToGroup);
        sortExec.setProgress(1.0);
        
        final ExecutionContext groupExec;
        groupExec = exec.createSubExecutionContext(0.5);
        
        // output table container
        DataTableSpec outSpec = createOutputSpecs(inTable.getDataTableSpec(), groupColumn, subsetColumn, parameterColumns, suffix);
        final BufferedDataContainer dc = exec.createDataContainer(outSpec);

        // maps for current group and previous group
        Map<String, DataCell> previousGroup = new LinkedHashMap<String, DataCell>();
        Map<String, DataCell> currentGroup = new LinkedHashMap<String, DataCell>();

        // map with column indices for grouping columns
        Map<String, Integer> colIdx = new LinkedHashMap<String, Integer>();
        for(String col : columnsToGroup) {
        	colIdx.put(col, new Integer(inSpec.findColumnIndex(col)));
        }
        
        // map with column indices for processing columns
        Map<String, Integer> processColIdx = new LinkedHashMap<String, Integer>();
        for(String col : parameterColumns) {
        	processColIdx.put(col, new Integer(inSpec.findColumnIndex(col)));
        }
        
        // for each grouping column, register comparator
        Map<String, DataValueComparator> comparators = new HashMap<String, DataValueComparator>();
        for(String col : columnsToGroup) {
        	final DataColumnSpec colSpec = inSpec.getColumnSpec(col);
        	comparators.put(col, colSpec.getType().getComparator());
        }
        
        // for each column to process count missing data
        Map<String, MutableInteger> countMissing = BinningApplyNodeModel.createMissingCountMap(paramColumnList);

        // collects row data for each column to process until new group has bee detected
        // row key => <name of column to process + its data cell>
        Map<RowKey, Map<String, DataCell>> rowMap = new HashMap<RowKey, Map<String, DataCell>>();
        
        boolean firstRow = true;
        boolean newGroup = false;
        String groupLabel = null;	// label of the current group
        long currentRowIdx = 0;
        long rowCounter = 0;
        final double numOfRows = sortedTable.size();

        for(DataRow row : sortedTable) {
        	// fill previous values if this is the first row
        	if(firstRow) {
        		for(String col : columnsToGroup)
        			previousGroup.put(col, row.getCell(colIdx.get(col)));
        		firstRow = false;
        		groupLabel = BinningApplyNodeModel.createGroupLabelForProgress(previousGroup);
        	}
        	
        	// collect data cells of columns to process if not missing
        	RowKey key = row.getKey();
        	Map<String, DataCell> dataMap = new HashMap<String, DataCell>();
        	for(String col : parameterColumns) {        		
        		//dataMap.put(col, row.getCell(processColIdx.get(col)));     
        		DataCell cell = row.getCell(processColIdx.get(col));
        		if(cell.isMissing())
        			countMissing.get(col).inc();
        		else
        			dataMap.put(col, row.getCell(processColIdx.get(col)));
        	}

        	// compare previous group with current group
        	for(String col : columnsToGroup) {
        		DataCell currentCell = row.getCell(colIdx.get(col));
        		DataCell previousCell = previousGroup.get(col);
        		
        		// if cells are not the same a new group starts
        		if(comparators.get(col).compare(currentCell, previousCell) != 0) {
        			newGroup = true;
        		}
        		currentGroup.put(col, currentCell);
        	}
        	
        	// if new group has been detected
        	if(newGroup) {
        		// do something
        		if(subsetIsIncluded(previousGroup, groupMap, settings)) {
        			DefaultRow outRow = createRow(previousGroup, rowMap, settings, rowCounter);  
        			dc.addRowToTable(outRow);
        			rowCounter++;
        		}
        		
        		newGroup = false;
        		previousGroup = new LinkedHashMap<String, DataCell>();
        		previousGroup.putAll(currentGroup);
        		currentGroup = new LinkedHashMap<String, DataCell>();
        		groupLabel = BinningApplyNodeModel.createGroupLabelForProgress(previousGroup);
        		rowMap = new HashMap<RowKey, Map<String, DataCell>>();
        	}
        	
        	// collect row data (important: not before group check as data of a new group 
        	// should go to the new collection of row data
        	rowMap.put(key, dataMap);
        	
        	groupExec.checkCanceled();
        	currentRowIdx ++;
            groupExec.setProgress(currentRowIdx/numOfRows, groupLabel);
        }
        
        // process last group
        // do something
		if(subsetIsIncluded(previousGroup, groupMap, settings)) {
			DefaultRow outRow = createRow(previousGroup, rowMap, settings, rowCounter);  
			dc.addRowToTable(outRow);
		}
        
        dc.close();

		return new BufferedDataTable[]{dc.getTable()};
		
	}

	private String[] getIncludedSubsets(SettingsModelValueFilter smvf, DataTableSpec inSpec) {
		
		String subsetColumn = smvf.getSelectedColumn();
		
		if(subsetColumn.equals(CFG_SUBSET_COL_DFT))
			return new String[] {};
		
		Set<DataCell> domainValues = inSpec.getColumnSpec(subsetColumn).getDomain().getValues();
		
		NominalValueFilterConfiguration filterConfig = smvf.getFilterConfig();
		NominalValueFilterResult filterResult = filterConfig.applyTo(domainValues);
		return filterResult.getIncludes();
	}

	private boolean subsetIsIncluded(Map<String, DataCell> previousGroup, HashMap<String, String> groupMap,
			CVNodeSettings settings) {
		
		// if subset column is set to <none> all groups (defined by grouping column) will be included
		if(!groupMap.containsKey(CFG_SUBSET_COL))
			return true;
		
		String subsetValue = ((StringCell)previousGroup.get(groupMap.get(CFG_SUBSET_COL))).getStringValue();
		
		return settings.doesSubsetContain(subsetValue);
	}

	private DefaultRow createRow(Map<String, DataCell> previousGroup, Map<RowKey, Map<String, DataCell>> rowMap, CVNodeSettings settings, long rowCounter) {
		
		List<String> parameterColumn = settings.getParameterColumns();
		
		final RowKey rowKey = RowKey.createRowKey((long)rowCounter);
		
		List<DataCell> list = new ArrayList<DataCell>(previousGroup.values());
		
		HashMap<String, ExtDescriptiveStats> dataMap = new HashMap<String, ExtDescriptiveStats>();
		
		for(RowKey key : rowMap.keySet()) {
			for(String param : parameterColumn) {
				if(!dataMap.containsKey(param))
					dataMap.put(param, new ExtDescriptiveStats());
				DataCell cell = rowMap.get(key).get(param);
				dataMap.get(param).addValue(((DoubleValue)cell).getDoubleValue());
			}
		}
			
		for(String param : parameterColumn) {
			
			ExtDescriptiveStats stats = dataMap.get(param);
			
			try {
				double location = settings.getRobustStatisticsFlag() ? stats.getMedian() :stats.getMean();
				double dispersion = settings.getRobustStatisticsFlag() ? stats.getMad() : stats.getStandardDeviation();
				
				double cv = 100 * (dispersion / location);
				list.add(new DoubleCell(cv));
				
			} catch (IllegalMadFactorException e) {
				list.add(DataType.getMissingCell());
			}
		}
		
		return new DefaultRow(rowKey, list);
	}
	
	

}
