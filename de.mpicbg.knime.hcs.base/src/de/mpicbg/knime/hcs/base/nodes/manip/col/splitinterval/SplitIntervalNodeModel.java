package de.mpicbg.knime.hcs.base.nodes.manip.col.splitinterval;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.IntervalValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;

import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * Node model for Split Interval node
 * 
 * @author Antje Janosch
 *
 */
public class SplitIntervalNodeModel extends AbstractNodeModel {
	
	// ================= NODE SETTINGS + DEFAULTS ======================
	
	public static String CFG_IV_COLUMN = "interval.column";
	
	public static String CFG_INCL_MODE = "include.mode";
	public static boolean CFG_INCL_MODE_DFT = true;
	
	// =================================================================
	
	/**
	 * constructor
	 */
	public SplitIntervalNodeModel() {
		super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	/**
	 * add model settings
	 */
	private void initializeSettings() {
		this.addModelSetting(CFG_IV_COLUMN, createIntervalColumnnModel());
		this.addModelSetting(CFG_INCL_MODE, createIncludeModeModel());
	}


	/**
	 * create node model setting for flag if incl/excl columns shall be created
	 * @return {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createIncludeModeModel() {
		return new SettingsModelBoolean(CFG_INCL_MODE, CFG_INCL_MODE_DFT);
	}

	/**
	 * create node model setting for selection of interval column
	 * @return {@link SettingsModelString}
	 */
	public static SettingsModelString createIntervalColumnnModel() {
		return new SettingsModelString(CFG_IV_COLUMN, null);
	}

	/**
	 * {|{@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		DataTableSpec inSpec = inSpecs[0];
		
		String columnAutoGuessed = null;
		
		// is there any column oftype interval? if yes, store its name for autoguessing
		for(DataColumnSpec cSpec : inSpec) {
			if(cSpec.getType().isCompatible(IntervalValue.class)) {
				columnAutoGuessed = cSpec.getName();
				continue;
			}
		}
		
		// no Interval column found
		if(columnAutoGuessed == null)
			throw new InvalidSettingsException("Input table has no column of type interval");
		
		// get selected column from settings
		SettingsModelString sm_selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_IV_COLUMN));
		String selectedColumn = sm_selectedColumn.getStringValue();
		
		// no column selected yet => autoguess
		if(selectedColumn == null) {
			this.updateModelSetting(CFG_IV_COLUMN, new SettingsModelString(CFG_IV_COLUMN, columnAutoGuessed));
			selectedColumn = columnAutoGuessed;
			this.setWarningMessage("Auto-guessed settings, interval column \""
                    + selectedColumn + "\"");
		} else {
			checkColumnsForAvailability(inSpec, new String[]{selectedColumn}, IntervalValue.class, false, true);			
		}
		
		boolean createModeColumn = ((SettingsModelBoolean) this.getModelSetting(CFG_INCL_MODE)).getBooleanValue();
		
		ColumnRearranger cRearrange = new ColumnRearranger(inSpec);
		cRearrange.append(new SplitIntervalCellFactory(selectedColumn, inSpec.findColumnIndex(selectedColumn), createModeColumn));
		
		return new DataTableSpec[] {cRearrange.createSpec()};
	}
		
	/**
	 * {|{@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		
		String selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_IV_COLUMN)).getStringValue();
		boolean createModeColumn = ((SettingsModelBoolean) this.getModelSetting(CFG_INCL_MODE)).getBooleanValue();
		
		DataTableSpec inSpec = inData[0].getDataTableSpec();
		
		ColumnRearranger cRearrange = new ColumnRearranger(inSpec);
		cRearrange.append(new SplitIntervalCellFactory(selectedColumn, inSpec.findColumnIndex(selectedColumn), createModeColumn));
		BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], cRearrange, exec);
		return new BufferedDataTable[]{out};
	}
	
	

}
