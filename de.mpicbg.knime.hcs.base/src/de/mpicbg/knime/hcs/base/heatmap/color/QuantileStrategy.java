package de.mpicbg.knime.hcs.base.heatmap.color;

import de.mpicbg.knime.hcs.core.model.PlateUtils;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to rescale a screen consisting of a {@link Collection} of {@link Plate}s
 * between the lower and upper quartile of the value distribution.
 *
 * @author Holger Brandl
 */

public class QuantileStrategy implements RescaleStrategy {

    /** maps minimum values to readout names */
    Map<String, Double> minMap = new HashMap<String, Double>();
    /** maps maximum values to readout names */
    Map<String, Double> maxMap = new HashMap<String, Double>();
    /** readout has constant value? */
    Map<String, Boolean> constValueMap = new HashMap<String, Boolean>();
    /** data to be scaled */
    private Collection<Plate> screen;


    /** {@inheritDoc */
    @Override
    public void configure(Collection<Plate> screen) {
        this.screen = screen;

        minMap.clear();
        maxMap.clear();
        constValueMap.clear();
    }

    /** {@inheritDoc */
    @Override
    public Double getMinValue(final String selectedReadout) {
        if (!minMap.containsKey(selectedReadout)) {
            updateMinMaxForReadOut(selectedReadout);
        }

        return minMap.get(selectedReadout);
    }

    /** {@inheritDoc */
    @Override
    public Double getMaxValue(String selectedReadout) {
        if (!maxMap.containsKey(selectedReadout)) {
            updateMinMaxForReadOut(selectedReadout);
        }

        return maxMap.get(selectedReadout);
    }

    /**
     * Calculate the distribution descriptors of a given readout
     *
     * @param selectedReadout readout to calculate the descriptors for
     */
    private void updateMinMaxForReadOut(final String selectedReadout) {

        DescriptiveStatistics sumStats = new DescriptiveStatistics();
        ArrayList<Well> allWells = new ArrayList<Well>(PlateUtils.flattenWells(screen));

        for (Well allWell : allWells) {
            Double readout = allWell.getReadout(selectedReadout);
            if (readout != null && !readout.equals(Double.NaN)) {
                sumStats.addValue(readout);
            }
        }

/*        if (allWells.isEmpty()) { // just in case that an empty plate was included into the screen
            System.err.println("screen contains no valid wells. Setup of min-max-normalization failed!");
            return;
        }*/

        // now sort them according to the given readout
        
        if(sumStats.getN() < 1) {
        	minMap.put(selectedReadout, Double.NaN);
        	maxMap.put(selectedReadout, Double.NaN);
        	constValueMap.put(selectedReadout, true);
        }

        double q1 = sumStats.getPercentile(25);
        double q3 = sumStats.getPercentile(75);

        double iqr = q3 - q1;

        double min = q1 - 1.5 * iqr;
        double max = q3 + 1.5 * iqr;
        
        double valMin = sumStats.getMin();
        double valMax = sumStats.getMax();

        min = min < valMin ? valMin : min;
        max = max > valMax ? valMax : max;

        minMap.put(selectedReadout, min);
        maxMap.put(selectedReadout, max);
        constValueMap.put(selectedReadout, Double.valueOf(valMin).equals(valMax));
    }

    /** {@inheritDoc */
    @Override
    public Double normalize(Double wellReadout, String selectedReadout) {
        if (wellReadout == null)
            return null;

        // TODO: Find out why it this statement is here to prevent the update mechanism of the class for the extrema values????
//        if (minMap.isEmpty()) {
//            return null;
//        }

        double minValue = getMinValue(selectedReadout);
        double maxValue = getMaxValue(selectedReadout);

        // apply the bounds
        if (wellReadout < minValue) {
            wellReadout = minValue;
        }

        if (wellReadout > maxValue) {
            wellReadout = maxValue;
        }

        return (wellReadout - minValue) / (maxValue - minValue);
    }

	@Override
	public Boolean isConstantReadout(String selectedReadout) {
		if (!constValueMap.containsKey(selectedReadout)) {
            updateMinMaxForReadOut(selectedReadout);
        }

        return constValueMap.get(selectedReadout);
	}

}
