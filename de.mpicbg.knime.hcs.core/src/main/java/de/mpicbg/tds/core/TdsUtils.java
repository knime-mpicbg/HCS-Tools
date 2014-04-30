package de.mpicbg.tds.core;

import au.com.bytecode.opencsv.CSVReader;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;


/**
 * A collection of utility methods which have not yet been refactored into proper services.
 *
 * @author Holger Brandl
 */
public class TdsUtils {

    public static final String SCREEN_MODEL_TREATMENT = "treatment";
    public static final String SCREEN_MODEL_BARCODE = "barcode";
    public static final String SCREEN_MODEL_WELL = "well";
    public static final String SCREEN_MODEL_WELL_COLUMN = "plateColumn";
    public static final String SCREEN_MODEL_WELL_ROW = "plateRow";
    public static final String SCREEN_MODEL_LIB_CODE = "library code";
    public static final String SCREEN_MODEL_LIB_PLATE_NUMBER = "library plate number";
    public static final String SCREEN_MODEL_CONCENTRATION = "concentration";
    public static final String SCREEN_MODEL_CONCENTRATION_UNIT = "unit of concentration";
    public static final String SCREEN_MODEL_COMPOUND_ID = "compound id";
    public static final String TREATMENT_LIBRARY = "library";
    public static final String TREATMENT_UNTREATED = "untreated";

    // row labels up to 1536 well plate ('A','B',...'Z','AA','AB',...)
    public static final List<String> rowLabels;

    static {
        List<String> list = new ArrayList<String>();

        int j = 0;
        int offset = 'Z' - 'A' + 1;   // to restart the alphabet with i > 26

        for (int i = 1; i <= 32; i++) {
            if (j == 0) {
                char c = (char) ('A' + (i - 1));
                list.add(String.valueOf(c));
                if (c == 'Z') j = 1;
            } else {
                char c = (char) ('A' + (i - offset - 1));
                list.add("A" + String.valueOf(c));
            }
        }

        rowLabels = Collections.unmodifiableList(list);
    }


    public static File adapt2OS(String path) {
        if (!Utils.isWindowsPlatform()) {
            return new File(path.replace("Z:", "/Volumes/tds"));
        } else {
            return new File(path.replace("/Volumes/tds", "Z:"));
        }
    }


//    private static Map<String, String> lowerCase2normalC2ase = new HashMap();
//
//
//    static {
//        lowerCase2normalCase.put(SCREEN_MODEL_WELL_COLUMN, SCREEN_MODEL_WELL_COLUMN.toLowerCase());
//        lowerCase2normalCase.put(SCREEN_MODEL_WELL_ROW, SCREEN_MODEL_WELL_ROW.toLowerCase());
//        lowerCase2normalCase.put(SCREEN_MODEL_LIB_CODE, SCREEN_MODEL_LIB_CODE.toLowerCase());
//        lowerCase2normalCase.put(SCREEN_MODEL_LIB_PLATE_NUMBER, SCREEN_MODEL_LIB_PLATE_NUMBER.toLowerCase());
//    }


    /**
     * Converts 1 to A, 2 to B and so on.   Note: this conflicts with the usual array notation in java which start with
     * rowNumber 0.
     */
    public static String mapPlateRowNumberToString(int rowNumber) {
        //return new String(new char[]{(char) ('A' + rowNumber - 1)});
        if (rowLabels.size() <= (rowNumber - 1)) return null;
        return rowLabels.get(rowNumber - 1);
    }

    /**
     * converts A,B,C, .., Z, AA, AB, .., AF  to 1,2,3,...  (supports lower case strings)
     *
     * @param rowString
     * @return row number
     */
    public static int mapPlateRowStringToNumber(String rowString) {
        //assert rowString.length() == 1;
        //return rowString.toUpperCase().charAt(0) - 64;
        if (!rowLabels.contains(rowString.toUpperCase())) return -1;
        return rowLabels.indexOf(rowString) + 1;
    }


    public static Collection<String> collectTreatments(Collection<Plate> plates) {
        return collectAnnotationLevels(plates, TdsUtils.SCREEN_MODEL_TREATMENT);
    }

    public static Collection<String> collectAnnotationLevels(Collection<Plate> plates, String overlayName) {
        Set<String> treatments = new HashSet<String>();

        for (Plate plate : plates) {
            for (Well well : plate.getWells()) {
                treatments.add(well.getAnnotation(overlayName));
            }
        }

        return treatments;
    }


