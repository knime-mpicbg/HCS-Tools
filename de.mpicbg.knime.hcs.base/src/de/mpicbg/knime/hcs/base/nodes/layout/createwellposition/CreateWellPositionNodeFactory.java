package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CreateWellPosition" Node.
 * 
 *
 * @author 
 */
public class CreateWellPositionNodeFactory 
        extends NodeFactory<CreateWellPositionNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateWellPositionNodeModel createNodeModel() {
        return new CreateWellPositionNodeModel();
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
    public NodeView<CreateWellPositionNodeModel> createNodeView(final int viewIndex,
            final CreateWellPositionNodeModel nodeModel) {
        return null;
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
        return new CreateWellPositionNodeDialog();
    }

}

