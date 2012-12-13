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
                return getPlateNumberComparator();
            case BARCODE:
                return null;
            case ASSAY:
                return null;
            case LIBRARY_CODE:
                return null;
            case BATCH_NAME:
                return null;
            case REPLICATE:

        }
        return null;
    }

    public static Comparator<Plate> getDateComparator(){
        return new SortByDate();
    }

    public static Comparator<Plate> getPlateNumberComparator(){
        return new SortByPlateNumber();
    }


    // Utilities to handle the PlateAttributes
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
     * User: niederle
     * Date: 10/6/11
     * Time: 5:33 PM
     */
    private static class SortByDate implements Comparator<Plate> {

        public int compare(Plate p1, Plate p2) {
            if (p1.getScreenedAt() == null || p2.getScreenedAt() == null) return 0;
            return p1.getScreenedAt().compareTo(p2.getScreenedAt());
        }
    }



    /**
     * User: niederle
     * Date: 10/6/11
     * Time: 5:43 PM
     */
    private static class SortByPlateNumber implements Comparator<Plate> {
        public int compare(Plate p1, Plate p2) {

            if (p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null) return 0;
            // debug values
            System.out.println("Is Plate " + p1.getLibraryPlateNumber() + " < " + p2.getLibraryPlateNumber());
            if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) < 0) System.out.println("yes");
            if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) > 0) System.out.println("no");

            if (!(p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null)) {
                return p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber());
            }
            return 0;
        }
    }


}
