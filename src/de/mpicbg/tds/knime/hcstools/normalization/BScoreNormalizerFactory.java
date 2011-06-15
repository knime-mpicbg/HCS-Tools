package de.mpicbg.tds.knime.hcstools.normalization;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * <code>NodeFactory</code> for the "POCNormalizer" Node. Some nodes to ease the handling and mining of HCS-data.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class BScoreNormalizerFactory
        extends NodeFactory<BScoreNormalizer> {


    @Override
    public BScoreNormalizer createNodeModel() {
        return new BScoreNormalizer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<BScoreNormalizer> createNodeView(final int viewIndex,
                                                     final BScoreNormalizer nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractScreenTrafoDialog() {

//            public DialogComponentStringSelection treatmentControl;
//
//
//            @Override
//            public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
//                super.loadAdditionalSettingsFrom(settings, specs);
//
//                AttributeUtils.updateTreatmentControl(specs[0], treatmentControl);
//            }
//
//
//            @Override
//            protected void createControls() {
//                treatmentControl = new DialogComponentStringSelection(AbstractScreenTrafoModel.createTreatmentSelector(AbstractScreenTrafoModel.TREATMENT_CONTROL),
//                        AbstractScreenTrafoModel.TREATMENT_CONTROL, Arrays.asList(AbstractScreenTrafoModel.TREATMENT_LIBRARY), true);
//                addDialogComponent(treatmentControl);
//
//                String[] methods = {METHOD_ROBUST_ZSCORE,METHOD_ZSCORE, METHOD_BSCORE};
//                addDialogComponent(new DialogComponentStringSelection(createMethodProperty(), "Normalization method", methods));
//
//            }

        };
    }


}