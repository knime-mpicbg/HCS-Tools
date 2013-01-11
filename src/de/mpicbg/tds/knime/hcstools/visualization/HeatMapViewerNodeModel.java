package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.knime.hcstools.utils.ExpandPlateBarcode;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Conventions;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createTreatmentAttributeSelector;
import static de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerFactory.createSettingModelStringArray;
import static de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerFactory.createSettingsModelFilterString;
import static de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerFactory.createSettingsModelString;

import org.knime.core.data.*;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * This is the model implementation of HCS Heat Map Viewer.
 *
 * @author Holger Brandl (MPI-CBG)
 */

public class HeatMapViewerNodeModel extends AbstractNodeModel {

    /** Input port number */
    public static final int IN_PORT = 0;

    /** File name to save internal data */
    public static final String INTERNAL_BIN_FILENAME = "heatmap_model.bin";

    /** Setting names */
    static String READOUT_SETTING_NAME = "readout.setting";
    static String FACTOR_SETTING_NAME = "factor.setting";
    static String GROUP_BY_SETTING_NAME = "group.by.setting";
    static String PLATE_ROW_SETTING_NAME = "plate.row.setting";
    static String PLATE_COLUMN_SETTING_NAME = "plate.column.setting";
    static String PLATE_LABEL_SETTING_NAME = "plate.label.setting";
    static String REFERENCE_POPULATIONS_SETTING_NAME = "reference.populations";
    static String REFERENCE_PARAMETER_SETTING_NAME = "reference.parameter";

    /** Setting models */
    protected SettingsModelFilterString propReadouts = createSettingsModelFilterString(READOUT_SETTING_NAME);
    protected SettingsModelFilterString propFactors = createSettingsModelFilterString(FACTOR_SETTING_NAME);
    protected SettingsModelString propPlateLabel = createSettingsModelString(PLATE_LABEL_SETTING_NAME, Conventions.CBG.BARCODE);
    protected SettingsModelString propGroupBy = createSettingsModelString(GROUP_BY_SETTING_NAME, Conventions.CBG.BARCODE);
    protected SettingsModelString propPlateRow = createSettingsModelString(PLATE_ROW_SETTING_NAME, Conventions.CBG.WELL_ROW);
    protected SettingsModelString propPlateCol = createSettingsModelString(PLATE_COLUMN_SETTING_NAME, Conventions.CBG.WELL_COLUMN);
    protected SettingsModelStringArray propRefNames = createSettingModelStringArray(REFERENCE_POPULATIONS_SETTING_NAME);
    protected SettingsModelString propRefParameter = createTreatmentAttributeSelector();

    /** Data container */
    public List<Plate> plates;

    /** Used for delayed deserialization of plate-dump-file */
    public File internalBinFile;

    /** Parameter name and group names of the reference populations. */
    public HashMap<String, String[]> reference = new HashMap<String, String[]>();


    /**
     * Constructor
     */
    public HeatMapViewerNodeModel() {
        super(1,1,true); // Set the flag for the new settings model.

        addModelSetting(READOUT_SETTING_NAME, propReadouts);
        addModelSetting(FACTOR_SETTING_NAME, propFactors);
        addModelSetting(GROUP_BY_SETTING_NAME, propGroupBy);
        addModelSetting(PLATE_ROW_SETTING_NAME, propPlateRow);
        addModelSetting(PLATE_COLUMN_SETTING_NAME, propPlateCol);
        addModelSetting(PLATE_LABEL_SETTING_NAME, propPlateLabel);
        addModelSetting(REFERENCE_POPULATIONS_SETTING_NAME, propRefNames);
        addModelSetting(REFERENCE_PARAMETER_SETTING_NAME, propRefParameter);

        reset();
    }


    /** {@inheritDoc} */
    @Override
    protected void reset() {
        plates = null;
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<String> includeReadouts = propReadouts.getIncludeList();
        AttributeUtils.validate(includeReadouts, inSpecs[0]);

        return new DataTableSpec[]{inSpecs[0]};
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];
        plates = parseInputData(input);

