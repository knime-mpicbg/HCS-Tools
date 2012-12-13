package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.PlateComparators;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 12/12/12
 *
 * Small dialog to select plate attributes.
 */

public class PlateAttributeDialog extends JDialog {

    private HashMap<String, Integer> selection = new HashMap<String, Integer>();
    private HeatMapModel2 heatMapModel;
    private JTable table;
    private String[][] tableData = new String[3][2];
    private ListSelectionModel listSelectionModel;

    private static final String[] columnNames = {"order", "Attribute"};


    public PlateAttributeDialog() {
        this(null);
    }

    public PlateAttributeDialog (HeatMapModel2 model) {
        heatMapModel = model;
        setSize(new Dimension(250, 200));
        initialize();
        configureTable();
        setModal(true);
        setLocationRelativeTo(getOwner());
        setTitle("Filtering Selector");
        setResizable(false);
    }


    private void onOK() {
        selection = getSelectionFromTable();
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void initialize() {
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
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weighty = 0.9;
        constraints.weightx = 1;
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.fill = GridBagConstraints.BOTH;
        contentPane.add(createTable(), constraints);
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

    public HashMap<String, Integer> getSelection() {
        return selection;
    }

    public HashMap<String, Integer> getSelectionFromTable() {
        HashMap<String, Integer> sel = new HashMap<String, Integer>();
        for (int i=0; i<table.getRowCount(); i++) {
            if (!table.getValueAt(i,0).equals("-")) {
                sel.put( (String)table.getValueAt(i,1) , Integer.parseInt((String) table.getValueAt(i,0)));
            }
        }
        return sel;
    }

    private JScrollPane createTable() {
        DefaultTableModel model = new DefaultTableModel(tableData, columnNames);
        table = new JTable(model){
            @Override
            public boolean isCellEditable ( int row, int column )
            {
                return false;
            }
        };

        listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SelectionHandler());
        table.setSelectionModel(listSelectionModel);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(table);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return pane;
    }

    private void configureTable() {

        if ( !(heatMapModel == null) ) {
            // Set the attribute names.
            List<String> attributes = Arrays.asList(PlateComparators.getPlateAttributeTitles(heatMapModel.getPlateAttributes()));
            tableData = new String[attributes.size()][1];
            DefaultTableModel model = new DefaultTableModel(tableData, columnNames);
            table.setModel(model);

            int index = 0;
            for (String attribute : attributes) {
                model.setValueAt("-", index,0);
                model.setValueAt(attribute,index++,1);
            }

            // Restore the previous selection.
            if (!(heatMapModel.getSortAttributeSelection() == null)) {
                selection = heatMapModel.getSortAttributeSelection();

                //... of the attributes
                int pos;
                for (String key : selection.keySet()) {
                    pos = attributes.indexOf(key);
                    model.setValueAt(""+selection.get(key), pos, 0);
                    listSelectionModel.addSelectionInterval(pos, pos);
                }
            }
        }

        // TODO: find a neat way to set the column widths. This does not work for some reason.
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        TableColumnModel columnModel = table.getColumnModel();
//        columnModel.getColumn(0).setPreferredWidth(40);
//        columnModel.getColumn(0).setWidth(40);
//        columnModel.getColumn(1).setPreferredWidth(getWidth()-54);
//        columnModel.getColumn(1).setWidth(getWidth()-54);
//        table.setColumnModel(columnModel);
        repaint();
    }


    public static void main(String[] args) {
        PlateAttributeDialog dialog = new PlateAttributeDialog();
        dialog.setVisible(true);
        System.exit(0);
    }



    private class SelectionHandler implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            ListSelectionModel lsm = (ListSelectionModel) event.getSource();

            // Only use one of the actions
            if (lsm.getValueIsAdjusting()){
                return;
            }

            // fetch previous index map (order, position) and the indices of the selected rows
            HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
            ArrayList<Integer> selectedIndices = new ArrayList<Integer>();

            for (int i=0; i <table.getRowCount(); i++) {

                if (lsm.isSelectedIndex(i)) {
                    if (!table.getValueAt(i,0).equals("-")) {
                        indexMap.put((String) table.getValueAt(i, 0), i);
                    }
                    selectedIndices.add(i);
                } else if (!table.getValueAt(i,0).equals("-")) {
                    table.setValueAt("-", i, 0);
                }
            }

            // Sort according the order
            List<String> orders = new ArrayList<String>(indexMap.keySet());
            Collections.sort(orders);

            // Re-index if one selection dropped out. Also invert keys and values for the next step
            HashMap<Integer, String> validatedIndexMap = new HashMap<Integer, String>();
            int newOrder = 1;
            for (String order : orders) {
                validatedIndexMap.put(indexMap.get(order), ""+newOrder);
                newOrder++;
            }

            // Update the table
            int order = indexMap.size();
            for (Integer pos : selectedIndices) {

                if (validatedIndexMap.containsKey(pos)) {
                    table.setValueAt(validatedIndexMap.get(pos), pos, 0);
                } else {
                    ++order;
                    table.setValueAt("" + order, pos, 0);
                }
            }
            repaint();
        }
    }


}