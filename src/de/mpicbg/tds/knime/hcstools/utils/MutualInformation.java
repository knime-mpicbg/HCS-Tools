/*
 * Module Name: hcstools
 * This module is a plugin for the KNIME platform <http://www.knime.org/>
 *
 * Copyright (c) 2011.
 * Max Planck Institute of Molecular Cell Biology and Genetics, Dresden
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Detailed terms and conditions are described in the license.txt.
 *     also see <http://www.gnu.org/licenses/>.
 */

package de.mpicbg.tds.knime.hcstools.utils;


import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;


/**
 * Author: Felix Meyenhofer
 * Date: 8/19/11
 * Time: 1:35 PM
 */
public class MutualInformation {


    private Double base = 2.0; // logarithmic base
    private String met = "unbiased";
    private int Nx = 100;    // Number of bins for vector x
    private int Ny = 100;    // Number of bins for vector y
    private Double[] x;      // vector containing samples of variable X
    private Double[] y;      // vector containing samples of variable Y


    // Constructors
    public MutualInformation() {
    }

    public MutualInformation(Double[] x, Double[] y) {
        this.x = x;
        this.y = y;
        this.set_binning();
    }

    public MutualInformation(Double[] x, Double[] y, int n) {
        this.x = x;
        this.y = y;
        this.set_binning(n);
    }

    public MutualInformation(Double[] x, Double[] y, int nx, int ny) {
        this.x = x;
        this.y = y;
        this.set_binning(nx, ny);
    }

    public MutualInformation(Double[] x, Double[] y, String method) {
        this.met = method;
        this.x = x;
        this.y = y;
        this.set_binning();
    }


    // Setter
    public void set_base(Double b) {
        base = b;
    }

    public void set_binning() {
        int bins = (int) Math.ceil(Math.pow(Math.max(x.length, y.length), 1 / 3));
        set_binning(bins);
    }

    public void set_binning(int n) {
        Nx = n;
        Ny = n;
    }

    public void set_binning(int n1, int n2) {
        Nx = n1;
        Ny = n2;
    }

    public void set_xvector(Double[] v) {
        x = v;
    }

    public void set_yvector(Double[] v) {
        y = v;
    }

    public void set_vectors(Double[] v1, Double[] v2) {
        x = v1;
        y = v2;
    }

    public void set_method(String method) {
        met = method;
    }


    // Methods
    public Double[] calculate() throws Exception {
        Double[] res;
        if (met.contentEquals("unbiased")) {
            res = unbiased();
        } else if (met.contentEquals("biased")) {
            res = biased();
        } else if (met.contentEquals("mms_estimate")) {
            res = mms_estimate();
        } else {
            throw new RuntimeException("The met '" + met + "' is unknown.");
        }
        return res;
    }


    // Private helper methods
    private Double[] unbiased() {
        Double[] values = biased();
        values[0] = values[0] - values[2];
        values[2] = 0.0;
        return values;
    }

    private Double[] mms_estimate() {
        Double[] values = biased();
        values[0] = values[0] - values[2];
        Double lambda = Math.pow(values[0], 2) / (Math.pow(values[0], 2) + Math.pow(values[1], 2));
        values[2] = (1 - lambda) * values[0];
        values[0] = lambda * values[0];
        values[1] = lambda * values[1];
        return values;
    }

