package de.mpicbg.tds.knime.hcstools.qualitycontrol;

import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.ArrayList;
import java.util.Arrays;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * @author Felix Meyenhofer (MPI-CBG)
 */
public class MultivariateZPrimesFactory extends NodeFactory<MultivariateZPrimes> {


    @Override
    public MultivariateZPrimes createNodeModel() {
        return new MultivariateZPrimes();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<MultivariateZPrimes> createNodeView(final int viewIndex, final MultivariateZPrimes nodeModel) {
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

                DialogComponentStringListSelection multiPosCtrlsProperty = new DialogComponentStringListSelection(createMultiCtls(MultivariateZPrimes.POS_CTRL_MULTIPLE), "Positive Controls", new ArrayList<String>(), false, 7);
                DialogComponentStringListSelection multiNegCtrlProperty = new DialogComponentStringListSelection(createMultiCtls(MultivariateZPrimes.NEG_CTRL_MULTIPLE), "Negative Controls", new ArrayList<String>(), false, 7);

                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(multiPosCtrlsProperty, multiNegCtrlProperty));

                addDialogComponent(multiPosCtrlsProperty);
                addDialogComponent(multiNegCtrlProperty);

                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumericFilter()));
            }
        };
    }


    static SettingsModelStringArray createMultiCtls(String propName) {
        return new SettingsModelStringArray(propName, new String[0]);
    }
}