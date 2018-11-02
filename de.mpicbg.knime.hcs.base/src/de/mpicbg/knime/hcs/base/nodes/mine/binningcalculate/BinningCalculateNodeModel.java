package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

import de.mpicbg.knime.hcs.base.node.port.binning.BinningPortObject;
import de.mpicbg.knime.hcs.base.node.port.binning.BinningPortObjectSpec;
import de.mpicbg.knime.hcs.core.math.BinningAnalysis;
import de.mpicbg.knime.hcs.core.math.BinningAnalysisModel;
import de.mpicbg.knime.hcs.core.math.Interval;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of BinningCalculate.
 * 
 *
 * @author Tim Nicolaisen, Antje Janosch
 */
public class BinningCalculateNodeModel extends AbstractNodeModel {


	// Configuration for Bins
	public static final String CFG_BIN = "nBins";
	private static final Integer CFG_BIN_DFT = 5;
	private static final Integer CFG_BIN_MIN = 2;
	private static final Integer CFG_BIN_MAX = Integer.MAX_VALUE;
	
	// Configuration for selected Columns to bin
	public static final String CFG_COLUMN = "selectedCols";
	
	
	/**
	 * constructor
	 */
	protected BinningCalculateNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE, BinningPortObject.TYPE}, true);

		initializeSettings();
	}

	/**
	 * init model settings	
	 */
	private void initializeSettings() 
	{
		this.addModelSetting(CFG_BIN, createBinSelectionModel());
		this.addModelSetting(CFG_COLUMN, createColumnFilterModel());
	}
	
	/**
	 * settings model for number of bins
	 * 
	 * @return {@link SettingsModelIntegerBounded}
	 */
	public static SettingsModelIntegerBounded createBinSelectionModel() {

		return new SettingsModelIntegerBounded(CFG_BIN, CFG_BIN_DFT, CFG_BIN_MIN, CFG_BIN_MAX);
	}

	/**
	 * settings model for selected columns
	 * @return {@link SettingsModelColumnFilter2}
	 */
	@SuppressWarnings("unchecked")
	public static SettingsModelColumnFilter2 createColumnFilterModel() {
        return new SettingsModelColumnFilter2(CFG_COLUMN, DoubleValue.class);
    }		

	@Override
	protected PortObject[] execute(final PortObject[] inPorts,
			final ExecutionContext exec) throws Exception {

		BufferedDataTable inData = (BufferedDataTable)inPorts[0];
		DataTableSpec inSpec = inData.getDataTableSpec();

		// retrieve model settings
		int nBins = ((SettingsModelIntegerBounded) getModelSetting(CFG_BIN)).getIntValue();

		FilterResult filterResult = ((SettingsModelColumnFilter2)this.getModelSetting(CFG_COLUMN)).applyTo(inSpec);
		List<String> selectedCols = Arrays.asList(filterResult.getIncludes());

		// number of parameters tp process
		int n = selectedCols.size();
		// number of rows in data table
		long nrows = inData.size();
		// current progress
		double progress = 0.0;

		// will be filled with interval information and statistics
		BufferedDataContainer statisticDataContainer = exec.createDataContainer(createStatisticOutSpec(inData.getSpec(), nBins));
		
		// collect intervals for port creation
		HashMap<String, LinkedList<Interval>> binningMap = new LinkedHashMap<String, LinkedList<Interval>>();
		
		int newRowIdx = 0;

		// Iterate for each parameter through the table and creating bins
		// current column
		int currentCol = 1;
		for (String col : selectedCols) {

			//delivers the index of the column to get the cell 
			int colIdx = inSpec.findColumnIndex(col);

			// number of missing values
			int countMissing = 0;

			// parameter / aggregation string / values
			List<Double> allData = new ArrayList<Double>();
			
			exec.setMessage("Binning for parameter " + col + " (" + currentCol + "/" + n + ")");
			ExecutionMonitor execParam = exec.createSubProgress(1.0/n);

			// current row
			int currentRowIdx = 1;
			for (DataRow row : inData) {

				DataCell valueCell = row.getCell(colIdx);
				Double value = null;
				// int cell represents numeric cell (can deliver int and double, double cell can be cast to int cell)
				if (!valueCell.isMissing()) {
					value = ((DoubleValue) valueCell).getDoubleValue();
					allData.add(value);
				} else {
					countMissing++;
				}

				// check whether execution was canceled
				exec.checkCanceled();
				// set progress
				progress = ((double)currentRowIdx/nrows);
				execParam.setProgress(progress, "Reading row: " + currentRowIdx + "/" + nrows + ")");
				currentRowIdx++;
			}
			
			execParam.setMessage("Creating bins");

			// warning if the reference label does not contain any data (domain available though no data in the table
			// don't calculate the binning for this parameter
			if (allData.isEmpty()) {
				setWarningMessage("there is no reference data available for \"" + col + "\"");
				continue;
			}

			// warning if missing values were filtered from the column
			if (countMissing > 0) {
				this.logger.info(col + ": " + countMissing + " values were skipped because of missing values");
			}
			
			// collects the values to be binned
			HashMap<Object, List<Double>> dataMap = new HashMap<Object, List<Double>>();
			dataMap.put("ungrouped", allData);

			// do the binning analysis on the collected data
			BinningAnalysis binAnalysis = new BinningAnalysis(dataMap, nBins, col);
			LinkedList<Interval> bins = binAnalysis.getBins();
			if(!bins.isEmpty()) {
				binningMap.put(col, bins);
			}
			
			if(bins.size() < nBins) {
				setWarningMessage("\"" + col + "\": Less than " + nBins + " bins created. Input data lacks variability");
			}
			
			// create a hashmap to allow missing bins for certain percentile
			Map<String, Interval> intervalMap = new HashMap<String, Interval>();
			String[] percentiles = binAnalysis.getPercentileLabels();
			for(String label : percentiles)
			{
				Interval foundIv = null;
				for(int i = 0; i < bins.size(); i++) {
					Interval iv = bins.get(i);
					if(iv.getLabel().equals(label))
						foundIv = iv;
				}
				intervalMap.put(label, foundIv);
			}

			// create statistics table content
			//for(int i = 0; i < nBins; i++)
			for(String label : percentiles)
			{
				Interval currentIv = intervalMap.get(label);
				// fill binning data into the new table
				List<DataCell> statisticCells = new ArrayList<DataCell>();
				
				// parameter name
				statisticCells.add(new StringCell(col));
				// number of bins created
				statisticCells.add(new IntCell(bins.size()));
				// interval label
				statisticCells.add(new StringCell(label));
				
				// bounds (interval cell) for each bin; missing cell if not available
				if(currentIv != null) {
					statisticCells.add(new IntervalCell(currentIv.getLowerBound(),currentIv.getUpperBound(), currentIv.checkModeLowerBound(), currentIv.checkModeUpperBound()));
				} else {
					statisticCells.add(DataType.getMissingCell());
				}
				
				DataRow currentRow = new DefaultRow(RowKey.createRowKey((long)newRowIdx), statisticCells);
				statisticDataContainer.addRowToTable(currentRow);
				newRowIdx++;
			}
			currentCol++;
		}

		statisticDataContainer.close();

		BinningAnalysisModel model = new BinningAnalysisModel(selectedCols, nBins, binningMap);
		BinningPortObject bpo = new BinningPortObject(model);

		return new PortObject[] {statisticDataContainer.getTable(), bpo};
	}
	


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		DataTableSpec inSpec = (DataTableSpec) inSpecs[0];

		// check if numeric columns are available at all
		if (!inSpec.containsCompatibleType(DoubleValue.class))
            throw new InvalidSettingsException("input table requires at least one numeric column (Double or Integer)");   
		
		// number of bins value controlled by SettingsModel
		int nBins = ((SettingsModelIntegerBounded) getModelSetting(CFG_BIN)).getIntValue();
		
		// check number of selected columns
		FilterResult filterResult = ((SettingsModelColumnFilter2)this.getModelSetting(CFG_COLUMN)).applyTo(inSpec);
		List<String> selectedCols = Arrays.asList(filterResult.getIncludes());
		if(selectedCols.isEmpty()) {
			this.setWarningMessage("No column has been selected for processing");
		} else {
			// check if column selection settings are still fully applicable to input table
			String[] inclColumns = filterResult.getRemovedFromIncludes();
			if(inclColumns.length > 0 ) {
				String missingInclColumns = String.join(", ", inclColumns);
				this.setWarningMessage("The following included columns are not available anymore: " + missingInclColumns);
			}
			// do not give message for missing excluded columns as its not important
		}
		
		BinningPortObjectSpec bpSpec = new BinningPortObjectSpec(selectedCols.toArray(new String[selectedCols.size()]));

		return new PortObjectSpec[]{(PortObjectSpec)createStatisticOutSpec(inSpec, nBins), bpSpec};
	}


	/**
	 * generates the table specs for the ouput table
	 *
	 * @param inSpec
	 * @return new {@link DataTableSpec}
	 */
	private DataTableSpec createStatisticOutSpec(DataTableSpec inSpec, int nBins) {

		DataColumnSpec[] columnArray = new DataColumnSpec[4];
		DataColumnSpecCreator colCreator;

		colCreator = new DataColumnSpecCreator("Parameter", StringCell.TYPE);
		columnArray[0] = colCreator.createSpec();

		colCreator = new DataColumnSpecCreator("Number of bins created", IntCell.TYPE);
		columnArray[1] = colCreator.createSpec();
		
		colCreator = new DataColumnSpecCreator("Interval", StringCell.TYPE);
		columnArray[2] = colCreator.createSpec();
		
		colCreator = new DataColumnSpecCreator("Bounds", IntervalCell.TYPE);
		columnArray[3] = colCreator.createSpec();
		
		return new DataTableSpec("Binning Summary", columnArray);
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
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// check that at least one numeric column has been selected
		if (settings.containsKey(CFG_COLUMN)) {
			String[] selectedColumns = settings.getNodeSettings(CFG_COLUMN).getStringArray("included_names");
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