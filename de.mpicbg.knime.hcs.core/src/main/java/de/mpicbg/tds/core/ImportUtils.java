package de.mpicbg.tds.core;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * A collection of static utility methods to make the implemenation of new screen-parsers as painless as possible.
 *
 * @author Holger Brandl
 */
public class ImportUtils {

    // just use this string for parsing. It can be adapted to the needs of a screen
    public static String NotANumber = "nan";
    public static final Date TODAY = new Date();


    public static double parseDouble(String s) {
        if (s == null || s.trim().isEmpty() || s.equals(NotANumber)) {
            return Double.NaN;
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }


    public static Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }

        return (int) parseDouble(s);
    }


    /**
     * Converts 1 to A, 2 to B and so on.   Note: this conflicts with the usual array notation in java which start with
     * index 0.
     */
    public static String mapIndexToPlateColumn(int index) {
        return new String(new char[]{(char) ('A' + index - 1)});
    }


    public static Integer mapRowCharToIndex(String rowPos) {
        if (rowPos == null || rowPos.isEmpty())
            return null;

        assert rowPos.length() == 1;
        return rowPos.charAt(0) - 64;
    }


    public static int getPlateIndex(String excelColIndex) {
        int index = 0;
        char[] characters = excelColIndex.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char character = characters[i];
            index += (character - 64) + (i * (26 - 1));
        }

        return index - 1;
    }


    public static void main(String[] args) {
        System.err.println("A: " + getPlateIndex("A"));
        System.err.println("AA: " + getPlateIndex("AA"));
        System.err.println("C: " + getPlateIndex("C"));
        System.err.println("AC: " + getPlateIndex("AC"));
    }


    public static Double getNumber(Row row, int colindex) {
        Cell cell = row.getCell(colindex);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return cell.getNumericCellValue();
        } else {
            return parseDouble(cell.getStringCellValue());
        }
    }


    public static Integer getIntNumber(Row row, int colIndex) {
        Double number = getNumber(row, colIndex);
        return number != null ? number.intValue() : null;
    }


    private static DecimalFormat doneFormat = new DecimalFormat("##.##");


    public static String procDone(Row row) {
        double doneRatio = 100 * row.getRowNum() / (double) row.getSheet().getLastRowNum();
        return " done: " + doneFormat.format(doneRatio) + " %          ";
    }


    public static String getString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);

        if (cell == null) {
            return null;

        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            // return int if it is an integer
            double numericCellValue = cell.getNumericCellValue();
            if ((numericCellValue - (int) numericCellValue) < 1E-15)
                return (int) numericCellValue + "";

            return cell.toString();
        } else {
            return cell.getStringCellValue();
        }
    }


    public static void logPercentDone(Row row, String msg, Class logClass) {
        double doneRatio = 100 * row.getRowNum() / (double) row.getSheet().getLastRowNum();
        if ((doneRatio - (int) doneRatio < 0.005)) {
            Logger.getLogger(logClass.getSimpleName()).fine(msg);
        }
    }


    public static void validateHeader(Row row, List<String> expectedHeaderModel) {
        for (Cell cell : row) {
            String expectedValue = expectedHeaderModel.get(cell.getColumnIndex());
            String obtainedValue = cell.getStringCellValue();

            if (!obtainedValue.equals(expectedValue)) {
                throw new RuntimeException("Header mismatch. Expected: '" + expectedValue + "' Obtained: '" + obtainedValue + "'");
            }
        }
    }
}