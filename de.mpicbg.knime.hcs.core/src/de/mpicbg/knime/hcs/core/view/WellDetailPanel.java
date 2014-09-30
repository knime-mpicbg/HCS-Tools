/*
 * Created by JFormDesigner on Fri Jun 04 09:04:23 CEST 2010
 */

package de.mpicbg.knime.hcs.core.view;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.model.Well;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


/**
 * @author Holger Brandl
 */
public class WellDetailPanel extends JPanel {

    public WellDetailPanel(Well well) {

        initComponents();

        setMinimumSize(new Dimension(200, 300));
        setPreferredSize(new Dimension(200, 300));

        StringBuffer toolTipHeader = new StringBuffer();
        toolTipHeader.append("Plate: " + well.getPlate().getBarcode() + "\n");
        toolTipHeader.append("Position: [" + TdsUtils.mapPlateRowNumberToString(well.getPlateRow()) + "," + well.getPlateColumn() + "]\n");
        toolTipHeader.append("Treatment: " + well.getTreatment() + "\n");

        JTextArea headerArea = new JTextArea();
        headerArea.setEditable(false);
        headerArea.setEnabled(false);
        headerArea.setText(toolTipHeader.toString());
        overviewPanel.add(headerArea, BorderLayout.NORTH);


        //  append also all its readout values
        StringBuffer readouts = new StringBuffer();
        Object[][] tableData = new Object[well.getReadOutNames().size()][2];

        int counter = 0;
        for (String readoutName : well.getReadOutNames()) {
            tableData[counter][0] = readoutName;
            Double value = well.getReadout(readoutName);
            tableData[counter++][1] = value == null ? "" : value;
        }

        // replace the model
        readoutTable.setModel(new DefaultTableModel(tableData, new Object[]{"Name", "Value"}));


        splitPane1.remove(compoundPanel);
        splitPane1.setDividerLocation(1.);
        splitPane1.setDividerSize(0);
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        overviewPanel = new JPanel();
        splitPane1 = new JSplitPane();
        scrollTable = new JScrollPane();
        readoutTable = new JTable();
        compoundPanel = new JPanel();

        //======== this ========
        setLayout(new BorderLayout());

        //======== overviewPanel ========
        {
            overviewPanel.setLayout(new BorderLayout());
        }
        add(overviewPanel, BorderLayout.NORTH);

        //======== splitPane1 ========
        {
            splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane1.setResizeWeight(0.8);

            //======== scrollTable ========
            {

                //---- readoutTable ----
                readoutTable.setModel(new DefaultTableModel(
                        new Object[][]{
                                {null, null},
                                {null, null},
                        },
                        new String[]{
                                "Name", "Value"
                        }
                ) {
                    boolean[] columnEditable = new boolean[]{
                            false, true
                    };


                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return columnEditable[columnIndex];
                    }
                });
                scrollTable.setViewportView(readoutTable);
            }
            splitPane1.setTopComponent(scrollTable);

            //======== compoundPanel ========
            {
                compoundPanel.setMinimumSize(new Dimension(100, 100));
                compoundPanel.setLayout(new BorderLayout());
            }
            splitPane1.setBottomComponent(compoundPanel);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JPanel overviewPanel;
    private JSplitPane splitPane1;
    private JScrollPane scrollTable;
    private JTable readoutTable;
    private JPanel compoundPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
