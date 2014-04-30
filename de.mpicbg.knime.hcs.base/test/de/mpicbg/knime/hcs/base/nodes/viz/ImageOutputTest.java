package de.mpicbg.knime.hcs.base.nodes.viz;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.io.ScreenImage;
import de.mpicbg.knime.hcs.base.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatTrellis;

import org.knime.core.data.DataTable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * User: Felix Meyenhofer
 * Date: 11/27/12
 * Time: 14:41
 */

public class ImageOutputTest {

    // Information that usually would be provided from the KNIME configuration dialogs.
    public static String testDataPath = "/Users/turf/Sources/CBG/HCS-Tools/test/data/plateviewerinput.table";      //TODO put the testdata on a dropbox to not weigh down the package.

    public static List<String> factors = Arrays.asList(
            "date",
            "transfection",
            "concentration",
            "left_right",
            "top_bottom",
            "inner_outer"
    );
    public static List<String> readouts = Arrays.asList(
//            "Nuclei Intensity",
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


    public static void main(String[] args) throws AWTException, IOException {
        DataTable table = PseudoHeatMapViewerNodeModel.loadTable(testDataPath);
        HeatMapModel model = PseudoHeatMapViewerNodeModel.parseBufferedTable(table, readouts, factors);

        // Set the row column configuration
        Integer columns = 5;
        Integer rows =  (int) Math.ceil(model.getCurrentNumberOfPlates() * 1.0 / columns);
        model.setNumberOfTrellisColumns(columns);
        model.setNumberOfTrellisRows(rows);
        model.setAutomaticTrellisConfiguration(false);

        // Create the heatmap panel from screen (turn the buffers off)
        ScreenViewer viewer = new ScreenViewer(model);
        HeatTrellis trellis = viewer.getHeatTrellis();
        trellis.setTrellisHeatMapSize(420, 280);
        trellis.setPlateNameFontSize(28);

        trellis.setDoubleBuffered(false);
        trellis.updateContainerDimensions(rows, columns);
        trellis.repopulatePlateGrid();

//        for (HeatScreen map : trellis.getHeatMaps())
//            map.setDoubleBuffered(false);

        JPanel component = trellis.getHeatMapsContainer();
        component.setDoubleBuffered(false);

        // Create the colorbar
        HeatMapColorToolBar colorBar = new HeatMapColorToolBar(model);
        colorBar.setMinimumSize(new Dimension(500,37));
        colorBar.setPreferredSize(new Dimension(500,37));
        colorBar.setLabelFont(22);
//        colorBar.repaint();
//        JPanel colorBar = viewer.getColorBar().getColormap();
//        colorBar.setDoubleBuffered(false);
//        colorBar.repaint();
//        JFrame frame = new JFrame();
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add(colorBar);
//        frame.pack();

        // Add the colorbar
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        panel.add(colorBar, BorderLayout.PAGE_END);
        panel.setMinimumSize(new Dimension(300, 20));

        // Create the image.
        BufferedImage image = ScreenImage.createImage(panel);
        ScreenImage.writeImage(image, "/Users/turf/Desktop/test_heatmapviewer_2.png");
    }

}
