/*
 * Created by JFormDesigner on Wed Aug 04 13:11:37 CEST 2010
 */

package de.mpicbg.tds.knime.hcstools.joinimages;

import javax.swing.*;
import java.awt.*;


/**
 * @author Holger Brandl
 */
public class WellListImagePanel extends JPanel {

    public WellListImagePanel() {
        initComponents();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        splitPane1 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        wellTable = new JTable();
        wellImagesPanel = new WellImagesPanel();

        //======== this ========
        setLayout(new BorderLayout());

        //======== splitPane1 ========
        {

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(wellTable);
            }
            splitPane1.setLeftComponent(scrollPane1);
            splitPane1.setRightComponent(wellImagesPanel);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JSplitPane splitPane1;
    private JScrollPane scrollPane1;
    private JTable wellTable;
    private WellImagesPanel wellImagesPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
