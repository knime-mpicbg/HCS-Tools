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

        WellViewer wellViewer = new WellViewer(well);
        JFrame viewer = wellViewer.createViewerWindow();
        viewer.pack();

        viewer.setVisible(true);

    }

}
