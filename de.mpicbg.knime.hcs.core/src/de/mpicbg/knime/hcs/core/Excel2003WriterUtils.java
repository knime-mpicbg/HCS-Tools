package de.mpicbg.knime.hcs.core;

import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import de.mpicbg.knime.hcs.core.util.StringTable;
import de.mpicbg.knime.hcs.core.view.color.ScreenColorScheme;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class Excel2003WriterUtils {

    public static int CONC_TABLE_ROW_OFFSET = 4;
    public static int ROW_OFFSET = 6;


    public static void saveLayoutAsExcel(Plate plate, File outFile) {

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(outFile);
            WritableSheet sheet = workbook.createSheet(LayoutUtils.DEFAULT_LAYOUT_SHEET_NAME, 0);

            int concTableOffset = CONC_TABLE_ROW_OFFSET + plate.getNumRows();

            WritableFont font = new WritableFont(WritableFont.ARIAL);
            font.setBoldStyle(WritableFont.BOLD);

            WritableCellFormat boldFormat = new WritableCellFormat(font);

            sheet.addCell(new Label(LayoutUtils.COL_OFFSET - 1, ROW_OFFSET - 2, TdsUtils.SCREEN_MODEL_TREATMENT));
            sheet.addCell(new Label(LayoutUtils.COL_OFFSET - 1, ROW_OFFSET + concTableOffset - 2, TdsUtils.SCREEN_MODEL_CONCENTRATION));

            ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();

            boldFormat.setBackground(jxl.format.Colour.GRAY_25);


            for (int rowIndex = 0; rowIndex <= plate.getNumRows(); rowIndex++) {
                for (int colIndex = 0; colIndex <= plate.getNumColumns(); colIndex++) {
                    int excelCol = colIndex + LayoutUtils.COL_OFFSET - 1;
                    int excelRow = rowIndex + ROW_OFFSET - 1;

                    if (rowIndex == 0 && colIndex == 0)
                        continue;

                    if (rowIndex == 0 && colIndex > 0) {
                        sheet.addCell(new Label(excelCol, excelRow, colIndex + "", boldFormat));
                        sheet.addCell(new Label(excelCol, excelRow + concTableOffset, colIndex + "", boldFormat));
                        continue;
                    }

                    if (colIndex == 0 && rowIndex > 0) {
                        sheet.addCell(new Label(excelCol, excelRow, TdsUtils.mapPlateRowNumberToString(rowIndex), boldFormat));
                        sheet.addCell(new Label(excelCol, excelRow + concTableOffset, TdsUtils.mapPlateRowNumberToString(rowIndex), boldFormat));
                        continue;
                    }

                    // write treatment-field
                    Well well = plate.getWell(colIndex, rowIndex);
                    String treatment = well.getTreatment();

                    WritableCellFormat cellFormat = new WritableCellFormat();
                    cellFormat.setBackground(colorScheme.getBestMatchingExcelColor(treatment));


                    if (treatment != null) {
                        sheet.addCell(new Label(excelCol, excelRow, treatment, cellFormat));
                    }

                    // write concentration
                    String concentration = well.getCompoundConcentration();
                    if (concentration != null) {
                        sheet.addCell(new Label(excelCol, excelRow + concTableOffset, concentration, cellFormat));
                    }
                }
            }

            workbook.write();
            workbook.close();

        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, BiffException {
        org.apache.poi.ss.usermodel.Workbook workbook = LayoutUtils.openWorkBook(new File("/Users/brandl/ttt.xls"));
        Rectangle plateDim = StringTable.guessPlateBounds(workbook.getSheetAt(0), new Point(LayoutUtils.COL_OFFSET, ROW_OFFSET));
        StringTable table = StringTable.readStringGridFromExcel(plateDim, workbook.getSheetAt(0));
        System.err.println("table is" + table);
    }
}
