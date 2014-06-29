package de.mpicbg.knime.hcs.base.nodes.qc;

import de.mpicbg.knime.hcs.base.HCSSettingsFactory;
import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel;
import de.mpicbg.knime.hcs.base.utils.AttributeStatistics;
import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.knime.knutils.*;
import org.knime.core.data.*;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.*;

import static de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel.*;
import static de.mpicbg.knime.hcs.base.utils.AttributeStatistics.*;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class SSMD extends AbstractNodeModel {

    private SettingsModelString posControl = createTreatmentSelector(TREATMENT_POS_CONTROL);
    private SettingsModelString negControl = createTreatmentSelector(TREATMENT_NEG_CONTROL);
    private SettingsModelBoolean propRobustStats = createPropRobustStats();
    private SettingsModelFilterString propReadouts = createPropReadoutSelection();
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();
    public SettingsModelString groupBy = HCSSettingsFactory.createGroupBy();


    public SSMD() {
        addSetting(groupBy);

        addSetting(treatmentAttribute);
        addSetting(propReadouts);

        addControlSettings();

        addSetting(propRobustStats);
    }


    protected void addControlSettings() {
        addSetting(posControl);
        addSetting(negControl);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute plateAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        List<String> posCtrls = getPosControls();
        List<String> negCtrls = getNegControls();

        // make sure that both sets are disjunct
        HashSet<String> intersection = new HashSet<String>(posCtrls);
        intersection.retainAll(negCtrls);
        if (!intersection.isEmpty()) {
            throw new RuntimeException("Selected positive and negative control(s) are identical (not disjunct), which is unlikely to have meaningful semantics");
        }

        Boolean beRobust = propRobustStats.getBooleanValue();

        List<String> readouts = propReadouts.getIncludeList();


        Map<String, List<DataRow>> plates = AttributeUtils.splitRows(input, plateAttribute);


        // create a simple table with just one column which contains the barcodes of the plates
        BufferedDataTable barcodeTable = prepareZPrimeTable(exec, new ArrayList<String>(plates.keySet()));
        TableUpdateCache updateCache = new TableUpdateCache(barcodeTable.getDataTableSpec());

        Attribute barcodeAttr = new InputTableAttribute(groupBy.getStringValue(), barcodeTable);
        Attribute positiveAttr = new InputTableAttribute(POSITIVE_CONTROL_DESC, barcodeTable);
        Attribute negativeAttr = new InputTableAttribute(NEGATIVE_CONTROL_DESC, barcodeTable);

        int counter = 0;
        for (DataRow dataRow : barcodeTable) {
            String barcode = barcodeAttr.getNominalAttribute(dataRow);
            String positiveControl = positiveAttr.getNominalAttribute(dataRow);
            String negativeControl = negativeAttr.getNominalAttribute(dataRow);

            logger.info("Calculating ssmd for plate '" + barcode + "'");

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = plates.get(barcode);
            List<DataRow> posCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, positiveControl);
            List<DataRow> negCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, negativeControl);


            for (String readout : readouts) {
                Attribute readoutAttribute = new InputTableAttribute(readout, input);

                // calculate the z-prime factor for the current plate

                double posCtrlMean = beRobust ? median(posCtrlWells, readoutAttribute) : mean(posCtrlWells, readoutAttribute);
                double posCtrlSD = beRobust ? mad(posCtrlWells, readoutAttribute) : AttributeStatistics.stdDev(posCtrlWells, readoutAttribute);

                double negCtrlMean = beRobust ? median(negCtrlWells, readoutAttribute) : mean(negCtrlWells, readoutAttribute);
                double negCtrlSD = beRobust ? mad(negCtrlWells, readoutAttribute) : AttributeStatistics.stdDev(negCtrlWells, readoutAttribute);

                //todo in the original publication the denominator includes a covarance term, which vanishes for independent samples
                double ssmd = (posCtrlMean - negCtrlMean) / Math.sqrt(posCtrlSD * posCtrlSD + negCtrlSD * negCtrlSD);

                String ssmdAttributeName = readout;

                Attribute ssmdAttribute = new Attribute(ssmdAttributeName, DoubleCell.TYPE);
                updateCache.add(dataRow, ssmdAttribute, isValidNumber(ssmd) ? DataType.getMissingCell() : new DoubleCell(ssmd));
            }

            BufTableUtils.updateProgress(exec, counter++, plates.size());
        }


        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(barcodeTable, c, exec);

        return new BufferedDataTable[]{out};
    }


    protected List<String> getNegControls() {
        return Arrays.asList(AbstractScreenTrafoModel.getAndValidateTreatment(negControl));
    }


    protected List<String> getPosControls() {
        return Arrays.asList(AbstractScreenTrafoModel.getAndValidateTreatment(posControl));
    }


    private boolean isValidNumber(double zPrime) {
        return Double.isInfinite(zPrime) || Double.isNaN(zPrime);
    }


    private BufferedDataTable prepareZPrimeTable(ExecutionContext exec, List<String> barcodes) {
        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
        allColSpecs[0] = new DataColumnSpecCreator(groupBy.getStringValue(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC, StringCell.TYPE).createSpec();
        allColSpecs[2] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC, StringCell.TYPE).createSpec();

        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int index = 0;
        for (String positiveControl : getPosControls()) {
            for (String negativeControl : getNegControls()) {
                for (String barcode : barcodes) {
                    DataCell[] cells = new DataCell[]{new StringCell(barcode), new StringCell(positiveControl), new StringCell(negativeControl)};
                    DataRow row = new DefaultRow(new RowKey("Row " + index++), cells);
                    container.addRowToTable(row);
                }
            }
        }

        // once we are done, we close the container and return its table
        container.close();

        return container.getTable();
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<String> readouts = propReadouts.getIncludeList();

        DataColumnSpec[] allColSpecs = new DataColumnSpec[3 + readouts.size()];


        allColSpecs[0] = new DataColumnSpecCreator(groupBy.getStringValue(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC, StringCell.TYPE).createSpec();
        allColSpecs[2] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC, StringCell.TYPE).createSpec();

        int specCounter = 0;
        for (String attributeName : readouts) {
            allColSpecs[3 + specCounter++] = new DataColumnSpecCreator(attributeName, DoubleCell.TYPE).createSpec();
        }

        return new DataTableSpec[]{new DataTableSpec(allColSpecs)};
    }
}