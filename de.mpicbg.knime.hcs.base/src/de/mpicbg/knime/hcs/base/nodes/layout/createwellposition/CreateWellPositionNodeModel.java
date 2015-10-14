package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;
import java.util.ArrayList;
import java.util.List;

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
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * The node should be able to create a new string column with the well position, given two input columns with the plate row (numeric or alphabetic) and plate column (numeric) information.
There should be an option whether to keep (default) or remove the two input columns.
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
    public static final String CFG_formateColumn = "format.gen.column";


    /**
     * Constructor for the node model.
     */
    protected CreateWellPositionNodeModel() {

	// TODO: Specify the amount of input and output ports needed.
	super(1, 1, true);
	addModelSetting(CreateWellPositionNodeModel.CFG_PlateColumn, createPlateColumn());
	addModelSetting(CreateWellPositionNodeModel.CFG_PlateRow, createPlateRow());
	addModelSetting(CreateWellPositionNodeModel.CFG_deleteSouceCol,  createDelSourceCol());
	addModelSetting(CreateWellPositionNodeModel.CFG_formateColumn, createDelSourceRow());

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
	return new SettingsModelBoolean(CFG_formateColumn, false);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable[] execute(BufferedDataTable[] inData,
	    ExecutionContext exec) throws Exception {

	BufferedDataTable input = inData[0];
	DataTableSpec tSpec = input.getSpec();

	String plateColumn = null;
	if(getModelSetting(CFG_PlateColumn) != null) plateColumn = ((SettingsModelString) getModelSetting(CFG_PlateColumn)).getStringValue();
	int idCol = tSpec.findColumnIndex(plateColumn);

	String plateRow = null;
	if(getModelSetting(CFG_PlateRow) != null) plateRow = ((SettingsModelString) getModelSetting(CFG_PlateRow)).getStringValue();
	int idRow = tSpec.findColumnIndex(plateRow);  

	ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec(),idCol ,idRow);



	if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) {c.remove(idCol, idRow);}
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

	// if plateColumn and plateRow column is not set, try autoguessing
	if(plateColumn == null) {
	    List<String> guessedColums = tryAutoGuessingPlateColumns(tSpec);
	    plateColumn = guessedColums.get(0);
	    plateRow = guessedColums.get(1);

	    ((SettingsModelString)this.getModelSetting(CFG_PlateColumn)).setStringValue(plateColumn);
	    ((SettingsModelString)this.getModelSetting(CFG_PlateRow)).setStringValue(plateRow);

	} 

	// check if barcode column is available in input column
	if(!tSpec.containsName(plateColumn))
	    throw new InvalidSettingsException("Column '" + plateColumn + "' is not available in input table.");    		
	//if(!tSpec.getColumnSpec(plateColumn).getType().isCompatible(StringValue.class))
	//throw new InvalidSettingsException("Column '" + plateColumn + "' is not a string column");

	int idCol = tSpec.findColumnIndex(plateColumn);
	int idRow = tSpec.findColumnIndex(plateRow);


	ColumnRearranger c = createColumnRearranger(in[0], idCol , idRow);
	if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) {c.remove(idCol, idRow);}
	DataTableSpec result = c.createSpec();
	return new DataTableSpec[]{result};
    }


    private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final Integer idCol, final Integer idRow) {
	ColumnRearranger c = new ColumnRearranger(inSpec);
	// column spec of the appended column
	DataColumnSpec newColSpec = new DataColumnSpecCreator(
		"WellPosition", StringCell.TYPE).createSpec();
	// utility object that performs the calculation
	CellFactory factory = new SingleCellFactory(newColSpec) {
	    public DataCell getCell(DataRow row) {
		DataCell c0 = row.getCell(idCol);
		DataCell c1 = row.getCell(idRow);
		if (c0.isMissing() || c1.isMissing()) {
		    return DataType.getMissingCell();
		} else {
		    // configure method has checked if column 0 and 1 are numeric
		    // safe to type cast
		    String d0 = TdsUtils.mapPlateRowNumberToString(((IntValue)c1).getIntValue());
		    String d1 = c0.toString();
		    if(((SettingsModelBoolean) getModelSetting(CFG_formateColumn)).getBooleanValue() == true && ((IntValue)c0).getIntValue()  < 10) {
			return new StringCell(d0.concat("0").concat(d1));
			
		    }
		    return new StringCell(d0.concat(d1));
		}
	    }
	};
	c.append(factory);
	return c;
    }

    // Autoguessing for plate column and row in a dataset 
    private List<String> tryAutoGuessingPlateColumns(DataTableSpec tSpec) throws InvalidSettingsException {

	List<String> guessedColums = new ArrayList<String>();


	// check if "Plate" column available
	if(tSpec.containsName(CFG_PlateColumn_DFT)) {
	    if(tSpec.getColumnSpec(CFG_PlateColumn_DFT).getType().isCompatible(DoubleValue.class)) {
		guessedColums.add(0, CFG_PlateColumn_DFT);
	    }
	}

	// check if "Row" column available
	if(tSpec.containsName(CFG_PlateRow_DFT)) {
	    if(tSpec.getColumnSpec(CFG_PlateRow_DFT).getType().isCompatible(DoubleValue.class)) {
		guessedColums.add(1, CFG_PlateRow_DFT);
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
	    throw new InvalidSettingsException("Input table must contain at least one string or double column");
	}
	return guessedColums;
    }

}
