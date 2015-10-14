package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;





/**
 * <code>NodeDialog</code> for the "CreateWellPosition" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class CreateWellPositionNodeDialog extends DefaultNodeSettingsPane {





    @SuppressWarnings("unchecked")
    public CreateWellPositionNodeDialog() {
	super();

	addDialogComponent(new DialogComponentColumnNameSelection(
		new SettingsModelString(
			CreateWellPositionNodeModel.CFG_PlateColumn,
			"Select a column"),
			"Select the Plate Column",
			0,
			DoubleValue.class, StringValue.class));     



	addDialogComponent(new DialogComponentColumnNameSelection(
		new SettingsModelString(
			CreateWellPositionNodeModel.CFG_PlateRow,
			"Select a Row"), 
			"Select the Plate Row",
			0,
			DoubleValue.class, StringValue.class));          

	addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
		CreateWellPositionNodeModel.CFG_deleteSouceCol, false), "Delete the Source Columns"));
	
	addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
		CreateWellPositionNodeModel.CFG_formateColumn, false), "Formate new Column like A01, B09 for sorting."));


    }



}

