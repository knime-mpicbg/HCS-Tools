package de.mpicbg.knime.hcs.core.util;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Calendar;
import java.util.Date;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class StringArrayDummyCell implements Cell {

    private String value;


    public StringArrayDummyCell(String value) {
        this.value = value;
    }


    public int getColumnIndex() {
        return 0;
    }


    public int getRowIndex() {
        return 0;
    }


    public Sheet getSheet() {
        return null;
    }


    public Row getRow() {
        return null;
    }


    public void setCellType(int i) {
    }


    public int getCellType() {
        return Cell.CELL_TYPE_STRING;
    }


    public int getCachedFormulaResultType() {
        return 0;
    }


    public void setCellValue(double v) {
    }


    public void setCellValue(Date date) {
    }


    public void setCellValue(Calendar calendar) {
    }


    public void setCellValue(RichTextString richTextString) {
    }


    public void setCellValue(String s) {
    }


    public void setCellFormula(String s) throws FormulaParseException {
    }


    public String getCellFormula() {
        return null;
    }


    public double getNumericCellValue() {
        return 0;
    }


    public Date getDateCellValue() {
        return null;
    }


    public RichTextString getRichStringCellValue() {
        return null;
    }


    public String getStringCellValue() {
        return value;
    }


    public void setCellValue(boolean b) {
    }


    public void setCellErrorValue(byte b) {
    }


    public boolean getBooleanCellValue() {
        return false;
    }


    public byte getErrorCellValue() {
        return 0;
    }


    public void setCellStyle(CellStyle cellStyle) {
    }


    public CellStyle getCellStyle() {
        return null;
    }


    public void setAsActiveCell() {
    }


    public void setCellComment(Comment comment) {
    }


    public Comment getCellComment() {
        return null;
    }


    public void removeCellComment() {
    }


    public Hyperlink getHyperlink() {
        return null;
    }


    public void setHyperlink(Hyperlink hyperlink) {
    }


    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }


    public boolean isPartOfArrayFormulaGroup() {
        return false;
    }


    @Override
    public String toString() {
        return getStringCellValue();
    }
}
