package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model;

/**
 * Author: Felix Meyenhofer
 * Date: 12/22/12
 *
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

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

}
