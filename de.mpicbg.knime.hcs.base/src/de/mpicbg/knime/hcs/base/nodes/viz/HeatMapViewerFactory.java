package de.mpicbg.knime.hcs.base.nodes.viz;

import java.util.UUID;

import org.knime.core.node.*;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;

/**
 * Creating the HCS Heat Map Viewer node.
 *
 * @author Holger Brandl (MPI-CBG)
 */

public class HeatMapViewerFactory extends NodeFactory<HeatMapViewerNodeModel> {

    /** {@inheritDoc} */
    @Override
    public HeatMapViewerNodeModel createNodeModel() {
        return new HeatMapViewerNodeModel();
    }

    /** {@inheritDoc} */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public NodeView<HeatMapViewerNodeModel> createNodeView(final int viewIndex, final HeatMapViewerNodeModel nodeModel) {
    	// register HeatMapModel of the view
    	HeatMapModel viewModel = new HeatMapModel(UUID.randomUUID());
    	nodeModel.registerViewModel(viewModel);
        return new HeatMapViewerNodeView(nodeModel, viewModel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new HeatMapViewerNodeDialog();
    }

}
