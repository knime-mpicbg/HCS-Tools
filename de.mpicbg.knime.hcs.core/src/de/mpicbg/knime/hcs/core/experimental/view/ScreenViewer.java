/*
 * Created by JFormDesigner on Fri Oct 07 16:50:56 CEST 2011
 */

package de.mpicbg.knime.hcs.core.experimental.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author User #1
 */
public class ScreenViewer extends JPanel {
    public ScreenViewer() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        tabbedPane1 = new JTabbedPane();
        panel4 = new JPanel();
        slider1 = new JSlider();
        panel1 = new JPanel();
        menuBar1 = new JMenuBar();
        menu3 = new JMenu();
        menu4 = new JMenu();
        menuItem7 = new JMenuItem();
        menuItem8 = new JMenuItem();
        menuItem9 = new JMenuItem();
        menuItem10 = new JMenuItem();
        menu5 = new JMenu();
        radioButtonMenuItem1 = new JRadioButtonMenuItem();
        radioButtonMenuItem2 = new JRadioButtonMenuItem();
        hSpacer1 = new JPanel(null);
        label5 = new JLabel();
        textField1 = new JTextField();
        label1 = new JLabel();
        button3 = new JButton();
        label2 = new JLabel();
        panel3 = new JPanel();
        scrollPane4 = new JScrollPane();
        panel5 = new JPanel();
        panel8 = new JPanel();
        label3 = new JLabel();
        hSpacer2 = new JPanel(null);
        panel6 = new JPanel();
        button1 = new JButton();
        scrollPane2 = new JScrollPane();
        table1 = new JTable();
        panel9 = new JPanel();
        label4 = new JLabel();
        hSpacer3 = new JPanel(null);
        panel7 = new JPanel();
        button2 = new JButton();
        scrollPane3 = new JScrollPane();
        table2 = new JTable();

        //======== tabbedPane1 ========
        {

            //======== panel4 ========
            {
                panel4.setLayout(new BorderLayout());
                panel4.add(slider1, BorderLayout.SOUTH);

                //======== panel1 ========
                {
                    panel1.setLayout(new BorderLayout());

                    //======== menuBar1 ========
                    {

                        //======== menu3 ========
                        {
                            menu3.setText("Configure");

                            //======== menu4 ========
                            {
                                menu4.setText("Sort by");

                                //---- menuItem7 ----
                                menuItem7.setText("Date");
                                menu4.add(menuItem7);

                                //---- menuItem8 ----
                                menuItem8.setText("text");
                                menu4.add(menuItem8);

                                //---- menuItem9 ----
                                menuItem9.setText("text");
                                menu4.add(menuItem9);

                                //---- menuItem10 ----
                                menuItem10.setText("text");
                                menu4.add(menuItem10);
                            }
                            menu3.add(menu4);

                            //======== menu5 ========
                            {
                                menu5.setText("Z-projection");

                                //---- radioButtonMenuItem1 ----
                                radioButtonMenuItem1.setText("Mean");
                                menu5.add(radioButtonMenuItem1);

                                //---- radioButtonMenuItem2 ----
                                radioButtonMenuItem2.setText("Sum");
                                menu5.add(radioButtonMenuItem2);
                            }
                            menu3.add(menu5);
                        }
                        menuBar1.add(menu3);
                        menuBar1.add(hSpacer1);

                        //---- label5 ----
                        label5.setText("Filter plates");
                        menuBar1.add(label5);
                        menuBar1.add(textField1);

                        //---- label1 ----
                        label1.setIcon(new ImageIcon("/Users/niederle/Desktop/zoomIn.png"));
                        menuBar1.add(label1);

                        //---- button3 ----
                        button3.setText("Zoom 1:1");
                        menuBar1.add(button3);

                        //---- label2 ----
                        label2.setIcon(new ImageIcon("/Users/niederle/Desktop/zoomIn.png"));
                        menuBar1.add(label2);
                    }
                    panel1.add(menuBar1, BorderLayout.NORTH);

                    //======== panel3 ========
                    {
                        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
                    }
                    panel1.add(panel3, BorderLayout.SOUTH);
                    panel1.add(scrollPane4, BorderLayout.CENTER);
                }
                panel4.add(panel1, BorderLayout.CENTER);
            }
            tabbedPane1.addTab("View", panel4);


            //======== panel5 ========
            {
                panel5.setLayout(new BoxLayout(panel5, BoxLayout.Y_AXIS));

                //======== panel8 ========
                {
                    panel8.setLayout(new BoxLayout(panel8, BoxLayout.X_AXIS));

                    //---- label3 ----
                    label3.setText("Parameters");
                    panel8.add(label3);
                    panel8.add(hSpacer2);
                }
                panel5.add(panel8);

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));

                    //---- button1 ----
                    button1.setText("add / remove");
                    button1.setHorizontalTextPosition(SwingConstants.LEFT);
                    panel6.add(button1);

                    //======== scrollPane2 ========
                    {
                        scrollPane2.setPreferredSize(new Dimension(200, 150));

                        //---- table1 ----
                        table1.setBorder(new EtchedBorder());
                        scrollPane2.setViewportView(table1);
                    }
                    panel6.add(scrollPane2);
                }
                panel5.add(panel6);

                //======== panel9 ========
                {
                    panel9.setLayout(new BoxLayout(panel9, BoxLayout.X_AXIS));

                    //---- label4 ----
                    label4.setText("Meta information");
                    panel9.add(label4);
                    panel9.add(hSpacer3);
                }
                panel5.add(panel9);

                //======== panel7 ========
                {
                    panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));

                    //---- button2 ----
                    button2.setText("add / remove");
                    panel7.add(button2);

                    //======== scrollPane3 ========
                    {
                        scrollPane3.setPreferredSize(new Dimension(200, 150));

                        //---- table2 ----
                        table2.setBorder(new EtchedBorder());
                        scrollPane3.setViewportView(table2);
                    }
                    panel7.add(scrollPane3);
                }
                panel5.add(panel7);
            }
            tabbedPane1.addTab("Adapt", panel5);

        }

        //---- buttonGroupZProject ----
        ButtonGroup buttonGroupZProject = new ButtonGroup();
        buttonGroupZProject.add(radioButtonMenuItem1);
        buttonGroupZProject.add(radioButtonMenuItem2);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JTabbedPane tabbedPane1;
    private JPanel panel4;
    private JSlider slider1;
    private JPanel panel1;
    private JMenuBar menuBar1;
    private JMenu menu3;
    private JMenu menu4;
    private JMenuItem menuItem7;
    private JMenuItem menuItem8;
    private JMenuItem menuItem9;
    private JMenuItem menuItem10;
    private JMenu menu5;
    private JRadioButtonMenuItem radioButtonMenuItem1;
    private JRadioButtonMenuItem radioButtonMenuItem2;
    private JPanel hSpacer1;
    private JLabel label5;
    private JTextField textField1;
    private JLabel label1;
    private JButton button3;
    private JLabel label2;
    private JPanel panel3;
    private JScrollPane scrollPane4;
    private JPanel panel5;
    private JPanel panel8;
    private JLabel label3;
    private JPanel hSpacer2;
    private JPanel panel6;
    private JButton button1;
    private JScrollPane scrollPane2;
    private JTable table1;
    private JPanel panel9;
    private JLabel label4;
    private JPanel hSpacer3;
    private JPanel panel7;
    private JButton button2;
    private JScrollPane scrollPane3;
    private JTable table2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
