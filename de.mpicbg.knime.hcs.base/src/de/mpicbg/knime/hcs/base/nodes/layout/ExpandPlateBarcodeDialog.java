/**
 * 
 */
package de.mpicbg.knime.hcs.base.nodes.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionPanel;

/**
 * @author Antje Janosch
 *
 */
public class ExpandPlateBarcodeDialog extends NodeDialogPane {
	
	private final ColumnSelectionPanel m_columnPanel;
	
	private DefaultComboBoxModel<String> m_patternControl;
	private JComboBox<String> m_patternBox;
	
	@SuppressWarnings("unchecked")
	public ExpandPlateBarcodeDialog() {
		super();
		
		JPanel panel = new JPanel(new BorderLayout());
		
		// Barcode column selection
		m_columnPanel = new ColumnSelectionPanel(StringValue.class);
        m_columnPanel.setRequired(true);
              
        // Barcode pattern selection
        m_patternControl = new DefaultComboBoxModel<String>();
        m_patternBox = new JComboBox<String>(m_patternControl);
        
        panel.add(m_columnPanel, BorderLayout.CENTER);
        panel.add(m_patternBox);
        this.addTab("Settings", panel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		//get barcode column
		String barcodeColumn = settings.getString(ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN, ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN_DFT);
		m_columnPanel.setSelectedColumn(barcodeColumn);
		m_columnPanel.update(specs[0], barcodeColumn);
		
		// barcode pattern
		String barcodePattern = settings.getString(ExpandPlateBarcodeModel.CFG_REGEX, null);		
		List<String> patternList = ExpandPlateBarcodeModel.getPreferencePatterns();
		boolean invalidPattern = false;
		if(!patternList.contains(barcodePattern)) {
			patternList.add(barcodePattern);
			invalidPattern = true;
		}
		
		m_patternControl.removeAllElements();
		for(String p : patternList) {
			m_patternControl.addElement(p);
		}
		if(barcodePattern != null) m_patternControl.setSelectedItem(barcodePattern);
		if(invalidPattern) m_patternBox.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeDialogPane#saveSettingsTo(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		settings.addString(ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN, m_columnPanel.getSelectedColumn());
		settings.addString(ExpandPlateBarcodeModel.CFG_REGEX, (String)m_patternControl.getSelectedItem());
	}

}
