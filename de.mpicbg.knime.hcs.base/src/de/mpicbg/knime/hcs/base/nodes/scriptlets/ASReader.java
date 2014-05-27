package de.mpicbg.knime.hcs.base.nodes.scriptlets;

import de.mpicbg.knime.hcs.base.utils.ArrayScanKnimeUtils;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.util.ArrayScanHelper;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ASReader {

    public static BufferedDataTable execute(ExecutionContext exec) throws CanceledExecutionException, SQLException {

        int protocolVerision = 3;
        String protocolName = "MS_ME_EMT_Colony_10x_090412";
        String projectCodeRegExp = "(EM|PCP)";


        // 1) Connect to the database
        Connection connection = ArrayScanKnimeUtils.connectToASDB("jdbc:jtds:sqlserver://tds-array-store.mpi-cbg.de:1433;DatabaseName=store");


        // 2) get a list of plates to be queried (and filter it if necessary
        List<Plate> plates = ArrayScanHelper.getPlateSelection(connection, protocolName, protocolVerision);


        // 3) get all possible features and create a subset if necessary
        HashMap<Integer, String> readoutSelection = ArrayScanHelper.getSelectedProtocolFeatures(connection, protocolName, protocolVerision);
        ArrayScanHelper.fetchReadoutData(connection, plates, readoutSelection);


        // 4) convert into knime-table
        BufferedDataContainer container = ArrayScanKnimeUtils.createTable(exec, plates, true);

        return container.getTable();
    }


    public static BufferedDataTable loadPlatesWithFeatures(ExecutionContext exec, BufferedDataTable input, BufferedDataTable input2, boolean pruneBioAppName) throws CanceledExecutionException, SQLException {
        return loadPlatesWithFeatures(exec, input, input2, "jdbc:jtds:sqlserver://array-store.mpi-cbg.de:1433;DatabaseName=store", pruneBioAppName);
    }


    public static BufferedDataTable loadPlatesWithFeatures(ExecutionContext exec, BufferedDataTable input, BufferedDataTable input2) throws CanceledExecutionException, SQLException {
        return loadPlatesWithFeatures(exec, input, input2, "jdbc:jtds:sqlserver://array-store.mpi-cbg.de:1433;DatabaseName=store", true);
    }


    public static BufferedDataTable loadPlatesWithFeatures(ExecutionContext exec, BufferedDataTable input, BufferedDataTable input2, String dbURL) throws CanceledExecutionException, SQLException {
        return loadPlatesWithFeatures(exec, input, input2, dbURL, true);
    }


    public static BufferedDataTable loadPlatesWithFeatures(ExecutionContext exec, BufferedDataTable input, BufferedDataTable input2, String dbURL, boolean pruneBioAppName) throws CanceledExecutionException, SQLException {

        // 1) create the plates
        Attribute<Integer> csPlateID = (Attribute) new InputTableAttribute("CS_PlateId", input);
        Attribute<String> barcode = (Attribute) new InputTableAttribute("Barcode", input);

        List<Plate> plates = new ArrayList<Plate>();
        for (DataRow dataRow : input) {
            Plate plate = new Plate();
            plate.setId(csPlateID.getValue(dataRow) + "");
            plate.setBarcode(barcode.getValue(dataRow));

            plates.add(plate);
        }


        // 2) populate the plates with features
        Attribute<String> featureID = (Attribute) new InputTableAttribute("featureID", input2);
        Attribute<String> featureDesc = (Attribute) new InputTableAttribute("description", input2);

        HashMap<Integer, String> readoutSelection = new HashMap<Integer, String>();
        for (DataRow dataRow : input2) {
            readoutSelection.put(featureID.getIntAttribute(dataRow), featureDesc.getRawValue(dataRow));
        }


        Connection connection = ArrayScanKnimeUtils.connectToASDB(dbURL);
        int plateCounter = 0;
        for (Plate plate : plates) {
            ArrayScanHelper.fetchReadoutData(connection, Arrays.asList(plate), readoutSelection);
            BufTableUtils.updateProgress(exec, plateCounter++, plates.size());
        }


        BufferedDataContainer container = ArrayScanKnimeUtils.createTable(exec, plates, pruneBioAppName);

        return container.getTable();
    }

}
