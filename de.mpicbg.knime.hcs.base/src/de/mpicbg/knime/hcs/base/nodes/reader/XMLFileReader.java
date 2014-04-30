package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.knime.knutils.ui.FileSelectPanel;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the model implementation of GeniusPro-file reader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class XMLFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();
    public SettingsModelString propFileSuffix = XMLFileReaderFactory.createPropSuffix();
    public SettingsModelString propXPathQuery = XMLFileReaderFactory.createPropXPathQuery();


    public XMLFileReader() {
        super(0, 1);

        addSetting(propInputDir);

        addSetting(propFileSuffix);
        addSetting(propXPathQuery);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        List<File> xmlFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), propFileSuffix.getStringValue());

        List<Attribute> attributes = compileAttributes(xmlFiles);

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(attributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int fileCounter = 0;

        int rowCounter = 0;

        for (File inputFile : xmlFiles) {
            logger.info("reading file " + inputFile);

            Builder parser = new Builder();
            Document doc = parser.build(inputFile);
            Nodes resultNodes = doc.query(propXPathQuery.getStringValue());
            List<String> attrNames = getAttributeNames((Element) resultNodes.get(0));


            for (int i = 0; i < resultNodes.size(); i++) {
                Element element = (Element) resultNodes.get(i);


                DataCell[] cells = new DataCell[attributes.size()];
                cells[0] = new StringCell(inputFile.getPath());

                for (int j = 0; j < element.getAttributeCount(); j++) {
//                String attrName = element.getAttribute(j).getLocalName();
                    String attrValue = element.getAttribute(j).getValue();
                    cells[j + 1] = new StringCell(attrValue);
                }

                cells[cells.length - 1] = new StringCell(element.getValue());

                DataRow row = new DefaultRow(new RowKey("Row " + rowCounter++), cells);
                container.addRowToTable(row);
            }

            BufTableUtils.updateProgress(exec, fileCounter++, xmlFiles.size());
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};

    }


    private List<Attribute> compileAttributes(List<File> xmlFiles) {
        List<Attribute> attributes;

        try {
            attributes = new ArrayList<Attribute>();
            attributes.add(new Attribute("File", StringCell.TYPE));

            Builder parser = new Builder();
            Document doc = parser.build(xmlFiles.get(0));
            Nodes picNodes = doc.query(propXPathQuery.getStringValue());
            List<String> attrNames = getAttributeNames((Element) picNodes.get(0));


            for (String attributeName : attrNames) {
                attributes.add(new Attribute(attributeName, StringCell.TYPE));

            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return attributes;
    }


    private static List<String> getAttributeNames(Element element) {
        List<String> names = new ArrayList<String>();

        for (int j = 0; j < element.getAttributeCount(); j++) {
            String attrName = element.getAttribute(j).getLocalName();
            names.add(attrName);
        }

        names.add("Node Value");

        return names;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<File> xmlFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), propFileSuffix.getStringValue());
        if (xmlFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }


        List<Attribute> colAttributes = compileAttributes(xmlFiles);

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }
}
