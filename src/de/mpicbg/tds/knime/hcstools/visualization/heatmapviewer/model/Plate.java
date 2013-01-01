package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model;

import de.mpicbg.tds.barcodes.BarcodeParser;

import java.io.Serializable;
import java.util.*;


/**
 * A model of microtiter plates containing typically between 96 (12 x 8) or 384 (24 x 16) wells.
 *
 * @author Holger Brandl
 */
public class Plate implements Serializable{

    /**
     * Number of well rows and well columns in the plate.
     */
    private int numRows = 16;
    private int numColumns = 24;

//    private Screen screen;

    /**
     * Barcode of the plate
     */
    private String barcode;

        /**
         * Date of meansurement / cell passage (Usually part of the barcode)
         */
        private Date screenedAt;

        /**
         * ? (Usually part of the barcode)
         */
        private String batchName;

        /**
         * Code/ID of the molecule library and the number of the library plate (Usually part of the barcode)
         */
        private String libraryCode;
        private Integer libraryPlateNumber;

        /**
         * Contains the assay description part of the barcode (Usually part of the barcode)
         */
        private String assay;

        /**
         * Replicate indicator (A,B,C...)
         */
        private String replicate;


    /**
     * A free documentation field, which allows to add further comments about a plate.
     */
    private String description;

    /**
     * Plate label for display purposes.
     */
    private String label;

    /**
     * List of wells containing additional well information, annotations, and readouts.
     */
    private List<Well> wells = new ArrayList<Well>();

    /**
     * A transient property map for analysis caching
     */
    private HashMap<String, Object> plateStatistics;

    /**
     * ID (from where? what id?) TODO: add proper description
     */
    private String id;

    /**
     * UUID for an easy plate identification
     */
    private UUID uuid;



    /**
     * Constructor
     * @param barcode unique string containing plate information (at least across the experiment)
     */
    public Plate(String barcode) {
        this();
        this.barcode = barcode;
    }

    /**
     * Constructor for an empty well.
     */
    public Plate() {
        this.uuid = UUID.randomUUID();
    }

//    @ManyToOne
//    public Screen getScreen() {
//        return screen;
//    }
//
//
//    public void setScreen(Screen screen) {
//        this.screen = screen;
//    }


    /**
     * Note: we use remove-cascading here to drop all wells of a plate if the plate itself is being removed from the
     * db.
     */
    public List<Well> getWells() {
        return wells;
    }

    public void setWells(List<Well> wells) {
        this.wells = wells;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLibraryCode() {
        return libraryCode;
    }

    public void setLibraryCode(String libraryCode) {
        this.libraryCode = libraryCode;
    }

    public Integer getLibraryPlateNumber() {
        return libraryPlateNumber;
    }

    public void setLibraryPlateNumber(Integer libraryPlateNumber) {
        this.libraryPlateNumber = libraryPlateNumber;
    }

    public Date getScreenedAt() {
        return screenedAt;
    }

    public void setScreenedAt(Date screenedAt) {
        this.screenedAt = screenedAt;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
    }

    public String getReplicate() {
        return replicate;
    }

    public void setReplicate(String replicate) {
        this.replicate = replicate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public UUID getUuid() {
        return uuid;
    }

    public HashMap<String, Object> getPlateStatistics() {
        // use a lazy initialization approach here
        if (plateStatistics == null)
            plateStatistics = new HashMap<String, Object>();

        return plateStatistics;
    }


    /**
     * @return THe wells which match one of the given treatments.
     */
    public Collection<Well> getWellsByTreatment(String treatments) {
        return getWellsByTreatments(Arrays.asList(treatments));
    }


    /**
     * @return THe wells which match one of the given treatments.
     */
    public Collection<Well> getWellsByTreatments(Collection<String> treatments) {

        List<Well> matchingWells = new ArrayList<Well>();

        for (Well well : getWells()) {
            if (well.getTreatment() != null && treatments.contains(well.getTreatment())) {
                matchingWells.add(well);
            }
        }

        return matchingWells;
    }


    private transient Map<String, Well> transientGrid;


    public Well getWell(Integer plateColumn, Integer plateRow) {
        if (transientGrid == null) {
            transientGrid = new HashMap<String, Well>();
            for (Well well : getWells()) {
                String gridKey = well.getPlateColumn() + "-" + well.getPlateRow();

                if (transientGrid.containsKey(gridKey)) {
                    throw new RuntimeException("Corrupted plate structure: Several wells per plate position detected on plate '" + getBarcode() + "'");
                }

                transientGrid.put(gridKey, well);
            }
        }

        return transientGrid.get(plateColumn + "-" + plateRow);
    }


    @Override
    public String toString() {
        return getBarcode() + " : " + getNumRows() + "x" + getNumColumns();
    }


    public void addWell(Well well) {
        transientGrid = null;
        well.setPlate(this);
        getWells().add(well);
    }


    public static void inferPlateDimFromWells(Plate plate) {
        List<Well> wells = new ArrayList<Well>(plate.getWells());
        Collections.sort(wells, new Comparator<Well>() {
            public int compare(Well well, Well well2) {
                return (well.getPlateColumn() - well2.getPlateColumn()) + (well.getPlateRow() - well2.getPlateRow());
            }
        });

        Well lastWell = wells.get(wells.size() - 1);

        Integer lastRow = lastWell.getPlateRow();
        Integer lastCol = lastWell.getPlateColumn();

        // from wikipedia: A microplate typically has 6, 12, 24, 96, 384 or even 1536 sample wells arranged in a 2:3 rectangular matrix.

        if (lastRow > 16 || lastCol > 24) {
            // its a 1546 well plate
            plate.setNumRows(32);
            plate.setNumColumns(48);

        } else if (lastRow > 8 || lastCol > 12) {
            // its a 384 well plate
            plate.setNumRows(16);
            plate.setNumColumns(24);

        } else if (lastRow > 4 || lastCol > 6) {
            // its a 96 well plate
            plate.setNumRows(8);
            plate.setNumColumns(12);

        } else if (lastRow > 3 || lastCol > 4) {
            // its a 24 well plate
            plate.setNumRows(4);
            plate.setNumColumns(6);

        } else if (lastRow > 2 || lastCol > 3) {
            // its a 12 well plate
            plate.setNumRows(3);
            plate.setNumColumns(4);
        } else {
            // its a 6 well plate
            plate.setNumRows(2);
            plate.setNumColumns(3);
        }
    }


    public static void configurePlateByBarcode(Plate plate, BarcodeParser barcodeReader) {
        plate.setLibraryCode(barcodeReader.getLibraryCode()); // use a fake barcode here
        plate.setLibraryPlateNumber(barcodeReader.getPlateNumber());
        plate.setScreenedAt(barcodeReader.getDate());
        plate.setReplicate(barcodeReader.getReplicate());
        plate.setAssay(barcodeReader.getAssay());
    }

}
