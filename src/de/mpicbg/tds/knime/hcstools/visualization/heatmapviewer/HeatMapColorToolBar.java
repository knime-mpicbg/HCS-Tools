package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ReadoutRescaleStrategy;

/**
 * User: Felix Meyenhofer
 * Date: 12/8/12
 * Time: 1:35
 *
 * Colorbar to integrate in a heatmap frame.
 * TODO: When changing the orientation of the toolbar, the layout should be changed and the colorgradient rotated.
 */

public class HeatMapColorToolBar extends JToolBar {

    private HeatMapModel2 heatMapModel;
    private ColorGradientPanel colorPanel;
    private JLabel minLabel = new JLabel("min");
    private JLabel medLabel = new JLabel("middle");
    private JLabel maxLabel = new JLabel("max");
    private Color missingValue = new Color(255,0,255);

    public static final DecimalFormat scienceFormat = new DecimalFormat("0.###E0");
    public static final DecimalFormat basicFormat = new DecimalFormat("######.###");


    // Constructors
    public HeatMapColorToolBar() {
        initialize();
    }

    public HeatMapColorToolBar(HeatMapModel2 model) {
        this();
        configure(model);
    }


    private void initialize() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Create a panel with the labels for the colorbar.
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.add(minLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(medLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(maxLabel);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0.2;
        constraints.fill = GridBagConstraints.BOTH;
        add(labelPanel, constraints);

        // Create a label for the panel indicating the missing value color.
        constraints.gridx = 1;
        constraints.weightx = -1;
        JLabel missLabel = new JLabel("  NaN  ");
        missLabel.setHorizontalAlignment(JLabel.CENTER);
        add(missLabel, constraints);

        // Add the color panel
        colorPanel = new ColorGradientPanel();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.8;
        add(colorPanel, constraints);

        // Add the panel indicating the missing value color.
        JPanel missPanel = new JPanel();
        missPanel.setBackground(missingValue);
        constraints.gridy = 1;
        constraints.gridx = 1;
        constraints.weightx = -1;
        add(missPanel, constraints);
    }

    protected void configure(HeatMapModel2 model) {
        heatMapModel = model;
        if ( model.colorGradient == null ) {
            model.colorGradient = colorPanel.getGradientPainter();
            System.err.println("The HeatMapModels colorGradient attribute is not set, taking the default from ColorGradientPanel.");
        } else {
            colorPanel.configure(model.colorGradient);
        }

        ReadoutRescaleStrategy displayNormStrategy = heatMapModel.getRescaleStrategy();
        if ( heatMapModel.getSelectedReadOut() == null ) {
            System.err.println("No Readout is selected, can't calculate the minimum and maximum value of the color bar.");
        } else {
            Double minValue = displayNormStrategy.getMinValue(heatMapModel.getSelectedReadOut());
            Double maxValue = displayNormStrategy.getMaxValue(heatMapModel.getSelectedReadOut());
            assert minValue < maxValue : "maximal readout value does not differ from minimal one";
            minLabel.setText(format(minValue));
            medLabel.setText(format((maxValue+minValue)/2));
            maxLabel.setText(format(maxValue));
        }
    }

    public static String format(double value) {
        if (value == 0) {
            return basicFormat.format(value);
        } else if (Math.abs(value) < 1E-4 || Math.abs(value) > 1E6)
            return scienceFormat.format(value);
        else
            return basicFormat.format(value);
    }


    public static void main(String[] args) {
        HeatMapColorToolBar toolBar = new HeatMapColorToolBar();
        JFrame frame = new JFrame();
        frame.setSize(500, 100);
        frame.add(toolBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
