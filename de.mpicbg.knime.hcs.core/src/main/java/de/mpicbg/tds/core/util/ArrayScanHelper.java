package de.mpicbg.tds.core.util;

import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ArrayScanHelper {


    private static void findWells(Connection connection, List<Plate> plates) {

        try {
            Statement st = connection.createStatement();

            // iterate over all plates and find the wells
            for (Plate plate : plates) {

//                String query = "select well.ID as 'wellID',  well.pCol, well.pRow from well where well.CS_PlateID = '460'";
                String query = "select well.ID as 'wellID',  well.pCol, well.pRow from well where well.CS_PlateID = '" + plate.getId() + "' ";
                ResultSet resultSet = st.executeQuery(query);

                while (resultSet.next()) {
                    Well well = new Well();

                    well.setPlate(plate);
                    well.setId("" + resultSet.getInt("wellID"));

                    well.setPlateRow(1 + resultSet.getInt("pRow"));
                    well.setPlateColumn(1 + resultSet.getInt("pCol"));

                    plate.getWells().add(well);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void fetchReadoutData(Connection connection, List<Plate> plates, HashMap<Integer, String> featureTypeIDSelection) {
        // first populate all plates with wells
        findWells(connection, plates);

        try {
            Statement st = connection.createStatement();

            // iterate over all plates and find the wells
            for (Plate plate : plates) {

// SELECT wellfeature.valdbl as "wellValue",  wellfeature.typeID as 'featureID' from well, wellfeature where well.CS_PlateID = 460  and well.ID = 60386 and wellfeature.wellID = well.ID

                for (Well well : plate.getWells()) {

                    String query = "SELECT wellfeature.valdbl as 'wellValue',  wellfeature.typeID as 'featureID' from well, wellfeature where well.CS_PlateID = '" + plate.getId() + "' and well.ID = '" + well.getId() + "' and wellfeature.wellID = well.ID ";
                    ResultSet resultSet = st.executeQuery(query);


                    while (resultSet.next()) {
                        int featureTypeID = resultSet.getInt("featureID");
                        double value = resultSet.getDouble("wellValue");


                        if (featureTypeIDSelection.containsKey(featureTypeID)) {
                            well.getWellStatistics().put(featureTypeIDSelection.get(featureTypeID), value);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Plate> getPlateSelection(Connection connection, String protocolName, int protocolVersion) {
        List<Plate> plates = new ArrayList<Plate>();

        try {
            Statement st = connection.createStatement();

//  SELECT protocol.name, protocol.protocolVersion, plate.cs_plateid, plate.PlateBarCode as 'barcode' FROM protocol, plate where protocol.name = 'MS_ME_EMT_Colony_10x_090412' and protocol.protocolVersion= 3 and plate.protocolID = protocol.ID
            String query = "SELECT protocol.name, protocol.protocolVersion, plate.cs_plateid, plate.PlateBarCode as 'barcode' FROM protocol, plate where protocol.name = '" + protocolName + "' and protocol.protocolVersion= '" + protocolVersion + "' and plate.protocolID = protocol.ID";
            ResultSet resultSet = st.executeQuery(query);

            while (resultSet.next()) {
                Plate plate = new Plate();
                plate.setBarcode(resultSet.getString("barcode"));
                plate.setId(resultSet.getString("cs_plateid"));

                plates.add(plate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plates;
    }


    /**
     * Given some plates and a protocol it is necessary to allow the user to select a few plates.
     * <p/>
     * There are 3 ids in the feature
     *
     * @param connection
     * @param protocolVersion
     */
    public static HashMap<Integer, String> getSelectedProtocolFeatures(Connection connection, String protocolName, int protocolVersion) {
        HashMap<Integer, String> selProtocolReadouts = new HashMap<Integer, String>();


        try {
            Statement st = connection.createStatement();


//select protocolwellfeature.featureID, featureType.description
//from protocolwellfeature,  protocol, featureType
//where protocol.protocolVersion = 3 and
//protocol.name ='MS_ME_EMT_Colony_10x_090412' and
//protocol.id = protocolwellfeature.protocolID and
//featureType.ID  = protocolwellfeature.featureID
//

            String table = "select protocolwellfeature.featureID, featureType.description \n" +
                    "from protocolwellfeature,  protocol, featureType  \n" +
                    "where protocol.protocolVersion = '" + protocolVersion + "' and \n" +
                    "protocol.name ='" + protocolName + "' and \n" +
                    "protocol.id = protocolwellfeature.protocolID and \n" +
                    "featureType.ID  = protocolwellfeature.featureID ";

            ResultSet resultSet = st.executeQuery(table);

            while (resultSet.next()) {
                selProtocolReadouts.put(resultSet.getInt("featureID"), resultSet.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return selProtocolReadouts;
    }
}
