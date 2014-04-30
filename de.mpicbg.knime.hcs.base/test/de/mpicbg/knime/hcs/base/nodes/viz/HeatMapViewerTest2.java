package de.mpicbg.knime.hcs.base.nodes.viz;

import java.awt.*;
import java.util.*;
import java.util.List;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.menu.HiLiteMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.TrellisMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.ViewMenu;

import org.knime.core.data.DataTable;
import javax.swing.*;

/**
 *
 * Reads a data set produced by the table writer node and passes it to the View.
 *
 * @author Felix Meyenhofer
 *         creation: 1/1/13
 */

public class HeatMapViewerTest2 {

    // Information that usually would be provided from the KNIME configuration dialogs.
    static String testDataPath = "/Users/turf/Sources/CBG/HCS-Tools/test/data/96well_color_image.table";      //TODO put the testdata on a dropbox to not weigh down the package.

    static  List<String> factors = Arrays.asList(
            "barcode",
            "BS_Controls",
            "SBS_Doses"
    );
    static List<String> readouts = Arrays.asList(
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
            "Mean_Nuclei_Math_Ratio1",
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


    public static void main(String[] args) {

        DataTable table = PseudoHeatMapViewerNodeModel.loadTable(testDataPath);
        HeatMapModel model = PseudoHeatMapViewerNodeModel.parseBufferedTable(table, readouts, factors);//test.parseBufferedTable(table);

        // Create screen view panel
        ScreenViewer view = new ScreenViewer(model);

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
