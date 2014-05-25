package de.mpicbg.knime.hcs.core.view.model;

import de.mpicbg.knime.hcs.core.model.Plate;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: niederle
 * Date: 10/6/11
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateSortByPlateNumber implements Comparator<Plate> {
    public int compare(Plate p1, Plate p2) {

        if (p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null) return 0;
        // debug values
        System.out.println("Is Plate " + p1.getLibraryPlateNumber() + " < " + p2.getLibraryPlateNumber());
        if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) < 0) System.out.println("yes");
        if (p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber()) > 0) System.out.println("no");

        if (!(p1.getLibraryPlateNumber() == null || p2.getLibraryPlateNumber() == null)) {
            return p1.getLibraryPlateNumber().compareTo(p2.getLibraryPlateNumber());
        }
        return 0;
    }
}
