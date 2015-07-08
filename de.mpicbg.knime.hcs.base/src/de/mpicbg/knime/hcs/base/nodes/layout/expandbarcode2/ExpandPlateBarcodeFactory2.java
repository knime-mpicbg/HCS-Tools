package de.mpicbg.knime.hcs.base.nodes.layout.expandbarcode2;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandPlateBarcodeFactory2 extends NodeFactory<ExpandPlateBarcodeModel2> {

	/**
	 * {@inheritDoc}
	 */
    @Override
    public ExpandPlateBarcodeModel2 createNodeModel() {
        return new ExpandPlateBarcodeModel2();
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
    public NodeView<ExpandPlateBarcodeModel2> createNodeView(final int viewIndex, final ExpandPlateBarcodeModel2 nodeModel) {
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
    	
    	return new ExpandPlateBarcodeDialog2();
    }
}