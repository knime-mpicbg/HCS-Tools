package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

public class BinningApplyNodeDialog2 extends NodeDialogPane {
	
	private final BinningApplyGroupingPanel m_groupingPanel;
	private final BinningApplySamplingPanel m_samplingPanel;
	
	/**
     * Constructor that inits the GUI.
     */
    public BinningApplyNodeDialog2() {
        super();
        m_samplingPanel = new BinningApplySamplingPanel();
        m_groupingPanel = new BinningApplyGroupingPanel();
        
        super.addTab("Grouping", m_groupingPanel);
        super.addTab("Sampling Method", m_samplingPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        try {
			m_groupingPanel.loadSettingsFrom(settings, (DataTableSpec)specs[0]);
			m_samplingPanel.loadSettingsFrom(settings, (DataTableSpec)specs[0]);
		} catch (InvalidSettingsException e) {
			// method should not throw an exception
			e.printStackTrace();
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        m_groupingPanel.saveSettingsTo(settings);
        m_samplingPanel.saveSettingsTo(settings);
    }

}
