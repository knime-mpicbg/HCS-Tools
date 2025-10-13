/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package de.mpicbg.knime.hcs2.base.node.layout.expandwellposition;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
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
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.StringColumnsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.core.TDSUtils;


/** Settings for the "Unit Converter" node. */
final class ExpandWellPositionNodeSettings implements NodeParameters {
	
	protected static final String WELLCOLUMN_PATTERN = ".*[Ww]ell.*";
	
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

	@Widget(title = "Well Position", description = "Choose the column containing the well position to split")
	@ChoicesProvider(StringColumnsProvider.class)
	/** setting - choice of column */
	String m_wellPositionColumn;
	
	@Widget(title = "Conversion settings (plate row index)", description = "If unchecked, the row index will be numeric")
	@ValueSwitchWidget
	/** setting - convert plate row to number? */
	StringOrNumber m_rowConversion = StringOrNumber.NUMERIC;
	
	enum StringOrNumber {

		@Label(value = "Convert row letter to number", description = "Return the row index as number")
		NUMERIC(IntCell.TYPE),
		
		@Label(value = "Keep row letter", description = "Returns the row index as letter")
		LETTER(StringCell.TYPE);

		private final DataType m_dataType;

		StringOrNumber(final DataType dataType) {
			this.m_dataType = dataType;
		}

		DataType getDataType() {
			return m_dataType;
		}

		DataCell createCell(final String outputValue) {
			if (this == LETTER) {
				return new StringCell(outputValue);
			} else {
				return new IntCell(TDSUtils.mapPlateRowStringToNumber(outputValue));
			}
		}
	}
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Position of output columns", description = "New columns can be either appended to the end of the table or inserted after the well position column")
	@ValueSwitchWidget
	/** setting - where to put the new columns */
	OutputColumnPosition m_columnPosition = OutputColumnPosition.APPEND;
	
	enum OutputColumnPosition {
		@Label(value = "End of the table", description = "Appends the new columns")
		APPEND,

		@Label(value = "Behind well position column", description = "New columns will be placed next to the well position column")
		BEHIND_WELL_POSITION;
	}

	@Layout(DialogSections.Output.class)
	@Widget(title = "Output columns", description = "New columns may get default names or custom names may be set")
	@ValueSwitchWidget
	@ValueReference(StandardRef.class)
	/** setting - rename columns? */
	OutputColumn m_rename = OutputColumn.DEFAULT_NAMES;
	
	enum OutputColumn {
		@Label(value = "Keep default", description = "Output columns will be named 'plateRow' and 'plateColumn' respectively")
		DEFAULT_NAMES,

		@Label(value = "Rename", description = "Give custom names to outputput columns")
		RENAME;
	}

	@Layout(DialogSections.Output.class)
	@Widget(title = "Column name (plate row index)", description = "Choose a name for the column containing the plate row identifier")
	@Effect(predicate = OutputColumnIsRename.class, type = EffectType.SHOW)
	@TextInputWidget(patternValidation = ColumnNameValidationUtils.ColumnNameValidation.class)
	/** setting - column name for plate row */
	String m_plateRowName = "plateRow";

	@Layout(DialogSections.Output.class)
	@Widget(title = "Column name (plate column index)", description = "Choose a name for the column containing the plate column identifier")
	@Effect(predicate = OutputColumnIsRename.class, type = EffectType.SHOW)
	@TextInputWidget(patternValidation = ColumnNameValidationUtils.ColumnNameValidation.class)
	/** setting - column name for plate column */
	String m_plateColumnName = "plateColumn";
	
	interface StandardRef extends ParameterReference<OutputColumn> {
	}

	static final class OutputColumnIsRename implements EffectPredicateProvider {
		@Override
		public EffectPredicate init(final PredicateInitializer i) {
			return i.getEnum(StandardRef.class).isOneOf(OutputColumn.RENAME);
		}
	}
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Delete source column", description = "If checked, the well position column will be removed from the table")
	/** setting - delete source column */
	boolean m_deleteSourceColumn = false;

	/* ============================================================================================== */
	
    public ExpandWellPositionNodeSettings() {
    	this((DataTableSpec)null);
	}
    
    ExpandWellPositionNodeSettings(final NodeParametersInput context) {
        this(context.getInTableSpec(0).orElse(null));
    }
	
	ExpandWellPositionNodeSettings(final DataTableSpec spec) {
        if (spec == null) {
            return;
        }

        // get all string columns
        List<DataColumnSpec> columnList = ColumnSelectionUtil.getStringColumns(spec);
        
        if(columnList.isEmpty())
        	return;
        
        m_wellPositionColumn = columnList.stream()
        	    .filter(str -> str.getName().matches(WELLCOLUMN_PATTERN))
        	    .findFirst()
        	    .map(DataColumnSpec::getName)
        	    .orElseGet(() -> columnList.get(0).getName());
        
        /* 
         * suggest new column names
         */
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
    	m_plateColumnName = uniqueNameGenerator.newName(TDSUtils.SCREEN_MODEL_WELL_COLUMN);
    	m_plateRowName = uniqueNameGenerator.newName(TDSUtils.SCREEN_MODEL_WELL_ROW);
    }


}
