package de.mpicbg.knime.hcs.base.echofilereader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "EchoFileReader" Node.
 * 
 *
 * @author 
 */
public class EchoFileReaderNodeFactory 
        extends NodeFactory<EchoFileReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public EchoFileReaderNodeModel createNodeModel() {
        return new EchoFileReaderNodeModel();
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
    public NodeView<EchoFileReaderNodeModel> createNodeView(final int viewIndex,
            final EchoFileReaderNodeModel nodeModel) {
        return new EchoFileReaderNodeView(nodeModel);
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
        return new EchoFileReaderNodeDialog();
    }

}

