package de.mpicbg.tds.knime.hcstools.populationanalysis;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BinningAnalysis" Node.
 * will be done later
 *
 * @author MPI-CBG
 */
public class BinningAnalysisNodeFactory
        extends NodeFactory<BinningAnalysisNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BinningAnalysisNodeModel createNodeModel() {
        return new BinningAnalysisNodeModel();
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
    public NodeView<BinningAnalysisNodeModel> createNodeView(final int viewIndex,
                                                             final BinningAnalysisNodeModel nodeModel) {
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
        return new BinningAnalysisNodeDialog();
    }

}

