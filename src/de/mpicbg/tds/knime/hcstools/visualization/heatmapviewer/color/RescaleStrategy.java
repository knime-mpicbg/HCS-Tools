package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;

import java.io.Serializable;
import java.util.Collection;


/**
 * This is implemented using a lazy initialization approach: Normalization factors are calculated on request only.
 *
 * @author Holger Brandl
 */

public interface RescaleStrategy extends Serializable {

	void configure(Collection<Plate> screen);

	Double getMinValue(String selectedReadOut);

	Double getMaxValue(String selectedReadOut);

	Double normalize(Double wellReadout, String selectedReadOut);
}


