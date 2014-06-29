package de.mpicbg.knime.hcs.base.nodes.norm;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.ROBUST_STATS_PROPERTY_DESCS;
import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.createPropRobustStats;


/**
 * <code>NodeFactory</code> for the "POCNormalizer" Node. Some nodes to ease the handling and mining of HCS-data.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class POCNormalizerFactory
        extends NodeFactory<POCNormalizer> {


    @Override
    public POCNormalizer createNodeModel() {
        return new POCNormalizer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<POCNormalizer> createNodeView(final int viewIndex,
                                                  final POCNormalizer nodeModel) {
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

            }
        };
    }
}

