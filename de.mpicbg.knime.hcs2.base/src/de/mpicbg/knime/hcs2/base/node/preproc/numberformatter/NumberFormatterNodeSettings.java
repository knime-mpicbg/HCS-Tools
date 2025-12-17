package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.OptionalWidget.DefaultValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.message.TextMessage;
import org.knime.node.parameters.widget.message.TextMessage.Message;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;


final class NumberFormatterNodeSettings implements NodeParameters {
	
	/*
	 * ============================================ Sections ==================================================
	 */

	interface DialogSections {
		
		@Section(title = "Notation")
		interface Notation {
		}
		
		@Section(title = "Leading Characters")
		@After(Notation.class)
		interface LeadingCharacters {
			@HorizontalLayout
			interface SetNumberOf {
			}
		}
		
		@Section(title = "Signs")
		@After(LeadingCharacters.class)
		interface Signs {
		}
		
		@Section(title = "Unit")
		@After(Signs.class)
		interface Unit {
			
			@HorizontalLayout
		    interface UnitSettingsLayout {
		    }
		}
		
		@Section(title = "Output")
		@After(Unit.class)
		@Advanced
		interface Output {
		}
	}
	
	/*
	 * ============================================ UI Elements ==================================================
	 */

	/**
	 * input column selection
	 * 
	 * assumptions to test before execution
	 * - not null
	 * - available in input table
	 * - compatible to string or double
	 * 
	 * Note: can be null (no compatible column available or settings applied without input) 	 
	 **/
	@Widget(title = "Column to format", description = "Choose the column containing numbers to format.")
	@ValueReference(value = ColumnNameReference.class)
	@ChoicesProvider(DoubleStringColumnsProvider.class)
	/** setting - choice of input column */
	String m_inputColumn;
	
	interface ColumnNameReference extends ParameterReference<String> {
    }
	
	/*
	 * ### Notation - Section ###
	 */
	
	@Layout(DialogSections.Notation.class)
	@ValueReference(value = NotationReference.class)
	@Widget(title = "Notation", description = "Numbers can be formatted either using decimal notation or scientific notation.")
	NumberFormat m_Format = NumberFormat.DECIMAL;
	
	enum NumberFormat {
		@Label("decimal")
        DECIMAL,

        @Label("scientific")
        SCIENTIFIC;
	}
	
	interface NotationReference extends ParameterReference<NumberFormat> {
    }
	
	/* --- */
	
	@Layout(DialogSections.Notation.class)
	@TextMessage(SameSeparatorWarning.class)
	Void m_sameColumnWarning;
	static final class SameSeparatorWarning implements StateProvider<Optional<TextMessage.Message>> {
		
		Supplier<DecimalSeparator> m_decimalSep;
        Supplier<Optional<ThousandsSeparator>> m_thousandsSep;
        Supplier<NumberFormat> m_format;
        //boolean m_noNeedToCompare;

		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_decimalSep = initializer.computeFromValueSupplier(DecimalSeparatorReference.class);
			m_thousandsSep = initializer.computeFromValueSupplier(ThousandsSeparatorReference.class);
			m_format = initializer.computeFromValueSupplier(NotationReference.class);
		}

		@Override
		public Optional<Message> computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
			
			final var decSep = m_decimalSep.get();
            final var thousSep = m_thousandsSep.get();
            
            if (m_format.get().equals(NumberFormat.SCIENTIFIC))
            	return Optional.empty();
            
