package de.mpicbg.tds.core.view;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Implements a more details view for a single plate which also includes compounds, concentration, etc.
 *
 * @author Holger Brandl
 */
public class PlateDetailsHeatMap extends JPanel {

    private Plate plate;

    private HeatMapModel heatmapModel;

    Map<Well, HeatWellPanel> wellPanelGrid = new HashMap<Well, HeatWellPanel>();


    public PlateDetailsHeatMap(Plate plate, HeatMapModel heatmapModel) {

        this.plate = plate;
        this.heatmapModel = heatmapModel;

        WellSelectionController selectionController = new WellSelectionController();

        // configure the actual grid
        TableLayout tableLayout = new TableLayout();
        setLayout(tableLayout);

        // configure the layout and populate it with well rendern
        int numRows = plate.getNumRows();
        int numColumns = plate.getNumColumns();

        double[] rowConfig = new double[numRows + 1];
        double[] columnConfig = new double[numColumns + 1];

        rowConfig[0] = 25;
        for (int i = 1; i < rowConfig.length; i++) {
            rowConfig[i] = 1. / (double) numRows;
        }

        columnConfig[0] = 25;
        for (int i = 1; i < columnConfig.length; i++) {
            columnConfig[i] = 1. / (double) numColumns;
        }

        tableLayout.setRow(rowConfig);
        tableLayout.setColumn(columnConfig);


        // populate the grid
        JLabel topRightCornerLabel = new JLabel("");
        add(topRightCornerLabel, "0,0");
        topRightCornerLabel.addMouseListener(selectionController);

        // 1) row-header
        for (int i = 0; i < numRows; i++) {
            JLabel rowLabel = new JLabel(TdsUtils.mapPlateRowNumberToString(i + 1));
            rowLabel.addMouseListener(selectionController);
            rowLabel.setBackground(Color.RED);
            add(rowLabel, "0, " + (i + 1));
        }


        // 2) column-header
        for (int i = 0; i < numColumns; i++) {
            JLabel colLabel = new JLabel((i + 1) + "");
            colLabel.addMouseListener(selectionController);
            colLabel.setBackground(Color.GREEN);
            add(colLabel, (i + 1) + ", 0");
        }


        // 3) actual well renderers (by iterating over the plate as it's much more efficient compared to using the service)
        for (Well well : plate.getWells()) {
            String insertPosition = (well.getPlateColumn()) + ", " + (well.getPlateRow());
            HeatWellPanel heatWellPanel = new HeatWellPanel(well, heatmapModel);

            heatWellPanel.addMouseListener(selectionController);

            wellPanelGrid.put(well, heatWellPanel);
            add(heatWellPanel, insertPosition);
//            add(new JLabel(insertPosition), insertPosition);
        }
    }


    public void setSelection(Collection<Well> highlightWells) {
        for (Well highlightWell : highlightWells) {
            wellPanelGrid.get(highlightWell).setSelected(true);
        }
    }


    public void showGrid(boolean doShowGrid) {
        for (HeatWellPanel heatWellPanel : wellPanelGrid.values()) {
            heatWellPanel.setShowGrid(doShowGrid);
        }
    }


    class WellSelectionController extends MouseAdapter {

        HeatWellPanel dragStart;


        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            Object source = mouseEvent.getSource();
            if (source instanceof HeatWellPanel && dragStart == null) {
                dragStart = ((HeatWellPanel) source);
            }
        }


//        @Override
//        public void mouseDragged(MouseEvent mouseEvent) {
////            super.mouseDragged(mouseEvent);
//
//
//        }


        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            super.mouseReleased(mouseEvent);

            if (dragStart != null) {

                HeatWellPanel sourcePanel = (HeatWellPanel) mouseEvent.getSource();
                Point releasePoint = mouseEvent.getPoint();
                Object source = getComponentAt(sourcePanel.getX() + releasePoint.x, sourcePanel.getY() + releasePoint.y);

                if (source instanceof HeatWellPanel) {
                    if (!mouseEvent.isMetaDown()) {
                        heatmapModel.getWellSelection().clear();
                    }

                    HeatWellPanel dragStop = (HeatWellPanel) source;

                    // now iterate over all wells in the rectangle to select 
                    for (int colIndex = dragStart.getWell().getPlateColumn(); colIndex <= dragStop.getWell().getPlateColumn(); colIndex++) {
                        for (int rowIndex = dragStart.getWell().getPlateRow(); rowIndex <= dragStop.getWell().getPlateRow(); rowIndex++) {
                            invertSelection(plate.getWell(colIndex, rowIndex));
                        }
                    }

                    PlateDetailsHeatMap.this.repaint();
                }
            }

            dragStart = null;
        }


        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            super.mouseClicked(mouseEvent);

            if (!mouseEvent.isMetaDown()) {
                heatmapModel.getWellSelection().clear();
            }


            Object source = mouseEvent.getSource();
            if (source instanceof JLabel) {

                String label = ((JLabel) source).getText();

                if (label.isEmpty()) {
                    for (Well well : plate.getWells()) {
                        invertSelection(well);
                    }

                } else if (label.matches("[\\d]*")) {
                    int column = Integer.parseInt(label);

                    for (int i = 1; i <= plate.getNumRows(); i++) {
                        Well well = plate.getWell(column, i);
                        invertSelection(well);
                    }
                } else {
                    int rowIndex = TdsUtils.mapPlateRowStringToNumber(label);

                    for (int i = 1; i <= plate.getNumColumns(); i++) {
                        Well well = plate.getWell(i, rowIndex);
                        invertSelection(well);
                    }
                }

                // column or row selection

            } else if (source instanceof HeatWellPanel) {
                // single well selection
                HeatWellPanel wellPanel = (HeatWellPanel) source;
                invertSelection(wellPanel.getWell());
            }

            PlateDetailsHeatMap.this.repaint();
        }


        private void invertSelection(Well well) {
//			wellPanelGrid.get(well).repaint();
            if (heatmapModel.isSelected(well)) {
                heatmapModel.getWellSelection().remove(well);
            } else {
                heatmapModel.getWellSelection().add(well);
            }
        }
    }

}
