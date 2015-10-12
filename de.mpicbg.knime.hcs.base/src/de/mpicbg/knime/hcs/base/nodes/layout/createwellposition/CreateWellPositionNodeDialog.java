package de.mpicbg.knime.hcs.base.nodes.layout.createwellposition;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnSelectionPanel;





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
		
			
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		
		String PlateColumn = settings.getString(CreateWellPositionNodeModel.CFG_PlateColumn, CreateWellPositionNodeModel.CFG_PlateColumn_DFT);
		
	
	}
	
	
	
	
}

