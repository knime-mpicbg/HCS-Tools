package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.DomainCacheAttribute;
import de.mpicbg.knime.knutils.Utils;
import de.mpicbg.tds.core.LayoutUtils;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.util.StringTable;
import de.mpicbg.knime.knutils.*;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This is the model implementation of ExcelWriter.
 *
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class LoadLayout extends AbstractNodeModel {

    public SettingsModelString propLayoutFile = LoadLayoutFactory.createLayoutFileChooser();
    public SettingsModelString propSheetName = LoadLayoutFactory.createSheetName();


    public LoadLayout() {
        this(0, 1);
    }


    public LoadLayout(int ins, int outs) {
        super(ins, outs);

        addSetting(propLayoutFile);
        addSetting(propSheetName);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        String sheetName = propSheetName.getStringValue();
        Map<String, StringTable> layoutDimensions = LayoutUtils.loadLayout(sheetName, propLayoutFile.getStringValue());


        // 2) Create a table

        Attribute rowAttribute = new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE);
        Attribute colAttribute = new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE);


        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(rowAttribute);
        attributes.add(colAttribute);

        List<Attribute> layoutAttributes = parseHeaders(layoutDimensions);
        attributes.addAll(layoutAttributes);

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(attributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        StringTable firstLayoutDim = layoutDimensions.values().iterator().next();


        int rowCounter = 0;
        for (int colIndex = 1; colIndex < firstLayoutDim.getWidth(); colIndex++) {
            for (int rowIndex = 1; rowIndex < firstLayoutDim.getHeight(); rowIndex++) {
                DataCell[] knimeRow = new DataCell[attributes.size()];


                // if there's no treatment we also do NOT add any layout-well to the table
                if (firstLayoutDim.get(rowIndex, colIndex).trim().length() == 0) {
                    continue;
                }

                int attributeCounter = 0;

                knimeRow[attributeCounter++] = rowAttribute.createCell(rowIndex);
                knimeRow[attributeCounter++] = colAttribute.createCell(colIndex);


                for (String attributeName : layoutDimensions.keySet()) {
                    Attribute attribute = AttributeUtils.find(attributes, attributeName);

                    StringTable table = layoutDimensions.get(attributeName);
                    String value = table.get(rowIndex, colIndex);

                    knimeRow[attributeCounter++] = attribute.createCell(value);
                }

                DataRow tableRow = new DefaultRow(new RowKey((rowCounter++) + ""), knimeRow);
                container.addRowToTable(tableRow);
            }
        }

        container.close();
        return new BufferedDataTable[]{container.getTable()};

    }


    protected List<Attribute> parseHeaders(Map<String, StringTable> layout) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        for (String factorName : layout.keySet()) {

            Class intStrOrDouble = layout.get(factorName).guessType(true);
            if (intStrOrDouble == null) {
                throw new RuntimeException("Empty factor '" + factorName + "' is unlikely to have meaningful semantics");
            }

            DataType type = Utils.mapType(intStrOrDouble);

            attributes.add(new DomainCacheAttribute(factorName, type));
        }

        return attributes;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        String sheetName = propSheetName.getStringValue();
        List<Attribute> layoutAttributes = new ArrayList<Attribute>();

        layoutAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
        layoutAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));
        layoutAttributes.addAll(parseHeaders(LayoutUtils.loadLayout(sheetName, propLayoutFile.getStringValue())));

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(layoutAttributes)};
    }
}