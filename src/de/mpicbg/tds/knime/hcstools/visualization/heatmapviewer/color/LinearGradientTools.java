package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import java.awt.*;

/**
 * User: Felix Meyenhofer
 * Date: 12/10/12
 * Time: 24:50
 *
 * Useful manipulations for a LinearGradientPaint instance.
 */

public abstract class LinearGradientTools {

    public static final Color getColorAt(LinearGradientPaint painter, float input) {
        int lowerIndex = 0;
        int upperIndex = 1;
        float lowerBound = 0;
        float upperBound = 1;
        float[] fractions = painter.getFractions();
        Color[] colors = painter.getColors();

        if ( input > 1 ) {
            input = 1f;
        }
        if ( input < 0 ) {
            input = 0f;
        }

        int index = 0;
        for (float fraction : fractions ) {

            if ( fraction < input ) {
                lowerBound = fraction;
                lowerIndex = index;
            } else if ( fraction == input ) {
                return colors[index];
            } else if ( fraction > input ) {
                upperBound = fraction;
                upperIndex = index;
            }
            index++;
        }

        float rescaled = (input - lowerBound) / (upperBound - lowerBound);

        return interpolateColor(colors[lowerIndex], colors[upperIndex], rescaled);
    }

    private static java.awt.Color interpolateColor(final Color COLOR1, final Color COLOR2, final float FRACTION) {
        assert(Float.compare(FRACTION, 0f) >= 0 && Float.compare(FRACTION, 1f) <= 0);

        final float INT_TO_FLOAT_CONST = 1f / 255f;

        final float RED1 = COLOR1.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN1 = COLOR1.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE1 = COLOR1.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA1 = COLOR1.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED2 = COLOR2.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN2 = COLOR2.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE2 = COLOR2.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA2 = COLOR2.getAlpha() * INT_TO_FLOAT_CONST;

        final float DELTA_RED = RED2 - RED1;
        final float DELTA_GREEN = GREEN2 - GREEN1;
        final float DELTA_BLUE = BLUE2 - BLUE1;
        final float DELTA_ALPHA = ALPHA2 - ALPHA1;

        float red = RED1 + (DELTA_RED * FRACTION);
        float green = GREEN1 + (DELTA_GREEN * FRACTION);
        float blue = BLUE1 + (DELTA_BLUE * FRACTION);
        float alpha = ALPHA1 + (DELTA_ALPHA * FRACTION);

        red = red < 0f ? 0f : red;
        red = red > 1f ? 1f : red;
        green = green < 0f ? 0f : green;
        green = green > 1f ? 1f : green;
        blue = blue < 0f ? 0f : blue;
        blue = blue > 1f ? 1f : blue;
        alpha = alpha < 0f ? 0f : alpha;
        alpha = alpha > 1f ? 1f : alpha;

        return new java.awt.Color(red, green, blue, alpha);
    }

}
