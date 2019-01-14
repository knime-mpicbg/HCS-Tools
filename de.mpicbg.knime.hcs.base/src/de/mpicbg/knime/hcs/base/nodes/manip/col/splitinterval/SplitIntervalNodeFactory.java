package de.mpicbg.knime.hcs.base.nodes.manip.col.splitinterval;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * node factory for node Split Intervall
 * 
 * @author Antje Janosch
 *
 */
public class SplitIntervalNodeFactory extends NodeFactory<SplitIntervalNodeModel> {

	@Override
	public SplitIntervalNodeModel createNodeModel() {
		return new SplitIntervalNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SplitIntervalNodeModel> createNodeView(int viewIndex, SplitIntervalNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new SplitIntervalNodeDialog();
	}

}
