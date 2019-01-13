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
	
	public static String CFG_IV_COLUMN = "interval.column";
	
	public static String CFG_INCL_MODE = "include.mode";
	public static boolean CFG_INCL_MODE_DFT = true;
	
	public SplitIntervalNodeModel() {
		super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	private void initializeSettings() {
		this.addModelSetting(CFG_IV_COLUMN, createIntervalColumnnModel());
		this.addModelSetting(CFG_INCL_MODE, createIncludeModeModel());
	}

	public static SettingsModelBoolean createIncludeModeModel() {
		return new SettingsModelBoolean(CFG_INCL_MODE, CFG_INCL_MODE_DFT);
	}

	public static SettingsModelString createIntervalColumnnModel() {
		return new SettingsModelString(CFG_IV_COLUMN, null);
	}

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
		String selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_IV_COLUMN)).getStringValue();
		
		// no column selected yet => autoguess
		if(selectedColumn == null) {
			
		} else {
			checkColumnsForAvailability(inSpec, new String[]{selectedColumn}, IntervalValue.class, false, true);			
		}
		
		boolean createModeColumn = ((SettingsModelBoolean) this.getModelSetting(CFG_INCL_MODE)).getBooleanValue();
		
		ColumnRearranger cRearrange = new ColumnRearranger(inSpec);
		cRearrange.append(new SplitIntervalCellFactory(selectedColumn, inSpec.findColumnIndex(selectedColumn), createModeColumn));
		
		return new DataTableSpec[] {cRearrange.createSpec()};
	}
	
	

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
