package de.mpicbg.knime.hcs.base.nodes.layout;

import de.mpicbg.knime.hcs.base.HCSToolsBundleActivator;
import de.mpicbg.knime.hcs.base.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.tds.barcodes.BarcodeParser;
import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.knime.knutils.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.*;


public class ExpandPlateBarcode extends AbstractNodeModel {

    public SettingsModelString propBarcode = ExpandPlateBarcodeFactory.createPropBarcode();


    protected ExpandPlateBarcode() {
        addSetting(propBarcode);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];


        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        Attribute barcodeAttribute = new InputTableAttribute(propBarcode.getStringValue(), input);


        // 2) create the barcode-parser-factory given the patterns from the preferences
        BarcodeParserFactory bpf = loadFactory();


        // 3) create the additional attributes
        List<String> someBarcodes = collectBarcodes(input, barcodeAttribute);
        Map<String, Attribute> groupMapping = createGroupAttributeMapping(someBarcodes);

        // log which barcode are invalid and plot just one barcode for invalid barcode
        Map<String, Integer> invalidBarcodes = new HashMap<String, Integer>();

        // 4) go through the table again and expand barcodes as possible
        for (DataRow dataRow : input) {
            String barcode = barcodeAttribute.getNominalAttribute(dataRow);

            BarcodeParser barcodeParser = bpf.getParser(barcode);

            // if it was not possible to create a parser update the error log
            if (barcodeParser == null) {
                if (!invalidBarcodes.containsKey(barcode)) {
                    invalidBarcodes.put(barcode, 0);
                }

                // increment the number of how many times the problem occured
                invalidBarcodes.put(barcode, 1 + invalidBarcodes.get(barcode));
            }

            // try to extract all attributes from the given barcode
            for (String groupName : groupMapping.keySet()) {
                Attribute barcodeAttr = groupMapping.get(groupName);

                String barcodeInfo = barcodeParser != null ? barcodeParser.getGroup(groupName) : null;

                if (barcodeInfo == null) {
                    updateCache.add(dataRow, barcodeAttr, DataType.getMissingCell());
                } else {
                    updateCache.add(dataRow, barcodeAttr, barcodeAttr.createCell(barcodeInfo));
                }
            }
        }

        for (String barcode : invalidBarcodes.keySet()) {
            logger.error("The barcode '" + barcode + "' found in " + invalidBarcodes.get(barcode) + " rows is not compatible with the barcode-schemata defined under Preferences->Knime->HCS-Tools");
        }


        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(input, c, exec);

        return new BufferedDataTable[]{out};
    }


    private List<String> collectBarcodes(BufferedDataTable input, Attribute barcodeAttribute) {
        Set<String> someBarcodes = new HashSet<String>();

        int counter = 0;
        for (DataRow dataRow : input) {
            // just collect a subset of all barchodes
//            if (counter++ > 100) {
//                break;
//            }

            someBarcodes.add(barcodeAttribute.getNominalAttribute(dataRow));
        }

        return new ArrayList<String>(someBarcodes);
    }


    public static BarcodeParserFactory loadFactory() {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();

        List<String> patterns = Arrays.asList(prefStore.getString(HCSToolsPreferenceInitializer.BARCODE_PATTERNS).split(";"));
        return new BarcodeParserFactory(patterns);
    }


    private Map<String, Attribute> createGroupAttributeMapping(List<String> someBarcodes) {
        Set<String> presentGroups = new HashSet<String>();


        BarcodeParserFactory bpf = loadFactory();

        for (String barcode : someBarcodes) {
            BarcodeParser barcodeParser = bpf.getParser(barcode);
            if (barcodeParser != null) {
                presentGroups.addAll(barcodeParser.getAvailableGroups());
            }

        }

        // create attributes for each group
        Map<String, Attribute> groupMapping = new TreeMap<String, Attribute>();
        for (String presentGroup : presentGroups) {
            groupMapping.put(presentGroup, new Attribute(bpf.getVerboseName(presentGroup), getAttributeType(presentGroup)));
        }

        return groupMapping;
    }


    private DataType getAttributeType(String groupName) {
        Object groupType = BarcodeParser.groupTypes.get(groupName);

        if (groupType == null) {
            return StringCell.TYPE;
        } else if (groupType.equals(Integer.class)) {
            return IntCell.TYPE;
        } else if (groupType.equals(Double.class)) {
            return DoubleCell.TYPE;
        } else {
            return StringCell.TYPE;
        }
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inputSpecs = inSpecs[0];
        TableUpdateCache updateCache = new TableUpdateCache(inputSpecs);

        Attribute barcodeAttribute = new InputTableAttribute(propBarcode.getStringValue(), inputSpecs);


        Set<DataCell> domain = barcodeAttribute.getColumnSpec().getDomain().getValues();
        if (domain == null) {
            logger.warn("Could not determine the available barcode attributes because the domain (set of all possible values)\n" +
                    " of the barcode-column is missing.The node will in most cases still work as expected, but Knime will " +
                    "show a warning (red cross) about a the unexpected change of the DatSpec after the execution.\n" +
                    "To avoid this problem insert a 'Domain Calculator' before the 'Expand Barcode' and configure it to" +
                    " NOT 'Restrict the number of possible values'. ");
        } else {

            Map<String, Attribute> groupMapping = createGroupAttributeMapping(AttributeUtils.toStringList(domain));

            for (String groupName : groupMapping.keySet()) {
                updateCache.registerAttribute(groupMapping.get(groupName));
            }
        }

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }
}