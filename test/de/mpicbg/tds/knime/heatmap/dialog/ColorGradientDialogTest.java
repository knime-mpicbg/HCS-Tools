package de.mpicbg.tds.knime.heatmap.dialog;

import de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerTest;
import de.mpicbg.tds.knime.hcstools.visualization.PseudoHeatMapViewerNodeModel;
import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import org.knime.core.data.DataTable;

/**
 * @author Felix Meyenhofer
 *         creation: 3/15/13
 */

public class ColorGradientDialogTest {

    public static void main(String[] args) {
        DataTable table = PseudoHeatMapViewerNodeModel.loadTable(HeatMapViewerTest.testDataPath);
        HeatMapModel model = PseudoHeatMapViewerNodeModel.parseBufferedTable(table, HeatMapViewerTest.readouts, HeatMapViewerTest.factors);
        model.setCurrentReadout("Ch2 Median of Maximum");

        ColorGradientDialog dialog = new ColorGradientDialog(model);
        dialog.setVisible(true);
    }

}
