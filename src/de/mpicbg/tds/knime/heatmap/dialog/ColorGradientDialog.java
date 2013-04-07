package de.mpicbg.tds.knime.heatmap.dialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashMap;

import com.bric.swing.GradientSlider;
import com.bric.swing.MultiThumbSlider;
import de.mpicbg.tds.knime.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.heatmap.color.LinearGradientTools;
import de.mpicbg.tds.knime.heatmap.color.RescaleStrategy;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Dialog to edit the color map
 *
 * @author Felix Meyenhofer
 *         11/22/12
 */

public class ColorGradientDialog extends JDialog {

    /** Data model */
    private HeatMapModel heatMapModel;

    /** Gradient slider component */
    private GradientSlider slider;
    /** Container holding the PopulationPanels */
    private JPanel populationPanel;
    /** Label for the minimum value of the colormap */
    private JLabel minLabel = new JLabel("");
    /** Label for the mean value of the colormap */
    private JLabel medLabel = new JLabel("");
    /** Label for the maximum value of the population panel */
    private JLabel maxLabel = new JLabel("");
    /** Text field to indicate the position on the color gradient */
    private JTextArea thumbPosition = new JTextArea("??");
    /** Spinner to input the deviation factor */
    private JSpinner deviationFactorSpinner;
    /** Combobox to input the the descriptor types */
    private JComboBox populationDescriptorCombobox;

    /** current colormap */
    private LinearGradientPaint currentGradient = LinearGradientTools.getStandardGradient("GB");
    /** minimum value of the color scale */
    protected float minScaleValue = 0;
    /** maximum value of the color scale */
    protected float maxScaleValue = 100;
    /** buffer for the population descriptors. They are only calculated once during configuration*/
    private HashMap<String,Double[]> populationDescriptors = new HashMap<String, Double[]>();


    /**
     * Constructor for initialization
     */
    public ColorGradientDialog() {
        setContentPane(initialize());
        setTitle("Color Gradient Editor");
        updateDialogDimensions();
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setModal(true);
    }

    /**
     * Constructor for a given data set
     *
     * @param model holding the data
     */
    public ColorGradientDialog(HeatMapModel model) {
        this();
        configure(model);
    }


