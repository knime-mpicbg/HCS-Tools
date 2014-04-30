package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandWellPositionFactory extends NodeFactory<ExpandWellPosition> {


    @Override
    public ExpandWellPosition createNodeModel() {
        return new ExpandWellPosition();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<ExpandWellPosition> createNodeView(final int viewIndex, final ExpandWellPosition nodeModel) {
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

                addDialogComponent(new DialogComponentColumnNameSelection(createWellPosProp(), "Well Position", 0, new Class[]{StringValue.class}));
                addDialogComponent(new DialogComponentBoolean(createConvertRowCharsProp(), "Convert Row Characters"));
                addDialogComponent(new DialogComponentBoolean(createDeleteSourceColProp(), "Delete Source Column"));
            }
        };
    }


    public static SettingsModelString createWellPosProp() {
        return new SettingsModelString("well.position.column", "position");   // position is just a guess about the column name in the table
    }


    public static SettingsModelBoolean createConvertRowCharsProp() {
        return new SettingsModelBoolean("convert.row.characters", true);
    }


    public static SettingsModelBoolean createDeleteSourceColProp() {
        return new SettingsModelBoolean("delete.source.column", true);
    }
}