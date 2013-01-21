package de.mpicbg.tds.knime.heatmap.renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import de.mpicbg.tds.knime.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.heatmap.WellViewer;
import de.mpicbg.tds.knime.heatmap.color.ColorScheme;
import de.mpicbg.tds.core.model.Well;

/**
 * A JPanel which renders a detailed view on a single well within a heat-map.
 * replaces HeatWellPanel
 *
 * @author Holger Brandl
 */

public class HeatWell extends JPanel {

    /** Data model with the stuff for display */
    private Well well;

    /** Data model of the entire UI */
    private HeatMapModel heatMapModel;

    /** Stroke width of the overlay */
    public static int STROKE_WIDTH = 3;
    public static BasicStroke overlayStroke = new BasicStroke(STROKE_WIDTH);

    /** flag for the plate grid */
    private boolean showGrid = false;
    /** flag for pre-selection */
    protected boolean isPreselected = false;


    /**
     * Constructor of the one colored well
     *
     * @param well data for display
     * @param heatMapModel data model of the entire UI
     */
    public HeatWell(final Well well, HeatMapModel heatMapModel) {
        super();

        this.well = well;
        this.heatMapModel = heatMapModel;

        // Mouse listener for the tooltip, which is the well details.
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);
                setToolTipText(" ");
            }
        });
    }


    /**
     * Open a new well details view
     *
     * @param position from where the command was fired
     * @see {@link WellViewer}
     */
    protected void openNewWellViewer(Point position) {
        // A small window, to show that something is happening, since it might take a moment to retrieve the images.
        // TODO wait until this is rendered
        JWindow window = new JWindow();
        window.setLayout(new BorderLayout());
        JLabel label = new JLabel();
        label.setText("Opening Well Viewer...");
        label.setHorizontalAlignment(JLabel.CENTER);
        window.getContentPane().add(label, BorderLayout.CENTER);
        window.setLocation(position.x, position.y);
        window.setSize(new Dimension(180,20));
        window.setVisible(true);

        // Create the WellViewer
        WellViewer wellViewer = new WellViewer(this, well);
        final JDialog viewer = wellViewer.createDialog();

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

    /**
     * Getter for the displayed data
     *
     * @return data in display
     */
    public Well getWell() {
        return well;
    }

    /** {@inheritDoc} */
    @Override
    public JToolTip createToolTip() {
        JToolTip jToolTip = new JToolTip();
        jToolTip.setLayout(new BorderLayout());
        WellViewer wellDetailsPanel = new WellViewer(well);
        jToolTip.add(wellDetailsPanel, BorderLayout.CENTER);
        jToolTip.setPreferredSize(wellDetailsPanel.getPreferredSize());

        return jToolTip;
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (isPreselected) {
            setBackground(ColorScheme.SELECTING);
            return;
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
                dotColor = ColorScheme.HILITED_AND_SELECTED;
            } else if ( heatMapModel.isWellHiLited(well) ) {
                dotColor = ColorScheme.HILITED;
            } else if ( heatMapModel.isWellSelected(well)) {
                dotColor = ColorScheme.SELECTED;
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
