package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

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

    private LinearGradientPaint gradientPaint;
    private static final Dimension dimension = new Dimension(400, 30);


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
        setGradientPaint(painter);
    }

    public void setGradientPaint(LinearGradientPaint painter) {
        gradientPaint = painter;
    }

    public LinearGradientPaint getGradientPaint() {
        return gradientPaint;
    }

    // Overwrite the JPanel renderer
    @Override
    public void paintComponent(Graphics graphics) {
        // Create the 2D copy
        Graphics2D graphics2D = (Graphics2D) graphics.create();

        // Create a new gradient painter with the current panel width.
        Point2D sta = new Point2D.Double(0, 0);
        Point2D sto = new Point2D.Double(getWidth(), 0);
        float[] pos = gradientPaint.getFractions();
        Color[] col = gradientPaint.getColors();
        LinearGradientPaint gradient = new LinearGradientPaint(sta, sto, pos, col);

        // Render the graphics
        graphics2D.setPaint(gradient);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());

        // Dispose of copy
        graphics2D.dispose();
    }


    public static void main(String[] args) {
        ColorGradientDialog dialog = new ColorGradientDialog();
        dialog.setVisible(true);
        JFrame frame = new JFrame("ColorGradientToolBar Test");
        frame.setSize(dimension);
        if (dialog.getGradientPainter() == null) {
            frame.add(new JLabel("Well, from nothing comes nothing."));
        } else {
            ColorGradientPanel bar = new ColorGradientPanel(dialog.getGradientPainter());
            frame.add(bar);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
