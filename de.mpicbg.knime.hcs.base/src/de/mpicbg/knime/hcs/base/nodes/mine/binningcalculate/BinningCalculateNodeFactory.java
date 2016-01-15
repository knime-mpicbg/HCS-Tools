package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BinningCalculate" Node.
 * 
 *
 * @author 
 */
public class BinningCalculateNodeFactory 
        extends NodeFactory<BinningCalculateNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BinningCalculateNodeModel createNodeModel() {
        return new BinningCalculateNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<BinningCalculateNodeModel> createNodeView(final int viewIndex,
            final BinningCalculateNodeModel nodeModel) {
        return new BinningCalculateNodeView(nodeModel);
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
        return new BinningCalculateNodeDialog();
    }

}