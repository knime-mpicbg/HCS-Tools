package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.core.model.Plate;

import java.util.Collection;
import java.util.Comparator;

/**
 * User: Felix Meyenhofer
 * Date: 12/13/12
 * Time: 16:09
 *
 * Class providing the plate comparators. It's a wrapper class, that groups the Comparators for the Plate class in one
 * location. Furthermore it provides easy selection.
 */

public abstract class PlateComparators {


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
     *  Utilities to handle the PlateAttributes
     */
    public static String[] getPlateAttributeTitles(Collection<PlateAttribute> collection) {
        String[] titles = new String[collection.size()];
        int index = 0;
        for (PlateAttribute item : collection) {
            titles[index++] = item.getTitle();
        }
        return titles;
    }

    public static String[] getPlateAttributeNames(Collection<PlateAttribute> collection) {
        String[] attributes = new String[collection.size()];
        int index = 0;
        for (PlateAttribute item : collection) {
            attributes[index++] = item.getName();
        }
        return attributes;
    }

    public static PlateAttribute getPlateAttributeByTitle(String title) {
        for (PlateAttribute attribute : PlateAttribute.values()) {
            if (attribute.getTitle().equals(title)) {
                return attribute;
            }
        }
        return null;
    }



    /**
     * This class makes the connection between the Plate attribute names and the something readable. It's also a way to
     * what attributes are available (Could not find a decent way to derive this directly form the Plate class.
     * TODO: There is surly a neater way to do this. I don't like the fact, that if the Plate class changes, this might fail...
     */
    public enum PlateAttribute {
        SCREENED_AT             ("screenedAt", "Date of Acquisition"),
        LIBRARY_PLATE_NUMBER    ("libraryPlateNumber", "Library Plate Number"),
        BARCODE                 ("barcode", "Barcode"),
        ASSAY                   ("assay", "Assay"),
        LIBRARY_CODE            ("libraryCode", "Library Code"),
        BATCH_NAME              ("batchName", "Batch Name"),
        REPLICATE               ("replicate", "Replicate");

        private final String name;
        private final String title;

        PlateAttribute(String name, String title) {
            this.name = name;
            this.title = title;
        }

        public String getName(){
            return this.name;
        }

        public String getTitle(){
            return this.title;
        }
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
