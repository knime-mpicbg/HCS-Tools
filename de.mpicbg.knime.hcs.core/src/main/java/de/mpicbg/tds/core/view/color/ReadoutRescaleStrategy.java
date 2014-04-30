package de.mpicbg.tds.core.view.color;

import de.mpicbg.tds.core.model.Plate;

import java.util.Collection;


/**
 * This is implemented using a lazy initialization approach: Normalization factors are calculated on request only.
 *
 * @author Holger Brandl
 */
public interface ReadoutRescaleStrategy {

	void configure(Collection<Plate> screen);

	Double getMinValue(String selectedReadOut);

	Double getMaxValue(String selectedReadOut);

	Double normalize(Double wellReadout, String selectedReadOut);
}


