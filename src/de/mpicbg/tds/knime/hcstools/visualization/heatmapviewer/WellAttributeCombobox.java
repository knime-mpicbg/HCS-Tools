package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * User: Felix Meyenhofer
 * Date: 12/10/12
 * Time: 13:42
 */
public class WellAttributeComboBox extends JComboBox {
    private HeatMapModel2 heatMapModel;
    private JPanel parent;


    public void configure(List<String> options, final HeatMapModel2 heatMapModel, final AttributeType selType) {
        this.heatMapModel = heatMapModel;
        this.parent = parent;

        // populate the readout selector with readout-types of the given well-type
        //        Collections.sort(readoutNames);
        DefaultComboBoxModel readoutModel = new DefaultComboBoxModel(options.toArray());
        setModel(readoutModel);


        switch (selType) {
            case READOUT:
                if (heatMapModel.getSelectedReadOut() != null) {
                    setSelectedItem(heatMapModel.getSelectedReadOut());
                } else {
                    heatMapModel.setCurrentReadout((String) getSelectedItem());
                }
                break;
            case OVERLAY_ANNOTATION:
                if (heatMapModel.getOverlay() != null) {
                    setSelectedItem(heatMapModel.getOverlay());
                } else {
                    heatMapModel.setOverlay((String) getSelectedItem());
                }
                break;
        }


        // register for readout changes
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                HeatMapModel2 mapModel = WellAttributeComboBox.this.heatMapModel;

                // apply the changed selection
                switch (selType) {
                    case READOUT:
                        mapModel.setCurrentReadout((String) getModel().getSelectedItem());
                        break;
                    case OVERLAY_ANNOTATION:
                        mapModel.setOverlay((String) getModel().getSelectedItem());
                        break;
                }

                heatMapModel.fireModelChanged();
            }
        });

        //http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm
        setRenderer(new MyComboBoxRenderer());
    }


    class MyComboBoxRenderer extends BasicComboBoxRenderer {

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (-1 < index) {
                    String s = value.toString();
                    if (s.length() > 35)
                        list.setToolTipText(s);
                    else
                        list.setToolTipText("");

                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}


enum AttributeType {

    READOUT, OVERLAY_ANNOTATION
}
