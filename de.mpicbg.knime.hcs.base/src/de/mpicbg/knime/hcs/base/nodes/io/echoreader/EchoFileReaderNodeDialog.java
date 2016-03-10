package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

import javax.swing.JFileChooser;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 * <code>NodeDialog</code> for the "EchoFileReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Magda Rucinska
 */
public class EchoFileReaderNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring EchoFileReader node dialog.
	 * This is just a suggestion to demonstrate possible default dialog
	 * components.
	 */
	protected EchoFileReaderNodeDialog() {
		super();

		FlowVariableModel flowVarBrowseModel = createFlowVariableModel(EchoFileReaderNodeModel.CFG_FILE_URL, Type.STRING);

		addDialogComponent(new DialogComponentFileChooser(new SettingsModelString(EchoFileReaderNodeModel.CFG_FILE_URL, ""),
				EchoFileReaderNodeModel.CFG_FILE_URL, JFileChooser.OPEN_DIALOG, false, flowVarBrowseModel, ".xml"));
		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				EchoFileReaderNodeModel.CFG_splitDestinationCol, false), "Split Destination Well column in two rows"));
		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				EchoFileReaderNodeModel.CFG_splitSourceCol, false), "Split Source Well column in two rows"));  
	}

}


