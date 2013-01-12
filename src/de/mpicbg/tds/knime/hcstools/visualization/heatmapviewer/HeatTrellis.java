package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ScreenColorScheme;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 11/27/12
 *
 * A panel to create a trellis of plate heat-maps, a toolbar to select what readout to display and another toolbar
 * containing the color-bar.
 *
 * TODO: Clean out the commented methods.
 */

public class HeatTrellis extends JPanel implements HeatMapModelChangeListener, MouseListener {

    // Panel size factors
    public final int MIN_HEATMAP_WIDTH = 80;
    public final int cellGap = 5;

    // The current heatmap size (width/height = 1.5 is the real microtiter plate proportions)
    private int HEATMAP_WIDTH = 180;
    private int HEATMAP_HEIGHT = 120;

    private int PREFERRED_WITH = 600;
    private int PREFERRED_HEIGHT = 400;

    // Component fields.
    protected HeatMapModel heatMapModel;
    private JPanel heatMapsContainer;
    private JScrollPane heatMapScrollPane;
    private JPanel containerPositioner;

    // List of PlateViewers.
    Map<UUID, PlateViewer> plateViewers = new HashMap<UUID, PlateViewer>();


    /**
     *  Constructors
     */
    public HeatTrellis() {
        initialize();

        ToolTipManager.sharedInstance().setDismissDelay(7500);
        ToolTipManager.sharedInstance().setInitialDelay(500);

        // re-layout the app when the window size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (getWidth() > 0) {
                    repopulatePlateGrid();
                }
            }
        });
        heatMapsContainer.setDoubleBuffered(true);

        setMinimumSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
        new PanelImageExporter(this, true);
    }

    public HeatTrellis(HeatMapModel model) {
        this();
        configure(model);
    }


    public HeatMapModel getHeatMapModel() {
        return this.heatMapModel;
    }


    /**
     * HeatMapModelChangeListener method.
     */
    @Override
    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repopulatePlateGrid();
        }
    }


    /**
     * GUI creation helpers
     */
    public void configure(HeatMapModel model) {
        this.heatMapModel = model;
        heatMapModel.addChangeListener(this);
//        this.setPlates(plates);
    }

    private void initialize() {
        heatMapsContainer = new JPanel();
        heatMapsContainer.setPreferredSize(new Dimension(PREFERRED_WITH-10,PREFERRED_HEIGHT-10));
        heatMapsContainer.setLayout(new TableLayout(new double[][]{{TableLayout.PREFERRED}, {TableLayout.PREFERRED}}));
        TableLayout layout = (TableLayout) heatMapsContainer.getLayout();
        layout.setHGap(cellGap);
        layout.setVGap(cellGap);

        // Just some indicating text in case the panel remains empty.
        JTextArea text = new JTextArea("Heatmap container panel");
        text.setEditable(false);
        heatMapsContainer.add(text, "0, 0");

        // Panel to avoid the table filling out the entire scroll pane.
        containerPositioner = new JPanel();
        containerPositioner.add(heatMapsContainer);

        // The scroll pane...
        heatMapScrollPane = new JScrollPane();
        heatMapScrollPane.setViewportView(containerPositioner);

        setLayout(new BorderLayout());
        add(heatMapScrollPane, BorderLayout.CENTER);
    }

//    public void setPlates(List<Plate> plates) {
//        if ( plates == null ) {
//            return;
//        }
//
//        heatMaps = new ArrayList<HeatScreen>();
//        for (Plate plate : plates) {
//            heatMaps.add(new HeatScreen(plate, heatMapModel));
//        }
//
//        // pre-configure the heatmap configuration model
////        heatMapModel.setScreen(plates);
//        parsePlateBarCodes();
////        toolbar.configure(heatMapModel);
////        colorbar.configure(heatMapModel); // Careful the toolbar has to be configured first, since the colorbar needs the readout for its configuration.
//    }

