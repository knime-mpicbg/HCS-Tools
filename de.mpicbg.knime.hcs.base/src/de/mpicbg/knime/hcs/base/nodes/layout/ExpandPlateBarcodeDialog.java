/**
 * 
 */
package de.mpicbg.knime.hcs.base.nodes.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.util.ColumnSelectionPanel;

import de.mpicbg.knime.hcs.core.barcodes.namedregexp.PatternRenderer;

/**
 * @author Antje Janosch
 *
 */
public class ExpandPlateBarcodeDialog extends NodeDialogPane {
	
	// GUI COMPONENTS
	
	/**
	 * checkbox to enable/disable pattern selection
	 */
	private JCheckBox m_selectAutoGuess;
	
	/**
	 * panel to select barcode column
	 */
	private final ColumnSelectionPanel m_columnPanel;
	
	/**
	 * list model for {@link #m_patternListbox}
	 */
	private DefaultListModel<String> m_patternListboxModel;
	/**
	 * list component for barcode patterns
	 */
	private JList<String> m_patternListbox;
	
	// FURTHER MEMBERS
	
	/**
	 * list of barcode patterns
	 */
	List<String> m_patternList;
	
	/**
	 * settings model to store selected barcode pattern and if auto-guess is enabled or disabled
	 */
	private SettingsModelOptionalString m_barcodePatternSM = ExpandPlateBarcodeModel.createBarcodePatternSM();
	
	/**
	 * constructor, initializes dialog components
	 */
	@SuppressWarnings("unchecked")
	public ExpandPlateBarcodeDialog() {
		super();
		
		JPanel panel = new JPanel(new BorderLayout());
		
		m_selectAutoGuess = new JCheckBox("Guess pattern");
		m_selectAutoGuess.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				m_patternListbox.setEnabled(!m_selectAutoGuess.isSelected());
			}
			
		});
		
		panel.add(m_selectAutoGuess, BorderLayout.NORTH);
              
        // Barcode pattern selection
        m_patternListboxModel = new DefaultListModel<String>();
        m_patternListbox = new JList<String>(m_patternListboxModel);
        m_patternListbox.setCellRenderer(new PatternRenderer());
        m_patternListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //m_patternList.setPreferredSize(this.getPanel().getPreferredSize());
        
        JScrollPane scrollPane = new JScrollPane(m_patternListbox);
        scrollPane.setPreferredSize(this.getPanel().getPreferredSize());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
		// Barcode column selection
		m_columnPanel = new ColumnSelectionPanel("Barcode Column", StringValue.class);
        m_columnPanel.setRequired(true);
        
        panel.add(m_columnPanel,BorderLayout.SOUTH);
        
        this.addTab("Settings", panel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		//get barcode column
		String barcodeColumn = settings.getString(ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN, ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN_DFT);
		m_columnPanel.setSelectedColumn(barcodeColumn);
		m_columnPanel.update(specs[0], barcodeColumn);
		
		// barcode pattern
		String barcodePattern = null;
		boolean guessPattern = true;
		if(settings.containsKey(ExpandPlateBarcodeModel.CFG_REGEX)) {
			try {
				m_barcodePatternSM.loadSettingsFrom(settings.getNodeSettings(ExpandPlateBarcodeModel.CFG_REGEX));
			} catch (InvalidSettingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			barcodePattern = m_barcodePatternSM.getStringValue();
			guessPattern = !m_barcodePatternSM.isEnabled();
			
		}
		
		m_selectAutoGuess.setSelected(guessPattern);
		
		m_patternList = ExpandPlateBarcodeModel.getPreferencePatterns();
		((PatternRenderer)m_patternListbox.getCellRenderer()).setPreferencePatterns(new ArrayList<String>(m_patternList));
		// if no pattern is given by the settings, the first preference pattern is used as default
		if(barcodePattern == null) barcodePattern = m_patternList.get(0);
		// if the pattern is given by the settings but not part of the preferences, mark as invalid (= orange color)
		if(!m_patternList.contains(barcodePattern))
			m_patternList.add(barcodePattern);

		
		// update control with pattern list
		m_patternListboxModel.removeAllElements();
		for(String p : m_patternList) {
			m_patternListboxModel.addElement(p);
		}
		if(barcodePattern != null) m_patternListbox.setSelectedValue(barcodePattern, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		
		settings.addString(ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN, m_columnPanel.getSelectedColumn());
		
		m_barcodePatternSM.setEnabled(!m_selectAutoGuess.isSelected());
		m_barcodePatternSM.setStringValue(m_patternListbox.getSelectedValue());
		NodeSettings patternSettings = new NodeSettings(ExpandPlateBarcodeModel.CFG_REGEX);
		m_barcodePatternSM.saveSettingsTo(patternSettings);
		settings.addNodeSettings(patternSettings);
	}

}
