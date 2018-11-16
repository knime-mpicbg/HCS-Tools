package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
                exec.createSubExecutionContext(0.5);
            exec.setMessage("Sorting input table...");
            sortedTable = GroupByTable.sortTable(sortExec, inData, columnsToGroup);
            sortExec.setProgress(1.0);
            groupExec = exec.createSubExecutionContext(0.5);
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
        Map<String, Map<String, MutableInteger>> countData = null;
        
        Map<RowKey, Map<String, DataCell>> rowMap = new HashMap<RowKey, Map<String, DataCell>>();
        
        String groupLabel = null;
           
        for (final DataRow row : sortedTable) {
        	// fill previous values if this is the first row
        	if(firstRow) {
        		for(String col : columnsToGroup)
        			previousGroup.put(col, row.getCell(colIdx.get(col)));
        		firstRow = false;
        		groupLabel = createGroupLabelForProgress(previousGroup);
        	}
        	
        	RowKey key = row.getKey();
        	Map<String, DataCell> dataMap = new HashMap<String, DataCell>();
        	
    		//count data
        	for(String col : columnsToProcess) {
           		
        		dataMap.put(col, row.getCell(processColIdx.get(col)));     		
        		/*if(row.getCell(processColIdx.get(col)).isMissing()) {
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

        		countData.get(col).get(label).inc();*/
        	}
        	
        	rowMap.put(key, dataMap);
        	
        	
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
        		
        		rowMap = selectSubset(rowMap);
        		countData = calculateCounts(columnsToProcess, binMap, rowMap, countMissing, groupExec);
        		
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
        		groupLabel = createGroupLabelForProgress(previousGroup);
        		rowMap = new HashMap<RowKey, Map<String, DataCell>>();
        	}
        	
        	groupExec.checkCanceled();
            groupExec.setProgress(++rowCounter/numOfRows, groupLabel);
        }
        
        // last row
        rowMap = selectSubset(rowMap);
		countData = calculateCounts(columnsToProcess, binMap, rowMap, countMissing, groupExec);
        List<LinkedList<DefaultRow>> rows = createRows(groupCounter, previousGroup, countData, rowCounter, extremeRowCounter);  
		
		for(DefaultRow r : rows.get(0)) {
			dc.addRowToTable(r);
			rowCounter ++;
		}
		for(DefaultRow r : rows.get(1)) {
			extremeDc.addRowToTable(r);
			extremeRowCounter ++;
		}
		// last row end
        
        dc.close();
        extremeDc.close();
        
        showWarningForMissing(countMissing);
        
		return new BufferedDataTable[]{dc.getTable(),extremeDc.getTable()};
	}
    
    private Map<String, Map<String, MutableInteger>> calculateCounts(
    		List<String> columnsToProcess, 
    		Map<String, LinkedList<Interval>> binMap, 
    		Map<RowKey, Map<String, DataCell>> rowMap, 
    		Map<String, MutableInteger> countMissing, 
    		ExecutionContext groupExec) 
    				throws CanceledExecutionException {
    	
    	Map<String, Map<String, MutableInteger>> countData = createCountMap(columnsToProcess, binMap);
		for(RowKey r : rowMap.keySet()) {
			Map<String, DataCell> rowValues = rowMap.get(r);
			for(String col : columnsToProcess) {

				DataCell cell = rowValues.get(col);
				    		
				if(cell.isMissing()) {
					((MutableInteger)countMissing.get(col)).inc();
					continue;
				}

				double value = ((DoubleCell)cell).getDoubleValue();

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
			groupExec.checkCanceled();
		}
		return countData;
	}

	private Map<RowKey, Map<String, DataCell>> selectSubset(Map<RowKey, Map<String, DataCell>> rowMap) {
    	/*
		 * Randomly select rows (if requested)
		 */
		Random generator = new Random();
		
		int nRequired = 700;
		int rowCount = rowMap.size();
		boolean toFew = (rowCount - nRequired > 0) ? false : true;
		
		if(!toFew) {
			//int nRows = (rowCount - nRequired > 0) ? nRequired : rowCount;

			//TODO: check what happens if rouwCount is big AND required is nearly as big...
			// how long does it take to get all rowkeys
			Set<Integer> idxSet = new HashSet<Integer>();
			while(nRequired > 0) {
				Integer r = new Integer(generator.nextInt(rowCount));
				boolean added = idxSet.add(r);
				if(added) {
					nRequired--;
				}
			}

			Object[] keys = rowMap.keySet().toArray();
			Set<RowKey> keepRows = new HashSet<RowKey>();
			for(Integer i : idxSet) {
				keepRows.add((RowKey)keys[i.intValue()]);
			}

			// filter for random selected rows
			rowMap.keySet().retainAll(keepRows);
		}
		
		return rowMap;
	}

	/** Get a string describing the current group. Used in progress message. (copied from {@link org.knime.base.node.preproc.groupby.BigGroupByTable}
     * @param previousGroup The current group
     * @return That string. */
    private String createGroupLabelForProgress(final Map<String, DataCell> previousGroup) {
        final StringBuilder b = new StringBuilder("(");
        int i = 0;
        for (String col : previousGroup.keySet()) {
            b.append(i > 0 ? "; " : "");
            if (i > 3) {
                b.append("...");
            } else {
                b.append('\"');
                String s = previousGroup.get(col).toString();
                if (s.length() > 31) {
                    s = s.substring(0, 30).concat("...");
                }
                s = s.replace('\n', '_');
                b.append(s).append('\"');
            }
        }
        b.append(')');
        return b.toString();
    }
    
    private void showWarningForMissing(Map<String, MutableInteger> countMissing) {
    	
    	boolean showWarning = false;
    	int n = countMissing.size();
    	String[] message = new String[n];
    	int i = 0;
    	for(String col : countMissing.keySet()) {
    		long nMissing = countMissing.get(col).longValue();
    		if(nMissing > 0) showWarning = true;
    		message[i] = col + " (" + nMissing + ")";
    		i++;
    	}
    	
    	String warningMessage = "Ignored missing values for: " + String.join(", ", message);
    	
		if(showWarning) this.setWarningMessage(warningMessage);
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
