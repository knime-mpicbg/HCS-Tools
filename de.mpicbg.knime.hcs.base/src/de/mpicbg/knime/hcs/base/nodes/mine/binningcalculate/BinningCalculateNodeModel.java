package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;


import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
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

    /**
     * Constructor for the node model.
     */
    protected BinningCalculateNodeModel() {
	// TODO: Specify the amount of input and output ports needed.
	super(1, 3, true);
    
    }

    private void initializeSettings() {
	this.addModelSetting(CFG_COLUMN, createColumnSelectionModel());
	this.addModelSetting(CFG_BIN, createBinSelectionModel());
	this.addModelSetting(CFG_REFCOLUMN, createRefColumnSelectionModel());
	this.addModelSetting(CFG_REFSTRING, createFormateColumn());
	this.addModelSetting(CFG_AGGR, createAggregationSelectionModel());
    }


    public final SettingsModelFilterString createColumnSelectionModel() {
	return new SettingsModelFilterString(CFG_COLUMN);
    }

    static final SettingsModelNumber createBinSelectionModel() {
	return new SettingsModelIntegerBounded(CFG_BIN, CFG_BIN_DFT, CFG_BIN_MIN, CFG_BIN_MAX);
    }

    static final SettingsModelBoolean createRefColumnSelectionModel() {
	return new SettingsModelBoolean(CFG_REFCOLUMN, false);
    }

    static final SettingsModelBoolean createFormateColumn() {
	return new SettingsModelBoolean(CFG_REFSTRING, false);
    }
    static final SettingsModelBoolean createAggregationSelectionModel() {
	return new SettingsModelBoolean(CFG_AGGR, false);
    }

    

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
	    final ExecutionContext exec) throws Exception {

	// TODO: Return a BufferedDataTable for each output port 
	return new BufferedDataTable[]{};
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
	    throws InvalidSettingsException {
	// TODO: generated method stub
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
	// TODO: generated method stub
    }

}