package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
@Deprecated
// this class has been remove from the node repository. Use ConvertRowChars instead
public class ConvertRowsFactory extends NodeFactory<ConvertRows> {

    public static final String NUM2LET = "num2let";
    public static final String LET2NUM = "let2num";


    @Override
    public ConvertRows createNodeModel() {
        return new ConvertRows();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<ConvertRows> createNodeView(final int viewIndex, final ConvertRows nodeModel) {
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
                Class[] filters = {StringValue.class, IntValue.class, DoubleValue.class};
                addDialogComponent(new DialogComponentColumnNameSelection(createTargetColumn(), "Target Column", 0, filters));
                addDialogComponent(new DialogComponentButtonGroup(createConversionType(), "Conversion Direction", false, new String[]{"Numbers to Letters", "Letters to Numbers"}, new String[]{NUM2LET, LET2NUM}));
            }
        };
    }


    public static SettingsModelString createConversionType() {
        return new SettingsModelString("conversion.direction", NUM2LET);
    }


    public static SettingsModelString createTargetColumn() {
        return new SettingsModelString("target.column", TdsUtils.SCREEN_MODEL_BARCODE);
    }
}