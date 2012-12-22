package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateAttribute;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateComparators;

/**
 * Author: Felix Meyenhofer
 * Date: 10/12/12
 *
 * HeatMapViewer Toolbar
 */

public class HeatMapInputToolbar extends JToolBar {

    // The toolbar size influences the automatic size (pack()) of all windows it is used in.
    public final int TOOLBAR_HEIGHT = 30;
    public final int READOUT_WIDTH = 250;
    public final int OVERLAY_WIDTH = 100;
    public final int FILTER_WIDTH = 300;

    private HeatMapModel2 heatMapModel;
    private WellAttributeComboBox readoutSelector;
    private WellAttributeComboBox overlaySelector;
    private JComboBox filterSelector;
    private HeatMapViewer parent;


    // Constructor
    public HeatMapInputToolbar(HeatMapViewer parent) {
        this.parent = parent;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(new JLabel("Readout:"));
        readoutSelector = new WellAttributeComboBox();
        readoutSelector.setPreferredSize(new Dimension(READOUT_WIDTH, -1));
        add(readoutSelector);
        addSeparator();

        add(new JLabel("Overlay:"));
        overlaySelector = new WellAttributeComboBox();
        overlaySelector.setPreferredSize(new Dimension(OVERLAY_WIDTH, -1));
        add(overlaySelector);

        // Add the filter functionality for the ScreenViewer.
        if ( (parent == null ) || (parent instanceof ScreenViewer) ) {
            addSeparator();
            add(new JLabel("Filter Plates by:"));
            filterSelector = new JComboBox();
            filterSelector.setPreferredSize(new Dimension(FILTER_WIDTH/2, -1));
            add(filterSelector);
            JFormattedTextField filterString = new JFormattedTextField();
            filterString.setPreferredSize(new Dimension(FILTER_WIDTH/2, (int) Math.round(TOOLBAR_HEIGHT/3.0*2)));
            filterString.setMaximumSize(new Dimension(FILTER_WIDTH, (int) Math.round(TOOLBAR_HEIGHT/3.0*2)));
            filterString.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    filterPlatesAction(actionEvent);
                }
            });
            add(filterString);

            setPreferredSize(new Dimension(READOUT_WIDTH + OVERLAY_WIDTH + FILTER_WIDTH, TOOLBAR_HEIGHT));
        } else {
            setPreferredSize(new Dimension(READOUT_WIDTH + OVERLAY_WIDTH, TOOLBAR_HEIGHT));
        }
    }


    private void filterPlatesAction(ActionEvent event) {
        String filterString = event.getActionCommand();
        String filterAttribute = (String) filterSelector.getSelectedItem();
        heatMapModel.filterPlates(filterString, PlateUtils.getPlateAttributeByTitle(filterAttribute));
        heatMapModel.fireModelChanged();
    }


    // Configure the gui components with the menu items.
    protected void configure(HeatMapModel2 hmm) {
        heatMapModel = hmm;

        // populate the overlay menu based on the first plate
        if (heatMapModel.getScreen() == null) {
            System.err.println("Could not configure the Toolbar, since there is no data loaded.");
            return;
        }

        List<Plate> subScreen = Arrays.asList(hmm.getScreen().get(0));

        // reconfigure the selectors
        List<String> annotations = PlateUtils.flattenAnnotationTypes(subScreen);
        annotations.add(0, "");
        overlaySelector.configure(annotations, heatMapModel, AttributeType.OVERLAY_ANNOTATION);

        List<String> readouts = PlateUtils.flattenReadoutNames(subScreen);
        readoutSelector.configure(readouts, heatMapModel, AttributeType.READOUT);

        // Configure the filter functionality for the ScreenViewer.
        if ( (parent == null ) || (parent instanceof ScreenViewer) ) {
            Collection<PlateAttribute> plateAttributes = heatMapModel.getPlateAttributes();
            DefaultComboBoxModel model = new DefaultComboBoxModel(PlateUtils.getPlateAttributeTitles(plateAttributes));
            filterSelector.setModel(model);
        }
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
        panel.add(new HeatMapInputToolbar(null));
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
