package de.mpicbg.tds.core.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class StringArrayDummyRow implements Row {

    private String[] rowData;


    public StringArrayDummyRow(String[] rowData) {
        this.rowData = rowData;
    }


    public Cell createCell(int i) {
        return null;
    }


    public Cell createCell(int i, int i1) {
        return null;
    }


    public void removeCell(Cell cell) {
    }


    public void setRowNum(int i) {
    }


    public int getRowNum() {
        return 0;
    }


    public Cell getCell(int i) {
        return rowData.length > i ? new StringArrayDummyCell(rowData[i]) : null;
    }


    public Cell getCell(int i, MissingCellPolicy missingCellPolicy) {

        Cell cell = getCell(i);
        if (cell == null) {
            return null;
        }

        if (cell.getStringCellValue().trim().isEmpty() && missingCellPolicy.equals(Row.RETURN_BLANK_AS_NULL)) {
            return null;
        }

        return cell;
    }


    public short getFirstCellNum() {
        return 0;
    }


    public short getLastCellNum() {
        return (short) (rowData.length);
    }


    public int getPhysicalNumberOfCells() {
        return 0;
    }


    public void setHeight(short i) {
    }


    public void setZeroHeight(boolean b) {
    }


    public boolean getZeroHeight() {
        return false;
    }


    public void setHeightInPoints(float v) {
    }


    public short getHeight() {
        return 0;
    }


    public float getHeightInPoints() {
        return 0;
    }


    public Iterator<Cell> cellIterator() {
        return null;
    }


    public Sheet getSheet() {
        return null;
    }


    public Iterator<Cell> iterator() {
        return null;
    }
}
