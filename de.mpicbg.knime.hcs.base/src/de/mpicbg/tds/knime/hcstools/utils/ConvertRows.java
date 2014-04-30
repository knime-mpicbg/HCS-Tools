package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.core.TdsUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * @author Holger Brandl (MPI-CBG)
 */
@Deprecated
// this class has been remove from the node repository. Use ConvertRowChars instead
public class ConvertRows extends AbstractNodeModel {

    public SettingsModelString propTargetColumn = ConvertRowsFactory.createTargetColumn();
    public SettingsModelString propConversionType = ConvertRowsFactory.createConversionType();


    public ConvertRows() {
        this(1, 1);
    }


    public ConvertRows(int ins, int outs) {
        super(ins, outs);

        addSetting(propTargetColumn);
        addSetting(propConversionType);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];

        Attribute sourceAttribute = new InputTableAttribute(propTargetColumn.getStringValue(), inputTable);
        TableUpdateCache updateCache = new TableUpdateCache(inData[0].getDataTableSpec());

        boolean letters2numbers = propConversionType.getStringValue().equals(ConvertRowsFactory.LET2NUM);
        Attribute targetAttribute = createTargetAttribute(letters2numbers);


        for (DataRow dataRow : inputTable) {
            Object rowDescriptor = sourceAttribute.getValue(dataRow);

            // handle missing values
            if (rowDescriptor == null) {
                updateCache.add(dataRow, targetAttribute, DataType.getMissingCell());
                continue;
            }

            String strinigfiedRowDesc = rowDescriptor.toString();

            if (letters2numbers) {
                int convertedRow = TdsUtils.mapPlateRowStringToNumber(strinigfiedRowDesc);
                updateCache.add(dataRow, targetAttribute, targetAttribute.createCell(convertedRow));
            } else {
                String convertedRow = TdsUtils.mapPlateRowNumberToString((int) Double.parseDouble(strinigfiedRowDesc));
                updateCache.add(dataRow, targetAttribute, targetAttribute.createCell(convertedRow));
            }
        }

        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(inputTable, c, exec);

        return new BufferedDataTable[]{out};

    }


    private Attribute createTargetAttribute(boolean letters2numbers) {
        Attribute targetAttribute;
        if (letters2numbers) {
            targetAttribute = new Attribute(propTargetColumn.getStringValue(), IntCell.TYPE);

        } else {
            targetAttribute = new Attribute(propTargetColumn.getStringValue(), StringCell.TYPE);
        }

        return targetAttribute;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean letters2numbers = propConversionType.getStringValue().equals(ConvertRowsFactory.LET2NUM);
        Attribute targetAttribute = createTargetAttribute(letters2numbers);

        TableUpdateCache updateCache = new TableUpdateCache(inSpecs[0]);
        updateCache.registerAttribute(targetAttribute);

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }
}