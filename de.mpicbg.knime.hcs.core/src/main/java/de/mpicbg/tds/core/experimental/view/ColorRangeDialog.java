/*
 * Created by JFormDesigner on Fri Oct 07 20:21:53 CEST 2011
 */

package de.mpicbg.tds.core.experimental.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
//import com.jgoodies.forms.factories.*;

/**
 * @author User #1
 */
public class ColorRangeDialog extends JDialog {
    public ColorRangeDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public ColorRangeDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        radioButton1 = new JRadioButton();
        textField1 = new JTextField();
        panel2 = new JPanel();
        radioButton2 = new JRadioButton();
        comboBox1 = new JComboBox();
        textField2 = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

                //======== panel1 ========
                {
                    panel1.setBorder(new TitledBorder("Fixed Color"));
                    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
                    panel1.add(radioButton1);

                    //---- textField1 ----
                    textField1.setBackground(new Color(255, 93, 255));
                    textField1.setEditable(false);
                    panel1.add(textField1);
                }
                contentPanel.add(panel1);

                //======== panel2 ========
                {
                    panel2.setBorder(new TitledBorder("LUT"));
                    panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
                    panel2.add(radioButton2);
                    panel2.add(comboBox1);

                    //---- textField2 ----
                    textField2.setEditable(false);
                    textField2.setBackground(new Color(141, 205, 140));
                    panel2.add(textField2);
                }
                contentPanel.add(panel2);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText("OK");
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroupColorRange ----
        ButtonGroup buttonGroupColorRange = new ButtonGroup();
        buttonGroupColorRange.add(radioButton1);
        buttonGroupColorRange.add(radioButton2);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JRadioButton radioButton1;
    private JTextField textField1;
    private JPanel panel2;
    private JRadioButton radioButton2;
    private JComboBox comboBox1;
    private JTextField textField2;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
