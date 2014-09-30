package de.mpicbg.knime.hcs.core.tools;

import de.mpicbg.knime.hcs.core.util.StringTable;

import org.apache.poi.ss.usermodel.Sheet;

import java.awt.*;
import java.io.File;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class EnvisionReaderUtils {

    public static void main(String[] args) {
        File envisionFile = new File("./resources/envision-example 1.xls");
        Sheet sheet = StringTable.openWorkSheet(envisionFile).getSheetAt(0);

        Point bckndTablePos = StringTable.findNextPlatePosition(sheet, new Point(1, 1));
        Point corTablePos = StringTable.findNextPlatePosition(sheet, new Point((int) bckndTablePos.getX(), (int) (bckndTablePos.getY() + 1)));


        Rectangle tableBounds = StringTable.guessPlateBounds(sheet, corTablePos);
        StringTable table = StringTable.readStringGridFromExcel(tableBounds, sheet);

        System.err.println("starting point is" + corTablePos);
        System.err.println("table-bounds are" + tableBounds);
        System.err.println("the table is " + table);
//        envisionFile  = StringTable.readStringGridFromCsv(envisionFile, ",", new Point(2, 4), new Point(25, 19))
    }
}

