package de.mpicbg.knime.hcs.core.util;

import de.mpicbg.knime.hcs.core.LayoutUtils;
import de.mpicbg.knime.hcs.core.TdsUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static de.mpicbg.knime.hcs.core.ScreenParserUtils.createFileReader;
import static de.mpicbg.knime.hcs.core.ScreenParserUtils.readLine;


/**
 * An immutable string table along with some utility methods to read csv-files.
 *
 * @author Holger Brandl
 */
public class StringTable {

    private final List<List<String>> table = new ArrayList<List<String>>();


    private StringTable() {
    }


    public int getHeight() {
        return table.size();
    }


    public int getWidth() {
        return table.get(0).size();
    }


    /**
     * Retrieves layout values from a given position
     *
     * @param rowIndex
     * @param colIndex
     * @return layout value at given position or null
     */
    public String get(int rowIndex, int colIndex) {

        String returnValue = null;

        // check bounds
        if (table.size() > rowIndex) {
            if (table.get(rowIndex).size() > colIndex) {
                returnValue = table.get(rowIndex).get(colIndex);  // .trim() ?
            }
        }

        return returnValue;
    }


    public static StringTable createFromArray(String[][] input) {
        StringTable newTable = new StringTable();

        for (int i = 0; i < input.length; i++) {
            String[] row = input[i];

            newTable.table.add(new ArrayList<String>());
            for (int j = 0; j < row.length; j++) {
                newTable.table.get(i).add(row[j]);
            }
        }

        return newTable;
    }


    public static StringTable readStringGrid(File tableFile, int numSkipLines, String separator) {
        BufferedReader reader = new BufferedReader(createFileReader(tableFile));

        StringTable newTable = new StringTable();
        // skip first lines
        for (int i = 0; i < numSkipLines; i++) {
            readLine(reader);
        }

        String line;

        while ((line = readLine(reader)) != null) {
            if (line.isEmpty())
                continue;

            String[] splitString = line.split(separator);
            ArrayList<String> newRow = new ArrayList<String>();
            for (String s : splitString) {
                newRow.add(s.trim());
            }

            newTable.table.add(newRow);
        }

        return newTable;
    }


    public static StringTable readStringGrid(File tableFile, int numSkipLines, String separator, int numReadLines) {
        BufferedReader reader = new BufferedReader(createFileReader(tableFile));

        StringTable newTable = new StringTable();
        // skip first lines
        for (int i = 0; i < numSkipLines; i++) {
            readLine(reader);
        }

        String line;

        for (int i = 0; i < numReadLines; i++) {
            line = readLine(reader);
            assert !line.isEmpty();

            String[] splitString = line.split(separator);
            ArrayList<String> newRow = new ArrayList<String>();
            for (String s : splitString) {
                newRow.add(s.trim());
            }

            newTable.table.add(newRow);
        }

        return newTable;
    }


    public static StringTable readStringGridFromExcel(File tableFile, Point upperLeft, Point lowerRight, String sheetName) {
        try {
            Workbook workbook = LayoutUtils.openWorkBook(tableFile);

            return readStringGridFromExcel(new Rectangle((int) upperLeft.getX(), (int) upperLeft.getY(), (int) lowerRight.getX(), (int) lowerRight.getY()), workbook.getSheet(sheetName));

        } catch (Throwable t) {
            //could not read excel sheet
            throw new RuntimeException(t);
        }
    }


    public static StringTable readStringGridFromExcel(Rectangle bounds, File layoutFile, String sheetName) {
        try {
            return readStringGridFromExcel(bounds, LayoutUtils.openWorkBook(layoutFile).getSheet(sheetName));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    /**
     * Reads a table form the excel-sheet.
     *
     * @param bounds The bounds in excel-coordinates. this means no offset
     * @param sheet
     */
    public static StringTable readStringGridFromExcel(Rectangle bounds, Sheet sheet) {
        return readStringGridFromExcel(bounds, sheet, 1);
    }


    public static StringTable readStringGridFromExcel(Rectangle bounds, Sheet sheet, int numCellReadouts) {

        StringTable newTable = new StringTable();

        int rowOffset = (int) bounds.getY();
        int colOffset = (int) bounds.getX();

        for (int rowIndex = 0; rowIndex < bounds.getHeight(); rowIndex++) {
            ArrayList<String> newRow = new ArrayList<String>();

            for (int colIndex = 0; colIndex < bounds.getWidth(); colIndex++) {
                int column = colOffset + colIndex - 1;
                int row = rowOffset + rowIndex * numCellReadouts - 1;

//                if (column >= sheet.getRow(row).getLastCellNum() || row > (sheet.getLastRowNum()))
//                if (row > (sheet.getLastRowNum()))
//                    throw new RuntimeException("out of sheet  " + sheet);

                if (column >= sheet.getRow(row).getLastCellNum() || row > (sheet.getLastRowNum())) {

                    // the cell is out of the sheet (either current row or row bounds) so we just fill up the matrix being read with 'null's
                    newRow.add(null);
                    continue;
                }


                Cell cell = sheet.getRow(row).getCell(column, Row.RETURN_BLANK_AS_NULL);
                if (cell == null) {
                    newRow.add(null);
                    continue;
                }

                if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
                        newRow.add("" + cell.getNumericCellValue());
                    } else if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
                        newRow.add("" + cell.getStringCellValue());
                    } else {
                        newRow.add("ERROR");
                    }

                } else {
                    //
                    String rawContent = cell.toString();

                    if (rawContent.endsWith(".0")) {
                        rawContent = rawContent.substring(0, rawContent.length() - 2);
                    }

                    newRow.add(rawContent);
                }
            }

            newTable.table.add(newRow);
        }