            return Optional.ofNullable(thousSep)
            	    .filter(sep -> !sep.isEmpty())
            	    .filter(sep -> (decSep.getValue() == sep.get().getValue()))
            	    .map(sep -> new TextMessage.Message(
            	        "Same separators selected.",
            	        "Decimal Separator and Thousand Separator cannot be the same. Please select different separators.",
            	        TextMessage.MessageType.ERROR
            	    ));
		}
	}
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Thousands Separator", description = "If selected, choose a separator character used for digit grouping. Otherwise, no digit grouping is applied.")
	@OptionalWidget(defaultProvider = ThousandsSeparatorDefaultProvider.class)
	@Effect(predicate = ShowThousandsSeparator.class, type = EffectType.SHOW)
	@ValueReference(value = ThousandsSeparatorReference.class)
	@ChoicesProvider(AvailableSeparatorsProvider.class)
	Optional<ThousandsSeparator> m_thousandsSeparator = Optional.empty();

	static final class ThousandsSeparatorDefaultProvider implements DefaultValueProvider<ThousandsSeparator> {

		@Override public ThousandsSeparator computeState(final NodeParametersInput
				context) throws StateComputationFailureException { return
						ThousandsSeparator.COMMA; }

	}
	
	static final class AvailableSeparatorsProvider implements EnumChoicesProvider<ThousandsSeparator> {
	    @Override
	    public List<ThousandsSeparator> choices(final NodeParametersInput context) {
	        return Arrays.asList(ThousandsSeparator.values());
	    }
	}

	interface ThousandsSeparatorReference extends ParameterReference<Optional<ThousandsSeparator>> {
	}
	
	static final class ShowThousandsSeparator implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getEnum(NotationReference.class).isOneOf(NumberFormat.DECIMAL);
		}
	}
	
	enum ThousandsSeparator {
        @Label(". (full stop)")
        FULL_STOP ('.'),

        @Label(", (comma)")
        COMMA (','),
        
        @Label("\u2009  (thin space)")
        SPACE ('\u2009'),
        
        @Label("' (apostrophe)")
        APOSTROPHE ('\'');
        
        private final char value;       

	    private ThousandsSeparator(char s) {
	        value = s;
	    }

	    public char getValue() {
	    	return value;
	    }
	}
	
	/* --- */
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Exponent", description = "Select which notation should be used")
	@RadioButtonsWidget()
	@Effect(predicate = ShowExponentNotation.class, type = EffectType.SHOW)
	@ValueReference(value = ExponentNotationReference.class)
	ExponentNotation m_exponent = ExponentNotation.EXP_NOTATION_CAPITAL_E;
	
	enum ExponentNotation {
		@Label("E (1.5E-2)")
		EXP_NOTATION_CAPITAL_E ("E"),
		
		@Label("e (1.5e-2)")
		EXP_NOTATION_SMALL_E ("e"),
		
		@Label("x10^ (1.5x10^-2)")
		EXP_NOTATION_TEN ("x10^"),
		
		@Label("\u00D7 10 (1.5 \u00D7 10\u207b\u00b2)")
		EXP_NOTATION_TEN_SUPERSCRIPT (" \u00D7 10");
		
		private final String value;       

	    private ExponentNotation(String s) {
	        value = s;
	    }

	    public String getValue() {
	    	return value;
	    }
	}
	
	interface ExponentNotationReference extends ParameterReference<ExponentNotation> {
	}
	
	static final class ShowExponentNotation implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getEnum(NotationReference.class).isOneOf(NumberFormat.SCIENTIFIC);
		}
	}
	

	
	/* --- */
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Decimal Separator", description = "Select a character which should be used as decimal separator")
	@ValueReference(value = DecimalSeparatorReference.class)
	DecimalSeparator m_decimalSeparator = DecimalSeparator.FULL_STOP;
	
	enum DecimalSeparator {
		@Label(". (full stop)")
        FULL_STOP ('.'),

        @Label(", (comma)")
        COMMA (',');
		
		private final char value;       

	    private DecimalSeparator(char s) {
	        value = s;
	    }

	    public char getValue() {
	    	return value;
	    }
	    
	}
	
	interface DecimalSeparatorReference extends ParameterReference<DecimalSeparator> {
	}
	
	/* --- */
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Restrict number of decimal places", description = "If selected, numbers are rounded to a given number of decimals (choose 0 for reoresenation as whole number). Otherwise, numbers are formatted with full precision.")
	@OptionalWidget(defaultProvider = DecimalPlacesDefaultProvider.class)
	@NumberInputWidget(minValidation = IsNonNegativeValidation.class)
	Optional<Integer> m_numberDecimalPlaces = Optional.empty();
	
	static final class DecimalPlacesDefaultProvider implements DefaultValueProvider<Integer> {

		@Override
		public Integer computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
			return 1;
		}	
	}
	
	/*
	 * ### Leading Characters - Section ###
	 */
	
	@Layout(DialogSections.LeadingCharacters.class)	
	@Widget(title = "Add leading characters", description = "If selected, leading characters will be added.")
	@ValueReference(SetLeadingCharacters.class)
	boolean m_useleadingCharacters = false;
	
	static final class SetLeadingCharacters implements BooleanReference {
    }

	LeadingCharacterGroup m_leadingCharacterSettings = new LeadingCharacterGroup();
	
	// TODO: if it becomes possible effects in the section (autoguess and number of leading chars) should be used
	// at the moment they would overwrite the section effect
	@Layout(DialogSections.LeadingCharacters.class)
	@Effect(predicate = SetLeadingCharacters.class, type = EffectType.SHOW)
	static class LeadingCharacterGroup implements NodeParameters {
		
		@Layout(DialogSections.LeadingCharacters.class)
		@Widget(title = "Leading character", description = "Choose the leading character which should be used.")
		LeadingCharacter m_leadingCharacter = LeadingCharacter.ZERO;
		
		enum LeadingCharacter {
			
			@Label("0 (zero)")
			ZERO ('0'),
			
			@Label("  (space)")
	        SPACE (' '),

	        @Label("_ (underscore)")
	        UNDERSCORE ('_');
			
			private final char value;       

		    private LeadingCharacter(char s) {
		        value = s;
		    }

		    public char getValue() {
		    	return value;
		    }
		}
		
		/* --- */
		
		@ValueProvider(InputColumnHasDomainValuesProvider.class)
		@ValueReference(InputColumnHasDomainValuesReference.class)
		@Persistor(InputColumnHasDomainValuesPersistor.class)
		boolean m_inputColumnHasDomainValues;
		
		interface InputColumnHasDomainValuesReference extends BooleanReference {			
		}
		
		static final class InputColumnHasDomainValuesProvider implements StateProvider<Boolean> {
			
			private Supplier<String> m_selectedColumnSupplier;


			@Override
			public void init(StateProviderInitializer initializer) {
				initializer.computeAfterOpenDialog();
				initializer.computeFromValueSupplier(ColumnNameReference.class);
	            m_selectedColumnSupplier = initializer.getValueSupplier(ColumnNameReference.class);
			}

			@Override
			public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
				// input column not set
				if (m_selectedColumnSupplier.get() == null || m_selectedColumnSupplier.get().isEmpty())
					return Boolean.FALSE;
				// input column not available in tables specs	
				if( parametersInput.getInTableSpec(0).get().getColumnSpec(m_selectedColumnSupplier.get()) == null )
					return Boolean.FALSE;
				
				boolean hasBounds = parametersInput.getInTableSpec(0).get().getColumnSpec(m_selectedColumnSupplier.get()).getDomain().hasBounds();
				boolean hasDomainValues = parametersInput.getInTableSpec(0).get().getColumnSpec(m_selectedColumnSupplier.get()).getDomain().hasValues();
					
				return Boolean.valueOf(hasBounds || hasDomainValues);
				
			}
			
		}
		
		static final class InputColumnHasDomainValuesPersistor implements NodeParametersPersistor<Boolean> {

			@Override
			public Boolean load(NodeSettingsRO settings) throws InvalidSettingsException {
				return false;
			}

			@Override
			public void save(Boolean param, NodeSettingsWO settings) {
			}

			@Override
			public String[][] getConfigPaths() {
				return new String[0][];
			}	
		}
		
		/* --- */
		
		@Layout(DialogSections.LeadingCharacters.SetNumberOf.class)
		@Widget(title = "autoguess from domain values", description = "If domain values are available, the number of leading characters can be set automatically (only for decimal notation)")
		@ValueReference(value = AutosetLeadingCharsReference.class)
		//@Effect(predicate = InputColumnHasDomainValuesPredicate.class, type = EffectType.ENABLE)		// currently the effect overwrites effects of the section
		boolean m_getNLeadingCharsFromDomain = false;
		
		static final class InputColumnHasDomainValuesPredicate implements EffectPredicateProvider {

			@Override
			public EffectPredicate init(PredicateInitializer i) {
				return i.getBoolean(InputColumnHasDomainValuesReference.class).isTrue();
			}
			
		}
		
		interface AutosetLeadingCharsReference extends BooleanReference {	
		}
		
		/* --- */
		
		@Layout(DialogSections.LeadingCharacters.SetNumberOf.class)
		@Widget(title = "Number of leading characters", description = "Sets a fixed number of digits left from the decimal separator. If the number requires less digits, leading characters are addded. If the number requires more digits this setting is ignored.")
		@NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
		//@Effect(predicate = AutosetLeadingCharsPredicate.class, type = EffectType.ENABLE)		// currently the effect overwrites effects of the section
		int m_numberLeadingCharacters = 1;
		
		static final class AutosetLeadingCharsPredicate implements EffectPredicateProvider {

			@Override
			public EffectPredicate init(PredicateInitializer i) {
				return i.getBoolean(AutosetLeadingCharsReference.class).isTrue();
			}
			
		}		
	}
	
	/*
	 * ### Signs - Section ###
	 */
	
	@Layout(DialogSections.Signs.class)
	@Widget(title = "Add sign for positive numbers", description = "If selected, numbers >= 0 will get the selected sign character (also applies to positive exponents)")
	@OptionalWidget(defaultProvider = DefaultSignProvider.class)
	@ChoicesProvider(AvailableSignsProvider.class)
	Optional<SignCharacter> m_signCharacter = Optional.empty();
	
	enum SignCharacter {
		@Label("  (space)")
        SPACE (' '),

        @Label("+ (plus)")
        PLUS ('+');
		
		private final char value;       

	    private SignCharacter(char s) {
	        value = s;
	    }

	    public char getValue() {
	    	return value;
	    }
	    
	}
	
	
	static final class AvailableSignsProvider implements
	EnumChoicesProvider<SignCharacter> {

		@Override 
		public List<SignCharacter> choices(final NodeParametersInput context) { 
			return Arrays.asList(SignCharacter.values()); 
		} 
	}

	static final class DefaultSignProvider implements DefaultValueProvider<SignCharacter> {

		@Override 
		public SignCharacter computeState(NodeParametersInput arametersInput) throws StateComputationFailureException { 
			return SignCharacter.PLUS; 
		} 
	}
	 
	
	/*
	 * ### Unit - Section ###
	 */
	
	@Layout(DialogSections.Unit.class)	
	@Widget(title = "Add unit", description = "If selected, a unit will be added.")
	@ValueReference(SetUnit.class)
	boolean m_useUnit = false;
	
	static final class SetUnit implements BooleanReference {
    }

	/* --- */
	
	@Layout(DialogSections.Unit.UnitSettingsLayout.class)
	@Widget(title = "Location", description = "Choose if the unit should be placed as prefix or suffix")
	@ValueSwitchWidget
	@Effect(predicate = SetUnit.class, type = EffectType.SHOW)
	UnitMode m_unitMode = UnitMode.AFTER;

	enum UnitMode {
		@Label(value = "Before", description = "Unit as prefix")
		BEFORE,

		@Label(value = "After", description = "Unit as suffix")
		AFTER;
	}
	
	/* --- */
	
	@Layout(DialogSections.Unit.UnitSettingsLayout.class)
	@Widget(title = "Unit", description = "Provide a unit which should be added.")
	@TextInputWidget()
	@Effect(predicate = SetUnit.class, type = EffectType.SHOW)
	String m_unit = " µM";
	
	/*
	 * ### Output - Section ###
	 */
	
	/** setting - new column or replace input column */
	@Layout(DialogSections.Output.class)
	@Widget(title = "Output Mode", description = "Node output can either replace the input column or a new column is appended")
	@ValueSwitchWidget
	@ValueReference(StandardRef.class)
	OutputColumnMode m_columnMode = OutputColumnMode.REPLACE;

	enum OutputColumnMode {
		@Label(value = "Replace", description = "Replace the input columns with node output (column data type will change)")
		REPLACE,

		@Label(value = "Append", description = "Append a new column with node output")
		APPEND;
	}
	
	interface StandardRef extends ParameterReference<OutputColumnMode> { }

	/** setting - output column name **/
	@Layout(DialogSections.Output.class)
	@Widget(title = "Output Column Name", description = "Name of the created output column. Only available if output mode is `Append`.")
	@Effect(predicate = OutputColumnIsAppend.class, type = EffectType.SHOW)
	@TextInputWidget(patternValidation = ColumnNameValidationUtils.ColumnNameValidation.class )
	String m_outputColumnName = "Input column (formatted)";

	static final class OutputColumnIsAppend implements EffectPredicateProvider {

		@Override public EffectPredicate init(final PredicateInitializer i) { 
			return i.getEnum(StandardRef.class).isOneOf(OutputColumnMode.APPEND); }
	}	
	
	/* ============================================================================================== */

	
    public NumberFormatterNodeSettings() {
    	this((DataTableSpec)null);
	}
    
	NumberFormatterNodeSettings(final NodeParametersInput context) {
        this(context.getInTableSpec(0).orElse(null));
    }
	
	NumberFormatterNodeSettings(final DataTableSpec spec) {
		// empty (no columns) table
        if (spec == null) {
            return;
        }

        // get all compatible columns for plate row column
        List<DataColumnSpec> columnList = ColumnSelectionUtil.getCompatibleColumns(spec, DoubleStringColumnsProvider.getValueClassesList());
        
        // no compatible columns 
        if(columnList.isEmpty())
        	return;
        
        /* 
         * from compatible columns:
         * use first column in list
         */
        m_inputColumn = columnList.get(0).getName();

        /* 
         * suggest new column name
         */
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
    	m_outputColumnName = uniqueNameGenerator.newName(m_inputColumn + " (formatted)");
        
    }
}
