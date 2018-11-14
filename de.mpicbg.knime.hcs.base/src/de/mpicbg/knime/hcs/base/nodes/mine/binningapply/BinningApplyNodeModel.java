package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		
		DataColumnSpecCreator colCreator;
		colCreator = new DataColumnSpecCreator("Parameter", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Interval", StringCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Counts", IntCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator("Percentage", DoubleCell.TYPE);
		dtsc.addColumns(colCreator.createSpec());
		
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
		
		BufferedDataTable countTable = createBinningCountsTable(exec, inData, 
				createCountDataTableSpec(getGroupColumnSpecs(inSpec, groupingColumns)),
				columnsToProcess, groupingColumns, binMap);
		
		/*BufferedDataContainer buf = exec.createDataContainer(createCountDataTableSpec(getGroupColumnSpecs(inSpec, groupingColumns)));
		
		for(String currentColumn : columnsToProcess) {
			LinkedList<Interval> ivList = (LinkedList<Interval>) binMap.get(currentColumn);
			
			
		}
		
		buf.close();*/
		
		// TODO Auto-generated method stub
		return new BufferedDataTable[] {countTable};
	}
	
    private BufferedDataTable createBinningCountsTable(ExecutionContext exec, BufferedDataTable inData,
			DataTableSpec countDataTableSpec, List<String> columnsToProcess, String[] groupingColumns, Map<String, LinkedList<Interval>> binMap) throws CanceledExecutionException {
    	
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
        
        final BufferedDataContainer dc = exec.createDataContainer(countDataTableSpec);
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
        		
        		List<DefaultRow> addRows = createRows(groupCounter, previousGroup, countData, rowCounter);     		
        		for(DefaultRow r : addRows) {
        			dc.addRowToTable(r);
        			rowCounter ++;
        		}
        		
        		groupCounter++;
        		newGroup = false;
        		previousGroup.clear();
        		previousGroup.putAll(currentGroup);
        		countData = createCountMap(columnsToProcess, binMap);
        	}
        }
        
        // last row
        List<DefaultRow> addRows = createRows(groupCounter, previousGroup, countData, rowCounter);     		
		for(DefaultRow r : addRows) {
			dc.addRowToTable(r);
			rowCounter ++;
		}
		
		
        
        dc.close();
        
		return dc.getTable();
	}
    
    private LinkedList<DefaultRow> createRows(int groupCounter, Map<String, DataCell> previousGroup, Map<String, Map<String, MutableInteger>> countData, long rowCounter) {
    	
    	LinkedList<DefaultRow> addRows = new LinkedList<DefaultRow>();
    	
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
    			
    			if(i == 0) {
    				keep = counts;
    			}
    			if(i > 0 && i < nBins) {
    				counts = counts + keep;
    	    		setRow = true;
    			}
    			if(i == nBins) {
    				counts = counts + countMap.get(this.KEY_HIGHER).longValue();
    				setRow = true;
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
    			i++;
    		}
    	}
		
		return addRows;
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

