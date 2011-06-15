package de.mpicbg.tds.knime.hcstools.scriptlets;

import nu.xom.*;
import org.knime.core.data.*;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class MiscScriptlets {


    public static BufferedDataTable parseMeaFile(ExecutionContext exec, File meaFile) throws CanceledExecutionException, SQLException, ParsingException, IOException {

        Builder parser = new Builder();
        Document doc = parser.build(meaFile);
        Nodes picNodes = doc.query("/Measurement/Pictures/Host/Picture");
        List<String> attrNames = getAttributeNames((Element) picNodes.get(0));


        // define the column specs
        DataColumnSpec[] allColSpecs = new DataColumnSpec[attrNames.size()];

        for (int i = 0; i < allColSpecs.length; i++) {
            allColSpecs[i] = new DataColumnSpecCreator(attrNames.get(i), StringCell.TYPE).createSpec();
        }

        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        for (int i = 0; i < picNodes.size(); i++) {
            Element element = (Element) picNodes.get(i);


            DataCell[] cells = new DataCell[attrNames.size()];

            for (int j = 0; j < element.getAttributeCount(); j++) {
//                String attrName = element.getAttribute(j).getLocalName();
                String attrValue = element.getAttribute(j).getValue();
                cells[j] = new StringCell(attrValue);
            }

            DataRow row = new DefaultRow(new RowKey("Row " + i), cells);
            container.addRowToTable(row);
        }

        container.close();
        return container.getTable();
    }


    public static void main(String[] args) throws ParsingException, IOException {
        File meaFile = new File("/Volumes/operadb/Projects/Endocytosis_GWS/2010_07/002GW100706B-KPIHgws/Meas_01/Meas_01(2010-07-30_16-42-06).mea");


        Builder parser = new Builder();
        Document doc = parser.build(meaFile);
//            System.out.println(doc.toXML());

        Element rootElement = doc.getRootElement();
        rootElement.getAttribute("Pictures");


        Nodes picNodes = doc.query("/Measurement/Pictures/Host/Picture");
        for (int i = 0; i < picNodes.size(); i++) {
            Element element = (Element) picNodes.get(i);


            for (int j = 0; j < element.getAttributeCount(); j++) {
                String attrName = element.getAttribute(j).getLocalName();
                String attrValue = element.getAttribute(j).getValue();

            }
        }
        System.err.println("" + picNodes);

    }


    private static List<String> getAttributeNames(Element element) {
        List<String> names = new ArrayList<String>();

        for (int j = 0; j < element.getAttributeCount(); j++) {
            String attrName = element.getAttribute(j).getLocalName();
            names.add(attrName);
        }

        return names;
    }
}
