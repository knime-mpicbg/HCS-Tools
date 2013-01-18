package de.mpicbg.tds.knime.hcstools.visualization.heatmap.model;

import java.util.Comparator;

/**
 * Class providing the plate comparators. It's a wrapper class, that groups the Comparators for the Plate class in one
 * location. Furthermore it provides easy selection.
 *
 * @author Felix Meyenhofer
 *         12/13/12
 */

public abstract class PlateComparators {


    /**
     * Constructor accepting the plate attribute as input to choose the correct comparator
     *
     * @param type of the attribute
     * @return attribute comparator
     */
    public static Comparator<Plate> getComparator(PlateAttribute type){

        switch (type) {
            case SCREENED_AT:
                return getDateComparator();
            case LIBRARY_PLATE_NUMBER:
                return getLibraryPlateNumberComparator();
            case BARCODE:
                return getBarcodeComparator();
            case ASSAY:
                return getAssayComparator();
            case LIBRARY_CODE:
                return getLibraryCodeComparator();
            case BATCH_NAME:
                return getBatchNameComparator();
            case REPLICATE:
                return getReplicateComparator();

        }
        return null;
    }

    public static Comparator<Plate> getDateComparator(){
        return new SortByDate();
    }

    public static Comparator<Plate> getLibraryPlateNumberComparator(){
        return new SortByLibraryPlateNumber();
    }

    public static Comparator<Plate> getBarcodeComparator(){
        return new SortByBarcode();
    }

    public static Comparator<Plate> getAssayComparator(){
        return new SortByAssay();
    }

    public static Comparator<Plate> getLibraryCodeComparator(){
        return new SortByLibraryCode();
    }

    public static Comparator<Plate> getBatchNameComparator(){
        return new SortByBatchName();
    }

    public static Comparator<Plate> getReplicateComparator(){
        return new SortByReplicate();
    }





    /**
     *  Comparator classes accessing the respective Plate getters.
     */
    private static class SortByBarcode implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getBarcode() == null || p2.getBarcode() == null) return 0;
            return p1.getBarcode().compareTo(p2.getBarcode());
        }
    }

    private static class SortByAssay implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getAssay() == null || p2.getAssay() == null) return 0;
            return p1.getAssay().compareTo(p2.getAssay());
        }
    }

    private static class SortByLibraryCode implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getLibraryCode() == null || p2.getLibraryCode() == null) return 0;
            return p1.getLibraryCode().compareTo(p2.getLibraryCode());
        }
    }

    private static class SortByBatchName implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getBatchName() == null || p2.getBatchName() == null) return 0;
            return p1.getBatchName().compareTo(p2.getBatchName());
        }
    }

    private static class SortByReplicate implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getReplicate() == null || p2.getReplicate() == null) return 0;
            return p1.getReplicate().compareTo(p2.getReplicate());
        }
    }

    private static class SortByDate implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getScreenedAt() == null || p2.getScreenedAt() == null) return 0;
            return p1.getScreenedAt().compareTo(p2.getScreenedAt());
        }
    }

    private static class SortByLibraryPlateNumber implements Comparator<Plate> {
        public int compare(Plate p1, Plate p2) {

//            if (p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null) return 0;
//            // debug values
//            System.out.println("Is Plate " + p1.getLibraryPlateNumber() + " < " + p2.getLibraryPlateNumber());
//            if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) < 0) System.out.println("yes");
//            if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) > 0) System.out.println("no");

            if (!(p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null)) {
                return p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber());
            }
            return 0;
        }
    }

}
