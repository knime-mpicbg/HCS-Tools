package de.mpicbg.knime.hcs.base.nodes.img;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class WellFieldImages implements Serializable {

    Map<String, List<File>> dataCache = new HashMap<String, List<File>>();


    public void addWellfieledImages(String rowKey, List<File> files) {
        if (!dataCache.containsKey(rowKey)) {
            throw new RuntimeException("Cache already contains well");
        }

        dataCache.put(rowKey, files);
    }
}
