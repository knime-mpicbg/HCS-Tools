package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/**
 * User: Felix Meyenhofer
 * Date: 13/12/12
 *
 * Small dialog to choose between the automatic trellis layout in the screen view or to fix the row and column number.
 */

public class RowColumnDialog extends JDialog implements ItemListener {

    private JPanel contentPane;
    private JRadioButton automaticRadioButton;
    private JRadioButton setRadioButton;
    private JSpinner rowSpinner;
    private JSpinner columnSpinner;
    private JPanel automaticPanel;
    private JPanel manualPanel;

    private final Border passiveBorder = BorderFactory.createEtchedBorder();
    private final Border activeBorder = BorderFactory.createBevelBorder(1);


    public RowColumnDialog() {
        initialize();
        setContentPane(contentPane);
        setSize(new Dimension(180,230));
        setResizable(false);
        setModal(true);

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


    public void itemStateChanged(ItemEvent itemEvent) {
        JRadioButton source = (JRadioButton) itemEvent.getSource();
        if (source == setRadioButton) {
            columnSpinner.setEnabled(true);
            rowSpinner.setEnabled(true);
            manualPanel.setBorder(BorderFactory.createTitledBorder(activeBorder, setRadioButton.getName()));
            automaticPanel.setBorder(BorderFactory.createTitledBorder(passiveBorder, automaticRadioButton.getName()));
        } else if (source == automaticRadioButton) {
            columnSpinner.setEnabled(false);
            rowSpinner.setEnabled(false);
            manualPanel.setBorder(BorderFactory.createTitledBorder(passiveBorder, setRadioButton.getName()));
            automaticPanel.setBorder(BorderFactory.createTitledBorder(activeBorder, automaticRadioButton.getName()));
        }
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void initialize() {
        automaticRadioButton = new JRadioButton();
        automaticRadioButton.setName("Automatic");
        automaticRadioButton.setSelected(true);
        automaticRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
        automaticRadioButton.addItemListener(this);
        automaticPanel = new JPanel();
        automaticPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        automaticPanel.setBorder(BorderFactory.createTitledBorder(activeBorder, automaticRadioButton.getName()));
        automaticPanel.add(automaticRadioButton);

        rowSpinner = new JSpinner();
        rowSpinner.setEnabled(false);
        rowSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
        columnSpinner = new JSpinner();
        columnSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
        columnSpinner.setEnabled(false);

        setRadioButton = new JRadioButton();
        setRadioButton.setName("Set");
        setRadioButton.addItemListener(this);
        manualPanel = new JPanel(new GridBagLayout());
        manualPanel.setBorder(BorderFactory.createTitledBorder(passiveBorder, setRadioButton.getName()));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        manualPanel.add(setRadioButton, constraints);
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.fill = GridBagConstraints.VERTICAL;
        manualPanel.add(rowSpinner, constraints);
        constraints.gridy = 2;
        manualPanel.add(columnSpinner, constraints);
        constraints.weightx = 0.9;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        manualPanel.add(new JLabel("Rows"), constraints);
        constraints.gridy = 1;
        manualPanel.add(new JLabel("Columns"), constraints);
        constraints.gridx = 2;
        constraints.weightx = 1;
        manualPanel.add(Box.createHorizontalGlue(), constraints);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
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
        getRootPane().setDefaultButton(buttonOK);

        // Grouping the buttons
        ButtonGroup group = new ButtonGroup();
        group.add(automaticRadioButton);
        group.add(setRadioButton);

        // Create a contentPane
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        contentPane.add(automaticPanel);
        contentPane.add(manualPanel);
        contentPane.add(buttonPanel);
    }

    public Integer getNumberOfRows() {
        return (Integer) rowSpinner.getValue();
    }

    public Integer getNumberOfColumns() {
        return (Integer) columnSpinner.getValue();
    }

    public boolean isAutomatic() {
        return automaticRadioButton.isSelected();
    }


    public static void main(String[] args) {
        RowColumnDialog dialog = new RowColumnDialog();
        dialog.setVisible(true);
        System.exit(0);
    }

}
