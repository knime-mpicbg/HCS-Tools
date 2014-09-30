package de.mpicbg.knime.hcs.base.nodes.trans;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 7/27/11
 * Time: 11:09 AM
 */
public class BoxCoxTransformFactory extends NodeFactory<BoxCoxTransform> {
    @Override
    public BoxCoxTransform createNodeModel() {
        return new BoxCoxTransform();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<BoxCoxTransform> createNodeView(int i, BoxCoxTransform boxCoxTransform) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new BoxCoxNodeDialog();
    }
}
