package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of NumberFormatter.
 * 
 * 
 * @author: Magda Rucinska
 */
public class NumberFormatterNodeModel extends AbstractNodeModel {

	// NODE SETTINGS KEYS + DEFAULTS

	public static final String CFG_ConcentrationColumn = "con.column";
	public static final String CFG_ConcentrationColumn_DFT = "Concentration";

	public static final String CFG_deleteSouceCol = "delete.source.column";
	public static final String CFG_leadingCharacter = "leading.character";
	
	public static final String CFG_DECIMALS = "number.decimals";
	public static final int CFG_DECIMALS_DFT = -1;

	/**
	 * Constructor for the node model.
	 */
	protected NumberFormatterNodeModel() {

		super(1, 1, true);
		addModelSetting(NumberFormatterNodeModel.CFG_ConcentrationColumn,
				createConcentrationColumn());
		addModelSetting(NumberFormatterNodeModel.CFG_deleteSouceCol,
				createDelSourceCol());
		addModelSetting(NumberFormatterNodeModel.CFG_leadingCharacter,
				createleadingCharacter());
		addModelSetting(CFG_DECIMALS, createDecimalSM());
	}

	/**
	 * setting model for number of decimals
	 * @return
	 */
	public static SettingsModel createDecimalSM() {
		return new SettingsModelIntegerBounded(CFG_DECIMALS, CFG_DECIMALS_DFT, -1, Integer.MAX_VALUE);
	}

	// store and retrieve values from - transport of values
	private SettingsModel createConcentrationColumn() {
		return new SettingsModelString(CFG_ConcentrationColumn, null);
	}

	private SettingsModel createDelSourceCol() {
		return new SettingsModelBoolean(CFG_deleteSouceCol, false);
	}

