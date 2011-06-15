package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.utils.ScreenConversionUtils;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
@SuppressWarnings({"unchecked"})
public class ParseEMTData {

    public static void main(String[] args) {

    }


    public BufferedDataTable devScript1(ExecutionContext exec, BufferedDataTable input) {
        Attribute<String> barcodeAttribute = (Attribute) new InputTableAttribute("plateName", input);
        Attribute<String> rowAttribute = (Attribute) new InputTableAttribute("row", input);
        Attribute<Integer> colAttribute = (Attribute) new InputTableAttribute("col", input);
        Attribute<String> imAnAttribute = (Attribute) new InputTableAttribute("imageAnaylsis", input);
        Attribute<String> paramAttribute = (Attribute) new InputTableAttribute("Parameter", input);
        Attribute<Double> readoutValueAttribute = (Attribute) new InputTableAttribute("wellValue", input);


        List<Attribute> layoutColumns = AttributeUtils.convert(input.getDataTableSpec());
        layoutColumns.removeAll(Arrays.asList(
                barcodeAttribute, rowAttribute, colAttribute, imAnAttribute, paramAttribute, readoutValueAttribute,
                new InputTableAttribute("cs_plateid", input), new InputTableAttribute("featureID", input), new InputTableAttribute("barcodeImageAnalysisParameter", input)));

        Map<String, Plate> barcode2Plate = new HashMap<String, Plate>();

        //iterate over all rows
        for (DataRow dataRow : input) {
            String barcode = barcodeAttribute.getValue(dataRow);

            if (!barcode2Plate.containsKey(barcode)) {
                Plate plate = new Plate();
                plate.setBarcode(barcode);
                barcode2Plate.put(barcode, plate);
            }

            Plate plate = barcode2Plate.get(barcode);

            // check if the well is there and create it if necessary
            int plateRow = TdsUtils.mapRowCharToIndex(rowAttribute.getValue(dataRow));
            int plateCol = colAttribute.getIntAttribute(dataRow);

            // query the well from the plate or create if not yet present
            Well well = plate.getWell(plateCol, plateRow);
            if (well == null) {
                well = new Well(plateRow, plateCol);

                // concatenate all the readout information
                StringBuffer layoutInfos = new StringBuffer();
                for (Attribute layoutColumn : layoutColumns) {
                    layoutInfos.append(layoutColumn.getValue(dataRow) + "|");
                }
                well.setDescription(layoutInfos.toString());

                plate.addWell(well);
            }

            String readoutName = imAnAttribute.getNominalAttribute(dataRow) + "-" + paramAttribute.getNominalAttribute(dataRow);
            Double readoutValue = readoutValueAttribute.getDoubleAttribute(dataRow);

            well.getWellStatistics().put(readoutName, readoutValue);
        }


        // convert the plate collection into a knime-table
        return ScreenConversionUtils.convertPlates2KnimeTable(barcode2Plate.values(), exec);
    }

}


