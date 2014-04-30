package de.mpicbg.tds.barcodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * https://wiki.mpi-cbg.de/wiki/tds/index.php/Barcode_standards
 *
 * @author Holger Brandl
 */
public class LibraryPlateBarcodeParser {


    public Matcher barcodeMatcher;
    static String LIB_PLATE_BARCODE_PATTERN_NONAMED
            = "([0-9]{3})" +
            "([A-Z]{3})" +
            "([_\\d]{3})" +
            "([A-z_]{4})" +
            "([\\d]{1})" +
            "([A-z]{1})" +
            "([A-z]{1})" +
            "_" +
            "([A-z_]{3})";


    public LibraryPlateBarcodeParser(String barcode) {
        Pattern barcodePattern = Pattern.compile(LIB_PLATE_BARCODE_PATTERN_NONAMED);
        barcodeMatcher = barcodePattern.matcher(barcode);

        if (!barcodeMatcher.matches()) {
            throw new IllegalArgumentException("barcode '" + barcode + "' doesn't match pattern: " + LIB_PLATE_BARCODE_PATTERN_NONAMED);
        }
    }


    /**
     * Get the plate number of library .
     */
    public int getPlateNumber() {
        return Integer.parseInt(barcodeMatcher.group(1));
    }


    /**
     * Get the library abbreviation.
     */
    public String getLibrary() {
        return barcodeMatcher.group(2);
    }


    /**
     * Get the number of concentration (3 digits!, e.g. 100 or _10 or __1) .
     */
    public int getConcenctration() {
        return Integer.parseInt(barcodeMatcher.group(3).replaceAll("_", ""));
    }


    /**
     * Get the .
     */
    public String getUnitOfConcentration() {
        return barcodeMatcher.group(4).replaceAll("_", "");
    }


    /**
     * Get the well number abbreviation ("9" for 96well, "3" for 384well) .
     */
    public int getWellCountAbbreviation() {
        return Integer.parseInt(barcodeMatcher.group(5));
    }


    /**
     * Get the type of the plate.
     */
    public String getStoragePlateTypeAbbreviation() {
        return barcodeMatcher.group(6);
    }


    /**
     * Get storage plate content abbreviation .
     */
    public String getStoragePlateContentAbbreviation() {
        return barcodeMatcher.group(7);
    }


    /**
     * Get the aliqouting hierarchy .
     */
    public String getAliquotingHierarchy() {
        return barcodeMatcher.group(8);
    }

}
