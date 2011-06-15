package de.mpicbg.tds.knime.hcstools.scriptlets;

import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.InputTableAttribute;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class SplitRows {

    public static BufferedDataTable devScript1(ExecutionContext exec, BufferedDataTable input) throws CanceledExecutionException {

        Attribute<String> locDetAttribute = (Attribute) new InputTableAttribute("Location Details", input);
        Attribute<String> locAttribute = (Attribute) new InputTableAttribute("Location", input);
        Attribute<String> parLocAttribute = (Attribute) new InputTableAttribute("Parent Location", input);
        Attribute<String> parParLocAttribute = (Attribute) new InputTableAttribute("Parent Parent Location", input);

        List<Attribute> colAttributes = AttributeUtils.convert(input.getDataTableSpec());

        BufferedDataContainer container = exec.createDataContainer(input.getDataTableSpec());

        int rowCounter = 0;
        for (DataRow dataRow : input) {
            String value = locDetAttribute.getValue(dataRow);

            if (value == null) {
                value = "";
            }

            value = value.trim();

            if (!value.trim().isEmpty()) {
                for (String locDetail : value.split(";")) {
                    locDetail = locDetail.trim().replace(" ", "");

                    DataCell[] cells = de.mpicbg.tds.knime.hcstools.scriptlets.SplitRows.duplicateLine(input, colAttributes, dataRow);

                    String freezerLetter = locDetail.substring(0, 1);

                    // first set the new location details
                    Matcher matcher = Pattern.compile("[AB]{1}[-]*([\\d]{1,2})-([\\d]{1,2})").matcher(locDetail);
                    if (!matcher.matches()) {
                        throw new RuntimeException("Does not match");
                    }
                    String boxNr = matcher.group(1);
                    String boxPosOnly = matcher.group(2);

                    cells[locDetAttribute.getColumnIndex()] = new StringCell(boxPosOnly);

                    // now put the freezer in the right column
                    cells[parParLocAttribute.getColumnIndex()] = new StringCell(locAttribute.getNominalAttribute(dataRow).replace("A", freezerLetter));

                    // and finally add the rack according the the freezer
                    if (freezerLetter.equals("A")) {
                        cells[parLocAttribute.getColumnIndex()] = new StringCell("Rack 02");
                    } else if (freezerLetter.equals("B")) {
                        cells[parLocAttribute.getColumnIndex()] = new StringCell("Rack 05");
                    } else {
                        throw new RuntimeException("Unexcpected freezer letter");
                    }

                    cells[locAttribute.getColumnIndex()] = new StringCell("Box " + boxNr);


                    RowKey nextRowKey = new RowKey("Row " + rowCounter++);
                    DataRow row = new DefaultRow(nextRowKey, cells);
                    container.addRowToTable(row);
                }

            } else {
                DataCell[] cells = de.mpicbg.tds.knime.hcstools.scriptlets.SplitRows.duplicateLine(input, colAttributes, dataRow);

                cells[locAttribute.getColumnIndex()] = new StringCell("");
                RowKey nextRowKey = new RowKey("Row " + rowCounter++);
                DataRow row = new DefaultRow(nextRowKey, cells);
                container.addRowToTable(row);
            }
        }

        container.close();
        return container.getTable();
    }


    private static DataCell[] duplicateLine(BufferedDataTable input, List<Attribute> colAttributes, DataRow dataRow) {
        DataCell[] cells = new DataCell[input.getDataTableSpec().getNumColumns()];

        for (Attribute colAttribute : colAttributes) {
            cells[colAttribute.getColumnIndex()] = dataRow.getCell(colAttribute.getColumnIndex());
        }
        return cells;
    }
}
