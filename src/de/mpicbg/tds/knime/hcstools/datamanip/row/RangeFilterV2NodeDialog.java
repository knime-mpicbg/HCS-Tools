package de.mpicbg.tds.knime.hcstools.datamanip.row;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/6/12
 * Time: 3:21 PM
 */
public class RangeFilterV2NodeDialog extends AbstractConfigDialog {

    @Override
    @SuppressWarnings("unchecked")
    protected void createControls() {
        addDialogComponent(new DialogComponentNumberEdit(RangeFilterV2NodeModel.createLowerBoundSetting(), "Lower Bound"));
        addDialogComponent(new DialogComponentNumberEdit(RangeFilterV2NodeModel.createUpperBoundSetting(), "Upper Bound"));

        addDialogComponent(new DialogComponentButtonGroup(RangeFilterV2NodeModel.createFilterRuleMatchSetting(), false, "Rows match if", RangeFilterV2NodeModel.FILTER_RULE_MATCHALL));
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentButtonGroup(RangeFilterV2NodeModel.createFilterRuleIncludeSetting(), false, "Matching rows", RangeFilterV2NodeModel.FILTER_RULE_INCLUDE));
        addDialogComponent(new DialogComponentButtonGroup(RangeFilterV2NodeModel.createFilterRuleMissingSetting(), false, "Do missing values match?", RangeFilterV2NodeModel.FILTER_RULE_MISSING));
        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentColumnFilter(RangeFilterV2NodeModel.createParameterFilterSetting(), 0, true, new Class[]{DoubleValue.class}));
    }
}
