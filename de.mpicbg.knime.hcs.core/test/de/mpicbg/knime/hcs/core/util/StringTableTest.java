package de.mpicbg.knime.hcs.core.util;

import org.junit.Assert;
import org.junit.Test;

import de.mpicbg.knime.hcs.core.util.StringTable;


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
