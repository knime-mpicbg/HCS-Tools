package de.mpicbg.tds.core.util;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class StringArrayDummySheet implements Sheet {

    private final ArrayList<StringArrayDummyRow> rows;


    public StringArrayDummySheet(List<String[]> csvLines) {

        rows = new ArrayList<StringArrayDummyRow>();
        for (String[] rowData : csvLines) {
            rows.add(new StringArrayDummyRow(rowData));
        }

    }


    public Row createRow(int i) {
        return null;
    }


    public void removeRow(Row row) {
    }


    public Row getRow(int i) {
        return rows.get(i);
    }


    public int getPhysicalNumberOfRows() {
        return 0;
    }


    public int getFirstRowNum() {
        return 0;
    }


    public int getLastRowNum() {
        return rows.size() - 1;
    }


    public void setColumnHidden(int i, boolean b) {
    }


    public boolean isColumnHidden(int i) {
        return false;
    }


    public void setColumnWidth(int i, int i1) {
    }


    public int getColumnWidth(int i) {
        return 0;
    }


    public void setDefaultColumnWidth(int i) {
    }


    public int getDefaultColumnWidth() {
        return 0;
    }


    public short getDefaultRowHeight() {
        return 0;
    }


    public float getDefaultRowHeightInPoints() {
        return 0;
    }


    public void setDefaultRowHeight(short i) {
    }


    public void setDefaultRowHeightInPoints(float v) {
    }


    public CellStyle getColumnStyle(int i) {
        return null;
    }


    public int addMergedRegion(CellRangeAddress cellRangeAddress) {
        return 0;
    }


    public void setVerticallyCenter(boolean b) {
    }


    public void setHorizontallyCenter(boolean b) {
    }


    public boolean getHorizontallyCenter() {
        return false;
    }


    public boolean getVerticallyCenter() {
        return false;
    }


    public void removeMergedRegion(int i) {
    }


    public int getNumMergedRegions() {
        return 0;
    }


    public CellRangeAddress getMergedRegion(int i) {
        return null;
    }


    public Iterator<Row> rowIterator() {
        return null;
    }


    public void setAutobreaks(boolean b) {
    }


    public void setDisplayGuts(boolean b) {
    }


    public void setDisplayZeros(boolean b) {
    }


    public boolean isDisplayZeros() {
        return false;
    }


    public void setFitToPage(boolean b) {
    }


    public void setRowSumsBelow(boolean b) {
    }


    public void setRowSumsRight(boolean b) {
    }


    public boolean getAutobreaks() {
        return false;
    }


    public boolean getDisplayGuts() {
        return false;
    }


    public boolean getFitToPage() {
        return false;
    }


    public boolean getRowSumsBelow() {
        return false;
    }


    public boolean getRowSumsRight() {
        return false;
    }


    public boolean isPrintGridlines() {
        return false;
    }


    public void setPrintGridlines(boolean b) {
    }


    public PrintSetup getPrintSetup() {
        return null;
    }


    public Header getHeader() {
        return null;
    }


    public Footer getFooter() {
        return null;
    }


    public void setSelected(boolean b) {
    }


    public double getMargin(short i) {
        return 0;
    }


    public void setMargin(short i, double v) {
    }


    public boolean getProtect() {
        return false;
    }


    public boolean getScenarioProtect() {
        return false;
    }


    public void setZoom(int i, int i1) {
    }


    public short getTopRow() {
        return 0;
    }


    public short getLeftCol() {
        return 0;
    }


    public void showInPane(short i, short i1) {
    }


    public void shiftRows(int i, int i1, int i2) {
    }


    public void shiftRows(int i, int i1, int i2, boolean b, boolean b1) {
    }


    public void createFreezePane(int i, int i1, int i2, int i3) {
    }


    public void createFreezePane(int i, int i1) {
    }


    public void createSplitPane(int i, int i1, int i2, int i3, int i4) {
    }


    public PaneInformation getPaneInformation() {
        return null;
    }


    public void setDisplayGridlines(boolean b) {
    }


    public boolean isDisplayGridlines() {
        return false;
    }


    public void setDisplayFormulas(boolean b) {
    }


    public boolean isDisplayFormulas() {
        return false;
    }


    public void setDisplayRowColHeadings(boolean b) {
    }


    public boolean isDisplayRowColHeadings() {
        return false;
    }


    public void setRowBreak(int i) {
    }


    public boolean isRowBroken(int i) {
        return false;
    }


    public void removeRowBreak(int i) {
    }


    public int[] getRowBreaks() {
        return new int[0];
    }


    public int[] getColumnBreaks() {
        return new int[0];
    }


    public void setColumnBreak(int i) {
    }


    public boolean isColumnBroken(int i) {
        return false;
    }


    public void removeColumnBreak(int i) {
    }


    public void setColumnGroupCollapsed(int i, boolean b) {
    }


    public void groupColumn(int i, int i1) {
    }


    public void ungroupColumn(int i, int i1) {
    }


    public void groupRow(int i, int i1) {
    }


    public void ungroupRow(int i, int i1) {
    }


    public void setRowGroupCollapsed(int i, boolean b) {
    }


    public void setDefaultColumnStyle(int i, CellStyle cellStyle) {
    }


    public void autoSizeColumn(int i) {
    }


    public void autoSizeColumn(int i, boolean b) {
    }


    public Comment getCellComment(int i, int i1) {
        return null;
    }


    public Drawing createDrawingPatriarch() {
        return null;
    }


    public Workbook getWorkbook() {
        return null;
    }


    public String getSheetName() {
        return null;
    }


    public boolean isSelected() {
        return false;
    }


    public CellRange<? extends Cell> setArrayFormula(String s, CellRangeAddress cellRangeAddress) {
        return null;
    }


    public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
        return null;
    }


    public DataValidationHelper getDataValidationHelper() {
        return null;
    }


    public void addValidationData(DataValidation dataValidation) {
    }


    public Iterator<Row> iterator() {
        return null;
    }
}
