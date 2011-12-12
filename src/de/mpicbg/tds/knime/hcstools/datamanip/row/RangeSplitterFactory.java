package de.mpicbg.tds.knime.hcstools.datamanip.row;
/**
 * Factory of Range Splitter node
 *
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 12/12/11
 * Time: 1:30 PM
 */

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.NumericFilter;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.*;

public class RangeSplitterFactory extends NodeFactory<RangeSplitterModel> {
    //overwrites will be generated here

    @Override
    public RangeSplitterModel createNodeModel() {
        return new RangeSplitterModel();
    }

    @Override
    protected int getNrNodeViews() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NodeView<RangeSplitterModel> createNodeView(int i, RangeSplitterModel rangeSplitterModel) {
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
}
