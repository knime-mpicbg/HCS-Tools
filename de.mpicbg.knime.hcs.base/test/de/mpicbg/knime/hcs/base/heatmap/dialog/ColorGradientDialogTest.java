package de.mpicbg.knime.hcs.base.heatmap.dialog;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.dialog.ColorGradientDialog;
import de.mpicbg.knime.hcs.base.nodes.viz.HeatMapViewerTest;
import de.mpicbg.knime.hcs.base.nodes.viz.PseudoHeatMapViewerNodeModel;

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
