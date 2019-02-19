package de.mpicbg.knime.hcs.base.nodes.layout.expandwellposition;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.util.MutableInteger;

import de.mpicbg.knime.hcs.core.TdsUtils;

/**
 * cell factory for Expand Well Position (V2)
 * 
 * @author Antje Janosch
 *
 */
public class ExpandWellPositionV2CellFactory extends AbstractCellFactory {
	
	// column index of well column
	private final int m_colIdx;
	// true, if row letters shall be translated into numeric representation
	private final boolean m_convertRowValues;
	
	// name of new column for plate row 
	private final String m_plateRowName;
	// name of new column for plate column
	private final String m_plateColumnName;
	
	// well pattern
	private final Pattern m_pattern = Pattern.compile(TdsUtils.WELL_PATTERN);
	
	// counts mismatches for final warning message
	private MutableInteger m_countMismatches;

	/**
	 * constructor
	 * 
	 * @param colIdx				index of column with well position
	 * @param convertRowValues		if true, row values will become integer
	 * @param plateRowName			name of new column for plate row 
	 * @param plateColumnName		name of new column for plate column
	 * @param countMismatches		count mismatches
	 */
	public ExpandWellPositionV2CellFactory(int colIdx, boolean convertRowValues, String plateRowName, String plateColumnName, MutableInteger countMismatches) {
		super();
		
		m_colIdx = colIdx;
		m_convertRowValues = convertRowValues;
		
		m_plateRowName = plateRowName;
		m_plateColumnName= plateColumnName;
		
		m_countMismatches= countMismatches;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumnSpec[] getColumnSpecs() {
		
		List<DataColumnSpec> columnsToAdd = new LinkedList<DataColumnSpec>();
		DataColumnSpecCreator colCreator;
		
		DataType rowValuesType = StringCell.TYPE;
		if(m_convertRowValues)
			rowValuesType = IntCell.TYPE;
		
		colCreator = new DataColumnSpecCreator(m_plateRowName, rowValuesType);
		columnsToAdd.add(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator(m_plateColumnName, IntCell.TYPE);
		columnsToAdd.add(colCreator.createSpec());
		
		return columnsToAdd.toArray(new DataColumnSpec[columnsToAdd.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataCell[] getCells(DataRow row) {
		
		DataCell wellCell = row.getCell(m_colIdx);
		DataCell[] missingResult = new DataCell[] {DataType.getMissingCell(), DataType.getMissingCell()};
		
		if(wellCell.isMissing()) {
			return missingResult;
		}
		
		String wellString = ((StringValue) wellCell).getStringValue();
		
		Matcher matcher = m_pattern.matcher(wellString);

		// if string does not match, return missing
        if (!matcher.matches()) {
            m_countMismatches.inc();
            return missingResult;
        }
              
        String plateRowString = matcher.group(1);
        String plateColumnString = matcher.group(2);
        
        // test, whether plate row string is valid
        int plateRow = TdsUtils.mapPlateRowStringToNumber(plateRowString);
        // if plate row is not valid for supported plate formats
        if(plateRow == -1) {
        	m_countMismatches.inc();
        	return missingResult;
        }
        	
        int plateColumn = Integer.parseInt(plateColumnString);
        // if plate column is not valid for supported plate formats
        if(plateColumn < 1 || plateColumn > TdsUtils.MAX_PLATE_COLUMN) {
        	m_countMismatches.inc();
        	return missingResult;
        }
        
        DataCell rowCell = null;
        
        // create new cells
        if(m_convertRowValues) {
        	rowCell = new IntCell(plateRow);
        } else
        	rowCell = new StringCell(plateRowString);
        
        IntCell columnCell = new IntCell(plateColumn);
		
		return new DataCell[] {rowCell, columnCell};
	}

}
