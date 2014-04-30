package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class MotionTrackingFileReaderFactory extends NodeFactory<MotionTrackingFileReader> {


    @Override
    public MotionTrackingFileReader createNodeModel() {
        return new MotionTrackingFileReader();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<MotionTrackingFileReader> createNodeView(final int viewIndex, final MotionTrackingFileReader nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DefaultMicroscopeReaderDialog("MotionTracking Export", "csv", "txt") {
            @Override
            public void createControls() {
                super.createControls();

                createNewTab("Advanced");
                addDialogComponent(new DialogComponentString(MotionTrackingFileReader.createSuffixPatternProperty(), "MTF-filename pattern after barcode"));
                addDialogComponent(new DialogComponentStringSelection(createFileExtensionSelection(), "File extension", createFileExtensionOptions()));
                addDialogComponent(new DialogComponentStringSelection(createColumnSeperatorSelection(), "Column seperator", createColumnSeperatorOptions()));

            }
        };
    }


    static SettingsModelString createFileExtensionSelection() {
        return new SettingsModelString("FileExtension", "csv");
    }


    static Collection<String> createFileExtensionOptions() {
        Collection<String> options = new ArrayList<String>();
        options.add("csv");
        options.add("txt");
        return options;
    }


    static SettingsModelString createColumnSeperatorSelection() {
        return new SettingsModelString("ColumnSeperator", ",");
    }


    static Collection<String> createColumnSeperatorOptions() {
        Collection<String> options = new ArrayList<String>();
        options.add(",");
        options.add("\t");
        return options;
    }
}