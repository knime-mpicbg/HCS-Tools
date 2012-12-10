package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 12/6/12
 * Time: 21:
 *
 * Property selector combobox to select plate attributes.
 */

public class PlateAttributeComboBox extends JComboBox{


    public  void configure(List<Plate> plates) {
        List<String> plateAttributes = getPlateAttributes(plates); // TODO: This should be solved via the configuration dialog of the node eventually
        DefaultComboBoxModel model = new DefaultComboBoxModel(plateAttributes.toArray());
        setModel(model);
    }


    private List<String> getPlateAttributes(List<Plate> plates) {
        Collection<String> attributes = new HashSet<String>();

        for (Plate plate : plates) {
            Class plateClass = plate.getClass();

            for (Field field : plateClass.getDeclaredFields()) {
                attributes.add(field.getName());
            }
        }

        return new ArrayList<String>(attributes);
    }


}
