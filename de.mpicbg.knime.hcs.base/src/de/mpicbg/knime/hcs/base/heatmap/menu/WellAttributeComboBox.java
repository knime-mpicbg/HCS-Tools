package de.mpicbg.knime.hcs.base.heatmap.menu;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.HeatMapModelChangeListener;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * Combobox to select the well attributes (factors and readouts)
 *
 * @author Felix Meyenhofer
 *         12/10/12
 */
public class WellAttributeComboBox extends JComboBox implements HeatMapModelChangeListener {

    /** Data model */
    private HeatMapModel heatMapModel;
    
    /** either READOUT or OVERLAY_ANNOTATION */
    private AttributeType selectionType;


    /**
     * Configure the UI components
     *
     * @param options List of attribute values
     * @param heatMapModel data model delivering the data
     * @param selType type of attribute (factor or readout)
     */
    public void configure(List<String> options, final HeatMapModel heatMapModel, final AttributeType selType) {
        this.heatMapModel = heatMapModel;
        this.heatMapModel.addChangeListener(this);
        this.selectionType = selType;

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
                if (heatMapModel.getCurrentOverlay() != null) {
                    setSelectedItem(heatMapModel.getCurrentOverlay());
                } else {
                    heatMapModel.setCurrentOverlay((String) getSelectedItem());
                }
                break;
        }
        
        //listen to selection changes
        addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent event) {
               if (event.getStateChange() == ItemEvent.SELECTED) {
            	   HeatMapModel mapModel = WellAttributeComboBox.this.heatMapModel;
            	   
            	// apply the changed selection
                   switch (selType) {
                       case READOUT:
                           mapModel.setCurrentReadout((String) getModel().getSelectedItem());
                           break;
                       case OVERLAY_ANNOTATION:
                           mapModel.setCurrentOverlay((String) getModel().getSelectedItem());
                           break;
                   }

                   heatMapModel.fireModelChanged();
               }
        	}
        });

        //http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm
        setRenderer(new MyComboBoxRenderer());
    }


    /**
     * Renderer class
     */
    class MyComboBoxRenderer extends BasicComboBoxRenderer {

        /** {@inheritDoc} */
        @Override
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


	@Override
	public void modelChanged() {
		switch (selectionType) {
	        case READOUT:
	            if (heatMapModel.getSelectedReadOut() != null) {
	                setSelectedItem(heatMapModel.getSelectedReadOut());
	            } else {
	                heatMapModel.setCurrentReadout((String) getSelectedItem());
	            }
	            break;
	        case OVERLAY_ANNOTATION:
	            if (heatMapModel.getCurrentOverlay() != null) {
	                setSelectedItem(heatMapModel.getCurrentOverlay());
	            } else {
	                heatMapModel.setCurrentOverlay((String) getSelectedItem());
	            }
	            break;
	    }		
	}
}


/**
 * The two allowed attribute types
 */
enum AttributeType {

    READOUT, OVERLAY_ANNOTATION
}
