package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;

import de.mpicbg.tds.core.TdsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * @author Holger Brandl
 *
 * Create a Legend window.
 */

public class OverlayLegend extends JDialog implements HeatMapModelChangeListener {

	private HeatMapModel2 heatMapModel;
	public LegendPanel legendPanel;


	public OverlayLegend(Frame owner) {
		super(owner);
		initComponents();
//		setSize(new Dimension(80, 150));
	}

	public OverlayLegend(Dialog owner) {
		super(owner);
		initComponents();
	}


	private void initComponents() {
        setLayout(new BorderLayout());
        legendPanel = new LegendPanel();
        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setViewportView(legendPanel);
        add(scrollPane1);
		setLocationRelativeTo(getOwner());
        legendPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_MASK), JComponent.WHEN_FOCUSED);
	}

	public void setModel(HeatMapModel2 heatMapModel) {
		this.heatMapModel = heatMapModel;
		heatMapModel.addChangeListener(this);
		legendPanel.setModel(heatMapModel);
		modelChanged();
	}

	public void modelChanged() {
		setTitle(StringUtils.isBlank(heatMapModel.getOverlay()) ? "No Overlay" : heatMapModel.getOverlay());
		repaint();
	}

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.add(new JLabel("just some text"));
        window.setSize(200, 200);
        window.setVisible(true);
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        OverlayLegend frame = new OverlayLegend(window);
        frame.setVisible(true);
    }



    public class LegendPanel extends JPanel implements HeatMapModelChangeListener {

        private HeatMapModel2 heatMapModel;
        private int nullCounter = 0;


        public void setModel(HeatMapModel2 heatMapModel) {
            this.heatMapModel = heatMapModel;
            heatMapModel.addChangeListener(this);
            modelChanged();
        }


        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
        }

        public void modelChanged() {
            removeAll();

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

                return;
            }

            nullCounter = 0;

            if (cache == null || cache.isEmpty()) {
                return;
            }

    //		setPreferredSize(new Dimension(100, cache.size() * 25));
            setLayout(new GridLayout(0, 1));

            Collection<String> allOverlayValues = TdsUtils.collectAnnotationLevels(heatMapModel.getScreen(), heatMapModel.getOverlay());

            Font font = new Font("Arial", Font.PLAIN, 11);
            DescriptiveStatistics stats = new DescriptiveStatistics();
            FontMetrics metrics = null;
            for (String overlayValue : cache.keySet()) {
                if (!allOverlayValues.contains(overlayValue))
                    continue;

                JLabel legendEntry = new JLabel();
                legendEntry.setFont(font);
                metrics = legendEntry.getFontMetrics(font);
                String str = "  " + overlayValue;
                legendEntry.setText(str);
                stats.addValue(metrics.stringWidth(str));
                legendEntry.setOpaque(true);
                Color color = cache.get(overlayValue);
                if (color != null) {
                    legendEntry.setBackground(color);
                }

                add(legendEntry);
            }


            Double panelWidth;
            Double panelHeight;
            if (!(metrics == null)) {
                stats.addValue(metrics.stringWidth(heatMapModel.getOverlay() + 80)); //Add some space for the window controls.
                panelWidth = stats.getMax();
                panelHeight = (double) metrics.getHeight() + 5;
            } else {
                panelWidth = 200d;
                panelHeight = 20d;
            }

            // Set the appropriate window dimensions
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Double width = panelWidth;
            Double maxWidth = screenSize.getWidth() - 200;
            width = ((width > maxWidth) ? maxWidth : width);
            Double height = (double) allOverlayValues.size() * panelHeight;
            Double maxHeight = screenSize.getHeight() - 100;
            height = ((height > maxHeight) ? maxHeight : height);
            getTopLevelAncestor().setSize(new Dimension(width.intValue(), height.intValue()));

            revalidate();
            repaint();
        }
    }


}
