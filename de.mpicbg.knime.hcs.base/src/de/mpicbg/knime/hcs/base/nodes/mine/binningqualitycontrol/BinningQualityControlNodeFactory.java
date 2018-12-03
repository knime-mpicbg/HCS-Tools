package de.mpicbg.knime.hcs.base.nodes.mine.binningqualitycontrol;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BinningQualityControl" Node.
 * 
 *
 * @author Tim Nicolaisen
 */
public class BinningQualityControlNodeFactory 
        extends NodeFactory<BinningQualityControlNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BinningQualityControlNodeModel createNodeModel() {
        return new BinningQualityControlNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<BinningQualityControlNodeModel> createNodeView(final int viewIndex,
            final BinningQualityControlNodeModel nodeModel) {
        return new BinningQualityControlNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new BinningQualityControlNodeDialog();
    }

}

