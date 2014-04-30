package de.mpicbg.tds.knime.hcstools.screenmining;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class EnrichmentAnalyzerFactory extends NodeFactory<EnrichmentAnalyzer> {


    @Override
    public EnrichmentAnalyzer createNodeModel() {
        return new EnrichmentAnalyzer();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<EnrichmentAnalyzer> createNodeView(final int viewIndex, final EnrichmentAnalyzer nodeModel) {
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
                addDialogComponent(new DialogComponentColumnNameSelection(createGroupBy(), EnrichmentAnalyzer.GROUP_BY_COLUMN_DESC, 0, new Class[]{StringValue.class, IntValue.class}));
                addDialogComponent(new DialogComponentColumnNameSelection(createOntologyTermProperty(), EnrichmentAnalyzer.ONTOLOGY_TERMS_COLUMN_DESC, 0, new Class[]{StringValue.class, IntValue.class}));
//                addDialogComponent(new DialogComponentBoolean(useBonferroniCorrection(), EnrichmentAnalyzer.USE_BONFERRONI_DESC));
            }
        };
    }


    public static SettingsModelString createGroupBy() {
        return new SettingsModelString(EnrichmentAnalyzer.GROUP_BY_COLUMN, "");
    }


    public static SettingsModelString createOntologyTermProperty() {
        return new SettingsModelString(EnrichmentAnalyzer.ONTOLOGY_TERMS_COLUMN, "");
    }


    public static SettingsModelBoolean useBonferroniCorrection() {
        return new SettingsModelBoolean(EnrichmentAnalyzer.USE_BONFERRONI, false);
    }
}