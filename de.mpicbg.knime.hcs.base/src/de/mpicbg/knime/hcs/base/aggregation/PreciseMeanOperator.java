package de.mpicbg.knime.hcs.base.aggregation;

import org.knime.base.data.aggregation.AggregationOperator;
import org.knime.base.data.aggregation.GlobalSettings;
import org.knime.base.data.aggregation.OperatorColumnSettings;
import org.knime.base.data.aggregation.OperatorData;
import org.knime.base.data.aggregation.numerical.MeanOperator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;

public class PreciseMeanOperator extends AggregationOperator {
	
	private final DataType m_type = DoubleCell.TYPE;
	private int m_count = 0;
    private double m_mean = 0;
    

	public PreciseMeanOperator(OperatorData operatorData) {
		super(operatorData);
	}
	
    public PreciseMeanOperator() {
    	//super(new OperatorData("HCS-Mean", false, false, DoubleValue.class, false));
    	super(new OperatorData("HCS-Mean", false, false, DoubleValue.class, false),
    			GlobalSettings.DEFAULT, OperatorColumnSettings.DEFAULT_EXCL_MISSING);
	}

	public PreciseMeanOperator(OperatorData operatorData,
			GlobalSettings globalSettings, OperatorColumnSettings opColSettings) {
		super(operatorData, globalSettings, opColSettings);
	}

	@Override
	public String getDescription() {
		return "Calculates the mean value per group. (HCS-Tools)";
	}

	@Override
	public AggregationOperator createInstance(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		return new PreciseMeanOperator(getOperatorData(), globalSettings, opColSettings);
	}

	@Override
	protected boolean computeInternal(DataCell cell) {
		final double d = ((DoubleValue)cell).getDoubleValue();
		m_count++;
        m_mean = m_mean + (d - m_mean)/(m_count);
        
        return false;
	}

	@Override
	protected DataType getDataType(DataType origType) {
		return m_type;
	}

	@Override
	protected DataCell getResultInternal() {
		if (m_count == 0) {
            return DataType.getMissingCell();
        }
        return new DoubleCell(m_mean);
	}

	@Override
	protected void resetInternal() {
		m_mean = 0;
        m_count = 0;
	}

}
