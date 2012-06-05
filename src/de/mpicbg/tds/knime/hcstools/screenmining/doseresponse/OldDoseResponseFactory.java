package de.mpicbg.tds.knime.hcstools.screenmining.doseresponse;

import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.scripting.r.RUtils;
import de.mpicbg.tds.knime.scripting.r.genericr.GenericRPlotNodeModel;
import de.mpicbg.tds.knime.scripting.r.genericr.RPortObject;
import de.mpicbg.tds.knime.scripting.r.genericr.RPortObjectSpec;
import de.mpicbg.tds.knime.scripting.r.templatenodes.rgg.HardwiredGenericRPlotNodeFactory;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The IC50 is a measure of the effectiveness of a compound in inhibiting biological or biochemical function
 *
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class OldDoseResponseFactory extends HardwiredGenericRPlotNodeFactory {

    public GenericRPlotNodeModel createNodeModelInternal() {
        return new GenericRPlotNodeModel(AbstractNodeModel.createPorts(1), createOutputPorts()) {

            protected PortObject[] prepareOutput(ExecutionContext exec, RConnection connection) {
                try {
                    // prepare the output model  (table as well as model port)
                    REXP resultTable = connection.eval("ictable");
                    BufferedDataTable dataTable = RUtils.convert2DataTable(exec, resultTable, null);

                    File rWorkspaceFile = File.createTempFile("genericR", "R");
                    RUtils.saveToLocalFile(rWorkspaceFile, connection, RUtils.getHost(), "R");

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
                String readoutName = o != null ? o.toString() : "readout";

                outputSpecs.add(new Attribute(readoutName, StringCell.TYPE));
                outputSpecs.add(new Attribute("IC 50", DoubleCell.TYPE));
                outputSpecs.add(new Attribute("Std. Error", DoubleCell.TYPE));

                DataTableSpec tableSpec = AttributeUtils.compileTableSpecs(outputSpecs);
                return new PortObjectSpec[]{tableSpec, RPortObjectSpec.INSTANCE};
            }
        };
    }


    public String getTemplateFileName() {
        return "OldDoseResponse.rgg";
    }


    private static PortType[] createOutputPorts() {
        PortType[] tablePort = AbstractNodeModel.createPorts(1);
        PortType[] modelPort = AbstractNodeModel.createPorts(1, RPortObject.TYPE, RPortObject.class);

        return new PortType[]{tablePort[0], modelPort[0]};
    }

}


