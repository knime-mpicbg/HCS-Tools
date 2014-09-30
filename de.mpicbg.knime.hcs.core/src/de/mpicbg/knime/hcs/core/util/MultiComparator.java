package de.mpicbg.knime.hcs.core.util;

import java.util.Comparator;
import java.util.Vector;


/**
 * "Inspired by http://en.allexperts.com/q/Java-1046/Sort-2-attributes-collection.htm
 *
 * @author Holger Brandl
 */
@Deprecated
public class MultiComparator<C> implements Comparator<C> {

    Vector<Comparator<C>> comparators;


    public MultiComparator() {
        comparators = new Vector<Comparator<C>>();
    }


    public void addComparator(Comparator<C> c) {
        comparators.add(c);
    }


    public void removeComparator(Comparator<C> c) {
        comparators.remove(c);
    }


    public int compare(C obj1, C obj2) {
        int result, i;
        Comparator c;
        result = 0;
        for (i = 0; i < comparators.size(); i++) {
            c = comparators.elementAt(i);
            result = c.compare(obj1, obj2);
            if (result != 0) break;
        }
        return result;
    }

}
