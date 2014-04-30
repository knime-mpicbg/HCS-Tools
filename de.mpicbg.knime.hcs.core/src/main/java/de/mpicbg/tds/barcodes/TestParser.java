package de.mpicbg.tds.barcodes;

import de.mpicbg.tds.barcodes.namedregexp.NamedPattern;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class TestParser {

    public static void main(String[] args) {
        BarcodeParser bparser = new BarcodeParser("004lkmsdkin241108a", NamedPattern.compile("(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1})"));

//        String mtfPattern = "(?<barcode>(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*))__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{3})(?<wellfield>[0-9]{1,2})[.]mtf";
        String mtfPattern = "(?<barcode>(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1}))__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{3})(?<wellfield>[0-9]{1,2})[.]mtf";
        String barcode = "004lkmsdkin241108a__015013001_1004.mtf";

        BarcodeParser barcodeParser = new BarcodeParser(barcode, NamedPattern.compile(mtfPattern));
        System.err.println(barcodeParser);
        System.err.println("timpoint " + barcodeParser.getGroup(BarcodeParser.GROUP_TIMEPOINT));
    }

}