        return newTable;
    }


    public static StringTable readStringGridFromCsv(File tableFile, String separator, Point upperLeft, Point lowerRight) {

        BufferedReader reader = new BufferedReader(createFileReader(tableFile));

        StringTable newTable = new StringTable();
        // skip first lines
        for (int i = 0; i < upperLeft.getY() - 1; i++) {
            readLine(reader);
        }

        String line;

        for (int i = 0; i < lowerRight.getY() - upperLeft.getY() + 1; i++) {
            line = readLine(reader);
            assert !line.isEmpty();

            ArrayList<String> newRow = new ArrayList<String>();

            line = line.replace(separator, separator + " ");
            String[] splitString = line.split(separator);
            for (int j = (int) upperLeft.getX(); j <= lowerRight.getX(); j++) {
                String s = splitString[j - 1];
                newRow.add(s.trim());
            }

            newTable.table.add(newRow);
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newTable;

    }


    /**
     * @param point The point in excel-coordinates that means the most top-left point is (1,1).
     */
    public static Rectangle guessPlateBounds(Sheet sheet, Point point) {
        return guessPlateBounds(sheet, point, 1);

    }


    public static Rectangle guessPlateBounds(Sheet sheet, Point point, int numCellReadouts) {
        // go in both direction of starting cell and detect the end of the sequence

        int rowOffset = (int) point.getY() - 1;
        int colOffset = (int) point.getX() - 1;


        //first guess number of rows
        int rowCounter = 0;
        while (sheet.getLastRowNum() >= (rowOffset + rowCounter + 1)) {
            Row row = sheet.getRow(rowOffset + rowCounter * numCellReadouts + 1);
            if (row == null)
                break;

            String cellContent = row.getCell(colOffset, Row.CREATE_NULL_AS_BLANK).toString().trim();
            if (!cellContent.equals(TdsUtils.mapPlateRowNumberToString(rowCounter + 1))) {
                break;
            }

            rowCounter++;
        }


        int colCounter = 0;
        while (sheet.getRow(rowOffset).getLastCellNum() > (colOffset + colCounter + 1)) {
            Cell cell = sheet.getRow(rowOffset).getCell(colOffset + colCounter + 1, Row.RETURN_NULL_AND_BLANK);
            if (cell == null) {
                break;
            }

            String cellContent = cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? "" + (int) cell.getNumericCellValue() : cell.toString().trim();
            if (!cellContent.equals(colCounter + 1 + "")) {
                break;
            }

            colCounter++;
        }


        if (colCounter == 0 || rowCounter == 0) {
            return null;
        } else {
            // +1 because we include the row and column annotations
            return new Rectangle((int) point.getX(), (int) point.getY(), colCounter + 1, rowCounter + 1);
        }
    }


    /**
     * Attempts to guess the type of data in the table.
     *
     * @param ignoreFirstRowAndCol
     * @return the type or null if the table was completely empty.
     */
    public Class guessType(boolean ignoreFirstRowAndCol) {
        // collect all values
        Collection<String> values = new HashSet<String>();

        int startIndex = ignoreFirstRowAndCol ? 1 : 0;
        for (int i = startIndex; i < getWidth(); i++) {
            for (int j = startIndex; j < getHeight(); j++) {
                values.add(get(j, i));
            }
        }

        // now try guess the type: assume double, if yes try int, if not string
        if (values.isEmpty() || (values.size() == 1 && values.iterator().next().equals(""))) {
            return null;
        }

        Boolean isDouble = isCompatible(values, "[\\d.]*");
        if (isDouble) {
            return isCompatible(values, "[\\d]*") ? Integer.class : Double.class;
        }

        return String.class;
    }


    private Boolean isCompatible(Collection<String> values, String pattern) {
        boolean allEmpty = true;

        Pattern compiledPattern = Pattern.compile(pattern);

        for (String value : values) {
            if (value == null) {
                continue;
            }

            value = value.trim();
            if (value.isEmpty()) {
                continue;
            }

            allEmpty = false;

            if (!compiledPattern.matcher(value).matches()) {
                return false;
            }
        }

        return allEmpty ? null : true;
    }


    public static Workbook openWorkSheet(File layoutFile) {
        Workbook workbook;
        try {
            workbook = LayoutUtils.openWorkBook(layoutFile);
        } catch (Throwable e) {
            throw new RuntimeException("could not open file " + layoutFile);
        }
        return workbook;
    }


    /**
     * @param point the starting point in excel-coordinates (not java).
     */
    public static Point findNextPlatePosition(Sheet sheet, Point point) {
        int colIndex = (int) point.getX() - 1;
        int rowIndex = (int) point.getY() - 1;

        while (rowIndex + 3 < sheet.getLastRowNum()) { //3 because its the upper left position
            Cell A = sheet.getRow(rowIndex + 1) == null ? null : sheet.getRow(rowIndex + 1).getCell(colIndex);
            Cell one = sheet.getRow(rowIndex) == null ? null : sheet.getRow(rowIndex).getCell(colIndex + 1, Row.RETURN_BLANK_AS_NULL);

            rowIndex++;

            if (A == null || one == null) {
                continue;
            }

            if (A.toString().toLowerCase().equals("a") && (one.toString().equals("1") || one.toString().equals("1.0"))) {
                return new Point(colIndex + 1, rowIndex);
            }

        }

        return null;
    }
}
