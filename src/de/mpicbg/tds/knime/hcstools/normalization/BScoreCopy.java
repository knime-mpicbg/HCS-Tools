package de.mpicbg.tds.knime.hcstools.normalization;

import de.mpicbg.tds.knime.hcstools.utils.MadStatistic;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.rank.Median;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class BScoreCopy {

    public double[] colResiduals;
    public double[] rowResiduals;
    private RealMatrix input;
    private RealMatrix original;

    double resiudalMAD;
    double inputMean;


    public BScoreCopy(RealMatrix matrix) {
        input = matrix;
        original = matrix.copy();

        rowResiduals = new double[matrix.getRowDimension()];
        colResiduals = new double[matrix.getColumnDimension()];

        doMedianPolish(3);
        resiudalMAD = calcResidualMAD();
    }

    private double calcResidualMAD() {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (double colResidual : colResiduals) {
            if (colResidual == Double.NaN)
                continue;

            stats.addValue(colResidual);
        }

        for (double rowResidual : rowResiduals) {
            if (rowResidual == Double.NaN)
                continue;

            stats.addValue(rowResidual);
        }


        stats.setVarianceImpl(new MadStatistic(stats.getPercentile(50)));
        return stats.getStandardDeviation();
    }


    private void doMedianPolish(int numIt) {


        for (int numItCounter = 0; numItCounter < numIt; numItCounter++) {

            System.err.println("iteration");

            // substract row-medians
            for (int i = 0; i < input.getRowDimension(); i++) {
                double rowMedian = calcNaNAwareMedian(input.getRow(i));

                if (rowMedian != Double.NaN) {
                    input.setRowMatrix(i, input.getRowMatrix(i).scalarAdd(-1 * rowMedian));
                    rowResiduals[i] += rowMedian;
                }
            }


            // remove col-offset
            double colResMedian = calcNaNAwareMedian(colResiduals);
            for (int i = 0; i < colResiduals.length; i++) {
                colResiduals[i] -= colResMedian;

            }
            inputMean += colResMedian;

            // substract col-medians
            for (int j = 0; j < input.getColumnDimension(); j++) {
                double colMedian = calcNaNAwareMedian(input.getColumn(j));

                if (colMedian != Double.NaN) {
                    input.setColumnMatrix(j, input.getColumnMatrix(j).scalarAdd(-1 * colMedian));
                    colResiduals[j] += colMedian;
                }
            }

            // remove row-offset
            double rowResMedian = calcNaNAwareMedian(rowResiduals);
            for (int i = 0; i < rowResiduals.length; i++) {
                rowResiduals[i] -= rowResMedian;

            }
            inputMean += rowResMedian;
        }
    }


    private double calcNaNAwareMedian(double[] row) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (double value : row) {
            if (value == Double.NaN)
                continue;

            stats.addValue(value);
        }

        stats.setMeanImpl(new Median());
        return stats.getMean();
    }


    // could have used the values of input matrix (already residuals) instead of recalculating these values
    public double get(int plateRow, int plateColumn) {
        double predicted = inputMean + rowResiduals[plateRow] + colResiduals[plateColumn];
        return (original.getEntry(plateRow, plateColumn) - predicted) / resiudalMAD;
    }


    public RealMatrix getScoreMatrix() {
        Array2DRowRealMatrix scoreMatrix = new Array2DRowRealMatrix(input.getRowDimension(), input.getColumnDimension());

        for (int i = 0; i < input.getRowDimension(); i++) {
            for (int j = 0; j < input.getColumnDimension(); j++) {
                scoreMatrix.setEntry(i, j, get(i, j));
            }
        }

        return scoreMatrix;
    }


    public static void main(String[] args) {
        //Array2DRowRealMatrix inMatrix = new Array2DRowRealMatrix(new double[][]{{13, 17, 26, 18, 29}, {42, 48, 57, 41, 59}, {34, 31, 36, 22, 41}});
        Array2DRowRealMatrix inMatrix = new Array2DRowRealMatrix(new double[][]{{99, 108, 105, 98, 100, 101},
                {71, 79, 83, 70, 84, 80}, {100, 104, 92, 102, 99, 98}, {81, 75, 80, 82, 77, 78}});

        BScoreCopy bScore = new BScoreCopy(inMatrix);
        System.err.println("value is " + bScore.get(1, 2));
        System.err.println("mpolish " + bScore.input.toString());


        //System.err.println("value is " + bScore.get(1, 2));
        // System.err.println("outmatrix " + bScore.getScoreMatrix().toString());


    }
}
