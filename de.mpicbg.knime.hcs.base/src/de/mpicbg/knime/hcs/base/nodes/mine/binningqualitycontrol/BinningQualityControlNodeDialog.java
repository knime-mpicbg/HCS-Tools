package de.mpicbg.knime.hcs.base.nodes.mine.binningqualitycontrol;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import de.mpicbg.knime.hcs.base.nodes.manip.col.numformat.NumberFormatterNodeModel;
import de.mpicbg.knime.hcs.base.nodes.mine.BinningAnalysisNodeModel;

/**
 * <code>NodeDialog</code> for the "BinningQualityControl" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Tim Nicolaisen
 */
public class BinningQualityControlNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring BinningQualityControl node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected BinningQualityControlNodeDialog() {
        super();
        
     // Group data by
        addDialogComponent(new DialogComponentColumnNameSelection(BinningAnalysisNodeModel.createAggregationSelectionModel(), "Aggregate object data by", 0,
                true, false, new Class[]{org.knime.core.data.StringValue.class}));
        
        addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
        		NumberFormatterNodeModel.CFG_deleteSouceCol, false), "Delete Column with p-Value between "));
        // numeric field to configure number of bins
        DialogComponentNumberEdit nEdit = new DialogComponentNumberEdit(BinningAnalysisNodeModel.createBinSelectionModel(), "Lower Bound:");
        addDialogComponent(nEdit); // numeric field to configure number of bins
        DialogComponentNumberEdit nEdite = new DialogComponentNumberEdit(BinningAnalysisNodeModel.createBinSelectionModel(), "Upper Bound:");
        addDialogComponent(nEdit);
                    
    }
}

