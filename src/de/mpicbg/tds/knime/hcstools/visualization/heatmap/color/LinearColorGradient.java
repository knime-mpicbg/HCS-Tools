package de.mpicbg.tds.knime.hcstools.visualization.heatmap.color;

import java.awt.LinearGradientPaint;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This is a simple wrapper of the {@link LinearGradientPaint} to have
 * a serializable version of it.
 *
 * @author Felix Meyenhofer
 *         creation: 1/11/13
 */

public class LinearColorGradient implements Serializable {

    private LinearGradientPaint gradient;
    private String gradientName;


    public LinearColorGradient() {
        this.gradientName = "GBR";
        this.gradient = LinearGradientTools.getStandardGradient(gradientName);
    }

    public LinearColorGradient(String name, LinearGradientPaint gradient) {
        this.gradientName = name;
        this.gradient = gradient;
    }


    public LinearGradientPaint getGradient() {
        return gradient;
    }

    public String getGradientName() {
        return gradientName;
    }


    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        float[] fractions = (float[]) inputStream.readObject();
        Color[] colors = (Color[]) inputStream.readObject();
        Point2D start = (Point2D) inputStream.readObject();
        Point2D end = (Point2D) inputStream.readObject();
        gradient = new LinearGradientPaint(start, end, fractions, colors);
        gradientName = (String) inputStream.readObject();
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        float[] fractions = gradient.getFractions();
        Color[] colors = gradient.getColors();
        Point2D start = gradient.getStartPoint();
        Point2D end = gradient.getEndPoint();

        outputStream.writeObject(fractions);
        outputStream.writeObject(colors);
        outputStream.writeObject(start);
        outputStream.writeObject(end);
        outputStream.writeObject(gradientName);
    }

}
