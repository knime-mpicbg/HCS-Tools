package de.mpicbg.tds.core.view.color;

import java.awt.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class BlackGreenColorScale implements ColorScale {


	public Color mapReadout2Color(Double displayNormReadOut) {
		return ColorUtil.fromDouble(displayNormReadOut);
	}
}
