package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.*;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Node view of the HCS Heatmap Viewer.
 *
 * @author Felix Meyenhofer
 * creation: 12/27/12
 */

public class HeatMapViewerNodeView extends NodeView<HeatMapViewerNodeModel> {

    /** Store the node model for the HiLite registering */
    private final HeatMapViewerNodeModel nodeModel;


    /**
     * Constructor for the Node view.
     *
     * @param nodeModel {@link HeatMapViewerNodeModel}
     */
    public HeatMapViewerNodeView(HeatMapViewerNodeModel nodeModel) {
        super(nodeModel);
        this.nodeModel = nodeModel;

        if (nodeModel.getPlates() == null) {
            nodeModel.setPlotWarning("You need to re-execute the node before the view will show up");

        } else {

            if (nodeModel.getPlates().isEmpty()) {
                nodeModel.setPlotWarning("Could not create view for empty input table!");

            } else {
                // Create the ScreenViewer and add the menus
                ScreenViewer screenView = new ScreenViewer(nodeModel);
                this.setComponent(screenView);
                JMenuBar menu = this.getJMenuBar();
                menu.add(new HiLiteMenu(screenView));
                menu.add(new ViewMenu(screenView));
                menu.add(new TrellisMenu(screenView));
                this.getComponent().setPreferredSize(new Dimension(810, 600));

                // Remove the "Always on Top" menu item of the default File menu
                menu.getMenu(0).remove(0);

                // Register the HiLiteHandler
                HeatMapModel heatMapModel = screenView.getHeatMapModel();
                HiLiteHandler hiLiteHandler = nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.IN_PORT);
                hiLiteHandler.addHiLiteListener(heatMapModel);

                // ... and initialize the HiLites.
                Set<RowKey> hiLites = hiLiteHandler.getHiLitKeys();
                for (Well well : PlateUtils.flattenWells(heatMapModel.getScreen())) {
                    if ( hiLites.contains(well.getKnimeTableRowKey()) )
                        heatMapModel.addHilLites(well);
                }
                heatMapModel.fireModelChanged();
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        // Close the PlateViewer windows.
        ((ScreenViewer)getComponent()).getHeatTrellis().closePlateViewers();
        // Remove the HiLiteListeners.
        nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.IN_PORT).removeAllHiLiteListeners();
    }

    /** {@inheritDoc} */
    @Override
    protected void onOpen() {
        // blöterle
    }

    /** {@inheritDoc} */
    @Override
    protected void modelChanged() {
        HeatMapViewerNodeModel nodeModel = getNodeModel();

        if (nodeModel == null || nodeModel.getPlates() == null)
            return;

        HeatMapModel heatMapModel = ((ScreenViewer)getComponent()).getHeatMapModel();
        heatMapModel.setScreen(nodeModel.getPlates());
        heatMapModel.fireModelChanged();
    }

}
