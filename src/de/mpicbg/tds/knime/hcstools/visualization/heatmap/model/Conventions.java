package de.mpicbg.tds.knime.hcstools.visualization.heatmap.model;

/**
 * Author: Felix Meyenhofer
 * Date: 12/22/12
 *
 * This class is a collection of arbitrary conventions. This class can be used to try and guess some typical table
 * columns names or attribute names.
 */

public abstract class Conventions {

    public abstract class CBG {

        public abstract class Attr {

            public abstract class Name {
                /** Well attributes */
                public static final String WELL = "well";
                public static final String WELL_COLUMN = "plateColumn";
                public static final String WELL_ROW = "plateRow";

                /** Barcode and derived information */
                public static final String BARCODE = "barcode";
                public static final String TREATMENT = "treatment";
                public static final String LIB_CODE = "library code";
                public static final String LIB_PLATE_NUMBER = "library plate number";

                /** Common names of things */
                public static final String CONCENTRATION = "concentration";
                public static final String CONCENTRATION_UNIT = "unit of concentration";
                public static final String COMPOUND_ID = "compound id";
            }

            public abstract class Value {
                public static final String TREATMENT_LIBRARY = "library";
                public static final String TREATMENT_UNTREATED = "untreated";
            }

        }

    }

}
