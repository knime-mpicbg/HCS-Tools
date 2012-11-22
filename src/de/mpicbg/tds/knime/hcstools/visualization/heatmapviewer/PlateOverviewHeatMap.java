package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;


/**
 * Implements a memory-efficient and fast heatmap-preview panel for a single screening plate.
 *
 * @author Holger Brandl
 */
public class PlateOverviewHeatMap extends JPanel {

    private Plate plate;

    private HeatMapModel heatMapModel;


    public PlateOverviewHeatMap(final Plate plate, HeatMapModel heatMapModel) {
        this.plate = plate;

        this.heatMapModel = heatMapModel;

        setDoubleBuffered(true);
        setMinimumSize(new Dimension(100, 100));

        addMouseListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int colIndex = (int) (plate.getNumColumns() * e.getX() / (double) getWidth());
                int rowIndex = (int) (plate.getNumRows() * e.getY() / (double) getHeight());

//                Well well = wellService.getWell(plate.getBarcode(), rowIndex + 1, colIndex + 1);
                Well well = plate.getWell(colIndex + 1, rowIndex + 1);

//                assert well != null : "well could not be found at plate '" + plate.getBarcode() + "' and position [" + rowIndex + "," + colIndex + "]";

                if (well != null)
                    setToolTipText(well.getTreatment());
            }
        });

        setToolTipText(plate.getBarcode());

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() != 2) {
                    return;
                }

                // open a new detail-dialog for the plate
                HeatMapModel plateHeatMapModel = new HeatMapModel();

                // todo if we want to use a global color scale also in the single plate view we ave to use te next line
//                plateHeatMapModel.setScreen(PlateOverviewHeatMap.this.heatMapModel.getScreen());
                plateHeatMapModel.setCurrentReadout(PlateOverviewHeatMap.this.heatMapModel.getSelectedReadOut());
                plateHeatMapModel.setOverlay(PlateOverviewHeatMap.this.heatMapModel.getOverlay());
                plateHeatMapModel.setReadoutRescaleStrategy(PlateOverviewHeatMap.this.heatMapModel.getRescaleStrategy());
                plateHeatMapModel.setColorScheme(PlateOverviewHeatMap.this.heatMapModel.getColorScheme());
                plateHeatMapModel.setHideMostFreqOverlay(PlateOverviewHeatMap.this.heatMapModel.doHideMostFreqOverlay());

                if (PlateOverviewHeatMap.this.heatMapModel.getWellSelection().size() > 0) {
                    plateHeatMapModel.setWellSelection(TdsUtils.splitIntoPlateMap(PlateOverviewHeatMap.this.heatMapModel.getWellSelection()).get(plate));
                }

                Window ownerWindow = Utils.getOwnerDialog(PlateOverviewHeatMap.this);
                PlatePanel.createPanelDialog(plate, plateHeatMapModel, ownerWindow);
            }
        });
    }


    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        double widthInc = (getWidth() / (double) plate.getNumColumns());
        double heightInc = (getHeight() / (double) plate.getNumRows());

        for (Well well : plate.getWells()) {
            int i = well.getPlateRow() - 1;
            int j = well.getPlateColumn() - 1;

            // fill the rect for each well with the appropriate color

            Color layoutColor = heatMapModel.getOverlayColor(well);

            if (heatMapModel.isSelected(well)) {
                g.setColor(heatMapModel.getColorScheme().getHighlightColor());

            } else if (layoutColor != null) {
                g.setColor(layoutColor);
            } else {
                g.setColor(heatMapModel.getReadoutColor(well));
            }

            g.fillRect((int) (j * widthInc), (int) (i * heightInc), (int) Math.ceil(widthInc), (int) Math.ceil(heightInc));
        }
    }


    public Plate getPlate() {
        return plate;
    }


    public void setSelection(Collection<Well> highlightWells) {
        // todo is this actually updated wehn a selection is beeing made visible
        heatMapModel.setDoShowLayout(false);
        heatMapModel.setWellSelection(highlightWells);

        repaint();
    }
}
