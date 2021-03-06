package de.mpicbg.knime.hcs.base.nodes.layout;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.knime.hcs.base.utils.URLSupport;
import de.mpicbg.knime.hcs.core.ExcelLayout;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>NodeDialog</code> for the "JoinLayoutV2" Node.
 * <p/>
 * <p/>
 * This node dialog derives from {@link AbstractConfigDialog} which allows
 * creation of a simple dialog with standard components.
 *
 * @author Antje Niederlein
 */
public class JoinLayoutV2NodeDialog extends AbstractConfigDialog {

    private DialogComponentStringSelection layoutSheetComponent;
    private SettingsModelString layoutSheet;

    /**
     * New pane for configuring the JoinLayoutV2 node.
     */
    protected JoinLayoutV2NodeDialog() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void createControls() {
        SettingsModelString fileModelSetting = JoinLayoutV2NodeModel.createLayoutFileSelectionModel();
        fileModelSetting.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateSheetNames(((SettingsModelString) changeEvent.getSource()).getStringValue());
            }
        });
        addDialogComponent(new DialogComponentFileChooser(fileModelSetting, "plateLayoutFile", JFileChooser.OPEN_DIALOG, ".xls|.xlsx"));

        layoutSheet = JoinLayoutV2NodeModel.createLayoutSheetSelectionModel();
        layoutSheetComponent = new DialogComponentStringSelection(layoutSheet, "Select sheet name", "");
        addDialogComponent(layoutSheetComponent);

        addDialogComponent(new DialogComponentColumnNameSelection(JoinLayoutV2NodeModel.createPlateRowSelectionModel(), "Plate row", 0, true, new Class[]{org.knime.core.data.DoubleValue.class, org.knime.core.data.IntValue.class}));
        addDialogComponent(new DialogComponentColumnNameSelection(JoinLayoutV2NodeModel.createPlateColumnSelectionModel(), "Plate column", 0, true, new Class[]{org.knime.core.data.DoubleValue.class, org.knime.core.data.IntValue.class}));
    }

    private void updateSheetNames(String fileName) {
    	List<String> availableSheetNames;
    	
    	ExcelLayout layout = null;
		
			// try to access and read the file
    		URLSupport excelURL;
			try {
				excelURL = new URLSupport(fileName);
				InputStream excelStream = excelURL.getInputStream();
	    		layout = new ExcelLayout(excelStream,fileName, excelURL.getTimestamp());
	    		excelStream.close();
			} catch (IOException e) {
			}
    	
    	// if loading Excel file failed
    	if(layout == null) {
    		// clear available sheets
            availableSheetNames = new ArrayList<String>();
            availableSheetNames.add("");
    	} else {
    		availableSheetNames = layout.getSheetNames();
    	}
    	
    	// update component
    	layoutSheetComponent.replaceListItems(availableSheetNames, availableSheetNames.get(0));
    }
}