    private Double[] biased() {
        double[][] H = histogram2();
        // total-sum, row-sum and column-sum
        int r = H.length;
        int c = H[1].length;
        double[] Hx = new double[r];
        double[] Hy = new double[c];
        int count = 0;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                Hx[i] += H[i][j];
                Hy[i] += H[j][i];
                count += H[i][j];
            }
        }
        // Calculate mutual information.
        Double mutualinfo = 0.0;
        Double sigma = 0.0;
        Double logf;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                logf = log(H[i][j], Hx[i], Hy[j]);
                mutualinfo += H[i][j] * logf;
                sigma += H[i][j] * Math.pow(logf, 2);
            }
        }
        mutualinfo /= count;
        sigma = Math.sqrt((sigma / count - Math.pow(mutualinfo, 2)) / (count - 1));
        mutualinfo += Math.log(count);
        Double bias = (double) (r - 1) * (c - 1) / (2 * count);
        // Put the outputs into an array and do log-base transformations.
        Double[] out = new Double[]{mutualinfo, sigma, bias};
        out = basetransform(out, base);
        return out;
    }

    private double[][] histogram2() {
//        Double mi1 = StatUtils.min(x);
//        Double ma1 = StatUtils.max(x);
        Double[] mima1 = minmax(x);
        Double de1 = (mima1[1] - mima1[0]) / (x.length - 1);
        Double lb1 = mima1[0] - de1 / 2;
        Double ub1 = mima1[1] + de1 / 2;
//        Double mi2 = StatUtils.min(y);
//        Double ma2 = StatUtils.max(y);
        Double[] mima2 = minmax(y);
        Double de2 = (mima2[1] - mima2[0]) / (y.length - 1);
        Double lb2 = mima2[0] - de2 / 2;
        Double ub2 = mima2[1] + de2 / 2;
        // Bring the vectors to the same length.
        if (x.length < y.length) {
            x = bootstrap(x, y.length);
        } else if (x.length > y.length) {
            y = bootstrap(y, x.length);
        }
        // Correct the binning.
        if ((Nx >= x.length) || (Ny >= y.length)) {
            set_binning();
            System.out.println("Binning exceeded vector length and was set to" + Nx + ".");
        }
        // Compute the histogram/probability
        double[][] prob = new double[Nx][Ny];
        Double ra1 = (ub1 - lb1);
        Double ra2 = (ub2 - lb2);
        for (int i = 0; i < x.length; i++) {
            int ind1 = (int) Math.round((x[i] - lb1) / ra1 * Nx + 0.5);
            int ind2 = (int) Math.round((y[i] - lb2) / ra2 * Ny + 0.5);
            if ((1 <= ind1) & (ind1 <= Nx) & (1 <= ind2) & (ind2 <= Ny)) {
                prob[ind1 - 1][ind2 - 1] += 1;
            }
        }
        return prob;
    }

    private Double[] minmax(Double[] vect) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Double value : vect) {
            stats.addValue(value);
        }
        return new Double[]{stats.getMin(), stats.getMax()};
    }

    private Double log(Double hxy, Double hx, Double hy) {
        if ((hxy < 1e-6)) // || (hy < 1e-6) || (hxy < 1e6) )
            return 0.0;
        else
            return Math.log(hxy / hx / hy);
    }

    private Double[] basetransform(Double[] v, Double b) {
        for (int i = 0; i < v.length; i++) {
            v[i] /= Math.log(b);
        }
        return v;
    }

    private Double[] bootstrap(Double[] v, int Nboot) {
        Double[] boot = new Double[Nboot];
        int I;
        int maxI = v.length - 1;
        RandomData rand = new RandomDataImpl();
        for (int r = 0; r < Nboot; ++r) {
            I = rand.nextInt(0, maxI);
            boot[r] = v[I];
        }
        return boot;
    }


    // Testing
    public static void main(String[] args) throws Exception {

        Double[] a = new Double[]{1.0, 2.0, 2.0, 2.0, 0.0, 0.0, 1.0, 0.0, 1.0, 2.0};
        Double[] b = new Double[]{1.0, 2.0, 2.0, 2.0, 2.0, 1.0, 0.0, 2.0, 1.0, 0.0};

        MutualInformation mutinf = new MutualInformation(a, b, 3);
        Double[] res = new Double[3];
        res = mutinf.calculate();
        System.err.println("mutual information (" + mutinf.met + ", log" + mutinf.base + "): " + res[0] + ", sigma: " + res[1] + ", bias: " + res[2]);
        mutinf.set_method("biased");
        res = mutinf.calculate();
        System.err.println("mutual information (" + mutinf.met + ", log" + mutinf.base + "): " + res[0] + ", sigma: " + res[1] + ", bias: " + res[2]);
    }


}