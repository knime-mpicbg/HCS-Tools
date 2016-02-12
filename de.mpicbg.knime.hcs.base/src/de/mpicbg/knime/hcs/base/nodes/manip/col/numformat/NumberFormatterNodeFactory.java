package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "NumberFormatter" Node.
 * 
 *
 * @author 
 */
public class NumberFormatterNodeFactory 
        extends NodeFactory<NumberFormatterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public NumberFormatterNodeModel createNodeModel() {
        return new NumberFormatterNodeModel();
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
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new NumberFormatterNodeDialog();
    }

	@Override
	public NodeView<NumberFormatterNodeModel> createNodeView(final int viewIndex,
			final NumberFormatterNodeModel nodeModel) {
		// TODO Auto-generated method stub
		return null;
	}

}

