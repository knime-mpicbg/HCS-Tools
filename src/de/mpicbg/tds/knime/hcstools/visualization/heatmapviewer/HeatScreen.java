package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;

import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.model.Plate;


/**
 * Implements a memory-efficient and fast heatmap-preview panel for a single screening plate.
 *
 * @author Holger Brandl
 * TODO: Clean out commented methods.
 */

// Replaces the PlateOverviewHeatMap
public class HeatScreen extends JPanel {

    private Plate plate;
    private HeatMapModel2 heatMapModel;


    public HeatScreen(final Plate plate, HeatMapModel2 heatMapModel) {
        this.plate = plate;
        this.heatMapModel = heatMapModel;

        setBackground(heatMapModel.getColorScheme().emptyReadOut);
        setDoubleBuffered(true);
        setMinimumSize(new Dimension(100, 100));
        setToolTipText(plate.getBarcode());

        // Mouse listener for the well details tooltip.
        addMouseListener(new MouseAdapter() {
            @Override
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

//        // Mouse listener for the plate details view. Double clicking on a plate opens a new window.
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent mouseEvent) {
//                if (mouseEvent.getClickCount() != 2) {
//                    return;
//                }
//                openNewPlateViewer();
//
//
//            }
//        });
    }

//    protected void openNewPlateViewer() {
//        // open a new detail-dialog for the plate
//        HeatMapModel2 plateHeatMapModel = new HeatMapModel2();
//        // todo if we want to use a global color scale also in the single plate view we ave to use te next line
////                plateHeatMapModel.setScreen(PlateOverviewHeatMap.this.heatMapModel.getScreen());
//        plateHeatMapModel.setCurrentReadout(this.heatMapModel.getSelectedReadOut());
//        plateHeatMapModel.setOverlay(this.heatMapModel.getOverlay());
//        plateHeatMapModel.setReadoutRescaleStrategy(this.heatMapModel.getRescaleStrategy());
//        plateHeatMapModel.setColorScheme(this.heatMapModel.getColorScheme());
//        plateHeatMapModel.setHideMostFreqOverlay(this.heatMapModel.doHideMostFreqOverlay());
//        plateHeatMapModel.addChangeListener(this.heatMapModel.getChangeListeners());
//        plateHeatMapModel.setWellSelection(heatMapModel.getWellSelection());
////        if (this.heatMapModel.getWellSelection().size() > 0) {
////            plateHeatMapModel.setWellSelection(TdsUtils.splitIntoPlateMap(this.heatMapModel.getWellSelection()).get(plate));
////        }
//
////        Window ownerWindow = Utils.getOwnerDialog(this);
////        PlateViewer.createPanelDialog(this.getPlate(), plateHeatMapModel, ownerWindow);
////        new PlateViewer(thisgetPlate(), plateHeatMapModel);
//
//    }


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

            // Choose between selection layout and readout color.
//            if (heatMapModel.isWellSelected(well)) {
//                g.setColor(heatMapModel.getColorScheme().getSelectionMarkerColor());
//            } else
            if (layoutColor != null) {
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

//    public void setSelection(Collection<Well> highlightWells) {
//        // todo is this actually updated when a selection is being made visible
//        heatMapModel.setDoShowLayout(false);
//        heatMapModel.setWellSelection(highlightWells);
//        repaint();
//    }
}
