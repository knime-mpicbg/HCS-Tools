package de.mpicbg.tds.core.view.model;

import de.mpicbg.tds.core.model.Plate;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: niederle
 * Date: 10/6/11
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateSortByDate implements Comparator<Plate> {

    public int compare(Plate p1, Plate p2) {
        if (p1.getScreenedAt() == null || p2.getScreenedAt() == null) return 0;
        return p1.getScreenedAt().compareTo(p2.getScreenedAt());
    }
}
