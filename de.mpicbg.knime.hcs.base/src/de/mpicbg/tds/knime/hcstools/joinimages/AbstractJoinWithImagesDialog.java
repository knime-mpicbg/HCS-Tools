package de.mpicbg.tds.knime.hcstools.joinimages;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.GROUP_WELLS_BY_DESC;
import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.createPropReadoutSelection;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class AbstractJoinWithImagesDialog extends AbstractConfigDialog {

    SettingsModelFilterString additonalAttributes;


    @Override
    public void createControls() {
        removeTab("Options");

        createNewTab("Readouts");
        addDialogComponent(new DialogComponentColumnFilter(createPropReadoutSelection(), 0, false, new TdsNumericFilter()));
//                addDialogComponent(new DialogComponentBoolean(createPropUseCompoundDB(), "Use compoundDB to annotate compounds"));

        createNewTab("Factors");
        additonalAttributes = AbstractJoinWithImagesDialog.createPropReadouts();
        DialogComponentColumnFilter filter = new DialogComponentColumnFilter(additonalAttributes, 0, false);
        addDialogComponent(filter);


        createNewTab("Plate Definition");
        addDialogComponent(new DialogComponentColumnNameSelection(AbstractJoinWithImagesDialog.createPropBarcode(), GROUP_WELLS_BY_DESC, 0, StringValue.class, IntValue.class));
        addDialogComponent(new DialogComponentColumnNameSelection(AbstractJoinWithImagesDialog.createPropPlateRow(), "Plate Row", 0, new Class[]{IntValue.class, DoubleValue.class}));
        addDialogComponent(new DialogComponentColumnNameSelection(AbstractJoinWithImagesDialog.createPropPlateCol(), "Plate Column", 0, new Class[]{IntValue.class, DoubleValue.class}));
    }


    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        AttributeUtils.updateExcludeToNonSelected(specs[0], additonalAttributes);
    }


    public static SettingsModelFilterString createPropReadouts() {
        return new SettingsModelFilterString("readouts");
    }


    public static SettingsModelString createPropBarcode() {
        return new SettingsModelString("plate.barcode", AbstractScreenTrafoModel.GROUP_WELLS_BY_DEFAULT);
    }


    public static SettingsModelString createPropPlateRow() {
        return new SettingsModelString("plate.row", TdsUtils.SCREEN_MODEL_WELL_ROW);
    }


    public static SettingsModelString createPropPlateCol() {
        return new SettingsModelString("plate.col", TdsUtils.SCREEN_MODEL_WELL_COLUMN);
    }
}
