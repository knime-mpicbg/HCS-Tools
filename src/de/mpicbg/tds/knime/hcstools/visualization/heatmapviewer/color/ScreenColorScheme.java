package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.TdsUtils;
import jxl.format.Colour;
import jxl.format.RGB;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Defines a screen-dependent color set. Subclasses may override parts of it to adapt it to project specific needs.
 *
 * @author Holger Brandl
 */

public class ScreenColorScheme {

    private Map<String, Map<String, Color>> colorCache = new HashMap<String, Map<String, Color>>();
    private Random r = new Random(4711);   // use seed here, to keep colors consistent between different runs

//    private ScreenControlServiceImpl controlService;

    private static ScreenColorScheme instance;


    public static ScreenColorScheme getInstance() {
        if (instance == null)
            instance = new ScreenColorScheme();

        return instance;
    }


    public ScreenColorScheme() {
        try {
//			controlService = ScreenControlServiceImpl.getInstance();
        } catch (Exception e) {
            System.err.println("could not connect to control-db. Using random colors");
        }
    }


    public Color noReadOut() {
        return Color.GRAY;
    }


    public Color getColorFromCache(String cacheName, String keyValue) {
        Map<String, Color> colorMap = getNameColorCache(cacheName);

        if (colorMap.containsKey(keyValue)) {
            return colorMap.get(keyValue);
        }

//        if (controlService != null) {
        //todo reintegrate with new tdscore library
//            return controlService.getTreatmentColor(keyValue);
//        }

        if (keyValue == null) {
            keyValue = "";
        }

        // todo we should replace this with an orthogonal color set
        int backOS = 55;
        Color nextColor = new Color(backOS + r.nextInt(255 - backOS), backOS + r.nextInt(255 - backOS), backOS + r.nextInt(255 - backOS));


        if (keyValue.equals(TdsUtils.TREATMENT_LIBRARY)) {
            nextColor = Color.YELLOW;
        }

        colorMap.put(keyValue, nextColor);

        return nextColor;
    }


    public Map<String, Color> getNameColorCache(String cacheName) {
        if (!colorCache.containsKey(cacheName)) {
            colorCache.put(cacheName, new HashMap<String, Color>());
        }

        return colorCache.get(cacheName);
    }


    public Color getSelectionMarkerColor() {
        return Color.BLUE;
    }


    private Map<Color, Colour> excelColorCache = new HashMap<Color, Colour>();


    public Colour getBestMatchingExcelColor(String treatment) {
        final Color color = getColorFromCache(treatment, treatment);


        if (!excelColorCache.containsKey(color)) {
            List<Colour> colors = Arrays.asList(Colour.getAllColours());

            Colour bestMatch = Collections.min(colors, new Comparator<Colour>() {
                public int compare(Colour o, Colour o1) {

                    return calcRGBDist(color, o) - calcRGBDist(color, o1) > 0 ? +1 : -1;
                }


                private double calcRGBDist(Color color, Colour o) {
                    RGB rgb = o.getDefaultRGB();

                    int redDiff = color.getRed() - rgb.getRed();
                    int blueDiff = color.getBlue() - rgb.getBlue();
                    int greenDiff = color.getGreen() - rgb.getGreen();
                    return Math.sqrt(redDiff * redDiff + blueDiff * blueDiff + greenDiff * greenDiff);
                }
            });

            excelColorCache.put(color, bestMatch);

        }

        return excelColorCache.get(color);
    }

}
