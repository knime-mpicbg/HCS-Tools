package de.mpicbg.knime.hcs.base.heatmap.renderer;

import java.awt.*;
import javax.swing.*;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.color.ColorScheme;
import de.mpicbg.knime.hcs.core.model.Well;
import de.mpicbg.knime.hcs.core.model.Plate;

/**
 * Implements a memory-efficient and fast heatmap-preview panel for a single screening plate.
 *
 * @author Holger Brandl
 */

public class HeatScreen extends JPanel {

    /** Plate model for display */
    private Plate plate;
    /** Data model */
    private HeatMapModel heatMapModel;


    /**
     * Constructor of the plate heatmap
     *
     * @param plate for display
     * @param heatMapModel data model of the UI
     */
    public HeatScreen(final Plate plate, HeatMapModel heatMapModel) {
        this.plate = plate;
        this.heatMapModel = heatMapModel;

        setBackground(ColorScheme.EMPTY_READOUT);
        setDoubleBuffered(true);
        setMinimumSize(new Dimension(100, 100));
        setToolTipText(plate.getBarcode());
    }


    /** {@inheritDoc} */
    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        double widthInc = (getWidth() / (double) plate.getNumColumns());
        double heightInc = (getHeight() / (double) plate.getNumRows());
        
        boolean paintOverlay = !this.heatMapModel.getCurrentOverlay().isEmpty();

        for (Well well : plate.getWells()) {
            int i = well.getPlateRow() - 1;
            int j = well.getPlateColumn() - 1;

            // fill the rect for each well with the appropriate color
            if (paintOverlay) {
                g.setColor(heatMapModel.getOverlayColor(well));
            } else {
                g.setColor(heatMapModel.getReadoutColor(well));
            }

            g.fillRect((int) (j * widthInc), (int) (i * heightInc), (int) Math.ceil(widthInc), (int) Math.ceil(heightInc));
        }
    }

    /**
     * Getter for the plate data
     *
     * @return plate displayed
     */
    public Plate getPlate() {
        return plate;
    }

}