/*	protected BufferedDataTable createGroupByTable(final ExecutionContext exec,
            final BufferedDataTable table, final DataTableSpec resultSpec,
            final int[] groupColIdx) throws CanceledExecutionException {
        //LOGGER.debug("Entering createGroupByTable(exec, table) "
        //        + "of class BigGroupByTable.");
        final DataTableSpec origSpec = table.getDataTableSpec();
        
        //sort the data table in order to process the input table chunk wise
        final BufferedDataTable sortedTable;
        final ExecutionContext groupExec;
        final DataValueComparator[] comparators;
        if (groupColIdx.length < 1) {
            sortedTable = table;
            groupExec = exec;
            comparators = new DataValueComparator[0];
        } else {
            final ExecutionContext sortExec =
                exec.createSubExecutionContext(0.6);
            exec.setMessage("Sorting input table...");
            sortedTable = sortTable(sortExec, table, getGroupCols());
            sortExec.setProgress(1.0);
            groupExec = exec.createSubExecutionContext(0.4);
            comparators = new DataValueComparator[groupColIdx.length];
            for (int i = 0, length = groupColIdx.length; i < length; i++) {
                final DataColumnSpec colSpec =
                    origSpec.getColumnSpec(groupColIdx[i]);
                comparators[i] = colSpec.getType().getComparator();
            }
        }
        final BufferedDataContainer dc = exec.createDataContainer(resultSpec);
        exec.setMessage("Creating groups");
        final DataCell[] previousGroup = new DataCell[groupColIdx.length];
        final DataCell[] currentGroup = new DataCell[groupColIdx.length];
        final MutableInteger groupCounter = new MutableInteger(0);
        boolean firstRow = true;
        final double numOfRows = sortedTable.size();
        long rowCounter = 0;
        //In the rare case that the DataCell comparator return 0 for two
        //data cells that are not equal we have to maintain a map with all
        //rows with equal cells in the group columns per chunk.
        //This variable stores for each chunk these members. A chunk consists
        //of rows which return 0 for the pairwise group value comparison.
        //Usually only equal data cells return 0 when compared with each other
        //but in rare occasions also data cells that are NOT equal return 0 when
        //compared to each other
        //(such as cells that contain chemical structures).
        //In this rare case this map will contain for each group of data cells
        //that are pairwise equal in the chunk a separate entry.
        final Map<GroupKey, Pair<ColumnAggregator[], Set<RowKey>>> chunkMembers = new LinkedHashMap<>(3);
        boolean logUnusualCells = true;
        String groupLabel = "";
        initMissingValuesMap();  // cannot put init to the constructor, as the super() constructor directly calls the current function
        for (final DataRow row : sortedTable) {
            //fetch the current group column values
            for (int i = 0, length = groupColIdx.length; i < length; i++) {
                currentGroup[i] = row.getCell(groupColIdx[i]);
            }
            if (firstRow) {
                groupLabel = createGroupLabelForProgress(currentGroup);
                System.arraycopy(currentGroup, 0, previousGroup, 0,
                        currentGroup.length);
                firstRow = false;
            }
            //check if we are still in the same data chunk which contains
            //rows that return 0 for all pairwise comparisons of their
            //group column data cells
            if (!sameChunk(comparators, previousGroup, currentGroup)) {
                groupLabel = createGroupLabelForProgress(currentGroup);
                createTableRows(dc, chunkMembers, groupCounter);
                //set the current group as previous group
                System.arraycopy(currentGroup, 0, previousGroup, 0,
                        currentGroup.length);
                if (logUnusualCells && chunkMembers.size() > 1) {
                    //log unusual number of chunk members with the classes that
                    //cause the problem
                    if (LOGGER.isEnabledFor(LEVEL.INFO)) {
                        final StringBuilder buf = new StringBuilder();
                        buf.append("Data chunk with ");
                        buf.append(chunkMembers.size());
                        buf.append(" members occured in groupby node. "
                                + "Involved classes are: ");
                        final GroupKey key =
                            chunkMembers.keySet().iterator().next();
                        for (final DataCell cell : key.getGroupVals()) {
                            buf.append(cell.getClass().getCanonicalName());
                            buf.append(", ");
                        }
                        LOGGER.info(buf.toString());
                    }
                    logUnusualCells = false;
                }
                //reset the chunk members map
                chunkMembers.clear();
            }
            //process the row as one of the members of the current chunk
            Pair<ColumnAggregator[], Set<RowKey>> member =
                chunkMembers.get(new GroupKey(currentGroup));
            if (member == null) {
                Set<RowKey> rowKeys;
                if (isEnableHilite()) {
                    rowKeys = new HashSet<>();
                } else {
                    rowKeys = Collections.emptySet();
                }
                member = new Pair<>(cloneColumnAggregators(), rowKeys);
                final DataCell[] groupKeys = new DataCell[currentGroup.length];
                System.arraycopy(currentGroup, 0, groupKeys, 0,
                        currentGroup.length);
                chunkMembers.put(new GroupKey(groupKeys), member);
            }
            //compute the current row values
            for (final ColumnAggregator colAggr : member.getFirst()) {
                final int colIdx = origSpec.findColumnIndex(
                        colAggr.getOriginalColName());
                colAggr.getOperator(getGlobalSettings()).compute(row, colIdx);
            }
            if (isEnableHilite()) {
                member.getSecond().add(row.getKey());
            }
            groupExec.checkCanceled();
            groupExec.setProgress(++rowCounter/numOfRows, groupLabel);
        }
        //create the final row for the last chunk after processing the last
        //table row
        createTableRows(dc, chunkMembers, groupCounter);
        dc.close();
        return dc.getTable();
    }*/
}
