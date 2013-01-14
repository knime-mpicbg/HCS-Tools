package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.knime.hcstools.utils.ExpandPlateBarcode;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.color.LinearColorGradient;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.color.RescaleStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Conventions;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Well;
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

    /** File name to save internal plate data */
    private static final String PLATE_BIN_FILE = "plate_data.bin";

    /** File name to dump the view configuration */
    private static final String VIEW_BIN_FILE = "view_config.bin";

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

    /** Data model */
    private HeatMapModel heatMapModel = new HeatMapModel();

    /** Used for delayed deserialization of plate-dump-file */
    private File internalBinFile;

    /** Used for delayed deserialization of the view configuration.*/
    private File viewConfigFile;


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
        heatMapModel.setScreen(null);
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
        heatMapModel = new HeatMapModel();
        parseInputData(inData[0]);

        return inData;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
        // Make sure the data model is loaded.
        getDataModel();

        if ( heatMapModel == null ) {
            logger.info("No node internal data to save.");
        } else {
            internalBinFile = new File(nodeDir, PLATE_BIN_FILE);
            viewConfigFile = new File(nodeDir, VIEW_BIN_FILE);
            serializePlateData();
            serializeViewConfiguration();
            logger.debug("Node internal data was saved.");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
        super.loadInternals(nodeDir, executionMonitor);

        // Initialize the files in the node folder, so the data can be loaded when requested
        internalBinFile = new File(nodeDir, PLATE_BIN_FILE);
        viewConfigFile = new File(nodeDir, VIEW_BIN_FILE);
    }


    /**
     * Serialize the parts of the {@link HeatMapModel} ({@link #getDataModel()})
     *
     * @throws IOException
     */
    private void serializePlateData() throws IOException {
        FileOutputStream f_out = new FileOutputStream(internalBinFile);
        ObjectOutputStream obj_out = new ObjectOutputStream(new BufferedOutputStream(f_out));

        // Write object out to disk
        obj_out.writeObject(heatMapModel.getScreen());

        // Cleanup
        obj_out.flush();
        obj_out.close();
    }

    /**
     * Deserialize the the internal data. This method deserializes the data upon demand when {@link #getDataModel()}
     * is called.
     *
     * @throws IOException
     * @see {@link #getDataModel()}
     */
    private void deserializePlateData() throws IOException {
        if (internalBinFile.isFile()) {
            try {
                FileInputStream f_in = new FileInputStream(internalBinFile);
                ObjectInputStream obj_in = new ObjectInputStream(new BufferedInputStream(f_in));
                heatMapModel.setScreen((List<Plate>) obj_in.readObject());
                obj_in.close();

            } catch (ClassNotFoundException e) {
                logger.debug(e.getCause().toString() + " during deserialization of the plate data.");
            }

            logger.warn("The image data is transient. To be able to visualize it the node needs to be re-executed!");
        }
    }

    /**
     * Dumps the parts of the {@link HeatMapModel} that concern view settings
     * to a binary file.
     *
     * @throws IOException
     */
    protected void serializeViewConfiguration() throws IOException {
        FileOutputStream sout = new FileOutputStream(viewConfigFile);
        ObjectOutputStream oout = new ObjectOutputStream(sout);

        oout.writeObject(heatMapModel.getColorGradient());
        oout.writeObject(heatMapModel.getSortAttributesSelectionTitles());
        oout.writeObject(heatMapModel.getWellSelection());
        oout.writeObject(heatMapModel.getNumberOfTrellisRows());
        oout.writeObject(heatMapModel.getNumberOfTrellisColumns());
        oout.writeObject(heatMapModel.doHideMostFreqOverlay());
        oout.writeObject(heatMapModel.isGlobalScaling());
        oout.writeObject(heatMapModel.isFixedPlateProportion());
        oout.writeObject(heatMapModel.getReadoutRescaleStrategy());

        oout.flush();
        oout.close();
    }

    /**
     * De-serializes the parts of the {@link HeatMapModel} that concern the view settings
     * and parses it back to the object.
     *
     * @throws IOException
     */
    protected void deserializeViewConfiguration() throws IOException {
        FileInputStream sin = new FileInputStream(viewConfigFile);
        ObjectInputStream oin = new ObjectInputStream(sin);

        try {
            heatMapModel.setColorGradient((LinearColorGradient) oin.readObject());
            heatMapModel.setSortAttributeSelectionByTiles((String[]) oin.readObject());
            heatMapModel.setWellSelection((Collection<Well>) oin.readObject());
            heatMapModel.setNumberOfTrellisRows((Integer) oin.readObject());
            heatMapModel.setNumberOfTrellisColumns((Integer) oin.readObject());
            heatMapModel.setHideMostFreqOverlay((Boolean) oin.readObject());
            heatMapModel.setGlobalScaling((Boolean) oin.readObject());
            heatMapModel.setPlateProportionMode((Boolean) oin.readObject());
            heatMapModel.setReadoutRescaleStrategy((RescaleStrategy) oin.readObject());
            oin.close();
        } catch (ClassNotFoundException e) {
            logger.debug(e.getCause().toString() + " during serialization of the plate data.");
        }
    }

    /**
     * Get the {@link Plate} data objects containing the data for display. The method takes
     * care of loading the data on demand.
     *
     * @return a list of all the available {@link Plate}s
     */
    public HeatMapModel getDataModel() {
        if ((heatMapModel.getScreen() == null) && (internalBinFile != null) && internalBinFile.isFile()) {
            try {
                logger.warn("Restoring plates from disk. This might take a few seconds...");
                deserializePlateData();
                deserializeViewConfiguration();
                logger.debug("Loaded internal data.");
            } catch (IOException e) {
                heatMapModel.setScreen(null);
                logger.error(e.getCause().toString() + " during deserialization of the plate data.");
            }
        }

        return heatMapModel;
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
     */
    public void parseInputData(BufferedDataTable input) {
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
        HashMap<String, String[]> reference = new HashMap<String, String[]>();
        reference.put(propRefParameter.getStringValue(),  propRefNames.getStringArrayValue());
        heatMapModel.setReferencePopulations(reference);
        if (propRefNames.getStringArrayValue().length == 0)
            logger.warn("There are no reference groups selected ('Control' tab in the configure dialog)!");

        // Parse the plate data.
        heatMapModel.setScreen(parseIntoPlates(splitScreen,
                input.getDataTableSpec(),
                attributeModel,
                plateLabelAttribute,
                plateRowAttribute,
                plateColAttribute,
                imageAttributes,
                parameters,
                propFactors.getIncludeList(),
                ExpandPlateBarcode.loadFactory()));
    }

    /**
     * Parse the data from the {@link BufferedDataTable} into the internal data model.
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
     *
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
