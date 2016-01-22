package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.base.node.preproc.pmml.binner.PMMLBinningTranslator;
import org.knime.base.node.preproc.pmml.binner.BinnerColumnFactory.Bin;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of BinningCalculate.
 * 
 *
 * @author 
 */

public class BinningCalculateNodeModel extends AbstractNodeModel {

	public static final String CFG_AGGR = "groupBy";
	private static final String CFG_AGGR_DFT = TdsUtils.SCREEN_MODEL_WELL;
	//private static final String CFG_AGGR_DFT = "well";

	public static final String CFG_COLUMN = "selectedCols";

	public static final String CFG_BIN = "nBins";
	private static final Integer CFG_BIN_DFT = 5;
	private static final Integer CFG_BIN_MIN = 2;
	private static final Integer CFG_BIN_MAX = Integer.MAX_VALUE;

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


	private final Map<String, Bin[]> m_columnToBins =
			new HashMap<String, Bin[]>();

			private final Map<String, String> m_columnToAppended =
					new HashMap<String, String>();




			/** Creates a new binner.
			 * @param pmmlInEnabled
			 * @param pmmlOutEnabled */
			protected BinningCalculateNodeModel() {
				super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE, PMMLPortObject.TYPE});

			}


			private void initializeSettings() {
				this.addModelSetting(CFG_COLUMN, createColumnSelectionModel());
				this.addModelSetting(CFG_BIN, createBinSelectionModel());
				this.addModelSetting(CFG_REFCOLUMN, createRefColumnSelectionModel());
				this.addModelSetting(CFG_REFSTRING, createRefStringSelectionModel());
				this.addModelSetting(CFG_AGGR, createAggregationSelectionModel());
			}


			/**
			 * {@inheritDoc}
			 */
			@Override
			protected PortObject[] execute(final PortObject[] inPorts,
					final ExecutionContext exec) throws Exception {
				BufferedDataTable inData = (BufferedDataTable)inPorts[DATA_INPORT];
				DataTableSpec spec = inData.getDataTableSpec();


				PMMLPortObject outPMMLPort = createPMMLModel(null, spec, inData.getDataTableSpec());
				return new PortObject[]{inData, outPMMLPort};
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

			/**
			 * selection model for numeric columns
			 *
			 * @return selection model
			 */
			public static SettingsModelFilterString createColumnSelectionModel() {

				return new SettingsModelFilterString(CFG_COLUMN);
			}

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




}