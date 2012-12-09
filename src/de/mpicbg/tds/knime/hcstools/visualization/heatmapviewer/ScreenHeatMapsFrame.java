package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ColorBar;

import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */

public class ScreenHeatMapsFrame extends JFrame implements HiLiteListener{

//    private ScreenPanel screenPanel;
    private ScreenHeatMapsPanel screenPanel;
    protected HeatMapMenu menus;


    // Constructor
    public ScreenHeatMapsFrame(){
        setTitle("HCS Heat-map Viewer");
        screenPanel = new ScreenHeatMapsPanel(menus);
        menus = new HeatMapMenu(screenPanel);
        setJMenuBar(menus);
        add(screenPanel);
        setBounds(150, 150, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        pack();
        setVisible(true);
    }

    public ScreenHeatMapsFrame(List<Plate> plates) {
        setTitle("HCS Heat-map Viewer");
        screenPanel = new ScreenHeatMapsPanel(menus, plates);
        menus = new HeatMapMenu(screenPanel);
        setJMenuBar(menus);
        add(screenPanel);
        setBounds(150, 150, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        pack();
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


    public void setData(ScreenHeatMapsFrame data) {
    }

    public void getData(ScreenHeatMapsFrame data) {
    }

    public boolean isModified(ScreenHeatMapsFrame data) {
        return false;
    }


    public static void main(String[] args) {
        new ScreenHeatMapsFrame();
    }
}
