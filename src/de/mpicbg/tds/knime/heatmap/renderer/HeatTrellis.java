package de.mpicbg.tds.knime.heatmap.renderer;

import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.heatmap.HeatMapModelChangeListener;
import de.mpicbg.tds.knime.heatmap.PlateViewer;
import de.mpicbg.tds.knime.heatmap.color.ColorScheme;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * A panel to create a trellis of plate heat-maps.
 *
 * @author Felix Meyenhofer
 *         11/27/12
 */

public class HeatTrellis extends JPanel implements HeatMapModelChangeListener, MouseListener {

    /** Minimal heatmap width */
    public final int MIN_HEATMAP_WIDTH = 80;
    /** Gap between heatmaps */
    public final int cellGap = 5;

    /** The current heatmap size (width/height = 1.5 is the real microtiter plate proportions) */
        /** Heatmap width */
        private int HEATMAP_WIDTH = 180;
        /** heatmap height */
        private int HEATMAP_HEIGHT = 120;
        /** Preferred heatmap width */
        private int PREFERRED_WITH = 600;
        /** Preferred heatmap height */
        private int PREFERRED_HEIGHT = 400;

    /** Data model */
    public HeatMapModel heatMapModel;

    /** Collector container for all the heatmaps */
    private JPanel heatMapsContainer;
    /** Panel that is is buffer between the {@link #heatMapsContainer} and the {@link #heatMapScrollPane}
     * allowing the constraint of the real plate proportions */
    private JPanel containerPositioner;
    /** Scrollpane around the heatmap trellis. */
    private JScrollPane heatMapScrollPane;

    /** Container for all the child views */
    public Map<UUID, PlateViewer> plateViewers = new HashMap<UUID, PlateViewer>();

    /** Heatmap where the mouse draggig started */
    private HeatScreen pressedHeatMap;
    /** Heatmap where the mouse is just dragging accross */
    private HeatScreen currentHeatMap;
    /** Flag to recognise dragging events */
    private boolean drag = false;
    /** List of the previously selected heatmpas */
    private List<HeatScreen> previousPreSelection = new ArrayList<HeatScreen>();


    /**
     *  Constructor for initialization
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

    /**
     * Constructor for initialization and configuration of the UI components.
     *
     * @param model devlivering the data
     */
    public HeatTrellis(HeatMapModel model) {
        this();
        configure(model);
    }


