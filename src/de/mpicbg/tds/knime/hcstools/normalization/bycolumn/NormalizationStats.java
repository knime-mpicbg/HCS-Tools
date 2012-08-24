package de.mpicbg.tds.knime.hcstools.normalization.bycolumn;

import de.mpicbg.tds.knime.hcstools.utils.ExtDescriptiveStats;
import de.mpicbg.tds.knime.hcstools.utils.MadStatistic;

import java.util.List;

/**
 * class to hold statistic data
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/23/12
 * Time: 1:34 PM
 */
public class NormalizationStats {

    // hold mean or median of the values
    private double mean_median;
    // holds standard deviation or median absolute deviation of the values
    private double sd_mad;
    // holds the total number of values
    private long nSamples;
    // holds the number of missing values
    private int nMissing;

    /**
     * constructor
     */
    public NormalizationStats() {
        this.mean_median = Double.NaN;
        this.sd_mad = Double.NaN;
        this.nSamples = 0;
        this.nMissing = 0;
    }

    public double getMean_median() {
        return mean_median;
    }

    public double getSd_mad() {
        return sd_mad;
    }

    public int getnSamples() {
        return (new Long(nSamples).intValue());
    }

    public boolean hasEnoughSamples(int minSamples) {
        return nSamples >= minSamples;
    }

    public int getnMissing() {
        return nMissing;
    }

    /**
     * set class members by using the extended statistic class
     *
     * @param values
     * @param madScalingFactor
     * @param useRobustStats
     * @return
     * @throws de.mpicbg.tds.knime.hcstools.utils.MadStatistic.IllegalMadFactorException
     *
     */
    public NormalizationStats init(List<Double> values, double madScalingFactor, boolean useRobustStats) throws MadStatistic.IllegalMadFactorException {
        ExtDescriptiveStats stats = new ExtDescriptiveStats();
        stats.setMadImpl(new MadStatistic(madScalingFactor));

        // fill data
        for (double val : values) {
            if (Double.isNaN(val)) nMissing++;
            else stats.addValue(val);
        }

        // sample size
        nSamples = values.size();
        mean_median = (useRobustStats) ? stats.getMedian() : stats.getMean();
        sd_mad = (useRobustStats) ? stats.getMad() : stats.getStandardDeviation();
        return this;
    }

}
