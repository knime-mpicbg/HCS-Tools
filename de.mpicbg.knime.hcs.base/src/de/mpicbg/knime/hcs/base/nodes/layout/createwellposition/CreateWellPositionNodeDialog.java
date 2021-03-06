package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "CreateWellPosition" Node.
 * 
 * @author Tim Nicolaisen
 */
public class CreateWellPositionNodeDialog extends DefaultNodeSettingsPane {

    @SuppressWarnings("unchecked")
    public CreateWellPositionNodeDialog() {
	super();
	
	// adding Dialog for selecting the row
	addDialogComponent(new DialogComponentColumnNameSelection(CreateWellPositionNodeModel.createPlateRow(), 
			"Select the plate row",
			0,
			DoubleValue.class, StringValue.class)); 
	
	// adding Dialog for selecting the column
	addDialogComponent(new DialogComponentColumnNameSelection(CreateWellPositionNodeModel.createPlateColumn(),
			"Select the plate column",
			0,
			DoubleValue.class, StringValue.class));     

	// adding Dialog for optional deleting the source column
	addDialogComponent(new DialogComponentBoolean(CreateWellPositionNodeModel.createDelSourceCol(), "Delete source columns"));
	
	// adding Dialog for optional changing format into an sortable
	addDialogComponent(new DialogComponentBoolean(CreateWellPositionNodeModel.createFormateColumn(), "Use sortable format"));
    }
}

