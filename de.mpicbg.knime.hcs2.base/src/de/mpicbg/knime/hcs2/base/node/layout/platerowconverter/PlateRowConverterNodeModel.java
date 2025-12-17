package de.mpicbg.knime.hcs2.base.node.layout.platerowconverter;

import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.ColumnNameValidationMessageBuilder;
import org.knime.node.DefaultModel.RearrangeColumnsInput;
import org.knime.node.DefaultModel.RearrangeColumnsOutput;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils;

import de.mpicbg.knime.hcs2.base.node.layout.platerowconverter.PlateRowConverterNodeSettings.OutputColumnMode;
import de.mpicbg.knime.hcs2.base.utils.DoubleStringColumnsProvider;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsColumnAlreadyExists;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsColumnNotFoundException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsMissingSettingException;
import de.mpicbg.knime.hcs2.base.utils.exceptions.InvalidSettingsWrongDataTypeException;
import de.mpicbg.knime.hcs2.core.TDSUtils;


final class PlateRowConverterNodeModel {

	static void rearrangeColumns(final RearrangeColumnsInput in, final RearrangeColumnsOutput out)
	        throws InvalidSettingsException {
		
		final var spec = in.getDataTableSpec();
        final var settings = in.<PlateRowConverterNodeSettings> getParameters();
        final var rearranger = new ColumnRearranger(spec);
        
        // all checks resulting in possible InvalidSettingsException go here
        PlateRowConverterNodeModel.validateSettings(settings, spec);
        
        final var replaceInputColumn = settings.m_columnMode;      
        final var plateRowIdx = spec.findColumnIndex(settings.m_plateRowColumn); 
        final var isNumericPlateRow = spec.getColumnSpec(plateRowIdx).getType().isCompatible(DoubleValue.class);       
        final var outType = isNumericPlateRow ? StringCell.TYPE : IntCell.TYPE;
        
        if( replaceInputColumn == OutputColumnMode.REPLACE ) {
        	DataColumnSpecCreator cspecCreator =  new DataColumnSpecCreator(spec.getColumnSpec(plateRowIdx));
        	cspecCreator.setType(outType);
        	rearranger.replace(new PlateRowConverterCellFactory(cspecCreator.createSpec(), plateRowIdx, isNumericPlateRow), settings.m_plateRowColumn);
        }
        if( replaceInputColumn == OutputColumnMode.APPEND ) {        	
        	final var uniqueNameGenerator = new UniqueNameGenerator(spec);
        	DataColumnSpec cspec = uniqueNameGenerator.newColumn(settings.m_outputColumnName, outType);
        	rearranger.append(new PlateRowConverterCellFactory(cspec, plateRowIdx, isNumericPlateRow));
        }
        
        out.setColumnRearranger(rearranger);
	}
	
	static void validateSettings(PlateRowConverterNodeSettings settings, DataTableSpec spec) 
			throws InvalidSettingsException {
		
		// check if input column is set at all
        final var plateRowIdx = Optional.ofNullable(settings.m_plateRowColumn)
        		.map(columnName -> spec.findColumnIndex(columnName))
        		.orElseThrow(() -> new InvalidSettingsMissingSettingException("Plate Row Identifier"));
        
        // check if input column exists in input table
        if ( plateRowIdx < 0 ) {
        	throw new InvalidSettingsColumnNotFoundException(settings.m_plateRowColumn);
        }
        
        // check if data type of input column is compatible
        if ( !DoubleStringColumnsProvider.isCompatible(spec.getColumnSpec(plateRowIdx)) )
        	throw new InvalidSettingsWrongDataTypeException(settings.m_plateRowColumn);
        
        
        if ( settings.m_columnMode == OutputColumnMode.APPEND) {
        	// check if output column name is valid
    		ColumnNameValidationUtils.validateColumnName(settings.m_outputColumnName, new ColumnNameValidationMessageBuilder("output column name").build()); 	    			
        }
	}
	
	static final class PlateRowConverterCellFactory extends SingleCellFactory {

		private final int m_plateRowIdx;		
		private final boolean m_isNumericPlateRow;
	
		public PlateRowConverterCellFactory(DataColumnSpec cspec, int plateRowIdx, boolean isNumericPlateRow) {
			super(cspec);
			
			m_plateRowIdx = plateRowIdx;
			m_isNumericPlateRow = isNumericPlateRow;
		}

		@Override
		public DataCell getCell(DataRow row) {
			var outCell =  DataType.getMissingCell();
			
			final var inCell = row.getCell(m_plateRowIdx);
			
			if (inCell.isMissing() ) 
				return outCell;
		
			
			if(m_isNumericPlateRow) {
				var plateRowNumber = ((DoubleValue) inCell).getDoubleValue();
				
				if(Double.isNaN(plateRowNumber) || Double.isInfinite(plateRowNumber)) {
					//handleParseException(...);
					return outCell;
				}
				if(Math.floor(plateRowNumber) != plateRowNumber ) {
					//handleParseException(...);
					return outCell;
				}
				if(!TDSUtils.isValidPlateRow((int) plateRowNumber) ) {
					//handleParseException(...);
					return outCell;
				}
				
				outCell = new StringCell(TDSUtils.mapPlateRowNumberToString((int) plateRowNumber));
				
			} else {
				var plateRowString = ((StringValue) inCell).getStringValue().strip();
				
				try {
					outCell = new IntCell(TDSUtils.mapPlateRowStringToNumber(plateRowString));
					
				} catch (IllegalArgumentException iae) {
					//handleParseException(...);
					return outCell;
				}
			}
	        
			return outCell;
		}
	    
	}
}


