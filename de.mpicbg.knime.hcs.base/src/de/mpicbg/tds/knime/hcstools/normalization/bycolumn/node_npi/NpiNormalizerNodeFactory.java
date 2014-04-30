package de.mpicbg.tds.knime.hcstools.normalization.bycolumn.node_npi;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 11:41 AM
 */
public class NpiNormalizerNodeFactory extends NodeFactory<NpiNormalizerNodeModel> {
    @Override
    public NpiNormalizerNodeModel createNodeModel() {
        return new NpiNormalizerNodeModel();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<NpiNormalizerNodeModel> createNodeView(int i, NpiNormalizerNodeModel zScoreNormalizerNodeModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new NpiNormalizerNodeDialog();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
