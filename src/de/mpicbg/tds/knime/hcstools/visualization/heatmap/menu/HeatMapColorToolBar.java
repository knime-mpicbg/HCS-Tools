package de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModelChangeListener;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.color.ColorScheme;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.color.RescaleStrategy;

/**
 * Colorbar to integrate in a heatmap frame.
 * TODO: When changing the orientation of the toolbar, the layout should be changed and the color gradient rotated.
 *
 * @author Felix Meyenhofer
 *         12/8/12
 */

public class HeatMapColorToolBar extends JToolBar implements HeatMapModelChangeListener {

    /** Data model */
    private HeatMapModel heatMapModel;
    /** Color map */
    private LinearGradientTools.ColorGradientPanel gradientPanel;
    /** Label for the lower bound of the color scale */
    private JLabel minLabel = new JLabel("min");
    /** Label for mean of the color scale */
    private JLabel medLabel = new JLabel("middle");
    /** Label for the upper bound of the color scale */
    private JLabel maxLabel = new JLabel("max");

    /** Formating of the scale labels */
    public static final DecimalFormat scienceFormat = new DecimalFormat("0.###E0");
    public static final DecimalFormat basicFormat = new DecimalFormat("######.###");


    /**
     * Constructor for the initialization of the toolbar
     */
    public HeatMapColorToolBar() {
        initialize();
    }

    /**
     * Constructor for the initialization and the configuration of the toolbar
     *
     * @param model delivering the data
     */
    public HeatMapColorToolBar(HeatMapModel model) {
        this();
        configure(model);
    }

    /** {@inheritDoc} */
    @Override
    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (heatMapModel == null || heatMapModel.getSelectedReadOut() == null) {
            System.err.println("Incomplete HeatMapModel configurations, can't retrieve minimum and maximum values.");
            return;
        }

        // Set the color gradient
        RescaleStrategy displayNormStrategy = heatMapModel.getReadoutRescaleStrategy();
        gradientPanel.configure(heatMapModel.getColorGradient().getGradient());

        // Set the scale labels.
        Double minValue = displayNormStrategy.getMinValue(heatMapModel.getSelectedReadOut());
        Double maxValue = displayNormStrategy.getMaxValue(heatMapModel.getSelectedReadOut());
        assert minValue < maxValue : "maximal readout value does not differ from minimal one";
        minLabel.setText(format(minValue));
        medLabel.setText(format((maxValue+minValue)/2));
        maxLabel.setText(format(maxValue));
    }

    /**
     * Initialization of the UI components
     */
    private void initialize() {
        // Create a panel with the labels for the colorbar.
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.add(minLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(medLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(maxLabel);

        // Add the color panel
        gradientPanel = new LinearGradientTools.ColorGradientPanel();

        // Add the panel indicating the missing value color.
        JPanel errorPanel = new JPanel();
        errorPanel.setBackground(ColorScheme.ERROR_READOUT);

        // Create a label for the panel indicating the missing value color.
        JLabel errorLabel = new JLabel(" Err ");
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add the panel indicating the missing value color.
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(ColorScheme.EMPTY_READOUT);

        // Create a label for the panel indicating the missing value color.
        JLabel emptyLabel = new JLabel(" Empty ");
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add the component to the main panel
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0.9;
        constraints.fill = GridBagConstraints.BOTH;
        add(gradientPanel, constraints);
        constraints.gridy = 1;
        constraints.weighty = 0.1;
        add(labelPanel, constraints);

        constraints.gridx = 1;
        constraints.weightx = -1;
        add(errorLabel, constraints);
        constraints.gridy = 0;
        constraints.weighty = 0.9;
        add(errorPanel, constraints);

        constraints.gridx = 2;
        add(emptyPanel, constraints);
        constraints.gridy = 1;
        constraints.weighty = 0.1;
        add(emptyLabel, constraints);
    }

    /**
     * Configuration of the UI components
     *
     * @param model delivering the data
     */
    public void configure(HeatMapModel model) {
        heatMapModel = model;
        heatMapModel.addChangeListener(this);
    }

    /**
     * Return a formated string of the scale value
     *
     * @param value for a label
     * @return string representing the value
     */
    public static String format(double value) {
        if (value == 0) {
            return basicFormat.format(value);
        } else if (Math.abs(value) < 1E-4 || Math.abs(value) > 1E6)
            return scienceFormat.format(value);
        else
            return basicFormat.format(value);
    }


    /**
     * Quick testing
     * @param args whatever
     */
    public static void main(String[] args) {
        HeatMapColorToolBar toolBar = new HeatMapColorToolBar(new HeatMapModel());
        JFrame frame = new JFrame();
        frame.add(toolBar);
        frame.setSize(500, 60);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
