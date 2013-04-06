package de.mpicbg.tds.knime.hcstools.visualization;

import org.knime.core.node.*;

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
        return new HeatMapViewerNodeView(nodeModel);
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
