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


import de.mpicbg.knime.knutils.Attribute;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.knime.core.data.DataRow;

import java.util.List;

/**
 * Author: Felix Meyenhofer
 * Date: 7/5/11
 * Time: 6:59 PM
 */

public class Table2Matrix {


    public static RealMatrix extractMatrix(List<DataRow> rows, List<Attribute> params) {
        double[][] matrix = new double[rows.size()][params.size()];
        int nbparams = params.size();
        int m = 0;
        for (DataRow row : rows) {
            int n = 0;
            for (Attribute readout : params) {
                Double val = readout.getDoubleAttribute(row);
                if ((val == null) || Double.isInfinite(val) || Double.isNaN(val)) {
                    break;
                }
                matrix[m][n] = val;
                n += 1;
            }
            if (n == nbparams) {
                m += 1;
            }
        }
        // remove the unused rows.
        RealMatrix rmatrix = new Array2DRowRealMatrix(matrix);
        if (m > 0) {
            rmatrix = rmatrix.getSubMatrix(0, m - 1, 0, nbparams - 1);
        }
        return rmatrix;
    }

}
