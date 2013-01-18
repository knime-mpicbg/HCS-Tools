package de.mpicbg.tds.knime.hcstools.visualization.heatmap.dialog;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.PlateUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Small dialog to select plate attributes for plate sorting
 *
 * @author Felix Meyenhofer
 *         12/12/12
 */

public class PlateAttributeDialog extends JDialog {

    /** Data model */
    private HeatMapModel heatMapModel;

    /** Attribute Names in the order of selection */
    private TreeMap<Integer, String> selection = new TreeMap<Integer, String>();
    /** Table with the selection order and the attribute name */
    private JTable table;
    /** Data of the table */
    private String[][] tableData = new String[3][2];
    /** Selection listener of the table */
    private ListSelectionModel listSelectionModel;
    /** Radio button to determine the sorting direction */
    private JRadioButton descending;
    /** Column names of the table */
    private static final String[] columnNames = {"order", "Attribute"};


    /**
     * Constructor for testing
     */
    public PlateAttributeDialog() {
        this(null);
    }

    /**
     * Dialog constructor
     *
     * @param model data model
     */
    public PlateAttributeDialog (HeatMapModel model) {
        heatMapModel = model;
        setSize(new Dimension(300, 270));
        initialize();
        configureTable();
        setModal(true);
        setLocationRelativeTo(getOwner());
        setTitle("Sorting Attribute Selector");
        setResizable(false);
    }


    /**
     * Action called on pressing the ok button
     */
    private void onOK() {
        selection = getSelectionFromTable();
        dispose();
    }

    /**
     * Action called on pressing the cancel button
     */
    private void onCancel() {
        dispose();
    }

    /**
     * initialization of the UI components
     */
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

        // Sense
        Font font = new Font("Arial", Font.PLAIN, 12);
        JPanel sensPanel = new JPanel();
        Border border = BorderFactory.createEtchedBorder();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(border, "Sorting Direction");
        titledBorder.setTitleFont(font);
        sensPanel.setBorder(titledBorder);
        ButtonGroup group = new ButtonGroup();
        JRadioButton radio = new JRadioButton("Ascending");
        radio.setFont(font);
        group.add(radio);
        sensPanel.add(radio);
        descending = new JRadioButton("Descending");
        descending.setFont(font);
        descending.setSelected(true);
        group.add(descending);
        sensPanel.add(descending);

        // Reunite the different sections in one pane.
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.weighty = 0.8;
        constraints.weightx = 1;
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.fill = GridBagConstraints.BOTH;
        contentPane.add(createTable(), constraints);
        table.setFont(font);
        constraints.gridy = 1;
        constraints.weighty = 0.01;
        contentPane.add(sensPanel, constraints);
        constraints.gridy = 2;
        contentPane.add(buttonPanel, constraints);

        setContentPane(contentPane);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Getter of the selection status of the radiobutton
     *
     * @return selection status
     */
    public boolean isDescending() {
        return descending.isSelected();
    }

    /**
     * Returns the selected attribute names
     *
     * @return list of selected attribute names
     */
    public String[] getSelectedAttributeTitles() {
        String[] attributes = new String[selection.size()];
        int index = 0;
        for (Object attribute: selection.values()) {
            attributes[index++] = (String) attribute;
        }
        return attributes;
    }

    /**
     * Returns the treemap with the selected attributes in the order of selection
     *
     * @return ordered selection
     */
    public TreeMap<Integer, String> getSelectionFromTable() {
        TreeMap<Integer, String> sel = new TreeMap<Integer, String>();
        for (int i=0; i<table.getRowCount(); i++) {
            if (!table.getValueAt(i,0).equals("-")) {
                sel.put(Integer.parseInt((String) table.getValueAt(i,0)), (String)table.getValueAt(i,1));
            }
        }
        return sel;
    }

    /**
     * Create the attribute table
     *
     * @return scrollable table
     */
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

    /**
     * Configure the attribute table
     */
    private void configureTable() {

        if ( !(heatMapModel == null) ) {
            // Fetch the attribute names.
            String[] attributes = PlateUtils.getPlateAttributeTitles(heatMapModel.getPlateAttributes());
            String[] selectedAttributes = heatMapModel.getSortAttributesSelectionTitles();

            // initialize the table
            tableData = new String[attributes.length][1];
            DefaultTableModel model = new DefaultTableModel(tableData, columnNames);
            table.setModel(model);

            int index = 0;
            List<Integer> selectionIndex = new ArrayList<Integer>();
            for (String attribute : attributes) {
                if (ArrayUtils.contains(selectedAttributes, attribute)) {
                    selectionIndex.add(index);
                }
                model.setValueAt("-", index,0);
                model.setValueAt(attribute,index++,1);
            }

            // Restore the previous selection.
            for (Integer position : selectionIndex) {
                listSelectionModel.addSelectionInterval(position, position);
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


    /**
     * Quick testing
     *
     * @param args whatever
     */
    public static void main(String[] args) {
        PlateAttributeDialog dialog = new PlateAttributeDialog();
        dialog.setVisible(true);
        System.exit(0);
    }


    /**
     * Selection handler
     */
    private class SelectionHandler implements ListSelectionListener {

        /** {@inheritDoc} */
        @Override
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
