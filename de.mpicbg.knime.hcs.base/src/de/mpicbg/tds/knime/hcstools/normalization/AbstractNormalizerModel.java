package de.mpicbg.tds.knime.hcstools.normalization;

/**
 * The class handles common SettingModels for all normalization nodes
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/20/11
 * Time: 11:04 AM
 */

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.TableUpdateCache;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

public abstract class AbstractNormalizerModel extends AbstractNodeModel {

    // indentifier for configuration settings
    public static final String CFGKEY_REPLACE_VALUES = "inplace";
    public static final String CFGKEY_READOUT_SELECTION = "readouts";
    public static final String CFGKEY_ROBUST_STATS = "use.robust.statistics";


    //    public static final String SCREEN_MODEL_BARCODE = "barcode";
    public static final String CFGKEY_GROUP_WELLS_BY = "group.wells.by";
//    public static final String GROUP_WELLS_BY_DESC = "Group wells by";
//
//
//    public static final String TREATMENT = "Treatment";
//    public static final String TREATMENT_ATTRIBUTE_DESC = "Treatment attribute";
//    public static final String SELECT_TREATMENT_ADVICE = "Select a treatment!";
//    public static final String TREATMENT_ATTRIBTUTE = "treatment.attribute";
//
//    public static final String TREATMENT_LIBRARY = "library";
//
//    public static final String TREATMENT_POS_CONTROL = "PositiveControl";
//    public static final String TREATMENT_NEG_CONTROL = "NegativeControl";
//    public static final String POSITIVE_CONTROL_DESC = "Positive Control";
//    public static final String NEGATIVE_CONTROL_DESC = "Negative Control";

    public AbstractNormalizerModel() {
        super(1, 1);
    }

    /**
     * Constructor for the node model.
     */
    protected AbstractNormalizerModel(int inPorts, int outPorts) {
        super(inPorts, outPorts, true);

        final SettingsModelFilterString readoutSelection = new SettingsModelFilterString(CFGKEY_READOUT_SELECTION);
        final SettingsModelBoolean replaceValues = new SettingsModelBoolean(CFGKEY_REPLACE_VALUES, false);
        final SettingsModelBoolean useRobustStats = new SettingsModelBoolean(CFGKEY_ROBUST_STATS, true);

        addModelSetting(CFGKEY_READOUT_SELECTION, readoutSelection);
        addModelSetting(CFGKEY_REPLACE_VALUES, replaceValues);
        addModelSetting(CFGKEY_ROBUST_STATS, useRobustStats);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        TableUpdateCache updateCache = new TableUpdateCache(inSpecs[0]);
        String suffix = getAttributeNameSuffix();

        SettingsModelFilterString propReadouts = (SettingsModelFilterString) getModelSetting(CFGKEY_READOUT_SELECTION);

        for (String readoutName : propReadouts.getIncludeList()) {
            updateCache.registerAttribute(new Attribute(readoutName + suffix, DoubleCell.TYPE));
        }

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        return new BufferedDataTable[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*public static SettingsModelFilterString createPropReadoutSelection() {
        return new SettingsModelFilterString(CFGKEY_READOUT_SELECTION);
    }


    public static SettingsModelBoolean createPropReplaceValues() {
        return new SettingsModelBoolean(CFGKEY_REPLACE_VALUES, false);
    }


    public static SettingsModelBoolean createPropRobustStats() {
        return new SettingsModelBoolean(CFGKEY_ROBUST_STATS, true);
    }*/

    public String getAttributeNameSuffix() {
        boolean replace = ((SettingsModelBoolean) getModelSetting(CFGKEY_REPLACE_VALUES)).getBooleanValue();
        return replace ? "" : getAppendSuffix();
    }

    protected abstract String getAppendSuffix();
}
