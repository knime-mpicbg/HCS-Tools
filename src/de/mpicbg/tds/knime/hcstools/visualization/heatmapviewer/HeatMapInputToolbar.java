package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.PlateComparators;

/**
 * User: Felix Meyenhofer
 * Date: 10/12/12
 * Time: 21:17
 * To change this template use File | Settings | File Templates.
 */

public class HeatMapInputToolbar extends JToolBar {

    private HeatMapModel2 heatMapModel;
    private WellAttributeComboBox readoutSelector;
    private WellAttributeComboBox overlaySelector;
    private JComboBox filterSelector;
    private JFormattedTextField filterString;


    // Constructor
    public HeatMapInputToolbar() {
        setPreferredSize(new Dimension(600, 30));

        add(new JLabel("Readout:"));
        readoutSelector = new WellAttributeComboBox();
        readoutSelector.setPreferredSize(new Dimension(250, -1));
        add(readoutSelector);
        addSeparator();

        add(new JLabel("Overlay:"));
        overlaySelector = new WellAttributeComboBox();
        overlaySelector.setPreferredSize(new Dimension(100, -1));
        add(overlaySelector);
        addSeparator();

        add(new JLabel("Filter Plates by:"));
        filterSelector = new JComboBox();
        filterSelector.setPreferredSize(new Dimension(100, -1));
        add(filterSelector);
        filterString = new JFormattedTextField();
        filterString.setMinimumSize(new Dimension(100, 20));
        filterString.setPreferredSize(new Dimension(100,20));
        filterString.setMaximumSize(new Dimension(300,20));
        filterString.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                filterPlatesAction(actionEvent);
            }
        });
        add(filterString);
    }


    private void filterPlatesAction(ActionEvent event) {
        String filterString = event.getActionCommand();
        String filterAttribute = (String) filterSelector.getSelectedItem();
        heatMapModel.filterPlates(filterString, PlateComparators.getPlateAttributeByTitle(filterAttribute));
    }


    // Configure the gui components with the menu items.
    protected void configure(HeatMapModel2 hmm) {
        heatMapModel = hmm;

        // populate the overlay menu based on the first plate
        java.util.List<Plate> subScreen = Arrays.asList(hmm.getScreen().get(0));

        // reconfigure the selectors
        java.util.List<String> annotations = TdsUtils.flattenAnnotationTypes(subScreen);
        annotations.add(0, "");
        overlaySelector.configure(annotations, heatMapModel, AttributeType.OVERLAY_ANNOTATION);
        Collection<PlateComparators.PlateAttribute> plateAttributes = heatMapModel.getPlateAttributes();
        DefaultComboBoxModel model = new DefaultComboBoxModel(PlateComparators.getPlateAttributeTitles(plateAttributes));
        filterSelector.setModel(model);
        java.util.List<String> readouts = TdsUtils.flattenReadoutNames(subScreen);
        readoutSelector.configure(readouts, heatMapModel, AttributeType.READOUT);
    }


    // Reveal yourself!
    public static void main(String[] args) {
        JFrame frame = new JFrame("HeatMapInputToolbar Test");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextArea text = new JTextArea("Just some text");
        text.setEnabled(true);
        text.setEditable(false);
        panel.add(text);
        panel.add(new HeatMapInputToolbar());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
