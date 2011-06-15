package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import net.sourceforge.jtds.jdbc.Driver;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ArrayScanKnimeUtils {

    public static BufferedDataContainer createTable(ExecutionContext exec, List<Plate> plates, boolean pruneBioAppName) throws CanceledExecutionException {

        Attribute barcode = new Attribute<String>(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE);
        Attribute csPlateID = new Attribute<String>("Plate ID", IntCell.TYPE);
        Attribute<Integer> plateRow = new Attribute<Integer>(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE);
        Attribute<Integer> plateColumn = new Attribute<Integer>(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE);


        List<Attribute> readouts = new ArrayList<Attribute>();


        List<Attribute> attributes = new ArrayList<Attribute>(Arrays.asList(barcode, csPlateID, plateRow, plateColumn));
        Map<String, String> short2LongAttrName = new HashMap<String, String>();

        for (String readoutName : TdsUtils.flattenReadoutNames(plates)) {
            short2LongAttrName.put(readoutName, readoutName);

            if (pruneBioAppName) { // which are always separated by a colon
                if (readoutName.contains(":")) {
                    String shortReadoutName = readoutName.split(":")[1];

                    // replace the mapping in the list
                    short2LongAttrName.put(shortReadoutName, readoutName);
                    readoutName = shortReadoutName;
                }
            }

            readouts.add(new Attribute(readoutName, DoubleCell.TYPE));
        }

        attributes.addAll(readouts);

        Collection<Well> wells = TdsUtils.flattenWells(plates);

        BufferedDataContainer container = exec.createDataContainer(AttributeUtils.compileTableSpecs(attributes));

        int rowCounter = 0;
        for (Well well : wells) {
            DataCell[] cells = new DataCell[attributes.size()];

            cells[0] = new StringCell(well.getPlate().getBarcode());
            cells[1] = new IntCell(Integer.parseInt(well.getPlate().getId()));
            cells[2] = new IntCell(well.getPlateRow());
            cells[3] = new IntCell(well.getPlateColumn());

            for (int i = 0; i < readouts.size(); i++) {
                Attribute attribute = readouts.get(i);

                Double readout = well.getReadout(short2LongAttrName.get(attribute.getName()));
                if (readout == null) {
                    cells[4 + i] = DataType.getMissingCell();
                } else {
                    cells[4 + i] = new DoubleCell(readout);
                }
            }

            DataRow row = new DefaultRow(new RowKey("Row " + rowCounter++), cells);
            container.addRowToTable(row);
        }

        container.close();
        return container;
    }


    public static Connection connectToASDB(String url) throws SQLException {
        DriverManager.registerDriver(new Driver());
        return DriverManager.getConnection(url, "tdsuser-ro", "opera.2");
    }
}
