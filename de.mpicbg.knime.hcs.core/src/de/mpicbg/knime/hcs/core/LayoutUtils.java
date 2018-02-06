package de.mpicbg.knime.hcs.core;

import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import de.mpicbg.knime.hcs.core.util.StringTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class LayoutUtils {

    public static int COL_OFFSET = 3;

    public static String DEFAULT_LAYOUT_SHEET_NAME = "Layout";


    public static Plate loadBasicLayoutFromExcel(File layoutFile, boolean ignoreEmptyWells) {
        Workbook workbook = StringTable.openWorkSheet(layoutFile);

        Plate layoutPlate = new Plate();
        layoutPlate.setBarcode("plate");

        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(DEFAULT_LAYOUT_SHEET_NAME);


        Rectangle plateDim = StringTable.guessPlateBounds(sheet, new Point(COL_OFFSET, Excel2003WriterUtils.ROW_OFFSET));
        StringTable treatments = StringTable.readStringGridFromExcel(plateDim, workbook.getSheetAt(0));
        StringTable concentrations = StringTable.readStringGridFromExcel(LayoutUtils.getNextTableBounds(plateDim), workbook.getSheetAt(0));

        for (int colIndex = 1; colIndex < treatments.getWidth(); colIndex++) {
            for (int rowIndex = 1; rowIndex < treatments.getHeight(); rowIndex++) {
                Well well = new Well();
                well.setPlate(layoutPlate);

                well.setPlateColumn(colIndex);
                well.setPlateRow(rowIndex);
                well.setReadoutSuccess(false);

                String treatment = treatments.get(rowIndex, colIndex);
                if (treatment.trim().isEmpty() && ignoreEmptyWells)
                    continue;

                well.setTreatment(treatment);
                well.setCompoundConcentration(concentrations.get(rowIndex, colIndex));

                layoutPlate.getWells().add(well);
            }
        }

        return layoutPlate;
    }


    public static Rectangle getNextTableBounds(Rectangle plateDim) {

        double startRow = plateDim.getY() + plateDim.getHeight() + Excel2003WriterUtils.CONC_TABLE_ROW_OFFSET - 1;

        return new Rectangle((int) plateDim.getX(), (int) startRow, (int) plateDim.getWidth(), (int) plateDim.getHeight());
    }


    public static Workbook openWorkBook(File excelFile) {
        String fileName = excelFile.getName();

        if (!excelFile.isFile() || !(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
            return null;
        }

        Workbook wb;

        try {
            if (fileName.endsWith(".xlsx")) {
                InputStream excelStream = new BufferedInputStream(new FileInputStream(excelFile));
                wb = new XSSFWorkbook(excelStream);
            } else {
                wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFile)));

            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return wb;
    }


    public static Map<String, StringTable> loadLayout(String sheetName, String fileName) {
        Workbook workbook;
        try {
            workbook = openWorkBook(new File(fileName));

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        Map<String, StringTable> layoutDimensions = new LinkedHashMap<String, StringTable>();

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new RuntimeException("No sheet named '" + sheetName + "'");
        }


        Rectangle plateDim = null;

        // 1) parse all layout-grids into memory

        while (true) {
            if (plateDim == null) {
                plateDim = StringTable.guessPlateBounds(sheet, new Point(COL_OFFSET, Excel2003WriterUtils.ROW_OFFSET));
            } else {
                plateDim = getNextTableBounds(plateDim);
            }


            if (plateDim == null || !isInBounds(sheet, plateDim)) {
                break;
            }

            String dimName = getCell(sheet, (int) plateDim.getY() - 2, (int) plateDim.getX() - 1);
            if (dimName == null) {
                break;
            }

            // do some basic validation
            String cellOne = getCell(sheet, (int) plateDim.getY() - 1, (int) plateDim.getX());
            String cellA = getCell(sheet, (int) plateDim.getY(), (int) plateDim.getX() - 1);
            if (cellA == null || !cellA.trim().equals("A") || cellOne == null || !cellOne.trim().equals("1")) {
                break;
            }

            // if there's no treatment we don't do anything because this is unlikely to have meaningful semantics
            if (StringUtils.isBlank(dimName)) {
                break;
            }

            StringTable layoutDim = StringTable.readStringGridFromExcel(plateDim, sheet);

            if (layoutDimensions.containsKey(dimName)) {
                throw new RuntimeException("Duplicated layout property: " + dimName);
            }

            // make sure that dimensions are the same
            if (!layoutDimensions.isEmpty()) {
                StringTable aTable = layoutDimensions.values().iterator().next();

                if (aTable.getHeight() != layoutDim.getHeight() || aTable.getWidth() != layoutDim.getWidth()) {
                    throw new RuntimeException("table dimensions in input-files do not match");
                }
            }

            layoutDimensions.put(dimName, layoutDim);
        }

        return layoutDimensions;
    }


    private static String getCell(Sheet sheet, int rowIndex, int colIndex) {
        if (isMissingCell(sheet, rowIndex, colIndex)) {
            return null;
        }

        Cell cell = sheet.getRow(rowIndex).getCell(colIndex);
        return cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? "" + (int) cell.getNumericCellValue() : cell.toString().trim();
    }


    private static boolean isMissingCell(Sheet sheet, int rowIndex, int colIndex) {
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


    public static boolean isInBounds(Sheet sheet, Rectangle plateDim) {
        return sheet.getLastRowNum() >= (plateDim.getY() + plateDim.getHeight() - 2) && getLastColumnNum(sheet) >= (plateDim.getWidth() + plateDim.getX() - 1);
    }


    private static int getLastColumnNum(Sheet sheet) {
        int maxCol = -1;
        for (Row row : sheet) {
            maxCol = Math.max(maxCol, row.getLastCellNum());
        }

        return maxCol;
    }


    public static void main(String[] args) {

//        Workbook workbook = LayoutUtils.openWorkBook(new File("/Volumes/tds/projects/Alnylam/Data_Primary_Screen/Layout/Layout_Primary_Screen.xls"));
//        Sheet sheet = workbook.getSheetAt(0);

//        String path = "/Volumes/tds/projects/Alnylam/Data_Primary_Screen/Layout/Layout_Primary_Screen.xls";
//        String path = "/Users/brandl/Desktop/ECHOdeme.xls";
//        String path = "/Volumes/tds/projects/EPHRIN/EXPERIMENTS/100611EP_384_Traf/001EP100611_Layout.xls";
//        String path = "/Users/brandl/Desktop/Layout_Primary_Screen.xls";
//        Map<String, StringTable> stringStringTableMap = loadLayout("Layout", path);
//        System.err.println("layout is " + stringStringTableMap);

//        Rectangle plateDim = StringTable.guessPlateBounds(sheet, new Point(LayoutUtils.COL_OFFSET, Excel2003WriterUtils.ROW_OFFSET));
//
//        StringTable table1 = StringTable.readStringGridFromExcel(plateDim, sheet);
//
//        System.err.println("table is" + table1);
    }
}
