package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.view.WellPropertySelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WellAttributeDialog extends JDialog {
    private JPanel contentPane, buttonPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private WellPropertySelector propertySelector;
    private JList parameterList;

    public WellAttributeDialog() {
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
        // TODO: Add the option that the WellProbertySelector returns a list (to allow mutliptle selection.)
        propertySelector = new WellPropertySelector();

        buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonCancel = new JButton("Cancel");
        buttonPanel.add(buttonCancel);
        buttonOK = new JButton("OK");
        buttonPanel.add(buttonOK);

        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(propertySelector);
        contentPane.add(buttonPanel);
    }


    public static void main(String[] args) {
        WellAttributeDialog dialog = new WellAttributeDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
