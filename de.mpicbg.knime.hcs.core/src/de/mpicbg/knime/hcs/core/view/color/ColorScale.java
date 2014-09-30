package de.mpicbg.knime.hcs.core.view.color;

import java.awt.*;

/**
 * Document me!
 *
 * @author Holger Brandl
 */
public interface ColorScale {
	Color mapReadout2Color(Double displayNormReadOut);
}
