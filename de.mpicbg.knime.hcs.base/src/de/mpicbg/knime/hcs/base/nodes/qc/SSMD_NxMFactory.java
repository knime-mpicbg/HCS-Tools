package de.mpicbg.knime.hcs.base.nodes.qc;

import de.mpicbg.knime.hcs.base.HCSSettingsFactory;
import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoDialog;
import de.mpicbg.knime.hcs.base.utils.TdsNumericFilter;
import de.mpicbg.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Arrays;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.*;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class SSMD_NxMFactory extends SSMDFactory {


    @Override
    public SSMD createNodeModel() {
        return new SSMD_NxM();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<SSMD> createNodeView(final int viewIndex, final SSMD nodeModel) {
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

                DialogComponentStringListSelection multiPosCtrlsProperty = new DialogComponentStringListSelection(createMultiCtls(ZPrimesNxM.POS_CTRL_MULTIPLE), "Positive Controls", new ArrayList<String>(), false, 7);
                DialogComponentStringListSelection multiNegCtrlProperty = new DialogComponentStringListSelection(createMultiCtls(ZPrimesNxM.NEG_CTRL_MULTIPLE), "Negative Controls", new ArrayList<String>(), false, 7);

                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(multiPosCtrlsProperty, multiNegCtrlProperty));

                addDialogComponent(multiPosCtrlsProperty);
                addDialogComponent(multiNegCtrlProperty);

                addDialogComponent(new DialogComponentBoolean(createPropRobustStats(), ROBUST_STATS_PROPERTY_DESCS));
                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, true, new TdsNumericFilter()));
            }
        };
    }


    static SettingsModelStringArray createMultiCtls(String propName) {
        return new SettingsModelStringArray(propName, new String[8]);
    }
}