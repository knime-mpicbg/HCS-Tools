package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.view.color.ColorScale;
import de.mpicbg.tds.core.view.color.ColorUtil;

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
