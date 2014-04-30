package de.mpicbg.tds.core.util;

import org.junit.Assert;
import org.junit.Test;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class StringTableTest {


	@Test
	public void testGridTable() {

		StringTable stringTable = StringTable.createFromArray(new String[][]{{"U", "V", "W",}, {"X", "Y", "Z"}});

		Assert.assertEquals("U", stringTable.get(0, 0));
		Assert.assertEquals("Y", stringTable.get(1, 1));
	}
}
