package de.mpicbg.tds.knime.hcstools.populationanalysis;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <code>NodeDialog</code> for the "BinningAnalysis" Node.
 * description will be done later
 * <p/>
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author MPI-CBG
 */
public class BinningAnalysisNodeDialog extends AbstractConfigDialog {

    // Combobox which shows the domain values of a selected reference column
    private DialogComponentStringSelection refLabel;
    // Combobox which offers the selection of the reference column
    private DialogComponentColumnNameSelection refColumncomponent;

    private SettingsModelString refLabelString;

    /**
     * New pane for configuring the BinningAnalysis node.
     */
    protected BinningAnalysisNodeDialog() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void createControls() {

        // Group data by
        addDialogComponent(new DialogComponentColumnNameSelection(BinningAnalysisNodeModel.createAggregationSelectionModel(), "Aggregate object data by", 0,
                true, false, new Class[]{org.knime.core.data.StringValue.class}));

        // numerical column selection component
        addDialogComponent(new DialogComponentColumnFilter(BinningAnalysisNodeModel.createColumnSelectionModel(), 0, true,
                new Class[]{DoubleValue.class}));

        // numeric field to configure number of bins
        DialogComponentNumberEdit nEdit = new DialogComponentNumberEdit(BinningAnalysisNodeModel.createBinSelectionModel(), "Number of Bins");
        addDialogComponent(nEdit);

        setHorizontalPlacement(true);

        // settings model with change listener for choice of reference column
        SettingsModelString refColumn = BinningAnalysisNodeModel.createRefColumnSelectionModel();
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


        refLabelString = BinningAnalysisNodeModel.createRefStringSelectionModel();

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
            if (settings.containsKey(BinningAnalysisNodeModel.CFG_REFCOLUMN)) {
                refColumn = settings.getString(BinningAnalysisNodeModel.CFG_REFCOLUMN);
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

