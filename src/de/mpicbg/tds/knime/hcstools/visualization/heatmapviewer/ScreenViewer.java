package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.core.model.Plate;

import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.util.PanelImageExporter;
import info.clearthought.layout.TableLayout;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 * Time: 11:53
 *
 * Creates a window containing all the heat-maps of a screen.
 */

public class ScreenViewer extends JFrame implements HiLiteListener{

    private ScreenHeatMapsPanel screenPanel;
    protected HeatMapMenu menus;


    // Constructor
    public ScreenViewer(){
        this(null);
    }

    public ScreenViewer(List<Plate> plates) {
        if ( (plates == null) ) {
            screenPanel = new ScreenHeatMapsPanel();
        } else {
            screenPanel = new ScreenHeatMapsPanel(plates);
        }

        menus = new HeatMapMenu(screenPanel);
        setTitle("HCS Heat-map Viewer");
        setJMenuBar(menus);
        add(screenPanel);
        setBounds(150, 150, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    // HiLiteListener methods.
    public void hiLite(final KeyEvent event) {

    }

    public void unHiLite(final KeyEvent event) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void unHiLiteAll(final KeyEvent event) {

    }


    public void setData(ScreenViewer data) {
    }

    public void getData(ScreenViewer data) {
    }

    public boolean isModified(ScreenViewer data) {
        return false;
    }


    public static void main(String[] args) {
        new ScreenViewer();
    }



    /**
     * User: Felix Meyenhofer
     * Date: 11/27/12
     *
     * A panel to create a trellis of plate heat-maps, a toolbar to select what readout to display and another toolbar
     * containing the color-bar.
     */

    public static class ScreenHeatMapsPanel extends JPanel implements HeatMapModelChangeListener {

        protected HeatMapModel2 heatMapModel;
        private List<HeatScreen> heatMaps;
        private int MIN_HEATMAP_WIDTH = 180;
        private int MIN_HEATMAP_HEIGHT = 120;
        private int PREFERRED_WITH = 600;
        private int PREFERRED_HEIGHT = 400;

        protected HeatMapInputToolbar toolbar;
        private JPanel heatMapsContainer;
        private JScrollPane heatMapScrollPane;
        protected HeatMapColorToolBar colorbar;
        private int cellGap = 5;
        private JPanel containerPositioner;


        // Constructors
        public ScreenHeatMapsPanel() {
            initialize();
            heatMapModel = new HeatMapModel2();
            heatMapModel.addChangeListener(this);
            configure(heatMapModel);
            setMinimumSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
            setPreferredSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
            new PanelImageExporter(this, true);
        }

        public ScreenHeatMapsPanel(List<Plate> plates) {
            this();

            ToolTipManager.sharedInstance().setDismissDelay(7500);
            ToolTipManager.sharedInstance().setInitialDelay(500);
            setPlates(plates);

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
        }

        // Methods
        private void configure(HeatMapModel2 model) {
            toolbar.configure(model);
            colorbar.configure(model);
        }

        private void initialize() {
            toolbar = new HeatMapInputToolbar();

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

            colorbar = new HeatMapColorToolBar();

            heatMapScrollPane = new JScrollPane();
            heatMapScrollPane.setViewportView(containerPositioner);
            heatMapScrollPane.setBackground(Color.BLACK);  //TODO: This is just to better distinguish the components. Should be removed before deployment.

            setLayout(new BorderLayout());
            add(toolbar, BorderLayout.NORTH);
            add(heatMapScrollPane, BorderLayout.CENTER);
            add(colorbar, BorderLayout.SOUTH);
        }

        public void setPlates(List<Plate> plates) {
            heatMaps = new ArrayList<HeatScreen>();
            for (Plate plate : plates) {
                heatMaps.add(new HeatScreen(plate, heatMapModel));
            }

            // pre-configure the heatmap configuration model
            heatMapModel.setScreen(plates);
            parsePlateBarCodes();
            toolbar.configure(heatMapModel);
            colorbar.configure(heatMapModel); // Careful the toolbar has to be configured first, since the colorbar needs the readout for its configuration.
        }

        public List<HeatScreen> getHeatMaps() {
            return heatMaps;
        }

        public void setHeatMaps(List<HeatScreen> heatMaps) {
            this.heatMaps = heatMaps;
        }

        protected void zoom(double zoomFactor) {
            MIN_HEATMAP_WIDTH *= zoomFactor;
            MIN_HEATMAP_HEIGHT *= zoomFactor;
            repopulatePlateGrid();
        }

        private void repopulatePlateGrid() {
//            sortHeatmaps();
            List<HeatScreen> heatmapSelection = getFilteredHeatMap();

            JPanel plateContainer = heatmapSelection.get(0);

            int numRows, numColumns;
            if ( heatMapModel.getAutomaticTrellisConfiguration() ) {
                int plateWidth = (plateContainer.getWidth() < MIN_HEATMAP_WIDTH) ? MIN_HEATMAP_WIDTH : plateContainer.getWidth();
                numColumns = (int) Math.floor(getWidth() *1.0 / (plateWidth + cellGap) );
                numRows = (int) Math.ceil(heatmapSelection.size() / (double) numColumns);
                heatMapScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            } else {
                numRows = heatMapModel.getNumberOfTrellisRows();
                numColumns = heatMapModel.getNumberOfTrellisColumns();
                heatMapScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }
            heatMapModel.updateTrellisConfiguration(numRows, numColumns);

            //patch the layout if only a few plates are selected
            if (numColumns > heatmapSelection.size())
                numColumns = heatmapSelection.size();

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

            // track changes of the batch; this allows to alter the background color of the heatmaps
            String lastBatchName = "bubabuba";
            Color batchBcknd = Color.GRAY;

            // populate the view with plates
            int plateNameFontSize = Utils.isWindowsPlatform() ? 8 : 12;
            Font barcodeFont = new Font("Serif", Font.PLAIN, plateNameFontSize);

            for (int i = 0; i < heatmapSelection.size(); i++) {
                HeatScreen heatMapPanel = heatmapSelection.get(i);
                Plate plate = heatMapPanel.getPlate();

                int rowIndex = i / numColumns;
                String gridPosition = (i - rowIndex * numColumns) + ", " + (rowIndex);

                plateContainer = new JPanel();
                plateContainer.setBorder(new TitledBorder(null, plate.getBarcode(), TitledBorder.CENTER, TitledBorder.BOTTOM, barcodeFont));

                plateContainer.setLayout(new BorderLayout());

                // change the background according to the batch
                String curBatchName = plate.getBatchName();
                if (curBatchName != null) {
                    if ((curBatchName == null && lastBatchName != null) || !curBatchName.equals(lastBatchName)) {
                        lastBatchName = curBatchName;
                        batchBcknd = batchBcknd.equals(Color.GRAY) ? Color.LIGHT_GRAY : Color.GRAY;
                    }
                }
                plateContainer.setBackground(batchBcknd);

                heatMapsContainer.add(plateContainer, gridPosition);
                plateContainer.add(heatMapPanel, BorderLayout.CENTER);
            }


            // Estimate the panel size.
            int containerWidth = numColumns * MIN_HEATMAP_WIDTH + (numColumns-1) * cellGap;
            int containerHeight = numRows * MIN_HEATMAP_HEIGHT + (numRows - 1) * cellGap;
            Dimension containerDimensions = new Dimension(containerWidth, containerHeight);
            heatMapsContainer.setPreferredSize(containerDimensions);

            // Now That the approximate size is right, get the plate container insets and adjust.
            if ( !(plateContainer == null) ) {
                Insets plateInsets = plateContainer.getInsets();
                containerWidth = numColumns * MIN_HEATMAP_WIDTH + (numColumns-1) * cellGap + (plateInsets.left + plateInsets.right) * numColumns;
                containerHeight = numRows * MIN_HEATMAP_HEIGHT + (numRows - 1) * cellGap + (plateInsets.top + plateInsets.bottom) * numRows;
                containerDimensions = new Dimension(containerWidth, containerHeight);
                heatMapsContainer.setPreferredSize(containerDimensions);
            }

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

            invalidate();
            updateUI();
            repaint();
        }

//        private void sortHeatmaps() {
//            heatMaps.clear();
//            List<Plate> plates = heatMapModel.getScreen();
//            heatMaps = new ArrayList<HeatScreen>();
//            for (Plate plate : plates) {
//                heatMaps.add(new HeatScreen(plate, heatMapModel));
//            }
//        }

        private void parsePlateBarCodes() {
            for (HeatScreen heatmap : heatMaps) {
                Plate plate = heatmap.getPlate();
                if (plate.getScreenedAt() != null) {
                    continue;
                }

                try {
                    Plate.configurePlateByBarcode(plate, BarcodeParserFactory.getAssayPlateBarcodeOLDParser(plate.getBarcode()));
                } catch (Throwable t) {
                    // do nothing here
    //                heatMapViewerMenu.setSortingEnabled(false);
                }
            }
        }

        public List<HeatScreen> getFilteredHeatMap() {

            List<HeatScreen> heatMapSelection = new ArrayList<HeatScreen>();
            for (HeatScreen heatmap : heatMaps) {
                Plate plate = heatmap.getPlate();
                if(heatMapModel.isSelected(plate)) {
                    heatMapSelection.add(heatmap);
                }
            }
            return heatMapSelection;
        }

        public void setSelection(Collection<Well> wellSelection) {
            // sort the selected wells according to plate

            Map<Plate, Collection<Well>> plateWells = new HashMap<Plate, Collection<Well>>();
            for (HeatScreen heatmap : heatMaps) {
                plateWells.put(heatmap.getPlate(), new ArrayList<Well>());
            }

            heatMapModel.setWellSelection(wellSelection);
            repaint();

            heatMapModel.setWellSelection(wellSelection);
            for (Well well : wellSelection) {
                plateWells.get(well.getPlate()).add(well);
            }

            // propagate the selection to the different panels
            for (HeatScreen heatmap : heatMaps) {
                Collection<Well> plateSelection = plateWells.get(heatmap.getPlate());

                heatmap.setSelection(plateSelection);
            }
        }

        public void modelChanged() {
            if (isVisible() && getWidth() > 0) {
                repopulatePlateGrid();
            }
        }


        public static void main(String[] args) {
            JFrame frame = new JFrame();
            frame.setSize(new Dimension(200, 500));
            frame.add(new ScreenHeatMapsPanel(null));
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setVisible(true);
        }

    }
}
