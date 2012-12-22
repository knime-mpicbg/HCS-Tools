package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;

import java.util.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */

public class MinMaxStrategy implements RescaleStrategy {

    Map<String, Double> minMap = new HashMap<String, Double>();
    Map<String, Double> maxMap = new HashMap<String, Double>();

    private Collection<Plate> screen;


    public void configure(Collection<Plate> screen) {
        this.screen = screen;

        minMap.clear();
        maxMap.clear();
    }


    public Double getMinValue(final String selectedReadOut) {
        if (!minMap.containsKey(selectedReadOut)) {
            updateMinMaxForReadOut(selectedReadOut);
        }

        return minMap.get(selectedReadOut);
    }


    public Double getMaxValue(String selectedReadOut) {
        if (!maxMap.containsKey(selectedReadOut)) {
            updateMinMaxForReadOut(selectedReadOut);
        }

        return maxMap.get(selectedReadOut);
    }


    private void updateMinMaxForReadOut(final String selectedReadOut) {

        List<Double> doubles = new ArrayList<Double>();
        ArrayList<Well> allWells = new ArrayList<Well>(TdsUtils.flattenWells(screen));

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
