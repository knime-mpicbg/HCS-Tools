package de.mpicbg.knime.hcs.base.nodes.img;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public abstract class AbstractJoinWithImagesFactory<JoinModel extends AbstractJoinWithImagesModel> extends NodeFactory<JoinModel> {


    @Override
    public int getNrNodeViews() {
        return 1;
    }


    @Override
    public NodeView<JoinModel> createNodeView(int viewIndex, JoinModel nodeModel) {
        return new JoinedWithImagesView<JoinModel>(nodeModel);
    }


//    private Component createNewExplorer(ScreenExplorer nodeModel) {
////        if (nodeModel.doConnectToCompoundDB() && compoundService == null) {
////            compoundService = new AnnotationConfigApplicationContext(CompoundDBConfig.class).getBean(ChemicalService.class);
////        }
//
//        return new ScreenPanel(null, null, nodeModel.getPlates());
//    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {

        return new AbstractJoinWithImagesDialog();
    }


}
