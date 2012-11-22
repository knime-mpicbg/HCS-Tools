package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
//import de.mpicbg.tds.core.view.HeatMapModel;
//import de.mpicbg.tds.core.view.HeatMapModelChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class OverlayLegendPanel extends JPanel implements HeatMapModelChangeListener {

    private HeatMapModel heatMapModel;


    public void setModel(HeatMapModel heatMapModel) {
        this.heatMapModel = heatMapModel;
        heatMapModel.addChangeListener(this);
        modelChanged();
    }


    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
    }


    private int nullCounter = 0;


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

                    OverlayLegendPanel.this.modelChanged();
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

        for (String overlayValue : cache.keySet()) {
            if (!allOverlayValues.contains(overlayValue))
                continue;

            JLabel legendEntry = new JLabel();
            legendEntry.setText("  " + overlayValue);
            legendEntry.setOpaque(true);
            Color color = cache.get(overlayValue);
            if (color != null) {
                legendEntry.setBackground(color);
            }

            add(legendEntry);
        }

//		setBackground(Color.BLACK);
        revalidate();
//		invalidate();
        repaint();
    }
}
