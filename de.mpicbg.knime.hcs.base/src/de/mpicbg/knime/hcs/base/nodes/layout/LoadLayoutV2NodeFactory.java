package de.mpicbg.knime.hcs.base.nodes.layout;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "LoadLayoutV2" Node.
 *
 * @author
 */
public class LoadLayoutV2NodeFactory
        extends NodeFactory<LoadLayoutV2NodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LoadLayoutV2NodeModel createNodeModel() {
        return new LoadLayoutV2NodeModel();
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
    public NodeView<LoadLayoutV2NodeModel> createNodeView(final int viewIndex,
                                                          final LoadLayoutV2NodeModel nodeModel) {
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
        return new LoadLayoutV2NodeDialog();
    }

}