    /** {@inheritDoc} */
    @Override
    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repopulatePlateGrid();
        }
    }

    /**
     * Accessor for the data model
     *
     * @return data model
     */
    public HeatMapModel getHeatMapModel() {
        return this.heatMapModel;
    }

    /**
     * Configuration of the UI components
     *
     * @param model delivering the data.
     */
    public void configure(HeatMapModel model) {
        this.heatMapModel = model;
        heatMapModel.addChangeListener(this);
    }

    /**
     * Initialization of the UI components
     */
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

    /**
     * Method to scale up and down the heatmaps.
     *
     * @param zoomFactor zoom or scale factor applied to the heatmap size
     */
    public void zoom(double zoomFactor) {
        HEATMAP_WIDTH *= zoomFactor;
        // Make sure the new size does not exceed the limits.
        HEATMAP_WIDTH = (HEATMAP_WIDTH < MIN_HEATMAP_WIDTH) ? MIN_HEATMAP_WIDTH : HEATMAP_WIDTH;
        HEATMAP_WIDTH = (HEATMAP_WIDTH > getWidth()) ? getWidth()-cellGap : HEATMAP_WIDTH;
        // Derive the heatmap height.
        HEATMAP_HEIGHT = (int) Math.round(HEATMAP_WIDTH * (16.0/24.0));
        repopulatePlateGrid();
    }

    /**
     * Creating the heatmap trellis (renderer)
     */
    public void repopulatePlateGrid() {
        List<HeatScreen> heatmapSelection = createHeatMaps();

        // Figure out how many rows an columns are needed.
        int[] rowsColumns = calculateTrellisDimensions(heatmapSelection.size());
        int numRows = rowsColumns[0];
        int numColumns = rowsColumns[1];

        // Propagate the new configuration to the heat map model
        heatMapModel.updateTrellisConfiguration(numRows, numColumns);

        // Recalculate the Table layout of the heatMapContainer panel.
        numColumns = updateTrellisTableLayout(numRows, numColumns);

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
                    backgroundColor = ColorScheme.HILITED_AND_SELECTED;
                } else if ( heatMapModel.isPlateHiLited(plate) ) {
                    backgroundColor = ColorScheme.HILITED;
                } else if ( heatMapModel.isPlateSelected(plate) ) {
                    backgroundColor = ColorScheme.SELECTED;
                }
            }
            plateContainer.setBackground(backgroundColor);

            // Truncate the barcode.
            titledBorder.setTitle(truncateBarcode(plate.getBarcode(), plateContainer.getFontMetrics(barcodeFont)));

            plateContainer.add(heatMapPanel, BorderLayout.CENTER);
            heatMapsContainer.add(plateContainer, gridPosition);
        }

        // Estimate the panel size.
        updateContainerDimensions(numRows, numColumns);

        invalidate();
        updateUI();
        repaint();
    }

    /**
     * Truncating the barcodes if they are too long for the heatmaps
     *
     * @param barcode string representing the barcode
     * @param metric barcode string font metrics
     * @return truncated barcode
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

    /**
     * Compute the size of the trellis
     *
     * @param numberOfPlates total number of rendered plates
     * @return [width height]
     */
    private int[] calculateTrellisDimensions(int numberOfPlates) {
        int numRows, numColumns;
        int panelWidth = getWidth() - heatMapScrollPane.getVerticalScrollBar().getWidth();

        if ( heatMapModel.isAutomaticTrellisConfiguration() ) {
            numColumns = (int) Math.floor((panelWidth- cellGap) / (HEATMAP_WIDTH + cellGap));
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

    /**
     * Update the table layout of the {@link #heatMapsContainer}
     *
     * @param numRows number of rows
     * @param numColumns number of columns
     * @return new number of columns
     */
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

    /**
     * Recalculate the {@link #heatMapsContainer} dimensions
     *
     * @param numRows number of rows in the trellis
     * @param numColumns number of columns in the trellis
     */
    private void updateContainerDimensions(int numRows, int numColumns) {
        int containerWidth = numColumns * HEATMAP_WIDTH + (numColumns + 1) * cellGap;
        int containerHeight = numRows * HEATMAP_HEIGHT + (numRows + 1) * cellGap;
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

    /**
     * Fetching the available plate data (not filtered, etc.)
     * and creating {@link HeatScreen} heatmaps
     *
     * @return rendered heatmaps
     */
    private List<HeatScreen> createHeatMaps() {
        List<HeatScreen> currentHeatMaps = new ArrayList<HeatScreen>();

        for ( Plate plate : heatMapModel.getPlatesToDisplay() )
            currentHeatMaps.add(new HeatScreen(plate, heatMapModel));

        return currentHeatMaps;
    }

    /**
     * Get the currently displayed heatmaps
     *
     * @return list of displayed heatmaps
     */
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
     * Get the heatmap firing the mouseEvent
     *
     * @param mouseEvent fired by a heatmap
     * @return the heatmaps that fired the event
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

    /**
     * Close all the {@link PlateViewer}s opened from this trellis
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
     * Bring all {@link PlateViewer}s produced by this trellis to the front
     */
    public void bringToFrontPlateViewers() {
        for (PlateViewer viewer : plateViewers.values()) {
            if ( viewer != null) {
                viewer.toFront();
                viewer.repaint();
            }
        }
    }

    /**
     * Calculate the heatmap selection by the mouse drag action
     *
     * @param start heatmap the dragging started on
     * @param stop heatmap where the mouse was released
     * @return list of the heatmaps included by the square defined by input heatmaps
     */
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

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(MouseEvent mouseEvent) { /** Do Nothing */ }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
                    container.setBackground(ColorScheme.SELECTING);
                }
            }
            previousPreSelection = selectedHeatMaps;
        }
    }

    /** {@inheritDoc} */
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
