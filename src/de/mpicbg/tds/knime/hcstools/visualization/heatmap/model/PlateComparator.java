package de.mpicbg.tds.knime.hcstools.visualization.heatmap.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class providing the plate comparators. It's a wrapper class, that groups the Comparators for the Plate class in one
 * location. Furthermore it provides easy selection.
 *
 * @author Felix Meyenhofer
 *         12/13/12
 */

public class PlateComparator implements Comparator<Plate> {

    /** Field holding the comparators */
    private ArrayList<Comparator<Plate>> comparators = new ArrayList<Comparator<Plate>>();


    /**
     * Constructor for the comparator with the default order
     */
    public PlateComparator() {
        this(Arrays.asList(new PlateAttribute[]{PlateAttribute.SCREENED_AT,
                                                PlateAttribute.LIBRARY_PLATE_NUMBER,
                                                PlateAttribute.REPLICATE,
                                                PlateAttribute.BATCH_NAME,
                                                PlateAttribute.BARCODE}));
    }


    /**
     * Constructor for a comparators of choice
     *
     * @param type of the plate attribute
     */
    public PlateComparator(PlateAttribute type) {
        this.comparators.add(getComparator(type));
    }


    /**
     * Constructor for several comparators
     * The first comparator that returns a valid comparison (hits two comparable values)
     * is gives the result.
     *
     * @param types of the comparators
     */
    public PlateComparator(List<PlateAttribute> types) {
        for (PlateAttribute type : types) {
            this.comparators.add(getComparator(type));
        }
    }


    /**
     * method to fetch a comparator
     *
     * @param type of the attribute
     * @return attribute comparators
     */
    private static Comparator<Plate> getComparator(PlateAttribute type){

        switch (type) {
            case SCREENED_AT:
                return new SortByDate();
            case LIBRARY_PLATE_NUMBER:
                return new SortByLibraryPlateNumber();
            case BARCODE:
                return new SortByBarcode();
            case ASSAY:
                return new SortByAssay();
            case LIBRARY_CODE:
                return new SortByLibraryCode();
            case BATCH_NAME:
                return new SortByBatchName();
            case REPLICATE:
                return new SortByReplicate();

        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * I just takes the comparision result form the first comparator that hit tow valid values.
     */
    @Override
    public int compare(Plate plate, Plate plate2) {
        int result = 0;
        for (Comparator<Plate> comparator : this.comparators) {
            result = comparator.compare(plate, plate2);
            if (result != 0)
                break;

        }
        return result;
    }




    /**
     * Comparator for plate barcodes {@link PlateAttribute#BARCODE}
     */
    private static class SortByBarcode implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getBarcode() == null || p2.getBarcode() == null) return 0;
            return p1.getBarcode().compareTo(p2.getBarcode());
        }
    }

    /**
     * Comparator fo the assay String {@link PlateAttribute#ASSAY}
     */
    private static class SortByAssay implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getAssay() == null || p2.getAssay() == null) return 0;
            return p1.getAssay().compareTo(p2.getAssay());
        }
    }

    /**
     * Comparator fo the library code Integer {@link PlateAttribute#LIBRARY_CODE}
     */
    private static class SortByLibraryCode implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getLibraryCode() == null || p2.getLibraryCode() == null) return 0;
            return p1.getLibraryCode().compareTo(p2.getLibraryCode());
        }
    }

    /**
     * Comparator fo the Batch String {@link PlateAttribute#BATCH_NAME}
     */
    private static class SortByBatchName implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getBatchName() == null || p2.getBatchName() == null) return 0;
            return p1.getBatchName().compareTo(p2.getBatchName());
        }
    }

    /**
     * Comparator fo the Replicate Letter {@link PlateAttribute#REPLICATE}
     */
    private static class SortByReplicate implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getReplicate() == null || p2.getReplicate() == null) return 0;
            return p1.getReplicate().compareTo(p2.getReplicate());
        }
    }

    /**
     * Comparator fo the date {@link PlateAttribute#SCREENED_AT}
     */
    private static class SortByDate implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (p1.getScreenedAt() == null || p2.getScreenedAt() == null) return 0;
            return p1.getScreenedAt().compareTo(p2.getScreenedAt());
        }
    }

    /**
     * Comparator fo the Library plate number (Integer) {@link PlateAttribute#LIBRARY_PLATE_NUMBER}
     */
    private static class SortByLibraryPlateNumber implements Comparator<Plate> {

        /** {@inheritDoc} */
        @Override
        public int compare(Plate p1, Plate p2) {
            if (!(p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null)) {
                return p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber());
            }
            return 0;
        }
    }

}
