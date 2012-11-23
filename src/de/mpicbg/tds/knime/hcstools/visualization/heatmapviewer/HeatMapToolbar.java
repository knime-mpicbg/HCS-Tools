package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;

/**
 * User: Felix Meyenhofer
 * Date: 10/12/12
 * Time: 21:17
 * To change this template use File | Settings | File Templates.
 */

public class HeatMapToolbar extends JToolBar {

    private HeatMapModel heatMapModel;
    private WellPropertySelector readoutSelector;
    private WellPropertySelector overlaySelector;
    private WellPropertySelector filterSelector;
    private JFormattedTextField filterString;


    // Constructor
    public HeatMapToolbar() {
        setPreferredSize(new Dimension(600, 30));

        add(new JLabel("Readout:"));
        readoutSelector = new WellPropertySelector();
        readoutSelector.setPreferredSize(new Dimension(150, -1));
        add(readoutSelector);
        addSeparator();

        add(new JLabel("Overlay:"));
        overlaySelector = new WellPropertySelector();
        overlaySelector.setPreferredSize(new Dimension(150, -1));
        add(overlaySelector);
        addSeparator();

        add(new JLabel("Filter Plates by:"));
        filterSelector = new WellPropertySelector();
        filterSelector.setPreferredSize(new Dimension(150, -1));
        add(filterSelector);
        filterString = new JFormattedTextField();
        filterString.setPreferredSize(new Dimension(100, 20));
        add(filterString);
    }


    protected void configure(HeatMapModel heatMapModel) {
        this.heatMapModel = heatMapModel;

        // populate the overlay menu based on the first plate
        java.util.List<Plate> subScreen = Arrays.asList(heatMapModel.getScreen().get(0));

        // reconfigure the selectors
        java.util.List<String> annotTypes = TdsUtils.flattenAnnotationTypes(subScreen);
        annotTypes.add(0, "");
        overlaySelector.configure(annotTypes, heatMapModel, SelectorType.ANNOATION);
        java.util.List<String> readoutNames = TdsUtils.flattenReadoutNames(subScreen);
        readoutSelector.configure(readoutNames, heatMapModel, SelectorType.READOUT);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("HeatMapToolbar Test");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextArea text = new JTextArea("Just some text");
        text.setEnabled(true);
        text.setEditable(false);
        panel.add(text);
        panel.add(new HeatMapToolbar());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
