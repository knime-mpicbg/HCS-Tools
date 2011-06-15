package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.tds.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class EnvisionFileReaderFactory extends NodeFactory<EnvisionFileReader> {


    @Override
    public EnvisionFileReader createNodeModel() {
        return new EnvisionFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<EnvisionFileReader> createNodeView(final int viewIndex, final EnvisionFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("Envision Result Files", EnvisionFileReader.getAllowedFileExtensions()) {

            @Override
            public void createControls() {
                super.createControls();

                createNewTab("Advanced");
                addDialogComponent(new DialogComponentNumber(createTableIndex(), "Table index (first plate-table in file =1, etc.)", 1));
            }
        };
    }


    public static SettingsModelInteger createTableIndex() {
        return new SettingsModelInteger("input.table.index", 1);
    }
}