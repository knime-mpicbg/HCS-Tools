package de.mpicbg.tds.knime.hcstools.utils.cellsplitmerge;

import org.knime.base.node.preproc.cellsplit.CellSplitterNodeDialogPane;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class BetterCellSplitterFactory extends NodeFactory<BetterCellSplitter> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new CellSplitterNodeDialogPane();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BetterCellSplitter createNodeModel() {
        return new BetterCellSplitter();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<BetterCellSplitter> createNodeView(final int viewIndex,
                                                       final BetterCellSplitter nodeModel) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

}
