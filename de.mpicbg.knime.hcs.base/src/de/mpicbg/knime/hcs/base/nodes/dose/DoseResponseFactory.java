package de.mpicbg.knime.hcs.base.nodes.dose;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.scripting.core.ScriptingModelConfig;
import de.mpicbg.knime.scripting.core.rgg.TemplateUtils;
import de.mpicbg.knime.scripting.r.RColumnSupport;
import de.mpicbg.knime.scripting.r.RUtils;
import de.mpicbg.knime.scripting.r.generic.GenericRPlotNodeModel;
import de.mpicbg.knime.scripting.r.generic.RPortObject;
import de.mpicbg.knime.scripting.r.generic.RPortObjectSpec;
import de.mpicbg.knime.scripting.r.node.snippet.RSnippetNodeModel;
import de.mpicbg.knime.scripting.r.oldhardwired.HardwiredGenericRPlotNodeFactory;


/**
 * The IC50 is a measure of the effectiveness of a compound in inhibiting biological or biochemical function
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class DoseResponseFactory extends HardwiredGenericRPlotNodeFactory {
	
	private static ScriptingModelConfig nodeModelConfig = new ScriptingModelConfig(
			AbstractNodeModel.createPorts(1), 	// 1 table input
			createOutputPorts(), 				// 1 table, 1 R port 
			new RColumnSupport(), 	
			true, 					// no script
			false, 					// open in functionality
			true);					// use chunk settings

    public GenericRPlotNodeModel createNodeModelInternal() {
        return new GenericRPlotNodeModel(nodeModelConfig) {

            protected PortObject[] prepareOutput(ExecutionContext exec, RConnection connection) {
                try {
                    // prepare the output model  (table as well as model port)
                    REXP resultTable = connection.eval("ictable");
                    BufferedDataTable dataTable = RUtils.convert2DataTable(exec, resultTable, null);

                    File rWorkspaceFile = File.createTempFile("genericR", "R");
                    RUtils.saveToLocalFile(rWorkspaceFile, connection, RUtils.getHost(), RSnippetNodeModel.R_OUTVAR_BASE_NAME);

                    return new PortObject[]{dataTable, new RPortObject(rWorkspaceFile)};

                } catch (Throwable e) {
                    throw new RuntimeException("Could not save rmodel: " + e);
                }
            }


            @Override
            protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
                List<Attribute> outputSpecs = new ArrayList<Attribute>();

                // get the parameter value from the ui
                Map<String, Object> getTemplateConfig = getTemplateConfig(inSpecs);

                Object o = getTemplateConfig.get("Compound");
                String compoundName = o != null ? o.toString() : "readout";

                outputSpecs.add(new Attribute(compoundName, StringCell.TYPE));
                outputSpecs.add(new Attribute("Response Variable", StringCell.TYPE));
                outputSpecs.add(new Attribute("IC 50", DoubleCell.TYPE));
                outputSpecs.add(new Attribute("Std. Error", DoubleCell.TYPE));
                outputSpecs.add(new Attribute("Residual standard error", DoubleCell.TYPE));
                outputSpecs.add(new Attribute("DoF", DoubleCell.TYPE));

                DataTableSpec tableSpec = AttributeUtils.compileTableSpecs(outputSpecs);
                return new PortObjectSpec[]{tableSpec, RPortObjectSpec.INSTANCE};
            }


            @Override
            public String prepareScript() {
                String utilsFuns = TemplateUtils.readResourceAsString(this, "drcutils.R");

                return utilsFuns + super.prepareScript();
            }
        };
    }


    public String getTemplateFileName() {
        return "DoseResponse.rgg";
    }


    private static PortType[] createOutputPorts() {
        PortType[] tablePort = AbstractNodeModel.createPorts(1);
        PortType[] modelPort = AbstractNodeModel.createPorts(1, RPortObject.TYPE, RPortObject.class);

        return new PortType[]{tablePort[0], modelPort[0]};
    }

}


