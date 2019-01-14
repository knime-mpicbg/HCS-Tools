package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class CreateIntervalNodeFactory extends NodeFactory<CreateIntervalNodeModel> {

	@Override
	public CreateIntervalNodeModel createNodeModel() {
		return new CreateIntervalNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<CreateIntervalNodeModel> createNodeView(int viewIndex, CreateIntervalNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CreateIntervalNodeDialog();
	}

}
