package de.mpicbg.knime.hcs.core;

import java.awt.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ScreenParserUtils {

    static String OPERA_FILENAME_PATTERN = ".*([\\d]{3})([\\d]{3})([\\d]{3})_([\\d])([\\d]{3}).mtf";


    public static Point extractWellPositionFromMotionTrackingFile(String fileName) {
        Matcher matcher = Pattern.compile(OPERA_FILENAME_PATTERN).matcher(fileName);
        matcher.find();

        int row = Integer.parseInt(matcher.group(1));
        int column = Integer.parseInt(matcher.group(2));

        return new Point(column, row);
    }


    public static int extractFieldFromMotionTrackingFile(String fileName) {
        Matcher matcher = Pattern.compile(OPERA_FILENAME_PATTERN).matcher(fileName);
        matcher.find();

        return Integer.parseInt(matcher.group(5));
    }


    public static Reader createFileReader(File file) {
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        extractWellPositionFromMotionTrackingFile("001hfkinfullt150808a\\2008-08-19_Kinases_1st_run_001HFKINFULLT150808A_Meas_022008-08-19_11-46-14_002001000_0006.mtf");
    }

}
