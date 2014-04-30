package de.mpicbg.tds.core.view;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.Collection;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class AvgerageZStack {

    private Collection<Plate> curPlateSelection;


    public AvgerageZStack(List<Plate> curPlateSelection) {
        this.curPlateSelection = curPlateSelection;
        Plate avgZStack = new Plate();
        avgZStack.setBarcode("avgz-stack");

        for (Well well : curPlateSelection.get(0).getWells()) {
            Well zStackWell = new Well();
            zStackWell.setPlateColumn(well.getPlateColumn());
            zStackWell.setPlateRow(well.getPlateRow());

            populateWell(well.getPlateColumn(), well.getPlateRow(), zStackWell);
            avgZStack.getWells().add(zStackWell);
        }

        PlatePanel.createPanelDialog(avgZStack, null, null);
    }


    private void populateWell(Integer plateColumn, Integer plateRow, Well zStackWell) {
        List<String> readouts = TdsUtils.flattenReadoutNames(curPlateSelection);


        for (String readoutName : readouts) {

            DescriptiveStatistics descStats = new DescriptiveStatistics();

            for (Plate plate : curPlateSelection) {
                Well curWell = plate.getWell(plateColumn, plateRow);
                if (curWell == null)
                    continue;

                Double readoutValue = curWell.getReadout(readoutName);
                if (readoutValue != null) {
                    descStats.addValue(readoutValue);
                }
            }

            zStackWell.getWellStatistics().put(readoutName, descStats.getMean());
        }
    }
}
