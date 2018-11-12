package de.mpicbg.knime.hcs.base.nodes.mine.binningapply;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;

import de.mpicbg.knime.hcs.base.node.port.binning.BinningPortObject;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * This is the model implementation of BinningApply.
 * 
 * @author Antje Janosch
 *
 */
public class BinningApplyNodeModel extends AbstractNodeModel {
	
	public static final String CFG_GROUPS = "group.by";

	/**
	 * constructor
	 */
	protected BinningApplyNodeModel() 
	{
		super(new PortType[]{BufferedDataTable.TYPE, BinningPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE}, true);

		initializeSettings();
	}

	private void initializeSettings() {
		this.addModelSetting(CFG_GROUPS, (SettingsModel)createGroupFilterModel());
		
	}

	public static SettingsModelColumnFilter2 createGroupFilterModel() {
		return new SettingsModelColumnFilter2(CFG_GROUPS);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		return super.configure(inSpecs);
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inObjects, exec);
	}
	
	
}
