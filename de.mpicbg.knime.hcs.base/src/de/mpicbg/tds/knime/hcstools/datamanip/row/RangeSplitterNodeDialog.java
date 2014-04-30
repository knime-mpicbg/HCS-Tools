package de.mpicbg.tds.knime.hcstools.datamanip.row;

import de.mpicbg.knime.knutils.AbstractConfigDialog;

import org.knime.core.data.*;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.*;
import org.knime.core.node.util.ColumnFilter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/7/12
 * Time: 1:44 PM
 */
public class RangeSplitterNodeDialog extends AbstractConfigDialog {

    private SettingsModelNumber lowerBoundSM;
    private SettingsModelBoolean useLowerBoundSM;
    private SettingsModelNumber upperBoundSM;
    private SettingsModelBoolean useUpperBoundSM;

    @Override
    @SuppressWarnings("unchecked")
    protected void createControls() {
        setHorizontalPlacement(true);
        lowerBoundSM = RangeSplitterModel.createLowerBoundSetting();
        useLowerBoundSM = new SettingsModelBoolean("dummy", true);
        useLowerBoundSM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                boolean useLowerBound = useLowerBoundSM.getBooleanValue();
                lowerBoundSM.setEnabled(useLowerBound);
                if (!useLowerBound) ((SettingsModelDouble) lowerBoundSM).setDoubleValue(Double.NEGATIVE_INFINITY);
            }
        });
        DialogComponentBoolean useLowerBoundDC = new DialogComponentBoolean(useLowerBoundSM, "");
        addDialogComponent(useLowerBoundDC);
        addDialogComponent(new DialogComponentNumber(lowerBoundSM, "Lower Bound", 0.1));


        upperBoundSM = RangeSplitterModel.createUpperBoundSetting();
        useUpperBoundSM = new SettingsModelBoolean("dummy", true);
        useUpperBoundSM.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                boolean useUpperBound = useUpperBoundSM.getBooleanValue();
                upperBoundSM.setEnabled(useUpperBound);
                if (!useUpperBound) ((SettingsModelDouble) upperBoundSM).setDoubleValue(Double.POSITIVE_INFINITY);
            }
        });
        DialogComponentBoolean useUpperBoundDC = new DialogComponentBoolean(useUpperBoundSM, "");
        addDialogComponent(useUpperBoundDC);
        addDialogComponent(new DialogComponentNumber(upperBoundSM, "Upper Bound", 0.1));
        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentButtonGroup(RangeSplitterModel.createFilterRuleMatchSetting(), false, "Rows match if", RangeSplitterModel.FILTER_RULE_MATCHALL));
        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentButtonGroup(RangeSplitterModel.createFilterRuleMissingSetting(), false, "Do missing values match?", RangeSplitterModel.FILTER_RULE_MISSING));
        setHorizontalPlacement(false);

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

        addDialogComponent(new DialogComponentColumnFilter(RangeSplitterModel.createParameterFilterSetting(), 0, true, filterDoubleInt));
    }

    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        if (((SettingsModelDouble) lowerBoundSM).getDoubleValue() == Double.NEGATIVE_INFINITY) {
            useLowerBoundSM.setBooleanValue(false);
        }
        if (((SettingsModelDouble) upperBoundSM).getDoubleValue() == Double.POSITIVE_INFINITY) {
            useUpperBoundSM.setBooleanValue(false);
        }
    }
}
