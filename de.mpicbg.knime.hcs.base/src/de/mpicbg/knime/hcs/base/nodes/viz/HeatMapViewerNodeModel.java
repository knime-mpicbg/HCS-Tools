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
import org.knime.core.data.image.ImageValue;
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

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

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
    
    public static String WARN_MISSING_FILE_HANDLES = "Be aware: view settings will not be saved. Please save your workflow to enable it.";
    
    /** try to use node setting model to save internal data */
    private static final String VIEW_CONFIG_FILE_NAME = "view.config.xml";
    /** initial settings key */
    private static final String CFG_VIEW = "view.config";
    /** File handle for view configuration serialization/deserialization */
    private File viewConfigFile2;
    /** flag becomes true if the node is reset and the view configuration is kept */
    private boolean checkViewAgainstData = false;
    /** file name for plate data */
    private static final String PLATE_DATA_FILE_NAME = "plate.data.bin";
    /** File handle for plate data serialization*/
    private File plateDataFile2;
   
   /** Setting names */
    static String READOUT_SETTING_NAME = "readout.setting";
    static String FACTOR_SETTING_NAME = "factor.setting";
    static String GROUP_BY_SETTING_NAME = "group.by.setting";
    static String PLATE_ROW_SETTING_NAME = "plate.row.setting";
    static String PLATE_COLUMN_SETTING_NAME = "plate.column.setting";
    static String PLATE_LABEL_SETTING_NAME = "plate.label.setting";
    static String REFERENCE_POPULATIONS_SETTING_NAME = "reference.populations";
    static String REFERENCE_PARAMETER_SETTING_NAME = "reference.parameter";
    
    /** View settings models */
    private List<HeatMapModel> m_viewModels = new ArrayList<HeatMapModel>();
    
    /** Temporary place to keep node configurations */
    private HeatMapModel m_nodeConfigurations = null;

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
        m_nodeConfigurations = null;
        
        // delete internal files (only screen data, view configuration has to be checked against new data)
        if(this.plateDataFile2 != null) {
        	this.plateDataFile2.delete();
        	this.checkViewAgainstData = true;
        }
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
        	setWarningMessage("The node was configured automatically. Please check configuration settings before execution.");
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
        
        //reset flag, TODO: validate model against table spec, if already present
    	// this.checkViewAgainstData = false;
    	
    	// not possible to register the table for the models as they are not yet created
    	// temporarily store the incoming table as member
    	m_nodeConfigurations = new HeatMapModel(null);
    	m_nodeConfigurations.setInternalTables(inTables);
        
        // Parse the data Table
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
        Integer rows =  (int) Math.ceil(m_nodeConfigurations.getCurrentNumberOfPlates() * 1.0 / columns);
        m_nodeConfigurations.setNumberOfTrellisColumns(columns);
        m_nodeConfigurations.setNumberOfTrellisRows(rows);
        m_nodeConfigurations.setAutomaticTrellisConfiguration(false);

        // Create the heatmap panel from screen (turn the buffers off)
        ScreenViewer viewer = new ScreenViewer(m_nodeConfigurations);
        HeatTrellis trellis = viewer.getHeatTrellis();
        trellis.setTrellisHeatMapSize(420, 280);
        trellis.setPlateNameFontSize(28);

        trellis.setDoubleBuffered(false);
        trellis.updateContainerDimensions(rows, columns);
        trellis.repopulatePlateGrid();

        JPanel component = trellis.getHeatMapsContainer();
        component.setDoubleBuffered(false);

        // Create the colorbar
        HeatMapColorToolBar colorBar = new HeatMapColorToolBar(m_nodeConfigurations);
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
        m_nodeConfigurations.setAutomaticTrellisConfiguration(true);
        viewer.setVisible(false);

        return new ImagePortObject(imageContent, imagePortObjectSpec);
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {

        if ( m_nodeConfigurations.getScreen() == null ) {
            logger.info("No node internal data to save.");
        } else {
            // serialization
            this.viewConfigFile2 = new File(nodeDir, VIEW_CONFIG_FILE_NAME);
            this.plateDataFile2 = new File(nodeDir, PLATE_DATA_FILE_NAME);
            
            //remove previous warning
            if(getWarningMessage() != null) {
            	if(getWarningMessage().equals(WARN_MISSING_FILE_HANDLES))
            		setWarningMessage(null);
            }
            
            if(!serializePlateData())
            	setWarningMessage("Internal data could not be saved - See log file for more information");
            logger.debug("Node internal data was saved.");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {   
        // serialization	
        this.viewConfigFile2 = new File(nodeDir, VIEW_CONFIG_FILE_NAME);
        this.plateDataFile2 = new File(nodeDir, PLATE_DATA_FILE_NAME);
        
    	if(!(hasValidDataFile() && hasValidViewFile()))    		
    		throw new CanceledExecutionException("Invalid internal files - - Deserialization process not possible - Try to re-execute the node");
    	else
    		//as this warning will be restored when openening the workflow it needs to be removed
    		if(getWarningMessage().equals(WARN_MISSING_FILE_HANDLES))
    			setWarningMessage(null);
        
    }

    /**
     * Get the {@link Plate} data objects containing the data for display. The method takes
     * care of loading the data on demand.
     *
     * @return a list of all the available {@link Plate}s
     */
    public HeatMapModel getDataModel() {
    	
    	// if view data not yet present, load it from internal files
    	// this is the case if a workflow with an executed plate heatmap viewer node is loaded
/*        if ((m_nodeConfigurations.getScreen() == null) && !this.checkViewAgainstData) {
        	if(!hasInternalValidConfigFiles()) {
        		setWarningMessage("Invalid internal files - Deserialization process not possible");
        		return m_nodeConfigurations;
        	}
        	logger.warn("Restoring plates from disk. This might take a few seconds...");
        	// test another serialization approach
        	if(!deserializePlateDataTest())
        		setWarningMessage("Deserialization process for screen data failed - See log file for more information");
            if(!deserializeViewConfigurationTest())    
            	setWarningMessage("Deserialization process for view configuration failed - See log file for more information");
            logger.debug("Loaded internal data.");
        }*/

        return m_nodeConfigurations;
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
        	setWarningMessage("There are no readouts selected ('Readouts' tab in the configure dialog)!");

        // Get the chosen factors to visualize
        List<String> factorsFromSettings = ((SettingsModelFilterString)getModelSetting(FACTOR_SETTING_NAME)).getIncludeList();
        List<String> factors = new ArrayList<String>(factorsFromSettings);
        //Collections.copy(factors, factorsFromSettings);
        
        if (factors.isEmpty())
        	logger.warn("There are no factors selected ('Factors' tab in the configure dialog)!");

        // Store the oder of parameters and factors as in the configuration dialog
        m_nodeConfigurations.setReadouts(parameters);
        m_nodeConfigurations.setAnnotations(factors);

        // Split input table by grouping column
        SettingsModelString groupBySetting = (SettingsModelString)getModelSetting(GROUP_BY_SETTING_NAME);
        Attribute<String> barcodeAttribute = new InputTableAttribute<String>(groupBySetting.getStringValue(), input);
        Map<String, List<DataRow>> splitScreen = AttributeUtils.splitRows(input, barcodeAttribute);

        // Retrieve table spec
        List<Attribute> attributeModel = AttributeUtils.convert(input.getDataTableSpec());

        // Get the image columns
        ArrayList<String> imageAttributes = new ArrayList<String>();
        DataTableSpec tableSpec = input.getDataTableSpec();
        for (DataColumnSpec cspec : tableSpec) {
            if (cspec.getType().isCompatible(ImageValue.class) || cspec.getType().getPreferredValueClass().getName().contains("org.knime.knip.base.data")) {
                imageAttributes.add(cspec.getName());
            }
        }
        attributeModel.removeAll(imageAttributes);
        m_nodeConfigurations.setImageAttributes(imageAttributes);

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
        m_nodeConfigurations.setReferencePopulations(reference);
        if (referencePopulationsSetting.getStringArrayValue().length == 0)
        	logger.warn("There are no reference groups selected ('Control' tab in the configure dialog)!");

        // Set the knime color column
        Attribute<Object> knimeColor =  AttributeUtils.getKnimeColorAttribute(input.getDataTableSpec());
        if (knimeColor != null) {
            m_nodeConfigurations.setKnimeColorAttribute(knimeColor.getName());
            
            // use domain values to retrieve color model
            HashMap<String, Color> colorMap = ColorModelUtils.parseNominalColorModel(input.getDataTableSpec().getColumnSpec(knimeColor.getName()));
            m_nodeConfigurations.addKnimeColorMap(colorMap);
            
            // add knimeColor column to factors to store the column values within the screen object of the model (if not present)
            if(!factors.contains(knimeColor.getName())) factors.add(knimeColor.getName());
        }

        // Parse the plate data.
        m_nodeConfigurations.setScreen(parseIntoPlates(splitScreen,
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


	public boolean serializeViewConfiguration(HeatMapModel viewModel) {
		NodeSettings settings = new NodeSettings(CFG_VIEW);
		
		//populate node settings
		viewModel.saveViewConfigTo(settings);
		
		try {
			settings.saveToXML(new FileOutputStream(viewConfigFile2));
		} catch (FileNotFoundException e) {
			logger.error(e);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * reads view settings from internal file and populates heatmap view model
	 * screen data should be loaded first to ensure proper settings
	 * @return true, if deserialization process was sucessfull
	 */
	private boolean deserializeViewConfiguration(HeatMapModel viewModel) {
		NodeSettingsRO settings = null;
		
		try {
			settings = NodeSettings.loadFromXML(new FileInputStream(viewConfigFile2));
		} catch (FileNotFoundException e) {
			logger.debug(e.toString());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			logger.debug(e.toString());
			e.printStackTrace();
			return false;
		}
		
		try {
			viewModel.loadViewConfigFrom(settings);
		} catch (InvalidSettingsException e) {
			logger.debug(e);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
/*	*//**
	 * checks whether the file handles for internal data is present (only the case after loadInternals or saveInternals)
	 * @return
	 *//*
	public boolean hasInternalFileHandles() {
		return (viewConfigFile2 != null && plateDataFile2 != null);
	}

	*//**
	 * check if file handles are available and whether both files exist as normal file
	 * @return
	 *//*
	public boolean hasInternalValidConfigFiles() {
		if(!hasInternalFileHandles()) return false;
		return viewConfigFile2.isFile() && plateDataFile2.isFile();
	}*/
	
	public boolean hasValidDataFile() {
		if(plateDataFile2 == null) return false;
		return plateDataFile2.isFile();
	}
	
	public boolean hasValidViewFile() {
		if(viewConfigFile2 == null) return false;
		return viewConfigFile2.isFile();
	}
	
	public boolean hasDataFileHandle() {
		return plateDataFile2 != null;
	}
	
	public boolean hasViewFileHandle() {
		return viewConfigFile2 != null;
	}


	public boolean serializePlateData(){
		
		logger.debug("Try to deserialize plate data");

		FileOutputStream f_out;
		try {
			f_out = new FileOutputStream(plateDataFile2);
			ObjectOutputStream obj_out = new ObjectOutputStream(new BufferedOutputStream(f_out));

	        // Write plate data out to disk
	        obj_out.writeObject(m_nodeConfigurations.getScreen());

	        // Cleanup
	        obj_out.flush();
	        obj_out.close();	
		} catch (FileNotFoundException e) {
			logger.debug(e);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			logger.debug(e);
			e.printStackTrace();
			return false;
		}
        return true;	
	}

    @SuppressWarnings("unchecked")
	private boolean deserializePlateData(HeatMapModel viewModel){

    	try {
    		FileInputStream f_in = new FileInputStream(plateDataFile2);
    		ObjectInputStream obj_in = new ObjectInputStream(new BufferedInputStream(f_in));
    		viewModel.setScreen((List<Plate>) obj_in.readObject());
    		obj_in.close();

    	} catch (ClassNotFoundException e) {
    		logger.debug(e);
    		e.printStackTrace();
    		return false;
    	} catch (IOException e) {
    		logger.debug(e);
    		e.printStackTrace();
    		return false;
    	}

    	if(viewModel.hasImageData())
    		setWarningMessage("The image data is transient. To be able to visualize it the node needs to be re-executed!");
    return true;
    }

    /**
     * add view data / configuration settings to model and put it into the list of heatmap models
     * @param viewModel
     * @param uuid 
     */
	public void registerViewModel(HeatMapModel viewModel) {
		
		//load old view settings if available
		if(hasValidViewFile()) {
			if(!deserializeViewConfiguration(viewModel))    
	        	setWarningMessage("Deserialization process for view configuration failed - See log file for more information");	
		} else
			setPlotWarning(WARN_MISSING_FILE_HANDLES);
		
		// set screen data, internal table, configurations either after execute of from deserialization process
		if(m_nodeConfigurations == null) {
			m_nodeConfigurations = new HeatMapModel();
			// push node configurations to data model
			m_nodeConfigurations.setNodeConfigurations(viewModel);
			if(!deserializePlateData(m_nodeConfigurations))
				setWarningMessage("Deserialization process for screen data failed - See log file for more information");
			// push data to view model
			viewModel.setScreen(m_nodeConfigurations.getScreen());
		} else
			viewModel.setNodeConfigurations(m_nodeConfigurations);
		
		m_viewModels.add(viewModel);
	}

	/**
	 * remove the heatmap model form the list (if the view is closed)
	 * @param modelID
	 */
	public void unregisterViewModel(UUID modelID) {
		int idx = -1;
		for(HeatMapModel viewModel : m_viewModels) {
			if(viewModel.getModelID().equals(modelID))
				idx = m_viewModels.indexOf(viewModel);
		}
		m_viewModels.remove(idx);
	}


	public void keepViewModel(HeatMapModel m_viewModel) {
		// TODO Auto-generated method stub
		m_nodeConfigurations = m_viewModel;
	}

}
