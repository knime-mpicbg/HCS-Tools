package de.mpicbg.knime.hcs.base.nodes.viz;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.menu.HiLiteMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.TrellisMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.ViewMenu;

import org.knime.core.data.DataTable;

//import de.mpicbg.tds.core.TdsUtils;
//import de.mpicbg.tds.core.model.Plate;
//import de.mpicbg.tds.core.model.Well;

import javax.swing.*;
//import static de.mpicbg.tds.core.TdsUtils.SCREEN_MODEL_TREATMENT;
//import static de.mpicbg.tds.core.model.Plate.configurePlateByBarcode;
//import static de.mpicbg.tds.core.model.Plate.inferPlateDimFromWells;

/**
 * User: Felix Meyenhofer
 * Date: 11/27/12
 * Time: 14:41
 */

public class HeatMapViewerTest {

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


    public static void main(String[] args) {
        DataTable table = PseudoHeatMapViewerNodeModel.loadTable(testDataPath);
        HeatMapModel model = PseudoHeatMapViewerNodeModel.parseBufferedTable(table, readouts, factors);

        // Create screen view panel
        ScreenViewer view = new ScreenViewer(model);

        // Create a frame to carry it.
        JFrame frame = new JFrame("Whatever");

        // Create the menu bar and populate it
        JMenuBar menu = new JMenuBar();
        menu.add(new HiLiteMenu(view));
        menu.add(new ViewMenu(view));
        menu.add(new TrellisMenu(view));
        frame.setJMenuBar(menu);

        frame.setContentPane(view);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                ScreenViewer viewer = (ScreenViewer)((JFrame) windowEvent.getComponent()).getContentPane();
                viewer.getHeatTrellis().closePlateViewers();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

}