    /**
     * Initialize the UI components
     *
     * @return content panel
     */
    private JPanel initialize() {
        // Create the cancel and ok buttons in a separate panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        thumbPosition.setEnabled(false);
        thumbPosition.setBackground(this.getBackground());
        buttonPanel.add(thumbPosition);
        buttonPanel.add(Box.createHorizontalGlue());
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        buttonPanel.add(buttonCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton buttonOK = new JButton("OK");
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonPanel.add(buttonOK);

        // Create the Gradient slider
        slider = new GradientSlider(MultiThumbSlider.HORIZONTAL);
        slider.setValues(currentGradient.getFractions(), currentGradient.getColors());
        slider.setPaintTicks(true);
        slider.putClientProperty("MultiThumbSlider.indicateComponent", "false");
        slider.putClientProperty("GradientSlider.useBevel", "true");
        slider.setToolTipText("<html>click on the thumbs to select them<br/>drag the thumbs to slide or remove them<br/>" +
                "double click on a thumb to choose it's color<html>");

        // Create the label panel
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.add(minLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(medLabel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(maxLabel);

        // Panel for the population descriptors
        populationPanel = new JPanel(new GridBagLayout());

        // Option panel
        populationDescriptorCombobox = new JComboBox();
        populationDescriptorCombobox.setModel(new DefaultComboBoxModel(new String[]{"mean, SD", "median, MAD"}));
        populationDescriptorCombobox.setSelectedIndex(0);
        Dimension dim = new Dimension(135,25);
        populationDescriptorCombobox.setMinimumSize(dim);
        populationDescriptorCombobox.setPreferredSize(dim);
        populationDescriptorCombobox.setMaximumSize(dim);
        populationDescriptorCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                updatePopulationPanels();
            }
        });
        deviationFactorSpinner = new JSpinner();
        deviationFactorSpinner.setName("rows");
        deviationFactorSpinner.setModel(new SpinnerNumberModel(3d, 0d, 10d, 0.25d));
        dim = new Dimension(60,20);
        deviationFactorSpinner.setMinimumSize(dim);
        deviationFactorSpinner.setPreferredSize(dim);
        deviationFactorSpinner.setMaximumSize(dim);
        deviationFactorSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                updatePopulationPanels();
            }
        });

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.LINE_AXIS));
        optionPanel.setBorder(new EmptyBorder(10,5,-1,-1));
        optionPanel.add(populationDescriptorCombobox);
        optionPanel.add(deviationFactorSpinner);
        optionPanel.add(Box.createHorizontalGlue());

        // Create the content paine and layout the buttons and the slider.
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0,10,0,10);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 0.2;
        contentPane.add(optionPanel, constraints);

        constraints.insets = new Insets(5,15,0,15);
        constraints.weighty = -1;
        constraints.gridy = 1;
        contentPane.add(populationPanel, constraints);

        constraints.weighty = 0.8;
        constraints.gridy = 2;
        constraints.insets = new Insets(5,10,0,10);
        contentPane.add(slider, constraints);

        constraints.insets = new Insets(0,10,20,10);
        constraints.gridy = 3;
        constraints.weighty = -1;
        contentPane.add(labelPanel, constraints);

        constraints.insets = new Insets(0,10,0,10);
        constraints.gridy = 4;
        constraints.weighty = 0.2;
        contentPane.add(buttonPanel, constraints);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Add mouse movement listener to update the cursor position on the gradient slider.
        slider.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                //TODO this property should be accessed from GradientSlider class.
                int thumbSize = 8;

                if (mouseEvent.getX() < thumbSize || (slider.getWidth()-thumbSize) < mouseEvent.getX())
                    return;

                float correctedPosition = (float) mouseEvent.getX()-thumbSize;
                float correctedWidth = (float) slider.getWidth()-thumbSize*2;
                float scaleValue = correctedPosition / correctedWidth * (maxScaleValue - minScaleValue) + minScaleValue;
                thumbPosition.setText(HeatMapColorToolBar.format(scaleValue));
            }
        });

        return contentPane;
    }

    /**
     * Configure the content panels components
     *
     * @param model holding the data
     */
    protected void configure(HeatMapModel model) {
        heatMapModel = model;
        currentGradient = model.getColorGradient().getGradient();

        // Update the sliders gradient.
        slider.setValues(currentGradient.getFractions(), currentGradient.getColors());

        // Set the gradient labels
        RescaleStrategy displayNormStrategy = heatMapModel.getReadoutRescaleStrategy();
        if ( heatMapModel.getSelectedReadOut() == null ) {
            System.err.println("No Readout is selected, can't calculate the minimum and maximum value of the color bar.");
        } else {
            Double minValue = displayNormStrategy.getMinValue(heatMapModel.getSelectedReadOut());
            Double maxValue = displayNormStrategy.getMaxValue(heatMapModel.getSelectedReadOut());
            assert minValue < maxValue : "maximal readout value does not differ from minimal one";
            minScaleValue = minValue.floatValue();
            maxScaleValue = maxValue.floatValue();
            minLabel.setText(HeatMapColorToolBar.format(minValue));
            medLabel.setText(HeatMapColorToolBar.format((maxValue + minValue) / 2));
            maxLabel.setText(HeatMapColorToolBar.format(maxValue));

            // Update the population panels
            String[] populations = heatMapModel.getReferencePopulationNames();
            if ((populations == null) || (populations.length == 0)) {
                deviationFactorSpinner.setEnabled(false);
                populationDescriptorCombobox.setEnabled(false);
            } else {
                populationDescriptors = getDescriptors();
                populationPanel.removeAll();
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1;
                constraints.weighty = -1;
                constraints.gridx = 0;
                int index = 0;
                for (String population : populationDescriptors.keySet()) {
                    PopulationPanel populationIndicator = new PopulationPanel(this, population);
                    constraints.gridy = index++;
                    populationPanel.add(populationIndicator, constraints);
                }
                updatePopulationPanels();
            }
            updateDialogDimensions();
        }
    }


    /**
     * Method to update the descriptors (depending on the descriptor type and the deviation factor)
     * of all the population panels.
     */
    private void updatePopulationPanels() {
        Component[] panels = populationPanel.getComponents();
        int index = 0;
        for (String population : populationDescriptors.keySet()) {
            Double[] values = populationDescriptors.get(population);
            PopulationPanel populationIndicator = (PopulationPanel)panels[index++];
            if (populationDescriptorCombobox.getSelectedItem().equals("mean, SD")) {
                populationIndicator.configure(population, values[0].floatValue(), values[1].floatValue(),
                        minScaleValue, maxScaleValue, ((Double) deviationFactorSpinner.getValue()).floatValue());
            } else {
                populationIndicator.configure(population, values[2].floatValue(), values[3].floatValue(),
                        minScaleValue, maxScaleValue, ((Double) deviationFactorSpinner.getValue()).floatValue());
            }
            populationIndicator.repaint();
        }
    }

    /**
     * Set the dialog size in function of the number of PopulationPanels
     */
    private void updateDialogDimensions() {
        int panelNumber = populationDescriptors == null ? 1 : populationDescriptors.keySet().size();
        setSize(new Dimension(600, 220 + 30 * panelNumber));
    }

    /**
     * Get the population descriptors of a distribution
     *
     * @return the population names with the descriptors {mean, std}
     */
    private HashMap<String, Double[]> getDescriptors() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        DescriptiveStatistics adStats = new DescriptiveStatistics();
        String readout = heatMapModel.getSelectedReadOut();
        String factor = heatMapModel.getReferencePopulationAttribute();
        String[] groups = heatMapModel.getReferencePopulationNames();
        HashMap<String, Double[]> groupDescriptors = new HashMap<String, Double[]>();
        for (String group : groups) {

            // Collect all the values of the group
            for (Plate plate : heatMapModel.getScreen()) {
                if (heatMapModel.isPlateFiltered(plate)) {
                    for (Well well : plate.getWells()) {
                        if (well.getAnnotation(factor).equals(group))
                            stats.addValue(well.getReadout(readout));
                    }
                }
            }

            // Calculate the median and the median absolute deviation
            double median = stats.getPercentile(50);
            for (double value : stats.getValues()) {
                adStats.addValue(StrictMath.abs(value - median));
            }

            // Aggregate (buffer) all the descriptors in a single variable
            groupDescriptors.put(group, new Double[]{stats.getMean(), stats.getStandardDeviation(),
                                                     median, adStats.getPercentile(50)});

            // Clear the group buffer.
            stats.clear();
            adStats.clear();
        }

        return groupDescriptors;
    }

    /**
     * Getter for the colormap
     *
     * @return colormap
     */
    public LinearGradientPaint getGradientPainter() {
        return currentGradient;
    }

    /**
     * Action executed on pressing the ok button
     */
    private void onOK() {
        Color[] colors = slider.getColors();
        float[] positions = slider.getThumbPositions();
        Point2D startPoint = new Point2D.Double(0,0);
        Point2D endPoint = new Point2D.Double(255,0);
        currentGradient = new LinearGradientPaint(startPoint, endPoint, positions, colors);
        dispose();
    }

    /**
     * Action executed after pressing the cancel button
     */
    private void onCancel() {
        dispose();
    }

    /**
     * Add thumbnails to the {@link GradientSlider}
     *
     * @param scaleValue position of the thumbnail
     */
    public void addThumbnail(float scaleValue) {
        float pos = (scaleValue - minScaleValue) / (maxScaleValue - minScaleValue);
        if (pos < 0 || 1 < pos){
            System.err.println("The thumb at position " + pos + " lies outside the color scale and will not be added.");
            return;
        }

        for (float exi : slider.getThumbPositions() ) {
            if (pos == exi) {
                System.err.println("The Thumbs at the position " + pos + "already exists.");
            }
        }
        slider.addThumb(pos);
    }



    /**
     * Panels to indicate the bounds of a given reference population
     */
    class PopulationPanel extends JPanel {

        /** mean of the population */
        public float mean = 50;
        /** standard deviation of the population */
        public float sd = 10;
        /** lower bound of the color scale */
        public float minScale = 0;
        /** upper bound of the color scale */
        public float maxScale = 100;
        /** panel name (population name) */
        public String panelName = "test";
        /** factor to calculate the population bounds (3 std) */
        private float factor = 3;
        /** parent */
        private ColorGradientDialog parent;


        /**
         * Constructor for initialization
         */
        public PopulationPanel() {
            super();

            // Add the mouse listener for the popup menu
            addMouseListener(new MouseAdapter() {
                JPopupMenu popup = createPopupMenu();

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popup.show(e.getComponent(),
                                e.getX(), e.getY());
                    }
                }
            });

            // Set the tooltip
            setToolTipText("<html>The colored rectangle indicates the parameters average&plusmn;" + factor + "&middot;deviation.<br/>" +
                    "Use right click to pass descriptor values as thumbs to the color gradient");
        }

        /**
         * Constructor for initialization and configuration of the UI components
         *
         * @param dialog parent
         * @param name population name
         */
        public PopulationPanel(ColorGradientDialog dialog, String name) {
            this();
            parent = dialog;
            panelName = name;
            Border border = BorderFactory.createEtchedBorder();
            setBorder(BorderFactory.createTitledBorder(border, panelName));
        }


        /**
         * Configure the panel
         *
         * @param name of the population
         * @param m mean of the population
         * @param s standard deviation of the population
         * @param mis lower bound of the color scale
         * @param mas upper bound of the color scale
         */
        public void configure(String name, float m, float s, float mis, float mas, float fac) {
            panelName = name;
            setName(panelName);
            mean = m;
            sd = s;
            minScale = mis;
            maxScale = mas;
            factor = fac;
            repaint();
        }

        /** {@inheritDoc} */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2d = (Graphics2D) graphics;
            Font font = new Font("Arial", Font.PLAIN, 11);
            g2d.setFont(font);
            FontMetrics metrics = g2d.getFontMetrics(font);
            Insets insets = getBorder().getBorderInsets(this);

            RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHints(rh);

            // Calculate the positions in pixels.
            float scaleFactor = this.getWidth()/StrictMath.abs(maxScale-minScale);
            float lowerBound = mean-factor*sd;
            float upperBound = mean+factor*sd;

            int left = StrictMath.round((lowerBound-minScale)*scaleFactor);
            int top = insets.top/2;
            int width = StrictMath.round(2*factor*sd*scaleFactor);
            int right = left+width;
            int middle = (left+right) / 2;
            int height = this.getHeight()-(insets.top/2+insets.bottom);
            int halfHeight = StrictMath.round(height/2+top+5);

            // Make sure the we don't try to paint over the panel borders.
            if (left < 0) {
                width += left;
                left = 0;
            }
            width = width > this.getWidth() ? this.getWidth() : width;

            // Draw the rectangles
            g2d.setColor(new Color(165, 205, 255));
            g2d.drawRect(left, top, width, height);
            g2d.fillRect(left, top, width, height);

            // Draw the labels
            g2d.setColor(new Color(0, 0, 0));
            String centerLabel = HeatMapColorToolBar.format(mean);
            g2d.drawString(centerLabel, middle-metrics.stringWidth(centerLabel)/2, halfHeight);

            String leftLabel = HeatMapColorToolBar.format(lowerBound);
            int minDist = metrics.stringWidth(leftLabel)+metrics.stringWidth(centerLabel)/2 + 5;
            leftLabel = (middle-left) < minDist ? "" : leftLabel;
            g2d.drawString(leftLabel, left+metrics.stringWidth(" "), halfHeight);

            String rightLabel = HeatMapColorToolBar.format(upperBound);
            minDist = metrics.stringWidth(rightLabel)+metrics.stringWidth(centerLabel)/2 + 5;
            rightLabel = (right-middle) < minDist ? "" : HeatMapColorToolBar.format(upperBound);
            g2d.drawString(rightLabel, right-metrics.stringWidth(rightLabel+" "), halfHeight);

            // Add the bounds to the tooltip
            if (populationDescriptorCombobox.getSelectedItem().equals("mean, SD")) {
                setToolTipText("<html>The colored rectangle indicates the parameters mean&plusmn;" + factor + "&middot;SD.<br/>" +
                        "[" + lowerBound + "..." + mean + "..." + upperBound + "]<br/>" +
                        "Use right click to pass descriptor values as thumbs to the color gradient");
            } else {
                setToolTipText("<html>The colored rectangle indicates the parameters median&plusmn;" + factor + "&middot;MAD.<br/>" +
                        "[" + lowerBound + "..." + mean + "..." + upperBound + "]<br/>" +
                        "Use right click to pass descriptor values as thumbs to the color gradient");
            }
        }

        /**
         * Create a popup menu allowing to add thumbnails for the population descriptors
         *
         *  @return context menu
         */
        private JPopupMenu createPopupMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem option1 = new JMenuItem("Add Thumb for mean");
            option1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    parent.addThumbnail(mean);

                }
            });
            menu.add(option1);
            JMenuItem option2 = new JMenuItem("Add Thumb for bounds");
            option2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    parent.addThumbnail(mean-factor*sd);
                    parent.addThumbnail(mean+factor*sd);
                }
            });
            menu.add(option2);
            JMenuItem option3 = new JMenuItem("Add Thumbs for all");
            option3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    parent.addThumbnail(mean);
                    parent.addThumbnail(mean-factor*sd);
                    parent.addThumbnail(mean+factor*sd);
                }
            });
            menu.add(option3);
            return menu;
        }
    }

}
