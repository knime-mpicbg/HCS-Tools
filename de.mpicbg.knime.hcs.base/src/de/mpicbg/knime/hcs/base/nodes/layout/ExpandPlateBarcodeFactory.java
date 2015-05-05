package de.mpicbg.knime.hcs.base.nodes.layout;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandPlateBarcodeFactory extends NodeFactory<ExpandPlateBarcodeModel> {

	/**
	 * {@inheritDoc}
	 */
    @Override
    public ExpandPlateBarcodeModel createNodeModel() {
        return new ExpandPlateBarcodeModel();
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
    public NodeView<ExpandPlateBarcodeModel> createNodeView(final int viewIndex, final ExpandPlateBarcodeModel nodeModel) {
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
    	
    	return new ExpandPlateBarcodeDialog();
    }
}