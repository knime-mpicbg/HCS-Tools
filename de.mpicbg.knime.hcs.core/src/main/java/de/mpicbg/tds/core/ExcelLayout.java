package de.mpicbg.tds.core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 5/14/12
 * Time: 1:54 PM
 */
public class ExcelLayout implements Serializable {

    public static final int START_COL = 3 - 1;  // row and column indexing starts at 0
    public static final int START_ROW = 5 - 1;

    private long timestamp;

    private String fileName;
    private String sheetName = null;

    // keys: row, column, label, value
    private LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, String>>> layout = null;

    // list of labels and their data type (string, integer or double)
    private LinkedHashMap<String, Class<?>> labels;
    // plate dimensions
    private Dimension dims = null;

    // transient = cannopt be serialized
    private transient Workbook workbook = null;
    private transient Sheet sheet = null;


    public ExcelLayout(String fileName) throws IOException {
        this.fileName = fileName;
        //this.labels = new ArrayList<String>();
        this.labels = new LinkedHashMap<String, Class<?>>();

        openWorkbook();
    }

    /**
     * checks wether the file has been modified (or ist not accessible at all anymore
     *
     * @return
     */
    public boolean hasChanged() {

        File testAgainst = new File(fileName);
        if (testAgainst.lastModified() != timestamp) return true;

        return false;
    }

