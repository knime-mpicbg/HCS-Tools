package de.mpicbg.tds.knime.hcstools.visualization.heatmap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.PlateUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Create a Legend window for the heat map overlay colors.
 *
 * @author Holger Brandl
 */

public class LegendViewer extends JDialog implements HeatMapModelChangeListener {

    /** Data model */
	private HeatMapModel heatMapModel;
    /** {@link JPanel}  containing the legend entries*/
	public LegendPanel legendPanel;


    /**
     * Constructor of the LegendViewer
     *
     * @param owner of the legend
     */
    public LegendViewer(Window owner) {
        super(owner);
        initialize();
    }


    /**
     * initialize the UI components
     */
	private void initialize() {
        setLayout(new BorderLayout());
        legendPanel = new LegendPanel(this);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(legendPanel);
        add(scrollPane, BorderLayout.CENTER);
		setLocationRelativeTo(getOwner());
        legendPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
	}

    /**
     * configure the UI components (set the data)
     *
     * @param heatMapModel containing the data for display
     */
	public void configure(HeatMapModel heatMapModel) {
		this.heatMapModel = heatMapModel;
		heatMapModel.addChangeListener(this);
		legendPanel.configure(heatMapModel);
		modelChanged();
	}

    /** {@inheritDoc} */
    @Override
	public void modelChanged() {
		setTitle(StringUtils.isBlank(heatMapModel.getOverlay()) ? "No Overlay" : heatMapModel.getOverlay());
		repaint();
	}


    /**
     * Quick testing
     *
     * @param args whatever
     */
    public static void main(String[] args) {
        JDialog window = new JDialog();
        window.add(new JLabel("just some text"));
        window.setSize(200, 200);
        window.setVisible(true);
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        LegendViewer frame = new LegendViewer(window);
        frame.setVisible(true);
    }



    /**
     * Panel containing the legend entry (renderer)
     */
    public class LegendPanel extends JPanel implements HeatMapModelChangeListener {

        /** data model containing the data for display */
        private HeatMapModel heatMapModel;
        /** TODO */
        private int nullCounter = 0;
        /** Parent dialog */
        private JDialog parent;
        /** Font for the legend entries */
        private Font font = new Font("Arial", Font.PLAIN, 12);


        /**
         * Constructor of the legend panel
         *
         * @param component enclosing this panel
         */
        protected LegendPanel(JDialog component) {
            super();
            parent = component;
        }


        /**
         * configure the UI components
         *
         * @param heatMapModel containing the legend entries
         */
        public void configure(HeatMapModel heatMapModel) {
            this.heatMapModel = heatMapModel;
            heatMapModel.addChangeListener(this);
            modelChanged();
        }

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
        }

        /** {@inheritDoc} */
        @Override
        public void modelChanged() {
            removeAll();

            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.weightx = 1;
            constraints.weighty = -1;
            constraints.fill = GridBagConstraints.BOTH;
            int index = 0;

            if ( heatMapModel.getOverlay().equals(HeatMapModel.KNIME_OVERLAY_NAME) ){        // TODO: Retrieve proper group names.
                for (Color color : heatMapModel.getKnimeColors()) {
                    constraints.gridy = index++;
                    add(createLegendEntry("Nominal " + index, color), constraints);
                }

            } else {
                Map<String, Color> cache = getColorCache();
                if (cache == null)
                    return;

                Collection<String> allOverlayValues = PlateUtils.collectAnnotationLevels(heatMapModel.getScreen(), heatMapModel.getOverlay());
                for (String overlayValue : cache.keySet()) {
                    if (!allOverlayValues.contains(overlayValue) || StringUtils.isBlank(overlayValue))
                        continue;
                    constraints.gridy = index++;
                    Color color = cache.get(overlayValue);
                    add(createLegendEntry(overlayValue, color), constraints);
                }
            }

            // Make sure the window is not too small nor too big.
            parent.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            int width = parent.getWidth();
            Double screenWidth = screenSize.getWidth();
            int maxWidth = screenWidth.intValue() - 200;
            width = ((width > maxWidth) ? maxWidth : width);
            font = parent.getFont();
            FontMetrics metrics = parent.getFontMetrics(font);
            int minWidth = (parent.getTitle() == null) ? 250 : metrics.stringWidth(parent.getTitle()) + 100;
            width = ((width < minWidth) ? minWidth : width);

            int height = parent.getHeight();
            Double screenHeight = screenSize.getHeight();
            int maxHeight = screenHeight.intValue()- 100;
            height = ((height > maxHeight) ? maxHeight : height);
            parent.setSize(new Dimension(width, height));

            revalidate();
            repaint();
        }


        /**
         * fetch the color cache from the data model
         *
         * @return map with a bunch of colors
         */
        private Map<String, Color> getColorCache() {
            Map<String, Color> cache = heatMapModel.getColorScheme().getNameColorCache(heatMapModel.getOverlay());

            if (nullCounter < 1 && (cache == null || cache.isEmpty())) {
                nullCounter++;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        LegendPanel.this.modelChanged();
                    }
                }.start();

                return null;
            }

            nullCounter = 0;

            if (cache == null || cache.isEmpty()) {
                return null;
            }
            return cache;
        }

        /**
         * helper method to create a legend component
         *
         * @param title of the legend entry
         * @param color of the legend entry
         * @return legend component
         */
        private JLabel createLegendEntry(String title, Color color) {
            JLabel legendEntry = new JLabel();
            legendEntry.setFont(font);
            legendEntry.setText(title);
            legendEntry.setOpaque(true);

            if (color != null)
                legendEntry.setBackground(color);

            legendEntry.setBorder(BorderFactory.createEmptyBorder(2,10,2,2));

            return legendEntry;
        }
    }

}
