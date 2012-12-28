package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.*;

import org.knime.core.node.NodeView;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Node view of the HCS Heatmap Viewer.
 *
 * @author Felix Meyenhofer
 * creation: 12/27/12
 */

public class HeatMapViewerNodeView extends NodeView<HeatMapViewerNodeModel> {

    public HeatMapViewerNodeView(HeatMapViewerNodeModel nodeModel) {
        super(nodeModel);

        if (nodeModel.getPlates() == null) {
            nodeModel.setPlotWarning("You need to re-execute the node before the view will show up");

        } else {

            if (nodeModel.getPlates().isEmpty()) {
                nodeModel.setPlotWarning("Could not create view for empty input table!");

            } else {
                ScreenViewer screenView = new ScreenViewer(nodeModel);
                this.setComponent(screenView);
                JMenuBar menu = this.getJMenuBar();
                menu.add(new HiLiteMenu(screenView));
                menu.add(new ViewMenu(screenView));
                menu.add(new TrellisMenu(screenView));
                this.getComponent().setSize(new Dimension(810,600));
            }
        }
    }


    @Override
    protected void onClose() {
        ((ScreenViewer)getComponent()).getHeatTrellis().closePlateViewers();

        //TODO: un-register the hilitehandler
    }

    @Override
    protected void onOpen() {
        //blöterle
    }

    @Override
    protected void modelChanged() {
        HeatMapViewerNodeModel nodeModel = getNodeModel();

        if (nodeModel == null || nodeModel.getPlates() == null)
            return;

        HeatMapModel2 heatMapModel = ((ScreenViewer)getComponent()).getHeatMapModel();
        heatMapModel.setScreen(nodeModel.getPlates());
        heatMapModel.fireModelChanged();

        //TODO: register the hilitehandler
    }

}