    /**
     * creates a new workbook object (different Excel formats)
     *
     * @throws IOException
     */
    private void openWorkbook() throws IOException {
        File excelFile = new File(fileName);

        timestamp = excelFile.lastModified();

        // open excel file
        if (fileName.endsWith(".xlsx")) {
            InputStream excelStream = new BufferedInputStream(new FileInputStream(excelFile));
            this.workbook = new XSSFWorkbook((excelStream));

        } else {
            this.workbook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelFile)));
        }
    }

    /**
     * method returns the names of all available excel sheets in the given file
     *
     * @return string array with sheet names
     * @throws IOException
     */
    public List<String> getSheetNames() {

        List<String> sheetNames = new ArrayList<String>();

        // get all existing sheet names
        int nSheets = workbook.getNumberOfSheets();

        for (int i = 0; i < nSheets; i++) {
            sheetNames.add(workbook.getSheetName(i));
        }

        return sheetNames;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) throws IOException {
        // try to load the sheet from the workbook
        this.sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new IOException("File does not contain the sheet '" + sheetName + "'");
        this.sheetName = sheetName;
    }

    public void parseLayoutLabels() throws ExcelLayoutException {
        // if the sheet exists
        if (sheet != null) {
            Point cellIdx = new Point(START_ROW, START_COL);

            // try to parse layouts as long as the end of the sheet is not reached
            while (cellIdx.getX() < sheet.getLastRowNum()) {
                String label = getLayoutLabel(cellIdx);
                if (label == null) break;
                cellIdx = parseDimensions(cellIdx, label);
            }
        }
        if (this.labels.isEmpty())
            throw new ExcelLayoutException("No layout could be loaded. Please check if the sheet '" + sheetName + "' contains layout information with the expected format.");

    }

    /**
     * parses the dimensions of the given position
     *
     * @param cellIdx
     * @param curLayoutlabel
     * @return
     */
    private Point parseDimensions(Point cellIdx, String curLayoutlabel) throws ExcelLayoutException {

        int sRow = (int) cellIdx.getX();
        int sCol = (int) cellIdx.getY();

        int numCols = 0;
        int numRows = 0;

        Row columnLabels = sheet.getRow(sRow + 1);
        if (columnLabels == null)
            throw new ExcelLayoutException("Row '" + (sRow + 1) + "' does not contain any column labels");

        // iterate columnwise over the row which should contain the column labels (1,2,...,m)
        // checks only for some content at the moment
        Cell curCell = columnLabels.getCell((sCol + 1), Row.RETURN_BLANK_AS_NULL);

        while (curCell != null) {
            numCols++;

            // check cell content (as expected?)
            String colLabel = getCellContent(curCell);
            double col = -1;
            // regular expressions shoul match any positive integer number; even like "15.00"
            if (colLabel.matches("^[0-9]+(\\.0+)?$")) col = Double.valueOf(colLabel);
            if (col != (double) numCols)
                throw new ExcelLayoutException("Layout " + curLayoutlabel + ": Column label '" + colLabel + "' does not fit to column " + numCols);

            curCell = columnLabels.getCell(curCell.getColumnIndex() + 1, Row.RETURN_BLANK_AS_NULL);
        }

        // iterate rowwise over the column which should contain the row labels (A,B,C,...)
        Row curRow = sheet.getRow(sRow + 2);
        if (curRow != null) curCell = curRow.getCell(sCol, Row.RETURN_BLANK_AS_NULL);

        while (curRow != null && curCell != null) {
            numRows++;

            // check cell content (as expected)
            String rowLabel = getCellContent(curCell);
            String expectedLabel = TdsUtils.mapPlateRowNumberToString(numRows);
            double row = -1;
            if (rowLabel.matches("^[0-9]+(\\.0+)?$")) row = Double.valueOf(rowLabel);
            if (!(expectedLabel.equals(rowLabel) || row == numRows))
                throw new ExcelLayoutException("Layout " + curLayoutlabel + ": Row label '" + rowLabel + "' does not fit to row " + expectedLabel + " or " + numRows);

            curRow = sheet.getRow(curRow.getRowNum() + 1);
            if (curRow != null) curCell = curRow.getCell(sCol, Row.RETURN_BLANK_AS_NULL);
        }

        labels.put(curLayoutlabel, null);

        if (numRows > 0 && numCols > 0) {

            // check whether the dimensions are the same with the dimensions read before
            Dimension newDims = new Dimension(numCols, numRows);
            if (dims != null) {
                if (!dims.equals(newDims)) return null;
            } else dims = newDims;

            // new point = old point + 1 (empty upper left corner) + number of plate rows + 2 (empty rows until next layout)
            return new Point(sRow + 1 + numRows + 3, sCol);
        } else throw new ExcelLayoutException("Layout " + curLayoutlabel + ": Failed to parse layout dimensions");
    }

    /**
     * @param cellIdx
     * @return
     */
    private String getLayoutLabel(Point cellIdx) {
        int sRow = (int) cellIdx.getX();
        int sCol = (int) cellIdx.getY();

        // check whether the line has some content at all
        if (sheet.getRow(sRow) == null) return null;
        // check wether the cell contains any data
        if (sheet.getRow(sRow).getCell(sCol, Row.RETURN_BLANK_AS_NULL) == null) return null;

        return getCellContent(sheet.getRow((int) cellIdx.getX()).getCell((int) cellIdx.getY()));
    }

    /**
     * the method parses the content of each layout label and tries to estimate the data type
     *
     * @throws ExcelLayoutException
     */
    public void parseLayoutContent() throws ExcelLayoutException {
        int sRow = START_ROW;
        int sCol = START_COL;

        int nRows = (int) dims.getHeight();
        int nCols = (int) dims.getWidth();

        // initialize layout content table
        if (dims == null)
            throw new ExcelLayoutException("Cannot parse layout content. Layout dimensions are not yet determined");
        else {
            // create a new hashmap with all keys
            layout = new LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, String>>>();
            for (int r = 1; r <= nRows; r++) {
                layout.put(r, new LinkedHashMap<Integer, HashMap<String, String>>());
                for (int c = 1; c <= nCols; c++)
                    layout.get(r).put(c, new HashMap<String, String>());
            }
        }

        Cell curCell = null;

        for (String label : this.labels.keySet()) {
            List<String> values = new ArrayList<String>();
            // iterate rowwise
            for (int r = 1; r <= nRows; r++) {
                Row curRow = sheet.getRow(sRow + 1 + r);
                for (int c = 1; c <= nCols; c++) {
                    curCell = curRow.getCell(sCol + c, Row.RETURN_BLANK_AS_NULL);
                    String cellContent = null;
                    if (curCell != null) cellContent = getCellContent(curCell);

                    layout.get(r).get(c).put(label, cellContent);
                    if (cellContent != null) values.add(cellContent);
                }
            }

            // now try guess the type: assume double, if yes try int, if not string
            Class<?> dataType = String.class;
            if (!values.isEmpty()) {
                boolean isDouble = isCompatible(values, "-?[\\d.]*");
                if (isDouble) {
                    if (isCompatible(values, "^-?[0-9]+(\\.0+)?$")) dataType = Integer.class;
                    else dataType = Double.class;
                }
            }
            labels.put(label, dataType);

            sRow = sRow + 1 + nRows + 3;
        }
    }

    /**
     * checks a list of strings if they all match the same pattern
     *
     * @param values
     * @param pattern
     * @return
     */
    private boolean isCompatible(List<String> values, String pattern) {

        boolean allEmpty = true;
        Pattern compiledPattern = Pattern.compile(pattern);

        for (String value : values) {
            if (value == null) {
                continue;
            }
            if (value.isEmpty()) {
                continue;
            }

            allEmpty = false;

            if (!compiledPattern.matcher(value).matches()) {
                return false;
            }
        }

        return !allEmpty;
    }

    public String getLayoutValue(String label, int plateRow, int plateColumn) {
        if (layout.containsKey(plateRow)) {
            if (layout.get(plateRow).containsKey(plateColumn)) {
                if (layout.get(plateRow).get(plateColumn).containsKey(label)) {
                    return layout.get(plateRow).get(plateColumn).get(label);
                }
            }
        }
        return null;
    }

    /**
     * @param cell
     * @return string representation of cell content
     */
    private String getCellContent(Cell cell) {
        int type = cell.getCellType();

        if (type == Cell.CELL_TYPE_FORMULA) type = cell.getCachedFormulaResultType();

        if (type == Cell.CELL_TYPE_BOOLEAN) return Boolean.toString(cell.getBooleanCellValue());
        if (type == Cell.CELL_TYPE_ERROR) return "ERROR";
        if (type == Cell.CELL_TYPE_NUMERIC) return Double.toString(cell.getNumericCellValue());
        if (type == Cell.CELL_TYPE_STRING) return cell.getStringCellValue();

        return null;
    }

    /**
     * Getter of layout labels and data types
     *
     * @return
     */
    public LinkedHashMap<String, Class<?>> getLabels() {
        return labels;
    }

    public static void main(String[] args) {

        // test if sheet names can be extracted
        String filename = "/Users/niederle/knime_sandbox/open/2012-05-11_JoinLayout_V2/data/testLayout1.xlsx";

        try {
            ExcelLayout layout = new ExcelLayout(filename);
            List<String> sheets = layout.getSheetNames();

            for (String sheet : sheets) {
                System.out.println(sheet + ", ");
            }

            // test whether a given sheet exists
            //layout.setSheetName("I do not exist");

            // handle wrong positioning
            //layout.setSheetName("wrongPos");

            // handle wrong column labeling
            //layout.setSheetName("wrongColLabel");

            // handle wrong row labels
            //layout.setSheetName("wrongRowLabel1");
            //layout.setSheetName("wrongRowLabel2");

            // correct layout
            layout.setSheetName("Chemical layout");

            layout.parseLayoutLabels();
            layout.parseLayoutContent();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExcelLayoutException e) {
            e.printStackTrace();
        }

    }

    /**
     * delivers a nested hashmap with keys: plate row, plate column, layout label and the corresponding value as string
     *
     * @return
     */
    public LinkedHashMap<Integer, LinkedHashMap<Integer, HashMap<String, String>>> getLayout() {
        return layout;
    }

    public class ExcelLayoutException extends Exception {
        private ExcelLayoutException(String s) {
            super(s);
        }
    }
}
