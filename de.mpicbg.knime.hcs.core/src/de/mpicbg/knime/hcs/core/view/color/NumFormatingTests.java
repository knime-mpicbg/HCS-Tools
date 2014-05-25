package de.mpicbg.knime.hcs.core.view.color;

import java.text.DecimalFormat;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class NumFormatingTests {

	public static void main(String[] args) {
		DecimalFormat formatter = new DecimalFormat("000.###E0");

		System.out.println(formatter.format(0.123)); // 1.2345E-1
		System.out.println(formatter.format(12.123)); // 1.2345E-1
		System.out.println(formatter.format(123.0)); // 1.2345E-1
		System.out.println(formatter.format(123.0)); // 1.2345E-1
	}
}
