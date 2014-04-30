package de.mpicbg.knime.hcs.base;

import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.tds.core.util.PlateComparator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import java.util.Arrays;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class TestHCSScripting {

    public static void main(String[] args) {


    }


    public void devScript0(ExecutionContext exec) {
        List<Attribute> attributes = Arrays.asList(new Attribute("test1", StringCell.TYPE));

        DataTableSpec dataTableSpec = AttributeUtils.compileTableSpecs(attributes);
        BufferedDataContainer container = exec.createDataContainer(dataTableSpec);


        for (int rowCounter = 1; rowCounter < 100; rowCounter++) {

            DataCell[] knimeRow = new DataCell[attributes.size()];
            knimeRow[0] = attributes.get(0).createCell("test" + (rowCounter * 2));

            DataRow tableRow = new DefaultRow(new RowKey(rowCounter + ""), knimeRow);
            container.addRowToTable(tableRow);
        }

        new PlateComparator();

//        container.close();

    }


    public void devScript1(ExecutionContext exec, BufferedDataTable in, List<Attribute> attributes) {

        // add a column

    }


    public void devScript2(ExecutionContext exec, BufferedDataTable in, List<Attribute> attributes, BufferedDataTable in2, List<Attribute> attributes2) {

        // add a column

    }

}