package de.mpicbg.knime.hcs.base.nodes.manip.row;

/**
 * Factory of RangeFilter node
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/1/11
 * Time: 2:19 PM
 */

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class RangeFilterV2Factory extends NodeFactory<RangeFilterV2NodeModel> {
    //overwrites will be generated here

    @Override
    public RangeFilterV2NodeModel createNodeModel() {
        return new RangeFilterV2NodeModel(1);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<RangeFilterV2NodeModel> createNodeView(int i, RangeFilterV2NodeModel rangeFilterModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new RangeFilterV2NodeDialog();
    }


}
