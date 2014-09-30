package de.mpicbg.knime.hcs.base.nodes.norm;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.knime.core.data.*;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;
import org.knime.core.node.util.ColumnFilter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

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
    //protected DialogComponentStringSelection refStringDC;
    // Combobox which offers the selection of the reference column
    protected DialogComponentColumnNameSelection refColumnDC;

    // settings model of the reference string
    //protected SettingsModelString refStringSM;

    protected HashMap<String, DialogComponentStringSelection> refStringDCList;
    protected HashMap<String, SettingsModelString> refStringSMList;

    // settings model of number of columns to process at once
    private static SettingsModelNumber procOptSM;
    //settings model if processing options should be used at all
    private static SettingsModelBoolean useProcOptSM;

    /**
     * initialize some dialog components (even if not used)
     */
    @Override
    protected void createControls() {
        init("subset by:", false, true);
    }

    /**
     * allows to create node dialog with a different setup for the reference column dialog component
     *
     * @param refLabel
     * @param refColumnRequired
     * @param refColumnNone
     */
    protected void createControls(String refLabel, boolean refColumnRequired, boolean refColumnNone) {
        init(refLabel, refColumnRequired, refColumnNone);
    }

    /**
     * initialize some dialog components
     *
     * @param refLabel
     * @param refColumnrequired
     * @param refColumnNone
     */
    private void init(String refLabel, boolean refColumnrequired, boolean refColumnNone) {

        refStringDCList = new HashMap<String, DialogComponentStringSelection>();
        refStringSMList = new HashMap<String, SettingsModelString>();

        addRefStringSM(AbstractNormNodeModel.CFG_REFSTRING, AbstractNormNodeModel.createRefStringSM(AbstractNormNodeModel.CFG_REFSTRING));
        addRefStringDC(AbstractNormNodeModel.CFG_REFSTRING, getRefStringDC(refStringSMList.get(AbstractNormNodeModel.CFG_REFSTRING), refLabel));

        refColumnDC = getRefColumnDC(0, refColumnrequired, refColumnNone);

        procOptSM = null;
        useProcOptSM = null;
    }

    /**
     * a new reference column dialog component is added
     *
     * @param key
     * @param refStringDC
     */
    protected void addRefStringDC(String key, DialogComponentStringSelection refStringDC) {
        refStringDCList.put(key, refStringDC);
    }

    /**
     * a new reference column setting model is added
     *
     * @param key
     * @param refStringSM
     */
    protected void addRefStringSM(String key, SettingsModelString refStringSM) {
        refStringSMList.put(key, refStringSM);
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


        try {
            String refColumn;
            // test whether the reference column already has been chosen; if yes update the content of the combobox containing the domain values
            if (settings.containsKey(AbstractNormNodeModel.CFG_REFCOLUMN)) {
                refColumn = settings.getString(AbstractNormNodeModel.CFG_REFCOLUMN);
                this.setFirstTableSpecs(specs[0]);
                updateSubsetSelector(refColumn);

                for (String key : refStringDCList.keySet()) {
                    boolean componentEnables = refStringSMList.get(key).isEnabled();
                    refStringDCList.get(key).loadSettingsFrom(settings, specs);
                    refStringSMList.get(key).setEnabled(componentEnables);
                }
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
        boolean enableList = false;
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
                    }
                }
                if (!domainList.isEmpty()) {
                    enableList = true;
                } else {
                    domainList.add("");

                }
            } else {
                domainList.add("");
            }

            Collections.sort(domainList);
            for (String key : refStringDCList.keySet()) {
                refStringDCList.get(key).replaceListItems(domainList, domainList.get(0));
                refStringSMList.get(key).setEnabled(enableList);
                if (!enableList)
                    refStringSMList.get(key).setStringValue(null);
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
        return new DialogComponentColumnNameSelection(AbstractNormNodeModel.createAggregationSM(), "Group data by", specIndex,
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
        /*ColumnFilter filterNominalColumn = new ColumnFilter() {
            @Override
            public boolean includeColumn(DataColumnSpec dataColumnSpec) {
                return dataColumnSpec.getType().isCompatible(NominalValue.class) && dataColumnSpec.getDomain().hasValues();
            }

            @Override
            public String allFilteredMsg() {
                return "No nominal column with domain values available.";
            }
        };*/

        return new DialogComponentColumnNameSelection(refColumnSM, "Column with reference label", specIndex,
                isRequired, addNoneCol, new Class[]{NominalValue.class});
    }

    /**
     * @return dialog component for reference population string
     */
    @SuppressWarnings("unchecked")
    protected DialogComponentStringSelection getRefStringDC(SettingsModelString settingsModel, String label) {
        // combobox has to be editable to provide
        return new DialogComponentStringSelection(settingsModel, label, Arrays.asList(""), true);
    }

    /**
     * @return dialog component for suffix string
     */
    @SuppressWarnings("unchecked")
    protected static DialogComponent getSuffixDC(String cgfSuffixDft) {
        return new DialogComponentOptionalString(AbstractNormNodeModel.createSuffixSM(cgfSuffixDft), "column suffix");
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
