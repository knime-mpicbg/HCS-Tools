package de.mpicbg.tds.knime.hcstools.utils;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.SCREEN_MODEL_BARCODE;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ScreenConversionUtils {

    public static BufferedDataTable convertPlates2KnimeTable(Collection<Plate> plates, ExecutionContext exec) {

        List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();

        Attribute barcode = new Attribute(SCREEN_MODEL_BARCODE, StringCell.TYPE);
        colSpecs.add(barcode.getColumnSpec());
        Attribute plateRow = new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE);
        colSpecs.add(plateRow.getColumnSpec());
        Attribute plateCol = new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE);
        colSpecs.add(plateCol.getColumnSpec());
        Attribute description = new Attribute("description", StringCell.TYPE);
        colSpecs.add(description.getColumnSpec());

        List<Attribute> readouts = new ArrayList<Attribute>();

        for (String readoutName : plates.iterator().next().getWells().get(0).getReadOutNames()) {
            Attribute readoutAttribute = new Attribute(readoutName, DoubleCell.TYPE);
            colSpecs.add(readoutAttribute.getColumnSpec());
            readouts.add(readoutAttribute);
        }


        DataTableSpec outputSpec = new DataTableSpec(colSpecs.toArray(new DataColumnSpec[0]));
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int wellCounter = 0;
        for (Plate plate : plates) {

            for (Well well : plate.getWells()) {
                DataCell[] cells = new DataCell[colSpecs.size()];
                cells[0] = barcode.createCell(plate.getBarcode());
                cells[1] = plateRow.createCell(well.getPlateRow());
                cells[2] = plateCol.createCell(well.getPlateColumn());
                cells[3] = description.createCell(well.getDescription());

                for (int i = 0; i < readouts.size(); i++) {
                    Attribute readout = readouts.get(i);

                    if (well.getReadOutNames().contains(readout.getName())) {
                        cells[4 + i] = readout.createCell(well.getReadout(readout.getName()));
                    } else {
                        cells[4 + i] = DataType.getMissingCell();
                    }
                }

                DataRow rowd = new DefaultRow(new RowKey("Row " + wellCounter++), cells);
                container.addRowToTable(rowd);

            }
        }

        // once we are done, we close the container and return its table
        container.close();

        return container.getTable();
    }
}
