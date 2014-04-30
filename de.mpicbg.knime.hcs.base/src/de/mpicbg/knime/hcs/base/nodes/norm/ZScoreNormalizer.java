package de.mpicbg.knime.hcs.base.nodes.norm;

import de.mpicbg.knime.hcs.base.utils.AttributeStatistics;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.InputTableAttribute;
import de.mpicbg.knime.knutils.TableUpdateCache;
import de.mpicbg.knime.knutils.*;
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

import static de.mpicbg.knime.hcs.base.utils.AttributeStatistics.*;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class ZScoreNormalizer extends AbstractScreenTrafoModel {

    private SettingsModelString propRefSample = createTreatmentSelector(TREATMENT);
    SettingsModelBoolean propRobustStats = createPropRobustStats();
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    public ZScoreNormalizer() {
        addSetting(treatmentAttribute);
        addSetting(propRefSample);
        addSetting(propRobustStats);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute plateAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        Boolean beRobust = propRobustStats.getBooleanValue();
        List<String> readouts = propReadouts.getIncludeList();
        String refControl = AbstractScreenTrafoModel.getAndValidateTreatment(propRefSample);

        int counter = 0;

        Map<Object, List<DataRow>> plates = AttributeUtils.splitRowsGeneric(input, plateAttribute);


        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        for (Object barcode : plates.keySet()) {
            logger.info("Normalizing plate " + barcode + " (" + counter++ + " of " + plates.size());

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = plates.get(barcode);
            List<DataRow> refSampleWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, refControl);


            for (String readout : readouts) {
                Attribute readoutAttribute = new InputTableAttribute(readout, input);
                Attribute normAttribute = new Attribute(readout + getAttributeNameSuffix(), DoubleCell.TYPE);

                // normalize the value
                double mean = beRobust ? median(refSampleWells, readoutAttribute) : AttributeStatistics.mean(refSampleWells, readoutAttribute);
                double stdDev = beRobust ? mad(refSampleWells, readoutAttribute) : accuStats(refSampleWells, readoutAttribute).getStandardDeviation();

                for (DataRow dataRow : plate) {
                    Double readoutValue = readoutAttribute.getDoubleAttribute(dataRow);

                    if (readoutValue == null) {
                        updateCache.add(dataRow, normAttribute, DataType.getMissingCell());

                    } else {
                        double newValue = (readoutValue - mean) / (stdDev);
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
        return ".zscore";
    }

}