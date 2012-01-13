package de.mpicbg.tds.knime.hcstools.normalization.zscore; /**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/22/11
 * Time: 8:36 AM
 */

import de.mpicbg.tds.knime.hcstools.normalization.AbstractNormalizerDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class ZScoreNormalizerV2Factory extends NodeFactory<ZScoreNormalizerV2Model> {
    //overwrites will be generated here

    @Override
    public ZScoreNormalizerV2Model createNodeModel() {
        return new ZScoreNormalizerV2Model();
    }

    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    @Override
    public NodeView<ZScoreNormalizerV2Model> createNodeView(int i, ZScoreNormalizerV2Model zScoreNormalizerModel) {
        return null;
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new AbstractNormalizerDialog() {
            @Override
            protected void createControls() {
                //createGroupByComboBox(ZScoreNormalizerV2Model.);
            }
        };  //To change body of implemented methods use File | Settings | File Templates.
    }
}
