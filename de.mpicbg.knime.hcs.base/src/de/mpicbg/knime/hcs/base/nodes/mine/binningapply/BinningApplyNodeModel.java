package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knime.base.node.preproc.groupby.GroupByTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.core.util.MutableInteger;

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
	
	public final String KEY_LOWER = "lower values";
	public final String KEY_HIGHER = "higher values";

	/**
	 * constructor
	 */
	protected BinningApplyNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE, BinningPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE}, true);

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
		
		DataTableSpec[] countDataTableSpecs = createCountDataTableSpec(groupingSpecs);
		
		return new DataTableSpec[]{countDataTableSpecs[0], countDataTableSpecs[1]};
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
	private DataTableSpec[] createCountDataTableSpec(List<DataColumnSpec> groupingSpecs) {
		DataTableSpecCreator dtsc = new DataTableSpecCreator();
		
		// add column specs of grouping columns
		for(DataColumnSpec dcs : groupingSpecs)
			dtsc.addColumns(dcs);
		
		DataColumnSpecCreator colCreator;
		colCreator = new DataColumnSpecCreator("Parameter", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Interval", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Counts", IntCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Percentage", DoubleCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		
		/*DataTableSpecCreator extremeCountsSpec = new DataTableSpecCreator();
		
		// add column specs of grouping columns
		for(DataColumnSpec dcs : groupingSpecs)
			extremeCountsSpec.addColumns(dcs);
		
		colCreator = new DataColumnSpecCreator("Parameter", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Interval", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Counts", IntCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Percentage", DoubleCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());*/
		
		//TODO: might be enough to return one spec if it is the same for both tables...
		
		return new DataTableSpec[] {dtsc.createSpec(), dtsc.createSpec()};
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
		
		BufferedDataTable[] countTables = createBinningCountsTables(exec, inData, 
				createCountDataTableSpec(getGroupColumnSpecs(inSpec, groupingColumns)),
				columnsToProcess, groupingColumns, binMap);
		
		return new BufferedDataTable[] {countTables[0], countTables[1]};
	}
	
    private BufferedDataTable[] createBinningCountsTables(ExecutionContext exec, BufferedDataTable inData,
			DataTableSpec[] countDataTableSpec, List<String> columnsToProcess, String[] groupingColumns, Map<String, LinkedList<Interval>> binMap) throws CanceledExecutionException {
    	
    	final DataTableSpec inSpec = inData.getDataTableSpec();  	
    	final BufferedDataTable sortedTable;
        final ExecutionContext groupExec;
        Map<String, DataValueComparator> comparators = new HashMap<String, DataValueComparator>();
        
        List<String> columnsToGroup = new LinkedList<String>();
        for(String col : groupingColumns)
        	columnsToGroup.add(col);
        
        final int nColumns = columnsToGroup.size();
        
        if (nColumns < 1) {
            sortedTable = inData;
            groupExec = exec;
        } else {
            final ExecutionContext sortExec =
                exec.createSubExecutionContext(0.6);
            exec.setMessage("Sorting input table...");
            sortedTable = GroupByTable.sortTable(sortExec, inData, columnsToGroup);
            sortExec.setProgress(1.0);
            groupExec = exec.createSubExecutionContext(0.4);
            for(String col : columnsToGroup) {
            	final DataColumnSpec colSpec = inSpec.getColumnSpec(col);
            	comparators.put(col, colSpec.getType().getComparator());
            }        
        }
        
        final BufferedDataContainer dc = exec.createDataContainer(countDataTableSpec[0]);
        final BufferedDataContainer extremeDc = exec.createDataContainer(countDataTableSpec[1]);
        exec.setMessage("Creating groups");
        //final DataCell[] previousGroup = new DataCell[nColumns];
        //final DataCell[] currentGroup = new DataCell[nColumns];
        Map<String, DataCell> previousGroup = new LinkedHashMap<String, DataCell>();
        Map<String, DataCell> currentGroup = new LinkedHashMap<String, DataCell>();
        
        Map<String, Integer> colIdx = new LinkedHashMap<String, Integer>();
        for(String col : columnsToGroup) {
        	colIdx.put(col, new Integer(inSpec.findColumnIndex(col)));
        }
        Map<String, Integer> processColIdx = new LinkedHashMap<String, Integer>();
        for(String col : columnsToProcess) {
        	processColIdx.put(col, new Integer(inSpec.findColumnIndex(col)));
        }
        
        int groupCounter = 0;
        boolean firstRow = true;
        final double numOfRows = sortedTable.size();
        long rowCounter = 0;
        long extremeRowCounter = 0;
        Map<String, MutableInteger> countMissing = createMissingCountMap(columnsToProcess);
        boolean newGroup = false;
        
        // count data goes into this map (per column, per interval, new map per group)
        Map<String, Map<String, MutableInteger>> countData = createCountMap(columnsToProcess, binMap);
        
           
        for (final DataRow row : sortedTable) {
        	// fill previous values if this is the first row
        	if(firstRow) {
        		for(String col : columnsToGroup)
        			previousGroup.put(col, row.getCell(colIdx.get(col)));
        		firstRow = false;
        	}
        	
    		//count data
        	for(String col : columnsToProcess) {
        		
        		if(row.getCell(processColIdx.get(col)).isMissing()) {
        			((MutableInteger)countMissing.get(col)).inc();
        			continue;
        		}
        		
        		double value = ((DoubleCell)row.getCell(processColIdx.get(col))).getDoubleValue();
        		
        		LinkedList<Interval> ivList = binMap.get(col);
        		String label = null;	// will get the key where to increase the count
        		boolean first = true;
        		for(Interval iv : ivList) {
        			
        			// keep label of interval if value belongs to it
        			if(iv.contains(value))
        				label = iv.getLabel();
        			// if the first interval is tested and the value did not belong to it,
        			// check whether it is lower
        			if(label == null && first) {
        				if(iv.isBelowLowerBound(value))
        					label = this.KEY_LOWER;
        				first = false;
        			}
        		}
        		// if no interval found => values is higher than maximum
        		if(label == null)
        			label = this.KEY_HIGHER;

        		countData.get(col).get(label).inc();
        	}
        	
        	
        	for(String col : columnsToGroup) {
        		DataCell currentCell = row.getCell(colIdx.get(col));
        		DataCell previousCell = previousGroup.get(col);
        		
        		// if cells are not the same a new group starts
        		if(comparators.get(col).compare(currentCell, previousCell) != 0) {
        			newGroup = true;
        		}
        		currentGroup.put(col, currentCell);
        	}
        	
        	if(newGroup) {
        		
        		List<LinkedList<DefaultRow>> rows = createRows(groupCounter, previousGroup, countData, rowCounter, extremeRowCounter);  
        		
        		for(DefaultRow r : rows.get(0)) {
        			dc.addRowToTable(r);
        			rowCounter ++;
        		}
        		for(DefaultRow r : rows.get(1)) {
        			extremeDc.addRowToTable(r);
        			extremeRowCounter ++;
        		}
        		
        		groupCounter++;
        		newGroup = false;
        		previousGroup.clear();
        		previousGroup.putAll(currentGroup);
        		countData = createCountMap(columnsToProcess, binMap);
        	}
        }
        
        // last row
        List<LinkedList<DefaultRow>> rows = createRows(groupCounter, previousGroup, countData, rowCounter, extremeRowCounter);  
		
		for(DefaultRow r : rows.get(0)) {
			dc.addRowToTable(r);
			rowCounter ++;
		}
		for(DefaultRow r : rows.get(1)) {
			extremeDc.addRowToTable(r);
			extremeRowCounter ++;
		}
        
        dc.close();
        extremeDc.close();
        
        showWarningForMissing(countMissing);
        
		return new BufferedDataTable[]{dc.getTable(),extremeDc.getTable()};
	}
    
    private void showWarningForMissing(Map<String, MutableInteger> countMissing) {
    	
    	int n = countMissing.size();
    	String[] message = new String[n];
    	int i = 0;
    	for(String col : countMissing.keySet()) {
    		message[i] = col + " (" + countMissing.get(col).longValue() + ")";
    		i++;
    	}
    	
    	String warningMessage = "Ignored missing values for: " + String.join(", ", message);
    	
		this.setWarningMessage(warningMessage);
	}

	private Map<String, MutableInteger> createMissingCountMap(List<String> columnsToProcess) {
    	Map<String, MutableInteger> map = new LinkedHashMap<String,MutableInteger>();
    	
    	for(String col : columnsToProcess) {
    		map.put(col, new MutableInteger(0));
    	}
    	
		return map;
	}

	private LinkedList<LinkedList<DefaultRow>> createRows(int groupCounter, Map<String, DataCell> previousGroup, Map<String, Map<String, MutableInteger>> countData, long rowCounter, long extremeRowCounter) {
    	
    	LinkedList<DefaultRow> addRows = new LinkedList<DefaultRow>();
    	LinkedList<DefaultRow> addExtremeRows = new LinkedList<DefaultRow>();
    	
    	for(String col : countData.keySet()) {
    		Map<String, MutableInteger> countMap = countData.get(col);
    		int nBins = countMap.size() - 2;
    		
    		// get sum to calculate the percentage
    		long sum = 0;
    		for(String ivLabel : countMap.keySet()) {
    			sum = sum + countMap.get(ivLabel).longValue();
    		}
    		
    		/**
    		 * for 5 bins
    		 * LOWER:	keep counts
    		 * 20% bin: write row with counts + kept (set keep back to 0)
    		 * 40% bin: write row with counts
    		 * 60% bin: write row with counts
    		 * 80% bin:	write row with counts
    		 * 100% bin: get HIGHER and add to counts; write row
    		 * HIGHER:	don't do anything
    		 */
    		int i = 0;
    		long keep = 0;
    		for(String ivLabel : countMap.keySet()) {
    			
    			long counts = countMap.get(ivLabel).longValue();
    			boolean setRow = false;
    			boolean setExtremeRow = false;
    			
    			if(i == 0) {
    				keep = counts;
    				setExtremeRow = true;
    			}
    			if(i > 0 && i < nBins) {
    				counts = counts + keep;
    	    		setRow = true;
    			}
    			if(i == nBins) {
    				counts = counts + countMap.get(this.KEY_HIGHER).longValue();
    				setRow = true;
    			}
    			if(i == nBins + 1) {
    				setExtremeRow = true;
    			}
    			
    			if(setRow) {
    				final RowKey rowKey = RowKey.createRowKey((long)rowCounter);
    	    		
    	    		List<DataCell> list = new ArrayList<DataCell>(previousGroup.values());
    	    		list.add(new StringCell(col));
    	    		list.add(new StringCell(ivLabel));
    	    		list.add(new IntCell((int)counts));
    	    		list.add(new DoubleCell((double)counts / (double)sum * 100.0));
    	
    	    		addRows.add(new DefaultRow(rowKey, list));
    	    		rowCounter ++;
    	    		keep = 0;
    			}
    			if(setExtremeRow) {
    				final RowKey rowKey = RowKey.createRowKey((long)extremeRowCounter);
    	    		
    	    		List<DataCell> list = new ArrayList<DataCell>(previousGroup.values());
    	    		list.add(new StringCell(col));
    	    		list.add(new StringCell(ivLabel));
    	    		list.add(new IntCell((int)counts));
    	    		list.add(new DoubleCell((double)counts / (double)sum * 100.0));
    	
    	    		addExtremeRows.add(new DefaultRow(rowKey, list));
    	    		extremeRowCounter ++;
    			}
    			i++;
    		}
    	}
    	
    	LinkedList<LinkedList<DefaultRow>> rows = new LinkedList<LinkedList<DefaultRow>>();
    	rows.add(addRows);
    	rows.add(addExtremeRows);
    	
		return rows;
	}

	private Map<String, Map<String, MutableInteger>> createCountMap(List<String> columnsToProcess, Map<String, LinkedList<Interval>> binMap) {
    	
    	Map<String, Map<String, MutableInteger>> countData = new LinkedHashMap<String, Map<String, MutableInteger>>();
        // init map with zeros
        for(String col : columnsToProcess) {
        	
        	Map<String, MutableInteger> countMap = new LinkedHashMap<String, MutableInteger>();
        	LinkedList<Interval> ivList = binMap.get(col);
        	// first add a key for values lower than minimum value
        	countMap.put(KEY_LOWER, new MutableInteger(0));
        	for(Interval iv : ivList) {
        		countMap.put(iv.getLabel(), new MutableInteger(0));
        	}
        	// last add a key for values higher than maximum value
        	countMap.put(KEY_HIGHER, new MutableInteger(0));
        	
        	countData.put(col, countMap);
        }
        
        return countData;
    }
}
