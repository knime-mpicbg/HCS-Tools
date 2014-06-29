package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class OperettaFileReaderFactory extends NodeFactory<OperettaFileReader> {

    public static final String OPERETTA_FILE_SUFFIX = "txt";


    @Override
    public OperettaFileReader createNodeModel() {
        return new OperettaFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<OperettaFileReader> createNodeView(final int viewIndex, final OperettaFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("Operetta Files", OPERETTA_FILE_SUFFIX);
    }
}