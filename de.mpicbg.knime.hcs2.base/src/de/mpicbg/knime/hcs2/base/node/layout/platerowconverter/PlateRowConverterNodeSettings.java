package de.mpicbg.knime.hcs2.base.node.layout.platerowconverter;

import java.util.List;
import java.util.stream.Stream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.node.parameters.Advanced;
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
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;
import de.mpicbg.knime.hcs2.core.TDSUtils;

final class PlateRowConverterNodeSettings implements NodeParameters {

	/*
	 * ============================================ Sections ==================================================
	 */

	interface DialogSections {
		@Section(title = "Output")
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
	@Widget(title = "Plate Row Identifier", description = "Choose the column containing the plate row identifier")
	@ChoicesProvider(DoubleStringColumnsProvider.class)
	/** setting - choice of plate row column */
	String m_plateRowColumn;

	@Layout(DialogSections.Output.class)
	@Widget(title = "Output Mode", description = "Node output can either replace the input column or a new column is appended")
	@ValueSwitchWidget
	@ValueReference(StandardRef.class)
	/** setting - where to put the new columns */
	OutputColumnMode m_columnMode = OutputColumnMode.REPLACE;

	enum OutputColumnMode {
		@Label(value = "Replace", description = "Replace the input columns with node output (column data type will change)")
		REPLACE,

		@Label(value = "Append", description = "Append a new column with node output")
		APPEND;
	}
	
	interface StandardRef extends ParameterReference<OutputColumnMode> { }


	@Layout(DialogSections.Output.class)
	@Widget(title = "Output Column Name", description = "Name of the created output column. Only available if output mode is `Append`.")
	@Effect(predicate = OutputColumnIsAppend.class, type = EffectType.SHOW)
	@TextInputWidget(patternValidation = ColumnNameValidationUtils.ColumnNameValidation.class )
	/** setting - column name for plate row */
	String m_outputColumnName = TDSUtils.SCREEN_MODEL_WELL_ROW;

	static final class OutputColumnIsAppend implements EffectPredicateProvider {

		@Override public EffectPredicate init(final PredicateInitializer i) { 
			return i.getEnum(StandardRef.class).isOneOf(OutputColumnMode.APPEND); }
	}													 

	/* ============================================================================================== */
	
	public PlateRowConverterNodeSettings() {
    	this((DataTableSpec)null);
	}
    
	PlateRowConverterNodeSettings(final NodeParametersInput context) {
        this(context.getInTableSpec(0).orElse(null));
    }
	
	PlateRowConverterNodeSettings(final DataTableSpec spec) {
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
         * use "plateRow" if present
         * otherwise check for a column containing "row" or "Row"
         * otherwise use first column in list
         */
        m_plateRowColumn = Stream.concat(
        	            columnList.stream().filter(cspec -> cspec.getName().equals(TDSUtils.SCREEN_MODEL_WELL_ROW)),
        	            columnList.stream().filter(cspec -> cspec.getName().matches(".*[rR]ow.*"))
        	        ).findFirst()
        	        .orElseGet(() -> columnList.get(0))
        	        .getName();
        

        /* 
         * suggest new column name
         */
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
    	m_outputColumnName = uniqueNameGenerator.newName(TDSUtils.SCREEN_MODEL_WELL_ROW + " (converted)");
    }

}
