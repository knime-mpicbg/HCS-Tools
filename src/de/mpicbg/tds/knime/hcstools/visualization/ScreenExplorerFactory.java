package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.knutils.AbstractNodeView;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.view.ScreenPanel;
import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import java.awt.*;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.GROUP_WELLS_BY_DESC;
import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * @author Holger Brandl (MPI-CBG)
 */
@Deprecated
public class ScreenExplorerFactory extends NodeFactory<ScreenExplorer> {


    @Override
    public ScreenExplorer createNodeModel() {
        return new ScreenExplorer();
    }


    @Override
    public int getNrNodeViews() {
        return 1;
    }


    @Override
    public NodeView<ScreenExplorer> createNodeView(final int viewIndex,
                                                   final ScreenExplorer nodeModel) {
        return new AbstractNodeView<ScreenExplorer>(nodeModel) {


            @Override
            protected Component createViewComponent() {


                if (nodeModel.getPlates() == null) {
                    nodeModel.setPlotWarning("You need to re-execute the node before the view will show up");
                    return null;
                }

                if (nodeModel.getPlates().isEmpty()) {
                    nodeModel.setPlotWarning("Could not create view for empty input table!");
                    return null;

                }

                return createNewExplorer(nodeModel);
            }


            @Override
            protected void modelChanged() {
                if (getNodeModel() == null || getNodeModel().getPlates() == null) {
                    return;
                }


                ((ScreenPanel) getComponent()).setPlates(nodeModel.getPlates());
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        setComponent(createNewExplorer(nodeModel));
//                    }
//                });
            }
        };
    }


    private Component createNewExplorer(ScreenExplorer nodeModel) {
//        if (nodeModel.doConnectToCompoundDB() && compoundService == null) {
//            compoundService = new AnnotationConfigApplicationContext(CompoundDBConfig.class).getBean(ChemicalService.class);
//        }

        return new ScreenPanel(nodeModel.getPlates());
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {

        return new AbstractConfigDialog() {
            SettingsModelFilterString overlayFilterString;


            @Override
            public void createControls() {
                removeTab("Options");

                createNewTab("Readouts");
                addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, false, new TdsNumericFilter()));
//                addDialogComponent(new DialogComponentBoolean(createPropUseCompoundDB(), "Use compoundDB to annotate compounds"));

                createNewTab("Factors");
                overlayFilterString = createPropFactorSelection();
                DialogComponentColumnFilter filter = new DialogComponentColumnFilter(overlayFilterString, 0, false, new Class[]{StringValue.class, DoubleValue.class, IntValue.class});
                addDialogComponent(filter);

                createNewTab("Plate Definition");
                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createGroupBy(), GROUP_WELLS_BY_DESC, 0, StringValue.class, IntValue.class));
                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createPropPlateRow(), "Plate Row", 0, new Class[]{IntValue.class, DoubleValue.class}));
                addDialogComponent(new DialogComponentColumnNameSelection(HCSSettingsFactory.createPropPlateCol(), "Plate Column", 0, new Class[]{IntValue.class, DoubleValue.class}));
            }


            @Override
            public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
                super.loadAdditionalSettingsFrom(settings, specs);

                AttributeUtils.updateExcludeToNonSelected(specs[0], overlayFilterString);
            }
        };
    }


    public static SettingsModelFilterString createPropFactorSelection() {
        SettingsModelFilterString settingsModelFilterString = new SettingsModelFilterString("selected.overlays");
        settingsModelFilterString.setIncludeList(new String[]{TdsUtils.SCREEN_MODEL_TREATMENT});

        return settingsModelFilterString;
    }


    //
//    public static SettingsModelBoolean createPropUseCompoundDB() {
//        return new SettingsModelBoolean("usecompounddb", false);
//    }
}