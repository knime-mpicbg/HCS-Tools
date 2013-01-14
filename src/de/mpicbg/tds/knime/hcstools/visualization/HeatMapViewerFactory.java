package de.mpicbg.tds.knime.hcstools.visualization;

import java.util.ArrayList;
import java.util.Arrays;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.*;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.hcstools.utils.TdsNumericFilter;
import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoDialog;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Conventions;
import static de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel.*;

/**
 * Creating the HCS Heat Map Viewer node.
 *
 * @author Holger Brandl (MPI-CBG)
 *
 * TODO: The hiliting works only in one direction (other views catch it if the trellis does modifications)
 * TODO: firing hilite actions from the plate view does not work. (the internal hilites seem to be sync though)
 * TODO: hilite filtering chokes the view
 * TODO: The color map menu does not work properly. It's also missing the icons.
 */

public class HeatMapViewerFactory extends NodeFactory<HeatMapViewerNodeModel> {

    @Override
    public HeatMapViewerNodeModel createNodeModel() {
        return new HeatMapViewerNodeModel();
    }


    @Override
    public int getNrNodeViews() {
        return 1;
    }


    @Override
    public NodeView<HeatMapViewerNodeModel> createNodeView(final int viewIndex, final HeatMapViewerNodeModel nodeModel) {
        return new HeatMapViewerNodeView(nodeModel);
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {

        return new AbstractConfigDialog() {
            SettingsModelFilterString factorSettings;

            @Override
            public void createControls() {
                Class[] factorTypeList = new Class[] { StringValue.class, DoubleValue.class, IntValue.class };
                Class[] groupByTypes = new Class[]{StringValue.class, IntValue.class};
                Class[] rowColTypes = new Class[]{IntValue.class, DoubleValue.class};

                removeTab("Options");

                createNewTab("Readouts");
                addDialogComponent(new DialogComponentColumnFilter(createSettingsModelFilterString(READOUT_SETTING_NAME), 0, false, new TdsNumericFilter()));

                createNewTab("Factors");
                factorSettings = createSettingsModelFilterString(FACTOR_SETTING_NAME);
                addDialogComponent(new DialogComponentColumnFilter(factorSettings, 0, false, factorTypeList));

                createNewTab("Plate");
                createNewGroup("Definition/Identification");
                SettingsModelString groupBySetting = createSettingsModelString(GROUP_BY_SETTING_NAME, Conventions.CBG.BARCODE);
                addDialogComponent(new DialogComponentColumnNameSelection(groupBySetting, "Group Wells By", 0, groupByTypes));
                SettingsModelString rowSetting = createSettingsModelString(PLATE_ROW_SETTING_NAME, Conventions.CBG.WELL_ROW);
                addDialogComponent(new DialogComponentColumnNameSelection(rowSetting, "Plate Row", 0, rowColTypes));
                SettingsModelString columnSetting = createSettingsModelString(PLATE_COLUMN_SETTING_NAME, Conventions.CBG.WELL_COLUMN);
                addDialogComponent(new DialogComponentColumnNameSelection(columnSetting, "Plate Column", 0, rowColTypes));
                createNewGroup("Plate Label");
                SettingsModelString labelSetting = createSettingsModelString(PLATE_LABEL_SETTING_NAME, Conventions.CBG.BARCODE);
                addDialogComponent(new DialogComponentColumnNameSelection(labelSetting, "Column for Plate Labeling", 0, new Class[]{StringValue.class}));

                createNewTab("Controls");
                DialogComponentStringListSelection stringList =
                        new DialogComponentStringListSelection(createSettingModelStringArray(REFERENCE_POPULATIONS_SETTING_NAME), "Reference Groups", new ArrayList<String>(), false, 7);
                AbstractScreenTrafoDialog.setupControlAttributeSelector(this, Arrays.asList(stringList));
                addDialogComponent(stringList);
            }

            @Override
            public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
                super.loadAdditionalSettingsFrom(settings, specs);

                AttributeUtils.updateExcludeToNonSelected(specs[0], factorSettings);
            }
        };
    }


    static SettingsModelStringArray createSettingModelStringArray(String propName) {
        return new SettingsModelStringArray(propName, new String[0]);
    }

    static SettingsModelFilterString createSettingsModelFilterString(String name) {
        SettingsModelFilterString filterString = new SettingsModelFilterString(name);
        filterString.setIncludeList(new String[]{});
        return new SettingsModelFilterString(name);
    }

    static SettingsModelString createSettingsModelString(String name, String attribute) {
        return new SettingsModelString(name, attribute);
    }

}
