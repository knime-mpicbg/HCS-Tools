package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;

import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 * Time: 11:53
 *
 * Creates a window containing all the heat-maps of a screen.
 */

public class ScreenViewer extends JFrame implements HiLiteListener{

    private HeatTrellis heatTrellis;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;
    private HeatMapModel2 heatMapModel;


    // Constructor
    public ScreenViewer(){
        this(null);
    }

    public ScreenViewer(List<Plate> plates) {
        heatMapModel = new HeatMapModel2();
        heatMapModel.setScreen(plates);
        initialize();
        configure();
        setBounds(150, 150, 810, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    private void initialize() {
        toolbar = new HeatMapInputToolbar();
        colorbar = new HeatMapColorToolBar();
        heatTrellis = new HeatTrellis();

        add(toolbar, BorderLayout.NORTH);
        add(heatTrellis, BorderLayout.CENTER);
        add(colorbar, BorderLayout.SOUTH);

        HeatMapMenu menus = new HeatMapMenu(this);
        setTitle("HCS Heatmap Viewer");
        setJMenuBar(menus);
    }

    private void configure() {
        heatTrellis.configure(heatMapModel);
        toolbar.configure(heatMapModel);
        colorbar.configure(heatMapModel);
    }


    // HiLiteListener methods.
    public void hiLite(final KeyEvent event) {
    }

    public void unHiLite(final KeyEvent event) {
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


    public HeatTrellis getHeatTrellis() {
        return heatTrellis;
    }

    public HeatMapModel2 getHeatMapModel() {
        return heatMapModel;
    }


    public void toggleColorbarVisibility(boolean flag) {
        this.colorbar.setVisible(flag);
    }

    public void toggleToolbarVisibility(boolean flag) {
        this.toolbar.setVisible(flag);
    }


    public static void main(String[] args) {
        new ScreenViewer();
    }


}
