package de.mpicbg.tds.knime.hcstools.datamanip.row;

/**
 * Factory of RangeFilter node
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/1/11
 * Time: 2:19 PM
 */

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.NumericFilter;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

public class RangeFilterV2Factory extends NodeFactory<RangeFilterV2Model> {
    //overwrites will be generated here

    @Override
    public RangeFilterV2Model createNodeModel() {
        return new RangeFilterV2Model();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<RangeFilterV2Model> createNodeView(int i, RangeFilterV2Model rangeFilterModel) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean hasDialog() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new AbstractConfigDialog() {

            @Override
            protected void createControls() {

                addDialogComponent(new DialogComponentNumberEdit(createLowerBoundSetting(), "Lower Bound"));

                addDialogComponent(new DialogComponentNumberEdit(createUpperBoundSetting(), "Upper Bound"));

                addDialogComponent(new DialogComponentButtonGroup(createFilterRuleMatchSetting(), false, "Rows match if", RangeFilterV2Model.FILTER_RULE_MATCHALL));

                addDialogComponent(new DialogComponentButtonGroup(createFilterRuleIncludeSetting(), false, "Matching rows", RangeFilterV2Model.FILTER_RULE_INCLUDE));

                addDialogComponent(new DialogComponentColumnFilter(createParameterFilterSetting(), 0, true, new NumericFilter()));
            }
        };
    }

    static SettingsModelDouble createLowerBoundSetting() {
        return new SettingsModelDouble("LowerBoundSetting", Double.NEGATIVE_INFINITY);
    }

    static SettingsModelDouble createUpperBoundSetting() {
        return new SettingsModelDouble("UpperBoundSetting", Double.POSITIVE_INFINITY);
    }

    static SettingsModelFilterString createParameterFilterSetting() {
        return new SettingsModelFilterString("ParameterSetting");
    }

    static SettingsModelString createFilterRuleMatchSetting() {
        return new SettingsModelString("Filter Rule Match", RangeFilterV2Model.FILTER_RULE_MATCHALL[0]);
    }

    static SettingsModelString createFilterRuleIncludeSetting() {
        return new SettingsModelString("Filter Rule Include", RangeFilterV2Model.FILTER_RULE_INCLUDE[0]);
    }
}
