package de.mpicbg.knime.hcs.base.heatmap.menu;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.mpicbg.knime.hcs.base.heatmap.HeatMapModel;
import de.mpicbg.knime.hcs.base.heatmap.HeatMapModelChangeListener;

/**
 * Combobox to select the well attributes (factors and readouts)
 *
 * @author Felix Meyenhofer
 *         12/10/12
 */
public class WellAttributeComboBox extends JComboBox<String> implements HeatMapModelChangeListener {

    /** Data model */
    private HeatMapModel heatMapModel;
    
    /** either READOUT or OVERLAY_ANNOTATION */
    private AttributeType selectionType;

    ItemListener m_listener = null;
    
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
        
        //modify overlay-list to allow KNIME colors and no selection
        String[] items = options.toArray(new String[options.size()]);
        if(selectionType == AttributeType.OVERLAY_ANNOTATION) {
        	items = addOverlayOptions(items);
        }
        
        
        DefaultComboBoxModel<String> readoutModel = new DefaultComboBoxModel<String>(items);
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
        
        m_listener = new ItemListener(){
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
         };
        
        //listen to selection changes
        addItemListener(m_listener);

        //http://www.java2s.com/Code/Java/Swing-Components/ToolTipComboBoxExample.htm
        setRenderer(new MyComboBoxRenderer());
    }
    
    /**
     * the overlay combobox needs to provide the column with knime-colors (if available) and
     * the possibility to not show any overlay
     * @param items
     * @return array with additional options to feed the combobox model
     */
	private String[] addOverlayOptions(String[] items) {
		List<String> itemList = new ArrayList<String>(Arrays.asList(items));
		if ( heatMapModel.hasKnimeColorModel() )
            itemList.add(0, heatMapModel.getKnimeColorAttributeTitle());
        itemList.add(0, "");
		return itemList.toArray(new String[(itemList.size())]);
	}

	/** {@inheritDoc} */
	@Override
	public void modelChanged() {
		DefaultComboBoxModel<String> currentModel = (DefaultComboBoxModel<String>) this.getModel();
		// as the change is due to heatmap model changes, the listener has to be turned off
    	// to avoid cycles of model-changed-events between heatmap model and combobox model
		this.removeItemListener(m_listener);
		currentModel.removeAllElements();
		
		switch (selectionType) {
	        case READOUT:
	        	// update combobox content	
	        	List<String> readouts = heatMapModel.getReadouts();	        	        	
	        	for(String ro : readouts)
	        		currentModel.addElement(ro);	        	
	        	
	        	// update selection
	            if (heatMapModel.getSelectedReadOut() != null) {
	                setSelectedItem(heatMapModel.getSelectedReadOut());
	            } else {
	                heatMapModel.setCurrentReadout((String) getSelectedItem());
	            }
	            break;
	        case OVERLAY_ANNOTATION:
	        	// update combobox content
	        	List<String> annotations = heatMapModel.getAnnotations();
	        	String[] items = annotations.toArray(new String[annotations.size()]);
	        	items = addOverlayOptions(items);
	        	for(String an : items)
	        		currentModel.addElement(an);
	        	
	            if (heatMapModel.getCurrentOverlay() != null) {
	                setSelectedItem(heatMapModel.getCurrentOverlay());
	            } else {
	                heatMapModel.setCurrentOverlay((String) getSelectedItem());
	            }
	            break;
	    }		
		this.addItemListener(m_listener);
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
}


/**
 * The two allowed attribute types
 */
enum AttributeType {

    READOUT, OVERLAY_ANNOTATION
}
