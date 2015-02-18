package de.mpicbg.knime.hcs.base.aggregation;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.base.data.aggregation.AggregationOperator;
import org.knime.base.data.aggregation.GlobalSettings;
import org.knime.base.data.aggregation.OperatorColumnSettings;
import org.knime.base.data.aggregation.OperatorData;
import org.knime.base.data.aggregation.numerical.MedianOperator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;

import de.mpicbg.knime.hcs.base.HCSToolsBundleActivator;
import de.mpicbg.knime.hcs.base.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.knime.hcs.base.utils.ExtDescriptiveStats;
import de.mpicbg.knime.hcs.base.utils.MadStatistic;
import de.mpicbg.knime.hcs.base.utils.MadStatistic.IllegalMadFactorException;

public class HCSMadOperator extends MedianOperator {
	
	/* (non-Javadoc)
	 * @see org.knime.base.data.aggregation.numerical.MedianOperator#createInstance(org.knime.base.data.aggregation.GlobalSettings, org.knime.base.data.aggregation.OperatorColumnSettings)
	 */
	@Override
	public AggregationOperator createInstance(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		return new HCSMadOperator(getOperatorData(), globalSettings, opColSettings);
	}

	public HCSMadOperator() {
    	super(new OperatorData("HCS-Mad", false, false, DoubleValue.class, false),
    			GlobalSettings.DEFAULT, OperatorColumnSettings.DEFAULT_EXCL_MISSING);
	}

	public HCSMadOperator(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		super(globalSettings, opColSettings);
	}

	public HCSMadOperator(OperatorData operatorData, GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		super(operatorData, globalSettings, opColSettings);
	}

	/* (non-Javadoc)
	 * @see org.knime.base.data.aggregation.numerical.MedianOperator#getResultInternal()
	 */
	@Override
	protected DataCell getResultInternal() {
		
		// get preference data
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
		double madScalingFactor = prefStore.getDouble(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR);
		
		// return missing, if median is already missing value
		if(super.getResultInternal().isMissing()) return super.getResultInternal();
		
		final List<DataCell> cells = super.getCells();
		
		ExtDescriptiveStats stats = new ExtDescriptiveStats();
        stats.setMadImpl(new MadStatistic(madScalingFactor));
		
		for(DataCell dc : cells) {
			double val = ((DoubleCell)dc).getDoubleValue();
			stats.addValue(val);
		}
			
		try {
			return new DoubleCell(stats.getMad());
		} catch (IllegalMadFactorException e) {
			return new DoubleCell(Double.NaN);
		}
	}

	/* (non-Javadoc)
	 * @see org.knime.base.data.aggregation.numerical.MedianOperator#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Calculates the median absolute deviation value (MAD) per group. (HCS-Tools)";
	}
	
	

}
