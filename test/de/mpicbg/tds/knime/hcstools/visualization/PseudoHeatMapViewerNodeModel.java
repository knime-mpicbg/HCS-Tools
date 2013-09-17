package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.core.model.PlateUtils;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class simulating to some extent {@link de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel}
 * (essentially the execute method)
 *
 * @author Felix Meyenhofer
 *         creation: 1/17/13
 */
public abstract class PseudoHeatMapViewerNodeModel {

    private static List<String> patterns = Arrays.asList(
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*)",
            "(?<libplatenumber>[0-9]{3})(?<libcode>[A-Z]{3})(?<concentration>[_\\d]{3})(?<concunit>[A-z_]{4})(?<customa>[\\d]{1})(?<customb>[A-z]{1})(?<customc>[A-z]{1})_(?<customd>[A-z_]{3})",
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1})"
    );


    public static DataTable loadTable(String testDataPath) {
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
    public static HeatMapModel parseBufferedTable(DataTable input, List<String> readouts, List<String> factors) {
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

        // Get the image columns
        ArrayList<Attribute> imageAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : attributeModel) {
            if (attribute.isImageAttribute()) {
                imageAttributes.add(attribute);
            }
        }
        attributeModel.removeAll(imageAttributes);

        // Create the viewers data model
        HeatMapModel model = new HeatMapModel();

        // Store the oder of parameters and factors as in the configuration dialog
        model.setReadouts(readouts);
        model.setAnnotations(factors);

        // Set some reference populations.
        HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();
        referencePopulations.put("transfection",new String[]{"Mock", "Tox3", "Neg5", "Control2", "Control4"});
        model.setReferencePopulations(referencePopulations);

        // Set the knime color column
        Attribute<Object> knimeColor =  AttributeUtils.getKnimeColorAttribute(input.getDataTableSpec());
        if (knimeColor != null)
            model.setKnimeColorAttribute(knimeColor.getName());

        // Set the plate data.
        List<Plate> plates = parseIntoPlates(splitScreen, input.getDataTableSpec(),
                attributeModel, customPlateNameAttr,
                plateRowAttribute, plateColAttribute,
                readouts,
                factors, new BarcodeParserFactory(patterns));

        model.setScreen(plates);

        model.setImageAttributes(imageAttributes);

        return model;
    }


    public static List<Plate> parseIntoPlates(Map<String, List<DataRow>> splitScreen,
                                              DataTableSpec tableSpec,
                                              List<Attribute> attributes,
                                              Attribute plateLabelAttribute,
                                              Attribute rowAttribute,
                                              Attribute colAttribute,
                                              List<String> readouts,
                                              List<String> factors,
                                              BarcodeParserFactory bpf){

        List<Plate> allPlates = new ArrayList<Plate>();

        double iterations = splitScreen.keySet().size();
        double iteration = 1;
        for (String barcode : splitScreen.keySet()) {

            Plate curPlate = new Plate();
            curPlate.setBarcode(barcode);

            // Control collection.
            allPlates.add(curPlate);

            // Try to parse the barcode
            try {
                BarcodeParser barcodeParser = bpf.getParser(barcode);
                if (barcodeParser != null)
                    Plate.configurePlateByBarcode(curPlate, barcodeParser);
            } catch (Throwable t) {
                NodeLogger.getLogger(HeatMapViewerNodeModel.class).error(t);
            }

            // Split the screen according barcodes
            List<DataRow> wellRows = splitScreen.get(barcode);

            // Set the plate label.
            String label;
            try {
                label = plateLabelAttribute.getRawValue(wellRows.get(0));
            } catch (Exception e) {
                System.err.println("The columns for plate labeling did not work out. Taking the barcode as label instead.");
                label = barcode;
            }
            curPlate.setLabel(label);

            // Fill plate with wells.
            for (DataRow tableRow : wellRows) {
                Well well = new Well();
                curPlate.getWells().add(well);

                well.setPlate(curPlate);
                well.setPlateRow(rowAttribute.getIntAttribute(tableRow));
                well.setPlateColumn(colAttribute.getIntAttribute(tableRow));
                well.setKnimeTableRowKey(tableRow.getKey().getString());
                well.setKnimeRowColor(tableSpec.getRowColor(tableRow).getColor());

                // Parse the attributes and factors
                for (Attribute attribute : attributes) {
                    String attributeName = attribute.getName();

                    if (StringUtils.equalsIgnoreCase(PlateUtils.SCREEN_MODEL_TREATMENT, attributeName)) {
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

}
