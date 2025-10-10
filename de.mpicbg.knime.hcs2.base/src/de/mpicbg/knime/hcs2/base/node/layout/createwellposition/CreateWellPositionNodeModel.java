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

import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEException.KNIMERuntimeException;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.node.DefaultModel.RearrangeColumnsInput;
import org.knime.node.DefaultModel.RearrangeColumnsOutput;

import de.mpicbg.knime.hcs2.base.node.layout.PlateRowColumnsProvider;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsColumnNotFoundException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsMissingSettingException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsWrongDataTypeException;
import de.mpicbg.knime.hcs2.core.TDSUtils;


final class CreateWellPositionNodeModel {

	static void rearrangeColumns(final RearrangeColumnsInput in, final RearrangeColumnsOutput out)
        throws InvalidSettingsException {
        final var spec = in.getDataTableSpec();
        final var settings = in.<CreateWellPositionNodeSettings> getParameters();
        final var rearranger = new ColumnRearranger(spec);
        final var uniqueNameGenerator = new UniqueNameGenerator(spec);
        
        CreateWellPositionNodeModel.validateSettings(settings, spec);
               
        final var plateRowIdx = spec.findColumnIndex(settings.m_plateRowColumn); 
        final var plateColumnIdx = spec.findColumnIndex(settings.m_plateColumnColumn); 
        
        // get flag based on input specs
        var isNumericPlateRow = spec.getColumnSpec(plateRowIdx).getType().isCompatible(DoubleValue.class);
        var exceedsAlphabet = false;
        if(settings.m_useSortableFormat) {
	        if( isNumericPlateRow ) {
	        	exceedsAlphabet = ((DoubleValue) spec.getColumnSpec(plateRowIdx).getDomain().getUpperBound()).getDoubleValue() > TDSUtils.MAX_PLATE_ROW;
	        } else {
	        	exceedsAlphabet = spec.getColumnSpec(plateRowIdx).getDomain().getValues()
	        			.stream()
	        			.map(cell -> ((StringCell) cell).getStringValue().length())
                        .max(Integer::compare)
                        .get() > 1;
	        }
        }
        
        if (settings.m_deleteSourceColumns)
        	rearranger.remove(plateRowIdx, plateColumnIdx);
        
        DataColumnSpec wellColumnSpec = uniqueNameGenerator.newColumn(settings.m_outputColumnName, StringCell.TYPE);
        
        if(settings.m_columnPosition == CreateWellPositionNodeSettings.OutputColumnPosition.BEHIND_WELL_POSITION) {
        	var offset = 0;
        	var idx = plateRowIdx > plateColumnIdx ? plateRowIdx : plateColumnIdx;
    		if (settings.m_deleteSourceColumns) offset = -1;
        	rearranger.insertAt(idx + 1 + offset, new CreateWellPositionCellFactory(plateRowIdx, plateColumnIdx, wellColumnSpec, settings.m_useSortableFormat, exceedsAlphabet, isNumericPlateRow  ));
        }
        else 
        	rearranger.append(new CreateWellPositionCellFactory(plateRowIdx, plateColumnIdx, wellColumnSpec, settings.m_useSortableFormat, exceedsAlphabet, isNumericPlateRow ));
        
        
        out.setColumnRearranger(rearranger);
    }
	
	static void validateSettings(CreateWellPositionNodeSettings settings, DataTableSpec spec) 
			throws InvalidSettingsException {
		// check if input column is set at all
		final var plateRowIdx = Optional.ofNullable(settings.m_plateRowColumn)
				.map(columnName -> spec.findColumnIndex(columnName))
				.orElseThrow(() -> new InvalidSettingsMissingSettingException("Plate Row Identifier"));

		final var plateColumnIdx = Optional.ofNullable(settings.m_plateColumnColumn)
				.map(columnName -> spec.findColumnIndex(columnName))
				.orElseThrow(() -> new InvalidSettingsMissingSettingException("Plate Column Identifier"));

		// check if input column exists in input table
		if ( plateRowIdx < 0 ) {
			throw new InvalidSettingsColumnNotFoundException(settings.m_plateRowColumn);
		}
		if ( plateColumnIdx < 0 ) {
			throw new InvalidSettingsColumnNotFoundException(settings.m_plateColumnColumn);
		}

		// check if data type of input column is compatible
		if ( !PlateRowColumnsProvider.isCompatible(spec.getColumnSpec(plateRowIdx)) )
			throw new InvalidSettingsWrongDataTypeException(settings.m_plateRowColumn);
		if ( !spec.getColumnSpec(plateColumnIdx).getType().isCompatible(DoubleValue.class) )
			throw new InvalidSettingsWrongDataTypeException(settings.m_plateColumnColumn);
		
		var isNumericPlateRow = spec.getColumnSpec(plateRowIdx).getType().isCompatible(DoubleValue.class);
        if(settings.m_useSortableFormat) {
	        if( isNumericPlateRow ) {
	        	// test if numeric domain is available
	        	if ( Optional.ofNullable(spec.getColumnSpec(plateRowIdx).getDomain().getUpperBound()).isEmpty())
	        			throw new InvalidSettingsException("No domain values available. Cannot provide sortable format. Please reconfigure");
	        } else {
	        	// test if nominal domain values are available and do contain at least one value
	        	if (Optional.ofNullable(spec.getColumnSpec(plateRowIdx).getDomain().getValues())
	        		    .filter(set -> set != null && !set.isEmpty())
	        		    .isEmpty())
	        			throw new InvalidSettingsException("No domain values available. Cannot provide sortable format. Please reconfigure");
	        }
        }
		
		// check if output column name is set
		if( Optional.ofNullable(settings.m_outputColumnName).isEmpty())
				throw new InvalidSettingsException("Output column name missing");   	
				
        // check if output column name already exists in input table
        //if ( spec.containsName(outputColumnName))
        //	throw new InvalidSettingsColumnAlreadyExists(outputColumnName);       			
	}

