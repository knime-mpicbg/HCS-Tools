package de.mpicbg.tds.knime.hcstools.preprocessing.outlierfilter; /**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 1/13/12
 * Time: 9:04 AM
 */

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OutlierFilterFactory extends NodeFactory<OutlierFilterModel> {
    //overwrites will be generated here

    @Override
    public OutlierFilterModel createNodeModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<OutlierFilterModel> createNodeView(int i, OutlierFilterModel outlierFilterModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