//    public List<HeatScreen> getHeatMaps() {
//        return heatMaps;
//    }
//
//    public void setHeatMaps(List<HeatScreen> heatMaps) {
//        this.heatMaps = heatMaps;
//    }

    /**
     * Method to scale up and down the heatmaps.
     * @param zoomFactor zoom or scale factor applied to the heatmap size
     */
    protected void zoom(double zoomFactor) {
        HEATMAP_WIDTH *= zoomFactor;
        // Make sure the new size does not exceed the limits.
        HEATMAP_WIDTH = (HEATMAP_WIDTH < MIN_HEATMAP_WIDTH) ? MIN_HEATMAP_WIDTH : HEATMAP_WIDTH;
        HEATMAP_WIDTH = (HEATMAP_WIDTH > getWidth()) ? getWidth()-cellGap : HEATMAP_WIDTH;
        // Derive the heatmap height.
        HEATMAP_HEIGHT = (int) Math.round(HEATMAP_WIDTH *(16.0/24.0));
        repopulatePlateGrid();
    }


    /**
     * Creating the heatmap trellis (renderer)
     */
    private void repopulatePlateGrid() {
//            sortHeatmaps();
//        List<HeatScreen> heatmapSelection = getFilteredHeatMap();
        List<HeatScreen> heatmapSelection = createHeatMaps();

        // Figure out how many rows an columns are needed.
        int[] rowsColumns = calculateTrellisDimensions(heatmapSelection.size());
        int numRows = rowsColumns[0];
        int numColumns = rowsColumns[1];

        // Propagate the new configuration to the heat map model
        heatMapModel.updateTrellisConfiguration(numRows, numColumns);

        // Recalculate the Table layout of the heatMapContainer panel.
        numColumns = updateTrellisTableLayout(numRows, numColumns);

//        // track changes of the batch; this allows to alter the background color of the heatmaps
//        String lastBatchName = "bubabuba";
//        Color batchBcknd = Color.GRAY;

        // populate the view with plates
        int plateNameFontSize = Utils.isWindowsPlatform() ? 8 : 12;
        Font barcodeFont = new Font("Serif", Font.PLAIN, plateNameFontSize);

        for (int i = 0; i < heatmapSelection.size(); i++) {
            HeatScreen heatMapPanel = heatmapSelection.get(i);
            Plate plate = heatMapPanel.getPlate();

            // Convert linear to xy position
            int rowIndex = i / numColumns;
            String gridPosition = (i - rowIndex * numColumns) + ", " + (rowIndex);

            // Create a panel with borders.
            JPanel plateContainer = new JPanel();
            TitledBorder titledBorder = new TitledBorder(BorderFactory.createBevelBorder(1),
                                                         plate.getBarcode(),
                                                         TitledBorder.CENTER,
                                                         TitledBorder.BOTTOM,
                                                         barcodeFont);
            plateContainer.setBorder(titledBorder);
            plateContainer.setLayout(new BorderLayout());

            // Add mouse listeners
            plateContainer.addMouseListener(this);
            heatMapPanel.addMouseListener(this);

            // Choose the background color.
            Color backgroundColor = heatMapModel.getBackgroundColor();//getRootPane().getBackground();
            if ( heatMapModel.doMarkSelection() ) {
                if ( heatMapModel.isPlateHiLited(plate) && heatMapModel.isPlateSelected(plate) ) {
                    backgroundColor = ScreenColorScheme.getInstance().selectionAndHiLiteColor;
                } else if ( heatMapModel.isPlateHiLited(plate) ) {
                    backgroundColor = ScreenColorScheme.getInstance().HilLiteColor;
                } else if ( heatMapModel.isPlateSelected(plate) ) {
                    backgroundColor = ScreenColorScheme.getInstance().selectionColor;
                }
            }
            plateContainer.setBackground(backgroundColor);

            // Truncate the barcode.
            titledBorder.setTitle(truncateBarcode(plate.getBarcode(), plateContainer.getFontMetrics(barcodeFont)));

//            // change the background according to the batch
//            String curBatchName = plate.getBatchName();
//            if (curBatchName != null) {
//                if ((curBatchName == null && lastBatchName != null) || !curBatchName.equals(lastBatchName)) {
//                    lastBatchName = curBatchName;
//                    batchBcknd = batchBcknd.equals(Color.GRAY) ? Color.LIGHT_GRAY : Color.GRAY;
//                }
//            }
//            plateContainer.setBackground(batchBcknd);

            plateContainer.add(heatMapPanel, BorderLayout.CENTER);
            heatMapsContainer.add(plateContainer, gridPosition);
        }

        // Estimate the panel size.
        updateContainerDimensions(numRows, numColumns);

        // Now That the approximate size is right, get the plate container insets and adjust.
        JPanel firstPlate = getFistPlate();
        if ( !(firstPlate == null) ) {
            Insets plateInsets = firstPlate.getInsets();
            updateContainerDimensions(numRows, numColumns, (plateInsets.left + plateInsets.right),
                                                           (plateInsets.top + plateInsets.bottom));
        }

        invalidate();
        updateUI();
        repaint();
    }



    /**
     * Helper methods for repopulatePlateGrid
     */
    private String truncateBarcode(String barcode, FontMetrics metric) {
        if ( metric.stringWidth(barcode) >= HEATMAP_WIDTH ) {
            while ( metric.stringWidth(barcode + "...") > HEATMAP_WIDTH ) {
                if (barcode.length() < 2) { break; }
                barcode = barcode.substring(0, barcode.length()-1);
            }
            barcode += "...";
        }
        return barcode;
    }

    private int[] calculateTrellisDimensions(int numberOfPlates) {
        int numRows, numColumns;
        if ( heatMapModel.getAutomaticTrellisConfiguration() ) {
//            JPanel firstPlate = getFistPlate();
//            int plateWidth;
//            if ( !(firstPlate == null) && firstPlate.getWidth() > HEATMAP_WIDTH) {
//                plateWidth = firstPlate.getWidth();
//            } else {
//                plateWidth = HEATMAP_WIDTH;
//            }
            numColumns = (int) Math.floor(getWidth() *1.0 / (HEATMAP_WIDTH + cellGap) );
            numRows = (int) Math.ceil(numberOfPlates/ (double) numColumns);
            heatMapScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            numRows = heatMapModel.getNumberOfTrellisRows();
            numColumns = heatMapModel.getNumberOfTrellisColumns();
            heatMapScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        //patch the layout if only a few plates are selected
        if (numColumns > numberOfPlates)
            numColumns = numberOfPlates;

        return new int[]{numRows, numColumns};
    }

    private JPanel getFistPlate() {
        try {
            return (JPanel) heatMapsContainer.getComponent(0);
        } catch (Exception e) {
            return null;
        }
    }

    private int updateTrellisTableLayout(int numRows, int numColumns) {

        double[] columnConfig = new double[numColumns];
        for (int i = 0; i < columnConfig.length; i++) {
            columnConfig[i] = 1. / (double) numColumns;
        }

        double[] rowConfig = new double[numRows];
        for (int i = 0; i < rowConfig.length; i++) {
            rowConfig[i] = 1. / (double) numRows;
        }

        // fix the table-layout config for 1 (which would be interpreted as a single pixel otherwise
        if (numRows == 1)
            rowConfig[0] = TableLayout.FILL;
        if (numColumns == 1)
            columnConfig[0] = TableLayout.FILL;

        // remove existing components
        heatMapsContainer.removeAll();

        // Set the grid
        TableLayout containerLayout = (TableLayout) heatMapsContainer.getLayout();
        containerLayout.setRow(rowConfig);
        containerLayout.setColumn(columnConfig);
        return numColumns;
    }

    private void updateContainerDimensions(int numRows, int numColumns, int hmarging, int vmarging) {
        int containerWidth = numColumns * HEATMAP_WIDTH + (numColumns-1) * cellGap + hmarging * numColumns;
        int containerHeight = numRows * HEATMAP_HEIGHT + (numRows - 1) * cellGap + vmarging * numRows;
        Dimension containerDimensions = new Dimension(containerWidth, containerHeight);
        heatMapsContainer.setPreferredSize(containerDimensions);

        // If the plate dimensions are fixed, put the container panel in another position panel to work around the
        // TableLayout properties, that wants to fill the entire space.
        if ( heatMapModel.isFixedPlateProportion() ) {
            heatMapsContainer.setMaximumSize(containerDimensions);
            heatMapsContainer.setMinimumSize(containerDimensions);
            containerPositioner.removeAll();
            containerPositioner.add(heatMapsContainer);
            heatMapScrollPane.setViewportView(containerPositioner);
        } else {
            heatMapsContainer.setMaximumSize(new Dimension(-1,-1));
            heatMapsContainer.setMinimumSize(new Dimension(-1,-1));
            heatMapScrollPane.setViewportView(heatMapsContainer);
        }
    }

    private void updateContainerDimensions(int numRows, int numColumns) {
        updateContainerDimensions(numRows, numColumns, 0, 0);
    }

//        private void sortHeatmaps() {
//            heatMaps.clear();
//            List<Plate> plates = heatMapModel.getScreen();
//            heatMaps = new ArrayList<HeatScreen>();
//            for (Plate plate : plates) {
//                heatMaps.add(new HeatScreen(plate, heatMapModel));
//            }
//        }

//    private void parsePlateBarCodes() {
//        for (HeatScreen heatmap : heatMaps) {
//            Plate plate = heatmap.getPlate();
//            if (plate.getScreenedAt() != null) {
//                continue;
//            }
//
//            try {
//                Plate.configurePlateByBarcode(plate, BarcodeParserFactory.getAssayPlateBarcodeOLDParser(plate.getBarcode()));
//            } catch (Throwable t) {
//                // do nothing here
////                heatMapViewerMenu.setSortingEnabled(false);
//            }
//        }
//    }

    private List<HeatScreen> createHeatMaps() {
        List<HeatScreen> currentHeatMaps = new ArrayList<HeatScreen>();

        for ( Plate plate : heatMapModel.getPlatesToDisplay() )
            currentHeatMaps.add(new HeatScreen(plate, heatMapModel));

        return currentHeatMaps;
    }

    private List<HeatScreen> getHeatMaps() {
        Component[] components = heatMapsContainer.getComponents();
        List<HeatScreen> heatMaps = new ArrayList<HeatScreen>();

        for (Component component : components) {
            JPanel container = (JPanel) component;
            heatMaps.add((HeatScreen) container.getComponent(0));
        }
        return heatMaps;
    }


    /**
     * Handling of the PlateViewer windows
     */
    public void closePlateViewers() {
        for (PlateViewer viewer : plateViewers.values()) {
            if ( viewer != null) {
                viewer.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(viewer, WindowEvent.WINDOW_CLOSING));
                viewer.dispose();
            }
        }
        plateViewers.clear();
    }

    /**
     * Handling of the PlateViewer windows
     */
    public void bringToFrontPlateViewers() {
        for (PlateViewer viewer : plateViewers.values()) {
            if ( viewer != null) {
                viewer.toFront();
                viewer.repaint();
            }
        }
    }


