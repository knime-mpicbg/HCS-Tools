package de.mpicbg.knime.hcs.core.util;

import org.apache.commons.lang.StringUtils;


/**
 * A collection of static utility methods to make the implemenation of new screen-parsers as painless as possible.
 *
 * @author Holger Brandl
 */
public class ScreenImportUtils {

    // just use this string for parsing. It can be adapted to the needs of a screen
    public static String NOT_A_NUMBER = "nan";


    public static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }

        if (s.toLowerCase().equals(NOT_A_NUMBER)) {
            return Double.NaN;
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public static Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }

        Double parsedDouble = parseDouble(s);
        return parsedDouble != null ? (int) Math.round(parsedDouble) : null;
    }


    public static String getString(String s) {
        return StringUtils.isBlank(s) ? null : s.trim();
    }
}
