package de.mpicbg.knime.hcs.base.nodes.img;

import de.mpicbg.knime.hcs.base.utils.ArrayScanKnimeUtils;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Well;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class JoinWithArrayScanImages extends AbstractJoinWithImagesModel {


    public static void main(String[] args) throws SQLException {

        // we should with lists right from the beginning to keeping the option of keeping the table order in the view
        List<Well> wells = new ArrayList<Well>(TdsUtils.flattenWells(TdsUtils.readWellsFromCSV(new File("resources/twoArrayScanPlates.csv"))));

        //iterate over each well and try to find the images by quering the database
        buildImageCache(wells);


    }


    private static void buildImageCache(List<Well> wells) throws SQLException {
        // key is the rowID
        Map<String, WellFieldImages> cache = new HashMap<String, WellFieldImages>();

        Connection connection = ArrayScanKnimeUtils.connectToASDB("jdbc:jtds:sqlserver://array-store.mpi-cbg.de:1433;DatabaseName=store");
        for (Well well : wells) {
            WellFieldImages imagesCache = new WellFieldImages();

            well.setId("" + (int) well.getReadout("Plate ID").intValue());

            List<File> images = locateImages(connection, well, null);

            // get all the paths
            imagesCache.addWellfieledImages(well.toString(), images);
        }
    }


    private static List<File> locateImages(Connection connection, Well well, List<Integer> wellFieldIndices) {
        List<File> files = new ArrayList<File>();


        String query = "select well.ID as 'wellID',  well.pCol, well.pRow from well where well.CS_PlateID = '" + well.getPlate().getId() + "' ";

        return null;
    }

    //todo keep the option to do the same thing for image-fields

}
