package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.hcs.core.barcodes.BarcodeParser;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;

/**
 * Created by IntelliJ IDEA.
 * User: meyenhof
 * Date: Sep 9, 2010
 * Time: 2:31:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MotionTrackBarcodeTests {


    public static void main(String[] args) {
//        String pattern = "(?<barcode>(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*))14__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{3})(?<wellfield>[0-9]{1})[.]mtf";
        String pattern = "(?<barcode>.*)__(?<row>[0-9]{3})(?<column>[0-9]{3})[0-9]{3}_(?<timpoint>[0-9]{1})(?<wellfield>[0-9]{3})[.]mtf";
//        String inputString = "001lk100528a-___rtchm\\001LK100528A-___RTCHM_Meas_01_2010-06-02_12-48-14__003003000_0000.mtf";
//        String inputString = "001MB100630A-MSD-AHe_Meas_01_2010-07-02_20-28-33__001002000_0017.mtf";
        String inputString = "002ec100527a-kbi-hel002EC100527A-KBI-HEL_Meas_01_2010-05-29_20-50-51__007018000_0009.mtf";

        BarcodeParser parser = new BarcodeParser(inputString, NamedPattern.compile(pattern));
        String barcode = parser.getGroup(MotionTrackingFileReader.GROUP_BARCODE);

        System.err.println("the barcode is " + barcode);
    }
}
