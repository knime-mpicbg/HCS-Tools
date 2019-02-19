package de.mpicbg.knime.hcs.base.nodes.layout.expandwellposition;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * node factory for Expand Well Position (V2)
 * 
 * @author Antje Janosch
 *
 */
public class ExpandWellPositionV2NodeFactory extends NodeFactory<ExpandWellPositionV2NodeModel> {

	@Override
	public ExpandWellPositionV2NodeModel createNodeModel() {
		return new ExpandWellPositionV2NodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<ExpandWellPositionV2NodeModel> createNodeView(int viewIndex,
			ExpandWellPositionV2NodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new ExpandWellPositionV2NodeDialog();
	}

}
