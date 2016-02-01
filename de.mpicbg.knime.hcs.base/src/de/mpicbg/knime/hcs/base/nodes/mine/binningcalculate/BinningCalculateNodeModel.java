package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.knime.base.node.preproc.pmml.binner.PMMLBinningTranslator;
import org.knime.base.node.preproc.pmml.binner.BinnerColumnFactory.Bin;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.IntervalCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.node.port.pmml.preproc.DerivedFieldMapper;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.math.BinningAnalysis;
import de.mpicbg.knime.hcs.core.math.BinningData;
import de.mpicbg.knime.hcs.core.math.Interval;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of BinningCalculate.
 * 
 *
 * @author 
 */

public class BinningCalculateNodeModel extends AbstractNodeModel {

	
	private static final String CFG_AGGR_DFT = TdsUtils.SCREEN_MODEL_WELL;
	public static final String CFG_AGGR = "groupBy";
	
	
	// Configuration for Bins
	public static final String CFG_BIN = "nBins";
	private static final Integer CFG_BIN_DFT = 5;
	private static final Integer CFG_BIN_MIN = 2;
	private static final Integer CFG_BIN_MAX = Integer.MAX_VALUE;

	// Configuration for selected Columns to bin
	public static final String CFG_REFCOLUMN = "refCol";

	public static final String CFG_REFSTRING = "refString";

	// provides the row id for the output table
	private int rowCount;

	/** Keeps index of the input port which is 0. */
	static final int DATA_INPORT = 0;

	/** Keeps index of the optional model port which is 1. */
	static final int MODEL_INPORT = 1;

	/** Keeps index of the output port which is 0. */
	static final int OUTPORT = 0;
	
	public static final String CFG_COLUMN = "selectedCols";
	


	private final Map<String, Bin[]> m_columnToBins =
			new HashMap<String, Bin[]>();

	private final Map<String, String> m_columnToAppended =
			new HashMap<String, String>();

	
	
	
	
	static final String COLUMN_NAMES = "GroupColNames";
	
