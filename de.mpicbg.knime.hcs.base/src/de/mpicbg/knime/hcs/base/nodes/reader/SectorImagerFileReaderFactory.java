package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class SectorImagerFileReaderFactory extends NodeFactory<SectorImagerFileReader> {

    public static final String[] SECTORIMAGER_FILE_SUFFIXES = new String[]{"txt"};


    @Override
    public SectorImagerFileReader createNodeModel() {
        return new SectorImagerFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<SectorImagerFileReader> createNodeView(final int viewIndex, final SectorImagerFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("SectorImager Excel Result Files", SECTORIMAGER_FILE_SUFFIXES);
    }
}