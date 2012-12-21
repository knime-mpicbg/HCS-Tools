package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;
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
    @SuppressWarnings("FieldCanBeLocal")
    private HeatPlate heatMap;
    private JPanel heatMapContainer;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;


    /**
     * Constructor
     * @param model an instance of the HeatMapModel class.
     */
    public PlateViewer(HeatMapModel2 model) {
        if ( model != null )
            this.heatMapModel = model;

        this.initialize();
        this.setTitle("HCS Plate Viewer");

        heatMapModel.addChangeListener(this);

        colorbar.configure(heatMapModel);

        new PanelImageExporter(heatMapContainer, true);
    }

    public PlateViewer(HeatTrellis parent,Plate plate, HeatMapModel2 heatMapModel) {
        this(heatMapModel);
        this.updater = parent;

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

        List<Plate> pseudoScreen = new ArrayList<Plate>();
        pseudoScreen.add(plate);
        this.heatMapModel.setScreen(pseudoScreen);

        setTitle(plate.getBarcode());

        toolbar.configure(heatMapModel);
        colorbar.configure(heatMapModel);

        heatMapModel.setScreen(Arrays.asList(plate));

        heatMap = new HeatPlate(this, plate, heatMapModel);
        heatMapContainer.add(heatMap);
        Dimension ms = heatMap.getPreferredSize();
        heatMapContainer.setPreferredSize(new Dimension(ms.width+10, ms.height+10));

        Random posJitter = new Random();
        setLocation(200 + posJitter.nextInt(100), 200 + posJitter.nextInt(100));
        pack();
        setResizable(false);
        setVisible(true);
    }


    private void initialize() {
        PlateMenu menu = new PlateMenu(this);
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
        PlateViewer plateViewer = new PlateViewer(new HeatMapModel2());
        plateViewer.setSize(new Dimension(600, 300));
        plateViewer.setVisible(true);
    }

}
