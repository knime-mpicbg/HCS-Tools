package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.knutils.*;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import java.util.List;


/**
 * This is the model implementation of ExcelWriter.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class ConvertPlateRows extends AbstractNodeModel {

    public SettingsModelFilterString propPlateRowColumns = ConvertPlateRowsFactory.createConvertColumns();


    public ConvertPlateRows() {
        this(1, 1);
    }


    public ConvertPlateRows(int ins, int outs) {
        super(ins, outs);

        addSetting(propPlateRowColumns);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];

        List<Attribute> convertPlateRows = AttributeUtils.compileSpecs(propPlateRowColumns.getIncludeList(), inputTable);
        TableUpdateCache updateCache = new TableUpdateCache(inData[0].getDataTableSpec());

        for (DataRow dataRow : inputTable) {

            // iterate over all columns to become converted
            for (Attribute sourceAttribute : convertPlateRows) {

                Attribute targetAttribute = createTargetAttribute(sourceAttribute);
                Object rowDescriptor = sourceAttribute.getValue(dataRow);

                // convert over all rows in the table


                // handle missing values
                if (rowDescriptor == null) {
                    updateCache.add(dataRow, targetAttribute, DataType.getMissingCell());
                    continue;
                }

                String strinigfiedRowDesc = rowDescriptor.toString();

                if (sourceAttribute.isNumerical()) {
                    String convertedRow = TdsUtils.mapIndexToPlateColumn((int) Double.parseDouble(strinigfiedRowDesc));
                    updateCache.add(dataRow, targetAttribute, targetAttribute.createCell(convertedRow));
                } else {
                    int convertedRow = TdsUtils.mapRowCharToIndex(strinigfiedRowDesc);
                    updateCache.add(dataRow, targetAttribute, targetAttribute.createCell(convertedRow));
                }
            }
        }


        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(inputTable, c, exec);

        return new BufferedDataTable[]{out};

    }


    private Attribute createTargetAttribute(Attribute sourceAttribute) {

        boolean isNumber = sourceAttribute.isNumerical();

        return new Attribute(sourceAttribute.getName(), isNumber ? StringCell.TYPE : IntCell.TYPE);
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inputSpec = inSpecs[0];

        TableUpdateCache updateCache = new TableUpdateCache(inputSpec);

        for (String convertCol : propPlateRowColumns.getIncludeList()) {
            updateCache.registerAttribute(createTargetAttribute(new InputTableAttribute(convertCol, inputSpec)));
        }

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }
}