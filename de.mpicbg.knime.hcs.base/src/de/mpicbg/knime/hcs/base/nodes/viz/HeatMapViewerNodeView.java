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

    /** View model */
    private HeatMapModel m_viewModel;
    
    //private boolean m_dirtyFlag = false;

    /**
     * Constructor for the Node view.
     *
     * @param nodeModel {@link HeatMapViewerNodeModel}
     * @param viewModel holding view settings and the node data
     */
    public HeatMapViewerNodeView(HeatMapViewerNodeModel nodeModel, HeatMapModel viewModel) {
        super(nodeModel);

        m_viewModel = viewModel;

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

        HeatMapViewerNodeModel nodeModel = getNodeModel();
        
        // Remove the HiLiteListeners.
        nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.IN_PORT).removeAllHiLiteListeners();

        // Remove the HiLiteHandler
        screenView.getHeatMapModel().setHiLiteHandler(null);

        // Save the view configuration:
        // why? the view configuration will not be saved if this is the only change in the workflow
        // then, the workflow is not flagged as modified and closes without saving internals
        if(m_viewModel.isModified() && nodeModel.hasViewFileHandle()) {
        	
        	if(!nodeModel.serializeViewConfiguration(m_viewModel))
        		nodeModel.setPlotWarning("Failed saving view configuration - See log file for more information");
        }
        
        //push view model into node model for image output
        nodeModel.keepViewModel(m_viewModel);
        
        //remove heatmap model in node model
        nodeModel.unregisterViewModel(m_viewModel.getModelID());

    }

/*    *//** {@inheritDoc} *//*
    @Override
	protected void updateModel(Object arg) {
		// TODO Auto-generated method stub
		super.updateModel(arg);
		m_dirtyFlag = true;
	}*/


	/** {@inheritDoc} */
    @Override
    protected void onOpen() {
        if (m_viewModel.getScreen() == null) {
            throw new NullPointerException("There is no data to display");
        }
        
        //reset flag for view configuration changes
        m_viewModel.resetModifiedFlag();

        ScreenViewer screenView = (ScreenViewer)getComponent();
        
        HeatMapViewerNodeModel nodeModel = getNodeModel();

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
        
        // update view model, data model might be null after reset node
        HeatMapModel dataModel = nodeModel.getDataModel();
        if(dataModel != null)
        	m_viewModel.setNodeConfigurations(dataModel);
        else
        	return;
                
        m_viewModel.validateViewSettings();

        m_viewModel.fireModelChanged();        
    }

}
