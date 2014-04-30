package de.mpicbg.knime.hcs.base.heatmap.color;

import de.mpicbg.tds.core.model.Plate;

import java.io.Serializable;
import java.util.Collection;

/**
 * This is implemented using a lazy initialization approach: Normalization factors are calculated on request only.
 *
 * @author Holger Brandl
 */

public interface RescaleStrategy extends Serializable {

    /**
     * Set the data to be scaled
     *
     * @param screen for rescaling
     */
	void configure(Collection<Plate> screen);


    /**
     * get the lower bound of the set.
     *
     * @param selectedReadOut readout name
     * @return lower bound of the data
     */
	Double getMinValue(String selectedReadOut);


    /**
     * get the upper bound
     *
     * @param selectedReadOut readout name
     * @return lower bound of the data
     */
	Double getMaxValue(String selectedReadOut);


    /**
     * Normalize a given value of a given readout.
     *
     * @param wellReadout value for normalization
     * @param selectedReadOut readout name the value is belonging to
     * @return rescaled value [0...1]
     */
	Double normalize(Double wellReadout, String selectedReadOut);

}
