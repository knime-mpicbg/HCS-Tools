package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class XMLFileReaderFactory extends NodeFactory<XMLFileReader> {


    @Override
    public XMLFileReader createNodeModel() {
        return new XMLFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<XMLFileReader> createNodeView(final int viewIndex, final XMLFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("XML Result Files", ".xml") {
            @Override
            public void createControls() {
                super.createControls();

                createNewTab("File Types");

                final SettingsModelString suffixProperty = createPropSuffix();
                suffixProperty.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        String newSuffix = suffixProperty.getStringValue();
                        if (newSuffix.length() > 1) {
                            setFileNameFilterSuffixes(new String[]{newSuffix});
                        }
                    }
                });
                addDialogComponent(new DialogComponentString(suffixProperty, "File suffix"));


                addDialogComponent(new DialogComponentString(createPropXPathQuery(), "XPath query", true, 50));
            }
        };
    }


    public static SettingsModelString createPropSuffix() {
        return new SettingsModelString("filetype.suffix", "xml");
    }


    public static SettingsModelString createPropXPathQuery() {
        return new SettingsModelString("xpath.query", "/");
    }
}