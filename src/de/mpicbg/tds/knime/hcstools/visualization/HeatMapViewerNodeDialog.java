package de.mpicbg.tds.knime.hcstools.visualization;

import de.mpicbg.tds.knime.HCSAttributeUtils;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.AttributeUtils;

import org.knime.core.data.*;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

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
        // Create the settings and the dialog components
        final SettingsModelString referenceParameterSettings = createReferenceParameterSettingModel();
        final SettingsModelStringArray referenceGroupSetting = createReferencePopulationsSettingModel();
        DialogComponentStringListSelection referenceGroupComponent =
                new DialogComponentStringListSelection(
                        referenceGroupSetting, "Reference Groups", new ArrayList<String>(), false, 7);
        DialogComponentColumnNameSelection referenceParameterComponent =
                createReferenceParameterDC(this, referenceParameterSettings, referenceGroupComponent);
        // Add them in the right order (opposite of creation order)
        addDialogComponent(referenceParameterComponent);
        addDialogComponent(referenceGroupComponent);
    }

    /** {@inheritDoc} */
    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        AttributeUtils.updateExcludeToNonSelected(specs[0], factorSettings);
    }

    /**
     * Creates the table column selector with a custom update mechanism.
     * This component acts on the group selection list and updates the choices
     * when the reference group parameter (column) changes.
     *
     * @param setting of the column selector
     * @param dependentComponent of the column parameter
     * @return column selector
     */
    private DialogComponentColumnNameSelection createReferenceParameterDC(final AbstractConfigDialog dialog,
            final SettingsModelString setting, final DialogComponentStringListSelection dependentComponent) {
        setting.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateReferenceGroupDC(setting, dependentComponent, dialog.getFirstSpec());
            }
        });
        return new DialogComponentColumnNameSelection(setting, "Subset Column",0,StringValue.class) {
            @Override
            protected void updateComponent() {
                super.updateComponent();

                updateReferenceGroupDC(setting, dependentComponent, (DataTableSpec) getLastTableSpec(0));
            }
        };
    }

    /**
     * Updates the reference group list dialog component when the
     * reference group parameter (column) changes.
     *
     * @param setting of the table column selector
     * @param dependentComponent of the table column selector
     * @param specs of the input table
     */
    private void updateReferenceGroupDC(SettingsModelString setting, DialogComponentStringListSelection dependentComponent, DataTableSpec specs) {

        if (specs == null)
            return;

        String selectedParameter = setting.getStringValue();
        ((SettingsModelStringArray) dependentComponent.getModel()).setStringArrayValue(new String[0]);

        List<String> groups = HCSAttributeUtils.getTreatments(specs, selectedParameter);

        if (groups == null) {
            groups = new ArrayList<String>();
        } else if (groups.isEmpty()) {
            groups.add("");
        }

        String[] currentSelection = ((SettingsModelStringArray) dependentComponent.getModel()).getStringArrayValue();
        dependentComponent.replaceListItems(groups, currentSelection);
    }

}
