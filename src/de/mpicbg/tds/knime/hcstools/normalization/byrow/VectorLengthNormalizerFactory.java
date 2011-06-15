package de.mpicbg.tds.knime.hcstools.normalization.byrow;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.NumericFilter;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReplaceValues;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class VectorLengthNormalizerFactory extends NodeFactory<VectorLengthNormalizer> {


    @Override
    public VectorLengthNormalizer createNodeModel() {
        return new VectorLengthNormalizer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<VectorLengthNormalizer> createNodeView(final int viewIndex, final VectorLengthNormalizer nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractConfigDialog() {

            @Override
            protected void createControls() {

                addDialogComponent(new DialogComponentColumnFilter(createVectorSpaceSelector(), 0, false, new NumericFilter()));
                addDialogComponent(new DialogComponentBoolean(createPropReplaceValues(), "Replace existing values"));
                addDialogComponent(new DialogComponentBoolean(createAppendLengthCol(), "Add a column containig the vector norm"));
            }
        };
    }

    public static SettingsModelBoolean createAppendLengthCol() {
        return new SettingsModelBoolean("add_vect_norm_col", false);
    }


    public static SettingsModelFilterString createVectorSpaceSelector() {
        return new SettingsModelFilterString("vector.space");
    }
}