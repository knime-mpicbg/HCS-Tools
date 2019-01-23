package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.IntervalCell;

import de.mpicbg.knime.hcs.core.math.Interval.Mode;

public class CreateIntervalCellFactory extends AbstractCellFactory {
	
	private final CreateIntervalNodeSettings m_settings;
	private final DataTableSpec m_spec;

	public CreateIntervalCellFactory(CreateIntervalNodeSettings settings, DataTableSpec spec) {
		super();
		this.m_settings = settings;
		this.m_spec = spec;
	}

	@Override
	public DataCell[] getCells(DataRow row) {
		DataCell leftBound = row.getCell(m_spec.findColumnIndex(m_settings.getLeftBoundColumn()));
		DataCell rightBound = row.getCell(m_spec.findColumnIndex(m_settings.getRightBoundColumn()));
		
		if(leftBound.isMissing() || rightBound.isMissing())
			return new DataCell[] {DataType.getMissingCell()};
		
		boolean inclLeft = false;
		boolean inclRight = false;
		
		if(m_settings.useModeColumns()) {
			DataCell leftMode = row.getCell(m_spec.findColumnIndex(m_settings.getLeftModeColumn()));
			DataCell rightMode = row.getCell(m_spec.findColumnIndex(m_settings.getRightModeColumn()));
			
			if(leftMode.isMissing() || rightMode.isMissing())
				return new DataCell[] {DataType.getMissingCell()};
			
			inclLeft = ((BooleanCell) leftMode).getBooleanValue();
			inclRight = ((BooleanCell) rightMode).getBooleanValue();
		} else {
			String modeString = m_settings.getFixedMode();
			if(modeString.equals(Mode.INCL_BOTH.toString())) {
				inclLeft = inclRight = true;
			}
			if(modeString.equals(Mode.INCL_LEFT.toString())) {
				inclLeft = true;
				inclRight = false;
			}
			if(modeString.equals(Mode.INCL_NONE.toString())) {
				inclLeft = inclRight = false;
			}
			if(modeString.equals(Mode.INCL_RIGHT.toString())) {
				inclLeft = false;
				inclRight = true;
			}
		}
		
		double left = ((DoubleValue) leftBound).getDoubleValue();
		double right = ((DoubleValue) rightBound).getDoubleValue();
		
		return new DataCell[] {new IntervalCell(left, right, inclLeft, inclRight)};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumnSpec[] getColumnSpecs() {
		DataColumnSpecCreator colCreator = new DataColumnSpecCreator(m_settings.getOutColumnName(), IntervalCell.TYPE);

		return new DataColumnSpec[] {colCreator.createSpec()};
	}
}
