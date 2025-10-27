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
package de.mpicbg.knime.hcs2.base.node.layout.createwellposition;

import java.util.List;
import java.util.stream.Stream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.DoubleColumnsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;
import de.mpicbg.knime.hcs2.core.TDSUtils;


/** Settings for the "Unit Converter" node. */
final class CreateWellPositionNodeSettings implements NodeParameters {
	
	/* ============================================ Sections ================================================== */
	
	interface DialogSections {
        @Section(title = "Output")
        @Advanced
        interface Output {
        }
    }
	
	/* ============================================ UI Elements ================================================== */

	@Widget(title = "Plate Row Identifier", description = "Choose the column containing the plate row indentifier")
	@ChoicesProvider(DoubleStringColumnsProvider.class)
	/** setting - choice of plate row column */
	String m_plateRowColumn;
	
	@Widget(title = "Plate Column Identifier", description = "Choose the column containing the plate column indentifier")
	@ChoicesProvider(DoubleColumnsProvider.class)
	/** setting - choice of plate row column */
	String m_plateColumnColumn;
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Output column name", description = "Choose a name for the output column containing the well position")
	@TextInputWidget(patternValidation = ColumnNameValidationUtils.ColumnNameValidation.class )
	/** setting - column name for plate row */
	String m_outputColumnName = TDSUtils.SCREEN_MODEL_WELL;
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Position of output column", description = "New columns can be either appended to the end of the table or inserted after the input columns")
	@ValueSwitchWidget
	/** setting - where to put the new columns */
	OutputColumnPosition m_columnPosition = OutputColumnPosition.APPEND;
	
	enum OutputColumnPosition {
		@Label(value = "End of the table", description = "Appends the new columns")
		APPEND,

		@Label(value = "Behind input columns", description = "New columns will be placed next to the input columns")
		BEHIND_WELL_POSITION;
	}
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Use sortable format", description = "If checked, the resulting well position will become alphabetical sortable like ' A01', ' B13', 'AE08'")
	boolean m_useSortableFormat = false;
	
	@Layout(DialogSections.Output.class)
	@Widget(title = "Delete source columns", description = "If checked, the input columns will be removed from the table")
	boolean m_deleteSourceColumns = false;
	
		
	/* ============================================================================================== */
	
    public CreateWellPositionNodeSettings() {
    	this((DataTableSpec)null);
	}
    
    CreateWellPositionNodeSettings(final NodeParametersInput context) {
        this(context.getInTableSpec(0).orElse(null));
    }
	
	CreateWellPositionNodeSettings(final DataTableSpec spec) {
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
        
        // filter for all compatible columns for plate column column
        List<DataColumnSpec> filteredColumnList = columnList.stream().filter(cspec -> cspec.getType().isCompatible(DoubleValue.class)).toList();
        
        // no compatible columns 
        if(filteredColumnList.isEmpty())
        	return;
        
        /* 
         * from compatible columns:
         * use "plateRow" if present
         * otherwise check for a column containing "row" or "Row"
         * otherwise use first column in list
         */
        m_plateColumnColumn = Stream.concat(
	            filteredColumnList.stream().filter(cspec -> cspec.getName().equals(TDSUtils.SCREEN_MODEL_WELL_COLUMN)),
	            filteredColumnList.stream().filter(cspec -> cspec.getName().matches(".*[cC]ol.*"))
	        ).findFirst()
	        .orElseGet(() -> filteredColumnList.get(0))
	        .getName();
        
        
        /* 
         * suggest new column name
         */
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
    	m_outputColumnName = uniqueNameGenerator.newName(TDSUtils.SCREEN_MODEL_WELL);
        
    }

}
