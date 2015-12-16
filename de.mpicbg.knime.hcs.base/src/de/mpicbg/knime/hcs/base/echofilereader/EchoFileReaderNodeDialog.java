package de.mpicbg.knime.hcs.base.echofilereader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "EchoFileReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class EchoFileReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring EchoFileReader node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected EchoFileReaderNodeDialog() {
        super();
      
        addDialogComponent(new DialogComponentFileChooser(
                new SettingsModelString(
                	EchoFileReaderNodeModel.CFG_FILE_URL, ""),
                   	EchoFileReaderNodeModel.CFG_FILE_URL,
                   	JFileChooser.OPEN_DIALOG,
                   	".xml")); 

           /*addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
	        		EchoFileReaderNodeModel.CFG_Metadata, false), "Create second table for Metainformation"));*/
			
			addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
	        		EchoFileReaderNodeModel.CFG_splitDestinationCol, false), "Split Destination Well column int two rows"));
			addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
	        		EchoFileReaderNodeModel.CFG_splitSourceCol, false), "Split Source Well column int two rows"));  
        }
            
    
			
			
    }


