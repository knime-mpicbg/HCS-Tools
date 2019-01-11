package de.mpicbg.knime.hcs.base.nodes.manip.col.splitinterval;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.DoubleCell;

public class SplitIntervalCellFactory extends AbstractCellFactory {

	@Override
	public DataCell[] getCells(DataRow row) {
		// TODO Auto-generated method stub
		return new DataCell[] {new DoubleCell(5)};
	}

	@Override
	public DataColumnSpec[] getColumnSpecs() {
		DataColumnSpecCreator colCreator;
		colCreator = new DataColumnSpecCreator("test - Parameter", DoubleCell.TYPE);
		return new DataColumnSpec[] {colCreator.createSpec()};
	}

}
