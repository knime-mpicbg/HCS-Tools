package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;

/**
 * User: Felix Meyenhofer
 * Date: 12/8/12
 * Time: 1:35
 *
 * Colorbar to integrate in a heatmap frame.
 */

public class HeatMapColorToolBar extends JToolBar {

    private HeatMapModel heatMapModel;
    private ColorGradientPanel colorPanel;


    // Constructors
    public HeatMapColorToolBar() {
        initialize();
    }

    public HeatMapColorToolBar(HeatMapModel model) {
        this();
        configure(model);
    }


    private void initialize() {
        colorPanel = new ColorGradientPanel();
        add(colorPanel);
    }

    private void configure(HeatMapModel model) {
        heatMapModel = model;
        colorPanel.configure(model.colorGradient);

    }


    public static void main(String[] args) {
        HeatMapModel model = new HeatMapModel();
        ColorGradientDialog dialog = new ColorGradientDialog();
        dialog.setVisible(true);
        model.setColorGradient(dialog.getGradientPainter());
        HeatMapColorToolBar toolBar = new HeatMapColorToolBar(model);
        JFrame frame = new JFrame();
        frame.add(toolBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
