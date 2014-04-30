package de.mpicbg.knime.hcs.base.nodes.manip.col;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class BetterColumnCombinerFactory extends NodeFactory<BetterColumnCombiner> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BetterColumnCombiner createNodeModel() {
        return new BetterColumnCombiner();
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
    public NodeView<BetterColumnCombiner> createNodeView(final int viewIndex,
                                                         final BetterColumnCombiner nodeModel) {
        throw new IllegalStateException("No view available");
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
        return new ColCombineNodeDialog();
    }

}
