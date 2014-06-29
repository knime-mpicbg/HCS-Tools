package de.mpicbg.knime.hcs.base.nodes.norm;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * The class only provides certain methods to create dialog components for all normalizer nodes
 * (as they have several common components)
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/22/11
 * Time: 8:50 AM
 */
public class AbstractNormalizerDialog extends AbstractConfigDialog {
    @Override
    protected void createControls() {
    }

    protected DialogComponentColumnNameSelection createGroupByComboBox(SettingsModelString groupBySetting, String label) {
        return new DialogComponentColumnNameSelection(groupBySetting, label, 0, StringValue.class);
    }
}
