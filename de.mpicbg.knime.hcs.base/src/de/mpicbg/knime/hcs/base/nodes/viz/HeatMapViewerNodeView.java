package de.mpicbg.knime.hcs.base.nodes.viz;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.ScreenViewer;
import de.mpicbg.knime.hcs.base.heatmap.menu.HiLiteMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.TrellisMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.ViewMenu;

import de.mpicbg.knime.hcs.core.model.PlateUtils;
import de.mpicbg.knime.hcs.core.model.Well;
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
    private HeatMapModel m_viewModel;

    /**
     * Constructor for the Node view.
     *
     * @param nodeModel {@link HeatMapViewerNodeModel}
     * @param viewModel holding view settings
     */
    public HeatMapViewerNodeView(HeatMapViewerNodeModel nodeModel, HeatMapModel viewModel) {
        super(nodeModel);
        this.nodeModel = nodeModel;

        // Do some data checks
        m_viewModel = viewModel;
/*        if (heatMapModel == null) {
            nodeModel.setPlotWarning("You need to re-execute the node before the view will show up");
            heatMapModel = new HeatMapModel();
        }*/

        if ((viewModel.getScreen() == null) || viewModel.getScreen().isEmpty()) {
            nodeModel.setPlotWarning("Could not create view for empty input table!");
        }

        // Propagate the views background color
        viewModel.setBackgroundColor(this.getComponent().getBackground());

        // Create the ScreenViewer and add the menus
        ScreenViewer screenView = new ScreenViewer(m_viewModel);
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

        // Save the view configuration:
        // why? the view configuration will not be saved if this is the only change in the workflow
        // then, the workflow is not flagged as modified and closes without saving internals
        if(nodeModel.getDataModel().isModified() && nodeModel.hasInternalValidConfigFiles()) {
        	
        	if(!nodeModel.serializePlateDataTest() || ! nodeModel.serializeViewConfigurationTest())
        		nodeModel.setPlotWarning("Failed saving data for view - See log file for more information");
        }
        
        //remove heatmap model in node model
        nodeModel.unregisterViewModel()

    }

    /** {@inheritDoc} */
    @Override
    protected void onOpen() {
        if (nodeModel.getDataModel().getScreen() == null) {
            throw new NullPointerException("There is no data to display");
        }
        
        //reset flag for view configuration changes
        nodeModel.getDataModel().resetModifiedFlag();

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
        
        HeatMapModel viewModel = nodeModel.getDataModel();

        if (nodeModel == null || viewModel == null) {
            viewer.getHeatTrellis().closePlateViewers();
            return;
        }
        
        // if the view is opened while 'reset' do not try to update any component
        if(viewModel.getScreen() == null)
        	return;

        viewer.setHeatMapModel(viewModel);
        viewModel.fireModelChanged();        
    }

}
