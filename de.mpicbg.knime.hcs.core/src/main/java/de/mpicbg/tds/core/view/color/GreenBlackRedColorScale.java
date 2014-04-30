package de.mpicbg.tds.core.view.color;

import java.awt.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class GreenBlackRedColorScale implements ColorScale {


	public Color mapReadout2Color(Double displayNormReadOut) {

		double v = -0.5 + displayNormReadOut;
		if (v > 0.0)
			return (ColorMap.colorRedToBlack[(int) (255.0 - 255.0 * (2 * v))]);

		else
			return (ColorMap.colorBlackToGreen[(int) (255.0 * (Math.abs(2 * v)))]);

	}
}