import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.HeatMapModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.HeatWell;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.WellViewer;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.knime.core.data.DataTable;

import javax.swing.*;
import java.util.List;

/**
 * @author Felix Meyenhofer
 *         creation: 1/2/13
 */

public class WellViewerTest {

    public static void main(String[] args) {

        PlateHeatMapViewerTest2 test = new PlateHeatMapViewerTest2();
        DataTable table = test.loadTable();
        List<Plate> plates = test.parseIntoPlates(table);
        Plate plate = plates.get(0);
        Well well = plate.getWell(1,1);

        WellViewer wellPane = new WellViewer(well);
        JDialog viewer = wellPane.createDialog();
//        viewer.pack();
        viewer.setVisible(true);

        WellViewer wellViewer = new WellViewer(new HeatWell(well, new HeatMapModel()), well);
        JDialog viewer2 = wellViewer.createDialog();
//        viewer2.pack();
        viewer2.setVisible(true);
    }

}
