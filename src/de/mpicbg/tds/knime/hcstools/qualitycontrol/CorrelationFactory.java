package de.mpicbg.tds.knime.hcstools.qualitycontrol;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.knutils.StringFilter;
import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Collection;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * @author Felix Meyenhofer (MPI-CBG)
 * @deprecated
 */
public class CorrelationFactory extends NodeFactory<Correlation> {


    @Override
    public Correlation createNodeModel() {
        return new Correlation();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<Correlation> createNodeView(final int viewIndex, final Correlation nodeModel) {
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

                addDialogComponent(new DialogComponentStringSelection(createCorrelationMethodSelection(), "Correlation method", createCorrelationMethodUsageOptions()));

                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createGroupBy(), "Subsets", 0, StringValue.class));

                addDialogComponent(new DialogComponentStringSelection(createColumnFilterUsageSelection(), "Use the following colum-filter for:", createColumnFilterUsageOptions()));

                addDialogComponent(new DialogComponentColumnFilter(createConstraintsSelection(), 0, true, new StringFilter()));

                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumericFilter()));
            }
        };
    }


    static SettingsModelString createCorrelationMethodSelection() {
        return new SettingsModelString("CorrelationMethodSetting", "Pearson");
    }


    static Collection<String> createCorrelationMethodUsageOptions() {
        Collection<String> options = new ArrayList<String>();
        options.add("Spearman");
        options.add("Pearson");
        return options;
    }


    static SettingsModelString createColumnFilterUsageSelection() {
        return new SettingsModelString("ColumnFilterUsageSetting", "batch");
    }


    static Collection<String> createColumnFilterUsageOptions() {
        Collection<String> options = new ArrayList<String>();
        options.add("batch-processing");
        options.add("measurement-association");
        return options;
    }


    static SettingsModelFilterString createConstraintsSelection() {
        return new SettingsModelFilterString("constraints");
    }
}