package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Random;

/**
 * User: Felix Meyenhofer
 * Date: 20/12/12
 *
 * Creates a window for a detailed plate view.
 * Replaces PlatePanel
 */

public class PlateViewer extends JFrame implements HeatMapModelChangeListener, HeatMapViewer {

    private HeatTrellis updater;
    private HeatMapModel2 heatMapModel;

    private JPanel heatMapContainer;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;
    private PlateMenu menu;


    /**
     * Constructors
     */
    public PlateViewer() {
        this.initialize();
    }

    public PlateViewer(HeatTrellis parent, Plate plate) {
        this();
        this.updater = parent;

        // Create a new instance of the HeatMapModel and copy some attributes.
        HeatMapModel2 model = new HeatMapModel2();
        model.setCurrentReadout(parent.heatMapModel.getSelectedReadOut());
        model.setOverlay(parent.heatMapModel.getOverlay());
        model.setColorScheme(parent.heatMapModel.getColorScheme());
        model.setHideMostFreqOverlay(parent.heatMapModel.doHideMostFreqOverlay());
        model.setWellSelection(parent.heatMapModel.getWellSelection());

        if ( parent.heatMapModel.isGlobalScaling() ) {
            this.menu.colorMenu.setEnabled(!parent.heatMapModel.isGlobalScaling());
        model.setScreen(parent.heatMapModel.getScreen());
        model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategy());
        } else {
            model.setScreen(Arrays.asList(plate));
            model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategyInstance());
        }

        this.heatMapModel = model;

        // Register the Viewer in the ChangeListeners
        parent.heatMapModel.addChangeListener(this);
        this.heatMapModel.addChangeListener(this);

        // Configure
        this.menu.configure(this.heatMapModel);
        this.toolbar.configure(this.heatMapModel);
        this.colorbar.configure(this.heatMapModel);

        // Give a meaningful title.
        this.setTitle("Plate Viewer (" + heatMapModel.getScreen().get(0).getBarcode() + ")");

        // Remove the HeatMapModelChangeListener when closing the window.
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                PlateViewer viewer = (PlateViewer) windowEvent.getSource();
                HeatMapModel2 model = viewer.updater.getHeatMapModel();
                model.removeChangeListener(viewer);
                viewer.setVisible(false);
            }
        });

        // Configure the toolbars.
        toolbar.configure(this.heatMapModel);
        colorbar.configure(this.heatMapModel);

        // Add the plate heatmap
        HeatPlate heatMap = new HeatPlate(this);
        heatMapContainer.add(heatMap);

        // Add "save image" functionality
        new PanelImageExporter(heatMapContainer, true);

        // Set the window dimensions given by the plate heatmap size.
        Dimension ms = heatMap.getPreferredSize();
        heatMapContainer.setPreferredSize(new Dimension(ms.width+10, ms.height+10));
        pack();
        setResizable(false);

        // Set the location of the new PlateViewer
        Random posJitter = new Random();
        double left = Toolkit.getDefaultToolkit().getScreenSize().getWidth() - this.getWidth() - 100;
        setLocation((int) left + posJitter.nextInt(100), 200 + posJitter.nextInt(100));
//        setVisible(true);
    }


    /**
     * Helper Methods
     */
    private void initialize() {
        menu = new PlateMenu(this);
        setJMenuBar(menu);

        setLayout(new BorderLayout());

        toolbar = new HeatMapInputToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        heatMapContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
        heatMapContainer.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
        add(heatMapContainer, BorderLayout.CENTER);

        colorbar = new HeatMapColorToolBar();
        add(colorbar, BorderLayout.SOUTH);
    }


    public HeatTrellis getUpdater() {
        return updater;
    }


    /**
     * The HeatMapModelChangeListener interface.
     */
    @Override
    public void modelChanged() {
        if (isVisible() && getWidth() > 0)
            repaint();
    }


    /**
     * Viewer interface
     */
    @Override
    public void toggleToolbarVisibility(boolean visibility) {
        toolbar.setVisible(visibility);
    }

    @Override
    public void toggleColorbarVisibility(boolean visibility) {
        colorbar.setVisible(visibility);
    }

    @Override
    public HeatMapModel2 getHeatMapModel() {
        return heatMapModel;
    }


    /**
     * Quick testing.
     */
    public static void main(String[] args) {
        PlateViewer plateViewer = new PlateViewer();
        plateViewer.heatMapModel = new HeatMapModel2();
        plateViewer.setSize(new Dimension(600, 300));
        plateViewer.setVisible(true);
    }

}
