package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import java.awt.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
@Deprecated
public class BlackGreenColorScale implements ColorScale {

	public Color mapReadout2Color(Double displayNormReadOut) {
		return ColorUtil.fromDouble(displayNormReadOut);
	}
}
