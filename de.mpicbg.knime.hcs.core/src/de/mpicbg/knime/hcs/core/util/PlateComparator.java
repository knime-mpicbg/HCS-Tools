package de.mpicbg.knime.hcs.core.util;

import de.mpicbg.knime.hcs.core.model.Plate;

import java.util.Comparator;
import java.util.Date;


/**
 * @author Holger Brandl
 */
@Deprecated // moved to the model package
public class PlateComparator extends MultiComparator<Plate> {

    public PlateComparator() {
        addComparator(new ScreeningDateComparator());
        addComparator(new LibCodeComparator());
        addComparator(new LibPlateNumberComparator());
//        addComparator(new ScreeningDateComparator());
    }


    private static class ScreeningDateComparator implements Comparator<Plate> {

        public int compare(Plate plateA, Plate plateB) {
            Date dataA = plateA.getScreenedAt();
            Date dateB = plateB.getScreenedAt();

            if (dataA == null && dateB == null) {
                return 0;
            }

            if (dataA == null)
                return 1;

            if (dateB == null) {
                return -1;
            }

            return dataA.compareTo(dateB);
        }
    }


    private static class BatchComparator implements Comparator<Plate> {

        public int compare(Plate plateA, Plate plateB) {
            String batchA = plateA.getBatchName();
            String batchB = plateB.getBatchName();

            if (batchA == null && batchB == null)
                return 0;

            if (batchA == null)
                return 1;

            if (batchB == null)
                return -1;

            return batchA.compareTo(batchB);
        }
    }


    private static class LibCodeComparator implements Comparator<Plate> {

        public int compare(Plate plateA, Plate plateB) {
            String libraryCodeA = plateA.getLibraryCode();
            String libraryCodeB = plateB.getLibraryCode();

            if (libraryCodeA == null && libraryCodeB == null)
                return 0;

            if (libraryCodeA == null)
                return 1;

            if (libraryCodeB == null)
                return -1;

            return libraryCodeA.compareTo(libraryCodeB);
        }
    }


    private static class LibPlateNumberComparator implements Comparator<Plate> {

        public int compare(Plate plateA, Plate plateB) {
            Integer libPlateNumberA = plateA.getLibraryPlateNumber();
            Integer libPlateNumberB = plateB.getLibraryPlateNumber();

            if (libPlateNumberA == null && libPlateNumberB == null)
                return 0;

            if (libPlateNumberA == null)
                return 1;

            if (libPlateNumberB == null)
                return -1;

            return libPlateNumberA.compareTo(libPlateNumberB);
        }
    }


    private static class BarcodeComparator implements Comparator<Plate> {

        public int compare(Plate plateA, Plate plateB) {
            String barcodeA = plateA.getBarcode();
            String barcodeB = plateB.getBarcode();

            if (barcodeA == null && barcodeB == null)
                return 0;

            if (barcodeA == null)
                return 1;

            if (barcodeB == null)
                return -1;

            return barcodeA.compareTo(barcodeB);
        }
    }
}
