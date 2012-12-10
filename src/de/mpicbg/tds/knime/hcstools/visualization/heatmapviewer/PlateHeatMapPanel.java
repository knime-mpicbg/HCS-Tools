package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Well;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

//import de.mpicbg.tds.core.view.HeatMapModel;
//import de.mpicbg.tds.core.view.WellDetailPanel;


/**
 * A JPanel which renders a detailed view on a single well within a heat-map.
 *
 * @author Holger Brandl
 * @see de.mpicbg.tds.core.view.PlateDetailsHeatMap
 */
public class PlateHeatMapPanel extends JPanel {


//    private final Border border = BorderFactory.createLineBorder(Color.blue, 2);

    private Well well;
    private HeatMapModel2 heatMapModel;

    public static int STROKE_WIDTH = 3;
    public static BasicStroke overlayStroke = new BasicStroke(STROKE_WIDTH);
    private boolean isSelected;

    private boolean showGrid = false;


    public PlateHeatMapPanel(final Well well, HeatMapModel2 heatMapModel) {
        super();

        this.well = well;
        this.heatMapModel = heatMapModel;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {


                    JDialog jDialog = new JDialog(getParentDialog(PlateHeatMapPanel.this), false);
                    jDialog.add(new WellDetailPanel(well));

                    Random random = new Random();
                    jDialog.setBounds(random.nextInt(100) + 200, random.nextInt(100) + 200, 300, 500);
                    jDialog.setVisible(true);

                    mouseEvent.consume();


                }
            }
        });
//
        addMouseMotionListener(new MouseMotionAdapter() {

            String toolTip;


            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);

                setToolTipText(" ");
            }
        });
    }


    public static Dialog getParentDialog(Container component) {
        while (component != null) {
            if (component instanceof JDialog) {
                return (Dialog) component;
            }

            component = component.getParent();
        }

        return null;
    }


    public static Container getParentContainer(Container component) {
        while (component != null) {
            if (component instanceof JDialog || component instanceof JFrame) {
                return component;
            }

            component = component.getParent();
        }

        return null;
    }


//    private static long requestID;
//    private static Random r  = new Random();


    @Override
    public JToolTip createToolTip() {
        JToolTip jToolTip = new JToolTip();
        jToolTip.setLayout(new BorderLayout());

//        // a hacky delay approach to let the app query for compounds just if the user actually hover over a well
//        int curRequestID =r.nextInt();
//        requestID = curRequestID;
//        Utils.sleep(100);
//        if(requestID != curRequestID)
//            return null;


        WellDetailPanel wellDetailsPanel = new WellDetailPanel(well);

        jToolTip.setPreferredSize(new Dimension(350, 500));


        jToolTip.add(wellDetailsPanel, BorderLayout.CENTER);

        return jToolTip;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (showGrid) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(new Rectangle(1, 1, getWidth() + 1, getHeight() + 1));
        }

        Color layoutColor = heatMapModel.getOverlayColor(well);

        if (layoutColor != null) {
            g2d.setColor(layoutColor);
            g2d.setStroke(overlayStroke);
            g2d.draw(new Rectangle(1, 1, getWidth() - STROKE_WIDTH, getHeight() - STROKE_WIDTH));

            if (getWidth() > 30) {
                g2d.drawString(heatMapModel.getOverlayValue(well), 5, 20);
            }
        }


        if (heatMapModel.isShowSelection() && heatMapModel.isSelected(well)) {
            g2d.setColor(heatMapModel.getColorScheme().getHighlightColor());
            g2d.fillRect(getWidth() / 2 - 3, getHeight() / 2 - 3, 6, 6);
        }


        setBackground(heatMapModel.getReadoutColor(well));
    }


    public Well getWell() {
        return well;
    }


    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        repaint();
    }


    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }
}

