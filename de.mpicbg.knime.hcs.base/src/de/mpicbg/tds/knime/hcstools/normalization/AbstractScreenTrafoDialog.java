package de.mpicbg.tds.knime.hcstools.normalization;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.HCSAttributeUtils;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Arrays;
import java.util.List;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.*;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public abstract class AbstractScreenTrafoDialog extends AbstractConfigDialog {

    public SettingsModelFilterString readoutFilterString;

    /**
     * Method is called to create the configuration dialog (overridden by each subclass
     */
    @Override
    protected void createControls() {

        // add the group-by selector
        addDialogComponent(new DialogComponentColumnNameSelection(createWellGroupingAttribute(), GROUP_WELLS_BY_DESC, 0, StringValue.class));

        // initialize column filter setting
        readoutFilterString = createPropReadoutSelection();
        // column filter GUI
        DialogComponentColumnFilter readoutSelector = new DialogComponentColumnFilter(readoutFilterString, 0, true, new TdsNumericFilter());
        readoutSelector.setIncludeTitle("Normalize");
        readoutSelector.setExcludeTitle(" Available column(s) ");
        addDialogComponent(readoutSelector);

        // checkbox wether replacing raw values
        addDialogComponent(new DialogComponentBoolean(createPropReplaceValues(), "Replace existing values"));
    }


    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        AttributeUtils.updateExcludeToNonSelected(specs[0], readoutFilterString);
    }


    public static DialogComponentStringSelection setupTreatmentSelector(final AbstractScreenTrafoDialog configDialog) {
        SettingsModelString treatmentModel = createTreatmentSelector(TREATMENT);
        DialogComponentStringSelection control = new DialogComponentStringSelection(treatmentModel,
                TREATMENT, Arrays.asList(TREATMENT_LIBRARY), true);

        setupControlAttributeSelector(configDialog, Arrays.asList(control));

        configDialog.addDialogComponent(control);

        return control;
    }


    public static void setupControlAttributeSelector(final AbstractConfigDialog configDialog, final List<? extends DialogComponent> treatmentControls) {
        final SettingsModelString attribute = createTreatmentAttributeSelector();

        attribute.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                updateDependentControls(configDialog.getFirstSpec(), attribute, treatmentControls);
            }
        });

        configDialog.addDialogComponent(new DialogComponentColumnNameSelection(attribute, TREATMENT_ATTRIBUTE_DESC, 0, StringValue.class) {

            @Override
            protected void updateComponent() {
                configDialog.setFirstTableSpecs((DataTableSpec) getLastTableSpec(0));

                super.updateComponent();

                updateDependentControls((DataTableSpec) getLastTableSpec(0), attribute, treatmentControls);
            }
        });
    }


    public static void updateDependentControls(DataTableSpec tableSpecs, SettingsModelString controlAttribute, List<? extends DialogComponent> dependentControls) {

        if (tableSpecs == null) {
            return;
        }

        String selTreatAttribute = controlAttribute.getStringValue();

        for (DialogComponent dialogComponent : dependentControls) {
            if (dialogComponent instanceof DialogComponentStringSelection) {
                ((SettingsModelString) dialogComponent.getModel()).setStringValue(SELECT_TREATMENT_ADVICE);
            } else if (dialogComponent instanceof DialogComponentStringListSelection) {
                ((SettingsModelStringArray) dialogComponent.getModel()).setStringArrayValue(new String[0]);
            }

            HCSAttributeUtils.updateTreatmentControl(dialogComponent, selTreatAttribute, tableSpecs);
        }
    }

}