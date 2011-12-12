package de.mpicbg.tds.knime.hcstools.qualitycontrol;

import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Arrays;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.*;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class CVCalculatorFactory extends NodeFactory<CVCalculator> {


    @Override
    public CVCalculator createNodeModel() {
        return new CVCalculator();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<CVCalculator> createNodeView(final int viewIndex, final CVCalculator nodeModel) {
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
                addDialogComponent(new DialogComponentColumnNameSelection(createWellGroupingAttribute(), GROUP_WELLS_BY_DESC, 0, new Class[]{StringValue.class, IntValue.class, DateAndTimeValue.class}));

                DialogComponentStringListSelection treatmentList = new DialogComponentStringListSelection(createTreatmentProperty(), TREATMENT, new ArrayList<String>(), false, 6);
                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(treatmentList));
                addDialogComponent(treatmentList);

                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumericFilter()));
            }
        };
    }

    static SettingsModelStringArray createTreatmentProperty() {
        return new SettingsModelStringArray("filter.treatment", new String[0]);
    }
}