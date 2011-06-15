package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.knime.knutils.AbstractConfigDialog;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
public class ConvertPlateRowsFactory extends NodeFactory<ConvertPlateRows> {


    @Override
    public ConvertPlateRows createNodeModel() {
        return new ConvertPlateRows();
    }


    @Override
    public int getNrNodeViews() {
        return 0;
    }


    public NodeView<ConvertPlateRows> createNodeView(final int viewIndex, final ConvertPlateRows nodeModel) {
        return null;
    }


    @Override
    public boolean hasDialog() {
        return true;
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {


        return new AbstractConfigDialog() {
            SettingsModelFilterString plateRowColumnsProp;


            @Override
            public void createControls() {

                plateRowColumnsProp = createConvertColumns();
                DialogComponentColumnFilter filter = new DialogComponentColumnFilter(plateRowColumnsProp, 0, false, new Class[]{StringValue.class, DoubleValue.class, IntValue.class});
                filter.setIncludeTitle("Plate-Row Columns");

                addDialogComponent(filter);
            }


            @Override
            public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
                super.loadAdditionalSettingsFrom(settings, specs);

                AttributeUtils.updateExcludeToNonSelected(specs[0], plateRowColumnsProp);
            }
        };
    }


    public static SettingsModelFilterString createConvertColumns() {
        return new SettingsModelFilterString("target.column");
    }
}