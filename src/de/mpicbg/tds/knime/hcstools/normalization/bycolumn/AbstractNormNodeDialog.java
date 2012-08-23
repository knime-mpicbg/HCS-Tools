package de.mpicbg.tds.knime.hcstools.normalization.bycolumn;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.*;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;
import org.knime.core.node.util.ColumnFilter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Abstract class provides methods to create the node dialog for normalization nodes
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 1:36 PM
 */
public abstract class AbstractNormNodeDialog extends AbstractConfigDialog {
    // Combobox which shows the domain values of a selected reference column
    protected DialogComponentStringSelection refStringDC;
    // Combobox which offers the selection of the reference column
    protected DialogComponentColumnNameSelection refColumnDC;

    // settings model of the reference string
    protected SettingsModelString refStringSM;

    // settings model of number of columns to process at once
    private static SettingsModelNumber procOptSM;
    //settings model if processing options should be used at all
    private static SettingsModelBoolean useProcOptSM;

    /**
     * initialize some dialog components (even if not used)
     */
    @Override
    protected void createControls() {
        refStringSM = AbstractNormNodeModel.createRefStringSM();
        refStringDC = getRefStringDC();
        refColumnDC = getRefColumnDC(0, true, true);

        procOptSM = null;
        useProcOptSM = null;
    }

    /**
     * creates the tab to enable processing options
     */
    protected void addProcessingOptionsTab() {
        createNewTab("Memory options");
        addDialogComponent(getUseProcessingOptionsDC());
        addDialogComponent(getProcessingOptionsDC());
    }

    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        procOptSM.setEnabled(useProcOptSM.getBooleanValue());

        // check if the input table contains numeric columns
        DataTableSpec inSpec = specs[0];
        if (!inSpec.containsCompatibleType(DoubleValue.class))
            throw new NotConfigurableException("input table requires at least one numeric column (Double or Integer)");
        // TODO: string columns are not needed?
        if (!inSpec.containsCompatibleType(StringValue.class))
            throw new NotConfigurableException("input table requires at least one column with nominal values (String)");

        try {
            String refColumn;
            // test whether the reference column already has been chosen; if yes update the content of the combobox containing the domain values
            if (settings.containsKey(AbstractNormNodeModel.CFG_REFCOLUMN)) {
                refColumn = settings.getString(AbstractNormNodeModel.CFG_REFCOLUMN);
                this.setFirstTableSpecs(specs[0]);
                updateSubsetSelector(refColumn);
                boolean componentEnables = refStringSM.isEnabled();
                // reload the setting of the component and restore enabled/disabled property
                refStringDC.loadSettingsFrom(settings, specs);
                refStringSM.setEnabled(componentEnables);
            }
        } catch (InvalidSettingsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * method updates possible reference string values after reference column selection
     *
     * @param refColumnName
     */
    private void updateSubsetSelector(String refColumnName) {
        DataTableSpec currentSpec = this.getFirstSpec();
        List<String> domainList = new ArrayList<String>();
        if (currentSpec != null) {
            // get the reference column
            int colIdx = currentSpec.findColumnIndex(refColumnName);
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
                        refStringDC.replaceListItems(domainList, domainList.get(0));
                        refStringSM.setEnabled(true);
                    }
                }
            } else {
                domainList.add("");
                refStringDC.replaceListItems(domainList, domainList.get(0));
                refStringSM.setEnabled(false);
            }
        }
    }

    /**
     * @return dialog component for replacing values
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponentBoolean getReplaceValuesDC() {
        return new DialogComponentBoolean(AbstractNormNodeModel.createReplaceValuesSM(), "Replace existing values");
    }

    /**
     * @return dialog component for numeric columns
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponentColumnFilter getColumnFilterDC(int inPortIdx) {
        // create filter to include only double and int columns
        ColumnFilter filterDoubleInt = new ColumnFilter() {
            /**
             * @return true, if the given column type is compatible with double
             *         but not with boolean values
             */
            @Override
            public boolean includeColumn(final DataColumnSpec cspec) {
                final DataType type = cspec.getType();
                return (type.isCompatible(DoubleValue.class) && !type.isCompatible(BooleanValue.class));
            }

            /** {@inheritDoc} */
            @Override
            public String allFilteredMsg() {
                return "No double-type columns available. No support of boolean columns.";
            }
        };

        return new DialogComponentColumnFilter(AbstractNormNodeModel.createColumnFilterSM(), inPortIdx, true, filterDoubleInt);
    }

    /**
     * @return dialog component for robust statistics
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponentBoolean getRobustStatsDC() {
        return new DialogComponentBoolean(AbstractNormNodeModel.createRobustStatsSM(), "Use robust statistics (median, mad)");
    }

    /**
     * @return dialog component for aggregation column
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponentColumnNameSelection getAggregationDC(int specIndex, boolean isRequired, boolean addNoneCol) {
        return new DialogComponentColumnNameSelection(AbstractNormNodeModel.createAggregationSM(), "Aggregate object data by", specIndex,
                isRequired, addNoneCol, new Class[]{org.knime.core.data.StringValue.class});
    }

    /**
     * @return dialog component for reference population column
     */
    @SuppressWarnings("unchecked")
    protected DialogComponentColumnNameSelection getRefColumnDC(int specIndex, boolean isRequired, boolean addNoneCol) {
        // settings model with change listener for choice of reference column
        SettingsModelString refColumnSM = AbstractNormNodeModel.createRefColumnSM();
        refColumnSM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updateSubsetSelector(((SettingsModelString) changeEvent.getSource()).getStringValue());
            }
        });
        ColumnFilter filterNominalColumn = new ColumnFilter() {
            @Override
            public boolean includeColumn(DataColumnSpec dataColumnSpec) {
                return dataColumnSpec.getType().isCompatible(NominalValue.class) && dataColumnSpec.getDomain().hasValues();
            }

            @Override
            public String allFilteredMsg() {
                return "No nominal column with domain values available.";
            }
        };

        return new DialogComponentColumnNameSelection(refColumnSM, "Column with reference label", specIndex,
                isRequired, addNoneCol, filterNominalColumn);
    }

    /**
     * @return dialog component for reference population string
     */
    @SuppressWarnings("unchecked")
    protected DialogComponentStringSelection getRefStringDC() {
        return new DialogComponentStringSelection(refStringSM, "subset by:", "");
    }

    /**
     * @return dialog component for suffix string
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponent getSuffixDC() {
        return new DialogComponentOptionalString(AbstractNormNodeModel.createSuffixSM(), "column suffix");
    }

    /**
     * @return dialog component for processing options: number of columns
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponent getProcessingOptionsDC() {
        procOptSM = AbstractNormNodeModel.createProcessingOptionsSM();
        useProcOptSM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                boolean useLowerBound = useProcOptSM.getBooleanValue();
                procOptSM.setEnabled(useLowerBound);
            }
        });
        return new DialogComponentNumberEdit(procOptSM, "", 2);
    }

    /**
     * @return dialog component for processing options: use options?
     */
    @SuppressWarnings("unchecked")
    protected DialogComponent getUseProcessingOptionsDC() {
        useProcOptSM = AbstractNormNodeModel.createUseProcessingOptionsSM();
        return new DialogComponentBoolean(useProcOptSM, "Load a limited number of columns per processing step");
    }
}
