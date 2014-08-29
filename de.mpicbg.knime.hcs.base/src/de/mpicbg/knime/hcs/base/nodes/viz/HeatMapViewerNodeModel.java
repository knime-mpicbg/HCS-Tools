package de.mpicbg.knime.hcs.base.nodes.viz;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.color.LinearColorGradient;
import de.mpicbg.knime.hcs.base.heatmap.color.RescaleStrategy;
import de.mpicbg.knime.hcs.base.heatmap.io.ScreenImage;
import de.mpicbg.knime.hcs.base.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatTrellis;
import de.mpicbg.knime.hcs.base.nodes.layout.ExpandPlateBarcode;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.data.property.ColorModelUtils;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParser;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParserFactory;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.PlateUtils;
import de.mpicbg.knime.hcs.core.model.Well;

import org.knime.core.data.*;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.data.property.ColorModelNominal;
import org.knime.core.node.*;
import org.knime.core.node.config.Config;
import org.knime.core.node.config.base.AbstractConfigEntry;
import org.knime.core.node.config.base.ConfigEntries;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

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

    /** Data model */
    private HeatMapModel heatMapModel = new HeatMapModel();

    /** Used for delayed deserialization of plate-dump-file */
    private File internalBinFile;

    /** Used for delayed deserialization of the view configuration */
    private File viewConfigFile;

    /** Image port output spec */
    private ImagePortObjectSpec imagePortObjectSpec = new ImagePortObjectSpec(PNGImageContent.TYPE);


    /**
     * Constructor
     */
    public HeatMapViewerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[] {BufferedDataTable.TYPE, ImagePortObject.TYPE},true); // Set the flag for the new settings model.

        addModelSetting(READOUT_SETTING_NAME, createReadoutSettingsModel());
        addModelSetting(FACTOR_SETTING_NAME, createFactorSettingModel());
        addModelSetting(GROUP_BY_SETTING_NAME, createGroupBySettingModel());
        addModelSetting(PLATE_ROW_SETTING_NAME, createPlateRowSettingModel());
        addModelSetting(PLATE_COLUMN_SETTING_NAME, createPlateColumnSettingModel());
        addModelSetting(PLATE_LABEL_SETTING_NAME, createPlateLabelSettingName());
        addModelSetting(REFERENCE_POPULATIONS_SETTING_NAME, createReferencePopulationsSettingModel());
        addModelSetting(REFERENCE_PARAMETER_SETTING_NAME, createReferenceParameterSettingModel());

        reset();
    }


    /**
     * Create the "reference population parameter" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "reference population parameter" setting model
     */
    static SettingsModelString createReferenceParameterSettingModel() {
        return new SettingsModelString(REFERENCE_PARAMETER_SETTING_NAME, PlateUtils.SCREEN_MODEL_TREATMENT);
    }

    /**
     * Create the "reference population names" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "reference population names" setting model
     */
    static SettingsModelStringArray createReferencePopulationsSettingModel() {
        return new SettingsModelStringArray(REFERENCE_POPULATIONS_SETTING_NAME, new String[0]);
    }

    /**
     * Create the "plate label" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "plate label" setting model
     */
    static SettingsModelString createPlateLabelSettingName() {
        return new SettingsModelString(PLATE_LABEL_SETTING_NAME, PlateUtils.SCREEN_MODEL_BARCODE);
    }

    /**
     * Create the "plate column" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "plate column" setting model
     */
    static SettingsModelString createPlateColumnSettingModel() {
        return new SettingsModelString(PLATE_COLUMN_SETTING_NAME, PlateUtils.SCREEN_MODEL_WELL_COLUMN);
    }

    /**
     * Create the "plate row" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "plate row" setting model
     */
    static SettingsModelString createPlateRowSettingModel() {
        return new SettingsModelString(PLATE_ROW_SETTING_NAME, PlateUtils.SCREEN_MODEL_WELL_ROW);
    }

    /**
     * Create a the "group by" parameters setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "group by" setting model
     */
    static SettingsModelString createGroupBySettingModel() {
        return new SettingsModelString(GROUP_BY_SETTING_NAME, PlateUtils.SCREEN_MODEL_BARCODE);
    }

    /**
     * Create the "factors" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "factors" setting model
     */
    static SettingsModelFilterString createFactorSettingModel() {
        return new SettingsModelFilterString(FACTOR_SETTING_NAME, new String[]{}, new String[]{});
    }

    /**
     * Create the "readouts" setting model with the default columns name as
     * defined by {@link PlateUtils}.
     *
     * @return "readouts" setting model
     */
    static SettingsModelFilterString createReadoutSettingsModel() {
        return new SettingsModelFilterString(READOUT_SETTING_NAME, new String[]{}, new String[]{});
    }


    /** {@inheritDoc} */
    @Override
    protected void reset() {
        heatMapModel.setScreen(null);
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        configure(new DataTableSpec[]{(DataTableSpec) inSpecs[0]});
        return new PortObjectSpec[]{inSpecs[0], imagePortObjectSpec};
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        SettingsModelFilterString readoutSettings = ((SettingsModelFilterString) getModelSetting(READOUT_SETTING_NAME));
        SettingsModelFilterString factorSettings = ((SettingsModelFilterString) getModelSetting(FACTOR_SETTING_NAME));

        // If nothing was configured (dialog) we take a guess...
        if (readoutSettings.getIncludeList().size() == 0) {
            logger.warn("The node was configured automatically. Please check configuration settings before execution.");
            List<String> numericColumnNames = new ArrayList<String>();
            List<String> otherColumnNames = new ArrayList<String>();

            List<String> defaultColumnNames = Arrays.asList(PlateUtils.SCREEN_MODEL_BARCODE,
                    PlateUtils.SCREEN_MODEL_WELL_ROW, PlateUtils.SCREEN_MODEL_WELL_COLUMN);

            // Separate readouts and factors (exclude the plate description)
            for (DataColumnSpec cSpec : inSpecs[0]) {
                if (defaultColumnNames.contains(cSpec.getName()))
                    continue;

                if (cSpec.getType().isCompatible(DoubleValue.class)) {
                    numericColumnNames.add(cSpec.getName());
                } else {
                    otherColumnNames.add(cSpec.getName());
                }
            }

            // By default all the readouts are included.
            readoutSettings.setNewValues(numericColumnNames, new ArrayList<String>(), false);
            addModelSetting(READOUT_SETTING_NAME, readoutSettings);

            // Set the factors
            factorSettings.setNewValues(otherColumnNames, new ArrayList<String>(), false);
            addModelSetting(FACTOR_SETTING_NAME, factorSettings);
        }

        AttributeUtils.validate(readoutSettings.getIncludeList(), inSpecs[0]);

        return new DataTableSpec[]{inSpecs[0], null};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {
        long startTime = System.currentTimeMillis();

        // Process the input table
        BufferedDataTable table = (BufferedDataTable)inData[0];
        execute(new BufferedDataTable[] {table}, exec);

        // Create the image output
        ImagePortObject imagePort = createImageOutput();

        // Arrange the output
        PortObject[] output = new PortObject[2];
        output[0] = inData[0];
        output[1] = imagePort;

        logger.info("HeatMapViewer node execution time (sec.): " + (System.currentTimeMillis() - startTime) / 1000 );

        return output;
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inTables, ExecutionContext exec) throws Exception {
        // React on empty tables
        if (!inTables[0].iterator().hasNext()) {
            return inTables;
        }

        // Parse the data Table
        heatMapModel = new HeatMapModel();
        heatMapModel.setInternalTables(inTables);
        parseInputData(inTables[0], exec);

        return inTables;
    }

    /**
     * Create a PNG image of the HeatTrellis
     *
     * @return port object
     * @throws IOException
     */
    private ImagePortObject createImageOutput() throws IOException {
        // Set the row column configuration
        Integer columns = 5;
        Integer rows =  (int) Math.ceil(heatMapModel.getCurrentNumberOfPlates() * 1.0 / columns);
        heatMapModel.setNumberOfTrellisColumns(columns);
        heatMapModel.setNumberOfTrellisRows(rows);
        heatMapModel.setAutomaticTrellisConfiguration(false);

        // Create the heatmap panel from screen (turn the buffers off)
        ScreenViewer viewer = new ScreenViewer(heatMapModel);
        HeatTrellis trellis = viewer.getHeatTrellis();
        trellis.setTrellisHeatMapSize(420, 280);
        trellis.setPlateNameFontSize(28);

        trellis.setDoubleBuffered(false);
        trellis.updateContainerDimensions(rows, columns);
        trellis.repopulatePlateGrid();

        JPanel component = trellis.getHeatMapsContainer();
        component.setDoubleBuffered(false);

        // Create the colorbar
        HeatMapColorToolBar colorBar = new HeatMapColorToolBar(heatMapModel);
        colorBar.setMinimumSize(new Dimension(500,37));
        colorBar.setPreferredSize(new Dimension(500,37));
        colorBar.setLabelFont(22);

        // Add the colorbar
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        panel.add(colorBar, BorderLayout.PAGE_END);
        panel.setMinimumSize(new Dimension(300, 20));

        // Create the image.
        PNGImageContent imageContent;
        File tempFile = File.createTempFile("HeatMapTrellis", ".png");
        ImageIO.write(ScreenImage.createImage(panel), "png", tempFile);
        FileInputStream in = new FileInputStream(tempFile);
        imageContent = new PNGImageContent(in);
        in.close();

        // Clean up
        heatMapModel.setAutomaticTrellisConfiguration(true);
        viewer.setVisible(false);

        return new ImagePortObject(imageContent, imagePortObjectSpec);
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
        //super.loadInternals(nodeDir, executionMonitor);

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
        obj_out.writeObject(heatMapModel.getAnnotations());
        obj_out.writeObject(heatMapModel.getReadouts());
        obj_out.writeObject(heatMapModel.getReferencePopulations());
        obj_out.writeObject(heatMapModel.getScreen());

        // Cleanup
        obj_out.flush();
        obj_out.close();
    }

    /**
     * De-serialize the the internal data. This method de-serializes the data upon demand when {@link #getDataModel()}
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
                heatMapModel.setAnnotations((List<String>) obj_in.readObject());
                heatMapModel.setReadouts((List<String>) obj_in.readObject());
                heatMapModel.setReferencePopulations((HashMap<String, String[]>) obj_in.readObject());
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
        if (viewConfigFile == null)
            return;

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
    public void parseInputData(BufferedDataTable input, ExecutionContext exec) throws CanceledExecutionException {
        // Get chosen parameters to visualize
        List<String> parameters =  ((SettingsModelFilterString)getModelSetting(READOUT_SETTING_NAME)).getIncludeList();
        if (parameters.isEmpty())
            logger.warn("There are no readouts selected ('Readouts' tab in the configure dialog)!");

        // Get the chosen factors to visualize
        List<String> factorsFromSettings = ((SettingsModelFilterString)getModelSetting(FACTOR_SETTING_NAME)).getIncludeList();
        List<String> factors = new ArrayList<String>(factorsFromSettings);
        //Collections.copy(factors, factorsFromSettings);
        
        if (factors.isEmpty())
            logger.warn("There are no factors selected ('Factors' tab in the configure dialog)!");

        // Store the oder of parameters and factors as in the configuration dialog
        heatMapModel.setReadouts(parameters);
        heatMapModel.setAnnotations(factors);

        // Split input table by grouping column
        SettingsModelString groupBySetting = (SettingsModelString)getModelSetting(GROUP_BY_SETTING_NAME);
        Attribute<String> barcodeAttribute = new InputTableAttribute<String>(groupBySetting.getStringValue(), input);
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
        heatMapModel.setImageAttributes(imageAttributes);

        // Get columns represent plateRow and plateColumn
        SettingsModelString plateRowSetting = (SettingsModelString)getModelSetting(PLATE_ROW_SETTING_NAME);
        Attribute<String> plateRowAttribute = new InputTableAttribute<String>(plateRowSetting.getStringValue(), input);
        SettingsModelString plateColumnSetting = (SettingsModelString)getModelSetting(PLATE_COLUMN_SETTING_NAME);
        Attribute<String> plateColAttribute = new InputTableAttribute<String>(plateColumnSetting.getStringValue(), input);

        // Plate Label
        SettingsModelString plateLabelSetting = (SettingsModelString)getModelSetting(PLATE_LABEL_SETTING_NAME);
        Attribute plateLabelAttribute = new InputTableAttribute(plateLabelSetting.getStringValue(), input);

        // Put the info about the reference populations
        SettingsModelString referenceParameterSetting = (SettingsModelString)getModelSetting(REFERENCE_PARAMETER_SETTING_NAME);
        SettingsModelStringArray referencePopulationsSetting = (SettingsModelStringArray)getModelSetting(REFERENCE_POPULATIONS_SETTING_NAME);
        HashMap<String, String[]> reference = new HashMap<String, String[]>();
        reference.put(referenceParameterSetting.getStringValue(),  referencePopulationsSetting.getStringArrayValue());
        heatMapModel.setReferencePopulations(reference);
        if (referencePopulationsSetting.getStringArrayValue().length == 0)
            logger.warn("There are no reference groups selected ('Control' tab in the configure dialog)!");

        // Set the knime color column
        Attribute<Object> knimeColor =  AttributeUtils.getKnimeColorAttribute(input.getDataTableSpec());
        if (knimeColor != null) {
            heatMapModel.setKnimeColorAttribute(knimeColor.getName());
            
            ModelContent model = new ModelContent("Color"); 
            input.getDataTableSpec().getColumnSpec(knimeColor.getName()).getColorHandler().save(model);
            try {
            	if(ColorModelUtils.isNominalKnimeColor(model)) {
            		HashMap<String, Color> colorMap = ColorModelUtils.parseNominalColorModel(model);
            		// add knime colors to the colorscheme
            		heatMapModel.addKnimeColorMap(colorMap);
            	}
			} catch (InvalidSettingsException e) {
				logger.debug("color model cannot be parsed. implementation issue");
				e.printStackTrace();
			}
            // add knimeColor column to factors to store the column values within the screen object of the model (if not present)
            if(!factors.contains(knimeColor.getName())) factors.add(knimeColor.getName());
        }

        // Parse the plate data.
        heatMapModel.setScreen(parseIntoPlates(splitScreen,
                input.getDataTableSpec(),
                attributeModel,
                plateLabelAttribute,
                plateRowAttribute,
                plateColAttribute,
                parameters,
                factors,
                ExpandPlateBarcode.loadFactory(),
                exec));
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
     * @param readouts list of readout names.
     * @param factors list of factor names
     * @param bpf barcode parser factory
     * @param exec execution context
     *
     * @return a list of all the available {@link Plate}s
     */
    public static List<Plate> parseIntoPlates(Map<String, List<DataRow>> splitScreen,
                                              DataTableSpec tableSpec,
                                              List<Attribute> attributes,
                                              Attribute plateLabelAttribute,
                                              Attribute rowAttribute,
                                              Attribute colAttribute,
                                              List<String> readouts,
                                              List<String> factors,
                                              BarcodeParserFactory bpf,
                                              ExecutionContext exec) throws CanceledExecutionException {

        List<Plate> allPlates = new ArrayList<Plate>();

        double iterations = splitScreen.keySet().size();
        double iteration = 1;
        for (String barcode : splitScreen.keySet()) {
            exec.checkCanceled();
            exec.setProgress(iteration++/iterations);
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
