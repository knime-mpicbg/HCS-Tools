package de.mpicbg.knime.hcs.base.nodes.layout.expandwellposition;

import org.knime.core.data.DataTableSpec;
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
		// TODO Auto-generated method stub
		return super.configure(inSpecs);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}

}
