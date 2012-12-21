package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ScreenColorScheme;
import info.clearthought.layout.TableLayout;

/**
 * @author Holger Brandl
 *
 * Implements a more detailed view for a single plate which also includes compounds, concentration, etc.
 * Replaces PlateDetailsHeatMap
 * TODO: clean out commented methods.
 */

public class HeatPlate extends JPanel implements MouseListener {

    // The well size determines the heat map size (panel) and influences the PlateViewers size.
    public final int WELL_SIZE = 22;

    // Component fields.
    private HeatMapModel2 heatMapModel;
    Map<Well, HeatWell> wellPanelGrid = new HashMap<Well, HeatWell>();


    /**
     * Constructor
     */
    public HeatPlate(PlateViewer parent) {
        this.heatMapModel = parent.getHeatMapModel();
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setBackground(ScreenColorScheme.getInstance().emptyReadOut);

        Plate plate = heatMapModel.getScreen().get(0);

        // configure the actual grid
        TableLayout tableLayout = new TableLayout();
        setLayout(tableLayout);

        // configure the layout and populate it with well renders
        int numRows = plate.getNumRows();
        int numColumns = plate.getNumColumns();
        double[] rowConfig = new double[numRows + 1];
        double[] columnConfig = new double[numColumns + 1];

        this.setPreferredSize(new Dimension((numColumns+1)*WELL_SIZE, (numRows+1)*WELL_SIZE));

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

        // populate the grid (The labels have to have the mouse listener so the outer wells can be drag-selected).
        JLabel space = new JLabel("");
        space.setBackground(parent.getBackground());
        space.setOpaque(true);
        add(space, "0,0");
        space.addMouseListener(this);

        // 1) row-header
        for (int i = 0; i < numRows; i++) {
            JLabel rowLabel = new JLabel(TdsUtils.mapPlateRowNumberToString(i + 1));
            rowLabel.setVerticalAlignment(JLabel.CENTER);
            rowLabel.setHorizontalAlignment(JLabel.CENTER);
            rowLabel.addMouseListener(this);
            rowLabel.setBackground(parent.getBackground());
            rowLabel.setOpaque(true);
            add(rowLabel, "0, " + (i + 1));
        }

        // 2) column-header
        for (int i = 0; i < numColumns; i++) {
            JLabel colLabel = new JLabel((i + 1) + "");
            colLabel.setHorizontalAlignment(JLabel.CENTER);
            colLabel.setVerticalAlignment(JLabel.BOTTOM);
            colLabel.addMouseListener(this);
            colLabel.setBackground(parent.getBackground());
            colLabel.setOpaque(true);
            add(colLabel, (i + 1) + ", 0");
        }

        // 3) actual well renderer (by iterating over the plate as it's much more efficient compared to using the service)
        for (Well well : plate.getWells()) {
            String insertPosition = (well.getPlateColumn()) + ", " + (well.getPlateRow());
            HeatWell heatWellPanel = new HeatWell(well, heatMapModel);
            heatWellPanel.addMouseListener(this);
            wellPanelGrid.put(well, heatWellPanel);
            add(heatWellPanel, insertPosition);
        }
    }


//    public void setSelection(Collection<Well> highlightWells) {
//        for (Well highlightWell : highlightWells) {
//            wellPanelGrid.get(highlightWell).setSelected(true);
//        }
//    }


//    public void showGrid(boolean doShowGrid) {
//        for (HeatWell heatWellPanel : wellPanelGrid.values()) {
//            heatWellPanel.setShowGrid(doShowGrid);
//        }
//    }

    /**
     * Helper methods for the heatmap selection with the mouse on the trellis
     */
    private List<HeatWell> getHeatWells() {
        Component[] components = this.getComponents();
        List<HeatWell> heatWells = new ArrayList<HeatWell>();

        for (Component component : components) {
            if ( component instanceof HeatWell )
                heatWells.add((HeatWell) component);
        }

        return heatWells;
    }

    private List<Well> calculateHeatMapSelection(Component start, Component stop) {
        List<HeatWell> components = getHeatWells();

        Point startPoint = start.getLocationOnScreen();
        Point stopPoint = stop.getLocationOnScreen();

        double xLow = Math.min(startPoint.getX(), stopPoint.getX());
        double xhigh = Math.max(startPoint.getX(), stopPoint.getX());
        double ylow = Math.min(startPoint.getY(), stopPoint.getY());
        double yhigh = Math.max(startPoint.getY(), stopPoint.getY());

        List<Well> selection = new ArrayList<Well>();
        for (HeatWell component : components) {
            Point point = component.getLocationOnScreen();
            double x = point.getX();
            double y = point.getY();
            if ( (xLow <= x) && (x <= xhigh) && (ylow <= y) && (y <= yhigh) )
                selection.add(component.getWell()) ;
        }

        return selection;
    }


    /**
     * MouseListener stuff and helper methods.
     */
    private Component pressedComponent;
    private Component currentComponent;

    @Override
    public void mouseClicked(MouseEvent mouseEvent) { /** Do Nothing */ }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // Left click selection action.
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            pressedComponent = (Component) mouseEvent.getSource();

            // Clear selection
            if ( !mouseEvent.isMetaDown() ) {
                heatMapModel.clearWellSelection();
            }

        // Open Plate details view on right click
        } else if ( mouseEvent.getButton() == MouseEvent.BUTTON3 ) {
            try {
                HeatWell heatWell = (HeatWell) mouseEvent.getSource();
                heatWell.openNewWellViewer();
            } catch (ClassCastException e) {
                System.err.println("Ignoring call to open Well dialog from: " + mouseEvent.getSource().getClass().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        // Mouse released is used only in the selection process, thus only listens to the first mouse button
        if ( (mouseEvent.getButton() == MouseEvent.BUTTON1) ) {
            if ( pressedComponent.equals(currentComponent) && (currentComponent instanceof HeatWell) )
                heatMapModel.updateWellSelection(((HeatWell) currentComponent).getWell());

            else {
                List<Well> selectedHeatWells = calculateHeatMapSelection(pressedComponent, currentComponent);
                heatMapModel.updateWellSelection(selectedHeatWells);
            }

            HeatPlate.this.repaint();
            heatMapModel.fireModelChanged();
            PlateViewer viewer = (PlateViewer) getTopLevelAncestor();
            viewer.getUpdater().getHeatMapModel().fireModelChanged();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // Register the map the mouse pointer last skidded in.
        currentComponent = (Component) mouseEvent.getSource();
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) { /** Do Nothing */ }

}
