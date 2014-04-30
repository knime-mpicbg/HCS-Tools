package de.mpicbg.knime.hcs.base.nodes.trans;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.scripting.r.RUtils;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 7/27/11
 * Time: 10:44 AM
 */

public class BoxCoxTransform extends AbstractNodeModel {

    public BoxCoxTransform() {
        super(1, 2);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        //DataTableSpec spec1 = super.configure(inSpecs)[0];
        DataColumnSpec col1 = new DataColumnSpecCreator("x", DataType.getType(DoubleCell.class)).createSpec();
        DataTableSpec spec1 = new DataTableSpec(new DataColumnSpec[]{col1});
        DataColumnSpec col2 = new DataColumnSpecCreator("a", DataType.getType(DoubleCell.class)).createSpec();
        DataTableSpec spec2 = new DataTableSpec(new DataColumnSpec[]{col2});
        return new DataTableSpec[]{spec1, spec2};    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        RConnection connection = RUtils.createConnection();

        REXP transformedData = connection.parseAndEval("data.frame(x = 1:10)");
        REXP metadata = connection.parseAndEval("data.frame(a = c(0.07,0.08))");

        Map<String, DataType> typeMapping = getColumnTypeMapping(inData[0]);

        BufferedDataTable transformTable = RUtils.convert2DataTable(exec, transformedData, typeMapping);
        BufferedDataTable metaTable = RUtils.convert2DataTable(exec, metadata, typeMapping);

        /*DataColumnSpec col1 = new DataColumnSpecCreator("a", DataType.getType(DoubleCell.class)).createSpec();
        DataTableSpec spec1 = new DataTableSpec(new DataColumnSpec[]{col1});
        BufferedDataContainer testcon = exec.createDataContainer(spec1);
        testcon.       */

        return new BufferedDataTable[]{transformTable, metaTable};  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static Map<String, DataType> getColumnTypeMapping(BufferedDataTable bufferedDataTable) {
        Iterator<DataColumnSpec> dataColumnSpecIterator = bufferedDataTable.getSpec().iterator();
        Map<String, DataType> typeMapping = new HashMap<String, DataType>();
        while (dataColumnSpecIterator.hasNext()) {
            DataColumnSpec dataColumnSpec = dataColumnSpecIterator.next();
            typeMapping.put(dataColumnSpec.getName(), dataColumnSpec.getType());
        }

        return typeMapping;
    }
}

