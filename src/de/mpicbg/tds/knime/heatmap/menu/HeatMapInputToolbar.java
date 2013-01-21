package de.mpicbg.tds.knime.heatmap.menu;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.heatmap.HeatMapViewer;
import de.mpicbg.tds.knime.heatmap.PlateViewer;
import de.mpicbg.tds.knime.heatmap.ScreenViewer;
import de.mpicbg.tds.knime.heatmap.model.PlateUtils;
import de.mpicbg.tds.knime.heatmap.model.Plate;
import de.mpicbg.tds.knime.heatmap.model.PlateAttribute;

/**
 * HeatMapViewer toolbar to select the parameter that determine the displayed content
 *
 * @author Felix Meyenhofer
 *         10/12/12
 */

public class HeatMapInputToolbar extends JToolBar {

    /** The toolbar size influences the automatic size (pack()) of all windows it is used in. */
    public final int TOOLBAR_HEIGHT = 30;
    public final int READOUT_WIDTH = 150;
    public final int OVERLAY_WIDTH = 100;
    public final int FILTER_WIDTH = 280;

    /** Data model */
    private HeatMapModel heatMapModel;

    /** Combobox to select the readout attribute */
    private WellAttributeComboBox readoutSelector;
    /** Combobox to select the overlay factor */
    private WellAttributeComboBox overlaySelector;
    /** Combobox to select the plate attribute for plate filtering */
    private JComboBox filterSelector;
    /** Parent viewer */
    private HeatMapViewer parent;


    /**
     * Constructor for initialization and configuration of the UI components
     */
    public HeatMapInputToolbar(HeatMapViewer parent) {
        this.setPreferredSize(new Dimension(READOUT_WIDTH + OVERLAY_WIDTH +FILTER_WIDTH +100, -1));
        this.parent = parent;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(new JLabel("Readout:"));
        readoutSelector = new WellAttributeComboBox();
        readoutSelector.setPreferredSize(new Dimension(READOUT_WIDTH, -1));
        readoutSelector.setMaximumSize(new Dimension(READOUT_WIDTH + 300, 50));
        add(readoutSelector);
        addSeparator();

        add(new JLabel("Overlay:"));
        overlaySelector = new WellAttributeComboBox();
        overlaySelector.setPreferredSize(new Dimension(OVERLAY_WIDTH, -1));
        overlaySelector.setMaximumSize(new Dimension(OVERLAY_WIDTH+200, 50));
        add(overlaySelector);

        // Add the filter functionality for the ScreenViewer.
        if ( (parent == null ) || !(parent instanceof PlateViewer) ) {
            addSeparator();
            add(new JLabel("Filter by:"));
            filterSelector = new JComboBox();
            filterSelector.setPreferredSize(new Dimension(FILTER_WIDTH/2, -1));
            filterSelector.setMaximumSize(new Dimension(FILTER_WIDTH/2+50, 50));
            add(filterSelector);
            JFormattedTextField filterString = new JFormattedTextField();
            filterString.setPreferredSize(new Dimension(FILTER_WIDTH/2, (int) Math.round(TOOLBAR_HEIGHT/3.0*2)));
            filterString.setMaximumSize(new Dimension(FILTER_WIDTH/2 +50, (int) Math.round(TOOLBAR_HEIGHT/3.0*2)));
            filterString.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    filterPlatesAction(actionEvent);
                }
            });
            filterString.setToolTipText("<html>The date format needs to one of the following:<br/>" +
                    "yyMMdd<br/>" +
                    "yyyyMMdd<br/>" +
                    "separators are filtered out (-_;:. etc.)");
            add(filterString);

            setPreferredSize(new Dimension(READOUT_WIDTH + OVERLAY_WIDTH + FILTER_WIDTH, TOOLBAR_HEIGHT));
        } else {
            setPreferredSize(new Dimension(READOUT_WIDTH + OVERLAY_WIDTH, TOOLBAR_HEIGHT));
        }
    }


    /**
     * Action executed on presssing enter in the filter string text edit box
     *
     * @param event filter event
     */
    private void filterPlatesAction(ActionEvent event) {
        String filterString = event.getActionCommand();
        String filterAttribute = (String) filterSelector.getSelectedItem();
        heatMapModel.filterPlates(filterString, PlateUtils.getPlateAttributeByTitle(filterAttribute));
        heatMapModel.fireModelChanged();
    }


    /**
     * Configure the gui components with the menu items.
     *
     * @param hmm data model delivering the content
     */
    public void configure(HeatMapModel hmm) {              //TODO: Find a better way for the component resizing (currently some components are not visible if the window size is small)
        heatMapModel = hmm;

        // populate the overlay menu based on the first plate
        if (heatMapModel.getScreen() == null) {
            System.err.println("Could not configure the Toolbar, since there is no data loaded.");
            return;
        }

        List<Plate> subScreen = Arrays.asList(hmm.getScreen().get(0));

        // reconfigure the selectors
        List<String> annotations = PlateUtils.flattenAnnotationTypes(subScreen);
        if ( heatMapModel.hasKnimeColorModel() )
            annotations.add(0, heatMapModel.getKnimeColorAttributeTitle());
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


    /**
     * Quick testing
     *
     * @param args whatever
     */
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
