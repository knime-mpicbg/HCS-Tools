package de.mpicbg.knime.hcs.base.heatmap.color;

import java.awt.*;
import java.util.*;

import de.mpicbg.knime.hcs.core.model.PlateUtils;
import org.knime.core.data.property.ColorAttr;

/**
 * Defines a screen-dependent color set. Subclasses may override parts of it to adapt it to project specific needs.
 *
 * @author Holger Brandl
 */

public class ColorScheme {

    /** 
     * Cache to remember the colors used as overlay already 
     * Overlay <Value, Color>
     * e.g. treatment <MOCK,Color("green")>
     * */
    private Map<String, Map<String, Color>> colorCacheMap = new HashMap<String, Map<String, Color>>();

    /** Random number generator */
    private Random r = new Random(4711);   // use seed here, to keep colors consistent between different runs


    /** color to mark the preselection during a mouse drag */
    public static final Color SELECTING =  new Color(108, 127,255);

    /** color for empty readouts */
    public static final Color EMPTY_READOUT = new Color(119, 119, 119);
    /** color for readouts producing an error during the parsing */
    private Color ERROR_READOUT = LinearGradientTools.errColorMap.get("DEFAULT");

    /** color to mark factor values that equal */
    public static final Color LIBRARY = new Color(255, 255, 0);

    /** color to mark selection (given by KNIME) */
    public static final Color SELECTED = ColorAttr.SELECTED; //new Color(0, 0, 255);
    /** color to mark simultaneously hiLited and selected (given by KNIME) */
    public static final Color HILITED_AND_SELECTED = ColorAttr.SELECTED_HILITE;
    /** color to mark the hiliting (given by KNIME) */
    public static final Color HILITED = ColorAttr.HILITE;


    /**
     * Constructor
     */
    public ColorScheme() {
    }

    /**
     * Constructor with certain error color
     * @param errColor
     */
    public ColorScheme(Color errColor) {
		ERROR_READOUT = errColor;
	}


	/**
     * Generate an overlay color
     *
     * @param cacheName belonging to the overlay attribute
     * @param keyValue overlay value
     * @return overlay color
     */
    public Color getOverlayColor(String cacheName, String keyValue) {
        Map<String, Color> colorMap = getColorCache(cacheName);

        if (colorMap.containsKey(keyValue)) {
            return colorMap.get(keyValue);
        }

        if (keyValue == null) {
            keyValue = "";
        }

        int blackOffset = 55;
        Color nextColor = new Color(blackOffset + r.nextInt(255 - blackOffset),
                                    blackOffset + r.nextInt(255 - blackOffset),
                                    blackOffset + r.nextInt(255 - blackOffset));

        if (keyValue.equals(PlateUtils.TREATMENT_LIBRARY)) {
            nextColor = ColorScheme.LIBRARY;//Color.YELLOW;
        }

        // cache the color
        colorMap.put(keyValue, nextColor);

        return nextColor;
    }

    /**
     * Get the color cache
     *
     * @param cacheName corresponding to the overlay attribute
     * @return color cache for the overlay
     */
    public Map<String, Color> getColorCache(String cacheName) {
        if (!colorCacheMap.containsKey(cacheName)) {
            colorCacheMap.put(cacheName, new HashMap<String, Color>());
        }

        return colorCacheMap.get(cacheName);
    }


    /**
     * @return error readout color
     */
	public Color getErrorReadoutColor() {
		return ERROR_READOUT;
	}

	/**
	 * sets the error readout color 
	 * @param errorReadoutColor
	 */
	public void setErrorReadoutColor(Color errorReadoutColor) {
		ERROR_READOUT = errorReadoutColor;
	}

	/**
	 * adds a color cache to the map
	 * @param key
	 * @param colorCache
	 */
	public void addColorCache(String key,	HashMap<String, Color> colorCache) {
		if(! colorCacheMap.containsKey(key))
			colorCacheMap.put(key, colorCache);		
	}
	
	/**
	 * method to put the content of the color caches into a single string
	 * @return string representing the color caches
	 */
	public String colorCachesToString() {
		String cacheString = new String("[");
		
		for(String key : colorCacheMap.keySet()) {
			Map<String, Color> cache = colorCacheMap.get(key);
			if(cache != null)
				cacheString = cacheString + key + "[" + cache.toString() + "]\n";
		}
		
		return cacheString + "]";
	}

}
