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

import org.apache.commons.lang3.StringUtils;
import org.knime.base.node.preproc.groupby.GroupByTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLong;
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
	
	/**
	 * Node configuration keys and default values
	 */
	
	public static final String CFG_GROUPS = "group.by";
	
	public static final String CFG_MISSING = "ignore.missing";
	public static final boolean CFG_MISSING_DFT = true;
	
	public static final String CFG_INCOMPLETE = "ignore.incomplete";
	public static final boolean CFG_INCOMPLETE_DFT = true;
	
	public static final String CFG_SORTED = "already.sorted";
	public static final boolean CFG_SORTED_DFT = false;
	
	
	
	public static final String CFG_SAMPLING = "use.sampling";
	public static final boolean CFG_SAMPLING_DFT = false;
	
	public static final String CFG_SEED = "use.random.seed";
	public static final boolean CFG_SEED_DFT = false;
	
	public static final String CFG_SAMPLE_SIZE = "sample.size";
	public static final int CFG_SAMPLE_SIZE_DFT = 100;
	
	public static final String CFG_SEED_VALUE = "random.seed";
	public static final long CFG_SEED_VALUE_DFT = System.currentTimeMillis();
	
	// labels for values below or above outer intervals
	
	public static final String KEY_LOWER = "lower values";
	public static final String KEY_HIGHER = "higher values";

	/**
	 * constructor
	 */
	protected BinningApplyNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE, BinningPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	/**
	 * add model settings
	 */
	private void initializeSettings() {
		this.addModelSetting(CFG_GROUPS, (SettingsModel) createGroupFilterModel());
		this.addModelSetting(CFG_MISSING, (SettingsModel) createIgnoreMissingSettingsModel()); 
		this.addModelSetting(CFG_INCOMPLETE, (SettingsModel) createIgnoreIncompleteSettingsModel());
		this.addModelSetting(CFG_SORTED, (SettingsModel) createSortedSettingsModel());
		
		this.addModelSetting(CFG_SAMPLING, (SettingsModel) createUseSamplingSettingsModel());
		this.addModelSetting(CFG_SEED, (SettingsModel) createUseSeedSettingsModel());
		this.addModelSetting(CFG_SAMPLE_SIZE, (SettingsModel) createSampleSizeSettingsModel());
		this.addModelSetting(CFG_SEED_VALUE, (SettingsModel) createSeedValueSettingsModel());
	}

	/**
	 * incoming data already sorted?
	 * @return {@link SettingsModelBoolean}
	 */
	private SettingsModel createSortedSettingsModel() {
		return new SettingsModelBoolean(CFG_SORTED, CFG_SORTED_DFT);
	}

	/**
	 * if model contains column but missing in incoming table - ignore?
	 * @return {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createIgnoreMissingSettingsModel() {
		return new SettingsModelBoolean(CFG_MISSING, CFG_MISSING_DFT);
	}
	
	/**
	 * if model for a column is incomplete (less bins) - dismiss?
	 * @return {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createIgnoreIncompleteSettingsModel() {
		return new SettingsModelBoolean(CFG_INCOMPLETE, CFG_INCOMPLETE_DFT);
	}
	
	/**
	 * column filter for grouping incoming data
	 * @return {@link SettingsModelColumnFilter2}
	 */
	public static SettingsModelColumnFilter2 createGroupFilterModel() {
		return new SettingsModelColumnFilter2(CFG_GROUPS);
	}
	
	/**
	 * use random samples instead all datapoints per group?
	 * @return {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createUseSamplingSettingsModel() {
		return new SettingsModelBoolean(CFG_SAMPLING, CFG_SAMPLING_DFT);
	}

	/**
	 * use fixed seed value for random sampling?
	 * @return {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createUseSeedSettingsModel() {
		SettingsModelBoolean sm = new SettingsModelBoolean(CFG_SEED, CFG_SEED_DFT);
		return sm;
	}
	
	/**
	 * sample size for random sampling
	 * @return {@link SettingsModelIntegerBounded}
	 */
	public static SettingsModelIntegerBounded createSampleSizeSettingsModel() {
		SettingsModelIntegerBounded sm = new SettingsModelIntegerBounded(CFG_SAMPLE_SIZE, CFG_SAMPLE_SIZE_DFT, 0, Integer.MAX_VALUE);
		return sm;
	}
	
	/**
	 * seed value for random sampling
	 * @return {@link SettingsModelLong}
	 */
	public static SettingsModelLong createSeedValueSettingsModel() {
		SettingsModelLong sm = new SettingsModelLong(CFG_SEED_VALUE, CFG_SEED_VALUE_DFT);
		return sm;
	}


	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * create list of data column specs based on grouping columns
	 * @param inSpec
	 * @param groupingColumns
	 * @return linked list of {@link DataColumnSpec}
	 */
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
		
		return new DataTableSpec[] {dtsc.createSpec(), dtsc.createSpec()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		
		BufferedDataTable inData = (BufferedDataTable) inObjects[0];
		DataTableSpec inSpec = inData.getDataTableSpec();
		BinningPortObject inModel = (BinningPortObject) inObjects[1];
		
		// retrieve node settings
		//boolean ignoreMissing = ((SettingsModelBoolean) this.getModelSetting(CFG_MISSING)).getBooleanValue();
		boolean dismissIncomplete = ((SettingsModelBoolean) this.getModelSetting(CFG_INCOMPLETE)).getBooleanValue();
		FilterResult filter = ((SettingsModelColumnFilter2) this.getModelSetting(CFG_GROUPS)).applyTo(inSpec);
		String[] groupingColumns = filter.getIncludes();
		boolean alreadySorted = ((SettingsModelBoolean) this.getModelSetting(CFG_SORTED)).getBooleanValue();
		
		boolean useSampling = ((SettingsModelBoolean) this.getModelSetting(CFG_SAMPLING)).getBooleanValue();
		boolean useSeed = ((SettingsModelBoolean) this.getModelSetting(CFG_SEED)).getBooleanValue();
		int sampleSize = ((SettingsModelIntegerBounded) this.getModelSetting(CFG_SAMPLE_SIZE)).getIntValue();
		long seedValue = ((SettingsModelLong) this.getModelSetting(CFG_SEED_VALUE)).getLongValue();
		
		SamplingSettings sampling = new SamplingSettings(useSampling, sampleSize, useSeed, seedValue);
		
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
		List<String> incomplete = new LinkedList<String>();
		for(String col : model.getColumns()) {			
			if(inSpec.containsName(col)) {
				if(binMap.containsKey(col)) {
					LinkedList<Interval> ivList = (LinkedList<Interval>) binMap.get(col);
					if(ivList.size() < nBins && dismissIncomplete)
						incomplete.add(col);
					else
						columnsToProcess.add(col);
				}
			} 
		}
		
		if(!incomplete.isEmpty()) 
			this.setWarningMessage("Incomplete models: The following columns will not be processed: " + String.join(", ", incomplete));
		
		if(columnsToProcess.isEmpty())
			this.setWarningMessage("No columns suitable for processing.");
		
		BufferedDataTable[] countTables = createBinningCountsTables(exec, inData, 
				createCountDataTableSpec(getGroupColumnSpecs(inSpec, groupingColumns)),
				columnsToProcess, groupingColumns, binMap, sampling, alreadySorted);
		
		return new BufferedDataTable[] {countTables[0], countTables[1]};
	}
	
	/**
	 * 1) sorting the table based on grouping columns
	 * 2) iterate through table collecting values per group
	 * 3) for each group create count data based on bins
	 * 
	 * @param exec					ExecutionContext
	 * @param inData				incoming data
	 * @param countDataTableSpec	table specs for out-tables
	 * @param columnsToProcess		list of columns to bin
	 * @param groupingColumns		list of columns to group on
	 * @param binMap				map of binning models
	 * @param sampling				settings for sampling
	 * @param alreadySorted			input data already sorted?
	 * 
	 * @return						result tables (count data and count data for extreme values)
	 * @throws CanceledExecutionException
	 */
    private BufferedDataTable[] createBinningCountsTables(
    		ExecutionContext exec, 
    		BufferedDataTable inData,
			DataTableSpec[] countDataTableSpec, 
			List<String> columnsToProcess, 
			String[] groupingColumns, 
			Map<String, LinkedList<Interval>> binMap, 
			SamplingSettings sampling, 
			boolean alreadySorted) throws CanceledExecutionException {
    	
    	final DataTableSpec inSpec = inData.getDataTableSpec();  	
    	final BufferedDataTable sortedTable;
        final ExecutionContext groupExec;
        Map<String, DataValueComparator> comparators = new HashMap<String, DataValueComparator>();
        
        // put grouping columns from array into list
        List<String> columnsToGroup = new LinkedList<String>();
        for(String col : groupingColumns)
        	columnsToGroup.add(col);
        
        final int nColumns = columnsToGroup.size();
        
        // sort incoming table based on grouping columns
        if (alreadySorted || nColumns < 1) {
            sortedTable = inData;
            groupExec = exec;
        } else {
            final ExecutionContext sortExec =
                exec.createSubExecutionContext(0.5);
            exec.setMessage("Sorting input table...");
            sortedTable = GroupByTable.sortTable(sortExec, inData, columnsToGroup);
            sortExec.setProgress(1.0);
            groupExec = exec.createSubExecutionContext(0.5);                   
        }

        
        // for each grouping column, register comparator
        for(String col : columnsToGroup) {
        	final DataColumnSpec colSpec = inSpec.getColumnSpec(col);
        	comparators.put(col, colSpec.getType().getComparator());
        }
        
        // output table containers
        final BufferedDataContainer dc = exec.createDataContainer(countDataTableSpec[0]);
        final BufferedDataContainer extremeDc = exec.createDataContainer(countDataTableSpec[1]);
             
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
        for(String col : columnsToProcess) {
        	processColIdx.put(col, new Integer(inSpec.findColumnIndex(col)));
        }
        
        // for each column to process count missing data
        Map<String, MutableInteger> countMissing = createMissingCountMap(columnsToProcess);
        
        // count data goes into this map (per column, per interval, new map per group)
        Map<String, Map<String, MutableInteger>> countData = null;
        
        // collects row data for each column to process until new group has bee detected
        // row key => <name of column to process + its data cell>
        Map<RowKey, Map<String, DataCell>> rowMap = new HashMap<RowKey, Map<String, DataCell>>();
        
        // set of detected groups (uniqueness check!)
        // <name of column to group on + its data cell>
        Set<Map<String, DataCell>> groupSet = new HashSet<Map<String, DataCell>>();
        
        int groupCounter = 0;		// count groups
        boolean firstRow = true;
        final double numOfRows = sortedTable.size();
        long rowCounter = 0;		// row counter for count data
        long extremeRowCounter = 0;	// row counter for extreme count data      
        boolean newGroup = false;	// new group detected?
        String groupLabel = null;	// label of the current group
              
        exec.setMessage("Creating groups");
           
        // iterate over sorted table
        for (final DataRow row : sortedTable) {
        	
        	// fill previous values if this is the first row
        	if(firstRow) {
        		for(String col : columnsToGroup)
        			previousGroup.put(col, row.getCell(colIdx.get(col)));
        		firstRow = false;
        		groupLabel = createGroupLabelForProgress(previousGroup);
        	}
        	
        	// collect data cells of columns to process if not missing
        	RowKey key = row.getKey();
        	Map<String, DataCell> dataMap = new HashMap<String, DataCell>();
        	for(String col : columnsToProcess) {        		
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
        		
        		// check if new group did not yet appear (for pre-sorted data)
        		if(alreadySorted && !groupSet.add(currentGroup)) {
        			throw new DuplicateGroupException("Input table was not sorted by grouping columns");
        		}
        		
        		// reduce data of that group if sampling is wanted
        		if(sampling.getUseSampling()) {
        			rowMap = selectSubset(rowMap, sampling);
        		}
        		
        		// create count data
        		countData = calculateCounts(columnsToProcess, binMap, rowMap, groupExec);
        		
        		// create new rows for count data and extreme count data
        		List<LinkedList<DefaultRow>> rows = createRows(previousGroup, countData, rowCounter, extremeRowCounter);  
        		
        		// add the new rows to their table
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
        	
        	// collect row data (important: not before group check as data of a new group 
        	// should go to the new collection of row data
        	rowMap.put(key, dataMap);
        	
        	groupExec.checkCanceled();
            groupExec.setProgress(++rowCounter/numOfRows, groupLabel);	// TODO: does that make sense?
        }
        
        // process last group (needs to be the same steps like for new group in the loop!)
		if(alreadySorted && !groupSet.add(currentGroup)) {
			throw new DuplicateGroupException("Input table was not sorted by grouping columns");
		}
        if(sampling.getUseSampling()) {
        	rowMap = selectSubset(rowMap, sampling);
        }
		countData = calculateCounts(columnsToProcess, binMap, rowMap, groupExec);
        List<LinkedList<DefaultRow>> rows = createRows(previousGroup, countData, rowCounter, extremeRowCounter);  
		
		for(DefaultRow r : rows.get(0)) {
			dc.addRowToTable(r);
			rowCounter ++;
		}
		for(DefaultRow r : rows.get(1)) {
			extremeDc.addRowToTable(r);
			extremeRowCounter ++;
		}
		// process last group end
        
		
        dc.close();
        extremeDc.close();
       
        showWarningForMissing(countMissing);
        
		return new BufferedDataTable[]{dc.getTable(),extremeDc.getTable()};
	}
    
    /**
     * applies the binning models to the data of a single group
     * 
     * @param columnsToProcess	list of columns to apply the binning models to
     * @param binMap			binning models
     * @param rowMap			group data
     * @param groupExec			execution context
     * 
     * @return count data and count data for extreme values
     * 
     * count data structure:
     * 		column name =>
     * 				'lower values' => count
     * 				interval label 1 => count
     * 				...
     * 				interval label n => count
     * 				'higher values' => count
     * 
     * @throws CanceledExecutionException
     */
    private Map<String, Map<String, MutableInteger>> calculateCounts(
    		List<String> columnsToProcess, 
    		Map<String, LinkedList<Interval>> binMap, 
    		Map<RowKey, Map<String, DataCell>> rowMap,  
    		ExecutionContext groupExec) 
    				throws CanceledExecutionException {
    	
    	Map<String, Map<String, MutableInteger>> countData = createCountMap(columnsToProcess, binMap);
    	
    	// iterate over all rows of this group
		for(RowKey r : rowMap.keySet()) {
			Map<String, DataCell> rowValues = rowMap.get(r);
			
			// iterate over all columns to process
			for(String col : columnsToProcess) {

				DataCell cell = rowValues.get(col);
				
				if(cell == null)	// in case of missing values
					continue;
				    		
				if(cell.isMissing()) {
					this.logger.error("implementation problem: missing values should be filtered out before.");
					continue;
				}
			
				double value = ((DoubleValue)cell).getDoubleValue();

				// get model and check which interval the value belongs to
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
							label = KEY_LOWER;
						first = false;
					}
				}
				// if no interval found => values is higher than maximum
				if(label == null)
					label = KEY_HIGHER;

				countData.get(col).get(label).inc();
			}
			groupExec.checkCanceled();
		}
		return countData;
	}

    /**
     * random row sampling of the rows of one group
     * @param rowMap		collected row data
     * @param sampling		sampling settings
     * 
     * @return subset of row data
     */
	private Map<RowKey, Map<String, DataCell>> selectSubset(
			Map<RowKey, Map<String, DataCell>> rowMap, 
			SamplingSettings sampling) {
    	
		/*
		 * Randomly select rows (if requested)
		 */
		
		int nRequired = sampling.getSampleSize();
		int rowCount = rowMap.size();
		boolean toFew = (rowCount - nRequired > 0) ? false : true;
		
		if(!toFew) {

			//TODO: check what happens if rouwCount is big AND required is nearly as big...
			// how long does it take to get all rowkeys
			
			// create index set based on random selection
			Set<Integer> idxSet = new HashSet<Integer>();
			while(nRequired > 0) {
				Integer r = new Integer(sampling.getNextRandomValue(rowCount));
				boolean added = idxSet.add(r);
				if(added) {
					nRequired--;
				}
			}

			// get set of row-keys from index set
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

	/** Get a string describing the current group. Used in progress message. 
	 * (copied from {@link org.knime.base.node.preproc.groupby.BigGroupByTable}
	 * 
     * @param previousGroup 	The current group
     * 
     * @return group string 
     * */
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
    
    /**
     * create warning message for missing values in processing columns
     * @param countMissing
     */
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

    /**
     * create an empty map to count missing values for each processing column
     * @param columnsToProcess
     * @return	empty map with mutable integers set to 0
     */
	private Map<String, MutableInteger> createMissingCountMap(List<String> columnsToProcess) {
    	Map<String, MutableInteger> map = new LinkedHashMap<String,MutableInteger>();
    	
    	for(String col : columnsToProcess) {
    		map.put(col, new MutableInteger(0));
    	}
    	
		return map;
	}

	/**
	 * creates rows for the output tables based on count data
	 * 	
	 * @param previousGroup			group data
	 * @param countData				count data
	 * @param rowCounter			row counter for count data table
	 * @param extremeRowCounter		row counter for extreme count data table
	 * 
	 * @return list of new rows for each output table
	 */
	private LinkedList<LinkedList<DefaultRow>> createRows( 
			Map<String, DataCell> previousGroup, 
			Map<String, Map<String, MutableInteger>> countData, 
			long rowCounter, 
			long extremeRowCounter) {
    	
    	LinkedList<DefaultRow> addRows = new LinkedList<DefaultRow>();
    	LinkedList<DefaultRow> addExtremeRows = new LinkedList<DefaultRow>();
    	
    	//for each processing column
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
    		// iterate over intervals and create rows
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
    				counts = counts + countMap.get(KEY_HIGHER).longValue();
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

	/**
	 * create empty map for count data
	 * 
	 * @param columnsToProcess	columns for processing
	 * @param binMap			binning models
	 * 
	 * @return empty map for count data (inits mutable integer with 0)
	 */
	private Map<String, Map<String, MutableInteger>> createCountMap(
			List<String> columnsToProcess, 
			Map<String, LinkedList<Interval>> binMap) {
    	
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
	
	/**
	 * class to store sampling settings and retrieve random values
	 * 
	 * @author Antje Janosch
	 *
	 */
	public class SamplingSettings {
		// settings set to their defaults
		private boolean m_useSampling = CFG_SAMPLING_DFT;	// use sampling? (not necessary...)
		private boolean m_useSeed = CFG_SEED_DFT;			// use seed value?
		private int m_sampleSize = CFG_SAMPLE_SIZE_DFT;		// sample size
		private long m_seedValue = CFG_SEED_VALUE_DFT;		// seed value
		
		private final Random m_random;	// random number generator
		
		/**
		 * constructor, creates random number generator based on seed value (if wanted)
		 * 
		 * @param useSampling
		 * @param sampleSize
		 * @param useSeed
		 * @param seedValue
		 */
		public SamplingSettings(boolean useSampling, int sampleSize, boolean useSeed, long seedValue) {
			this.m_sampleSize = sampleSize;
			this.m_seedValue = seedValue;
			this.m_useSampling = useSampling;
			this.m_useSeed = useSeed;
			if(useSeed) {
				this.m_random = new Random(seedValue);
			} else {
				this.m_random = new Random();
			}
		}
		
		public boolean getUseSampling() {
			return m_useSampling;
		}
		
		public boolean getUseSeed() {
			return m_useSeed;
		}
		
		public int getSampleSize() {
			return m_sampleSize;
		}
		
		public long getSeedValue() {
			return m_seedValue;
		}
		
		/**
		 * retrieve next random integer values
		 * @param bound		maximum int value
		 * @return random integer value
		 */
		public int getNextRandomValue(int bound) {
			return m_random.nextInt(bound);
		}
	}
	
	/**
	 * Exception thrown if group appears multiple times
	 * 
	 * @author Antje Janosch
	 *
	 */
	@SuppressWarnings("serial")
	public class DuplicateGroupException extends RuntimeException {
		
		public DuplicateGroupException(final String message) {
			super(message);
		}
	}
}
