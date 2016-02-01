package de.mpicbg.knime.hcs.base.nodes.mine.dosereponse2;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.scripting.core.ScriptingModelConfig;
import de.mpicbg.knime.scripting.core.rgg.TemplateUtils;
import de.mpicbg.knime.scripting.r.RColumnSupport;
import de.mpicbg.knime.scripting.r.node.generic.plot.GenericRPlotNodeModel2;
import de.mpicbg.knime.scripting.r.node.hardwired.HardwiredGenericRPlotNodeFactory2;
import de.mpicbg.knime.scripting.r.port.RPortObject;
import de.mpicbg.knime.scripting.r.port.RPortObjectSpec;

/**
 * Node Factory for 'Dose Response' node
 * 
 * @author Antje Janosch
 *
 */
public class DoseResponseFactory2 extends HardwiredGenericRPlotNodeFactory2 {
	
	private static ScriptingModelConfig nodeModelConfig = new ScriptingModelConfig(
			AbstractNodeModel.createPorts(1), 	// 1 table input
			createOutputPorts(), 				// 1 table, 1 R port 
			new RColumnSupport(), 	
			true, 					// no script
			false, 					// open in functionality
			true);					// use chunk settings

	/**
	 * {@inheritDoc}
	 */
	public String getTemplateFileName() {
        return "DoseResponse.rgg";
    }

	/**
	 * creates an array of output port types for that node
	 * @return
	 */
    private static PortType[] createOutputPorts() {
        PortType[] tablePort = AbstractNodeModel.createPorts(1);
        PortType[] modelPort = AbstractNodeModel.createPorts(1, RPortObject.TYPE, RPortObject.class);

        return new PortType[]{tablePort[0], ImagePortObject.TYPE, modelPort[0]};
    }

	@Override
	protected GenericRPlotNodeModel2 createNodeModelInternal() {
		
		return new GenericRPlotNodeModel2(nodeModelConfig) {

            @Override
            protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
            	
            	super.configure(inSpecs);

                // get the parameter value from the ui
                Map<String, Object> getTemplateConfig = getTemplateConfig(inSpecs);

                Object o = getTemplateConfig.get("Compound");
                String compoundName = o != null ? o.toString() : "readout";
                
                LinkedHashMap<String, DataType> newColumns = new LinkedHashMap<String, DataType>();
                newColumns.put(compoundName, StringCell.TYPE);
                newColumns.put("Response Variable", StringCell.TYPE);
                newColumns.put("IC 50", DoubleCell.TYPE);
                newColumns.put("Std. Error (IC 50)", DoubleCell.TYPE);
                newColumns.put("Lower Limit", DoubleCell.TYPE);
                newColumns.put("Std. Error (Lower Limit)", DoubleCell.TYPE);
                newColumns.put("Upper Limit", DoubleCell.TYPE);
                newColumns.put("Std. Error (Upper Limit)", DoubleCell.TYPE);
                newColumns.put("Slope", DoubleCell.TYPE);
                newColumns.put("Std. Error (Slope)", DoubleCell.TYPE);
                newColumns.put("Symmetry", DoubleCell.TYPE);
                newColumns.put("Std. Error (Symmetry)", DoubleCell.TYPE);
                newColumns.put("Residual standard error", DoubleCell.TYPE);
                newColumns.put("DoF", IntCell.TYPE);
                
                return new PortObjectSpec[]{BufTableUtils.createNewDataTableSpec(newColumns), IM_PORT_SPEC, RPortObjectSpec.INSTANCE};
            }


            @Override
            public String prepareScript() {
                String utilsFuns = TemplateUtils.readResourceAsString(this, "drcutils.R");
                return utilsFuns + super.prepareScript();
            }
        };
	}

}
