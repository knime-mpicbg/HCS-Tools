package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;
import java.util.ArrayList;

import org.knime.base.*;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
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


	// Node settings and keys
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

		// Setting up a node with one input and one output port 
		// HINT: Constructor with parameter true is because of using the AbstractNodemodel from TdsUtils, it makes lot easier to save settings
		super(1, 1, true);

		/**
		 * Adding model settings to node
		 */
		addModelSetting(CreateWellPositionNodeModel.CFG_PlateColumn, createPlateColumn());
		addModelSetting(CreateWellPositionNodeModel.CFG_PlateRow, createPlateRow());
		addModelSetting(CreateWellPositionNodeModel.CFG_deleteSouceCol,  createDelSourceCol());
		addModelSetting(CreateWellPositionNodeModel.CFG_formateColumn, createFormateColumn());
	}


	/**
	 * SettingModel for the PlateColumn
	 */
	static final SettingsModelString createPlateColumn() {
		return new SettingsModelString( CFG_PlateColumn, null);
	}

	/**
	 * SettingModel for the PlateRow
	 */
	static final SettingsModelString createPlateRow() {
		return new SettingsModelString(CFG_PlateRow, null);
	}

	/**
	 * SettingModel for the option delete all source columns
	 */
	static final SettingsModelBoolean createDelSourceCol() {
		return new SettingsModelBoolean(CFG_deleteSouceCol, false);
	}

	/**
	 * SettingModel for the option to formating the content for better sorting
	 */
	static final SettingsModelBoolean createFormateColumn() {
		return new SettingsModelBoolean(CFG_formateColumn, false);
	}

	/**
	 * Execution class of the Node
	 */
	@Override
	public BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		// Reading in input table over port "Table Input"	
		BufferedDataTable input = inData[0];

		// Getting table specifications for using it later
		DataTableSpec tSpec = input.getSpec();


		// Reading in Settings of the node
		String plateColumn = null;
		if(getModelSetting(CFG_PlateColumn) != null) plateColumn = ((SettingsModelString) getModelSetting(CFG_PlateColumn)).getStringValue();

		// Saving index of column "plateColumn" into variable idCol
		int idCol = tSpec.findColumnIndex(plateColumn);


		String plateRow = null;
		if(getModelSetting(CFG_PlateRow) != null) plateRow = ((SettingsModelString) getModelSetting(CFG_PlateRow)).getStringValue();

		// Saving index of column "plateRow" into variable idCol
		int idRow = tSpec.findColumnIndex(plateRow);  


		// Rearrange and creating new table wit information of id's of the columns to rearrange.
		ColumnRearranger rearranged_table = createColumnRearranger(inData[0].getDataTableSpec(),idCol ,idRow);


		// Checking for option to delete all source columns
		if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) 
		{
			rearranged_table.remove(idCol, idRow);
		}

		// Creating new table
		BufferedDataTable output_table = exec.createColumnRearrangeTable(inData[0], rearranged_table, exec);

		return new BufferedDataTable[]{output_table};
	}

	/**
	 * Configure Class for configuring and checking the input
	 */
	public DataTableSpec[] configure(DataTableSpec[] in)
			throws InvalidSettingsException {

		// Reading in input table over port "Table Input"	
		DataTableSpec tSpec = in[0];

		// Reading in Settings of the node, if available 
		// =====================================================================================
		String plateColumn = null;
		if(getModelSetting(CFG_PlateColumn) != null) {
			plateColumn = ((SettingsModelString) getModelSetting(CFG_PlateColumn)).getStringValue();
		}

		String plateRow = null;
		if(getModelSetting(CFG_PlateRow) != null) {
			plateRow = ((SettingsModelString) getModelSetting(CFG_PlateRow)).getStringValue();
		}
		// =====================================================================================


		// if plateColumn and plateRow column is not set in the settings, try auto guessing
		if(plateColumn == null) {
			List<String> guessedColums = tryAutoGuessingPlateColumns(tSpec);

			// saving names of the guessed Columns into variable
			plateColumn = guessedColums.get(0);
			plateRow = guessedColums.get(1);

			// saving names of the guessed Columns into settings of the model
			((SettingsModelString)this.getModelSetting(CFG_PlateColumn)).setStringValue(plateColumn);
			((SettingsModelString)this.getModelSetting(CFG_PlateRow)).setStringValue(plateRow);

		} 

		// Saving index of column and row into variable idCol and idRow
		int idCol = tSpec.findColumnIndex(plateColumn);
		int idRow = tSpec.findColumnIndex(plateRow);

		// Rearrange and creating new table wit information of id's of the columns to rearrange.
		ColumnRearranger rearranged_table = createColumnRearranger(in[0], idCol , idRow);
		
		// Checking for option to delete all source columns
				if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) 
				{
					rearranged_table.remove(idCol, idRow);
				}

		DataTableSpec output_table = rearranged_table.createSpec();

		return new DataTableSpec[]{output_table};
	}


	/**
	 * ColumnRearranger to create output table
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final Integer idCol, final Integer idRow) {

		// Creating new ColumRearranger instance
		ColumnRearranger rearranged_table = new ColumnRearranger(inSpec);

		// column specification of the appended column
		DataColumnSpec newColSpec = new DataColumnSpecCreator("WellPosition", StringCell.TYPE).createSpec();

		// utility object that performs the calculation
		CellFactory factory = new SingleCellFactory(newColSpec) {

			// iterating over every row of input table
			public DataCell getCell(DataRow row) {

				DataCell dcell0 = row.getCell(idCol);
				DataCell dcell1 = row.getCell(idRow);
				
				String ConvData0 = dcell0.toString();
				
				// Saving value of plateColumn column into ConvData1
				String ConvData1 = dcell1.toString();

				// checking for missing cell, if so then returning missing cell
				if (dcell0.isMissing() || dcell1.isMissing()) 
				{
					return DataType.getMissingCell();
				} 
				else 
				{
					try
					{
						// checking if the value of the position of the column is compatible to the supported well format
						if(Double.parseDouble(ConvData0) > TdsUtils.MAX_PLATE_COLUMN)
						{
							setWarningMessage("Can not use plate Column value of row " + dcell0.toString() + " - it's out of range of the supported well formats");
							return DataType.getMissingCell();
						}
						
						
						// check if row format already alphabetical
						if(ConvData1.matches("^[aA]{0,1}[a-zA-Z]{1}$"))
						{
							// Converts lower case input to upper case - better looking
							ConvData1 = ConvData1.toUpperCase();
						}
					
						// Converting the numeric column to alphabetical
						else
						{
							// Try to parse String value to double for using the mapPlateRowNumberToString
							Double ConvDataDouble = Double.parseDouble(ConvData1);
							
							// Cast double to integer for handling with double columns
							Integer ConvDataINT = (int)ConvDataDouble.doubleValue();
							
							// Converting numeric values to alphabetical
							ConvData1 = TdsUtils.mapPlateRowNumberToString(ConvDataINT);

							//check for row numbers compatible to supported well formats (up to 1536 well plate)
							if(ConvData1 == null)
							{
								// give a Warning message and returning a missing cell
								setWarningMessage("Can not convert Row Nr. " + dcell0.toString() + " - it's out of range of the supported well formats");
								return DataType.getMissingCell();
							}
							
						}
						
					}

					// catches number format exception while converting the values to alphabetical format
					catch (NumberFormatException e)
					{
						setWarningMessage("Wrong number format - Not able to convert or process the given values - check your selected columns in row " + dcell0.toString());
						return DataType.getMissingCell();
					}
					
					// catches Null pointer exception while converting the values to alphabetical format
					catch (NullPointerException e){
						setWarningMessage("Null Pointer Exception by converting your row column to alphabetical values - check row "+ dcell0.toString() + " in your source column");
						return DataType.getMissingCell();
					}
					
					//  catches missing entries in the auto guessing array
					catch (IndexOutOfBoundsException e){
						setWarningMessage("Autoguessing failed - the node did not get any column out of the autoguessing");
						return DataType.getMissingCell();
					}

					// checking current setting for formating columns for better sorting
					if(((SettingsModelBoolean) getModelSetting(CFG_formateColumn)).getBooleanValue() == true) {
						if(ConvData1.length() == 1 )
						{
							ConvData1 = " " + ConvData1;
						}
						if(ConvData0.length() == 1)
						{
							return new StringCell(ConvData1.concat("0").concat(ConvData0));
						}
					}
					return new StringCell(ConvData1.concat(ConvData0));
				}
			}



		};
		rearranged_table.append(factory);
		return rearranged_table;
	}

	/**
	 * Auto guessing for plate column and row in a data set 
	 */
	@SuppressWarnings("unused")
	private List<String> tryAutoGuessingPlateColumns(DataTableSpec tSpec) throws InvalidSettingsException {

		// Array for guessed Columns to return
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
		if(guessedColums.size() == 0){
			// check if input table has string or double compatible columns at all
			for(String col: tSpec.getColumnNames()) {
				if(tSpec.getColumnSpec(col).getType().isCompatible(StringValue.class) || tSpec.getColumnSpec(col).getType().isCompatible(DoubleValue.class)) {
					// if there is no column like that, he should use the last string out of the table
					guessedColums.add(0, col);
					guessedColums.add(1, col);
					break;
				}
			}
		}
		// if the guessing fails and there is no compatible column it throws an exception
		if(guessedColums.size() == 0) {
			throw new InvalidSettingsException("Input table must contain at least one string or double column");
		}
		return guessedColums;
	}

}
