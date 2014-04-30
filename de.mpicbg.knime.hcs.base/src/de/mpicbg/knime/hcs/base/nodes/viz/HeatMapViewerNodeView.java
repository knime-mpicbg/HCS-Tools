package de.mpicbg.knime.hcs.base.nodes.viz;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.menu.HiLiteMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.TrellisMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.ViewMenu;

import de.mpicbg.tds.core.model.PlateUtils;
import de.mpicbg.tds.core.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * Node view of the HCS Heatmap Viewer.
 *
 * @author Felix Meyenhofer
 * creation: 12/27/12
 *
 * TODO: The HiLite has some random behaviour. Clearing the HiLites is not in sync between views of different nodes when using the HeatMapViewer+ImageFileLinker workflow.
 * TODO: The view saves internals when closing. Since this is not instantanious, there should be a dialog to inform the user and giving him the option to cancel the process.
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

        // Do some data checks
        HeatMapModel heatMapModel = nodeModel.getDataModel();
        if (heatMapModel == null) {
            nodeModel.setPlotWarning("You need to re-execute the node before the view will show up");
            heatMapModel = new HeatMapModel();
        }

        if ((heatMapModel.getScreen() == null) || heatMapModel.getScreen().isEmpty()) {
            nodeModel.setPlotWarning("Could not create view for empty input table!");
        }

        // Propagate the views background color
        heatMapModel.setBackgroundColor(this.getComponent().getBackground());

        // Create the ScreenViewer and add the menus
        ScreenViewer screenView = new ScreenViewer(heatMapModel);
        this.setComponent(screenView);
        JMenuBar menu = this.getJMenuBar();
        menu.add(new HiLiteMenu(screenView));
        menu.add(new ViewMenu(screenView));
        menu.add(new TrellisMenu(screenView));
        this.getComponent().setPreferredSize(new Dimension(810, 600));

        // Remove the "Always on Top" menu item of the default File menu
        menu.getMenu(0).remove(0);
    }


    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        ScreenViewer screenView = (ScreenViewer)getComponent();

        // Close the PlateViewer windows.
        screenView.getHeatTrellis().closePlateViewers();

        // Remove the HiLiteListeners.
        nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.IN_PORT).removeAllHiLiteListeners();

        // Remove the HiLiteHandler
        screenView.getHeatMapModel().setHiLiteHandler(null);

        // Save the view configuration
        // TODO: It would be nicer if the workflows dirty-flag could be set (workflow modified), this way the view configuration would only be saved once, not each time the view is closed (because in that case the heatMapModel data is stored in memory)
        try {
            getLogger().warn("Saving view configuration. This might take a moment.");
            nodeModel.serializeViewConfiguration();
        } catch (IOException e) {
            nodeModel.setPlotWarning("Saving the view configuration failed.");
            nodeModel.setPlotWarning(e.toString());
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void onOpen() {
        if (nodeModel.getDataModel().getScreen() == null) {
            throw new NullPointerException("There is no data to display");
        }

        ScreenViewer screenView = (ScreenViewer)getComponent();

        // Set the HiLiteHandler
        screenView.getHeatMapModel().setHiLiteHandler(nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.IN_PORT));

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

        // Set the focus on the menu of the screen viewer so the keyboard shortcuts take effect immediately (without first manipulating the view)
        screenView.setRequestFocusEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void modelChanged() {
        HeatMapViewerNodeModel nodeModel = getNodeModel();

        ScreenViewer viewer;
        try {
            viewer = (ScreenViewer)getComponent();
        } catch (ClassCastException e) {
            getLogger().warn("The node was not executed/saved properly. Please re-execute!");
            return;
        }

        if (nodeModel == null || nodeModel.getDataModel() == null) {
            viewer.getHeatTrellis().closePlateViewers();
            return;
        }

        viewer.setHeatMapModel(nodeModel.getDataModel());
    }

}
