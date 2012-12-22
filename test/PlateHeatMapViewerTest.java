import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.ScreenPanelFrame;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.ScreenViewer;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Conventions;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.apache.commons.lang.StringUtils;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.NodeLogger;

import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
//import de.mpicbg.tds.core.TdsUtils;
//import de.mpicbg.tds.core.model.Plate;
//import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.hcstools.visualization.ScreenExplorer;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
//import static de.mpicbg.tds.core.TdsUtils.SCREEN_MODEL_TREATMENT;
//import static de.mpicbg.tds.core.model.Plate.configurePlateByBarcode;
//import static de.mpicbg.tds.core.model.Plate.inferPlateDimFromWells;

/**
 * User: Felix Meyenhofer
 * Date: 11/27/12
 * Time: 14:41
 */

public class PlateHeatMapViewerTest {

    // Information that usually would be provided from the KNIME configuration dialogs.
    private String testDataPath = "/Users/turf/Sources/CBG/HCS-Tools/test/data/plateviewerinput.table";      //TODO put the testdata on a dropbox to not weigh down the package.
    private static List<String> patterns = Arrays.asList(
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*)",
            "(?<libplatenumber>[0-9]{3})(?<libcode>[A-Z]{3})(?<concentration>[_\\d]{3})(?<concunit>[A-z_]{4})(?<customa>[\\d]{1})(?<customb>[A-z]{1})(?<customc>[A-z]{1})_(?<customd>[A-z_]{3})",
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1})"
    );
    private List<String> factors = Arrays.asList(
            "date",
            "transfection",
            "concentration",
            "left_right",
            "top_bottom",
            "inner_outer"
    );
    private List<String> readouts = Arrays.asList(
            "Nuclei Intensity",
            "Calculation time (seconds)",
            "Number of Cells",
            "Ch2 Median of Maximum",
            "Ch1 Median of Maximum",
            "Median Syto Intensity",
            "library plate number",
            "concentration",
            "Median Nuclei Intensity.poc",
            "Number of Cells.poc",
            "Ch2 Median of Maximum.poc",
            "Ch1 Median of Maximum.poc",
            "Median Syto Intensity.poc"
    );


    private DataTable loadTable() {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(testDataPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataTable table = null;
        try {
            table = DataContainer.readFromStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return table;
    }

    // Methods from the Attribute utils, signature had to be changed (DataTable instead of the BufferedDataTable)
    private static Map<String, List<DataRow>> splitRows(DataTable table, Attribute factor) {
        Map<Object, List<DataRow>> groupedRows = splitRowsGeneric(table, factor);

        Map<String, List<DataRow>> groupedRowsStringKeys = new LinkedHashMap<String, List<DataRow>>();
        for (Object o : groupedRows.keySet()) {
            groupedRowsStringKeys.put(o.toString(), groupedRows.get(o));
        }

        return groupedRowsStringKeys;
    }

    private static Map<Object, List<DataRow>> splitRowsGeneric(DataTable table, Attribute factor) {
        Map<Object, List<DataRow>> splitScreen = new LinkedHashMap<Object, List<DataRow>>();

        for (DataRow dataRow : table) {
            Object groupFactor = factor.getValue(dataRow);

            if (!splitScreen.containsKey(groupFactor)) {
                splitScreen.put(groupFactor, new ArrayList<DataRow>());
            }

            splitScreen.get(groupFactor).add(dataRow);
        }

        return splitScreen;
    }

    // Method from the ScreenExplorer (node model) where the signature had to be changed (DataTable instead of the BufferedDataTable)
    private List<Plate> parseIntoPlates(DataTable input) {
        // grouping column
        Attribute<String> barcodeAttribute = new InputTableAttribute<String>(AbstractScreenTrafoModel.SCREEN_MODEL_BARCODE, input.getDataTableSpec());

        // split input table by grouping column
        Map<String, List<DataRow>> splitScreen = splitRows(input, barcodeAttribute);

        // retrieve table spec
        List<Attribute> attributeModel = AttributeUtils.convert(input.getDataTableSpec());

        // get columns represent plateRow and plateColumn
        Attribute<String> plateRowAttribute = new InputTableAttribute<String>("plateRow", input.getDataTableSpec());
        Attribute<String> plateColAttribute = new InputTableAttribute<String>("plateColumn", input.getDataTableSpec());

        // HACK for custom plate labels as requested by Martin
        Attribute customPlateNameAttr = null;
        /*if (getFlowVariable("heatmap.label") != null) {
            customPlateNameAttr = new InputTableAttribute(getFlowVariable("heatmap.label"), input);

        }*/
        // HACK for custom plate labels as requested by Martin

        List<Plate> allPlates = new ArrayList<Plate>();
        BarcodeParserFactory bpf = new BarcodeParserFactory(patterns);
        //List<String> ignoreProps = Arrays.asList("barcode", "numrows", "numcolumns", "screenedat", "librarycode");

        for (String barcode : splitScreen.keySet()) {
            Plate curPlate = new Plate();
            curPlate.setBarcode(barcode);
            allPlates.add(curPlate);

            //try to parse the barcode
            try {
                BarcodeParser barcodeParser = bpf.getParser(barcode);
                if (barcodeParser != null)
                    Plate.configurePlateByBarcode(curPlate, barcodeParser);
            } catch (Throwable t) {
                NodeLogger.getLogger(ScreenExplorer.class).error(t);
            }

            List<DataRow> wellRows = splitScreen.get(barcode);

            // HACK for custom plate labels as requested by Martin
            /*if (customPlateNameAttr != null) {
                // collect all names
                Set customPlateNames = new HashSet();
                for (DataRow wellRow : wellRows) {
                    customPlateNames.add(customPlateNameAttr.getValue(wellRow));
                }

                // condense it into a new plate name
                String customName = Arrays.toString(customPlateNames.toArray()).toString().replace("[", "").replace("]", "");
                curPlate.setBarcode(customName);
            }*/
            // HACK for custom plate labels as requested by Martin

            for (DataRow tableRow : wellRows) {
                Well well = new Well();
                curPlate.getWells().add(well);
                well.setPlate(curPlate);
                well.setPlateRow(plateRowAttribute.getIntAttribute(tableRow));
                well.setPlateColumn(plateColAttribute.getIntAttribute(tableRow));

                for (Attribute attribute : attributeModel) {
                    String attributeName = attribute.getName();

                    /*if (ignoreProps.contains(attributeName)) {
                        continue;
                    }*/

                    if (StringUtils.equalsIgnoreCase(Conventions.CBG.TREATMENT, attributeName)) {
                        well.setTreatment(attribute.getNominalAttribute(tableRow));
                    }

                    if (readouts.contains(attributeName) && attribute.isNumerical()) {
                        Double readoutValue = attribute.getDoubleAttribute(tableRow);
                        well.getWellStatistics().put(attributeName, readoutValue);
                    }

                    if (factors.contains(attributeName)) {
                        well.setAnnotation(attributeName, attribute.getRawValue(tableRow));
                    }
                }
            }
        }

        // ensure plate integrity by requesting a well by coordinates (which will through an exception if the plate layout is not valid)
        for (Plate plate : allPlates) {
            plate.getWell(0, 0);
        }

        // fix the plate dimension if necessary, using some heuristics, which defaults to 384
        for (Plate plate : allPlates) {
            Plate.inferPlateDimFromWells(plate);
        }

        PlateUtils.unifyPlateDimensionsToLUB(allPlates);
        return allPlates;
    }


    public static void main(String[] args) {
        PlateHeatMapViewerTest test = new PlateHeatMapViewerTest();
        DataTable table = test.loadTable();
        List<Plate> plates = test.parseIntoPlates(table);
        new ScreenViewer(plates);
//        new ScreenPanelFrame(plates);
    }

}
