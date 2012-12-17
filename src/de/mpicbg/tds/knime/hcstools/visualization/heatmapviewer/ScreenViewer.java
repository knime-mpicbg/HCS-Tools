package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;

import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;
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
    protected HeatMapMenu menus;


    // Constructor
    public ScreenViewer(){
        this(null);
    }

    public ScreenViewer(List<Plate> plates) {
        if ( (plates == null) ) {
            heatTrellis = new HeatTrellis();
        } else {
            heatTrellis = new HeatTrellis(plates);
        }

        menus = new HeatMapMenu(heatTrellis);
        setTitle("HCS Heat-map Viewer");
        setJMenuBar(menus);
        add(heatTrellis);
        setBounds(150, 150, 810, 600);
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


}
