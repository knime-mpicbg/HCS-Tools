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

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEException.KNIMERuntimeException;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.node.DefaultModel.RearrangeColumnsInput;
import org.knime.node.DefaultModel.RearrangeColumnsOutput;

import de.mpicbg.knime.hcs2.base.node.layout.expandwellposition.ExpandWellPositionNodeSettings.StringOrNumber;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsColumnNotFoundException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsMissingSettingException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsWrongDataTypeException;
import de.mpicbg.knime.hcs2.core.TDSUtils;

/** Model for the "Unit Converter" node. */
final class ExpandWellPositionNodeModel{
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ExpandWellPositionNodeModel.class);

    static void rearrangeColumns(final RearrangeColumnsInput in, final RearrangeColumnsOutput out)
        throws InvalidSettingsException {
        final var spec = in.getDataTableSpec();
        final var settings = in.<ExpandWellPositionNodeSettings> getParameters();
        final var rearranger = new ColumnRearranger(spec);
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
        
        // get input column and check if it's available
        //final var wellPositionIndex = spec.findColumnIndex(settings.m_wellPositionColumn);
        
        ExpandWellPositionNodeModel.validateSettings(settings, spec);
        
        final var wellPositionIndex = spec.findColumnIndex(settings.m_wellPositionColumn);
    
        if (settings.m_deleteSourceColumn)
        	rearranger.remove(wellPositionIndex);
        
        String plateColumnName = TDSUtils.SCREEN_MODEL_WELL_COLUMN;
        String plateRowName = TDSUtils.SCREEN_MODEL_WELL_ROW;
        
        if (settings.m_rename == ExpandWellPositionNodeSettings.OutputColumn.RENAME) {
        	plateColumnName = settings.m_plateColumnName;
        	plateRowName = settings.m_plateRowName;
        }
        
        final var rowConversion = settings.m_rowConversion;
        DataColumnSpec plateRowSpec = uniqueNameGenerator.newColumn(plateRowName, rowConversion.getDataType());
        DataColumnSpec plateColumnSpec = uniqueNameGenerator.newColumn(plateColumnName, IntCell.TYPE);
        
        final DataColumnSpec[] specs ={plateRowSpec, plateColumnSpec}; 
        
        if(settings.m_columnPosition == ExpandWellPositionNodeSettings.OutputColumnPosition.BEHIND_WELL_POSITION) {
        	var offset = 0;
    		if (settings.m_deleteSourceColumn) offset = -1;
        	rearranger.insertAt(wellPositionIndex + 1 + offset, new ExpandWellPositionCellFactory(wellPositionIndex, specs, rowConversion ));
        }
        else 
        	rearranger.append(new ExpandWellPositionCellFactory(wellPositionIndex, specs, rowConversion ));
        
        out.setColumnRearranger(rearranger);
    }
    
    static void validateSettings(ExpandWellPositionNodeSettings settings, DataTableSpec spec) 
			throws InvalidSettingsException {
    	
    	// check if input column is set at all
        final var wellPositionColumnIdx = Optional.ofNullable(settings.m_wellPositionColumn)
        		.map(columnName -> spec.findColumnIndex(columnName))
        		.orElseThrow(() -> new InvalidSettingsMissingSettingException("Well Position"));
        
        // check if input column exists in input table
        if ( wellPositionColumnIdx < 0 ) {
        	throw new InvalidSettingsColumnNotFoundException(settings.m_wellPositionColumn);
        }
        
        // check if data type of input column is compatible
        if ( !spec.getColumnSpec(wellPositionColumnIdx).getType().isCompatible(StringValue.class) )
        	throw new InvalidSettingsWrongDataTypeException(settings.m_wellPositionColumn);
        
        // check if output column name is set
        if( Optional.ofNullable(settings.m_plateRowName).isEmpty())
        	throw new InvalidSettingsException("Output column name for plate row identifier is missing");   	
        // check if output column name is set
        if( Optional.ofNullable(settings.m_plateColumnName).isEmpty())
        	throw new InvalidSettingsException("Output column name for plate column identifier is missing");   	

        if(settings.m_plateRowName.equals(settings.m_plateColumnName)) {
        	throw new InvalidSettingsException("New column names for plate row index and plate column index cannot be the same.");
        }
    	
    }

    /* NOTE: At the moment it's not possible to use the messageBuilder to gather error messages like it was possible with 
     * class inheriting from NodeModel
     */
    
    static final class ExpandWellPositionCellFactory extends AbstractCellFactory {

        private final int m_columnIndex;
        
        private final StringOrNumber m_rowConversion;
        
        private final Pattern m_pattern = Pattern.compile(TDSUtils.WELL_PATTERN);
        
        //private final Consumer<Message> m_warningConsumer;

        ExpandWellPositionCellFactory(final int columnIndex, DataColumnSpec[] specs, StringOrNumber rowConversion/*, final Consumer<Message> warningConsumer*/) {
        	
        	super(specs);
        	
            m_columnIndex = columnIndex;
            m_rowConversion = rowConversion;
            
            //m_warningConsumer = warningConsumer;
        }

		@Override
		public DataCell[] getCells(DataRow row) {

			DataCell[] result = new DataCell[2];
			Arrays.fill(result, DataType.getMissingCell());
			
			//LOGGER.debug("xxx");
			
			final var inputCell = row.getCell(m_columnIndex);
			
			if (inputCell.isMissing()) {
				return result;
			}
	
			// actually split well position and return plate row and plate column
			String wellString = ((StringValue) inputCell).getStringValue();			
			
			Matcher matcher = m_pattern.matcher(wellString);
						
			// if string does not match, return missing
	        if (!matcher.matches()) {
	            handleParseException(row, wellString, null, m_columnIndex);
	            return result;
	        }
	              
	        String plateRowString = matcher.group(1);
	        String plateColumnString = matcher.group(2);
	        
	        // test, whether plate row string is valid
	        try {
	        	TDSUtils.mapPlateRowStringToNumber(plateRowString);
	        } catch(IllegalArgumentException e) {
	        	handleParseException(row, wellString, null, m_columnIndex);
	            return result;
	        }
	        	        	
	        int plateColumn = Integer.parseInt(plateColumnString);
	        // if plate column is not valid for supported plate formats
	        if(plateColumn < 1 || plateColumn > TDSUtils.MAX_PLATE_COLUMN) {
	        	handleParseException(row, wellString, null, plateColumn);
	        	return result;
	        }
	        
	        result[0] = m_rowConversion.createCell(plateRowString);
	        result[1] = new IntCell(plateColumn);
	        
			return result;
		}
        
        




		/**
		 * Handles the number parse exception, either by failing the execution or by recording the the error.
		 * NOT POSSIBLE AT THE MOMENT!
		 *
		 * @param column Column index
		 * @param row Row
		 * @param value DataCell value at which the parsing failed
		 * @param e NumberFormatException
		 *
		 * @throws KNIMERuntimeException if fail on error is set...
		 */
		private void handleParseException(final DataRow row, final String value, final Exception e, 
				final long rowIndex) throws KNIMERuntimeException {

			/*Supplier<String> message =
					() -> String.format("%s in cell [\"%s\", column \"%s\", row %d] is no valid well position",
							value == null ? "<null>" : ("\"" + StringUtils.abbreviate(value, 15) + "\""), //
									StringUtils.abbreviate(row.getKey().getString(), 15), //
									"to do", //
									rowIndex + 1); // messages to the user are "number based" 

			if (m_messageBuilder.getIssueCount() == 0) {
				m_messageBuilder.withSummary(message.get());
			}
			m_messageBuilder.addRowIssue(0, 0, rowIndex, e.getMessage());

			if (m_messageBuilder.getIssueCount() == 1L) { // first error
				LOGGER.debug(e.getMessage());
			}*/
		}
    }

}
