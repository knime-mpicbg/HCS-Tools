package de.mpicbg.knime.hcs.base.nodes.img;

import de.mpicbg.knime.knutils.AbstractNodeView;

import java.awt.*;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
class JoinedWithImagesView<Model extends AbstractJoinWithImagesModel> extends AbstractNodeView<Model> {

    public JoinedWithImagesView(Model nodeModel) {
        super(nodeModel);
    }


    @Override
    protected Component createViewComponent() {


//                if (nodeModel.getPlates() == null) {
//                    nodeModel.setPlotWarning();
//                    return null;
//                }
//
//                return createNewExplorer(nodeModel);

        // todo implement me
        return null;
    }


    @Override
    protected void modelChanged() {
//                if (getNodeModel() == null || getNodeModel().getPlates() == null) {
//                    return;
//                }
//
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        setComponent(createNewExplorer(nodeModel));
//                    }
//                });
    }
}
