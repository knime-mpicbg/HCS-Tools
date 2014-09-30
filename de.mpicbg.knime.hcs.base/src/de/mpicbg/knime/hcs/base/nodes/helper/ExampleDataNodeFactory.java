package de.mpicbg.knime.hcs.base.nodes.helper;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeFactory</code> for the "MatlabSnippet" Node. Matlab integration for Knime.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class ExampleDataNodeFactory
        extends NodeFactory<ExampleDataNodeModel> {

    @Override
    public ExampleDataNodeModel createNodeModel() {
        return new ExampleDataNodeModel();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    @Override
    public NodeView<ExampleDataNodeModel> createNodeView(final int viewIndex,
                                                         final ExampleDataNodeModel nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ExampleDataNodeDialog();
    }


    public static SettingsModelString createSelectedExampleProperty() {
        return new SettingsModelString("sel.ex", "");
    }


}

