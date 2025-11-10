package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import java.util.Optional;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
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
		
	}
	
	static void validateSettings(NumberFormatterNodeSettings settings, DataTableSpec spec) 
	throws InvalidSettingsException {
		
		/*
		 * String 						m_inputColumn						validate
		 * 
		 * NumberFormat 				m_Format							no need to validate (not null, Enum)
		 * Optional<ThousandsSeparator> m_thousandsSeparator				no need to validate, can be null, if settings not required	
		 * DecimalSeparator 			m_decimalSeparator					no need to validate (not null, Enum)
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