        return new BufferedDataTable[]{input};
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {

        if (getPlates() != null) {
            File f = new File(nodeDir, INTERNAL_BIN_FILENAME);
            FileOutputStream f_out = new FileOutputStream(f);

            // Write object with ObjectOutputStream
            ObjectOutputStream obj_out = new ObjectOutputStream(new BufferedOutputStream(f_out));

            // Write object out to disk
            obj_out.writeObject(getPlates());
            obj_out.flush();
            obj_out.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
        super.loadInternals(nodeDir, executionMonitor);

        internalBinFile = new File(nodeDir, INTERNAL_BIN_FILENAME);
    }


    /**
     * Deserialize the the internal data.
     */
    private void deserializePlates() {
        try {
            if (internalBinFile.isFile()) {
                FileInputStream f_in = new FileInputStream(internalBinFile);

                // Read object using ObjectInputStream
                ObjectInputStream obj_in = new ObjectInputStream(new BufferedInputStream(f_in));

                // Read an object
                plates = (List<Plate>) obj_in.readObject();

                logger.warn("The image data is transient. To be able to visualize it the node needs to be re-executed!");
            }

        } catch (Throwable e) {
            logger.error("The loading of the internal data failed.");
            logger.debug(e.getStackTrace());
        }
    }

    /**
     * Get the {@link Plate} data objects containing the data for display.
     *
     * @return a list of all the available {@link Plate}s
     */
    public List<Plate> getPlates() {
        if (plates == null && internalBinFile != null && internalBinFile.isFile()) {
            logger.warn("Restoring plates from disk. This might take a few seconds...");
            deserializePlates();
        }

        return plates;
    }

    /**
     * Set the optional warning message
     *
     * @param msg message string
     */
    public void setPlotWarning(String msg) {
        setWarningMessage(msg);
    }

    /**
     * Parse the configuration dialog input.
     *
     * @param input node input table
     * @return a list of all the available {@link Plate}s
     */
    public List<Plate> parseInputData(BufferedDataTable input) {
        // Get chosen parameters to visualize
        List<String> parameters = propReadouts.getIncludeList();
        if (parameters.isEmpty())
            logger.warn("There are no readouts selected ('Readouts' tab in the configure dialog)!");

        // Get the chosen factors to visualize
        List<String> factors = propFactors.getIncludeList();
        if (factors.isEmpty())
            logger.warn("There are no factors selected ('Factors' tab in the configure dialog)!");

        // Split input table by grouping column
        Attribute<String> barcodeAttribute = new InputTableAttribute<String>(propGroupBy.getStringValue(), input);
        Map<String, List<DataRow>> splitScreen = AttributeUtils.splitRows(input, barcodeAttribute);

        // Retrieve table spec
        List<Attribute> attributeModel = AttributeUtils.convert(input.getDataTableSpec());

        // Get the image columns
        ArrayList<Attribute> imageAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : attributeModel) {
            if (attribute.isImageAttribute()) {
                imageAttributes.add(attribute);
            }
        }
        attributeModel.removeAll(imageAttributes);

        // Get columns represent plateRow and plateColumn
        Attribute<String> plateRowAttribute = new InputTableAttribute<String>(propPlateRow.getStringValue(), input);
        Attribute<String> plateColAttribute = new InputTableAttribute<String>(propPlateCol.getStringValue(), input);

        // Plate Label
        Attribute plateLabelAttribute = new InputTableAttribute(propPlateLabel.getStringValue(), input);

        // Put the info about the reference populations
        reference.put(propRefParameter.getStringValue(),  propRefNames.getStringArrayValue());
        if (propRefNames.getStringArrayValue().length == 0)
            logger.warn("There are no reference groups selected ('Control' tab in the configure dialog)!");

        return parseIntoPlates(splitScreen,
                               input.getDataTableSpec(),
                               attributeModel,
                               plateLabelAttribute,
                               plateRowAttribute,
                               plateColAttribute,
                               imageAttributes,
                               parameters,
                               propFactors.getIncludeList(),
                               ExpandPlateBarcode.loadFactory());
    }

    /**
     * Parse the data from the {@link BufferedDataTable} into the internal data model
     *
     * @param splitScreen the table rows mapped according the factor allowing to distinguish the plates
     * @param tableSpec input table specs
     * @param attributes list of all data attributes
     * @param plateLabelAttribute attribute used for the plate labeling
     * @param rowAttribute attribute indicating the row coordinate in the plate
     * @param colAttribute attribute indicating the column coordinate in the plate
     * @param imgAttributes list of image attributes
     * @param readouts list of readout names.
     * @param factors list of factor names
     * @param bpf barcode parser factory
     * @return a list of all the available {@link Plate}s
     */
    public static List<Plate> parseIntoPlates(Map<String, List<DataRow>> splitScreen,
                                              DataTableSpec tableSpec,
                                              List<Attribute> attributes,
                                              Attribute plateLabelAttribute,
                                              Attribute rowAttribute,
                                              Attribute colAttribute,
                                              List<Attribute> imgAttributes,
                                              List<String> readouts,
                                              List<String> factors,
                                              BarcodeParserFactory bpf) {

        List<Plate> allPlates = new ArrayList<Plate>();

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

            // Initialize collector for the image data.
            String[] columnNames = new String[imgAttributes.size()];
            DataType[] columnTypes = new DataType[imgAttributes.size()];
            ArrayList<DataCell> imageCells = new ArrayList<DataCell>();

            // Fill plate with wells.
            for (DataRow tableRow : wellRows) {
                Well well = new Well();
                curPlate.getWells().add(well); // TODO: Why is the well with no data added?

                well.setPlate(curPlate);
                well.setPlateRow(rowAttribute.getIntAttribute(tableRow));
                well.setPlateColumn(colAttribute.getIntAttribute(tableRow));
                well.setKnimeTableRowKey(tableRow.getKey());
                well.setKnimeRowColor(tableSpec.getRowColor(tableRow).getColor());


                for (Attribute attribute : attributes) {
                    String attributeName = attribute.getName();

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

                // Collect the images.
                if (!imgAttributes.isEmpty()) {
                    int imageIndex = 0;
                    for (Attribute attribute : imgAttributes) {
                        columnNames[imageIndex] = attribute.getName();
                        imageCells.add(attribute.getImageAttribute(tableRow));
                        columnTypes[imageIndex] = imageCells.get(imageIndex).getType();
                        imageIndex++;
                    }

                    // Create a DataContainer (Bufferable) for the image data.
                    DataContainer table = new DataContainer(new DataTableSpec(columnNames, columnTypes));
                    table.addRowToTable(new DefaultRow(new RowKey(""), imageCells));
                    table.close();
                    well.setImageData(table);
                    imageCells.clear();
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
