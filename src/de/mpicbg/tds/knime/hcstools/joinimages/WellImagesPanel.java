/*
 * Created by JFormDesigner on Wed Aug 04 13:34:33 CEST 2010
 */

package de.mpicbg.tds.knime.hcstools.joinimages;

import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;


/**
 * @author Holger Brandl
 */
public class WellImagesPanel extends JPanel {

    public WellImagesPanel() {
        initComponents();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        imagesContainer = new JPanel();
        panel2 = new JPanel();

        //======== this ========
        setLayout(new BorderLayout());

        //======== imagesContainer ========
        {
            imagesContainer.setLayout(new BorderLayout());

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Display Properties"));
                panel2.setMinimumSize(new Dimension(120, 88));
                panel2.setPreferredSize(new Dimension(140, 88));
                panel2.setLayout(new TableLayout(new double[][]{
                        {TableLayout.FILL},
                        {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
            }
            imagesContainer.add(panel2, BorderLayout.SOUTH);
        }
        add(imagesContainer, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JPanel imagesContainer;
    private JPanel panel2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
