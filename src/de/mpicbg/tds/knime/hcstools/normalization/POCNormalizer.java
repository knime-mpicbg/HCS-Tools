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
 * This is the model implementation of POCNormalizer. Some nodes to ease the handling and mining of HCS-data.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class POCNormalizer extends AbstractScreenTrafoModel {


    private SettingsModelString control = createTreatmentSelector(TREATMENT);
    private SettingsModelBoolean propRobustStats = createPropRobustStats();
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    public POCNormalizer() {
        addSetting(treatmentAttribute);
        addSetting(control);
        addSetting(propRobustStats);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute plateAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        String refControlName = AbstractScreenTrafoModel.getAndValidateTreatment(control);
        Boolean useMedian = propRobustStats.getBooleanValue();
        List<String> readouts = propReadouts.getIncludeList();

        int counter = 0;

        Map<Object, List<DataRow>> plates = AttributeUtils.splitRowsGeneric(input, plateAttribute);


        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        for (Object barcode : plates.keySet()) {
            logger.info("Normalizing plate " + barcode + " (" + counter++ + " of " + plates.size());

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = plates.get(barcode);
            List<DataRow> ctrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, refControlName);


            for (String readout : readouts) {
                Attribute readoutAttribute = new InputTableAttribute(readout, input);
                Attribute normAttribute = new Attribute(readout + getAttributeNameSuffix(), DoubleCell.TYPE);

                // normalize the value                                         `
                double normFactor = useMedian ? AttributeStatistics.median(ctrlWells, readoutAttribute) : AttributeStatistics.mean(ctrlWells, readoutAttribute);

                for (DataRow dataRow : plate) {
                    // normalize values if possible or set them to missing if not:
                    Double originalValue = readoutAttribute.getDoubleAttribute(dataRow);

                    if (originalValue == null) {
                        updateCache.add(dataRow, normAttribute, DataType.getMissingCell());

                    } else {
                        double newValue = normFactor == Double.NaN ? Double.NaN : (100 * originalValue / normFactor);
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
        return ".poc";
    }
}

