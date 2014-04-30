package de.mpicbg.tds.knime.hcstools.normalization;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.ROBUST_STATS_PROPERTY_DESCS;
import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropRobustStats;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ZScoreNormalizerFactory
        extends NodeFactory<ZScoreNormalizer> {


    @Override
    public ZScoreNormalizer createNodeModel() {
        return new ZScoreNormalizer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<ZScoreNormalizer> createNodeView(final int viewIndex,
                                                     final ZScoreNormalizer nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractScreenTrafoDialog() {

            public DialogComponentStringSelection treatmentSelector;


            @Override
            protected void createControls() {
                super.createControls();

                treatmentSelector = setupTreatmentSelector(this);

                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
//                addDialogComponent(new DialogComponentBoolean(createPropTotalSetNorm(), "Total set normalization"));
            }
        };
    }


//    public static SettingsModelBoolean createPropTotalSetNorm() {
//           return new SettingsModelBoolean("dp.totalset.normalization", false);
//       }

}