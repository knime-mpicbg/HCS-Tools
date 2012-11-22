package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import org.jdesktop.swingx.JXGradientChooser;
import org.jdesktop.swingx.JXPanel;


public class ColorGradientDialog extends JDialog {

    private JPanel contentPane, mainPanel, buttonPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JXGradientChooser gradientChooser;

    private Component[] components;


    public ColorGradientDialog() {
        this.initialize();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

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
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void initialize() {
        buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonCancel = new JButton("Cancel");
        buttonOK = new JButton("OK");
        buttonPanel.add(buttonCancel);
        buttonPanel.add(buttonOK);

        mainPanel = new JPanel();
        gradientChooser = new JXGradientChooser();
//        components = gradientChooser.getRootPane().getContentPane().getComponents();
//        for (int i = 0; i < components.length; i++) {
//            System.out.println(components[i].getName());
//        }
        mainPanel.add(gradientChooser);

        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(mainPanel);
        contentPane.add(buttonPanel);
    }

    public static void main(String[] args) {
        ColorGradientDialog dialog = new ColorGradientDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