	private final SettingsModelColumnFilter2 m_filterGroupColModel =
	        BinningCalculateNodeDialog.getFilterDoubleColModel();
	
	
	protected BinningCalculateNodeModel() {
			super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE, PMMLPortObject.TYPE}, true);
			
			initializeSettings();
			}
	
	/**
	 * selection model for numeric columns
	 *
	 * @return selection model
	 
	public final SettingsModelColumnFilter2 CFG_aggregationCols = createAggregationColsModel();
	
	private final SettingsModelBoolean m_removeRetainedCols = createRemoveRetainedColsModel();

    private final SettingsModelBoolean m_removeAggregationCols =createRemoveAggregationColsModel();
    
	*/
	

	/**
	 * selection model for number of bins
	 *
	 * @return selection model
	 */
	public static SettingsModelNumber createBinSelectionModel() {

		return new SettingsModelIntegerBounded(CFG_BIN, CFG_BIN_DFT, CFG_BIN_MIN, CFG_BIN_MAX);
	}

	/**
	 * selection model for reference column
	 *
	 * @return selection model
	 */
	public static SettingsModelString createRefColumnSelectionModel() {

		return new SettingsModelString(CFG_REFCOLUMN, null);
	}

	/**
	 * selection model for reference label
	 *
	 * @return selection model
	 */
	public static SettingsModelString createRefStringSelectionModel() {

		return new SettingsModelString(CFG_REFSTRING, null);
	}

	/**
	 * selection model for aggregating object based data
	 *
	 * @return selection model
	 */
	public static SettingsModelString createAggregationSelectionModel() {

		return new SettingsModelString(CFG_AGGR, CFG_AGGR_DFT);
	}






	private void initializeSettings() {
		this.addModelSetting(CFG_AGGR, createAggregationSelectionModel());
		this.addModelSetting(CFG_BIN, createBinSelectionModel());
		this.addModelSetting(CFG_REFCOLUMN, createRefColumnSelectionModel());
		this.addModelSetting(CFG_REFSTRING, createRefStringSelectionModel());
		
		
		
	}

	static SettingsModelColumnFilter2 createAggregationColsModel() {
        return new SettingsModelColumnFilter2("aggregationColumns");
    }
	
	/**
     * @return the remove aggregation column model
     */
    static SettingsModelBoolean createRemoveAggregationColsModel() {
        return new SettingsModelBoolean("removeAggregationColumns", false);
    }

    /**
     * @return the remove aggregation column model
     */
    static SettingsModelBoolean createRemoveRetainedColsModel() {
        return new SettingsModelBoolean("removeRetainedColumns", false);
    }

	
	@Override
	protected PortObject[] execute(final PortObject[] inPorts,
			final ExecutionContext exec) throws Exception {
		
		BufferedDataTable inData = (BufferedDataTable)inPorts[DATA_INPORT];
		DataTableSpec inSpec = inData.getDataTableSpec();

		

		/**
		 * 
		 *			Reading in all Settings 
		 * 
		 */
		
		int nBins = ((SettingsModelIntegerBounded) getModelSetting(CFG_BIN)).getIntValue();
		
		
		FilterResult filterResult = m_filterGroupColModel.applyTo(inSpec);
			    List<String> selectedCols = Arrays.asList(filterResult.getIncludes());
		
        
      
        boolean hasRefColumn = true;
        
        String refColumn = ((SettingsModelString) getModelSetting(CFG_REFCOLUMN)).getStringValue();
        	// Checking if RefColumn is selected
        	if (refColumn == null)
        		{
        			hasRefColumn = false;
        		}
        String refString = null;
        
        if (hasRefColumn)
        	{
        		refString = ((SettingsModelString) getModelSetting(CFG_REFSTRING)).getStringValue();
        	}


		/**
		 * 
		 *			IMPORTANT - the aggIdx is set to 6 (well) - this needs to be changed
		 * 
		 */
        
       //int aggIdx = inSpec.findColumnIndex(aggColumn);
        int aggIdx = 6;
        int refIdx = -1;
        if (hasRefColumn) refIdx = inSpec.findColumnIndex(refColumn);

        List<Double> doubleList;

       
        int i = 1;
        int n = selectedCols.size();
        double progress = 0.0;
        int countMissing;
        
        BufferedDataContainer StatisticDataContainer = exec.createDataContainer(createStatisticOutSpec(inData.getSpec()));
        rowCount = 0;
        
        

		/**
		 * 
		 *			Iterate for each parameter through the table and creating bins
		 * 
		 */
        
        // gets the selected Columns in the dialog on by one
        for (String col : selectedCols) {
        	
        	//delivers the index of the column to get the cell 
            int colIdx = inSpec.findColumnIndex(col);
            
            // I DONT KNOW
            countMissing = 0;

            // parameter / aggregation string / values
            HashMap<Object, List<Double>> refData = new HashMap<Object, List<Double>>();
            HashMap<Object, List<Double>> allData = new HashMap<Object, List<Double>>();

           
            
            for (DataRow row : inData) {

            	
                DataCell aggCell = row.getCell(aggIdx);
                
                //System.out.println(rowCount);
                
                String aggString = null;
                if (!aggCell.isMissing()) aggString = ((StringCell) aggCell).getStringValue();

                String label = null;
                if (hasRefColumn) {
                    DataCell labelCell = row.getCell(refIdx);
                    if (!labelCell.isMissing()) label = ((StringCell) labelCell).getStringValue();
                }

                DataCell valueCell = row.getCell(colIdx);
                Double value = null;
                // int cell represents numeric cell (can deliver int and double, double cell can be cast to int cell)
                if (!valueCell.isMissing()) value = ((DoubleValue) valueCell).getDoubleValue();

                // skip data row if
                // - aggregation information is not available
                // - data value is missing
                // - reference column was selected but reference label is not available
                if (aggString == null || value == null || (hasRefColumn && label == null)) {
                    countMissing++;
                    continue;
                }

                // fill list of reference data
                if (hasRefColumn) {

                    if (label.equals(refString)) {

                        if (refData.containsKey(aggString)) doubleList = refData.get(aggString);
                        else doubleList = new ArrayList<Double>();

                        doubleList.add(value);
                        refData.put(aggString, doubleList);
                    }
                }

                // fill list with all data
                if (allData.containsKey(aggString)) doubleList = allData.get(aggString);
                else doubleList = new ArrayList<Double>();

                doubleList.add(value);
                allData.put(aggString, doubleList);
                rowCount++;
                // check whether execution was canceled
                exec.checkCanceled();
            }

            // if there is no reference column selected, take the whole dataset as reference
            if (!hasRefColumn) {
                refData = allData;
            }
            // warning if the reference label does not contain any data (domain available though no data in the table
            // don't calculate the binning for this parameter
            if (refData.isEmpty()) {
                setWarningMessage("there is no reference data available for " + col);
                continue;
            }

            // warning if missing values were filtered from the column
            if (countMissing > 0) {
                this.logger.info(col + ": " + countMissing + " values were skipped because of missing values");
                //setWarningMessage( col + ": " + countMissing + " rows were skipped because of missing values");
            }

            // do the binning analysis on the collected data
            BinningAnalysis binAnalysis = new BinningAnalysis(refData, nBins, col);
            LinkedList<Interval> bins = binAnalysis.getBins();
            //HashMap<Object, List<BinningData>> ret = binAnalysis.getZscore(allData);

            // fill binning data into the new table
            
            
            List<DataCell> cells = new ArrayList<DataCell>();

           
            
          
            if(bins.size() > 0){
            	 cells.add(new StringCell(col));
                 cells.add(new StringCell("5"));
                 
            	for(i= 0; i < (bins.size() - 1); i++)
            	{
            		cells.add(new IntervalCell(bins.get(i).getLowerBound(),bins.get(i).getUpperBound(), bins.get(i).checkModeLowerBound(), bins.get(i).checkModeUpperBound()));

            	}
            	
            	if((bins.size()-1) < nBins)
                {
                	for(i= 0; i < (nBins - (bins.size() -  1)); i++){
                		cells.add(DataType.getMissingCell());
                	}
                }
            	
            	
            }
            else{continue;}
        
            
         
            DataRow currentRow = new DefaultRow(new RowKey(Integer.toString(rowCount)), cells);
            StatisticDataContainer.addRowToTable(currentRow);
            
            
            // set progress
            progress = progress + 1.0 / n;
            i++;
            exec.setProgress(progress, "Binning done for parameter " + col + " (" + i + "/" + n + ")");
       
        }

        StatisticDataContainer.close();
        

		PMMLPortObject outPMMLPort = createPMMLModel(null, inSpec, inData.getDataTableSpec());
		return new PortObject[]{StatisticDataContainer.getTable(), outPMMLPort};
	}

	
	
	
	
	/**
     * generates the table specs for the ouput table
     *
     * @param inSpec
     * @return new specs
     */
    private DataTableSpec createStatisticOutSpec(DataTableSpec inSpec) {
    	
    	int nBins = ((SettingsModelIntegerBounded) getModelSetting(CFG_BIN)).getIntValue();
        DataColumnSpec[] columnArray = new DataColumnSpec[2 + nBins];

        DataColumnSpecCreator colCreator;

        colCreator = new DataColumnSpecCreator("parameter", StringCell.TYPE);
        columnArray[0] = colCreator.createSpec();
        
        colCreator = new DataColumnSpecCreator("bin_total", StringCell.TYPE);
        columnArray[1] = colCreator.createSpec();
        
        for(int i = 0; i < nBins; i++){
        	  colCreator = new DataColumnSpecCreator("bin_" + (i + 1), IntervalCell.TYPE);
              columnArray[2 + i] = colCreator.createSpec();
        }
      

        return new DataTableSpec("Binning Specs", columnArray);  //To change body of created methods use File | Settings | File Templates.
    }

	
    private DataRow[] createDataRow(String col, String aggLabel, List<BinningData> binningDatas) {
        DataRow[] newRows = new DataRow[binningDatas.size()];

        int i = 0;
        for (BinningData binData : binningDatas) {
            List<DataCell> cells = new ArrayList<DataCell>();

            cells.add(new StringCell(col));
            cells.add(new StringCell(aggLabel));
            cells.add(new StringCell(binData.getInterval().getLabel()));
            cells.add(new DoubleCell(binData.getPercentage()));
            double zval = binData.getZscore();
            if (Double.isNaN(zval)) cells.add(DataType.getMissingCell());
            else cells.add(new DoubleCell(zval));
            cells.add(new IntCell((int) binData.getCount()));

            DataRow currentRow = new DefaultRow(new RowKey(Integer.toString(rowCount)), cells);
            newRows[i] = currentRow;
            i++;
            rowCount++;
        }
        return newRows;
    }
	
	
	
	
	
			/**
			 * Creates the pmml port object.
			 * @param the in-port pmml object. Can be <code>null</code> (optional in-port)
			 */
			private PMMLPortObject createPMMLModel(final PMMLPortObject outPMMLPort, final DataTableSpec inSpec, final DataTableSpec outSpec) {
				PMMLBinningTranslator trans =
						new PMMLBinningTranslator(m_columnToBins, m_columnToAppended, new DerivedFieldMapper(outPMMLPort));
				PMMLPortObjectSpecCreator pmmlSpecCreator = new PMMLPortObjectSpecCreator(outSpec);
				PMMLPortObject outputPMMLPort = new PMMLPortObject(pmmlSpecCreator.createSpec(), null, inSpec);
				outputPMMLPort.addGlobalTransformations(trans.exportToTransDict());
				return outputPMMLPort;
			}


			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void reset() {
				// TODO: generated method stub
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
					throws InvalidSettingsException {



				// TODO: generated method stub
				return new DataTableSpec[]{null};
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void saveSettingsTo(final NodeSettingsWO settings) {
				// TODO: generated method stub
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
					throws InvalidSettingsException {
				// TODO: generated method stub
			}

			@Override
			protected void validateSettings(final NodeSettingsRO settings)
					throws InvalidSettingsException {
				// check that at least one numeric column has been selected
				if (settings.containsKey(CFG_COLUMN)) {
					String[] selectedColumns = settings.getNodeSettings(CFG_COLUMN).getStringArray("InclList");
					if (selectedColumns.length < 1)
						throw new InvalidSettingsException("at least one numeric column has to be selected");
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void loadInternals(final File internDir,
					final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
				// TODO: generated method stub
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void saveInternals(final File internDir,
					final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
				// no internals to save
			}

			
}