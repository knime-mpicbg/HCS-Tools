package de.mpicbg.knime.hcs.base.nodes.layout;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.hcs.core.LayoutUtils;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class LoadLayoutFactory extends NodeFactory<LoadLayout> {


    @Override
    public LoadLayout createNodeModel() {
        return new LoadLayout();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<LoadLayout> createNodeView(final int viewIndex, final LoadLayout nodeModel) {
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
            public void createControls() {
                addDialogComponent(new DialogComponentFileChooser(createLayoutFileChooser(), "screen.layout.file", "xls", "xlsx"));
                addDialogComponent(new DialogComponentString(createSheetName(), "Sheet name"));
            }
        };
    }


    public static SettingsModelString createLayoutFileChooser() {
        return new SettingsModelString("layout.file", "");
    }


    public static SettingsModelString createSheetName() {
        return new SettingsModelString("sheet.name", LayoutUtils.DEFAULT_LAYOUT_SHEET_NAME);
    }
}