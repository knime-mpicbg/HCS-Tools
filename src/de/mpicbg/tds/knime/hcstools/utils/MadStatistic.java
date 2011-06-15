package de.mpicbg.tds.knime.hcstools.utils;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.UnivariateStatistic;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class MadStatistic implements UnivariateStatistic {

    double median;


    public MadStatistic(double median) {

        this.median = median;
    }


    public double evaluate(double[] doubles) {
        return evaluate(doubles, 0, doubles.length);
    }


    public double evaluate(double[] doubles, int startIncl, int endExcl) {

        DescriptiveStatistics madStats = new DescriptiveStatistics();

        for (int i = startIncl; i < endExcl; i++) {
            madStats.addValue(Math.abs(median - doubles[i]));
        }

        return madStats.getPercentile(50);
    }


    public UnivariateStatistic copy() {
        throw new RuntimeException("Not implemented yet");
    }
}
