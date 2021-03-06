package de.mpicbg.knime.hcs.base.nodes.viz;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.WellViewer;
import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatWell;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import org.knime.core.data.DataTable;

import javax.swing.*;
import java.util.List;

/**
 * @author Felix Meyenhofer
 *         creation: 1/2/13
 */

public class WellViewerTest {
    private static String testDataPath = "/Users/turf/Sources/CBG/HCS-Tools/test/data/96well_color_image.table";      //TODO put the testdata on a dropbox to not weigh down the package.

    public static void main(String[] args) {

//        de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerTest2 test = new de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerTest2();
        DataTable table = PseudoHeatMapViewerNodeModel.loadTable(HeatMapViewerTest2.testDataPath);
        HeatMapModel model = PseudoHeatMapViewerNodeModel.parseBufferedTable(table, HeatMapViewerTest2.readouts, HeatMapViewerTest2.factors);
        List<Plate> plates = model.getScreen();
        Plate plate = plates.get(0);
        Well well = plate.getWell(1,1);

        WellViewer wellPane = new WellViewer(well);
        JDialog viewer = wellPane.createDialog();
//        viewer.pack();
        viewer.setVisible(true);

        WellViewer wellViewer = new WellViewer(new HeatWell(well, new HeatMapModel(), viewer.getBufferStrategy().getDrawGraphics()), well);
        JDialog viewer2 = wellViewer.createDialog();
//        viewer2.pack();
        viewer2.setVisible(true);
    }

}
