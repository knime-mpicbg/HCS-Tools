package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */

public class ScreenHeatMapsFrame implements HiLiteListener{

    private JPanel panel;
    private HeatMapMenu menus;
    private HeatMapToolbar toolbar;

    public ScreenHeatMapsFrame(){
        JFrame frame = new JFrame("HCS Heat-map Viewer");
        menus = new HeatMapMenu();
        panel = new JPanel();
        toolbar = new HeatMapToolbar();
        frame.setJMenuBar(menus.createJMenuBar());
        frame.setContentPane(panel);
        frame.setContentPane(toolbar.createJToolBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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
