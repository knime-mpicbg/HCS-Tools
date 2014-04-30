package de.mpicbg.knime.hcs.base.nodes.layout;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "JoinLayoutV2" Node.
 *
 * @author MPI-CBG
 */
public class JoinLayoutV2NodeFactory
        extends NodeFactory<JoinLayoutV2NodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JoinLayoutV2NodeModel createNodeModel() {
        return new JoinLayoutV2NodeModel();
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
    public NodeView<JoinLayoutV2NodeModel> createNodeView(final int viewIndex,
                                                          final JoinLayoutV2NodeModel nodeModel) {
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
        return new JoinLayoutV2NodeDialog();
    }

}

