package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * node factory for Create Interval node
 * 
 * @author Antje Janosch
 *
 */
public class CreateIntervalNodeFactory extends NodeFactory<CreateIntervalNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CreateIntervalNodeModel createNodeModel() {
		return new CreateIntervalNodeModel();
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
	public NodeView<CreateIntervalNodeModel> createNodeView(int viewIndex, CreateIntervalNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CreateIntervalNodeDialog();
	}

}
