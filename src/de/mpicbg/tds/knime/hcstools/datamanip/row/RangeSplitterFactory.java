package de.mpicbg.tds.knime.hcstools.datamanip.row;
/**
 * Factory of Range Splitter node
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/12/11
 * Time: 1:30 PM
 */

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class RangeSplitterFactory extends NodeFactory<RangeSplitterModel> {
    //overwrites will be generated here

    @Override
    public RangeSplitterModel createNodeModel() {
        return new RangeSplitterModel();
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<RangeSplitterModel> createNodeView(int i, RangeSplitterModel rangeSplitterModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new RangeSplitterNodeDialog();
    }
}
