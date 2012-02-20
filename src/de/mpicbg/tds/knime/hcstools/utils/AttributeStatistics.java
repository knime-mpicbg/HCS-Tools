package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import de.mpicbg.tds.knime.hcstools.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.tds.knime.knutils.Attribute;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataRow;
import org.knime.core.node.NodeLogger;

import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class AttributeStatistics {

    private static NodeLogger logger = NodeLogger.getLogger(AttributeStatistics.class);

    public static double median(List<DataRow> rows, Attribute attribute) {
        ExtDescriptiveStats statistics = accuStats(rows, attribute);

        return statistics.getMedian();
    }


    public static double mean(List<DataRow> rows, Attribute attribute) {
        ExtDescriptiveStats stats = accuStats(rows, attribute);

        ensureMinMeanSamples(stats.getN(), attribute);

        return stats.getMean();
    }


    public static double mad(List<DataRow> rows, Attribute attribute) {
        return mad(rows, attribute, getMadScalingFromPrefs());
    }


    public static double mad(List<DataRow> rows, Attribute attribute, double madScalingFactor) {
        ExtDescriptiveStats statistics = accuStats(rows, attribute);
        statistics.setMadImpl(new MadStatistic(madScalingFactor));

        ensureMinDispersionSamples(statistics.getN(), attribute);

        return statistics.getMad();
    }


    private static double getMadScalingFromPrefs() {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        double madScalingFactor = prefStore.getDouble(HCSToolsPreferenceInitializer.MAD_SCALING_FACTOR);


        if (madScalingFactor <= 0) {
            logger.error("MAD scaling factor has to be greater than 0 (see preference settings)");
        }

        return madScalingFactor;
    }


    private static void ensureMinDispersionSamples(long numSamples, Attribute attribute) {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        int minSamples = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_DISPERSION);

        if (numSamples < minSamples) {
            logger.warn("too few samples (" + numSamples + ")while calculating standard-deviation/mad for attribute: " + attribute.getName());
        }
    }


    private static void ensureMinMeanSamples(long numSamples, Attribute attribute) {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        int minSamples = prefStore.getInt(HCSToolsPreferenceInitializer.MIN_SAMPLE_NUMBER_FOR_MEANS);

        if (numSamples < minSamples) {
            logger.error("too few samples while calculating mean-statistic for attribute " + attribute.getName());
        }
    }


    public static double stdDev(List<DataRow> rows, Attribute attribute) {
        ExtDescriptiveStats statistics = accuStats(rows, attribute);

        ensureMinDispersionSamples(statistics.getN(), attribute);

        return statistics.getStandardDeviation();
    }


    public static double getVariance(List<DataRow> rows, Attribute attribute) {
        ExtDescriptiveStats statistics = accuStats(rows, attribute);

        ensureMinDispersionSamples(statistics.getN(), attribute);

        return statistics.getVariance();
    }


    public static ExtDescriptiveStats accuStats(List<DataRow> rows, Attribute attribute) {
        ExtDescriptiveStats statistics = new ExtDescriptiveStats();

        for (DataRow row : rows) {
            Double doubleAttribute = attribute.getDoubleAttribute(row);

            // ignore missing values
            if (doubleAttribute == null) {
                continue;
            }

            statistics.addValue(doubleAttribute);
        }

        return statistics;
    }
}