    public static List<String> flattenReadoutNames(Collection<Plate> plates) {
        HashSet<String> readoutNames = new HashSet<String>();
        for (Plate plate : plates) {
            for (Well well : plate.getWells()) {
                readoutNames.addAll(well.getReadOutNames());
            }
        }

        return new ArrayList<String>(readoutNames);
    }


    public static List<String> getReadoutNames(Collection<Well> wells) {
        HashSet<String> readoutNames = new HashSet<String>();
        for (Well well : wells) {
            readoutNames.addAll(well.getReadOutNames());
        }

        return new ArrayList<String>(readoutNames);
    }


    public static Collection<Plate> readWellsFromCSV(File file) {

        try {
            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(file)), '\t');

            String[] header = reader.readNext();

            Map<String, Plate> plateMap = new HashMap<String, Plate>();

            int barcodeColumn = 0;
            int plateRowColumn = 1;
            int plateColColumn = 2;

            String[] curLine;
            while ((curLine = reader.readNext()) != null) {
                Well well = new Well();

                for (int i = 0; i < curLine.length; i++) {
                    String colValue = curLine[i];

                    if (i == barcodeColumn) {
                        String barcode = curLine[i];
                        if (!plateMap.containsKey(barcode)) {
                            Plate plate = new Plate();
                            plate.setBarcode(barcode);

                            plateMap.put(barcode, plate);
                        }

                        Plate plate = plateMap.get(barcode);
                        plate.addWell(well);

                        continue;
                    }


                    if (i == plateRowColumn) {
                        well.setPlateRow(Integer.parseInt(colValue));
                        continue;
                    }

                    if (i == plateColColumn) {
                        well.setPlateColumn(Integer.parseInt(colValue));
                        continue;
                    }

                    Double readout;

                    try {
                        readout = Double.parseDouble(colValue);
                        well.getWellStatistics().put(header[i], readout);
//                        well.getWellStatistics().put(header[i], null);
                    } catch (NumberFormatException e) {
//                        well.setAnnotation(header[i], colValue);
                        well.getWellStatistics().put(header[i], null);
                    }
                }

            }

