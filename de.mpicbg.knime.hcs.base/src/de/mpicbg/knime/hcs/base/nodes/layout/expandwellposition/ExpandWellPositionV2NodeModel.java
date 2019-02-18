package de.mpicbg.knime.hcs.base.nodes.layout.expandwellposition;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.knutils.AbstractNodeModel;

public class ExpandWellPositionV2NodeModel extends AbstractNodeModel {
	
	public static final String CFG_WELL_COLUMN = "well.column";
	
	public static final String CFG_DELETE_SOURCE = "delete.source.column";
	public static final boolean CFG_DELETE_SOURCE_DFT = true;
	
	public static final String CFG_CONVERT_ROWVALS = "convert.row.characters";
	public static final boolean CFG_CONVERT_ROWVALS_DFT = true;
	
	public ExpandWellPositionV2NodeModel() {
		super(1,1,true);		
		
		this.addModelSetting(CFG_WELL_COLUMN, createWellColumnSettingsModel());
		this.addModelSetting(CFG_DELETE_SOURCE, createDeleteSourceSettingsModel());
		this.addModelSetting(CFG_CONVERT_ROWVALS, createConvertRowValuesSettingsModel());
	}

	public static SettingsModelBoolean createConvertRowValuesSettingsModel() {
		return new SettingsModelBoolean(CFG_CONVERT_ROWVALS, CFG_CONVERT_ROWVALS_DFT);
	}

	public static SettingsModelBoolean createDeleteSourceSettingsModel() {
		return new SettingsModelBoolean(CFG_DELETE_SOURCE, CFG_DELETE_SOURCE_DFT);
	}

	public static SettingsModelString createWellColumnSettingsModel() {
		return new SettingsModelString(CFG_WELL_COLUMN, null);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		DataTableSpec inSpec = inSpecs[0];
		String selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_WELL_COLUMN)).getStringValue();
		boolean deleteSource = ((SettingsModelBoolean) this.getModelSetting(CFG_DELETE_SOURCE)).getBooleanValue();
		boolean convertRowValues = ((SettingsModelBoolean) this.getModelSetting(CFG_CONVERT_ROWVALS)).getBooleanValue();
		
		// autoguess cplumn if not yet available
		if(selectedColumn == null) {
			for(DataColumnSpec cSpec : inSpec) {
				if(cSpec.getType().isCompatible(StringValue.class)) {
					selectedColumn = cSpec.getName();
					setWarningMessage("Autoguessing column with well strings: \"" + cSpec.getName() + "\"");
					this.updateModelSetting(CFG_WELL_COLUMN, new SettingsModelString(CFG_WELL_COLUMN, selectedColumn));
				}
			}
		}
		
		// check for availability
		if(!inSpec.containsName(selectedColumn))
			throw new InvalidSettingsException("Input table does not contain the expected columns: " + selectedColumn);
		
		// Saving index of column and row into variable idCol and idRow
		int colIdx = inSpec.findColumnIndex(selectedColumn);
		
		// check for correct data type
		DataType t = inSpec.getColumnSpec(colIdx).getType();
		if(!(t.isCompatible(StringValue.class) || t.isCompatible(DoubleValue.class)))
			throw new InvalidSettingsException("Column \"" + selectedColumn + "\" must not be of type \"" + t.getName() +"\".");
	
		
		ColumnRearranger rearranged_table = createColumnRearranger(inSpec, colIdx, deleteSource, convertRowValues);
		DataTableSpec outSpec = rearranged_table.createSpec();

		return new DataTableSpec[]{outSpec};	
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, int colIdx, boolean deleteSource,
			boolean convertRowValues) {
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		
		return rearranger;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}

}
