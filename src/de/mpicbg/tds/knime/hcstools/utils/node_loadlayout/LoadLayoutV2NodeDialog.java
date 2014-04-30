package de.mpicbg.tds.knime.hcstools.utils.node_loadlayout;

import de.mpicbg.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.core.ExcelLayout;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>NodeDialog</code> for the "LoadLayoutV2" Node.
 * <p/>
 * <p/>
 * This node dialog derives from {@link AbstractConfigDialog} which allows
 * creation of a simple dialog with standard components.
 *
 * @author Antje Niederlein
 */
public class LoadLayoutV2NodeDialog extends AbstractConfigDialog {

    private DialogComponentStringSelection layoutSheetComponent;
    private SettingsModelString layoutSheet;

    /**
     * New pane for configuring the LoadLayoutV2 node.
     */
    protected LoadLayoutV2NodeDialog() {
        super();
    }

    @Override
    protected void createControls() {
        SettingsModelString fileModelSetting = LoadLayoutV2NodeModel.createLayoutFileSelectionModel();
        fileModelSetting.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateSheetNames(((SettingsModelString) changeEvent.getSource()).getStringValue());
            }
        });
        addDialogComponent(new DialogComponentFileChooser(fileModelSetting, "plateLayoutFile", JFileChooser.OPEN_DIALOG, ".xls|.xlsx"));

        layoutSheet = LoadLayoutV2NodeModel.createLayoutSheetSelectionModel();
        layoutSheetComponent = new DialogComponentStringSelection(layoutSheet, "Select sheet name", "");
        addDialogComponent(layoutSheetComponent);
    }

    private void updateSheetNames(String fileName) {
        List<String> availableSheetNames;
        try {
            ExcelLayout layout = new ExcelLayout(fileName);
            availableSheetNames = layout.getSheetNames();
        } catch (IOException e) {
            e.printStackTrace();  //TODO: error message (comment: might be caught later when closing the dialog)
            // clear available sheets
            availableSheetNames = new ArrayList<String>();
            availableSheetNames.add("");
            layoutSheetComponent.replaceListItems(availableSheetNames, availableSheetNames.get(0));
        }

        layoutSheetComponent.replaceListItems(availableSheetNames, availableSheetNames.get(0));
    }
}

