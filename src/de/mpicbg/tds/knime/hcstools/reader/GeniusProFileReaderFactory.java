package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.tds.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class GeniusProFileReaderFactory extends NodeFactory<GeniusProFileReader> {


    @Override
    public GeniusProFileReader createNodeModel() {
        return new GeniusProFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<GeniusProFileReader> createNodeView(final int viewIndex, final GeniusProFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("GeniusPro Result Files", GeniusProFileReader.GENIUSPRO_FILE_SUFFIX);
    }
}