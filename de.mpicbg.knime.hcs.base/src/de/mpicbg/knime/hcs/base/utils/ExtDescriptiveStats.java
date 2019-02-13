package de.mpicbg.knime.hcs.base.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.util.ResizableDoubleArray;

/**
 * The class provides further statistics
 * Extensions:
 * - median absolute deviation
 * - median
 * <p/>
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 2/20/12
 * Time: 10:48 AM
 */
public class ExtDescriptiveStats extends DescriptiveStatistics {

    public static final String WARN_NOT_ENOUGH_MEAN_SAMPLES = "too few samples for mean / median estimation";
    public static final String WARN_NOT_ENOUGH_SD_SAMPLES = "too few samples for sd / mad estimation";

    /**
     * Median absolute deviation implementation - can be reset by setter.
     */
    private UnivariateStatistic madImpl = new MadStatistic();

    /**
     * Constructor
     */
    public ExtDescriptiveStats() {
        super();
    }

    /**
     * Contructor to provide a data array
     *
     * @param data
     */
    public ExtDescriptiveStats(ResizableDoubleArray data) {
        //super.eDA = data;
        super(data.getElements());
    }

    /**
     * @return 50th percentile of the data; dependent on PercentileImpl
     */
    public double getMedian() {
        return getPercentile(50);
    }

    /**
     * @return median absolute deviation; dependent on PercentileImpl and MadImpl
     */
    public double getMad() throws MadStatistic.IllegalMadFactorException {
        //calculate the median and set the value for the MAD calculation
        double median = getPercentile(50);
        if (madImpl instanceof MadStatistic) {
            ((MadStatistic) madImpl).setMedian(median);
            ((MadStatistic) madImpl).checkMadFactor();
        } else {
            try {
                madImpl.getClass().getMethod("setMedian", new Class[]{Double.TYPE}).invoke(madImpl,
                        new Object[]{median});
            } catch (NoSuchMethodException e1) { // Setter guard should prevent
                throw new IllegalArgumentException("Percentile implementation does not support setQuantile");
            } catch (IllegalAccessException e2) {
                throw new IllegalArgumentException("IllegalAccessException setting quantile");
            } catch (InvocationTargetException e3) {
                throw new IllegalArgumentException("Error setting quantile" + e3.toString());
            }
        }

        return apply(madImpl);
    }

    public void setMadImpl(UnivariateStatistic madImpl) {
        this.madImpl = madImpl;
    }


    public static void main(String[] args) {
        double[] vec = {-1.47367098, 2.33110135, -0.01785387, 0.04113220, -0.18595962,
                0.05292957, 1.50823067, 0.23152174, -0.08921781, 0.38920663, Double.NaN};

        ExtDescriptiveStats stats = new ExtDescriptiveStats();

        for (int i = 0; i < vec.length; i++)
            stats.addValue(vec[i]);

        double median = stats.getMedian();
        double mad = 0;
        try {
            mad = stats.getMad();
        } catch (MadStatistic.IllegalMadFactorException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("Median: " + median);
        System.out.println("Mad: " + mad);
    }
}
