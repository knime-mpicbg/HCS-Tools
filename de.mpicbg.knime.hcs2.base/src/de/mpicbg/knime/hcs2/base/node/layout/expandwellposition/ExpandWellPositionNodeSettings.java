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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.util.column.ColumnSelectionUtil;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.column.CompatibleColumnsProvider.StringColumnsProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;

import de.mpicbg.knime.hcs2.core.TDSUtils;

import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;


/** Settings for the "Unit Converter" node. */
final class ExpandWellPositionNodeSettings implements DefaultNodeSettings {
	
	protected static final String WELLCOLUMN_PATTERN = ".*[Ww]ell.*";

	@Widget(title = "Well Position", description = "...")
	@ChoicesProvider(StringColumnsProvider.class)
	/** setting - choice of column */
	String m_wellPositionColumn;
	
	@Widget(title = "Delete source column", description = "...")
	/** setting - delete source column */
	boolean m_deleteSourceColumn = false; 

	@Widget(title = "Conversion settings (plate row index)", description = "If unchecked, the row index will be numeric")
	@ValueSwitchWidget
	/** setting - convert plate row to number? */
	StringOrNumber m_rowConversion = StringOrNumber.NUMERIC;

	@Widget(title = "Output columns", description = "")
	@ValueSwitchWidget
	@ValueReference(StandardRef.class)
	/** setting - rename columns? */
	OutputColumn m_rename = OutputColumn.DEFAULT_NAMES;

	@Widget(title = "Column name (plate row index)", description = "...")
	@Effect(predicate = OutputColumnIsRename.class, type = EffectType.SHOW)
	@Persist(configKey = "PlateRowName")
	/** setting - column name for plate row */
	String m_plateRowName = "plateRow";

	@Widget(title = "Column name (plate column index)", description = "...")
	@Effect(predicate = OutputColumnIsRename.class, type = EffectType.SHOW)
	@Persist(configKey = "PlateColumnName")
	/** setting - column name for plate column */
	String m_plateColumnName = "plateColumn";

	interface StandardRef extends Reference<OutputColumn> {
	}

	static final class OutputColumnIsRename implements PredicateProvider {
		@Override
		public Predicate init(final PredicateInitializer i) {
			return i.getEnum(StandardRef.class).isOneOf(OutputColumn.RENAME);
		}
	}

	enum OutputColumn {
		@Label(value = "Keep default", description = "...")
		DEFAULT_NAMES,

		@Label(value = "Rename", description = "...")
		RENAME;
	}

	enum StringOrNumber {
		@Label(value = "Keep row letter", description = "Returns the row index as letter")
		LETTER(StringCell.TYPE),

		@Label(value = "Convert row letter to number", description = "Return the row index as number")
		NUMERIC(IntCell.TYPE);

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
	
    public ExpandWellPositionNodeSettings() {
    	this((DataTableSpec)null);
	}
    
    ExpandWellPositionNodeSettings(final DefaultNodeSettingsContext context) {
        this(context.getDataTableSpec(0).orElse(null));
    }
	
	ExpandWellPositionNodeSettings(final DataTableSpec spec) {
        if (spec == null) {
            return;
        }

        List<DataColumnSpec> columnList = ColumnSelectionUtil.getStringColumns(spec);
        
        if(columnList.isEmpty())
        	return;
        
        List<DataColumnSpec> filteredList = columnList.stream()
        .filter(str -> str.getName().matches(WELLCOLUMN_PATTERN)).toList();
        
        if(columnList.isEmpty())
        	return;
    
        m_wellPositionColumn = filteredList.get(0).getName();
    }


}
