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
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;
import de.mpicbg.knime.hcs2.core.TDSUtils;


final class NumberFormatterNodeSettings implements NodeParameters {
	
	/*
	 * ============================================ Sections ==================================================
	 */

	interface DialogSections {
		
		@Section(title = "Notation")
		interface Notation {
			
			@HorizontalLayout
		    interface SeparatorsLayout {
		    }	
		}
		
		@Section(title = "Leading Characters")
		@After(Notation.class)
		interface LeadingCharacters {
			@HorizontalLayout
			interface SetNumberOf {
			}
		}
		
		@Section(title = "Unit")
		@After(LeadingCharacters.class)
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
	@Widget(title = "Column to format", description = "...")
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
	@Widget(title = "Format", description = "...")
	NumberFormat m_Format = NumberFormat.STANDARD;
	
	enum NumberFormat {
		@Label("standard (0.01)")
        STANDARD,

        @Label("scientific (1.0 × 10\u207b\u00b2)")
        SCIENTIFIC,
        
        @Label("e (1.0E-2)")
		E_FORMAT2,
        
        @Label("E (1.0e-2)")
		E_FORMAT1;
	}
	
	/* --- */
	
	@Layout(DialogSections.Notation.SeparatorsLayout.class)
	@Widget(title = "Thousands Separator", description = "...")
	@OptionalWidget(defaultProvider = ThousandsSeparatorDefaultProvider.class)
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

	 
	
	enum ThousandsSeparator {
        @Label(". (full stop)")
        FULL_STOP,

        @Label(", (comma)")
        COMMA,
        
        @Label("  (space)")
        SPACE,
        
        @Label("' (apostrophe)")
        APOSTROPHE;
	}
	
	/* --- */
	
	@Layout(DialogSections.Notation.SeparatorsLayout.class)
	@Widget(title = "Decimal Separator", description = "...")
	//@ValueSwitchWidget
	DecimalSeparator m_decimalSeparator = DecimalSeparator.FULL_STOP;
	
	enum DecimalSeparator {
		@Label(". (full stop)")
        FULL_STOP,

        @Label(", (comma)")
        COMMA;
	}
	
	/*
	 * ### Leading Characters - Section ###
	 */
	
	@Layout(DialogSections.LeadingCharacters.class)	
	@Widget(title = "Add leading characters", description = "...")
	@ValueReference(SetLeadingCharacters.class)
	boolean m_useleadingCharacters = false;
	
	static final class SetLeadingCharacters implements BooleanReference {
    }
	
	//@Persistor(TestPersistor.class)
	LeadingCharacterGroup tg = new LeadingCharacterGroup();
	
	@Layout(DialogSections.LeadingCharacters.class)
	@Effect(predicate = SetLeadingCharacters.class, type = EffectType.ENABLE)
	static class LeadingCharacterGroup implements NodeParameters {
		
		@Layout(DialogSections.LeadingCharacters.class)
		@Widget(title = "Leading character", description = "...")
		LeadingCharacter m_leadingCharacter = LeadingCharacter.ZERO;
		
		enum LeadingCharacter {
			
			@Label("0 (zero)")
			ZERO,
			
			@Label("  (space)")
	        SPACE,

	        @Label("_ (underscore)")
	        UNDERSCORE;
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
	            m_selectedColumnSupplier = initializer.getValueSupplier(ColumnNameReference.class);
			}

			@Override
			public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
				if (m_selectedColumnSupplier.get() == null || m_selectedColumnSupplier.get().isEmpty()) {
				
				} else {
					boolean hasBounds = parametersInput.getInTableSpec(0).get().getColumnSpec(m_selectedColumnSupplier.get()).getDomain().hasBounds();
					boolean hasDomainValues = parametersInput.getInTableSpec(0).get().getColumnSpec(m_selectedColumnSupplier.get()).getDomain().hasValues();
					
					return Boolean.valueOf(hasBounds || hasDomainValues);
				}
				return Boolean.FALSE;
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
		@Widget(title = "autoguess from domain values", description = "...")
		@ValueReference(value = AutosetLeadingCharsReference.class)
		//@Effect(predicate = InputColumnHasDomainValuesPredicate.class, type = EffectType.ENABLE)
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
		@Widget(title = "Number of leading characters", description = "...")
		@NumberInputWidget(minValidation = IsNonNegativeValidation.class)
		//@Effect(predicate = AutosetLeadingCharsPredicate.class, type = EffectType.HIDE)
		int m_numberLeadingCharacters = 0;
		
		static final class AutosetLeadingCharsPredicate implements EffectPredicateProvider {

			@Override
			public EffectPredicate init(PredicateInitializer i) {
				return i.getBoolean(AutosetLeadingCharsReference.class).isTrue();
			}
			
		}		
	}
	

	


	
	/*
	 * ### Unit - Section ###
	 */
	
	@Layout(DialogSections.Unit.class)	
	@Widget(title = "Add unit", description = "...")
	@ValueReference(SetUnit.class)
	boolean m_useUnit = false;
	
	static final class SetUnit implements BooleanReference {
    }

	/* --- */
	
	@Layout(DialogSections.Unit.UnitSettingsLayout.class)
	@Widget(title = "Location", description = "...")
	@ValueSwitchWidget
	@Effect(predicate = SetUnit.class, type = EffectType.ENABLE)
	UnitMode m_unitMode = UnitMode.AFTER;

	enum UnitMode {
		@Label(value = "Before", description = "...")
		BEFORE,

		@Label(value = "After", description = "...")
		AFTER;
	}
	
	/* --- */
	
	@Layout(DialogSections.Unit.UnitSettingsLayout.class)
	@Widget(title = "Unit", description = "...")
	@TextInputWidget()
	@Effect(predicate = SetUnit.class, type = EffectType.ENABLE)
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
	String m_outputColumnName = TDSUtils.SCREEN_MODEL_WELL_ROW;

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
