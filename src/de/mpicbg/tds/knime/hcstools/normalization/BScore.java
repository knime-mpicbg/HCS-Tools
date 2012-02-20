package de.mpicbg.tds.knime.hcstools.normalization;

import de.mpicbg.tds.knime.hcstools.utils.ExtDescriptiveStats;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;


/**
 * Bscore-Reference:
 * Brideau C, Gunter B, Pikounis B, Liaw A: Improved statistical methods
 * for hit selection in high-throughput screening. J Biomol Screen 2003;8:634-647.
 * <p/>
 * Calculation is done plate wise
 * <p/>
 * 2-way-Median polish:
 * residual(ij)     = y(ij) - y_fit(ij)
 * = y(ij) - ( grand_effect(plate) + row_effect(i) + column_effect(J) )
 * <p/>
 * BScore:
 * Bscore(ij) = residual(ij) / mad(residuals(plate))
 *
 * @author Holger Brandl, Antje Niederlein
 */
public class BScore {

    private double[] colEffect;             // stores the column effect
    private double[] rowEffect;             // stores the row effect
    private RealMatrix residualMatrix;
    private RealMatrix original;

    double resiudalMAD;
    double grandEffect;


    public BScore(RealMatrix matrix) {
        // initialize
        residualMatrix = matrix;
        original = matrix.copy();

        rowEffect = new double[matrix.getRowDimension()];
        colEffect = new double[matrix.getColumnDimension()];

        // run the 2 way median polish
        medianPolish(3);
        // calculate the mad of the residuals
        resiudalMAD = calcResidualMAD();
    }

    /**
     * @return median absolute deviation of the residuals
     */
    private double calcResidualMAD() {

        ExtDescriptiveStats stats = new ExtDescriptiveStats();

        // create double array from residual matrix
        for (int row = 0; row < residualMatrix.getRowDimension(); row++) {
            double[] rowResiduals = residualMatrix.getRow(row);
            for (int i = 0; i < rowResiduals.length; i++) {
                if (((Double) rowResiduals[i]).isNaN())
                    continue;
                stats.addValue(rowResiduals[i]);
            }
        }

        return stats.getMad();
    }

    /**
     * implementation of median polish algorithm
     * TODO: implement stopping criteria
     *
     * @param numIt the maximum number of iterations
     */
    private void medianPolish(int numIt) {

        for (int numItCounter = 0; numItCounter < numIt; numItCounter++) {

            System.err.println("iteration");

            // row step

            // substract row-medians
            for (int i = 0; i < residualMatrix.getRowDimension(); i++) {
                double rowMedian = calcMedian(residualMatrix.getRow(i));

                if (rowMedian != Double.NaN) {
                    residualMatrix.setRowMatrix(i, residualMatrix.getRowMatrix(i).scalarAdd(-1 * rowMedian));
                    rowEffect[i] += rowMedian;
                }
            }


            // remove col-offset
            double colResMedian = calcMedian(colEffect);
            for (int i = 0; i < colEffect.length; i++) {
                colEffect[i] -= colResMedian;

            }
            grandEffect += colResMedian;

            //column step

            // substract col-medians
            for (int j = 0; j < residualMatrix.getColumnDimension(); j++) {
                double colMedian = calcMedian(residualMatrix.getColumn(j));

                if (colMedian != Double.NaN) {
                    residualMatrix.setColumnMatrix(j, residualMatrix.getColumnMatrix(j).scalarAdd(-1 * colMedian));
                    colEffect[j] += colMedian;
                }
            }

            // remove row-offset
            double rowResMedian = calcMedian(rowEffect);
            for (int i = 0; i < rowEffect.length; i++) {
                rowEffect[i] -= rowResMedian;

            }
            grandEffect += rowResMedian;
        }
    }

    /**
     * eliminates missing/infinite values and returns the median of the remaining values
     *
     * @param row
     * @return
     */
    private double calcMedian(double[] row) {

        ExtDescriptiveStats stats = new ExtDescriptiveStats();

        // remove datapoint which are NaN or Infinite
        for (double value : row) {
            //Double rowValue = value;
            if (((Double) value).isNaN() || ((Double) value).isInfinite())
                continue;
            stats.addValue(value);
        }

        return stats.getMedian();

    }

    /**
     * @param plateRow
     * @param plateColumn
     * @return bscore value of a certain position
     */
    public double get(int plateRow, int plateColumn) {
        return residualMatrix.getEntry(plateRow, plateColumn) / resiudalMAD;
    }

    //look if there is anything to change here; it's not used at all
    /*public RealMatrix getScoreMatrix() {
        Array2DRowRealMatrix scoreMatrix = new Array2DRowRealMatrix(residualMatrix.getRowDimension(), residualMatrix.getColumnDimension());

        for (int i = 0; i < residualMatrix.getRowDimension(); i++) {
            for (int j = 0; j < residualMatrix.getColumnDimension(); j++) {
                scoreMatrix.setEntry(i, j, get(i, j));
            }
        }

        return scoreMatrix;
    }*/


    public static void main(String[] args) {
        //Array2DRowRealMatrix inMatrix = new Array2DRowRealMatrix(new double[][]{{13, 17, 26, 18, 29}, {42, 48, 57, 41, 59}, {34, 31, 36, 22, 41}});
        Array2DRowRealMatrix inMatrix = new Array2DRowRealMatrix(new double[][]{{99, 108, 105, 98, 100, 101},
                {71, 79, 83, 70, 84, 80}, {100, 104, 92, 102, 99, 98}, {81, 75, 80, 82, 77, 78}});

        BScore bScore = new BScore(inMatrix);
        System.err.println("value is " + bScore.get(1, 2));
        System.err.println("mpolish " + bScore.residualMatrix.toString());
    }
}
