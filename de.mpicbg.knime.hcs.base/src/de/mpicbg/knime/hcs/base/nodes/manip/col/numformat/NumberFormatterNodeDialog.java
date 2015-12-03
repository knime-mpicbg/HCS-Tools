package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 
 * @author 
 */
public class NumberFormatterNodeDialog extends DefaultNodeSettingsPane {

	 @SuppressWarnings("unchecked")
    public NumberFormatterNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentColumnNameSelection(
        		new SettingsModelString(
        		NumberFormatterNodeModel.CFG_ConcentrationColumn,NumberFormatterNodeModel.CFG_ConcentrationColumn_DFT),
    			"Select a column",
    			0,
    			DoubleValue.class, StringValue.class)); 
                    // each dialog component requires an appropriate settings models to store the value in
        addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
        		NumberFormatterNodeModel.CFG_deleteSouceCol, false), "Delete the Source Column"));
        
        addDialogComponent(new DialogComponentStringSelection(
                new SettingsModelString(NumberFormatterNodeModel.CFG_leadingCharacter, "0"),
                "Choose the leading character:", "0", " ", "_"));
    }
}

