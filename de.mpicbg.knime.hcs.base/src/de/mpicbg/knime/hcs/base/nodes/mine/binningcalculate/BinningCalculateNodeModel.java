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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

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
         initializeSettings();
    
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