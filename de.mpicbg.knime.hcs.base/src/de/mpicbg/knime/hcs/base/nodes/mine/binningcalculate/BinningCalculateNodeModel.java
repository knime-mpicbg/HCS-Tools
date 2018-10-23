package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;


import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.TransformationDictionaryDocument.TransformationDictionary;
import org.knime.base.node.preproc.autobinner.pmml.DisretizeConfiguration;
import org.knime.base.node.preproc.autobinner.pmml.PMMLDiscretize;
import org.knime.base.node.preproc.autobinner.pmml.PMMLDiscretizeBin;
import org.knime.base.node.preproc.autobinner.pmml.PMMLInterval;
import org.knime.base.node.preproc.autobinner.pmml.PMMLInterval.Closure;
import org.knime.base.node.preproc.autobinner.pmml.PMMLPreprocDiscretize;
import org.knime.base.node.preproc.pmml.binner.BinnerColumnFactory.Bin;
import org.knime.base.node.preproc.pmml.binner.NumericBin;
import org.knime.base.node.preproc.pmml.binner.PMMLBinningTranslator;
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
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.node.port.pmml.preproc.DerivedFieldMapper;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

import de.mpicbg.knime.hcs.core.math.BinningAnalysis;
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
	
	//private PMMLPreprocPortObjectSpec m_pmmlOutSpec;

	// provides the row id for the output table
	//private int rowCount;

	/** Keeps index of the input port which is 0. *//*
	static final int DATA_INPORT = 0;

	*//** Keeps index of the optional model port which is 1. *//*
	static final int MODEL_INPORT = 1;

	*//** Keeps index of the output port which is 0. *//*
	static final int OUTPORT = 0;*/

	private final Map<String, Bin[]> m_columnToBins =
			new HashMap<String, Bin[]>();
			
	private Map<String, List<PMMLDiscretizeBin>>  m_binMap =
			new HashMap<String, List<PMMLDiscretizeBin>>();	
		
	private final Map<String, String> m_columnToAppended =
			new HashMap<String, String>();
	
	
	/**
	 * constructor
	 */
	protected BinningCalculateNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE, PMMLPortObject.TYPE}, true);

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
	 * @return settings model
	 */
	public static SettingsModelNumber createBinSelectionModel() {

		return new SettingsModelIntegerBounded(CFG_BIN, CFG_BIN_DFT, CFG_BIN_MIN, CFG_BIN_MAX);
	}


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
		
		double progress = 0.0;
		int countMissing;

		// will be filled with interval information and statistics
		BufferedDataContainer statisticDataContainer = exec.createDataContainer(createStatisticOutSpec(inData.getSpec(), nBins));

		// collects the values to be binned
		HashMap<Object, List<Double>> dataMap = new HashMap<Object, List<Double>>();
		int newRowIdx = 0;

		// Iterate for each parameter through the table and creating bins
		for (String col : selectedCols) {

			// One PMMLDiscretizeBin contains one bin for a single column - therefore we need a List of all bins
			//List<PMMLDiscretizeBin>  pmml_DicretizeBins =  new ArrayList<PMMLDiscretizeBin>();

			//delivers the index of the column to get the cell 
			int colIdx = inSpec.findColumnIndex(col);
			//String aggString = inSpec.getColumnSpec(colIdx).getName();

			// number of missing values for this parameter
			countMissing = 0;

			// parameter / aggregation string / values
			List<Double> allData = new ArrayList<Double>();

			for (DataRow row : inData) {

				DataCell valueCell = row.getCell(colIdx);
				Double value = null;
				// int cell represents numeric cell (can deliver int and double, double cell can be cast to int cell)
				if (!valueCell.isMissing()) value = ((DoubleValue) valueCell).getDoubleValue();
			
				if (valueCell.isMissing()) {
					countMissing++;
				}

				allData.add(value);
				
				// check whether execution was canceled
				exec.checkCanceled();
			}

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
			
			dataMap.put("ungrouped", allData);

			// do the binning analysis on the collected data
			BinningAnalysis binAnalysis = new BinningAnalysis(dataMap, nBins, col);
			LinkedList<Interval> bins = binAnalysis.getBins();
			List<PMMLDiscretizeBin> DiscretizeBins = discretizeBins(bins);
			m_binMap.put(col, DiscretizeBins);
			
			if(bins.size() < nBins) {
				setWarningMessage("\"" + col + "\": Less than " + nBins + " bins created. Input data lacks variability");
			}
			
			// fill binning data into the new table
			List<DataCell> cells = new ArrayList<DataCell>();

			// parameter name
			cells.add(new StringCell(col));
			// number of bins created
			cells.add(new IntCell(bins.size()));
			// bounds (interval cell) for each bin; missing cell if not available
			for(int i = 0; i < nBins; i++)
			{
				if(i < bins.size()) {
					cells.add(new IntervalCell(bins.get(i).getLowerBound(),bins.get(i).getUpperBound(), bins.get(i).checkModeLowerBound(), bins.get(i).checkModeUpperBound()));
				} else {
					cells.add(DataType.getMissingCell());
				}
			}

			DataRow currentRow = new DefaultRow(RowKey.createRowKey((long)newRowIdx), cells);
			statisticDataContainer.addRowToTable(currentRow);
			newRowIdx++;

			// set progress
			progress = progress + 1.0 / n;
			exec.setProgress(progress, "Binning done for parameter " + col + " (" + (newRowIdx + 1) + "/" + n + ")");
		}

		statisticDataContainer.close();

		// not in use
		PMMLPreprocDiscretize pmmlDiscretize = createDisretizeOp(m_binMap, selectedCols);
		//m_pmmlOutSpec = new PMMLDiscretizePreprocPortObjectSpec(pmmlDiscretize);
		//
		

		PMMLPortObject pmmlPortObject = translate(pmmlDiscretize, inSpec);
		//PMMLPortObject outPMMLPort = createPMMLModel(pmmlPortObject, inSpec, inData.getDataTableSpec());
		return new PortObject[]{statisticDataContainer.getTable(), pmmlPortObject};
	}





	/**
	 * generates the table specs for the ouput table
	 *
	 * @param inSpec
	 * @return new specs
	 */
	private DataTableSpec createStatisticOutSpec(DataTableSpec inSpec, int nBins) {

		DataColumnSpec[] columnArray = new DataColumnSpec[2 + nBins];
		DataColumnSpecCreator colCreator;

		colCreator = new DataColumnSpecCreator("Parameter", StringCell.TYPE);
		columnArray[0] = colCreator.createSpec();

		colCreator = new DataColumnSpecCreator("Number of bins", IntCell.TYPE);
		columnArray[1] = colCreator.createSpec();

		for(int i = 0; i < nBins; i++){
			double b = ((double)i + 1)* 1 / nBins;
			NumberFormat defaultFormat = NumberFormat.getPercentInstance();
			defaultFormat.setMinimumFractionDigits(0);
			String currentBin = defaultFormat.format(b);
			
			colCreator = new DataColumnSpecCreator(currentBin, IntervalCell.TYPE);
			columnArray[2 + i] = colCreator.createSpec();
		}


		return new DataTableSpec("Binning Summary", columnArray);  //To change body of created methods use File | Settings | File Templates.
	}



	private List<PMMLDiscretizeBin> discretizeBins(LinkedList<Interval> bins) 
	{

		// List of all discretize bins with information of label and interval
		List<PMMLDiscretizeBin> PMMLBins = new ArrayList<PMMLDiscretizeBin>();

		// counter for finding last bin
		int count = 0;    

		// Iterates through all bins
		for(Interval bin : bins	)
		{
			// List of PMML intervals
			List<PMMLInterval> PMMLIntervals = new ArrayList<PMMLInterval>();

			double lbound = bin.getLowerBound();
			double ubound = bin.getUpperBound();

			// finding the last bin and closing it - else using always closedOpen
			if(count == bins.size() - 1)
			{
				PMMLIntervals.add(new org.knime.base.node.preproc.autobinner.pmml.PMMLInterval(lbound, ubound,Closure.closedClosed));
			}
			else
			{
				PMMLIntervals.add(new org.knime.base.node.preproc.autobinner.pmml.PMMLInterval(lbound, ubound,Closure.closedOpen));
				count++;
			}

			PMMLBins.add(new PMMLDiscretizeBin(bin.getLabel(), PMMLIntervals));
		}

		return PMMLBins;
	}

	@SuppressWarnings("unused")
	private PMMLPreprocDiscretize createDisretizeOp(
			final Map<String, List<PMMLDiscretizeBin>> binMap, List<String> selectedCols) {

		List<String> names = new ArrayList<String>();
		Map<String, PMMLDiscretize> discretize =
				new HashMap<String, PMMLDiscretize>();
		for (String target : selectedCols) {

			names.add(target);
			discretize.put(target, new PMMLDiscretize(target,
					binMap.get(target)));
		}

		DisretizeConfiguration config = new DisretizeConfiguration(names,
				discretize);

		PMMLPreprocDiscretize op = new PMMLPreprocDiscretize(config);
		return op;
	}


	public static PMMLPortObject translate(final PMMLPreprocDiscretize pmmlDiscretize,
			final DataTableSpec dataTableSpec) {

		final Map<String, Bin[]> columnToBins = new HashMap<>();
		final Map<String, String> columnToAppend = new HashMap<>();

		List<String> replacedColumnNames = pmmlDiscretize.getConfiguration().getNames();

		for (String replacedColumnName : replacedColumnNames) {
			PMMLDiscretize discretize = pmmlDiscretize.getConfiguration().getDiscretize(replacedColumnName);
			List<PMMLDiscretizeBin> bins = discretize.getBins();
			String originalColumnName = discretize.getField();

			if (replacedColumnName.equals(originalColumnName)) { // wenn replaced, dann nicht anhängen
				columnToAppend.put(originalColumnName, null);
			} else { // nicht replaced -> anhängen
				columnToAppend.put(originalColumnName, replacedColumnName);
			}

			NumericBin[] numericBin = new NumericBin[bins.size()];
			int counter = 0;
			for (PMMLDiscretizeBin bin : bins) {
				String binName = bin.getBinValue();
				List<PMMLInterval> intervals = bin.getIntervals();
				boolean leftOpen = false;
				boolean rightOpen = false;
				double leftMargin = 0;
				double rightMargin = 0;
				//always returns only one interval
				for (PMMLInterval interval : intervals) {
					Closure closure = interval.getClosure();
					switch (closure) {
					case openClosed:
						leftOpen = true;
						rightOpen = false;
						break;
					case openOpen:
						leftOpen = true;
						rightOpen = true;
						break;
					case closedOpen:
						leftOpen = false;
						rightOpen = true;
						break;
					case closedClosed:
						leftOpen = false;
						rightOpen = false;
						break;
					default:
						leftOpen = true;
						rightOpen = false;
						break;
					}
					leftMargin = interval.getLeftMargin();
					rightMargin = interval.getRightMargin();
				}

				numericBin[counter] = new NumericBin(binName, leftOpen, leftMargin, rightOpen, rightMargin);
				counter++;

			}

			columnToBins.put(originalColumnName, numericBin);
		}

		// why should I create a new spec ???
        PMMLPortObjectSpecCreator pmmlSpecCreator = new PMMLPortObjectSpecCreator(dataTableSpec);
        PMMLPortObject pmmlPortObject = new PMMLPortObject(pmmlSpecCreator.createSpec(), null, dataTableSpec);
        PMMLBinningTranslator trans =
            new PMMLBinningTranslator(columnToBins, columnToAppend, new DerivedFieldMapper(pmmlPortObject));
        TransformationDictionary exportToTransDict = trans.exportToTransDict();
        pmmlPortObject.addGlobalTransformations(exportToTransDict);
        return pmmlPortObject;
	}

	/**
	 * Creates the pmml port object.
	 * @param the in-port pmml object. Can be <code>null</code> (optional in-port)
	 */
/*	private PMMLPortObject createPMMLModel(final PMMLPortObject outPMMLPort, final DataTableSpec inSpec, final DataTableSpec outSpec) {
		PMMLBinningTranslator trans =
				new PMMLBinningTranslator(m_columnToBins, m_columnToAppended, new DerivedFieldMapper(outPMMLPort));
		PMMLPortObjectSpecCreator pmmlSpecCreator = new PMMLPortObjectSpecCreator(outSpec);
		PMMLPortObject outputPMMLPort = new PMMLPortObject(pmmlSpecCreator.createSpec(), null, inSpec);
		outputPMMLPort.addGlobalTransformations(trans.exportToTransDict());
		return outputPMMLPort;
	}*/


	/*public static PMMLPortObject translate(final PMMLPreprocDiscretize pmmlDiscretize,
				        final DataTableSpec dataTableSpec) {

			 }*/

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