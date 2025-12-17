package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import de.mpicbg.knime.hcs2.base.node.preproc.numberformatter.NumberFormatterNodeSettings.ExponentNotation;
import de.mpicbg.knime.hcs2.base.node.preproc.numberformatter.NumberFormatterNodeSettings.NumberFormat;
import de.mpicbg.knime.hcs2.base.node.preproc.numberformatter.NumberFormatterNodeSettings.UnitMode;
import de.mpicbg.knime.hcs2.base.utils.DomainUtils;
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
        
        final var formatting = setDecimalFormatting(settings);
        Optional<Integer> n_leadingCharacters = settings.m_leadingCharacterSettings.m_getNLeadingCharsFromDomain ? 
        		Optional.empty() : Optional.of(settings.m_leadingCharacterSettings.m_numberLeadingCharacters); //settings.m_leadingCharacterSettings.m_numberLeadingCharacters;
        
        // value which can only be set if domain values available (and numeric)
        Optional<BigDecimal> maxAbsValue = Optional.empty();
        boolean domainContainsNumbers = false;
        boolean hasKnownNegativeValues = false;	// not used?
        
        var thousandsSep = settings.m_thousandsSeparator.map(sep -> String.valueOf(sep.getValue())).orElse("");
        
		Pattern leadingZerosPattern = Pattern.compile("^[^0-9]*([0" + thousandsSep + "]+)");
		Pattern integerPartPattern = Pattern.compile("([0-9" + thousandsSep + "]+)");
 
        final var domainValues = spec.getColumnSpec(columnIdx).getDomain();
        
        if(DomainUtils.hasDomainValues(spec.getColumnSpec(columnIdx))) {
        	
        	// 1) check if domain contains numbers
        	// 2) find out whether domain contains negative values
        	// 3) get maximum absolute value (important for leading characters)
        	if(isNumericColumn) {
        		final var minDomainValue = BigDecimal.valueOf(((DoubleValue) domainValues.getLowerBound()).getDoubleValue());
        		final var maxDomainValue = BigDecimal.valueOf(((DoubleValue) domainValues.getUpperBound()).getDoubleValue());
        		maxAbsValue = Optional.of((maxDomainValue.abs()).max(minDomainValue.abs()));
        		hasKnownNegativeValues = minDomainValue.compareTo(BigDecimal.ZERO) == -1;
        		domainContainsNumbers = true;
        	} else {
        		final var domainVals = domainValues.getValues();
        		for(DataCell dc : domainVals) {
        			try {
        				var currentNumber = new BigDecimal(((StringCell) dc).getStringValue());
        				if( maxAbsValue.isEmpty() ) {
            				maxAbsValue = Optional.of(currentNumber.abs());
            			} else {
            				var oldAbsMaxValue = maxAbsValue.get();
            				maxAbsValue = Optional.of(oldAbsMaxValue.max(currentNumber.abs()));
            			}
            			if(!hasKnownNegativeValues) {
                			if(currentNumber.compareTo(BigDecimal.ZERO) == -1) {
                				hasKnownNegativeValues = true;
                			}
                		}
            			domainContainsNumbers = true;
        			} catch (NumberFormatException e) {
					}
        			
        		} 		
        	}
        	
        	// set n_leadingCharacters based on domain information
        	if(settings.m_useleadingCharacters && settings.m_leadingCharacterSettings.m_getNLeadingCharsFromDomain && settings.m_Format.equals(NumberFormat.DECIMAL))
        	{
	        	if(!domainContainsNumbers)
	        		throw new InvalidSettingsException("Input column does not provide numeric domain values. Cannot guess number of leading characters."); 	
	        	if(maxAbsValue.isPresent()) {       		
	        		// not too complicated!!! --- I need the precision settings (to avoid wrong rounding of max value!)
	        		
	        		var currentFormatCopy = (DecimalFormat)formatting.clone();	    			
	    			currentFormatCopy.setGroupingUsed(false);
	    			
	    			// if full precision is required
	    			if(settings.m_numberDecimalPlaces.isEmpty()) {
						String pattern = currentFormatCopy.toPattern();
						var scale = maxAbsValue.get().scale();
						if(scale > 0) {
							pattern = pattern + "." + "0".repeat(scale);
							currentFormatCopy.applyPattern(pattern);
						}
					}
	    			
	        		String maxString = new String((currentFormatCopy.format(maxAbsValue.get())));
	        		var decSeparatorIdx = maxString.indexOf(currentFormatCopy.getDecimalFormatSymbols().getDecimalSeparator());
	        		if(decSeparatorIdx >= 0)
	        			maxString = maxString.substring(0, decSeparatorIdx);
	    			//maxString = maxString.replaceAll("-", maxString);
	    			n_leadingCharacters = Optional.of(maxString.length());
	    			
	        	}
        	}
        }
        
        // add leading ZEROS to decimal format if required
        if(settings.m_useleadingCharacters && n_leadingCharacters.isPresent()) {
        	
        	// create replacement pattern
        	BigDecimal patternNumber = new BigDecimal("1" + "0".repeat(n_leadingCharacters.get()));
        	
        	DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
        	dfs.setGroupingSeparator(',');
        	
			DecimalFormat dfLeadingChars = new DecimalFormat("0", dfs);
			dfLeadingChars.setGroupingUsed(formatting.isGroupingUsed());
			dfLeadingChars.setGroupingSize(formatting.getGroupingSize());
			
			String leadingCharsFormat = dfLeadingChars.format(patternNumber).replaceAll("^1,*","");
			
			var currentFormatCopy = (DecimalFormat)formatting.clone();
			var currentPattern = currentFormatCopy.toPattern();
			String patternToReplace = currentPattern;
			
			if( settings.m_Format.equals(NumberFormat.DECIMAL)) {
			
				var splitIdx = currentPattern.indexOf('.');

				if ( splitIdx >= 0 )
					patternToReplace = currentPattern.substring(0, splitIdx);
								
			} 
			if (settings.m_Format.equals(NumberFormat.SCIENTIFIC)) {
		
				var splitIdx = currentPattern.indexOf('E');
				
				// keep exponent to make sure to replace the right part
				patternToReplace = currentPattern.substring(splitIdx, currentPattern.length());
				leadingCharsFormat = 'E' + leadingCharsFormat;
			}
			
			formatting.applyPattern(formatting.toPattern().replace(patternToReplace, leadingCharsFormat));
			// has to be set again, as it might got lost by applying the new pattern
			formatting.setGroupingSize(dfLeadingChars.getGroupingSize());
			formatting.setGroupingUsed(dfLeadingChars.isGroupingUsed());
        }
        
      
        if( replaceInputColumn == NumberFormatterNodeSettings.OutputColumnMode.REPLACE) {
        	DataColumnSpecCreator cspecCreator =  new DataColumnSpecCreator(settings.m_inputColumn, StringCell.TYPE);	
        	rearranger.replace(new NumberFormatterCellFactory(
        			cspecCreator.createSpec(), 
        			columnIdx, 
        			isNumericColumn, 
        			settings, 
        			formatting,
        			leadingZerosPattern,
        			integerPartPattern), 
        			
        			columnIdx);
        }
        if( replaceInputColumn == NumberFormatterNodeSettings.OutputColumnMode.APPEND ) {        	
        	final var uniqueNameGenerator = new UniqueNameGenerator(spec);
        	DataColumnSpec cspec = uniqueNameGenerator.newColumn(settings.m_outputColumnName, StringCell.TYPE);
        	rearranger.append(new NumberFormatterCellFactory(
        			cspec, 
        			columnIdx, 
        			isNumericColumn, 
        			settings, 
        			formatting,
        			leadingZerosPattern,
        			integerPartPattern));
        }
        
        out.setColumnRearranger(rearranger);
		
	}
	
	/**
	 * define a first formatting including separator characters and decimal places
	 * @param settings
	 * @return DecimalFormat with adapted DecimalFormatSymbols
	 */
	private static DecimalFormat setDecimalFormatting(NumberFormatterNodeSettings settings) {
		
		DecimalFormat df = null;
		boolean restrictDecimals = settings.m_numberDecimalPlaces.isPresent();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
		dfs.setDecimalSeparator(settings.m_decimalSeparator.getValue());
		
		if(settings.m_Format == NumberFormatterNodeSettings.NumberFormat.DECIMAL) {
			
			boolean useThousandsSeparator = settings.m_thousandsSeparator.isPresent();			
			if(useThousandsSeparator) dfs.setGroupingSeparator(settings.m_thousandsSeparator.get().getValue());
		
			String format = "#,##0";
			
			if(restrictDecimals) {
				if( settings.m_numberDecimalPlaces.get() > 0 )
					format = format + "." + "0".repeat(settings.m_numberDecimalPlaces.get());
			} // else set number of decimal places for each individual value in the cell factory				
			
			df = new DecimalFormat(format,dfs);
			df.setGroupingUsed(useThousandsSeparator);

		} else {
			
			//if ( !settings.m_exponent.equals(ExponentNotation.EXP_NOTATION_TEN_SUPERSCRIPT) )
			//	dfs.setExponentSeparator(settings.m_exponent.getValue());
			//else
			dfs.setExponentSeparator("E");
	
			String format = "0E0";
			
			if(restrictDecimals) {
				if( settings.m_numberDecimalPlaces.get() > 0 )
					format = "0." + "0".repeat(settings.m_numberDecimalPlaces.get()) + "E0";
			} // else set number of decimal places for each individual value in the cell factory

			
			df = new DecimalFormat(format, dfs);
			df.setGroupingUsed(false);
		}
		
		return df;
	}

	static final class NumberFormatterCellFactory extends SingleCellFactory {
		
		private final int m_columnIdx;		
		private final boolean m_isNumericColumn;
		private final NumberFormatterNodeSettings m_settings;
		private final DecimalFormat m_decimalFormat;
		private final Pattern m_leadingZerosPattern;	// cannot be static, due to thousands separator
		private final Pattern m_integerPartPattern;		// cannot be static, due to thousands separator
		
		public static final Pattern LEADINGZEROS_SCIENTIFIC = Pattern.compile("(E-*0*)[0-9]*$");
		public static final Pattern INTEGERPART_SCIENTIFIC = Pattern.compile("(E-*[0-9]+)$");

		public NumberFormatterCellFactory(DataColumnSpec cspec, int columnIdx, boolean isNumericColumn, NumberFormatterNodeSettings settings, DecimalFormat df, Pattern leadingZerosPattern, Pattern integerPartPattern) {
			super(cspec);
			
			this.m_columnIdx = columnIdx;
			this.m_isNumericColumn = isNumericColumn;
			this.m_settings = settings;
			this.m_decimalFormat = df;
			this.m_leadingZerosPattern = leadingZerosPattern;
			this.m_integerPartPattern = integerPartPattern;
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
			
			boolean isNegative = numberToFormat.compareTo(BigDecimal.ZERO) == -1;
			
			// as there might be individual changes to the pattern, work with a copy
			DecimalFormat localDecimalFormat = (DecimalFormat) m_decimalFormat.clone();
			String formattedNumber = "";

			if(m_settings.m_Format == NumberFormatterNodeSettings.NumberFormat.DECIMAL) {
				
				// if full precision is required
				if(m_settings.m_numberDecimalPlaces.isEmpty()) {
					String pattern = localDecimalFormat.toPattern();
					var scale = numberToFormat.scale();
					if(scale > 0) {
						pattern = pattern + "." + "0".repeat(scale);
						localDecimalFormat.applyPattern(pattern);
					}
				}
				
				formattedNumber = localDecimalFormat.format(numberToFormat);
				
				/*
				 * if a leading character is wanted but not equal to "0" we have to replace the "leading string" with another leading character
				 * thousand separators will no be kept
				 * if the leading string covers the whole integer part (like "00,000.789" or 00,000"), the final zero will be kept
				 * the replacement should not change the length of the string
				 * 
				 */
				if(m_settings.m_useleadingCharacters && 
						!m_settings.m_leadingCharacterSettings.m_leadingCharacter.equals(NumberFormatterNodeSettings.LeadingCharacterGroup.LeadingCharacter.ZERO)) {
					
					Matcher m = m_leadingZerosPattern.matcher(formattedNumber);
					// get first matching group (if any)
					if (m.find()) {
						String leadingString = m.group(1);
						m = m_integerPartPattern.matcher(formattedNumber);
						// should always be true
						if(m.find()) {
							String integerPart = m.group();
							// if there is a zero before the decimal separator (or the number is like "00,000", remove the last zero from the leading string
							if(integerPart.length() == leadingString.length()) {
								leadingString = leadingString.substring(0, integerPart.length() - 1);
							}
						}
						formattedNumber = formattedNumber.replaceFirst(leadingString, String.valueOf(m_settings.m_leadingCharacterSettings.m_leadingCharacter.getValue()).repeat(leadingString.length()) );
				    }
				}
				
				/* add plus sign if reuired and number >= 0 */
				if(m_settings.m_signCharacter.isPresent() && !isNegative) {
					formattedNumber = m_settings.m_signCharacter.get().getValue() + formattedNumber;
				}
				
				
			// SCIENTIFIC format	
			} else {
				
				boolean hasNegativeExponent = numberToFormat.abs().compareTo(BigDecimal.ONE) < 0 && numberToFormat.abs().compareTo(BigDecimal.ZERO) > 0;
				
				// if full precision is required
				if(m_settings.m_numberDecimalPlaces.isEmpty()) {
					String pattern = localDecimalFormat.toPattern();
					var precision = numberToFormat.precision() - 1;
					if(precision > 0) {
						pattern = "0." + "0".repeat(precision - 1) + pattern;
						localDecimalFormat.applyPattern(pattern);
					}
				}
				
				// format number
				formattedNumber = localDecimalFormat.format(numberToFormat);
		
				// fix leading zeros if something else than zero is requested
				if(m_settings.m_useleadingCharacters && 
						!m_settings.m_leadingCharacterSettings.m_leadingCharacter.equals(NumberFormatterNodeSettings.LeadingCharacterGroup.LeadingCharacter.ZERO)) {
					Matcher m = LEADINGZEROS_SCIENTIFIC.matcher(formattedNumber);
					// get first matching group (if any)
					if (m.find()) {
						String leadingString = m.group(1);
						m = INTEGERPART_SCIENTIFIC.matcher(formattedNumber);
						// should always be true
						if(m.find()) {
							String integerPart = m.group();
							
							String expNotation = hasNegativeExponent ? "E-" : "E";
							int leadingStringLength = leadingString.length();
							
							// keep last zero in cases like 1.0E000
							if(integerPart.length() == leadingStringLength) {
								leadingString = leadingString.substring(0, integerPart.length() - 1);
								leadingStringLength = leadingString.length(); // update
							}
							
							// substract 1 for E 
							// and 1 for '-' (if present)
							int n = hasNegativeExponent ? leadingStringLength -2 : leadingStringLength -1;
							formattedNumber = formattedNumber.replaceFirst(leadingString, expNotation + String.valueOf(m_settings.m_leadingCharacterSettings.m_leadingCharacter.getValue()).repeat(n) );
						}
				    }
				}
				
				/* add plus sign if required and number OR exponent >= 0 */
				if(m_settings.m_signCharacter.isPresent()) {
					if(!isNegative) {
						formattedNumber = m_settings.m_signCharacter.get().getValue() + formattedNumber;
					}
					if(!hasNegativeExponent) {
						formattedNumber = formattedNumber.replaceFirst("E", "E" + m_settings.m_signCharacter.get().getValue() );
					}
				}
	
				// fix exponent notation
				if(!m_settings.m_exponent.equals(ExponentNotation.EXP_NOTATION_CAPITAL_E)) {
					
					if(m_settings.m_exponent.equals(ExponentNotation.EXP_NOTATION_TEN_SUPERSCRIPT)) {
						
						String exponent = formattedNumber.substring(formattedNumber.indexOf('E'));
						String newExponent = createSuperscript(exponent);
						formattedNumber = formattedNumber.replace(exponent, newExponent);	
						formattedNumber = formattedNumber.replace("E", ExponentNotation.EXP_NOTATION_TEN_SUPERSCRIPT.getValue());
					} else {
						
						formattedNumber = formattedNumber.replace("E", m_settings.m_exponent.getValue());
					}
				}

							
			}
			
			/*
			 * add unit
			 * 
			 * - either AFTER the number
			 * - otherwise BEFORE, check if it's a negative number or does contain a space is first character
			 * 		- if yes, put the unit in between the minus/space and the number
			 * 		- otherwise put it before the number
			 */
			if(m_settings.m_useUnit) {
				if(m_settings.m_unitMode.equals(UnitMode.AFTER))
					formattedNumber = formattedNumber + m_settings.m_unit;
				else {
					switch(formattedNumber.charAt(0)) {
					case '-': 
						formattedNumber = "-" + m_settings.m_unit + formattedNumber.substring(1);
						break;
					case ' ':
						formattedNumber = " " + m_settings.m_unit + formattedNumber.substring(1);
						break;
					default:
						formattedNumber = m_settings.m_unit + formattedNumber;
					}
				}
			}
			
			//return new StringCell(formattedNumber + " (" + localDecimalFormat.toPattern() + ")");
			return new StringCell(formattedNumber);

		}

		private String createSuperscript(String exponent) {
			
			Map<String ,String> lut = new HashMap<String, String>();
			lut.put("0", "\u2070" );
			lut.put("1", "\u00B9");
			lut.put("2", "\u00B2");
			lut.put("3", "\u00B3");
			lut.put("4", "\u2074");
			lut.put("5", "\u2075");
			lut.put("6", "\u2076");
			lut.put("7", "\u2077");
			lut.put("8", "\u2078");
			lut.put("9", "\u2079");
			lut.put("-", "\u207B");
			lut.put("+", "\u207A");
			
			for(String key : lut.keySet()) {
				exponent = exponent.replace(key, lut.get(key));
			}
			
			return exponent;
		}
	}
	
	
	static void validateSettings(NumberFormatterNodeSettings settings, DataTableSpec spec) 
	throws InvalidSettingsException {
		
		/*
		 * String 						m_inputColumn						validate
		 * 
		 * NumberFormat 				m_Format							no need to validate (not null, Enum)
		 * Optional<ThousandsSeparator> m_thousandsSeparator				validate, cannot be equal to decimal separator, can be null, if settings not required	
		 * ExponentNotation 			m_exponent							no need to validate (not null, Enum)
		 * DecimalSeparator 			m_decimalSeparator					validate, cannot be equal to thousands separator (not null, Enum)
		 * Optional<Integer> 			m_numberDecimalPlaces				no need to validate, can be null, if settings not required	
		 * 
		 * boolean						m_useleadingCharacters				no need to validate
		 * LeadingCharacterGroup 		m_leadingCharacterSettings
		 * LeadingCharacter 				m_leadingCharacter				no need to validate (not null, Enum)
		 * boolean 							m_getNLeadingCharsFromDomain	validate, only possible if domain available
		 * int 								m_numberLeadingCharacters		no need to validate (non-negative numbers only accepted)
		 * 
		 * Optional<SignCharacter> 		m_signCharacter						no need to validate (not null, Enum)
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
        
        // check if decimal separator and thousands separator are not the same
        if ( settings.m_Format.equals(NumberFormat.DECIMAL) && Optional.ofNullable(settings.m_thousandsSeparator)
        	.filter(sep -> !sep.isEmpty())
        	.filter(sep -> (settings.m_decimalSeparator.getValue() == sep.get().getValue()))
        	.isPresent() )
        	throw new InvalidSettingsException("Decimal Separator and Thousand Separator cannot be the same. Please select different separators.");
        
        if( settings.m_useleadingCharacters && settings.m_leadingCharacterSettings.m_getNLeadingCharsFromDomain) {
        	//DataColumnDomain cd = spec.getColumnSpec(settings.m_inputColumn).getDomain();
        	if( !DomainUtils.hasDomainValues(spec.getColumnSpec(settings.m_inputColumn)) )
        		throw new InvalidSettingsException("Input column does not provide domain values. Cannot guess number of leading characters.");
        }
        
        if ( settings.m_columnMode == NumberFormatterNodeSettings.OutputColumnMode.APPEND) {
        	// check if output column name is valid
    		ColumnNameValidationUtils.validateColumnName(settings.m_outputColumnName, new ColumnNameValidationMessageBuilder("output column name").build()); 	    			
        }
        
	}

}
