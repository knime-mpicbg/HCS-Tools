package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
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

    public PlateViewer(Plate plate, HeatMapModel2 heatMapModel) {
        this(heatMapModel);

        List<Plate> pseudoScreen = new ArrayList<Plate>();
        pseudoScreen.add(plate);
        this.heatMapModel.setScreen(pseudoScreen);

        setTitle(plate.getBarcode());

        toolbar.configure(heatMapModel);
        colorbar.configure(heatMapModel);

        heatMapModel.setScreen(Arrays.asList(plate));

        heatMap = new HeatPlate(plate, heatMapModel);
        heatMapContainer.add(heatMap);

        Random posJitter = new Random();
        this.setBounds(200 + posJitter.nextInt(100), 200 + posJitter.nextInt(100), 630, 500);
        setVisible(true);
    }


    private void initialize() {
        PlateMenu menu = new PlateMenu(this);
        setJMenuBar(menu);

        setLayout(new BorderLayout());

        toolbar = new HeatMapInputToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        heatMapContainer = new JPanel();
        heatMapContainer.setLayout(new BorderLayout());
        heatMapContainer.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        add(heatMapContainer, BorderLayout.CENTER);

        colorbar = new HeatMapColorToolBar();
        add(colorbar, BorderLayout.SOUTH);
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
