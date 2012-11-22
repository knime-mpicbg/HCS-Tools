package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import java.awt.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
public interface ColorScale {
	Color mapReadout2Color(Double displayNormReadOut);
}
