package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.tds.core.tools.resconverter.ResConverter;
import de.mpicbg.tds.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class OperaFileReaderFactory extends NodeFactory<OperaFileReader> {


    @Override
    public OperaFileReader createNodeModel() {
        return new OperaFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<OperaFileReader> createNodeView(final int viewIndex, final OperaFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("Opera Files", ResConverter.RES_SUFFIX);
    }
}