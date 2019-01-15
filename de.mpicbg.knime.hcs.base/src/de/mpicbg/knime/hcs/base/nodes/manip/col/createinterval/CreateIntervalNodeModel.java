package de.mpicbg.knime.hcs.base.nodes.manip.col.createinterval;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * node model class for Create Interval node
 * 
 * @author Antje Janosch
 *
 */
public class CreateIntervalNodeModel extends AbstractNodeModel {

	public static final String CFG_KEY = "create.interval.settings";
	
	public CreateIntervalNodeModel() {	
		super(1,1,true);
		this.addModelSetting(CFG_KEY, new CreateIntervalNodeSettings(CFG_KEY));
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		return super.configure(inSpecs);
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// TODO Auto-generated method stub
		return super.execute(inData, exec);
	}
	
	
}
