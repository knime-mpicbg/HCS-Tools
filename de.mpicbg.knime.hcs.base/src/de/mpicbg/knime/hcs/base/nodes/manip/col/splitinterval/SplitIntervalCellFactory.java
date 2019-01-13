package de.mpicbg.knime.hcs.base.nodes.manip.col.splitinterval;

import java.util.LinkedList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntervalCell;

public class SplitIntervalCellFactory extends AbstractCellFactory {
	
	private final String m_sourceColumnName;
	private final int m_sourceIndex;
	private final boolean m_createModeColumns;
	
	public SplitIntervalCellFactory(String sourceColumnName, int sourceIndex, boolean createModeColumns) {
		super();	
		m_sourceColumnName = sourceColumnName;
		m_sourceIndex = sourceIndex;
		m_createModeColumns = createModeColumns;
	}

	@Override
	public DataCell[] getCells(DataRow row) {
		
		if(row.getCell(m_sourceIndex).isMissing()) {
			if(m_createModeColumns) {
				return new DataCell[] {
						DataType.getMissingCell(),
						DataType.getMissingCell(),
						DataType.getMissingCell(),
						DataType.getMissingCell()
				};
			} else {
				return new DataCell[] {
						DataType.getMissingCell(),
						DataType.getMissingCell(),
				};
			}
		}
		
		IntervalCell ivCell = (IntervalCell) row.getCell(m_sourceIndex);
		
		double leftBound = ivCell.getLeftBound();
		double rightBound = ivCell.getRightBound();
		boolean inclLeft = ivCell.leftBoundIncluded();
		boolean inclRight = ivCell.rightBoundIncluded();
		
		if(m_createModeColumns) {
			return new DataCell[] {
					BooleanCellFactory.create(inclLeft),
					new DoubleCell(leftBound),
					new DoubleCell(rightBound),
					BooleanCellFactory.create(inclRight)
					};
		} else {
			return new DataCell[] {
					new DoubleCell(leftBound),
					new DoubleCell(rightBound),
					};
		}
	}

	@Override
	public DataColumnSpec[] getColumnSpecs() {
		List<DataColumnSpec> columnsToAdd = new LinkedList<DataColumnSpec>();
		DataColumnSpecCreator colCreator;
		
		if(m_createModeColumns) {
			colCreator = new DataColumnSpecCreator(m_sourceColumnName + " [includeLeft]", BooleanCell.TYPE);
			columnsToAdd.add(colCreator.createSpec());
		}
		colCreator = new DataColumnSpecCreator(m_sourceColumnName + " [leftBound]", DoubleCell.TYPE);
		columnsToAdd.add(colCreator.createSpec());
		colCreator = new DataColumnSpecCreator(m_sourceColumnName + " [rightBound]", DoubleCell.TYPE);
		columnsToAdd.add(colCreator.createSpec());
		if(m_createModeColumns) {
			colCreator = new DataColumnSpecCreator(m_sourceColumnName + " [includeRight]", BooleanCell.TYPE);
			columnsToAdd.add(colCreator.createSpec());
		}
		
		return columnsToAdd.toArray(new DataColumnSpec[columnsToAdd.size()]);
	}

}
