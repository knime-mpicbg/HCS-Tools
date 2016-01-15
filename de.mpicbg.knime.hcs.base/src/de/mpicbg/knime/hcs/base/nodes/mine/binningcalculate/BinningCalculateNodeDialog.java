package de.mpicbg.knime.hcs.base.nodes.mine.binningcalculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
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
 * @author Tim Nicolaisen
 */
public class BinningCalculateNodeDialog extends AbstractConfigDialog {
	
	// Combobox which shows the domain values of a selected reference column
    private DialogComponentStringSelection refLabel;
    // Combobox which offers the selection of the reference column
    private DialogComponentColumnNameSelection refColumncomponent;

    private SettingsModelString refLabelString;
    
    /**
     * New pane for configuring the BinningCalculate node.
     */

    @SuppressWarnings("unchecked")
    public BinningCalculateNodeDialog() {
	super();

	
	/**
	 * New Dialog for choosing Columns for binning
	 */

	addDialogComponent(new DialogComponentColumnFilter2(
		new SettingsModelColumnFilter2("column.filter", new DataTypeColumnFilter(DoubleValue.class),1)
		,0, false));

	
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void createControls() {

        // Group data by
        addDialogComponent(new DialogComponentColumnNameSelection(BinningCalculateNodeModel.createAggregationSelectionModel(), "Aggregate object data by", 0,
                true, false, new Class[]{org.knime.core.data.StringValue.class}));

        // numerical column selection component
        addDialogComponent(new DialogComponentColumnFilter(BinningCalculateNodeModel.createColumnSelectionModel(), 0, true,
                new Class[]{DoubleValue.class}));

        // numeric field to configure number of bins
        DialogComponentNumberEdit nEdit = new DialogComponentNumberEdit(BinningCalculateNodeModel.createBinSelectionModel(), "Number of Bins");
        addDialogComponent(nEdit);

        setHorizontalPlacement(true);

        // settings model with change listener for choice of reference column
        SettingsModelString refColumn = BinningCalculateNodeModel.createRefColumnSelectionModel();
        refColumn.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateSubsetSelector(((SettingsModelString) changeEvent.getSource()).getStringValue());
            }
        });
        
     // combobox to choose reference column
        refColumncomponent = new DialogComponentColumnNameSelection(refColumn, "Column with reference label", 0,
                false, true, new Class[]{org.knime.core.data.StringValue.class});
        addDialogComponent(refColumncomponent);


        refLabelString = BinningCalculateNodeModel.createRefStringSelectionModel();

        // combobox to choose the reference label
        refLabel = new DialogComponentStringSelection(refLabelString, "subset by:", "");
        addDialogComponent(refLabel);
        
        
}
    
    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        // check if the input table contains numeric columns and string columns
        DataTableSpec inSpec = specs[0];
        if (!inSpec.containsCompatibleType(DoubleValue.class))
            throw new NotConfigurableException("input table requires at least one numeric column (Double or Integer)");
        if (!inSpec.containsCompatibleType(StringValue.class))
            throw new NotConfigurableException("input table requires at least one column with nominal values (String)");

        try {
            String refColumn;
            // test whether the reference column already has been chosen; if yes update the content of the combobox containing the domain values
            if (settings.containsKey(BinningCalculateNodeModel.CFG_REFCOLUMN)) {
                refColumn = settings.getString(BinningCalculateNodeModel.CFG_REFCOLUMN);
                this.setFirstTableSpecs(specs[0]);
                updateSubsetSelector(refColumn);
                boolean componentEnables = refLabelString.isEnabled();
                // reload the setting of the component and restore enabled/disabled property
                refLabel.loadSettingsFrom(settings, specs);
                refLabelString.setEnabled(componentEnables);
            }
        } catch (InvalidSettingsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    /**
     * extracts the domain values of the selected reference column and puts it into the combobox
     *
     * @param refColumn
     */
    private void updateSubsetSelector(String refColumn) {
        DataTableSpec currentSpec = this.getFirstSpec();
        List<String> domainList = new ArrayList<String>();
        if (currentSpec != null) {
            // get the reference column
            int colIdx = currentSpec.findColumnIndex(refColumn);
            if (colIdx >= 0) {
                // check if it contains domain values
                Set<DataCell> domainValues = currentSpec.getColumnSpec(colIdx).getDomain().getValues();
                if (domainValues != null) {
                    if (!domainValues.isEmpty()) {
                        // collect all domain values, sort them and put them into the combobox
                        for (DataCell value : domainValues) {
                            domainList.add(((StringCell) value).getStringValue());
                        }
                        Collections.sort(domainList);
                        refLabel.replaceListItems(domainList, domainList.get(0));
                        refLabelString.setEnabled(true);
                    }
                }
            } else {
                domainList.add("");
                refLabel.replaceListItems(domainList, domainList.get(0));
                refLabelString.setEnabled(false);
            }
        }
    }
}