    /* NOTE: At the moment it's not possible to use the messageBuilder to gather error messages like it was possible with 
     * class inheriting from NodeModel
     */
    
    static final class CreateWellPositionCellFactory extends SingleCellFactory {

        private final int m_plateRowIdx;
        private final int m_plateColumnIdx; 
        private final boolean m_useSortableFormat;
        private final boolean m_exceedsAlphabet;
        private final boolean m_isNumericPlateRow;

        CreateWellPositionCellFactory(final int plateRowIdx, final int plateColumnIdx, DataColumnSpec spec, boolean useSortableFormat/*, final Consumer<Message> warningConsumer*/, boolean exceedsAlphabet, boolean isNumericPlateRow) {
        	
        	super(spec);
        	
            m_plateRowIdx = plateRowIdx;
            m_plateColumnIdx = plateColumnIdx;
            m_useSortableFormat = useSortableFormat;
            m_exceedsAlphabet = exceedsAlphabet;
            m_isNumericPlateRow = isNumericPlateRow;
        }

		@Override
		public DataCell getCell(DataRow row) {

			DataCell result = DataType.getMissingCell();
		
			final var plateRowCell = row.getCell(m_plateRowIdx);
			final var plateColumnCell = row.getCell(m_plateColumnIdx);
			
			if (plateRowCell.isMissing() || plateColumnCell.isMissing()) {
				return result;
			}
			
			/* ======================================== */
			
			double plateRowIdx;
			if( m_isNumericPlateRow ) {
				plateRowIdx = ((DoubleValue) plateRowCell).getDoubleValue();
			} else {			
				String plateRowString = ((StringValue) plateRowCell).getStringValue().trim();
				try {
					plateRowIdx = TDSUtils.mapPlateRowStringToNumber( plateRowString );
				} catch (IllegalArgumentException e) {
					//handleParseException(...);
					return result;
				}
			}
			
			if(Double.isNaN(plateRowIdx) || Double.isInfinite(plateRowIdx)) {
				//handleParseException(...);
				return result;
			}
			if(Math.floor(plateRowIdx) != plateRowIdx ) {
				//handleParseException(...);
				return result;
			}
			if(!TDSUtils.isValidPlateRow((int) plateRowIdx) ) {
				//handleParseException(...);
				return result;
			}
			
			/* ======================================== */
			
			double plateColumnIdx = ((DoubleValue) plateColumnCell).getDoubleValue();
			
			if(Double.isNaN(plateColumnIdx) || Double.isInfinite(plateColumnIdx)) {
				//handleParseException(...);
				return result;
			}
			if(Math.floor(plateColumnIdx) != plateColumnIdx ) {
				//handleParseException(...);
				return result;
			}
			if(!TDSUtils.isValidPlateColumn((int) plateColumnIdx) ) {
				//handleParseException(...);
				return result;
			}
			
			/* ======================================== */
			
			String columnIdxString = String.valueOf((int) plateColumnIdx);
			String rowIdxString = TDSUtils.mapPlateRowNumberToString((int) plateRowIdx);
			
			if(m_useSortableFormat) {
				if ( columnIdxString.length() == 1)
					columnIdxString = "0" + columnIdxString;
			
				if( m_exceedsAlphabet && rowIdxString.length() == 1)
					rowIdxString = " " + rowIdxString;
			}
	

	        
			return new StringCell(rowIdxString + columnIdxString);
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
