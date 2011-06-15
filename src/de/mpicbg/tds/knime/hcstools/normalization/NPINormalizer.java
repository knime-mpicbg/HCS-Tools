package de.mpicbg.tds.knime.hcstools.normalization;

import de.mpicbg.tds.knime.hcstools.utils.AttributeStatistics;
import de.mpicbg.tds.knime.knutils.*;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.List;
import java.util.Map;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class NPINormalizer extends AbstractScreenTrafoModel {

    private SettingsModelString posControl = createTreatmentSelector(TREATMENT_POS_CONTROL);
    private SettingsModelString negControl = createTreatmentSelector(TREATMENT_NEG_CONTROL);
    private SettingsModelBoolean propRobustStats = createPropRobustStats();
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    public NPINormalizer() {
        addSetting(treatmentAttribute);
        addSetting(posControl);
        addSetting(negControl);
        addSetting(propRobustStats);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute plateAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        String positiveControl = AbstractScreenTrafoModel.getAndValidateTreatment(posControl);
        String negativeControl = AbstractScreenTrafoModel.getAndValidateTreatment(negControl);

        if (positiveControl.equals(negativeControl)) {
            throw new RuntimeException("Selected positive and negative control are identical, which is unlikely to have meaningful semantics");
        }

        Boolean useMedian = propRobustStats.getBooleanValue();

        List<String> readouts = propReadouts.getIncludeList();

        int counter = 0;

        Map<Object, List<DataRow>> plates = AttributeUtils.splitRowsGeneric(input, plateAttribute);


        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        for (Object barcode : plates.keySet()) {
            logger.info("Normalizing plate " + barcode + " (" + counter++ + " of " + plates.size());

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = plates.get(barcode);
            List<DataRow> posCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, positiveControl);
            List<DataRow> negCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, negativeControl);


            for (String readout : readouts) {
                Attribute readoutAttribute = new InputTableAttribute(readout, input);
                Attribute normAttribute = new Attribute(readout + getAttributeNameSuffix(), DoubleCell.TYPE);


                // normalize the value
                double posCtrlMean = useMedian ? AttributeStatistics.median(posCtrlWells, readoutAttribute) : AttributeStatistics.mean(posCtrlWells, readoutAttribute);
                double negCtrlMean = useMedian ? AttributeStatistics.median(negCtrlWells, readoutAttribute) : AttributeStatistics.mean(negCtrlWells, readoutAttribute);

                for (DataRow dataRow : plate) {
                    Double readoutValue = readoutAttribute.getDoubleAttribute(dataRow);

                    if (readoutValue == null) {
                        updateCache.add(dataRow, normAttribute, DataType.getMissingCell());

                    } else {
                        double newValue = (posCtrlMean - readoutValue) / (posCtrlMean - negCtrlMean);
                        updateCache.add(dataRow, normAttribute, new DoubleCell(newValue));
                    }
                }
            }

            BufTableUtils.updateProgress(exec, counter, plates.size());
        }


        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(input, c, exec);

        return new BufferedDataTable[]{out};
    }


    @Override
    protected String getAppendSuffix() {
        return ".npi";
    }
}
