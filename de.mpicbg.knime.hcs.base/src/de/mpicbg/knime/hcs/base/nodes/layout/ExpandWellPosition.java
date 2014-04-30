package de.mpicbg.knime.hcs.base.nodes.layout;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.knime.knutils.*;
import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Expand a well position columns (containing entries like AC14, B3) into two columns named 'plateRow' and
 * 'plateColumn'. Addtional splitting characters (comma, colons, semicolons) between the letter and the column number
 * will be ignored.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class ExpandWellPosition extends AbstractNodeModel {

    public SettingsModelString propWellPosition = ExpandWellPositionFactory.createWellPosProp();
    private SettingsModelBoolean convertRowChars = ExpandWellPositionFactory.createConvertRowCharsProp();
    private SettingsModelBoolean deleteSourceCol = ExpandWellPositionFactory.createDeleteSourceColProp();


    public ExpandWellPosition() {
        this(1, 1);
    }


    public ExpandWellPosition(int ins, int outs) {
        super(ins, outs);

        addSetting(propWellPosition);
        addSetting(convertRowChars);
        addSetting(deleteSourceCol);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];

        TableUpdateCache updateCache = new TableUpdateCache(inData[0].getDataTableSpec());

        Attribute<String> wellPositionAttribute = (Attribute) new InputTableAttribute(propWellPosition.getStringValue(), inputTable);

        if (deleteSourceCol.getBooleanValue()) {
            updateCache.registerDeleteColumn(wellPositionAttribute.getName());
        }

        Attribute plateRowAttribute = createRowAttribute();
        Attribute plateColumnAttribute = createColumnAttribute();

        StringBuffer errorLog = new StringBuffer();

        // convert all rows in the table
        int rowCounter = 0;

        for (DataRow dataRow : inputTable) {
            String wellPosition = wellPositionAttribute.getValue(dataRow);

            // handle missing values
            if (StringUtils.isBlank(wellPosition)) {
//                updateCache.add(dataRow, plateRowAttribute, DataType.getMissingCell());
                continue;
            }


            // split it
            String regex = "([a-zA-Z]{1,2})[.,:_]{0,2}([\\d]{1,2})";

            Matcher matcher = Pattern.compile(regex).matcher(wellPosition);

            if (!matcher.matches()) {
                errorLog.append("Well position '" + wellPosition + "' in row " + dataRow.getKey());
            }

            if (convertRowChars.getBooleanValue()) {
                int plateRow = TdsUtils.mapPlateRowStringToNumber(matcher.group(1));
                updateCache.add(dataRow, plateRowAttribute, new IntCell(plateRow));

            } else {
                updateCache.add(dataRow, plateRowAttribute, new StringCell(matcher.group(1)));
            }

            int plateColumn = (int) Double.parseDouble(matcher.group(2));
            updateCache.add(dataRow, plateColumnAttribute, new IntCell(plateColumn));

            BufTableUtils.updateProgress(exec, rowCounter, inputTable.getRowCount());
        }


        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(inputTable, c, exec);

        return new BufferedDataTable[]{out};

    }


    private Attribute createColumnAttribute() {
        return new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE);
    }


    private Attribute createRowAttribute() {
        DataType type = convertRowChars.getBooleanValue() ? IntCell.TYPE : StringCell.TYPE;
        return new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, type);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inputSpec = inSpecs[0];

        TableUpdateCache updateCache = new TableUpdateCache(inputSpec);

        Attribute wellPosition = new InputTableAttribute(propWellPosition.getStringValue(), inputSpec);

        if (deleteSourceCol.getBooleanValue()) {
            updateCache.registerDeleteColumn(wellPosition.getName());
        }

        updateCache.registerAttribute(createRowAttribute());
        updateCache.registerAttribute(createColumnAttribute());

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }
}