package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.ColumnNameValidationMessageBuilder;
import org.knime.node.DefaultModel.RearrangeColumnsInput;
import org.knime.node.DefaultModel.RearrangeColumnsOutput;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsColumnNotFoundException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsMissingSettingException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsWrongDataTypeException;

final class NumberFormatterNodeModel {
	
	static void rearrangeColumns(final RearrangeColumnsInput in, final RearrangeColumnsOutput out)
	        throws InvalidSettingsException {
		
		final var spec = in.getDataTableSpec();
        final var settings = in.<NumberFormatterNodeSettings> getParameters();
        final var rearranger = new ColumnRearranger(spec);
        
        NumberFormatterNodeModel.validateSettings(settings, spec);
        
        final var replaceInputColumn = settings.m_columnMode;      
        final var columnIdx = spec.findColumnIndex(settings.m_inputColumn); 
        final var isNumericColumn = spec.getColumnSpec(columnIdx).getType().isCompatible(DoubleValue.class);  
        
        if( replaceInputColumn == NumberFormatterNodeSettings.OutputColumnMode.REPLACE) {
        	DataColumnSpecCreator cspecCreator =  new DataColumnSpecCreator(settings.m_inputColumn, StringCell.TYPE);	
        	rearranger.replace(new NumberFormatterCellFactory(cspecCreator.createSpec(), columnIdx, isNumericColumn, settings), columnIdx);
        }
        if( replaceInputColumn == NumberFormatterNodeSettings.OutputColumnMode.APPEND ) {        	
        	final var uniqueNameGenerator = new UniqueNameGenerator(spec);
        	DataColumnSpec cspec = uniqueNameGenerator.newColumn(settings.m_outputColumnName, StringCell.TYPE);
        	rearranger.append(new NumberFormatterCellFactory(cspec, columnIdx, isNumericColumn, settings));
        }
        
        out.setColumnRearranger(rearranger);
		
	}
	
	static final class NumberFormatterCellFactory extends SingleCellFactory {
		
		private final int m_columnIdx;		
		private final boolean m_isNumericColumn;
		private final NumberFormatterNodeSettings m_settings;

		public NumberFormatterCellFactory(DataColumnSpec cspec, int columnIdx, boolean isNumericColumn, NumberFormatterNodeSettings settings) {
			super(cspec);
			
			this.m_columnIdx = columnIdx;
			this.m_isNumericColumn = isNumericColumn;
			this.m_settings = settings;
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			
			var outCell =  DataType.getMissingCell();
			
			final var inCell = row.getCell(m_columnIdx);
			
			if (inCell.isMissing() ) 
				return outCell;
			
			// 1) convert cell content to double number
			
			BigDecimal numberToFormat;
			
			if(m_isNumericColumn) {
				double dval = ((DoubleValue) inCell).getDoubleValue();
				
				if(Double.isInfinite(dval)) {
					if(dval < 0)
						return new StringCell("-Infinity");
					else
						return new StringCell("Infinity");
				}
				if(Double.isNaN(dval)) {
					return new StringCell("NaN");
				}				
				numberToFormat = BigDecimal.valueOf(dval);
			}
			else {
				String inputString = ((StringCell) inCell).getStringValue();				
				try {
					numberToFormat = new BigDecimal(inputString);
				} catch(NumberFormatException nfe) {
					return outCell;
				}
			}

			if(m_settings.m_Format == NumberFormatterNodeSettings.NumberFormat.STANDARD) {
				boolean restrictDecimals = m_settings.m_numberDecimalPlaces.isPresent();
				
				int n_leadingCharacters = m_settings.m_leadingCharacterSettings.m_numberLeadingCharacters;
				
				String precision = "";
				
				if(restrictDecimals)
					precision = "0".repeat(m_settings.m_numberDecimalPlaces.get());
				
				DecimalFormat df = new DecimalFormat("#." + precision);
				
				return new StringCell(df.format(numberToFormat) + " (" + df.toPattern() + ")");
					
			}
			
			return outCell;
		}
	}
	
	
	static void validateSettings(NumberFormatterNodeSettings settings, DataTableSpec spec) 
	throws InvalidSettingsException {
		
		/*
		 * String 						m_inputColumn						validate
		 * 
		 * NumberFormat 				m_Format							no need to validate (not null, Enum)
		 * Optional<ThousandsSeparator> m_thousandsSeparator				no need to validate, can be null, if settings not required	
		 * DecimalSeparator 			m_decimalSeparator					no need to validate (not null, Enum)
		 * Optional<Integer> 			m_numberDecimalPlaces				no need to validate, can be null, if settings not required	
		 * 
		 * boolean						m_useleadingCharacters				no need to validate
		 * LeadingCharacterGroup 		m_leadingCharacterSettings
		 * LeadingCharacter 				m_leadingCharacter				no need to validate (not null, Enum)
		 * boolean 							m_getNLeadingCharsFromDomain	no need to validate
		 * int 								m_numberLeadingCharacters		no need to validate (non-negative numbers only accepted)
		 * 
		 * boolean 						m_useUnit							no need to validate
		 * UnitMode 					m_unitMode							no need to validate (not null, Enum)
		 * String 						m_unit								no need to validate
		 * 
		 * OutputColumnMode 			m_columnMode						no need to validate (not null, Enum)
		 * String 						m_outputColumnName					validate
		 */
		
		
		// check if input column is set at all
        final var selectedColumnIdx = Optional.ofNullable(settings.m_inputColumn)
        		.map(columnName -> spec.findColumnIndex(columnName))
        		.orElseThrow(() -> new InvalidSettingsMissingSettingException("Column to format"));
        
        // check if input column exists in input table
        if ( selectedColumnIdx < 0 ) {
        	throw new InvalidSettingsColumnNotFoundException(settings.m_inputColumn);
        }
        
        // check if data type of input column is compatible
        if ( !DoubleStringColumnsProvider.isCompatible(spec.getColumnSpec(selectedColumnIdx)) )
        	throw new InvalidSettingsWrongDataTypeException(settings.m_inputColumn);
        
        
        if ( settings.m_columnMode == NumberFormatterNodeSettings.OutputColumnMode.APPEND) {
        	// check if output column name is valid
    		ColumnNameValidationUtils.validateColumnName(settings.m_outputColumnName, new ColumnNameValidationMessageBuilder("output column name").build()); 	    			
        }
        
	}

}
