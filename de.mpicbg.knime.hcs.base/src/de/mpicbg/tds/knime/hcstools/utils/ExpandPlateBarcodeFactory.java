package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandPlateBarcodeFactory extends NodeFactory<ExpandPlateBarcode> {

    private static String DEF_BARCODE_COLUMN = "barcode.column";


    @Override
    public ExpandPlateBarcode createNodeModel() {
        return new ExpandPlateBarcode();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<ExpandPlateBarcode> createNodeView(final int viewIndex, final ExpandPlateBarcode nodeModel) {
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
                addDialogComponent(new DialogComponentColumnNameSelection(createPropBarcode(), "Barcode column", 0, StringValue.class));
            }
        };
    }


    public static SettingsModelString createPropBarcode() {
        return new SettingsModelString(DEF_BARCODE_COLUMN, AbstractScreenTrafoModel.GROUP_WELLS_BY_DEFAULT);
    }


}