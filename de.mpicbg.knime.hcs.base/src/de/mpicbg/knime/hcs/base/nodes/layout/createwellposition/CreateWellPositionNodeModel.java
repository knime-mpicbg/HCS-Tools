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
import org.knime.core.data.IntValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
	
	
	private void SendWarning(Integer WarnNum, String CellValue, String RowNum, String ColumnName) {
		String[] ValueErrorRange = new String[20];
		
		// Value Error - the column value for the well position is not supported or in a wrong type
		ValueErrorRange[0] = "Error in RowID \"" + RowNum + "\" in column " + ColumnName + " - the cell value " + CellValue + " seems to be not supported or in a wrong format";
		
		// Value Error - the column value for the well position is not supported or in a wrong type
		ValueErrorRange[1] = "Error in RowID \"" + RowNum + "\" in column \"" + ColumnName +"\" - the cell value " + CellValue + " seems to be not supported or in a wrong format";
		
		
		setWarningMessage(ValueErrorRange[WarnNum]);
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
		DataType type = tSpec.getColumnSpec(idRow).getType();
		
		double maxPlateRow= 0;
		for(DataRow row : inData[0]) {
			DataCell cell = row.getCell(idRow);
			if(cell.isMissing())
				continue;
			if(type.isCompatible(StringValue.class)) {
				String val = ((StringValue) cell).getStringValue();
				int rNum = TdsUtils.mapPlateRowStringToNumber(val);
				if(rNum > maxPlateRow)
					maxPlateRow = (double) rNum;
			}
			if(type.isCompatible(DoubleValue.class)) {
				double val = ((DoubleValue) cell).getDoubleValue();
				if(val > maxPlateRow)
					maxPlateRow = val;
			}
		}
		
		boolean needsSpace = maxPlateRow > 26;


		// Rearrange and creating new table wit information of id's of the columns to rearrange.
		ColumnRearranger rearranged_table = createColumnRearranger(inData[0].getDataTableSpec(),idCol ,idRow, needsSpace);


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
		ColumnRearranger rearranged_table = createColumnRearranger(in[0], idCol , idRow, false);

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
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final Integer idCol, final Integer idRow, final boolean needsSpace) {

		// Creating new ColumRearranger instance
		ColumnRearranger rearranged_table = new ColumnRearranger(inSpec);
		

		// column specification of the appended column
		DataColumnSpec newColSpec = new DataColumnSpecCreator("Well Position", StringCell.TYPE).createSpec();

		//final String[] ColumnNames = inSpec.getColumnNames();
		String columnName = inSpec.getColumnSpec(idCol).getName();
		String rowName = inSpec.getColumnSpec(idRow).getName();
		
		DataType rowDataType = inSpec.getColumnSpec(idRow.intValue()).getType();
		DataType columnDataType = inSpec.getColumnSpec(idCol.intValue()).getType();
		
		boolean sortable = ((SettingsModelBoolean) getModelSetting(CFG_formateColumn)).getBooleanValue();

		// utility object that performs the calculation
		CellFactory factory = new SingleCellFactory(newColSpec) {

			// iterating over every row of input table
			public DataCell getCell(DataRow row) {
				
				DataCell rowCell = row.getCell(idRow);
				DataCell columnCell = row.getCell(idCol);

				String rowString = null;
				String columnString = null;
				
				RowKey rowKey = row.getKey();

				// checking for missing cell, if so then returning missing cell
				if (rowCell.isMissing() || columnCell.isMissing()) 
				{
					return DataType.getMissingCell();
				} 

				//=========== Checking and Converting Column ===========//

				// checking the value for the column position on the well is supported
				int colVal = -1;
				if(columnDataType.isCompatible(DoubleValue.class))
				{
					Double colValDouble = ((DoubleValue) columnCell).getDoubleValue();
					
					if(colValDouble % 1 != 0) {
						throw new IllegalArgumentException(
								createValueErrorMessage(
										rowKey, 
										colValDouble.toString(), 
										columnName, 
										"is not integer."));
					}
										
					colVal = colValDouble.intValue();
					
				}
				if(columnDataType.isCompatible(StringValue.class)) {
					String colValString = ((StringValue) columnCell).getStringValue();
										
					try {
						colVal = Integer.parseInt(colValString);
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException(
								createValueErrorMessage(
										rowKey, 
										colValString, 
										columnName, 
										"does not represent an integer."));
					}
					
				}
				if(colVal < 1 || colVal > TdsUtils.MAX_PLATE_COLUMN)
					throw new IllegalArgumentException(
							createValueErrorMessage(
									rowKey, 
									Integer.toString(colVal), 
									columnName, 
									" is not within the expected range [1;" + TdsUtils.MAX_PLATE_COLUMN + "]"));
				columnString = Integer.toString(colVal);
				
				if(sortable && columnString.length() == 1)
					columnString = "0" + columnString;
				

				//=========== Checking and Converting Row ===========//

				if(rowCell.getType() == StringCell.TYPE)
				{
					rowString = ((StringValue) rowCell).getStringValue();

					
					// Checking if the alphabetical input values are compatible to well plates
					// IMPORTANT: if the supported well plates is gonna change - its necessary to change the regex
					if(rowString.matches("^[aA]{0,1}[aA-fF]{1}$"))
					{
						// Converts lower case input to upper case - better looking
						rowString = rowString.toUpperCase();
					}

					else
					{
						//SendWarning(1, rowCell.toString(), RowId, ColumnNames[idRow]);
						return DataType.getMissingCell();
					}
				}

				if(rowCell.getType() == DoubleCell.TYPE ||rowCell.getType() == IntCell.TYPE )
				{
					Double ConvDataDouble = ((DoubleValue) rowCell).getDoubleValue();

					if(ConvDataDouble > TdsUtils.MAX_PLATE_ROW)
					{
						//SendWarning(1, rowCell.toString(), RowId, ColumnNames[idRow]);
						return DataType.getMissingCell();
					}

					try
					{
						Integer ConvDataInt = (int)ConvDataDouble.doubleValue();
						columnString = TdsUtils.mapPlateRowNumberToString(ConvDataInt);

						//check for row numbers compatible to supported well formats (up to 1536 well plate)
						if(rowString == null)
						{
							// give a Warning message and returning a missing cell
							//SendWarning(1, rowCell.toString(), RowId, ColumnNames[idRow]);
							
							return DataType.getMissingCell();
						}

					}

					// catches number format exception while converting the values to alphabetical format
					catch (NumberFormatException e)
					{
						//SendWarning(1, rowCell.toString(), RowId, ColumnNames[idCol]);
						return DataType.getMissingCell();
					}
					// catches Null pointer exception while converting the values to alphabetical format
					catch (NullPointerException e)
					{
						//setWarningMessage("Null Pointer Exception please check your input table");
						return DataType.getMissingCell();
					}
					//  catches missing entries in the auto guessing array
					catch (IndexOutOfBoundsException e)
					{
						//setWarningMessage("ValueError - can not use cell value for creating row position in row " + dcell1.toString() + " - it's out of range of the supported well formats");
						//SendWarning(1, rowCell.toString(), RowId, ColumnNames[idCol]);
						return DataType.getMissingCell();
					}
				}

				// checking current setting for formating columns for better sorting
				if(((SettingsModelBoolean) getModelSetting(CFG_formateColumn)).getBooleanValue() == true) 
				{
					if(rowString.length() == 1 && needsSpace)
					{
						rowString = " " + rowString;
					}
				}
				
				return new StringCell(rowString.concat(columnString));
			}

			private String createValueErrorMessage(RowKey rowKey, String valueString, String columnName, String reason) {
				return rowKey.getString() + ": Value \"" 
						+ valueString + "\" in column \"" 
						+ columnName + "\" " + reason;
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
