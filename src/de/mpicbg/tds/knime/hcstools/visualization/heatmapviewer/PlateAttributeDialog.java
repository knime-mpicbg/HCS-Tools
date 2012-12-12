package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 12/12/12
 *
 * Small dialog to select plate attributes.
 * TODO: indicate in some way the selection order
 */

public class PlateAttributeDialog extends JDialog {

    private JList attributeList;
    private JList numberList;
    private HashMap<String, Integer> selection = new HashMap<String, Integer>();
    private HeatMapModel2 heatMapModel;


    public PlateAttributeDialog() {
        this(null);
    }

    public PlateAttributeDialog (HeatMapModel2 model) {
        heatMapModel = model;
        initialize();
        setSize(new Dimension(250, 200));
        setModal(true);
        setLocationRelativeTo(getOwner());
        setTitle("Plate Attribute Selector");
    }


    private void onOK() {
        heatMapModel.setPlateSortingAttributes(getSelection());
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void initialize() {
        numberList = new JList();
        numberList.setEnabled(false);
        numberList.setBorder(BorderFactory.createEtchedBorder());
//        numberList.setBackground(new Color(221, 221, 221));
        // Selection list.
        attributeList = new JList();
        attributeList.setVisibleRowCount(5);
        attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        attributeList.setToolTipText("<html>Select one or more plate attribute<br/>according to which the plates " +
                "should be sorted.<br/>the order of selection is relevant!");

        // Restore the previous state
        if ( !(heatMapModel == null) ) {
            List<String> plateAttributes = heatMapModel.getPlateAttributes();
            DefaultComboBoxModel model = new DefaultComboBoxModel(plateAttributes.toArray());
            attributeList.setModel(model);

            // Mark the previous selection.
            if (!(heatMapModel.getPlateSortingAttributes() == null)) {

                //... of the attributes
                List<String> oldAttributes = Arrays.asList(heatMapModel.getPlateSortingAttributes());
                int[] index = new int[oldAttributes.size()];
                for (int i = 0; i < oldAttributes.size(); i++) {
                    index[i] = plateAttributes.indexOf(oldAttributes.get(i));
                }
                attributeList.setSelectedIndices(index);

                // ...of the number list
                updateOrderNumberList();
            }
        }

        attributeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
//                ListSelectionModel lsm = (ListSelectionModel)listSelectionEvent.getSource();
//
//                int firstIndex = listSelectionEvent.getFirstIndex();
//                int lastIndex = listSelectionEvent.getLastIndex();

                updateOrderNumberList();
            }
        });

        // Put everything in a scroll pane
        JPanel listPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        listPanel.add(numberList, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0.9;
        listPanel.add(attributeList, constraints);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(listPanel);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Ok and cancel buttons
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

        // Reunite the different sections in one pane.
        JPanel contentPane = new JPanel(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weighty = 0.9;
        constraints.weightx = 1;
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.fill = GridBagConstraints.BOTH;
        contentPane.add(pane, constraints);
        constraints.gridy = 1;
        constraints.weighty = -1;
        contentPane.add(buttonPanel, constraints);

        setContentPane(contentPane);

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

    public String[] getSelection() {
        String[] list = new String[attributeList.getSelectedIndices().length];
        int index = 0;
        for (Object item : attributeList.getSelectedValues()) {
            list[index++] = (String) item;
        }
        return list;
    }

    private void updateOrderNumberList() {
        String[] numbers = new String[attributeList.getModel().getSize()];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = "-";
        }
        int[] indices = attributeList.getSelectedIndices();
        int nb;
        for (int i=0; i<indices.length; i++) {
            nb = i+1;
            numbers[indices[i]] = "" + nb;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(numbers);
        numberList.setModel(model);
    }


    public static void main(String[] args) {
        PlateAttributeDialog dialog = new PlateAttributeDialog();
        dialog.setVisible(true);
        System.exit(0);
    }

}
