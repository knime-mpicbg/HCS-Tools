/*
 * Copyright (C) 2008-2009 by Holger Arndt
 *
 * This file is part of the Universal Java Matrix Package (UJMP).
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * UJMP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * UJMP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with UJMP; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.view.color.ColorMap;

import java.awt.*;

@Deprecated
public abstract class ColorUtil {


	public static final Color contrastBW(Color c) {
		if ((c.getRed() + c.getGreen() + c.getBlue()) > 200.0) {
			return (Color.black);
		} else {
			return (Color.white);
		}
	}


	public static final Color fromDouble(double v) {
		// inf = 255 255 0 yellow
		// 1 = 0 255 0 green
		// 0 = 0 0 0 black
		// -1 = 255 0 0 red
		// -inf = 255 0 255 magenta
		// nan = 0 255 255 cyan
		if (v == Double.MIN_VALUE || Double.isNaN(v))
			return (Color.MAGENTA);
		else if (Double.isInfinite(v))
			return (Color.CYAN);
		else if (v > 1.0)
			return (de.mpicbg.tds.core.view.color.ColorMap.colorGreenToYellow[(int) (255.0 * Math
					.tanh((v - 1.0) / 10.0))]);
		else if (v > 0.0)
			return (de.mpicbg.tds.core.view.color.ColorMap.colorBlackToGreen[(int) (255.0 * v)]);
		else if (v > -1.0)
			return (de.mpicbg.tds.core.view.color.ColorMap.colorRedToBlack[(int) (255.0 * (v + 1.0))]);
		else
			return (ColorMap.colorRedToMagenta[(int) (255.0 * Math
					.tanh((-v - 1.0) / 10.0))]);
	}
}