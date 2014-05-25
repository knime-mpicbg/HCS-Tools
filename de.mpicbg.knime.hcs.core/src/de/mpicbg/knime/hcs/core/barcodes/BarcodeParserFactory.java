package de.mpicbg.knime.hcs.core.barcodes;

import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedMatcher;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;

import java.util.ArrayList;
import java.util.List;

import static de.mpicbg.knime.hcs.core.barcodes.BarcodeParser.*;


/**
 * A configurable barcode-parser that keeps a list of barcode-patterns and attempts to find the matching one when
 * analyzing barcode one. The different elements of the barcode are specified via named group in the barcode regexps.
 * <p/>
 * cf.   http://code.google.com/p/named-regexp
 *
 * @author Holger Brandl
 */
public class BarcodeParserFactory {

    List<NamedPattern> barcodePatterns = new ArrayList<NamedPattern>();

    /**
     * An example pattern which define the assay-plate barcode-schema of the tds-facility.
     */
    public static final String ASSAY_PLATE_PATTERN
            = "(?<libplatenumber>[0-9]{3})" +
            "(?<projectcode>[A-z]{2})" +
            "(?<date>[0-9]{6})" +
            "(?<replicate>[A-z]{1})" +
            "-" +
            "(?<libcode>[_A-z\\d]{3})" +
            "(?<assay>[-_\\s\\w\\d]*)";


    public static final String ASSAY_PLATE_PATTERN_OLD = "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1})";

    /**
     * An example pattern which define the library-plate barcode-schema of the tds-facility.
     */
    public static final String LIB_PLATE_BARCODE_PATTERN
            = "(?<libplatenumber>[0-9]{3})" +
            "(?<libcode>[A-Z]{3})" +
            "(?<" + GROUP_CONCENTRATION + ">[_\\d]{3})" +
            "(?<" + GROUP_CONCENTRATION_UNIT + ">[A-z_]{4})" +
            "(?<" + GROUP_CUSTOM_A + ">[\\d]{1})" +
            "(?<" + GROUP_CUSTOM_B + ">[A-z]{1})" +
            "(?<" + GROUP_CUSTOM_C + ">[A-z]{1})" +
            "_" +
            "(?<" + GROUP_CUSTOM_D + ">[A-z_]{3})";


    public BarcodeParserFactory() {
    }


    public BarcodeParserFactory(List<String> patterns) {
        for (String pattern : patterns) {
            registerPattern(pattern);
        }
    }


    /**
     * Registers a new patter via a string which is expected to be a regular expression according the barcode-specs.
     */
    public void registerPattern(String barcodePattern) {
        registerPattern(NamedPattern.compile(barcodePattern));
    }


    public void registerPattern(NamedPattern barcodePattern) {
        if (!barcodePatterns.contains(barcodePattern)) {
            barcodePatterns.add(barcodePattern);
        }
    }


    public boolean unregisterBarcodePattern(NamedPattern barcodePattern) {
        return barcodePatterns.remove(barcodePattern);
    }


    /**
     * @return A barcode parser for the first matchin registerd pattern. If there is no matching pattern it returns
     *         <code>null</null>. If there are several matching patterns, an IllegalArgumentException is being thrown
     *         which indicates the matching patterns.
     */
    public BarcodeParser getParser(String barcode) {
        List<NamedPattern> matchPatterns = new ArrayList<NamedPattern>();

        for (NamedPattern barcodePattern : barcodePatterns) {
            NamedMatcher matcher = barcodePattern.matcher(barcode);

            if (matcher.matches()) {
                matchPatterns.add(barcodePattern);
            }
        }

        if (matchPatterns.isEmpty()) {
            return null;
        }

        if (matchPatterns.size() > 2) {
            throw new IllegalArgumentException("More than one pattern matched to the barcode '" + barcode + "':  " + matchPatterns);
        }

        return new BarcodeParser(barcode, matchPatterns.get(0));
    }


    public static void main(String[] args) {
        BarcodeParserFactory barcodeParserFactory = new BarcodeParserFactory();
        barcodeParserFactory.registerPattern(ASSAY_PLATE_PATTERN);

        BarcodeParser barcodeParser = barcodeParserFactory.getParser("001AL100205B-KI_1_1");


        System.err.println("libcode" + barcodeParser.getLibraryCode());
    }


    // a small tds-helper method


    public static BarcodeParser getAssayPlateBarcodeParser(String barcode) {
        BarcodeParserFactory bpf = new BarcodeParserFactory();
        bpf.registerPattern(ASSAY_PLATE_PATTERN);

        return bpf.getParser(barcode);
    }


    public static BarcodeParser getAssayPlateBarcodeOLDParser(String barcode) {
        BarcodeParserFactory bpf = new BarcodeParserFactory();
        bpf.registerPattern("(?<libplatenumber>[0-9]{3})" +
                "(?<projectcode>[A-z]{2})" +
                "(?<libcode>[A-z\\d]{3})" +
                "(?<assay>[A-z]{3,5})" +
                "(?<date>[0-9]{6})" +
                "(?<replicate>[A-z]{1})");

        return bpf.getParser(barcode);
    }


    public static BarcodeParser getLibPlateBarcodeParser(String barcode) {
        BarcodeParserFactory bpf = new BarcodeParserFactory();
        bpf.registerPattern(LIB_PLATE_BARCODE_PATTERN);

        return bpf.getParser(barcode);
    }


    public String getVerboseName(String groupName) {
        return BarcodeParser.getVerboseName(groupName);
    }


    public ArrayList<NamedPattern> getPatterns() {
        return new ArrayList<NamedPattern>(barcodePatterns);
    }
}
