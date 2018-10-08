package de.mpicbg.knime.hcs.base.heatmap.color;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Useful manipulations for a LinearGradientPaint instance.
 *
 * @author Felix Meyenhofer
 *         12/10/12
 */
public abstract class LinearGradientTools {
	
	/**
	 * GB - green / black
	 * HSV - hue,saturation,value
	 * GBR - green / black / red
	 * Jet - jet gradient
	 * Dark - black / red / yellow
	 * RWB - red / white / blue
	 */

    /**
     * Available default color maps.
     */
	public static final Map<String, LinearGradientPaint> colorMap;
	static {
        Map<String, LinearGradientPaint> aMap = new HashMap<String,LinearGradientPaint>();
        aMap.put("GB", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,1f},
                new Color[] {new Color(0,0,0),
                             new Color(0, 255, 0)}));
        aMap.put("HSV", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.2f,0.4f,0.6f,0.8f,1f},
                new Color[] {new Color(255,0,0),
                             new Color(255, 255, 0),
                             new Color(0, 255, 0),
                             new Color(0, 255, 255),
                             new Color(0, 0, 255),
                             new Color(255, 0, 255)}));
        aMap.put("GBR", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.5f,1f},
                new Color[] {new Color(0,255,0),
                             new Color(0, 0, 0),
                             new Color(255, 0, 0)}));
        aMap.put("RBG", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.5f,1f},
                new Color[] {new Color(255, 0, 0),
                             new Color(0, 0, 0),
                             new Color(0, 255, 0)}));
        aMap.put("Jet", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.333f,0.666f,1f},
                new Color[] {new Color(255,0,0),
                             new Color(255, 255,0),
                             new Color(0, 255, 255),
                             new Color(0, 0, 255)}));
        aMap.put("Dark", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.5f,1f},
                new Color[] {new Color(0,0,0),
                             new Color(255, 0, 0),
                             new Color(255, 255, 0)}));
        aMap.put("RWB", new LinearGradientPaint(new Point2D.Double(0,0),
                new Point2D.Double(100, 0),
                new float[] {0f,0.5f,1f},
                new Color[] {new Color(255, 0,0),
                        new Color(255, 255, 255),
                        new Color(0, 0, 255)}));
        colorMap = Collections.unmodifiableMap(aMap);
    }
    
    /**
     * available default error colors 
     */
	public static final Map<String, Color> errColorMap;
	static {
        Map<String, Color> aMap = new HashMap<String,Color>();
        aMap.put("GB", Color.RED);
        aMap.put("HSV", Color.WHITE);
        aMap.put("GBR", Color.WHITE);
        aMap.put("RBG", Color.WHITE);
        aMap.put("Jet", Color.WHITE);
        aMap.put("Dark", Color.CYAN);
        aMap.put("RWB", Color.YELLOW);
        aMap.put("DEFAULT", new Color(72, 56, 69));
        errColorMap = Collections.unmodifiableMap(aMap);
    }


    /**
     * Get a color at a particular fraction of the gradient.
     *
     * @param painter to fetch the color from
     * @param input fraction [0...1]
     * @return the color at the input fraction
     */
    public static Color getColorAt(LinearGradientPaint painter, float input) {
        int lowerIndex = 0;
        int upperIndex = 1;
        float lowerBound = 0;
        float upperBound = 1;
        float[] fractions = painter.getFractions();
        Color[] colors = painter.getColors();

        if ( input > 1 ) {
            input = 1f;
        }

        if ( input < 0 ) {
            input = 0f;
        }

        // Select the closest two colors to the input fraction
        int index = 0;
        for (float fraction : fractions ) {

            if ( fraction < input ) {
                lowerBound = fraction;
                lowerIndex = index;
            } else if ( fraction == input ) {
                return colors[index];
            }
            index++;
        }

        index = 0;
        for (float fraction : fractions) {

            if ( fraction > input ) {
                upperBound = fraction;
                upperIndex = index;
                break;
            }
            index++;
        }

        // Scale the input
        float rescaled = (input - lowerBound) / (upperBound - lowerBound);

        // If there was no exact match, return the interpolated color.
        return interpolateColor(colors[lowerIndex], colors[upperIndex], rescaled);
    }


    /**
     * Linear interpolator between two colors
     *
     * @param color1 first bound
     * @param color2 second bound
     * @param fraction [0...1]
     * @return interpolation: color2 + (color2 - color1) * fraction
     */
    public static Color interpolateColor(final Color color1, final Color color2, final float fraction) {
        assert(Float.compare(fraction, 0f) >= 0 && Float.compare(fraction, 1f) <= 0);

        final float INT_TO_FLOAT_CONST = 1f / 255f;

        final float RED1 = color1.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN1 = color1.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE1 = color1.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA1 = color1.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED2 = color2.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN2 = color2.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE2 = color2.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA2 = color2.getAlpha() * INT_TO_FLOAT_CONST;

        final float DELTA_RED = RED2 - RED1;
        final float DELTA_GREEN = GREEN2 - GREEN1;
        final float DELTA_BLUE = BLUE2 - BLUE1;
        final float DELTA_ALPHA = ALPHA2 - ALPHA1;

        float red = RED1 + (DELTA_RED * fraction);
        float green = GREEN1 + (DELTA_GREEN * fraction);
        float blue = BLUE1 + (DELTA_BLUE * fraction);
        float alpha = ALPHA1 + (DELTA_ALPHA * fraction);

        red = red < 0f ? 0f : red;
        red = red > 1f ? 1f : red;
        green = green < 0f ? 0f : green;
        green = green > 1f ? 1f : green;
        blue = blue < 0f ? 0f : blue;
        blue = blue > 1f ? 1f : blue;
        alpha = alpha < 0f ? 0f : alpha;
        alpha = alpha > 1f ? 1f : alpha;

        return new Color(red, green, blue, alpha);
    }


    /**
     * Get a predefined color gradient
     *
     * @param str abbreviation or name of the gradient.
     * @return predefined gradient
     */
    public static LinearGradientPaint getStandardGradient(String str) {
        if (colorMap.containsKey(str)) 
        	return colorMap.get(str);
        else
            System.err.println("Don't know the '" + str + "' color map.");
        return null;
    }
    
    /**
     * retrieves the color for error-values according to color gradient
     * @param str
     * @return
     */
	public static Color getStandardErrorColor(String str) {
		if (errColorMap.containsKey(str)) return errColorMap.get(str);	
		return errColorMap.get("DEFAULT");
	}


    /**
     * Class to create a panel with a color gradient for display
     */
    public static class ColorGradientPanel extends JPanel {

        /**
         * Defaults
         */
        private static final Dimension dimension = new Dimension(400, 30);
        private LinearGradientPaint gradientPainter = getStandardGradient("GBR");


        /**
         * Constructor
         */
        public ColorGradientPanel() {
            setMinimumSize(dimension);
        }


        /**
         * Configure the gradient panel.
         *
         * @param painter color gradient to display.
         */
        public void configure(LinearGradientPaint painter) {
            this.gradientPainter = painter;
        }


        /** {@inheritDoc} */
        @Override
        public void paintComponent(Graphics graphics) {
            // Create the 2D copy
            //Graphics2D graphics2D = (Graphics2D) graphics.create();
            Graphics2D g2 = (Graphics2D) graphics;

            // Create a new gradient painter with the current panel width.
            Point2D sta = new Point2D.Double(0, 0);
            Point2D sto = new Point2D.Double(getWidth(), 0);
            float[] pos = gradientPainter.getFractions();
            Color[] col = gradientPainter.getColors();
            LinearGradientPaint gradient = new LinearGradientPaint(sta, sto, pos, col);
            
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            /*// Render the graphics
            graphics2D.setPaint(gradient);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            // Dispose of copy
            graphics2D.dispose();*/
        }


        /**
         * Quick testing
         *
         * @param args whatever
         */
        public static void main(String[] args) {
        	
        	// display with Eclipse/MacOS
        	final Display display = new Display();
            final Shell shell = new Shell(display);
            shell.setLayout(new FillLayout());
            shell.setText((new ColorGradientPanel()).getClass().getName());

            Composite myComp = new Composite(shell, SWT.EMBEDDED  | SWT.NO_BACKGROUND); 
            
            java.awt.Frame fileTableFrame = SWT_AWT.new_Frame(myComp);
            JPanel panel = new JPanel(new BorderLayout());
            fileTableFrame.add(panel);
            panel.add(new ColorGradientPanel(),java.awt.BorderLayout.CENTER);
            myComp.pack();

            shell.open();
            while (!shell.isDisposed()) {
              if (!display.readAndDispatch())
                display.sleep();
            }
            display.dispose();
                       
/*            //Schedule a job for the event-dispatching thread:
            //creating and showing this application's GUI.
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }

				private void createAndShowGUI() {
					// TODO Auto-generated method stub
					//ColorGradientPanel bar = new ColorGradientPanel();
		            JFrame frame = new JFrame("ColorGradientToolBar Test");
		            System.out.println(SwingUtilities.isEventDispatchThread());
		            //frame.setSize(new Dimension(400, 30));
		            //frame.add(bar);
		            //frame.add(new JButton("hello"));
		            frame.getContentPane().add(new JButton("hello"));
		            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		            frame.pack();
		            frame.setVisible(true);
				}
            });*/
            
        }
    }




}
