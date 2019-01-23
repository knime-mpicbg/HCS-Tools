package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.apache.commons.lang3.EnumUtils;
import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import de.mpicbg.knime.hcs.core.math.Interval.Mode;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * node model class for Create Interval node
 * 
 * @author Antje Janosch
 *
 */
public class CreateIntervalNodeModel extends AbstractNodeModel {

	public static final String CFG_KEY = "create.interval.settings";
	
	public CreateIntervalNodeModel() {	
		super(1,1,true);
		this.addModelSetting(CFG_KEY, new CreateIntervalNodeSettings(CFG_KEY));
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		DataTableSpec inSpec = inSpecs[0];
		
		String autoLeftBound = null;
		String autoRightBound = null;
		// mode columns should have no need of autoguessing as by default they are not required
		
		/* for autoguessing bounds, prefer double or int columns for the bounds */
		for(DataColumnSpec cSpec : inSpec) {
			DataType dType = cSpec.getType();
			if(dType.equals(DoubleCell.TYPE) || dType.equals(IntCell.TYPE)) {
				if(autoLeftBound == null) autoLeftBound = cSpec.getName();
				if(autoRightBound == null) autoRightBound = cSpec.getName();
				continue;
			}
		}
			
		// try to autoguess mode columns (boolean) and bounds columns if not yet set
		// with any other numeric type
		for(DataColumnSpec cSpec : inSpec) {
			DataType dType = cSpec.getType();
			if(autoLeftBound == null&& dType.isCompatible(DoubleValue.class) ) {
				if(autoLeftBound == null) autoLeftBound = cSpec.getName();
				if(autoRightBound == null) autoRightBound = cSpec.getName();
			}

		}
		
		// no numeric column found (required at least for bounds)
		if(autoLeftBound == null)
			throw new InvalidSettingsException("Input table has no numeric column");
		
		CreateIntervalNodeSettings settings = (CreateIntervalNodeSettings) this.getModelSetting(CFG_KEY);
		
		// apply autoguessing if no value set
		boolean settingsNeedUpdate = false;
		if(settings.getLeftBoundColumn() == null) {
			settings.setLeftBoundColumn(autoLeftBound);
			settingsNeedUpdate = true;
		}
		if(settings.getRightBoundColumn() == null) {
			settings.setRightBoundColumn(autoRightBound);
			settingsNeedUpdate = true;
		}
		if(settingsNeedUpdate) {
			updateModelSetting(CFG_KEY, settings);
			setWarningMessage("Applied autoguessing for bound columns: Please check the configuration");
		}
		
		String leftBoundColumn = settings.getLeftBoundColumn();
		String rightBoundColumn = settings.getRightBoundColumn();
		String leftModeColumn = settings.getLeftModeColumn();
		String rightModeColumn = settings.getRightModeColumn();
		String fixedMode = settings.getFixedMode();
		String outColumnName = settings.getOutColumnName();
		
		// check availability and data type for bound and mode ciolumns (if required)
		checkColumnsForAvailability(inSpec, new String[] {leftBoundColumn},
				DoubleValue.class,
				false, true);
		checkColumnsForAvailability(inSpec, new String[] {rightBoundColumn},
				DoubleValue.class,
				false, true);
		if(settings.useModeColumns()) {
			if(leftModeColumn == null || rightModeColumn == null)
				throw new InvalidSettingsException("Settings mismatch: Mode columns are required but not set");
			checkColumnsForAvailability(inSpec, new String[] {leftModeColumn},
					BooleanValue.class,
					false, true);
			checkColumnsForAvailability(inSpec, new String[] {rightModeColumn},
					BooleanValue.class,
					false, true);
		}
		else {
			if(!EnumUtils.isValidEnum(Mode.class, fixedMode))
				throw new InvalidSettingsException("Settings corrupt: Fixed mode is set to \""
						+ fixedMode + "\" which is not a value of {" + Mode.values() + "}");
		}
		
		// check availability of column which should be replaced
		if(!settings.createNewColumn()) {
			if(outColumnName == null)
				throw new InvalidSettingsException("Settings mismatch: Column name of column to be replaced cannot be null");
			checkColumnsForAvailability(inSpec, outColumnName, DataValue.class, false, true);
		}
		
		if(leftBoundColumn.equals(rightBoundColumn))
			setWarningMessage("Left bound column equals right bound column. This will result in a single value set or an empty set");
		
		ColumnRearranger cRearrange = createColumnRearranger(inSpecs[0], settings);
		
		return new DataTableSpec[] {cRearrange.createSpec()};
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec spec, CreateIntervalNodeSettings settings) {
		
		boolean appendColumn = settings.createNewColumn();
		String outColumnName = settings.getOutColumnName();
		
		// make sure the newly appended column is unique
		if(appendColumn) {
			outColumnName = DataTableSpec.getUniqueColumnName(spec, outColumnName);
			settings.setOutColumnName(outColumnName);
		}
		
		ColumnRearranger cRearrange = new ColumnRearranger(spec);
		
		if(appendColumn)
			cRearrange.append(new CreateIntervalCellFactory(settings, spec));
		else
			cRearrange.replace(new CreateIntervalCellFactory(settings, spec), outColumnName);
		
		return cRearrange;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}
	
	
}
