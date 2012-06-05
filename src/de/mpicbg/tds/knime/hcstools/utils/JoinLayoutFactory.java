package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;


/**
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class JoinLayoutFactory extends NodeFactory<JoinLayout> {


    @Override
    public JoinLayout createNodeModel() {
        return new JoinLayout();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<JoinLayout> createNodeView(final int viewIndex, final JoinLayout nodeModel) {
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
                addDialogComponent(new DialogComponentFileChooser(LoadLayoutFactory.createLayoutFileChooser(), "screen.layout.file", "xls", "xlsx"));
                addDialogComponent(new DialogComponentString(LoadLayoutFactory.createSheetName(), "Sheet name"));
            }
        };
    }

}