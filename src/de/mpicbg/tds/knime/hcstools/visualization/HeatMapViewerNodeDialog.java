package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.AttributeUtils;

import org.knime.core.data.*;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;
import java.util.Arrays;

import static de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel.*;

/**
 * Creates the Node configuration dialog.
 *
 * @author Felix Meyenhofer
 *         creation: 4/6/13
 */

public class HeatMapViewerNodeDialog extends AbstractConfigDialog {

    /** The factor settings field */
    SettingsModelFilterString factorSettings;

    /** {@inheritDoc} */
    @Override
    public void createControls() {
        Class[] factorTypeList = new Class[]{StringValue.class, DoubleValue.class, IntValue.class};
        Class[] groupByTypes = new Class[]{StringValue.class, IntValue.class};
        Class[] rowColTypes = new Class[]{IntValue.class, DoubleValue.class};

        removeTab("Options");

        createNewTab("Readouts");
        addDialogComponent(new DialogComponentColumnFilter(createReadoutSettingsModel(),
                0, false, new TdsNumericFilter()));

        createNewTab("Factors");
        factorSettings = createFactorSettingModel();
        addDialogComponent(new DialogComponentColumnFilter(factorSettings, 0, false, factorTypeList));

        createNewTab("Plate");
        createNewGroup("Definition/Identification");
        SettingsModelString groupBySetting = createGroupBySettingModel();
        addDialogComponent(new DialogComponentColumnNameSelection(groupBySetting, "Group Wells By", 0, groupByTypes));
        SettingsModelString rowSetting = createPlateRowSettingModel();
        addDialogComponent(new DialogComponentColumnNameSelection(rowSetting, "Plate Row", 0, rowColTypes));
        SettingsModelString columnSetting = createPlateColumnSettingModel();
        addDialogComponent(new DialogComponentColumnNameSelection(columnSetting,"Plate Column", 0, rowColTypes));
        createNewGroup("Plate Label");
        SettingsModelString labelSetting = createPlateLabelSettingName();
        addDialogComponent(new DialogComponentColumnNameSelection(labelSetting,
                "Column for Plate Labeling", 0, new Class[]{StringValue.class}));

        createNewTab("Controls");
        DialogComponentStringListSelection stringList =
                new DialogComponentStringListSelection(createReferencePopulationsSettingModel(),
                        "Reference Groups", new ArrayList<String>(), false, 7);
        AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(stringList));
        addDialogComponent(stringList);
    }

    /** {@inheritDoc} */
    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        AttributeUtils.updateExcludeToNonSelected(specs[0], factorSettings);
    }

}
