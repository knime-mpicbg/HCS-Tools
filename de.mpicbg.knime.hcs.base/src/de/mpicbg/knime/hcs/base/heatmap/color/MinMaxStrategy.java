package de.mpicbg.knime.hcs.base.heatmap.color;

import de.mpicbg.knime.hcs.core.model.PlateUtils;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;

import java.util.*;

/**
 * Class to rescale a screen consisting of a {@link Collection} of {@link Plate}s
 * between the minimum and maximum values of the distribution
 *
 * @author Holger Brandl
 */

public class MinMaxStrategy implements RescaleStrategy {

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

        List<Double> doubles = new ArrayList<Double>();
        ArrayList<Well> allWells = new ArrayList<Well>(PlateUtils.flattenWells(screen));

        for (Well allWell : allWells) {
            Double readout = allWell.getReadout(selectedReadout);
            if (readout != null && !readout.equals(Double.NaN)) {
                doubles.add(readout);
            }
        }

/*        if (allWells.size() < 2) { // just in case that an empty plate was included into the screen
            System.err.println("screen/plate contains less than 2 wells. Setup of min-max-normalization failed!");
            return;
        }*/

        // now sort them according to the given readout
        Collections.sort(doubles);

/*        if (doubles.size() < 2) {
            System.err.println("screen/plate contains less than 2 valid readout for selected readout '" + selectedReadOut + "'. Setup of min-max-normalization failed!");
            return;
        }*/
        
        if(doubles.isEmpty()) {
        	minMap.put(selectedReadout, Double.NaN);
        	maxMap.put(selectedReadout, Double.NaN);
        	constValueMap.put(selectedReadout, true);
        	return;
        }
        	

        Double minValue = doubles.get(0);
        Double maxValue = doubles.get(doubles.size() - 1);
        minMap.put(selectedReadout, minValue);
        maxMap.put(selectedReadout, maxValue);
        constValueMap.put(selectedReadout, minValue.equals(maxValue));
    }

    /** {@inheritDoc */
    @Override
    public Double normalize(Double wellReadout, String selectedReadout) {
        if (wellReadout == null) {
            return null;
        }
        
        // if readout has constant values only
        if(isConstantReadout(selectedReadout)) return getMinValue(selectedReadout);

        // TODO: Find out why it this statement is here to prevent the update mechanism of the class for the extrema values????
//        if (minMap.isEmpty()) {
//            return null;
//        }

        Double minValue = getMinValue(selectedReadout);
        Double maxValue = getMaxValue(selectedReadout);

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
