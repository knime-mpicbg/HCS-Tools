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
import org.knime.core.util.MutableInteger;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * node model class for Expand Well Position (V2)
 * 
 * @author Antje Janosch
 *
 */
public class ExpandWellPositionV2NodeModel extends AbstractNodeModel {
	
	// NODE SETTINGS
	
	public static final String CFG_WELL_COLUMN = "well.column";
	
	public static final String CFG_DELETE_SOURCE = "delete.source.column";
	public static final boolean CFG_DELETE_SOURCE_DFT = true;
	
	public static final String CFG_CONVERT_ROWVALS = "convert.row.characters";
	public static final boolean CFG_CONVERT_ROWVALS_DFT = true;
	
	// mismatch counter
	private MutableInteger m_mismatchCount = null;
	
	/**
	 * constructor
	 */
	public ExpandWellPositionV2NodeModel() {
		super(1,1,true);		
		
		this.addModelSetting(CFG_WELL_COLUMN, createWellColumnSettingsModel());
		this.addModelSetting(CFG_DELETE_SOURCE, createDeleteSourceSettingsModel());
		this.addModelSetting(CFG_CONVERT_ROWVALS, createConvertRowValuesSettingsModel());
	}

	/**
	 * create model setting for 'convert row values'
	 * @return	{link {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createConvertRowValuesSettingsModel() {
		return new SettingsModelBoolean(CFG_CONVERT_ROWVALS, CFG_CONVERT_ROWVALS_DFT);
	}

	/**
	 * create model setting for 'delete source column'
	 * @return	{link {@link SettingsModelBoolean}
	 */
	public static SettingsModelBoolean createDeleteSourceSettingsModel() {
		return new SettingsModelBoolean(CFG_DELETE_SOURCE, CFG_DELETE_SOURCE_DFT);
	}

	/**
	 * create model setting for well column
	 * @return {@link SettingsModelString}
	 */
	public static SettingsModelString createWellColumnSettingsModel() {
		return new SettingsModelString(CFG_WELL_COLUMN, null);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		DataTableSpec inSpec = inSpecs[0];
		
		// get settings
		String selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_WELL_COLUMN)).getStringValue();
		boolean deleteSource = ((SettingsModelBoolean) this.getModelSetting(CFG_DELETE_SOURCE)).getBooleanValue();
		boolean convertRowValues = ((SettingsModelBoolean) this.getModelSetting(CFG_CONVERT_ROWVALS)).getBooleanValue();
		
		// autoguess cplumn if not yet available
		boolean autoguess = false;
		if(selectedColumn == null) {
			for(DataColumnSpec cSpec : inSpec) {
				if(cSpec.getType().isCompatible(StringValue.class)) {
					selectedColumn = cSpec.getName();
					autoguess = true;
					continue;
				}
			}
		}
		
		// if even autoguessing failed
		if(selectedColumn == null)
			throw new InvalidSettingsException("Input table does not contain any column with well values");
		
		if(autoguess) {
			setWarningMessage("Autoguessing column with well strings: \"" + selectedColumn + "\"");
			this.updateModelSetting(CFG_WELL_COLUMN, new SettingsModelString(CFG_WELL_COLUMN, selectedColumn));
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

		return new DataTableSpec[]{rearranged_table.createSpec()};	
	}

	/**
	 * create column rearranger
	 * 
	 * @param inSpec			input table spec
	 * @param colIdx			index of well column
	 * @param deleteSource		if true, source column will be deleted
	 * @param convertRowValues	if true, row values will be converted into numbers
	 * @return		{@link ColumnRearranger}
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, int colIdx, boolean deleteSource,
			boolean convertRowValues) {
		ColumnRearranger cRearranger = new ColumnRearranger(inSpec);
		
		if(deleteSource)
			cRearranger.remove(colIdx);
		
		String plateRowName = DataTableSpec.getUniqueColumnName(cRearranger.createSpec(), TdsUtils.SCREEN_MODEL_WELL_ROW);
		String plateColumnName = DataTableSpec.getUniqueColumnName(cRearranger.createSpec(), TdsUtils.SCREEN_MODEL_WELL_COLUMN);
		
		cRearranger.append(new ExpandWellPositionV2CellFactory(colIdx, convertRowValues, plateRowName, plateColumnName, m_mismatchCount));			
		
		return cRearranger;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		DataTableSpec inSpec = inData[0].getDataTableSpec();
		
		// get settings
		String selectedColumn = ((SettingsModelString) this.getModelSetting(CFG_WELL_COLUMN)).getStringValue();
		boolean deleteSource = ((SettingsModelBoolean) this.getModelSetting(CFG_DELETE_SOURCE)).getBooleanValue();
		boolean convertRowValues = ((SettingsModelBoolean) this.getModelSetting(CFG_CONVERT_ROWVALS)).getBooleanValue();
		
		int colIdx = inSpec.findColumnIndex(selectedColumn);
		m_mismatchCount = new MutableInteger(0);
		
		ColumnRearranger cRearrange = createColumnRearranger(inSpec, colIdx, deleteSource, convertRowValues);
		
		BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], cRearrange, exec);
		
		// if any mismatch happend, throw warning message
		if(m_mismatchCount.intValue() > 0)
			this.setWarningMessage(m_mismatchCount.intValue() + " input string(s) did not match the pattern or are not in the expected range");
		
		return new BufferedDataTable[]{out};
	}

}
