/*
 * Created by JFormDesigner on Thu Dec 17 20:18:38 CET 2009
 */

package de.mpicbg.tds.core.view;

import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.util.PanelImageExporter;
import de.mpicbg.tds.core.view.color.ColorBar;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;


/**
 * A panel that shows plate-heatmaps in a grid view.
 *
 * @author Holger Brandl
 */
public class ScreenPanel extends JPanel implements HeatMapModelChangeListener {

    private HeatMapModel heatMapModel = new HeatMapModel();

    private int MIN_HEATMAP_WIDTH = 200;
    public List<PlateOverviewHeatMap> heatmaps;


    public ScreenPanel() {
        initComponents();

        heatMapModel.addChangeListener(this);
        new PanelImageExporter(this, true);
    }


    public ScreenPanel(List<Plate> plates) {
        this();

        ToolTipManager.sharedInstance().setDismissDelay(7500);
        ToolTipManager.sharedInstance().setInitialDelay(500);
        setPlates(plates);


        // relayout the app when the window size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (getWidth() > 0) {
                    repopulatePlateGrid();
                }
            }
        });

        screenViewContainer.setDoubleBuffered(true);


        // attach the listener for the plate filter
        plateFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                heatMapModel.filterPlates(plateFilter.getText());
            }
        });

        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoom(1.25);
            }
        });

        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoom(0.75);
            }
        });

        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(new Dimension(500, 400));

    }


    public void setPlates(List<Plate> plates) {

//        System.err.println("treatments: " + TdsUtils.collectTreatments(plates));

        heatmaps = new ArrayList<PlateOverviewHeatMap>();
        for (Plate plate : plates) {
            heatmaps.add(new PlateOverviewHeatMap(plate, heatMapModel));
        }

        // pre-configure the heatmap configuration model
        heatMapModel.setScreen(plates);

        parsePlateBarCodes();

        colorBar.configure(heatMapModel);

        heatMapViewerMenu.configure(heatMapModel);
    }


    private void zoom(double zoomFactor) {
        MIN_HEATMAP_WIDTH *= zoomFactor;
        repopulatePlateGrid();
    }


    private void repopulatePlateGrid() {
        sortHeatmaps();
        List<PlateOverviewHeatMap> heatmapSelection = getFilteredHeatMap();

        int numColumns = (int) Math.floor(getWidth() / MIN_HEATMAP_WIDTH);

        //patch the layout if only a few plates are selected
        if (numColumns > heatmapSelection.size())
            numColumns = heatmapSelection.size();

        double[] columnConfig = new double[numColumns];
        for (int i = 0; i < columnConfig.length; i++) {
            columnConfig[i] = 1. / (double) numColumns;
        }
        int numRows = (int) Math.ceil(heatmapSelection.size() / (double) numColumns);
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
        screenViewContainer.removeAll();

        ((TableLayout) screenViewContainer.getLayout()).setRow(rowConfig);
        ((TableLayout) screenViewContainer.getLayout()).setColumn(columnConfig);

        // track changes of the batch; this allows to alter the background color of the heatmaps
        String lastBatchName = "bubabuba";
        Color batchBcknd = Color.GRAY;


        // populate the view with plates
        int plateNameFontSize = Utils.isWindowsPlatform() ? 8 : 12;
        Font barcodeFont = new Font("Serif", Font.PLAIN, plateNameFontSize);

        for (int i = 0; i < heatmapSelection.size(); i++) {
            PlateOverviewHeatMap heatMapPanel = heatmapSelection.get(i);
            Plate plate = heatMapPanel.getPlate();

            int rowIndex = i / numColumns;
            String gridPosition = (i - rowIndex * numColumns) + ", " + (rowIndex);

            JPanel plateContainer = new JPanel();
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

            screenViewContainer.add(plateContainer, gridPosition);
            plateContainer.add(heatMapPanel, BorderLayout.CENTER);
        }

        screenViewContainer.setPreferredSize(new Dimension(numColumns * MIN_HEATMAP_WIDTH, numRows * MIN_HEATMAP_WIDTH));
        invalidate();
        updateUI();
        repaint();
    }

    private void sortHeatmaps() {
        heatmaps.clear();
        List<Plate> plates = heatMapModel.getScreen();

        heatmaps = new ArrayList<PlateOverviewHeatMap>();
        for (Plate plate : plates) {
            heatmaps.add(new PlateOverviewHeatMap(plate, heatMapModel));
        }

    }


    /*private void menuItem1ActionPerformed(ActionEvent e) {
        List<PlateOverviewHeatMap> heatmapSelection = getFilteredHeatMap();
        List<Plate> curPlateSelection = new ArrayList<Plate>();
        for (PlateOverviewHeatMap plateOverviewHeatMap : heatmapSelection) {
            curPlateSelection.add(plateOverviewHeatMap.getPlate());
        }

        new AvgerageZStack(curPlateSelection);

    }
*/

    private void parsePlateBarCodes() {
        for (PlateOverviewHeatMap heatmap : heatmaps) {
            Plate plate = heatmap.getPlate();
            if (plate.getScreenedAt() != null) {
                continue;
            }

            try {
                Plate.configurePlateByBarcode(plate, BarcodeParserFactory.getAssayPlateBarcodeOLDParser(plate.getBarcode()));
            } catch (Throwable t) {
                // do nothing here
                heatMapViewerMenu.setSortingEnabled(false);
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        panel2 = new JScrollPane();
        screenViewContainer = new JPanel();
        toolBar2 = new JToolBar();
        colorBar = new ColorBar();
        menuBar2 = new JToolBar();
        heatMapViewerMenu = new HeatMapViewerMenu();
        label4 = new JLabel();
        plateFilter = new JTextField();
        zoomOutButton = new JButton();
        zoomInButton = new JButton();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            //======== screenViewContainer ========
            {
                screenViewContainer.setLayout(new TableLayout(new double[][]{
                        {TableLayout.PREFERRED},
                        {TableLayout.PREFERRED}}));
                ((TableLayout) screenViewContainer.getLayout()).setHGap(5);
                ((TableLayout) screenViewContainer.getLayout()).setVGap(5);
            }
            panel2.setViewportView(screenViewContainer);
        }
        add(panel2, BorderLayout.CENTER);

        //======== toolBar2 ========
        {
            toolBar2.add(colorBar);
        }
        add(toolBar2, BorderLayout.SOUTH);

        //======== menuBar2 ========
        {
            menuBar2.add(heatMapViewerMenu);

            //---- label4 ----
            label4.setText("Filter plates:");
            menuBar2.add(label4);

            //---- plateFilter ----
            plateFilter.setMaximumSize(new Dimension(100, 30));
            plateFilter.setMinimumSize(new Dimension(100, 30));
            plateFilter.setPreferredSize(new Dimension(100, 30));
            menuBar2.add(plateFilter);

            //---- zoomOutButton ----
            zoomOutButton.setText("-");
            zoomOutButton.setToolTipText("zoom out");
            menuBar2.add(zoomOutButton);

            //---- zoomInButton ----
            zoomInButton.setText("+");
            zoomInButton.setToolTipText("zoom in");
            menuBar2.add(zoomInButton);
        }
        add(menuBar2, BorderLayout.NORTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JScrollPane panel2;
    private JPanel screenViewContainer;
    private JToolBar toolBar2;
    private ColorBar colorBar;
    private JToolBar menuBar2;
    private HeatMapViewerMenu heatMapViewerMenu;
    private JLabel label4;
    private JTextField plateFilter;
    private JButton zoomOutButton;
    private JButton zoomInButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public List<PlateOverviewHeatMap> getFilteredHeatMap() {

        List<PlateOverviewHeatMap> heatMapSelection = new ArrayList<PlateOverviewHeatMap>();
        for (PlateOverviewHeatMap heatmap : heatmaps) {
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
        for (PlateOverviewHeatMap heatmap : heatmaps) {
            plateWells.put(heatmap.getPlate(), new ArrayList<Well>());
        }

        heatMapModel.setWellSelection(wellSelection);
        repaint();

//        heatMapModel.setWellSelection(wellSelection);
//        for (Well well : wellSelection) {
//            plateWells.get(well.getPlate()).add(well);
//        }
//
//        // propagate the selection to the different panels
//        for (PlateOverviewHeatMap heatmap : heatmaps) {
//            Collection<Well> plateSelection = plateWells.get(heatmap.getPlate());
//
//            heatmap.setSelection(plateSelection);
//        }
    }


    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repopulatePlateGrid();
        }
    }


    public static void main(String[] args) {

        File plateListBinFile = new File(args[0] + "/plateList.bin");
        List<Plate> plates = null;

        // read a bunch of plates as stored by the PlateViewer node
        try {
            if (plateListBinFile.isFile()) {
                FileInputStream f_in = new FileInputStream(plateListBinFile);

                // Read object using ObjectInputStream
                ObjectInputStream obj_in = new ObjectInputStream(new BufferedInputStream(f_in));

                // Read an object
                plates = (List<Plate>) obj_in.readObject();
            }

        } catch (Throwable e) {
        }

        // display screen explorer containing these plates
        JFrame f = new ScreenPanelFrame(plates);

        /*ScreenPanel showScreen = new ScreenPanel(plates);
        f.add(showScreen);

        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.setSize( 300, 200 );
        f.setVisible( true ); */
    }
}

