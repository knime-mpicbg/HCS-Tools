package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.view.color.ReadoutRescaleStrategy;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class QuantileSmoothedStrategy implements ReadoutRescaleStrategy {

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

        DescriptiveStatistics sumStats = new DescriptiveStatistics();
        ArrayList<Well> allWells = new ArrayList<Well>(TdsUtils.flattenWells(screen));

        for (Well allWell : allWells) {
            Double readout = allWell.getReadout(selectedReadOut);
            if (readout != null && !readout.equals(Double.NaN)) {
                sumStats.addValue(readout);
            }
        }

        if (allWells.isEmpty()) { // just in case that an empty plate was included into the screen
            System.err.println("screen contains no valid wells. Setup of min-max-normalization failed!");
            return;
        }

        // now sort them according to the given readout


        double q1 = sumStats.getPercentile(25);
        double q3 = sumStats.getPercentile(75);

        double iqr = q3 - q1;

        double min = q1 - 1.5 * iqr;
        double max = q3 + 1.5 * iqr;

        min = min < sumStats.getMin() ? sumStats.getMin() : min;
        max = max > sumStats.getMax() ? sumStats.getMax() : max;

        minMap.put(selectedReadOut, min);
        maxMap.put(selectedReadOut, max);
    }


    public Double normalize(Double wellReadout, String selectedReadOut) {
        if (wellReadout == null)
            return null;

        if (minMap.isEmpty()) {
            return null;
        }

        double minValue = getMinValue(selectedReadOut);
        double maxValue = getMaxValue(selectedReadOut);

        // apply the bounds
        if (wellReadout < minValue) {
            wellReadout = minValue;
        }

        if (wellReadout > maxValue) {
            wellReadout = maxValue;
        }

        return (wellReadout - minValue) / (maxValue - minValue);
    }
}
