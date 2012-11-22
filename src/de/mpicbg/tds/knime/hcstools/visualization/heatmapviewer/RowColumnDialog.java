package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RowColumnDialog extends JDialog implements ItemListener {

    private JPanel contentPane, buttonPanel, functionPanel, setPanel, spinnerPanel, rowsPanel, columnPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton automaticRadioButton;
    private JRadioButton setRadioButton;
    private JSpinner rowSpinner;
    private JSpinner columnSpinner;

    public RowColumnDialog() {
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

        setRadioButton.addItemListener(this);
        automaticRadioButton.addItemListener(this);


    }

    public void itemStateChanged(ItemEvent itemEvent) {
        JRadioButton source = (JRadioButton) itemEvent.getSource();
        if (source == setRadioButton) {
            columnSpinner.setEnabled(true);
            rowSpinner.setEnabled(true);
        } else if (source == automaticRadioButton) {
            columnSpinner.setEnabled(false);
            rowSpinner.setEnabled(false);
        }
        //To change body of implemented methods use File | Settings | File Templates.
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
        // TODO: The spinners should stick to the right side. Tried several things (see commented out stuff) nothing seems to work so far

        automaticRadioButton = new JRadioButton("Automatic");
        automaticRadioButton.setSelected(true);
        setRadioButton = new JRadioButton("Set");
        ButtonGroup group = new ButtonGroup();
        group.add(automaticRadioButton);
        group.add(setRadioButton);

        rowsPanel = new JPanel();
//        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.LINE_AXIS));
//        rowsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel rowLabel = new JLabel("Rows");
//        rowLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rowsPanel.add(rowLabel);
        rowSpinner = new JSpinner();
        rowSpinner.setEnabled(false);
        rowSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
//        rowSpinner.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rowsPanel.add(rowSpinner);

        columnPanel = new JPanel();
//        columnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel columnsLabel = new JLabel("Columns");
//        columnsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnPanel.add(columnsLabel);
        columnSpinner = new JSpinner();
        columnSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
        columnSpinner.setEnabled(false);
//        columnSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnPanel.add(columnSpinner);

        spinnerPanel = new JPanel();
        spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.Y_AXIS));
//        spinnerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinnerPanel.add(rowsPanel);
        spinnerPanel.add(columnPanel);


        setPanel = new JPanel();
//        setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.Y_AXIS));
//        setPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        setPanel.setBorder(BorderFactory.createBevelBorder(1));
//        setPanel.setBounds(1,1,1,1);
        setPanel.add(setRadioButton);
        setPanel.add(spinnerPanel);


        functionPanel = new JPanel();
        functionPanel.add(automaticRadioButton);
        functionPanel.add(setPanel);

        buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonCancel = new JButton("Cancel");
        buttonPanel.add(buttonCancel);
        buttonOK = new JButton("OK");
        buttonPanel.add(buttonOK);


        contentPane = new JPanel();
//        contentPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(functionPanel);
        contentPane.add(buttonPanel);
    }


    public static void main(String[] args) {
        RowColumnDialog dialog = new RowColumnDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
