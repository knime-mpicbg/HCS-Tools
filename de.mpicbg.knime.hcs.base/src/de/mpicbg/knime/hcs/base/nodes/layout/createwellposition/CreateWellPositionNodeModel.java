package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;

import java.io.File;
import java.io.IOException;

import org.dmg.pmml.DATATYPE;
import org.knime.base.node.io.filereader.DataCellFactory;
import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.RowAppender;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.hcs.base.nodes.layout.ExpandWellPositionFactory;

import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel;
import de.mpicbg.knime.hcs.core.TdsUtils;

/**
 * The node should be able to create a new string column with the well position, given two input columns with the plate row (numeric or alphabetic) and plate column (numeric) information.
There should be an option whether to keep (default) or remove the two input columns.
There should be different formatting options available (expanddable).
 * 
 *
 * @author Tim Nicolaisen
 */
public class CreateWellPositionNodeModel extends AbstractNodeModel {
    
	
	// NODE SETTINGS KEYS + DEFAULTS
	
	public static final String CFG_PlateColumn =  "plate.column";
	public static final String CFG_PlateColumn_DFT = "plateColumn";
	
	public static final String CFG_PlateRow = "plate.row";
	public static final String CFG_PlateRow_DFT = "plateRow";
	
	public static final String CFG_deleteSouceCol = "delete.source.column";
	public static final String CFG_deleteSouceRow = "delete.source.row";
	
    /**
     * Constructor for the node model.
     */
    protected CreateWellPositionNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1, true);
        addModelSetting(CreateWellPositionNodeModel.CFG_PlateColumn, createPlateColumn());
		addModelSetting(CreateWellPositionNodeModel.CFG_PlateRow, createPlateRow());
		addModelSetting(CreateWellPositionNodeModel.CFG_deleteSouceCol,  createDelSourceCol());
		addModelSetting(CreateWellPositionNodeModel.CFG_deleteSouceRow, createDelSourceRow());
		
    }
    
    private SettingsModel createPlateColumn() {
		return new SettingsModelString( CFG_PlateColumn, null);
	}
    
    private SettingsModel createPlateRow() {
		return new SettingsModelString(CFG_PlateRow, null);
	}

    private SettingsModel createDelSourceCol() {
		return new SettingsModelBoolean(CFG_deleteSouceCol, false);
	}
    
    private SettingsModel createDelSourceRow() {
		return new SettingsModelBoolean(CFG_deleteSouceRow, false);
	}
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable[] execute(BufferedDataTable[] inData,
    	     ExecutionContext exec) throws Exception {
    	
    	
    	     ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
    	     BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
    	     return new BufferedDataTable[]{out};
    	 }

    	 public DataTableSpec[] configure(DataTableSpec[] in)
    	         throws InvalidSettingsException {
    		 DataTableSpec tSpec = in[0];
    		 
    		 // get settings if available
    			String plateColumn = null;
    			if(getModelSetting(CFG_PlateColumn) != null) plateColumn = ((SettingsModelString) getModelSetting(CFG_PlateColumn)).getStringValue();
    			
    			String plateRow = null;
    			if(getModelSetting(CFG_PlateRow) != null) plateRow = ((SettingsModelString) getModelSetting(CFG_PlateRow)).getStringValue();
    		 
    		// checks for barcode column
    			// =====================================================================================

    			// if barcode column is not set, try autoguessing
    			if(plateColumn == null) {
    				plateColumn = tryAutoGuessingPlateColumn(tSpec);
    				((SettingsModelString)this.getModelSetting(CFG_PlateColumn)).setStringValue(plateColumn);
    				
    			} 

    			// check if barcode column is available in input column
    			if(!tSpec.containsName(plateColumn))
    				throw new InvalidSettingsException("Column '" + plateColumn + "' is not available in input table.");    		
    			//if(!tSpec.getColumnSpec(plateColumn).getType().isCompatible(StringValue.class))
    				//throw new InvalidSettingsException("Column '" + plateColumn + "' is not a string column");

    		 
    		 
    		 
    		 
    	     DataColumnSpec c0 = in[0].getColumnSpec(3);
    	     DataColumnSpec c1 = in[0].getColumnSpec(2);
    	     if (!c0.getType().isCompatible(DoubleValue.class)) {
    	         throw new InvalidSettingsException(
    	           "Invalid type at first column.");
    	     }
    	     if (!c1.getType().isCompatible(DoubleValue.class)) {
    	         throw new InvalidSettingsException(
    	           "Invalid type at second column.");
    	     }
    	     ColumnRearranger c = createColumnRearranger(in[0]);
    	     DataTableSpec result = c.createSpec();
    	     return new DataTableSpec[]{result};
    	 }
    	 

    

    	 private ColumnRearranger createColumnRearranger(DataTableSpec in) {
    	     ColumnRearranger c = new ColumnRearranger(in);
    	     // column spec of the appended column
    	     DataColumnSpec newColSpec = new DataColumnSpecCreator(
    	     "WellPosition", StringCell.TYPE).createSpec();
    	     // utility object that performs the calculation
    	     CellFactory factory = new SingleCellFactory(newColSpec) {
    	         public DataCell getCell(DataRow row) {
    	             DataCell c0 = row.getCell(1);
    	             DataCell c1 = row.getCell(2);
    	             if (c0.isMissing() || c1.isMissing()) {
    	                 return DataType.getMissingCell();
    	             } else {
    	                 // configure method has checked if column 0 and 1 are numeric
    	                 // safe to type cast
    	                 String d0 = TdsUtils.mapPlateRowNumberToString(((IntValue)c0).getIntValue());
    	                 String d1 = c1.toString();
    	                 return new StringCell(d0.concat(d1));
    	             }
    	         }
    	     };
    	     c.append(factory);
    	     return c;
    	 }

    	 
    	 private String tryAutoGuessingPlateColumn(DataTableSpec tSpec) throws InvalidSettingsException {

    			// check if "Plate" column available
    			if(tSpec.containsName(CFG_PlateColumn)) {
    				if(tSpec.getColumnSpec(CFG_PlateColumn_DFT).getType().isCompatible(DoubleValue.class)) {
    					return CFG_PlateColumn_DFT;
    				}
    			}
    			

    			// check if input table has string or double compatible columns at all
    			String firstStringColumn = null;
    			for(String col: tSpec.getColumnNames()) {
    				if(tSpec.getColumnSpec(col).getType().isCompatible(StringValue.class) || tSpec.getColumnSpec(col).getType().isCompatible(DoubleValue.class)) {
    					if(col.contains("plateColumn")){
    						firstStringColumn = col;
    					}
    					else firstStringColumn = col;break;
    				}
    				
    			}
    			if(firstStringColumn == null) {
    				throw new InvalidSettingsException("Input table must contain at least one string column");
    			}
    			return "plateColumn";
    	 }
    
}
