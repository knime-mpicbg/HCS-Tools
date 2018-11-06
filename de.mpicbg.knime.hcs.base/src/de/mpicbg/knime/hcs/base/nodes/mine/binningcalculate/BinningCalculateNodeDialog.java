package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.util.filter.column.DataTypeColumnFilter;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

/**
 * <code>NodeDialog</code> for the "BinningCalculate" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Tim Nicolaisen, Antje Janosch
 */
public class BinningCalculateNodeDialog extends AbstractConfigDialog {

    
    public BinningCalculateNodeDialog() {
    	super();
    }
    
    @Override
    protected void createControls() {    
    	
    	this.createNewGroup("Column selection");
        // numerical column selection component
        addDialogComponent(new DialogComponentColumnFilter2(
                BinningCalculateNodeModel.createColumnFilterModel(), 0));
        this.closeCurrentGroup();

        // numeric field to configure number of bins
        DialogComponentNumberEdit nEdit = new DialogComponentNumberEdit(BinningCalculateNodeModel.createBinSelectionModel(), "Number of Bins");
        addDialogComponent(nEdit);
    }
    
    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        // check if the input table contains numeric columns and string columns
        DataTableSpec inSpec = specs[0];
        if (!inSpec.containsCompatibleType(DoubleValue.class))
            throw new NotConfigurableException("input table requires at least one numeric column (Double or Integer)");        
    }
}