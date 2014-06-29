package de.mpicbg.knime.hcs.base.nodes.norm;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import java.util.Arrays;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.*;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class NPINormalizerFactory extends NodeFactory<NPINormalizer> {


    @Override
    public NPINormalizer createNodeModel() {
        return new NPINormalizer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<NPINormalizer> createNodeView(final int viewIndex, final NPINormalizer nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractScreenTrafoDialog() {

            private DialogComponentStringSelection posCtrlProperty;
            private DialogComponentStringSelection negCtrlProperty;


            @Override
            protected void createControls() {
                super.createControls();

                posCtrlProperty = new DialogComponentStringSelection(createTreatmentSelector(TREATMENT_POS_CONTROL),
                        "Positive Control", Arrays.asList(SELECT_TREATMENT_ADVICE), true);

                negCtrlProperty = new DialogComponentStringSelection(createTreatmentSelector(TREATMENT_NEG_CONTROL),
                        "Negative Control", Arrays.asList(SELECT_TREATMENT_ADVICE), true);

                // setup the treatment-attribute selector
                setupControlAttributeSelector(this, Arrays.asList(posCtrlProperty, negCtrlProperty));

                addDialogComponent(posCtrlProperty);
                addDialogComponent(negCtrlProperty);
                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
            }
        };
    }
}