	private SettingsModel createleadingCharacter() {
		return new SettingsModelString(CFG_leadingCharacter, "0");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		BufferedDataTable input = inData[0]; // take all data
		DataTableSpec tSpec = input.getSpec(); // get specification of table
		// choose deafult settings
		String conColumn = null;

		if (getModelSetting(CFG_ConcentrationColumn) != null)
			conColumn = ((SettingsModelString) getModelSetting(CFG_ConcentrationColumn)).getStringValue();

		// take an id of a column with concentration name
		int idCol = tSpec.findColumnIndex(conColumn); 
		// get column specification of conColumn that have concentration data
		DataColumnSpec cSpec = inData[0].getDataTableSpec().getColumnSpec(idCol); 

		NumberFormatSettings nf = getFormatSettings(inData[0], idCol, checkForDataType(cSpec));
			
		AtomicInteger neg = new AtomicInteger();
		AtomicInteger nnum = new AtomicInteger();

		ColumnRearranger c = createColumnRearranger(
				inData[0].getDataTableSpec(), idCol, null, nf,
				checkForDataType(cSpec), nnum, neg);

		if (((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol))
				.getBooleanValue() == true) {
			c.remove(idCol);
		}

		BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c,
				exec);
		if (neg.get() > 0 || nnum.get() > 0) {
			setWarningMessage(neg.get()
					+ " input string(s) can not be represented as a number, "
					+ nnum.get() + " have a negative value");
		}
		return new BufferedDataTable[] { out };
	}

	/** check what kind of Type is your data: 0 - Numeric, 1 - String */
	private int checkForDataType(DataColumnSpec cSpec) {
		if (cSpec.getType().isCompatible(DoubleValue.class)) {
			return 0;
		} else {
			return 1;
		} // 1 is a string
	}

	/**
	 * parse the whole column to retrieve the formatting setting (number of leading/trailing characters and if 
	 * number should be represented as whole number or not
	 * @param inTable
	 * @param idCol index of the selected column
	 * @param namecolumntype
	 * @return settings to format the numbers
	 */
	private NumberFormatSettings getFormatSettings(BufferedDataTable inTable, int idCol,
			int namecolumntype) {
		NumberFormatSettings nf = new NumberFormatSettings();
		nf.setAsWholeNumber(true);
		nf.setNLeading(1);
		nf.setNTrailing(0);

		int nDecimals = ((SettingsModelIntegerBounded) getModelSetting(CFG_DECIMALS)).getIntValue();
		boolean estimateDecimals = (nDecimals == CFG_DECIMALS_DFT);

		for (DataRow row : inTable) {
			if (row.getCell(idCol).isMissing()) {
				continue;
			}
			
			// retrieve double value from cell
			BigDecimal value = null;
			if (namecolumntype == 0) {
				double cellVal = ((DoubleValue) row.getCell(idCol)).getDoubleValue();
				value = new BigDecimal(String.valueOf(cellVal));
			} else {
				try {
					value = new BigDecimal(((StringValue) row.getCell(idCol)).getStringValue());
				} catch (NumberFormatException e) {
					continue;
				}
			}
			
			// split number into integer-part and decimal-part
			String[] splitted = splitNumber(value);
			boolean hasDecimal = splitted.length > 1;
			if(hasDecimal) 
				if(!splitted[1].equals("0"))
					nf.setAsWholeNumber(false);

			// get length of both parts
			int nLeading = splitted[0].length();
			int nTrailing = hasDecimal ? splitted[1].length() : 0;
			
			// replace leading/trailing if necessary
			if(nLeading > nf.getNLeading())
				nf.setNLeading(nLeading);
			if(nTrailing > nf.getNTrailing())
				nf.setNTrailing(nTrailing);
		}
		
		if(!estimateDecimals) {
			nf.setNTrailing(nDecimals);
			nf.setAsWholeNumber(false);
		}

		return nf;
	}

	/**
	 * {@inheritDoc

	 */
	@Override
	public DataTableSpec[] configure(DataTableSpec[] in)
			throws InvalidSettingsException {
		DataTableSpec tSpec = in[0];

		// get settings if available
		SettingsModelString columnModelSetting = (SettingsModelString) getModelSetting(CFG_ConcentrationColumn);
		String conColumn = columnModelSetting.getStringValue();

		if (conColumn == null) {
			conColumn = tryAutoGuessingConcentrationColumn(tSpec);

			((SettingsModelString) this
					.getModelSetting(CFG_ConcentrationColumn))
					.setStringValue(conColumn);

		}

		// check if concentration column is available in input column
		if (!tSpec.containsName(conColumn))
			throw new InvalidSettingsException("Column '" + conColumn
					+ "' is not available in input table.");

		int idCol = tSpec.findColumnIndex(conColumn);

		// new column spec based on column id
		DataColumnSpec cSpec = tSpec.getColumnSpec(idCol); 
		// check if this is string (based on the function checkForDataType
		if (checkForDataType(cSpec) == 1) 
		{
			// if domain is not null
			if (cSpec.getDomain() != null) { 
				
				// check first that getValues is a Set, thats why we do Set<DataCell>
				// here we get a set of data that is present in domains. the set is called domVals
				Set<DataCell> domVals = cSpec.getDomain().getValues(); 
				if (domVals != null) { //
					int nNumericStrings = 0;
					// iteration for all data set
					for (DataCell cell : domVals) { 
						try {
							// try to parse it to double
							Double.parseDouble(((StringValue) cell).getStringValue()); 
							// this is number of successfully converted values
							nNumericStrings++; 
						} catch (NumberFormatException e) {
						}
					}
					// if any number converted throw an error
					if (nNumericStrings == 0) { 
						throw new InvalidSettingsException(
								"Selected column does not contain any numeric values");
					}
				}
			}

		} else {
			//if a lower bound is available make sure it is not < 0
			DataColumnDomain domain = tSpec.getColumnSpec(idCol).getDomain();
			if (domain != null) {
				if(domain.getLowerBound() != null) {
					double lower = ((DoubleValue) domain.getLowerBound())
							.getDoubleValue();
	
					if (lower < 0) {
						throw new InvalidSettingsException(
								"Negative values in the column");
					}
				}
			}
		}

		ColumnRearranger c = createColumnRearranger(in[0], idCol, null,
				null, checkForDataType(tSpec.getColumnSpec(idCol)),
				null, null); // why null there?
		// assume its always string - forcing
		if (((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol))
				.getBooleanValue() == true) {
			c.remove(idCol);
		}

		DataTableSpec result = c.createSpec();
		return new DataTableSpec[] { result };

	}

	/** create new column */
	private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec,
			final Integer idCol, final Integer idRow, final NumberFormatSettings nf,
			final int nameColumnType, final AtomicInteger errorCounter_nnum,
			final AtomicInteger errorCounter_neg) {
		ColumnRearranger c = new ColumnRearranger(inSpec);
		// column spec of the appended column
		
		String newColName = inSpec.getColumnSpec(idCol).getName() + " (formatted)";
		newColName = DataTableSpec.getUniqueColumnName(inSpec, newColName);
		
		String leading_char = ((SettingsModelString) getModelSetting(CFG_leadingCharacter)).getStringValue();
		int decimalsSetting = ((SettingsModelIntegerBounded) getModelSetting(CFG_DECIMALS)).getIntValue();

		DataColumnSpec newColSpec = new DataColumnSpecCreator(newColName, StringCell.TYPE)
				.createSpec();

		CellFactory factory = new SingleCellFactory(newColSpec) {
			@Override
			public DataCell getCell(DataRow row) {

				DataCell dcell0 = row.getCell(idCol);
				BigDecimal decimalValue = null;
				// check if value is missing
				if (dcell0.isMissing()) {
					return DataType.getMissingCell(); 
				}
				if (nameColumnType == 1) { // if string data type
					// get value of each row
					String value = ((StringValue) dcell0).getStringValue(); 
					try {
						// check if can be parse to double
						decimalValue = new BigDecimal(value); 
					// if it's not numeric
					} catch (NumberFormatException e) { 
						// increase atomicinteger string not a number
						errorCounter_nnum.incrementAndGet();
						return DataType.getMissingCell(); 
					}
					// check if this string is below zero
					if (decimalValue.compareTo(BigDecimal.ZERO) == -1) { 
						// increase atomicinteger values below zero
						errorCounter_neg.incrementAndGet();
						return DataType.getMissingCell();
					} // make a ? instead negative value
					
				} else { // is numeric
					// get the double value of each row
					double cellVal = ((DoubleValue) row.getCell(idCol)).getDoubleValue();
					decimalValue = new BigDecimal(String.valueOf(cellVal));
					// check if it is more than zero
					if (decimalValue.compareTo(BigDecimal.ZERO) == -1) { 
						// increase atomicinteger values below zero
						errorCounter_neg.incrementAndGet();
						return DataType.getMissingCell();
					}
				}
				
				// round double value if decimal setting available
				if(decimalsSetting != CFG_DECIMALS_DFT) 
				    decimalValue = decimalValue.setScale(decimalsSetting, BigDecimal.ROUND_HALF_UP);
				
				// split number into integer and decimal part (if decimal part available, might not be present)
				String[] splitted = splitNumber(decimalValue);
					
				// set integer part
				String formatted = splitted[0];
				// decimal string or "0" otherwise
				String decimalPart = splitted.length > 1 ? splitted[1] : "0";
			
				if(decimalsSetting != CFG_DECIMALS_DFT && decimalPart.length() > decimalsSetting)
					decimalPart = decimalPart.substring(0, decimalsSetting);
				
				// set decimal part if representation as decimal number
				formatted = nf.asWholeNumber() ? formatted : formatted + "." + decimalPart;
				
				// calculate the number of leading and trailing characters to add
				double addNLeading = nf.getNLeading() - splitted[0].length();
				double addNTrailing = nf.asWholeNumber() ? 0 : nf.getNTrailing() - decimalPart.length();
	
				// add leading and trailing characters
				for (int i = 0; i < addNLeading; i++)
					formatted = leading_char + formatted;

				for (int i = 0; i < addNTrailing; i++)
					formatted = formatted + "0";

				return new StringCell(formatted);
			}
		};
		c.append(factory);
		return c;
	}

	/** Autoguessing */
	private String tryAutoGuessingConcentrationColumn(DataTableSpec tSpec)
			throws InvalidSettingsException {

		// check if "concentration" column available
		if (tSpec.containsName(CFG_ConcentrationColumn_DFT)) {
			if (tSpec.getColumnSpec(CFG_ConcentrationColumn_DFT).getType()
					.isCompatible(DoubleValue.class)) {
				return CFG_ConcentrationColumn_DFT;
			}
		}

		// looking for a lower and upper boundary (how many digits)
		Double smallest_LB = Double.POSITIVE_INFINITY;
		String smallest_LB_Column = null;
		// check if input table has string compatible columns at all
		String firstDoubleCell = null;
		for (String col : tSpec.getColumnNames()) {
			if (tSpec.getColumnSpec(col).getType()
					.isCompatible(DoubleValue.class)) {
				DataColumnDomain domain = tSpec.getColumnSpec(col).getDomain();
				double lowerbound = ((DoubleValue) domain.getLowerBound())
						.getDoubleValue();
				if (lowerbound < smallest_LB) {
					smallest_LB = lowerbound;
					smallest_LB_Column = col;
				}
			}
			if (smallest_LB_Column != null) {
				return smallest_LB_Column;
			}
			firstDoubleCell = col;
			break;
		}

		if (firstDoubleCell == null) {
			throw new InvalidSettingsException(
					"Input table must contain at least one double column");
		}
		return firstDoubleCell;
	}
	
	/**
	 * @param value
	 * @return integer and decimal part (if available) of the value (as strings)
	 */
	private String[] splitNumber(BigDecimal value) {
	
		String numString = value.toPlainString();
		
		// remove trailing 0 or .0 (for integers)
		numString = numString.replaceAll("\\.*0+$","");

		String[] split = numString.split("\\.");

		return split;
	}

}