package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class BinningApplyNodeFactory extends NodeFactory<BinningApplyNodeModel> {

	@Override
	public BinningApplyNodeModel createNodeModel() {
		return new BinningApplyNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<BinningApplyNodeModel> createNodeView(int viewIndex, BinningApplyNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new BinningApplyNodeDialog();
	}

}
