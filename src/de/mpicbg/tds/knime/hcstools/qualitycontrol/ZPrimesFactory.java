package de.mpicbg.tds.knime.hcstools.qualitycontrol;

import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.Arrays;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.*;


/**
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class ZPrimesFactory extends NodeFactory<ZPrimes> {


    @Override
    public ZPrimes createNodeModel() {
        return new ZPrimes();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<ZPrimes> createNodeView(final int viewIndex, final ZPrimes nodeModel) {
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
                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createGroupBy(), "Group by", 0, StringValue.class));


                DialogComponentStringSelection posCtrlProperty = new DialogComponentStringSelection(createTreatmentSelector(TREATMENT_POS_CONTROL),
                        POSITIVE_CONTROL_DESC, Arrays.asList(SELECT_TREATMENT_ADVICE), true);

                DialogComponentStringSelection negCtrlProperty = new DialogComponentStringSelection(createTreatmentSelector(TREATMENT_NEG_CONTROL),
                        NEGATIVE_CONTROL_DESC, Arrays.asList(SELECT_TREATMENT_ADVICE), true);


                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(posCtrlProperty, negCtrlProperty));

                addDialogComponent(negCtrlProperty);
                addDialogComponent(posCtrlProperty);

                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumericFilter()));
            }
        };
    }


    static SettingsModelStringArray createMultiCtls(String propName) {
        return new SettingsModelStringArray(propName, new String[0]);
    }

}