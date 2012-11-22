package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;

/**
 * User: Felix Meyenhofer
 * Date: 10/12/12
 * Time: 21:17
 * To change this template use File | Settings | File Templates.
 */
public class HeatMapToolbar {

    private JToolBar toolBar;
    private de.mpicbg.tds.core.view.WellPropertySelector readoutSelector;
    private de.mpicbg.tds.core.view.WellPropertySelector overlaySelector;
    private de.mpicbg.tds.core.view.WellPropertySelector filterSelector;
    private JFormattedTextField filterString;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;

    public JToolBar createJToolBar() {
        toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(600,30));
        label1 = new JLabel();
        label1.setText("Readout:");
        toolBar.add(label1);
        readoutSelector = new de.mpicbg.tds.core.view.WellPropertySelector();
        readoutSelector.setPreferredSize(new Dimension(150, -1));
        toolBar.add(readoutSelector);
        toolBar.addSeparator();
        label2 = new JLabel();
        label2.setText("Overlay:");
        toolBar.add(label2);
        overlaySelector = new de.mpicbg.tds.core.view.WellPropertySelector();
        overlaySelector.setPreferredSize(new Dimension(150, -1));
        toolBar.add(overlaySelector);
        toolBar.addSeparator();
        label3 = new JLabel("Filter Plates by:");
        toolBar.add(label3);
        filterSelector = new de.mpicbg.tds.core.view.WellPropertySelector();
        filterSelector.setPreferredSize(new Dimension(150, -1));
        toolBar.add(filterSelector);
        filterString = new JFormattedTextField();
        filterString.setPreferredSize(new Dimension(100, 20));
        toolBar.add(filterString);
        return toolBar;
    }




    public static void main(String[] args) {
        JFrame frame = new JFrame("HeatMapToolbar");
        frame.setContentPane(new JPanel());
        HeatMapToolbar thisclass = new HeatMapToolbar();
        frame.setContentPane(thisclass.createJToolBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


}
