package de.mpicbg.tds.knime.hcstools.visualization.heatmap.color;

import java.awt.*;
import java.util.*;

import org.knime.core.data.property.ColorAttr;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Conventions;

/**
 * Defines a screen-dependent color set. Subclasses may override parts of it to adapt it to project specific needs.
 *
 * @author Holger Brandl
 */

public class ColorScheme {

    /** Cache to remember the colors used already */
    private Map<String, Map<String, Color>> colorCache = new HashMap<String, Map<String, Color>>();

    /** Random number generator */
    private Random r = new Random(4711);   // use seed here, to keep colors consistent between different runs


    /** color to mark the preselection during a mouse drag */
    public static final Color SELECTING =  new Color(108, 127,255);

    /** color for empty readouts */
    public static final Color EMPTY_READOUT = new Color(119, 119, 119);
    /** color for readouts producing an error during the parsing */
    public static final Color ERROR_READOUT = new Color(72, 56, 69);

    /** color to mark factor values that equal */
    public static final Color LIBRARY = new Color(255, 255, 0);

    /** color to mark selection (given by KNIME) */
    public static final Color SELECTED = ColorAttr.SELECTED; //new Color(0, 0, 255);
    /** color to mark simultaniously hiLited and selected (given by KNIME) */
    public static final Color HILITED_AND_SELECTED = ColorAttr.SELECTED_HILITE;
    /** color to mark the hiliting (given by KNIME) */
    public static final Color HILITED = ColorAttr.HILITE;


    /**
     * Constructor
     */
    public ColorScheme() {
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

        if (keyValue.equals(Conventions.CBG.Attr.Value.TREATMENT_LIBRARY)) {
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
        if (!colorCache.containsKey(cacheName)) {
            colorCache.put(cacheName, new HashMap<String, Color>());
        }

        return colorCache.get(cacheName);
    }

}
