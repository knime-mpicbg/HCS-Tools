package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.IntervalCell;

public class CreateIntervalCellFactory extends AbstractCellFactory {
	
	private final String m_outColumnName;

	public CreateIntervalCellFactory(String m_outColumnName) {
		super();
		this.m_outColumnName = m_outColumnName;
	}

	@Override
	public DataCell[] getCells(DataRow row) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataColumnSpec[] getColumnSpecs() {
		DataColumnSpecCreator colCreator = new DataColumnSpecCreator(m_outColumnName, IntervalCell.TYPE);

		return new DataColumnSpec[] {colCreator.createSpec()};
	}
}
