package de.mpicbg.knime.hcs2.base.node.preproc.numberformatter;

import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.OptionalWidget.DefaultValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;

final class NumberFormatterNodeSettings implements NodeParameters {
	
	/*
	 * ============================================ Sections ==================================================
	 */

	interface DialogSections {
		@Section(title = "Notation")
		interface Notation {
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
	@ChoicesProvider(NumberFormatterColumnsProvider.class)
	/** setting - choice of input column */
	String m_inputColumn;
	
	static final class NumberFormatterColumnsProvider extends CompatibleColumnsProvider {
		
		static final List<Class<? extends DataValue>> COMPATIBLE_TYPES =
	            List.of(DoubleValue.class, StringValue.class);

		public NumberFormatterColumnsProvider() {
			super(COMPATIBLE_TYPES);
		}
		
		public static boolean isCompatible(final DataColumnSpec colSpec) {
	        return COMPATIBLE_TYPES.stream().anyMatch(colSpec.getType()::isCompatible);
	    }

		public static List<Class<? extends DataValue>> getValueClassesList() {
			// TODO Auto-generated method stub
			return COMPATIBLE_TYPES;
		}
		
	}
	
	/* --- */
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Thousands Separator", description = "...")
	@OptionalWidget(defaultProvider = ThousandsSeparatorDefaultProvider.class)
	@Effect(predicate = UseThousandsSeparator.class, type = EffectType.ENABLE)
	Optional<ThousandsSeparator> m_thousandsSeparator = Optional.of(ThousandsSeparator.COMMA);
	
	static final class ThousandsSeparatorDefaultProvider implements DefaultValueProvider<ThousandsSeparator> {

        @Override
        public ThousandsSeparator computeState(final NodeParametersInput context) throws StateComputationFailureException {
            return ThousandsSeparator.COMMA;
        }

    }
	
	static final class UseThousandsSeparator implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getBoolean(EnabledRef.class).isTrue();
        }

    }
	
	static final class EnabledRef implements ParameterReference<Boolean> {
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
	
	@Layout(DialogSections.Notation.class)
	@Widget(title = "Decimal Separator", description = "...")
	//@ValueSwitchWidget
	DecimalSeparator m_decimalSeparator = DecimalSeparator.FULL_STOP;
	
	enum DecimalSeparator {
		@Label(". (full stop)")
        FULL_STOP,

        @Label(", (comma)")
        COMMA;
	}

}