            return plateMap.values();

        } catch (Throwable t) {
        }

        return null;
    }


    public static void dumpWells2CSV(Collection<Well> wells, File file) {
        try {
            BufferedWriter bfwriter = new BufferedWriter(new FileWriter(file));

            List<String> readouts = getReadoutNames(wells);

            // write header
            bfwriter.write("barcode\trow\tcolumn\ttreatment");
            for (String readoutName : readouts) {
                bfwriter.write(readoutName + "\t");

            }
            bfwriter.newLine();

            for (Well well : wells) {
                bfwriter.write(well.getPlateRow() + "");
                bfwriter.write("\t");
                bfwriter.write(well.getPlateColumn() + "");
                bfwriter.write("\t");
                bfwriter.write("\"" + well.getTreatment() + "\"");
                bfwriter.write("\t");
                bfwriter.write(well.getPlate().getBarcode());
                bfwriter.write("\t");

                for (String readoutName : readouts) {
                    Double readout = well.getReadout(readoutName);
                    bfwriter.write((readout != null ? readout : "NAN") + "\t");
                }

                bfwriter.newLine();
            }

            bfwriter.flush();
            bfwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void dumpWells2Excel(List<Well> wells, File outputFile) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(outputFile);
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);

            List<String> readouts = flattenReadoutNames(TdsUtils.splitIntoPlates(wells));

            int colCounter = 0;
            sheet.addCell(new Label(colCounter++, 0, "row"));
            sheet.addCell(new Label(colCounter++, 0, "column"));
            sheet.addCell(new Label(colCounter++, 0, TdsUtils.SCREEN_MODEL_TREATMENT));
            sheet.addCell(new Label(colCounter++, 0, TdsUtils.SCREEN_MODEL_BARCODE));

            // write header
            for (String readoutName : readouts) {
                Label label = new Label(colCounter++, 0, readoutName);
                sheet.addCell(label);
            }

            for (int i = 0, wellsSize = wells.size(); i < wellsSize; i++) {
                Well well = wells.get(i);

                colCounter = 0;
                sheet.addCell(new jxl.write.Number(colCounter++, i + 1, well.getPlateRow()));
                sheet.addCell(new jxl.write.Number(colCounter++, i + 1, well.getPlateColumn()));
                sheet.addCell(new jxl.write.Label(colCounter++, i + 1, well.getTreatment()));
                sheet.addCell(new jxl.write.Label(colCounter++, i + 1, well.getPlate().getBarcode()));


                for (String readoutName : readouts) {
                    Double readout = well.getReadout(readoutName);
                    if (readout == null || readout == Double.NaN) {
                        sheet.addCell(new jxl.write.Label(colCounter++, i + 1, "NaN"));
                        continue;
                    }

                    sheet.addCell(new jxl.write.Number(colCounter++, i + 1, readout != null ? readout : Double.NaN));
                }
            }

            workbook.write();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

    }


    public static Collection<Well> flattenWells(Collection<Plate> plates) {
        Collection<Well> wells = new ArrayList<Well>();
        for (Plate plate : plates) {
            wells.addAll(plate.getWells());
        }

        return wells;
    }


    public static List<Plate> splitIntoPlates(Collection<Well> wells) {
        List<Plate> plates = new ArrayList<Plate>();

        for (Well well : wells) {
            Plate plate = well.getPlate();
            if (!plates.contains(plate)) {
                plates.add(plate);
            }

            plate.getWells().add(well);
        }

        return plates;
    }


    public static Map<Plate, Collection<Well>> splitIntoPlateMap(Collection<Well> wells) {
        Map<Plate, Collection<Well>> plateWells = new LinkedHashMap<Plate, Collection<Well>>();

        for (Well well : wells) {
            Plate plate = well.getPlate();
            if (!plateWells.containsKey(plate)) {
                plateWells.put(plate, new ArrayList<Well>());
            }

            plateWells.get(plate).add(well);
        }

        return plateWells;
    }


    public static RealMatrix getReadoutGrid(Plate plate, String readout) {
        Array2DRowRealMatrix readoutGrid = new Array2DRowRealMatrix(plate.getNumRows(), plate.getNumColumns());

        for (int i = 0; i < plate.getNumRows(); i++) {
            for (int j = 0; j < plate.getNumColumns(); j++) {
                readoutGrid.setEntry(i, j, Double.NaN);

            }

        }
        for (Well well : plate.getWells()) {
            readoutGrid.setEntry(well.getPlateRow() - 1, well.getPlateColumn() - 1, well.getReadout(readout));
        }

        return readoutGrid;
    }


    public static List<String> flattenAnnotationTypes(List<Plate> plates) {
        Collection<Well> allWells = flattenWells(plates);

        Collection<String> annotations = new HashSet<String>();

        for (Well well : allWells) {
            annotations.addAll(well.getAnnotations().keySet());

        }

        return new ArrayList<String>(annotations);
    }


    public static void repaintParentWindow(Component component) {
        while (component != null) {
            if (component instanceof JDialog || component instanceof JFrame) {
                component.repaint();
            }

            component = component.getParent();
        }
    }


    public static void unifyPlateDimensionsToLUB(List<Plate> allPlates) {
        Plate maxPlate = Collections.max(allPlates, new Comparator<Plate>() {
            public int compare(Plate plateA, Plate plateB) {
                return plateA.getNumColumns() - plateB.getNumColumns();
            }
        });

        for (Plate plate : allPlates) {
            plate.setNumColumns(maxPlate.getNumColumns());
            plate.setNumRows(maxPlate.getNumRows());
        }
    }

    /**
     * Created by IntelliJ IDEA.
     * User: niederle
     * Date: 10/5/11
     * Time: 12:26 PM
     * To change this template use File | Settings | File Templates.
     */
    public static enum PlateFormat {
        PF_384, PF_96
    }

    public static void main(String[] args) {
        Collection<Plate> plates = readWellsFromCSV(new File("resources/twoArrayScanPlates.csv"));
        System.err.println("num plates " + plates.size());

        // test conversion of plate labels (number <-> string)
        String row = "G";
        System.out.println(row + " - " + TdsUtils.mapPlateRowStringToNumber(row));
        int rowI = 5;
        System.out.println(rowI + " - " + TdsUtils.mapPlateRowNumberToString(rowI));
        String rowIS = "22";
        System.out.println(rowIS + " - " + TdsUtils.mapPlateRowNumberToString(Integer.valueOf(rowIS)));
    }
}
