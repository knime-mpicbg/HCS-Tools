package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.RescaleStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ScreenColorScheme;

/**
 * User: Felix Meyenhofer
 * Date: 12/8/12
 * Time: 1:35
 *
 * Colorbar to integrate in a heatmap frame.
 * TODO: When changing the orientation of the toolbar, the layout should be changed and the color gradient rotated.
 */

public class HeatMapColorToolBar extends JToolBar implements HeatMapModelChangeListener{

    private HeatMapModel2 heatMapModel;
    private LinearGradientTools.ColorGradientPanel gradientPanel;
    private JLabel minLabel = new JLabel("min");
    private JLabel medLabel = new JLabel("middle");
    private JLabel maxLabel = new JLabel("max");
    private final ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();

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

    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repaint();
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (heatMapModel == null || heatMapModel.getSelectedReadOut() == null) {
            System.err.println("Incomplete HeatMapModel configurations, can't retrieve minimum and maximum values.");
            return;
        }

        // Set the color gradient
        RescaleStrategy displayNormStrategy = heatMapModel.getReadoutRescaleStrategy();
        gradientPanel.configure(heatMapModel.colorGradient);

        // Set the scale labels.
        Double minValue = displayNormStrategy.getMinValue(heatMapModel.getSelectedReadOut());
        Double maxValue = displayNormStrategy.getMaxValue(heatMapModel.getSelectedReadOut());
        assert minValue < maxValue : "maximal readout value does not differ from minimal one";
        minLabel.setText(format(minValue));
        medLabel.setText(format((maxValue+minValue)/2));
        maxLabel.setText(format(maxValue));
    }

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
        errorPanel.setBackground(colorScheme.errorReadOut);

        // Create a label for the panel indicating the missing value color.
        JLabel errorLabel = new JLabel(" Err ");
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add the panel indicating the missing value color.
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(colorScheme.emptyReadOut);

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

    protected void configure(HeatMapModel2 model) {
        heatMapModel = model;
        heatMapModel.addChangeListener(this);
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
        HeatMapColorToolBar toolBar = new HeatMapColorToolBar(new HeatMapModel2());
        JFrame frame = new JFrame();
        frame.add(toolBar);
        frame.setSize(500, 60);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
