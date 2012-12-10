package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import com.bric.swing.GradientSlider;
import com.bric.swing.MultiThumbSlider;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;


public class ColorGradientDialog extends JDialog {

    private GradientSlider slider;
    private LinearGradientPaint currentGradient;


    // Constructor
    public ColorGradientDialog() {
        setContentPane(initialize());
        setSize(new Dimension(500, 200));
        setModal(true);
    }

    public ColorGradientDialog(String title) {
        this();
        setTitle(title);
    }

    public ColorGradientDialog(LinearGradientPaint gradient) {
        currentGradient = gradient;
        setContentPane(initialize());
        setSize(new Dimension(500, 200));
        setModal(true);
    }


    // Utilities
    private JPanel initialize() {
        // Create the cancel and ok buttons in a seperate panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        buttonPanel.add(buttonCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton buttonOK = new JButton("OK");
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonPanel.add(buttonOK);

        // Create the Gradient slider
        slider = new GradientSlider(MultiThumbSlider.HORIZONTAL);
        if (currentGradient == null) {
            currentGradient = LinearGradientTools.getStandardGradient("GB");
        }
        slider.setValues(currentGradient.getFractions(), currentGradient.getColors());
        slider.setPaintTicks(true);
        slider.setBorder(BorderFactory.createEtchedBorder());
        slider.putClientProperty("MultiThumbSlider.indicateComponent", "false");
        slider.putClientProperty("GradientSlider.useBevel", "true");
        slider.setToolTipText("<html>click on the thumbs to select them<br/>dragg the thumbs to slide or remove them<br/>" +
                "double click on a thumb to choose it's color<html>");

        // Create the content paine and layout the buttons and the slider.
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10,10,10,10);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.weighty = 0.8;
        contentPane.add(slider, constraints);
        constraints.gridy = 1;
        constraints.weighty = 0.2;
        contentPane.add(buttonPanel, constraints);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        return contentPane;
    }

    public LinearGradientPaint getGradientPainter() {
        return currentGradient;
    }


    // Actions
    private void onOK() {
        Color[] colors = slider.getColors();
        float[] positions = slider.getThumbPositions();
        Point2D startPoint = new Point2D.Double(0,0);
        Point2D endPoint = new Point2D.Double(255,0);
        currentGradient = new LinearGradientPaint(startPoint, endPoint, positions, colors);
        dispose();
    }

    private void onCancel() {
        dispose();
    }


    // Reveal yourself!
    public static void main(String[] args) {
        ColorGradientDialog dialog = new ColorGradientDialog("Color gradient dialog test");
        dialog.setVisible(true);
        System.exit(0);
    }

}
