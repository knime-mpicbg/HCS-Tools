/*
 * Created by JFormDesigner on Tue Nov 23 08:33:01 CET 2010
 */

package de.mpicbg.knime.hcs.base.nodes.helper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Holger Brandl
 */
public class ExDataSelectPanel extends JPanel {

    private ExDataSet curDataSet;

    private List<ExDataSet> exDataSets;


    public ExDataSelectPanel(List<URL> exampleListURLs) {
        loadTemplates(exampleListURLs);

        initComponents();

        exampleList.setModel(new DefaultListModel());
        exFilterTextFieldActionPerformed();
    }


    private void loadTemplates(List<URL> exampleListURLs) {
        exDataSets = new ArrayList<ExDataSet>();

        for (URL exampleListURL : exampleListURLs) {
            List<ExDataSet> dataSetList = new ArrayList<ExDataSet>();

            try {
                dataSetList = ExDataSet.parseExampleList(exampleListURL);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            exDataSets.addAll(dataSetList);
        }
    }


    public ExDataSet getCurDataSet() {
        return curDataSet;
    }


    private void exFilterTextFieldActionPerformed() {
        DefaultListModel model = (DefaultListModel) exampleList.getModel();
        model.removeAllElements();

        String filter = exFilterTextField.getText().toLowerCase();

        for (ExDataSet exDataSet : exDataSets) {
            if (filter.isEmpty() ||
                    exDataSet.getExampleName().toLowerCase().contains(filter) ||
                    exDataSet.getDescription().toLowerCase().contains(filter)) {

                model.addElement(exDataSet);
            }
        }
    }


    public void setSelectedExample(String fileURL) {
        DefaultListModel model = (DefaultListModel) exampleList.getModel();

        for (int i = 0; i < model.size(); i++) {
            ExDataSet exDataSet = (ExDataSet) model.get(i);
            if (exDataSet.getFileURL().equals(fileURL)) {
                exampleList.setSelectedIndex(i);
            }
        }
    }


    private void exampleListValueChanged() {
        curDataSet = (ExDataSet) exampleList.getSelectedValue();

        descPanel.setText(curDataSet == null ? "" : curDataSet.getDescription());
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        panel1 = new JPanel();
        label1 = new JLabel();
        exFilterTextField = new JTextField();
        scrollPane1 = new JScrollPane();
        exampleList = new JList();
        panel2 = new JPanel();
        descPanel = new JTextPane();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setLayout(new BorderLayout());

            //---- label1 ----
            label1.setText("Search :");
            panel1.add(label1, BorderLayout.WEST);

            //---- exFilterTextField ----
            exFilterTextField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exFilterTextFieldActionPerformed();
                }
            });
            panel1.add(exFilterTextField, BorderLayout.CENTER);
        }
        add(panel1, BorderLayout.NORTH);

        //======== scrollPane1 ========
        {
            scrollPane1.setMinimumSize(new Dimension(23, 100));

            //---- exampleList ----
            exampleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            exampleList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    exampleListValueChanged();
                }
            });
            scrollPane1.setViewportView(exampleList);
        }
        add(scrollPane1, BorderLayout.CENTER);

        //======== panel2 ========
        {
            panel2.setBorder(new TitledBorder("Description"));
            panel2.setMinimumSize(new Dimension(22, 100));
            panel2.setPreferredSize(new Dimension(22, 100));
            panel2.setLayout(new BorderLayout());

            //---- descPanel ----
            descPanel.setEditable(false);
            descPanel.setBackground(null);
            panel2.add(descPanel, BorderLayout.CENTER);
        }
        add(panel2, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JPanel panel1;
    private JLabel label1;
    private JTextField exFilterTextField;
    private JScrollPane scrollPane1;
    private JList exampleList;
    private JPanel panel2;
    private JTextPane descPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
