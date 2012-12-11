package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * User: Felix Meyenhofer
 * Date: 12/7/12
 * Time: 21:09
 *
 * Class to create a panel with a color gradient to produce a color bar
 */

public class ColorGradientPanel extends JPanel{

    // Defaults
    private static final Dimension dimension = new Dimension(400, 30);
    private LinearGradientPaint gradientPainter = LinearGradientTools.getStandardGradient("GBR");


    // Constructors
    public ColorGradientPanel() {
        initialize();
    }

    public ColorGradientPanel(LinearGradientPaint paint) {
        this();
        configure(paint);
    }


    // Utilities
    private void initialize() {
        setMinimumSize(dimension);
    }

    public void configure(LinearGradientPaint painter) {
        setGradientPainter(painter);
    }

    public void setGradientPainter(LinearGradientPaint painter) {
        gradientPainter = painter;
    }

    public LinearGradientPaint getGradientPainter() {
        return gradientPainter;
    }

    // Overwrite the JPanel renderer
    @Override
    public void paintComponent(Graphics graphics) {
        // Create the 2D copy
        Graphics2D graphics2D = (Graphics2D) graphics.create();

        // Create a new gradient painter with the current panel width.
        Point2D sta = new Point2D.Double(0, 0);
        Point2D sto = new Point2D.Double(getWidth(), 0);
        float[] pos = gradientPainter.getFractions();
        Color[] col = gradientPainter.getColors();
        LinearGradientPaint gradient = new LinearGradientPaint(sta, sto, pos, col);

        // Render the graphics
        graphics2D.setPaint(gradient);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());

        // Dispose of copy
        graphics2D.dispose();
    }


    public static void main(String[] args) {
        ColorGradientPanel bar = new ColorGradientPanel();
        JFrame frame = new JFrame("ColorGradientToolBar Test");
        frame.setSize(dimension);
        frame.add(bar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
