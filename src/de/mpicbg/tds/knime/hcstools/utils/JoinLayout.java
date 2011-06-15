package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.core.LayoutUtils;
import de.mpicbg.tds.core.util.StringTable;
import de.mpicbg.tds.knime.HCSAttributeUtils;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.TableUpdateCache;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import java.util.List;
import java.util.Map;


/**
 * This is the model implementation of ExcelWriter.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class JoinLayout extends LoadLayout {


    public JoinLayout() {
        super(1, 1);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];

        String sheetName = propSheetName.getStringValue();
        Map<String, StringTable> layout = LayoutUtils.loadLayout(sheetName, propLayoutFile.getStringValue());

        Attribute rowAttribute = HCSAttributeUtils.getPlateRowAttribute(input);
        Attribute colAttribute = HCSAttributeUtils.getPlateColumnAttribute(input);

        List<Attribute> layoutAttributes = parseHeaders(layout);


        TableUpdateCache cache = new TableUpdateCache(input.getDataTableSpec());
        cache.registerAttributes(layoutAttributes);

        StringTable firstLayoutDim = layout.values().iterator().next();


        // iterate over all layout dimensions
        for (Attribute layoutAttribute : layoutAttributes) {

            StringTable factorTable = layout.get(layoutAttribute.getName());

            // iterate over all  rows and annoate them if possible
            for (DataRow dataRow : input) {

                int plateRow = rowAttribute.getIntAttribute(dataRow);
                int plateCol = colAttribute.getIntAttribute(dataRow);

                cache.add(dataRow, layoutAttribute, layoutAttribute.createCell(factorTable.get(plateRow, plateCol)));
            }
        }

        // build the output-table
        ColumnRearranger c = cache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(input, c, exec);

        return new BufferedDataTable[]{out};
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        String sheetName = propSheetName.getStringValue();
        List<Attribute> layoutAttributes = parseHeaders(LayoutUtils.loadLayout(sheetName, propLayoutFile.getStringValue()));

        TableUpdateCache updateCache = new TableUpdateCache(inSpecs[0]);
        updateCache.registerAttributes(layoutAttributes);

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }
}