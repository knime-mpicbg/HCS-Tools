package de.mpicbg.knime.hcs.base.nodes.norm.zscore;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 11:41 AM
 */
public class ZScoreNormalizerNodeFactory extends NodeFactory<ZScoreNormalizerNodeModel> {
    @Override
    public ZScoreNormalizerNodeModel createNodeModel() {
        return new ZScoreNormalizerNodeModel();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<ZScoreNormalizerNodeModel> createNodeView(int i, ZScoreNormalizerNodeModel zScoreNormalizerNodeModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new ZScoreNormalizerNodeDialog();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
