package de.mpicbg.knime.hcs.base.nodes.norm;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.TdsUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * Document me!
 * test commit
 *
 * @author Holger Brandl
 */
public abstract class AbstractScreenTrafoModel extends AbstractNodeModel {


    public static final String SCREEN_MODEL_BARCODE = "barcode";        // TODO this should not be here, rather in in TdsUtils (keep the SCREEN_MODEL variables at one place)
    public static final String GROUP_WELLS_BY_DEFAULT = "barcode";
    public static final String GROUP_WELLS_BY_PROPERTY = "group.wells.by";
    public static final String GROUP_WELLS_BY_DESC = "Group wells by";


    public static final String TREATMENT = "Choose subset(s)";
    public static final String TREATMENT_ATTRIBUTE_DESC = "Select subset column";
    public static final String SELECT_TREATMENT_ADVICE = "Select a subset!";
    public static final String TREATMENT_ATTRIBTUTE = "treatment.attribute";

    public static final String TREATMENT_LIBRARY = "library";

    public static final String TREATMENT_POS_CONTROL = "PositiveControl";
    public static final String TREATMENT_NEG_CONTROL = "NegativeControl";
    public static final String POSITIVE_CONTROL_DESC = "Positive Control";
    public static final String NEGATIVE_CONTROL_DESC = "Negative Control";


    public static final String REPLACE_VALUES = "inplace";
    public static final String READOUT_SELECTION = "readouts";

    protected SettingsModelBoolean propReplaceValues = createPropReplaceValues();
    protected SettingsModelFilterString propReadouts = createPropReadoutSelection();
    protected SettingsModelString groupBy = createWellGroupingAttribute();


    public final static String ROBUST_STATS_PROPERTY_DESCS = "Use robust statistics (median, mad)";


    /**
     * Constructor for the node model.
     */
    protected AbstractScreenTrafoModel(int inPorts, int outPorts) {
        super(inPorts, outPorts);
        addSetting(propReplaceValues);
        addSetting(propReadouts);
        addSetting(groupBy);
    }

    protected AbstractScreenTrafoModel() {
        this(1, 1);

    }


    public static SettingsModelFilterString createPropReadoutSelection() {
        return new SettingsModelFilterString(READOUT_SELECTION);
    }


    public static SettingsModelBoolean createPropReplaceValues() {
        return new SettingsModelBoolean(AbstractScreenTrafoModel.REPLACE_VALUES, false);
    }


    public static SettingsModelBoolean createPropRobustStats() {
        return new SettingsModelBoolean("use.robust.statistics", true);
    }


    public static SettingsModelString createWellGroupingAttribute() {
        return new SettingsModelString(GROUP_WELLS_BY_PROPERTY, GROUP_WELLS_BY_DEFAULT);
    }


    public static SettingsModelString createTreatmentSelector(String propertyName) {
        return new SettingsModelString(propertyName, SELECT_TREATMENT_ADVICE);
    }


    public static SettingsModelString createTreatmentAttributeSelector() {
        return new SettingsModelString(TREATMENT_ATTRIBTUTE, TdsUtils.SCREEN_MODEL_TREATMENT);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        TableUpdateCache updateCache = new TableUpdateCache(inSpecs[0]);
        String suffix = getAttributeNameSuffix();

        for (String readoutName : propReadouts.getIncludeList()) {
            updateCache.registerAttribute(new Attribute(readoutName + suffix, DoubleCell.TYPE));
        }

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }


    public String getAttributeNameSuffix() {
        return propReplaceValues.getBooleanValue() ? "" : getAppendSuffix();
    }


    protected abstract String getAppendSuffix();


    public static String getAndValidateTreatment(SettingsModelString treatmentType) {
        String selectedTreatment = treatmentType.getStringValue();

        if (SELECT_TREATMENT_ADVICE.equals(selectedTreatment))
            throw new RuntimeException("treatment selection is missing");

        return selectedTreatment;
    }


}

