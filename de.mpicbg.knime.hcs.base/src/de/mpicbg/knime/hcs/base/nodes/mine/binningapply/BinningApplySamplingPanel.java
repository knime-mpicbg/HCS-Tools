package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

@SuppressWarnings("serial")
public class BinningApplySamplingPanel extends JPanel {
	
	JCheckBox comp_useSampling = new JCheckBox("Enable sampling");
	JCheckBox comp_useSeed = new JCheckBox("Use random seed");
	JSpinner comp_sampleSize = new JSpinner();
	JSpinner comp_seed = new JSpinner();
	
	public BinningApplySamplingPanel() {
		
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		comp_sampleSize.setEnabled(false);
		comp_useSeed.setEnabled(false);
		comp_seed.setEnabled(false);
		
		Integer val = new Integer(0);
		Integer min = new Integer(0);
		Integer max = new Integer(Integer.MAX_VALUE);
		Integer stepSize = new Integer(1);
		SpinnerNumberModel model = new SpinnerNumberModel(val, min, max, stepSize);
		comp_sampleSize.setModel(model);
		
		Long lmin = new Long(0);
		Long lmax = Long.MAX_VALUE;
		Long lval = new Long(0);
		Long lstepSize = new Long(1);
		model = new SpinnerNumberModel(lval, lmin, lmax, lstepSize);
		comp_seed.setModel(model);
		
		comp_useSampling.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getItem() == comp_useSampling) {
					boolean selected = comp_useSampling.isSelected();
					comp_sampleSize.setEnabled(selected);
					comp_useSeed.setEnabled(selected);
					comp_seed.setEnabled(selected && comp_useSeed.isSelected());
					}
				}		
		});
		
		comp_useSeed.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getItem() == comp_useSeed) {
					comp_seed.setEnabled(comp_useSeed.isSelected());
				}			
			}
		});
		
		c.gridx = 0;			//x-pos in grid
		c.gridy = 0;			//y-pos in grid
		c.gridwidth = 1;		// amount of cells in x-direction
		c.insets = new Insets(10, 10, 0, 10);	// some space to left, upper and right
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 20;		// ratio of column width (not exactly...)
		add(comp_useSampling, c);
		
		c.gridx++;
		c.weightx = 80;		
		add(comp_sampleSize, c);
		
		c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.insets = new Insets(0, 10, 0, 10);	// some space to left and right
        add(new JSeparator(), c);
        
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 1;		// use complete y-space for the cells of the last row
        add(comp_useSeed, c);
        
        c.gridx++;
        add(comp_seed, c);
	}		

	public void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec dataTableSpec) throws InvalidSettingsException {
		boolean sm_useSampling = settings.getBoolean(BinningApplyNodeModel.CFG_SAMPLING);
		boolean sm_useSeed = settings.getBoolean(BinningApplyNodeModel.CFG_SEED);
		
		long sm_seedValue =  settings.getLong(BinningApplyNodeModel.CFG_SEED_VALUE);
		int sm_sampleSize = settings.getInt(BinningApplyNodeModel.CFG_SAMPLE_SIZE);
		
		comp_useSampling.setSelected(sm_useSampling);
		comp_useSeed.setSelected(sm_useSeed);
		
		comp_sampleSize.getModel().setValue(sm_sampleSize);
		comp_seed.getModel().setValue(sm_seedValue);
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		boolean useSampling = comp_useSampling.isSelected();
		boolean useSeed = comp_useSeed.isSelected();
		
		settings.addBoolean(BinningApplyNodeModel.CFG_SAMPLING, useSampling);
		settings.addBoolean(BinningApplyNodeModel.CFG_SEED, useSeed);
		
		int sampleSize = ((Integer)comp_sampleSize.getModel().getValue()).intValue();
		long seedValue = ((Long) comp_seed.getModel().getValue()).longValue();
		
		settings.addInt(BinningApplyNodeModel.CFG_SAMPLE_SIZE, sampleSize);
		settings.addLong(BinningApplyNodeModel.CFG_SEED_VALUE, seedValue);
		
	}

}
