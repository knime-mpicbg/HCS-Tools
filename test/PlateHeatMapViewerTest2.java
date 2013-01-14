
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.*;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.menu.HiLiteMenu;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.menu.TrellisMenu;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.menu.ViewMenu;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.container.DataContainer;

import de.mpicbg.tds.barcodes.BarcodeParserFactory;
//import de.mpicbg.tds.core.TdsUtils;
//import de.mpicbg.tds.core.model.Plate;
//import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;

import javax.swing.*;
//import static de.mpicbg.tds.core.TdsUtils.SCREEN_MODEL_TREATMENT;
//import static de.mpicbg.tds.core.model.Plate.configurePlateByBarcode;
//import static de.mpicbg.tds.core.model.Plate.inferPlateDimFromWells;

/**
 *
 * Reads a data set produced by the table writer node and passes it to the View.
 *
 * @author Felix Meyenhofer
 *         creation: 1/1/13
 */

public class PlateHeatMapViewerTest2 {

    // Information that usually would be provided from the KNIME configuration dialogs.
    private String testDataPath = "/Users/turf/Sources/CBG/HCS-Tools/test/data/96well_color_image.table";      //TODO put the testdata on a dropbox to not weigh down the package.
    private static List<String> patterns = Arrays.asList(
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<date>[0-9]{6})(?<replicate>[A-z]{1})-(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]*)",
            "(?<libplatenumber>[0-9]{3})(?<libcode>[A-Z]{3})(?<concentration>[_\\d]{3})(?<concunit>[A-z_]{4})(?<customa>[\\d]{1})(?<customb>[A-z]{1})(?<customc>[A-z]{1})_(?<customd>[A-z_]{3})",
            "(?<libplatenumber>[0-9]{3})(?<projectcode>[A-z]{2})(?<libcode>[_A-z\\d]{3})(?<assay>[-_\\s\\w\\d]{3})(?<date>[0-9]{6})(?<replicate>[A-z]{1})"
    );
    private List<String> factors = Arrays.asList(
            "barcode",
            "BS_Controls",
            "SBS_Doses"
    );
    private List<String> readouts = Arrays.asList(
            "Mean_Nuclei_AreaShape_Area",
            "Mean_Nuclei_AreaShape_Compactness",
            "Mean_Nuclei_AreaShape_Eccentricity",
            "Mean_Nuclei_AreaShape_EulerNumber",
            "Mean_Nuclei_AreaShape_Extent",
            "Mean_Nuclei_AreaShape_FormFactor",
            "Mean_Nuclei_AreaShape_Orientation",
            "Mean_Nuclei_AreaShape_Perimeter",
            "Mean_Nuclei_AreaShape_Solidity",
            "Mean_Nuclei_Children_DistCytoplasm_Count",
            "Mean_Nuclei_Children_DistanceCells_Count",
            "Mean_Nuclei_Children_PropCells_Count",
            "Mean_Nuclei_Children_PropCytoplasm_Count",
            "Mean_Nuclei_Correlation_Correlation_CorrGreen_CorrBlue",
            "Mean_Nuclei_Intensity_IntegratedIntensityEdge_CorrBlue",
            "Mean_Nuclei_Intensity_IntegratedIntensityEdge_CorrGreen",
            "Mean_Nuclei_Intensity_IntegratedIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_IntegratedIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_LowerQuartileIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_LowerQuartileIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_MassDisplacement_CorrBlue",
            "Mean_Nuclei_Intensity_MassDisplacement_CorrGreen",
            "Mean_Nuclei_Intensity_MaxIntensityEdge_CorrBlue",
            "Mean_Nuclei_Intensity_MaxIntensityEdge_CorrGreen",
            "Mean_Nuclei_Intensity_MaxIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_MaxIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_MeanIntensityEdge_CorrBlue",
            "Mean_Nuclei_Intensity_MeanIntensityEdge_CorrGreen",
            "Mean_Nuclei_Intensity_MeanIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_MeanIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_MedianIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_MedianIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_MinIntensityEdge_CorrBlue",
            "Mean_Nuclei_Intensity_MinIntensityEdge_CorrGreen",
            "Mean_Nuclei_Intensity_MinIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_MinIntensity_CorrGreen",
            "Mean_Nuclei_Intensity_StdIntensityEdge_CorrBlue",
            "Mean_Nuclei_Intensity_StdIntensityEdge_CorrGreen",
            "Mean_Nuclei_Intensity_StdIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_StdIntensity_CorrGreen",
            "Count_DistCytoplasm",
            "Mean_Nuclei_Intensity_UpperQuartileIntensity_CorrBlue",
            "Mean_Nuclei_Intensity_UpperQuartileIntensity_CorrGreen",
            " Mean_Nuclei_Math_Ratio1",
            "Mean_Nuclei_Math_Ratio2",
            "Mean_PropCells_AreaShape_Area",
            "Mean_PropCells_AreaShape_Compactness",
            "Mean_PropCells_AreaShape_Eccentricity",
            "Mean_PropCells_AreaShape_EulerNumber",
            "Mean_PropCells_AreaShape_Extent",
            "Mean_PropCells_AreaShape_FormFactor",
            "Count_DistanceCells",
            "Mean_PropCells_AreaShape_Orientation",
            "Mean_PropCells_AreaShape_Perimeter",
            "Mean_PropCells_AreaShape_Solidity",
            "Mean_PropCells_Children_PropCytoplasm_Count",
            "Mean_PropCells_Correlation_Correlation_CorrGreen_CorrBlue",
            "Mean_PropCells_Intensity_IntegratedIntensityEdge_CorrBlue",
            "Mean_PropCells_Intensity_IntegratedIntensityEdge_CorrGreen",
            "Mean_PropCells_Intensity_IntegratedIntensity_CorrBlue",
            "Mean_PropCells_Intensity_IntegratedIntensity_CorrGreen",
            "Mean_PropCells_Intensity_LowerQuartileIntensity_CorrBlue",
            "Count_Nuclei",
            "Mean_PropCells_Intensity_LowerQuartileIntensity_CorrGreen",
            "Mean_PropCells_Intensity_MassDisplacement_CorrBlue",
            "Mean_PropCells_Intensity_MassDisplacement_CorrGreen",
            "Mean_PropCells_Intensity_MaxIntensityEdge_CorrBlue",
            "Mean_PropCells_Intensity_MaxIntensityEdge_CorrGreen",
            "Mean_PropCells_Intensity_MaxIntensity_CorrBlue",
            "Mean_PropCells_Intensity_MaxIntensity_CorrGreen",
            "Mean_PropCells_Intensity_MeanIntensityEdge_CorrBlue",
            "Mean_PropCells_Intensity_MeanIntensityEdge_CorrGreen",
            "Mean_PropCells_Intensity_MeanIntensity_CorrBlue",
            "Count_PropCells",
            "Mean_PropCells_Intensity_MeanIntensity_CorrGreen",
            "Mean_PropCells_Intensity_MedianIntensity_CorrBlue",
            "Mean_PropCells_Intensity_MedianIntensity_CorrGreen",
            "Mean_PropCells_Intensity_MinIntensityEdge_CorrBlue",
            "Mean_PropCells_Intensity_MinIntensityEdge_CorrGreen",
            "Mean_PropCells_Intensity_MinIntensity_CorrBlue",
            "Mean_PropCells_Intensity_MinIntensity_CorrGreen",
            "Mean_PropCells_Intensity_StdIntensityEdge_CorrBlue",
            "Mean_PropCells_Intensity_StdIntensityEdge_CorrGreen",
            "Mean_PropCells_Intensity_StdIntensity_CorrBlue",
            "Count_PropCytoplasm",
            "Mean_PropCells_Intensity_StdIntensity_CorrGreen",
            "Mean_PropCells_Intensity_UpperQuartileIntensity_CorrBlue",
            "Mean_PropCells_Intensity_UpperQuartileIntensity_CorrGreen",
            "Mean_PropCells_Parent_Nuclei"
    );


    DataTable loadTable() {
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
    List<Plate> parseIntoPlates(DataTable input) {
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

        return HeatMapViewerNodeModel.parseIntoPlates(splitScreen, input.getDataTableSpec(),
                attributeModel, customPlateNameAttr,
                plateRowAttribute, plateColAttribute, imageAttributes,
                readouts,
                factors, new BarcodeParserFactory(patterns));
    }


    public static void main(String[] args) {
        PlateHeatMapViewerTest2 test = new PlateHeatMapViewerTest2();
        DataTable table = test.loadTable();
        List<Plate> plates = test.parseIntoPlates(table);

//        new ScreenViewer(plates);
//        new ScreenPanelFrame(plates);


        // Create screen view panel
        ScreenViewer view = new ScreenViewer(plates);

        // Set some reference populations.
        HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();
        referencePopulations.put("transfection",new String[]{"Mock", "Tox3", "Neg5"});
        view.getHeatMapModel().setReferencePopulations(referencePopulations);

        // Create a frame to carry it.
        JFrame frame = new JFrame("Whatever");
        frame.setSize(new Dimension(700,400));

        // Create the menu bar and populate it
        JMenuBar menu = new JMenuBar();
        menu.add(new HiLiteMenu(view));
        menu.add(new ViewMenu(view));
        menu.add(new TrellisMenu(view));
        frame.setJMenuBar(menu);

        frame.add(view);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setVisible(true);
    }

}
