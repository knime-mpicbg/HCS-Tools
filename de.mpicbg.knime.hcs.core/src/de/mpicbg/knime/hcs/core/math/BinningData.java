package de.mpicbg.knime.hcs.core.math;

/**
 * Container class for BinningAnalysis
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 2/28/12
 * Time: 1:40 PM
 */
public class BinningData {
    Interval interval = null;
    double zscore = Double.NaN;
    double percentage = Double.NaN;
    double count = Double.NaN;

    public BinningData() {
    }

    public BinningData(Interval interval, double zscore, double percentage, double sampleSize) {
        this.interval = interval;
        this.zscore = zscore;
        this.percentage = percentage;
        this.count = sampleSize;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public double getZscore() {
        return zscore;
    }

    public void setZscore(double zscore) {
        this.zscore = zscore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
}
