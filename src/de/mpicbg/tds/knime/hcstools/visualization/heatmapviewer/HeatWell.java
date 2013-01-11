package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ScreenColorScheme;

/**
 * @author Holger Brandl
 *
 * A JPanel which renders a detailed view on a single well within a heat-map.
 * replaces HeatWellPanel
 * TODO: clean out commented methods.
 * TODO: reintegrate the well detail panel.
 */

public class HeatWell extends JPanel {

    private Well well;
    private HeatMapModel heatMapModel;

    public static int STROKE_WIDTH = 3;
    public static BasicStroke overlayStroke = new BasicStroke(STROKE_WIDTH);
//    private boolean isSelected;
    private boolean showGrid = false;
    protected boolean isPreselected = false;


    public HeatWell(final Well well, HeatMapModel heatMapModel) {
        super();

        this.well = well;
        this.heatMapModel = heatMapModel;

//        // Mouse listener for the well details view. A well details dialog is opened with a double click
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent mouseEvent) {
//                // Open well detail panel on right click.
//                if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
//                    openNewWellViewer();
//                    mouseEvent.consume();
//                }
//            }
//        });

        // Mouse listener for the tooltip, which is the well details.
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);
                setToolTipText(" ");
            }
        });
    }


    protected void openNewWellViewer(Point position) {
//        JDialog jDialog = new JDialog(getParentDialog(this), false);
////        jDialog.add(new WellDetailPanel(this.well));
//
//        Random random = new Random();
//        jDialog.setBounds(random.nextInt(100) + 200, random.nextInt(100) + 200, 300, 500);
//        jDialog.setVisible(true);

        // A small window, to show that something is happening, since it might take a moment to retrieve the images.
        JWindow window = new JWindow();
        window.setLayout(new BorderLayout());
        JLabel label = new JLabel();
        label.setText("Opening Well Viewer...");
        label.setHorizontalAlignment(JLabel.CENTER);
        window.getContentPane().add(label, BorderLayout.CENTER);
        window.setLocation(position.x, position.y);
        window.setSize(new Dimension(180,20));
        window.setVisible(true);    // TODO wait until this is rendered

        // Create the WellViewer
        WellViewer wellViewer = new WellViewer(this, well);
        final JDialog viewer = wellViewer.createDialog();
//        viewer.setLocation(random.nextInt(100) + 200, random.nextInt(100) + 200);
//        viewer.setBounds(random.nextInt(100) + 200, random.nextInt(100) + 200, 300, 500);
//        viewer.pack();

        // Make sure the dialog exits if the PlateViewer is closed.
        viewer.setVisible(true);
        ((JFrame) this.getTopLevelAncestor()).addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                viewer.setVisible(false);
            }
        });

        window.setVisible(false);
    }

    public Well getWell() {
        return well;
    }

//    public void setSelected(boolean isSelected) {
//        this.isSelected = isSelected;
//        repaint();
//    }

//    public void setShowGrid(boolean showGrid) {
//        this.showGrid = showGrid;
//    }

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


    @Override
    public JToolTip createToolTip() {
        JToolTip jToolTip = new JToolTip();
        jToolTip.setLayout(new BorderLayout());
//        jToolTip.setPreferredSize(new Dimension(350, 500));
        WellViewer wellDetailsPanel = new WellViewer(well);

//        WellDetailPanel wellDetailsPanel = new WellDetailPanel(well);
        jToolTip.add(wellDetailsPanel, BorderLayout.CENTER);
        jToolTip.setPreferredSize(wellDetailsPanel.getPreferredSize());

        return jToolTip;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (isPreselected) {
            setBackground(ScreenColorScheme.getInstance().preselectionColor);
            return;
//            setOpaque(true);
        }

        // Border handling.
        if (showGrid) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(new Rectangle(1, 1, getWidth() + 1, getHeight() + 1));
        }

        // Overlay painting
        Color layoutColor = heatMapModel.getOverlayColor(well);
        if (layoutColor != null) {
            g2d.setColor(layoutColor);
            g2d.setStroke(overlayStroke);
            g2d.draw(new Rectangle(1, 1, getWidth() - STROKE_WIDTH, getHeight() - STROKE_WIDTH));

            if (getWidth() > 30) {
                g2d.drawString(heatMapModel.getOverlayValue(well), 5, 20);
            }
        }

        // Selection dot.
        if ( heatMapModel.doMarkSelection() ) {
            Color dotColor = null;
            if ( heatMapModel.isWellHiLited(well) && heatMapModel.isWellSelected(well)) {
                dotColor = ScreenColorScheme.getInstance().selectionAndHiLiteColor;
            } else if ( heatMapModel.isWellHiLited(well) ) {
                dotColor = ScreenColorScheme.getInstance().HilLiteColor;
            } else if ( heatMapModel.isWellSelected(well)) {
                dotColor = ScreenColorScheme.getInstance().selectionColor;
            }
            if (dotColor != null) {
                g2d.setColor(dotColor);
                g2d.fillRect(getWidth() / 2 - 3, getHeight() / 2 - 3, 6, 6);
            }
        }

        //the actual value color.
        setBackground(heatMapModel.getReadoutColor(well));
    }

}
