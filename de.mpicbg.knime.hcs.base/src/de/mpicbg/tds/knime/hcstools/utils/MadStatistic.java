package de.mpicbg.tds.knime.hcstools.utils;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.UnivariateStatistic;


/**
 * Implementation of the calculation of the median absolute deviation
 *
 * @author Holger Brandl, Antje Niederlein
 */
public class MadStatistic implements UnivariateStatistic {

    /**
     * MAD-factor of gaussian distributed data
     */
    public static double MAD_GAUSS_FACTOR = 1.4826;

    /**
     * Factor to apply to the MAD to get a good robust estimate of the standard deviation
     */
    private double m_madFactor = MAD_GAUSS_FACTOR;

    /**
     * median value of the data
     */
    private double median = Double.NaN;

    /**
     * Constructor
     */
    public MadStatistic() {
    }

    /**
     * Contructor with mad scaling factor
     *
     * @param m_madFactor
     */
    public MadStatistic(double m_madFactor) {
        this.m_madFactor = m_madFactor;
    }

    /**
     * Overridden from UnivariateStatistics
     *
     * @param values
     * @return
     */
    public double evaluate(double[] values) {
        return evaluate(values, 0, values.length);
    }

    /**
     * Overridden from UnivariateStatistics
     *
     * @param values
     * @param startIncl
     * @param endExcl
     * @return
     */
    public double evaluate(double[] values, int startIncl, int endExcl) {

        DescriptiveStatistics madStats = new DescriptiveStatistics();

        for (int i = startIncl; i < endExcl; i++) {
            madStats.addValue(Math.abs(median - values[i]));
        }

        return madStats.getPercentile(50) * m_madFactor;
    }


    /**
     * Overridden from UnivariateStatistics
     *
     * @return
     */
    public UnivariateStatistic copy() {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * @return the correction factor of the mad statistics
     */
    public double getMadFactor() {
        return m_madFactor;
    }

    /**
     * sets the correction factor of the mad statistics
     *
     * @param madFactor
     */
    public void setMadFactor(double madFactor) {
        this.m_madFactor = madFactor;
    }

    /**
     * sets the median value of the data
     *
     * @param median
     */
    public void setMedian(double median) {
        this.median = median;
    }

    public void checkMadFactor() throws IllegalMadFactorException {
        if (m_madFactor <= 0)
            throw new IllegalMadFactorException("MAD scaling factor has to be greater than 0 (see preference settings)");
    }


    public static void main(String[] args) {
        double values[] = {0.63365672, 0.73697871, 0.59948635, 0.99698017, 0.18938888,
                0.78711912, 0.84631022, 0.92974322, 0.22606262, 0.05171261};

        ExtDescriptiveStats stats = new ExtDescriptiveStats();

        // test if the change of the scaling factor works
        // stats.setMadImpl(new MadStatistic(1.0));

        for (int i = 0; i < values.length; i++)
            stats.addValue(values[i]);

        double mad = 0;
        try {
            mad = stats.getMad();
        } catch (IllegalMadFactorException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("Mad: " + mad);
    }


    public class IllegalMadFactorException extends Exception {
        public IllegalMadFactorException(String e) {
            super(e);
        }
    }
}
