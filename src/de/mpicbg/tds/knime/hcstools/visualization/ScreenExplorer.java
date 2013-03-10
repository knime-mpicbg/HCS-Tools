package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.hcstools.utils.ExpandPlateBarcode;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.*;
import java.util.*;

import static de.mpicbg.tds.core.TdsUtils.SCREEN_MODEL_TREATMENT;
import static de.mpicbg.tds.core.model.Plate.configurePlateByBarcode;
import static de.mpicbg.tds.core.model.Plate.inferPlateDimFromWells;
import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
@Deprecated
public class ScreenExplorer extends AbstractNodeModel {

    public SettingsModelFilterString propReadouts = createPropReadoutSelection();
    public SettingsModelFilterString propFactors = ScreenExplorerFactory.createPropFactorSelection();

    public SettingsModelString propGroupBy = HCSSettingsFactory.createGroupBy();
    public SettingsModelString propPlateRow = HCSSettingsFactory.createPropPlateRow();
    public SettingsModelString propPlateCol = HCSSettingsFactory.createPropPlateCol();

    // used for delayed deserialization of plate-dump-file
    public File plateListBinFile;


    public ScreenExplorer() {
        addSetting(propReadouts);
        addSetting(propFactors);

        addSetting(propGroupBy);
        addSetting(propPlateRow);
        addSetting(propPlateCol);

        reset();
    }


    public List<Plate> getPlates() {
        if (plates == null && plateListBinFile != null && plateListBinFile.isFile()) {
            logger.warn("Restoring plates from disk. This might take a few seconds...");
            deserializePlates();
        }

        return plates;
    }


    public List<Plate> plates;


    @Override
    protected void reset() {
        plates = null;
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        plates = parseIntoPlates(inData[0]);


//        if (wellSelection.isConnected()) {
//            Collection<Well> selectedWells = TdsUtils.flattenWells(parseIntoPlates(d));
//            screenExplorer.getScreenPanel().setSelection(selectedWells);
//        }

        return new BufferedDataTable[]{input};
    }


    public List<Plate> parseIntoPlates(BufferedDataTable input) {

        // get chosen parameters to visualize
        List<String> parameters = propReadouts.getIncludeList();

        // remove plateRow / plateColumn columns
        /*if (parameters.contains(TdsUtils.SCREEN_MODEL_WELL_COLUMN))
            parameters.remove(TdsUtils.SCREEN_MODEL_WELL_COLUMN);

        if (parameters.contains(TdsUtils.SCREEN_MODEL_WELL_ROW))
            parameters.remove(TdsUtils.SCREEN_MODEL_WELL_ROW);*/

        // grouping column
        Attribute<String> barcodeAttribute = new InputTableAttribute<String>(propGroupBy.getStringValue(), input);
        // split input table by grouping column
        Map<String, List<DataRow>> splitScreen = AttributeUtils.splitRows(input, barcodeAttribute);

        // retrieve table spec
        List<Attribute> attributeModel = AttributeUtils.convert(input.getDataTableSpec());

        // get columns represent plateRow and plateColumn

        Attribute<String> plateRowAttribute = new InputTableAttribute<String>(propPlateRow.getStringValue(), input);
        Attribute<String> plateColAttribute = new InputTableAttribute<String>(propPlateCol.getStringValue(), input);

        // HACK for custom plate labels as requested by Martin
        Attribute customPlateNameAttr = null;
        /*if (getFlowVariable("heatmap.label") != null) {
            customPlateNameAttr = new InputTableAttribute(getFlowVariable("heatmap.label"), input);

        }*/
        // HACK for custom plate labels as requested by Martin

        return parseIntoPlates(customPlateNameAttr, parameters, propFactors.getIncludeList(), splitScreen, attributeModel, plateRowAttribute, plateColAttribute);
    }


    public static List<Plate> parseIntoPlates(Attribute customPlateNameAttr, List<String> readouts, List<String> factors, Map<String, List<DataRow>> splitScreen, List<Attribute> attributeModel, Attribute rowAttribute, Attribute colAttribute) {
        List<Plate> allPlates = new ArrayList<Plate>();
        //List<String> ignoreProps = Arrays.asList("barcode", "numrows", "numcolumns", "screenedat", "librarycode");

        BarcodeParserFactory bpf = ExpandPlateBarcode.loadFactory();

        for (String barcode : splitScreen.keySet()) {
            Plate curPlate = new Plate();
            curPlate.setBarcode(barcode);

            allPlates.add(curPlate);

            //try to parse the barcode
            try {
                BarcodeParser barcodeParser = bpf.getParser(barcode);
                if (barcodeParser != null)
                    configurePlateByBarcode(curPlate, barcodeParser);
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


                well.setPlateRow(rowAttribute.getIntAttribute(tableRow));
                well.setPlateColumn(colAttribute.getIntAttribute(tableRow));

                for (Attribute attribute : attributeModel) {
                    String attributeName = attribute.getName();


                    /*if (ignoreProps.contains(attributeName)) {
                        continue;
                    }*/
//
                    if (StringUtils.equalsIgnoreCase(SCREEN_MODEL_TREATMENT, attributeName)) {
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
            inferPlateDimFromWells(plate);
        }


        TdsUtils.unifyPlateDimensionsToLUB(allPlates);

        return allPlates;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<String> includeReadouts = propReadouts.getIncludeList();
//        if (includeReadouts.isEmpty()) {
//            throw new RuntimeException("No attributes selected");
//        }

        AttributeUtils.validate(includeReadouts, inSpecs[0]);

        return new DataTableSpec[]{inSpecs[0]};
    }


    @Override
    protected void saveInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {

        if (getPlates() != null) {
            File f = new File(nodeDir, "plateList.bin");

            FileOutputStream f_out = new FileOutputStream(f);

            // Write object with ObjectOutputStream
            ObjectOutputStream obj_out = new ObjectOutputStream(new BufferedOutputStream(f_out));

            // Write object out to disk
            obj_out.writeObject(getPlates());
            obj_out.flush();
            obj_out.close();
        }

    }


    @Override
    protected void loadInternals(File nodeDir, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
        super.loadInternals(nodeDir, executionMonitor);

        plateListBinFile = new File(nodeDir, "plateList.bin");
    }


    private void deserializePlates() {
        try {
            if (plateListBinFile.isFile()) {
                FileInputStream f_in = new FileInputStream(plateListBinFile);

                // Read object using ObjectInputStream
                ObjectInputStream obj_in = new ObjectInputStream(new BufferedInputStream(f_in));

                // Read an object
                plates = (List<Plate>) obj_in.readObject();
            }

        } catch (Throwable e) {
        }
    }


    public void setPlotWarning(String msg) {
        setWarningMessage(msg);
    }
}