package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ColorBar;
import de.mpicbg.tds.knime.knutils.Attribute;
import org.knime.core.data.*;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.*;
import org.knime.core.node.config.Config;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;
import org.openscience.jchempaint.renderer.generators.ControllerFeedbackGenerator;

import javax.swing.*;
import javax.swing.text.TableView;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */

public class ScreenHeatMapsFrame extends JFrame implements HiLiteListener{

    private ScreenPanel screenPanel;
    private HeatMapMenu menus;
    private HeatMapToolbar toolbar;
    private ColorBar colorbar;


    // Constructor
    public ScreenHeatMapsFrame(){
        setTitle("HCS Heat-map Viewer");
        menus = new HeatMapMenu();
        screenPanel = new ScreenPanel();
        toolbar = new HeatMapToolbar();
        colorbar = new ColorBar();
        setJMenuBar(menus);
        add(screenPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);
        add(colorbar, BorderLayout.SOUTH);
        setBounds(150, 150, 800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
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

//        // Read the table spec.
//        FileInputStream stream = null;
//        try {
//            stream = new FileInputStream(new File("/Users/turf/Sources/CBG/HCS-Tools/test/spec.xml"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        NodeSettings settings = null;
//        try {
//            settings = (NodeSettings) NodeSettings.loadFromXML(stream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        DataTableSpec specs = null;
//        try {
//            specs = DataTableSpec.load(settings);
//        } catch (InvalidSettingsException e) {
//            e.printStackTrace();
//        }
//
//        // Read the table data.
//        DataContainer container = new DataContainer(specs);
//        ContainerTable output = null;
//        try {
//            output = container.readFromZip(new File("/Users/turf/Sources/CBG/HCS-Tools/test/data.zip"));
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        System.exit(0);
    }
}
