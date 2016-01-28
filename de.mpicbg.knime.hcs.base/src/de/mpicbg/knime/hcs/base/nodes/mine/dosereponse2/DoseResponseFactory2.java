package de.mpicbg.knime.hcs.base.nodes.mine.dosereponse2;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.scripting.core.ScriptingModelConfig;
import de.mpicbg.knime.scripting.core.rgg.TemplateUtils;
import de.mpicbg.knime.scripting.r.RColumnSupport;
import de.mpicbg.knime.scripting.r.RSnippetNodeModel;
import de.mpicbg.knime.scripting.r.RUtils;
import de.mpicbg.knime.scripting.r.generic.GenericRPlotNodeModel;
import de.mpicbg.knime.scripting.r.generic.RPortObject;
import de.mpicbg.knime.scripting.r.generic.RPortObjectSpec;
import de.mpicbg.knime.scripting.r.plots.RPlotCanvas;
import de.mpicbg.knime.scripting.r.rgg.HardwiredGenericRPlotNodeFactory;

/**
 * Node Factory for 'Dose Response' node
 * 
 * @author Antje Janosch
 *
 */
public class DoseResponseFactory2 extends HardwiredGenericRPlotNodeFactory {
	
	private static final ImagePortObjectSpec IM_PORT_SPEC = new ImagePortObjectSpec(PNGImageContent.TYPE);
	
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
	protected GenericRPlotNodeModel createNodeModelInternal() {
		
		return new GenericRPlotNodeModel(nodeModelConfig) {

            protected PortObject[] prepareOutput(ExecutionContext exec, RConnection connection) {
                try {
                    // prepare the output model  (table as well as model port)
                    REXP resultTable = connection.eval("ictable");
                    BufferedDataTable dataTable = RUtils.convert2DataTable(exec, resultTable, null);

                    File rWorkspaceFile = File.createTempFile("genericR", "R");
                    RUtils.saveToLocalFile(rWorkspaceFile, connection, RUtils.getHost(), RSnippetNodeModel.R_OUTVAR_BASE_NAME);
                    
                    // Rerun the image
                    PNGImageContent content;
                    File m_imageFile = File.createTempFile("RImage", ".png");
                    ImageIO.write(RPlotCanvas.toBufferedImage(image), "png", m_imageFile);
                    FileInputStream in = new FileInputStream(m_imageFile);
                    content = new PNGImageContent(in);
                    in.close();

                    PortObject imgPort = new ImagePortObject(content, IM_PORT_SPEC);

                    return new PortObject[]{dataTable, imgPort, new RPortObject(rWorkspaceFile)};

                } catch (Throwable e) {
                    throw new RuntimeException("Could not save rmodel: " + e);
                }
            }


            @Override
            protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

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
