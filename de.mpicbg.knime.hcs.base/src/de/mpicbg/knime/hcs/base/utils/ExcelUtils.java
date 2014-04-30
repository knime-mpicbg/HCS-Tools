package de.mpicbg.knime.hcs.base.utils;

import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ExcelUtils {

    /**
     * @param excelFile
     * @param workSheetIndex The worksheet starting with 1 for the first sheet.
     * @return
     */
    public static Sheet openWorkSheet(File excelFile, int workSheetIndex) {
        Workbook wb = openWorkBook(excelFile);
        if (wb == null) return null;

        if (workSheetIndex > wb.getNumberOfSheets()) {
            LogFactory.getLog("excelreader").error("There is no sheet No" + (workSheetIndex + 1));
            return null;
        }

        return wb.getSheetAt(workSheetIndex);
    }


    public static Workbook openWorkBook(File excelFile) {
        String fileName = excelFile.getName();

        if (!excelFile.isFile() || !(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
            return null;
        }

        Workbook wb;

        try {
            InputStream excelStream = new BufferedInputStream(new FileInputStream(excelFile));

            if (fileName.endsWith(".xlsx")) {

                try {

                    wb = new XSSFWorkbook(excelStream);

                } catch (Throwable t) {
                    LogFactory.getLog("excelreader").error("Could not open excel-file: " + excelFile.getAbsolutePath() + " using old excel-format reader as fallback...");
                    return new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFile)));
                }

            } else {

                try {

                    wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFile)));

                } catch (Throwable t) {
                    LogFactory.getLog("excelreader").error("Could not open excel-file: " + excelFile.getAbsolutePath() + " using new excel-format reader as fallback...");
                    return new XSSFWorkbook(excelStream);
                }

            }

        } catch (Throwable t) {
            LogFactory.getLog("excelreader").error("Could not open excel-file: " + excelFile.getAbsolutePath());
            return null;
        }
        return wb;
    }


    public static String getCellContents(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }

        return getCellContents(colIndex, row);
    }


    public static String getCellContents(int colIndex, Row row) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }

        return cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? "" + (int) cell.getNumericCellValue() : cell.toString().trim();
    }


    public static boolean isMissingCell(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return true;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return true;
        }

        return false;
    }


    public static String removeExcelSuffix(String name) {
        name = name.replace(".xlsx", "");
        name = name.replace(".xls", "");

        return name;
    }
}