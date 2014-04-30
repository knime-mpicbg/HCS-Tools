package de.mpicbg.tds.knime.hcstools.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.knime.base.data.aggregation.AggregationOperator;
import org.knime.base.data.aggregation.GlobalSettings;
import org.knime.base.data.aggregation.OperatorColumnSettings;
import org.knime.base.data.aggregation.OperatorData;
import org.knime.base.data.aggregation.numerical.MedianOperator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;

public class MADOperator extends AggregationOperator {
	
	private static final DataType TYPE = DoubleCell.TYPE;

    private final List<DataCell> m_cells;
    private final Comparator<DataCell> m_comparator = TYPE.getComparator();


	public MADOperator(OperatorData operatorData,
			GlobalSettings globalSettings, OperatorColumnSettings opColSettings) {
		super(operatorData, globalSettings, opColSettings);
		// TODO Auto-generated constructor stub
		try {
            m_cells = new ArrayList<DataCell>(getMaxUniqueValues());
        } catch (final OutOfMemoryError e) {
            throw new IllegalArgumentException(
            "Maximum unique values number too big");
        }
	}
	
	/**Constructor for class MedianOperator.
     * @param globalSettings the global settings
     * @param opColSettings the operator column specific settings
     */
    public MADOperator(final GlobalSettings globalSettings,
            final OperatorColumnSettings opColSettings) {
        this(new OperatorData("MAD", true, false, DoubleValue.class,
                false), globalSettings, setInclMissingFlag(opColSettings));
    }
    
    public MADOperator() {
        this(new OperatorData("MAD", false, false, DoubleValue.class,
                false),new GlobalSettings(0), new OperatorColumnSettings(false, null));
    }

    /**
     * Ensure that the flag is set correctly since this method does not
     * support changing of the missing cell handling option.
     *
     * @param opColSettings the {@link OperatorColumnSettings} to set
     * @return the correct {@link OperatorColumnSettings}
     */
    private static OperatorColumnSettings setInclMissingFlag(
            final OperatorColumnSettings opColSettings) {
        opColSettings.setInclMissing(false);
        return opColSettings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataType getDataType(final DataType origType) {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean computeInternal(final DataCell cell) {
        if (cell.isMissing()) {
            return false;
        }
        if (m_cells.size() >= getMaxUniqueValues()) {
            return true;
        }
        m_cells.add(cell);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AggregationOperator createInstance(
            final GlobalSettings globalSettings,
            final OperatorColumnSettings opColSettings) {
        return new MedianOperator(globalSettings, opColSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataCell getResultInternal() {
        if (m_cells.size() == 0) {
            return DataType.getMissingCell();
        }
        if (m_cells.size() == 1) {
            return convertToDoubleCellIfNecessary(m_cells.get(0));
        }
        Collections.sort(m_cells, m_comparator);
        final double middle = m_cells.size() / 2.0;
        if (middle > (int)middle) {
            return convertToDoubleCellIfNecessary(m_cells.get((int)middle));
        }
        //the list is even return the middle two
        final double val1 =
            ((DoubleValue)m_cells.get((int) middle - 1)).getDoubleValue();
        final double val2 =
            ((DoubleValue)m_cells.get((int) middle)).getDoubleValue();
        return new DoubleCell((val1 + val2) / 2);
    }

    /** Converts argument to DoubleCell if it does not fully support the
     * DataValue interfaces supported by DoubleCell.TYPE .
     * @param cell Cell to convert (or not)
     * @return The argument or a new DoubleCell.
     */
    private DataCell convertToDoubleCellIfNecessary(final DataCell cell) {
        if (cell.isMissing()) {
            return DataType.getMissingCell();
        }
        if (TYPE.isASuperTypeOf(cell.getType())) {
            return cell;
        }
        return new DoubleCell(((DoubleValue)cell).getDoubleValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetInternal() {
        m_cells.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Calculates the median of a list of numbers. "
                + "Missing cells are skipped.";
    }



}
