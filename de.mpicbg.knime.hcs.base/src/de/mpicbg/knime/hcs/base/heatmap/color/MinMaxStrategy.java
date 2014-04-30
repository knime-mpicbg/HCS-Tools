package de.mpicbg.knime.hcs.base.heatmap.color;

import de.mpicbg.tds.core.model.PlateUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;

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
    /** data to be scaled */
    private Collection<Plate> screen;

    /** {@inheritDoc */
    @Override
    public void configure(Collection<Plate> screen) {
        this.screen = screen;

        minMap.clear();
        maxMap.clear();
    }


    /** {@inheritDoc */
    @Override
    public Double getMinValue(final String selectedReadOut) {
        if (!minMap.containsKey(selectedReadOut)) {
            updateMinMaxForReadOut(selectedReadOut);
        }

        return minMap.get(selectedReadOut);
    }

    /** {@inheritDoc */
    @Override
    public Double getMaxValue(String selectedReadOut) {
        if (!maxMap.containsKey(selectedReadOut)) {
            updateMinMaxForReadOut(selectedReadOut);
        }

        return maxMap.get(selectedReadOut);
    }

    /**
     * Calculate the distribution descriptors of a given readout
     *
     * @param selectedReadOut readout to calculate the descriptors for
     */
    private void updateMinMaxForReadOut(final String selectedReadOut) {

        List<Double> doubles = new ArrayList<Double>();
        ArrayList<Well> allWells = new ArrayList<Well>(PlateUtils.flattenWells(screen));

        for (Well allWell : allWells) {
            Double readout = allWell.getReadout(selectedReadOut);
            if (readout != null && !readout.equals(Double.NaN)) {
                doubles.add(readout);
            }
        }

        if (allWells.size() < 2) { // just in case that an empty plate was included into the screen
            System.err.println("screen/plate contains less than 2 wells. Setup of min-max-normalization failed!");
            return;
        }

        // now sort them according to the given readout
        Collections.sort(doubles);

        if (doubles.size() < 2) {
            System.err.println("screen/plate contains less than 2 valid readout for selected readout '" + selectedReadOut + "'. Setup of min-max-normalization failed!");
            return;
        }

        minMap.put(selectedReadOut, doubles.get(0));
        maxMap.put(selectedReadOut, doubles.get(doubles.size() - 1));
    }

    /** {@inheritDoc */
    @Override
    public Double normalize(Double wellReadout, String selectedReadOut) {
        if (wellReadout == null) {
            return null;
        }

        // TODO: Find out why it this statement is here to prevent the update mechanism of the class for the extrema values????
//        if (minMap.isEmpty()) {
//            return null;
//        }

        double minValue = getMinValue(selectedReadOut);
        double maxValue = getMaxValue(selectedReadOut);

        return (wellReadout - minValue) / (maxValue - minValue);
    }

}