//    public List<HeatScreen> getFilteredHeatMap() {
//
//        List<HeatScreen> heatMapSelection = new ArrayList<HeatScreen>();
//        for (HeatScreen heatmap : heatMaps) {
//            Plate plate = heatmap.getPlate();
//            if(heatMapModel.isSelected(plate)) {
//                heatMapSelection.add(heatmap);
//            }
//        }
//        return heatMapSelection;
//    }


    /**
     * Helper methods for the heatmap selection with the mouse on the trellis
     */
    private HeatScreen getHeatMap(MouseEvent mouseEvent) {
        HeatScreen heatMap = null;
        try {
            heatMap = (HeatScreen) mouseEvent.getSource();
        } catch (ClassCastException e) {
            JPanel container = (JPanel) mouseEvent.getSource();
            heatMap = (HeatScreen) container.getComponent(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return heatMap;
    }

    private List<HeatScreen> calculateHeatMapSelection(HeatScreen start, HeatScreen stop) {
        List<HeatScreen> heatmaps = getHeatMaps();

        Point startPoint = start.getLocationOnScreen();
        Point stopPoint = stop.getLocationOnScreen();

        double xLow = Math.min(startPoint.getX(), stopPoint.getX());
        double xhigh = Math.max(startPoint.getX(), stopPoint.getX());
        double ylow = Math.min(startPoint.getY(), stopPoint.getY());
        double yhigh = Math.max(startPoint.getY(), stopPoint.getY());

        List<HeatScreen> selection = new ArrayList<HeatScreen>();
        for (HeatScreen heatmap : heatmaps) {
            Point point = heatmap.getLocationOnScreen();
            double x = point.getX();
            double y = point.getY();
            if ( (xLow <= x) && (x <= xhigh) && (ylow <= y) && (y <= yhigh) )
                selection.add(heatmap);
        }

        return selection;
    }


    /**
     * MouseListener stuff and helper methods.
     */
    private HeatScreen pressedHeatMap;
    private HeatScreen currentHeatMap;
    private boolean drag = false;
    private List<HeatScreen> previousPreSelection = new ArrayList<HeatScreen>();

    @Override
    public void mouseClicked(MouseEvent mouseEvent) { /** Do Nothing */ }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // Left click selection action.
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            drag = true;

            if ( !mouseEvent.isMetaDown() )
                heatMapModel.clearWellSelection();
            pressedHeatMap = getHeatMap(mouseEvent);

        // Open Plate details view on right click
        } else if ( mouseEvent.getButton() == MouseEvent.BUTTON3 ) {
            HeatScreen heatMap = getHeatMap(mouseEvent);
            PlateViewer viewer = new PlateViewer(this, heatMap.getPlate());

            if (plateViewers.containsKey(heatMap.getPlate().getUuid())) {
                viewer = plateViewers.get(heatMap.getPlate().getUuid());
                viewer.toFront();
                viewer.repaint();
            } else {
                viewer.setVisible(true);
                plateViewers.put(heatMap.getPlate().getUuid(), viewer);
                heatMapModel.addChangeListener(viewer);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        // Mouse released is used only in the selection process, thus only listens to the first mouse button
        if ( (mouseEvent.getButton() == MouseEvent.BUTTON1) ) {
            drag = false;
            previousPreSelection.clear();

            if ( pressedHeatMap.equals(currentHeatMap) )
                heatMapModel.updateWellSelection(currentHeatMap.getPlate().getWells());

            else {
                List<HeatScreen> selectedHeatMaps = calculateHeatMapSelection(pressedHeatMap, currentHeatMap);
                for (HeatScreen heatmap : selectedHeatMaps)
                    heatMapModel.updateWellSelection(heatmap.getPlate().getWells());
            }

            heatMapModel.fireModelChanged();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // Register the map the mouse pointer last skidded in.
        currentHeatMap = getHeatMap(mouseEvent);
        // Paint selection background
        if (drag) {
            List<HeatScreen> selectedHeatMaps = calculateHeatMapSelection(pressedHeatMap, currentHeatMap);
            previousPreSelection.removeAll(selectedHeatMaps);

            // Deselect.
            for (HeatScreen unselect : previousPreSelection) {
                if (!heatMapModel.isPlateSelected(unselect.getPlate())) {
                    JPanel container = (JPanel) unselect.getParent();
                    container.setBackground(getTopLevelAncestor().getBackground());
                }
            }

            // Mark current selection.
            for (HeatScreen select : selectedHeatMaps) {
                if (!heatMapModel.isPlateSelected(select.getPlate())) {
                    JPanel container = (JPanel) select.getParent();
                    container.setBackground(ScreenColorScheme.getInstance().preselectionColor);
                }
            }
            previousPreSelection = selectedHeatMaps;
        }
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) { /** Do Nothing */ }



    /**
     * Quick testing
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(200, 500));
        frame.add(new HeatTrellis(new HeatMapModel()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
