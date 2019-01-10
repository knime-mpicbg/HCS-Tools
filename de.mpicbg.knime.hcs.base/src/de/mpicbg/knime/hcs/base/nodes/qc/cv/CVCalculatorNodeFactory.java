package de.mpicbg.knime.hcs.base.nodes.qc.cv;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Node Factory for CV-node
 * 
 * @author Antje Janosch
 *
 */
public class CVCalculatorNodeFactory extends NodeFactory<CVCalculatorNodeModel> {

	@Override
	public CVCalculatorNodeModel createNodeModel() {
		return new CVCalculatorNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<CVCalculatorNodeModel> createNodeView(int viewIndex, CVCalculatorNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CVCalculatorNodeDialog();
	}

}
