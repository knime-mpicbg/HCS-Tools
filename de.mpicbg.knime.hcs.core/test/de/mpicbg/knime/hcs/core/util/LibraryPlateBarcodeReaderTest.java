package de.mpicbg.knime.hcs.core.util;

import de.mpicbg.knime.hcs.core.barcodes.BarcodeParser;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParserFactory;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class LibraryPlateBarcodeReaderTest {

	@Test
	public void testScreenPlateBarcodes() {

		BarcodeParser barcodeParser;
		// 1) test some screening campaign barcodes

		barcodeParser = BarcodeParserFactory.getAssayPlateBarcodeParser("034HS091123A-ESI-Flu");
		Assert.assertEquals(new Date(2009 - 1900, 10, 23), barcodeParser.getDate());
		Assert.assertEquals("ESI", barcodeParser.getLibraryCode());
		Assert.assertEquals(34, barcodeParser.getPlateNumber());

		barcodeParser = BarcodeParserFactory.getAssayPlateBarcodeParser("002HS091012B-esi-luc");
		Assert.assertEquals(new Date(2009 - 1900, 9, 12), barcodeParser.getDate());
		Assert.assertEquals("esi", barcodeParser.getLibraryCode());
		Assert.assertEquals("-luc", barcodeParser.getAssay());

		// now try to query a barcode property which is not present (which should result in a null-result)
		Assert.assertEquals(null, barcodeParser.getConcenctration());

		barcodeParser = BarcodeParserFactory.getAssayPlateBarcodeParser("276GW081203A-QG1E1T1");
		Assert.assertEquals("QG1", barcodeParser.getLibraryCode());
		Assert.assertEquals("E1T1", barcodeParser.getAssay());

		barcodeParser = BarcodeParserFactory.getAssayPlateBarcodeParser("048EM090114B-CBN-FNf");
		Assert.assertEquals("CBN", barcodeParser.getLibraryCode());
		Assert.assertEquals("-FNf", barcodeParser.getAssay());


		// 2) now test some assay plate barcodes

		barcodeParser = BarcodeParserFactory.getAssayPlateBarcodeParser("048EM090114B-ABC-Dies ist ein test");
		Assert.assertEquals("ABC", barcodeParser.getLibraryCode());
		Assert.assertEquals("-Dies ist ein test", barcodeParser.getAssay());
	}


	@Test
	public void testLibraryBarcode() {

		BarcodeParser plateBarcodeParser;
		plateBarcodeParser = BarcodeParserFactory.getLibPlateBarcodeParser("034ESI100ngul3CR_A__");


		Assert.assertEquals(34, plateBarcodeParser.getPlateNumber());
		Assert.assertEquals("ESI", plateBarcodeParser.getLibraryCode());
		Assert.assertEquals(100, plateBarcodeParser.getConcenctration().intValue());
		Assert.assertEquals("ngul", plateBarcodeParser.getUnitOfConcentration());
		Assert.assertEquals("3", plateBarcodeParser.getCustomA());
		Assert.assertEquals("C", plateBarcodeParser.getCustomB());
		Assert.assertEquals("R", plateBarcodeParser.getCustomC());
		Assert.assertEquals("A__", plateBarcodeParser.getCustomD());


		plateBarcodeParser = BarcodeParserFactory.getLibPlateBarcodeParser("048CBN_10uM__3DC_BAC");
		Assert.assertEquals("uM", plateBarcodeParser.getUnitOfConcentration());


		plateBarcodeParser = BarcodeParserFactory.getLibPlateBarcodeParser("276QGI200nM__3BR_B_B");
		Assert.assertEquals(200, plateBarcodeParser.getConcenctration().intValue());
		Assert.assertEquals("nM", plateBarcodeParser.getUnitOfConcentration());
	}

}
