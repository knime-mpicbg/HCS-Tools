package de.mpicbg.tds.knime.hcstools.qualitycontrol;

import de.mpicbg.tds.knime.knutils.*;
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
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.*;
import static de.mpicbg.tds.knime.hcstools.utils.AttributeStatistics.*;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class CVCalculator extends AbstractNodeModel {

    private SettingsModelStringArray treatmentList = CVCalculatorFactory.createTreatmentProperty();
    private SettingsModelString groupBy = createWellGroupingAttribute();
    private SettingsModelBoolean propRobustStats = createPropRobustStats();
    private SettingsModelFilterString propReadouts = createPropReadoutSelection();
    private SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();


    public CVCalculator() {
        addSetting(groupBy);

        addSetting(treatmentAttribute);
        addSetting(treatmentList);

        addSetting(propReadouts);

        addSetting(propRobustStats);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute groupingAttribute = new InputTableAttribute(this.groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        logger.info("TreatmentAttribute, stringValue='" + this.treatmentAttribute.getStringValue() + "', attribute='" + treatmentAttribute.getName() + "'");
        Boolean beRobust = propRobustStats.getBooleanValue();
        List<String> readouts = propReadouts.getIncludeList();

        Map<Object, List<DataRow>> plates = AttributeUtils.splitRowsGeneric(input, groupingAttribute);

        List<String> filters = new ArrayList<String>(Arrays.asList(treatmentList.getStringArrayValue()));

        BufferedDataTable cvTable = prepareCVTable(exec, new ArrayList<Object>(plates.keySet()), groupingAttribute, filters);
        TableUpdateCache updateCache = new TableUpdateCache(cvTable.getDataTableSpec());

        Attribute cvTableGroupingAttribute = new InputTableAttribute(this.groupBy.getStringValue(), cvTable);
        Attribute treatment = new InputTableAttribute(this.treatmentAttribute.getStringValue(), cvTable);

        int counter = 0;
        for (DataRow dataRow : cvTable) {
            String groupingFactor = cvTableGroupingAttribute.getNominalAttribute(dataRow);
            String filter = treatment.getNominalAttribute(dataRow);

            logger.info("Calculating CV for plate '" + groupingFactor + "', filter='" + filter + "'");

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = plates.get(groupingFactor);
            for (String readout : readouts) {
                Attribute readoutAttribute = new InputTableAttribute(readout, input);
                List<DataRow> targetTreatmentWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, filter);

                // Calculate the CV for the current plate
                double mean = beRobust ? median(targetTreatmentWells, readoutAttribute) : mean(targetTreatmentWells, readoutAttribute);
                double standardDeviation = beRobust ? mad(targetTreatmentWells, readoutAttribute) : stdDev(targetTreatmentWells, readoutAttribute);

                double cv = 100 * (standardDeviation / mean);

                String cvAttributeName = readout;
                Attribute cvAttribute = new Attribute(cvAttributeName, DoubleCell.TYPE);

                updateCache.add(dataRow, cvAttribute, isValidNumber(cv) ? DataType.getMissingCell() : new DoubleCell(cv));
            }

            BufTableUtils.updateProgress(exec, counter++, plates.size());
        }


        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(cvTable, c, exec);

        return new BufferedDataTable[]{out};
    }


    private boolean isValidNumber(double zPrime) {
        return Double.isInfinite(zPrime) || Double.isNaN(zPrime);
    }


    private BufferedDataTable prepareCVTable(ExecutionContext exec, List rowNames, Attribute groupingAttribute, List<String> filters) {
        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[2];
        allColSpecs[0] = new DataColumnSpecCreator(groupingAttribute.getName(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(this.treatmentAttribute.getStringValue(), StringCell.TYPE).createSpec();
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        int index = 0;
        for (String filter : filters) {
            for (int i = 0; i < rowNames.size(); i++) {
                String groupingFactor = rowNames.get(i).toString();

                DataCell[] cells = new DataCell[]{new StringCell(groupingFactor), new StringCell(filter)};
                DataRow row = new DefaultRow(new RowKey("Row " + index++), cells);
                container.addRowToTable(row);
            }
        }

        // once we are done, we close the container and return its table
        container.close();

        return container.getTable();
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<String> readouts = propReadouts.getIncludeList();
        Attribute groupingAttribute = new InputTableAttribute(this.groupBy.getStringValue(), inSpecs[0]);

        DataColumnSpec[] allColSpecs = new DataColumnSpec[2 + readouts.size()];
        allColSpecs[0] = new DataColumnSpecCreator(groupingAttribute.getName(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(this.treatmentAttribute.getStringValue(), StringCell.TYPE).createSpec();

        for (int i = 0; i < readouts.size(); i++) {
            String zprimeAttributeName = readouts.get(i);

            allColSpecs[i + 2] = new DataColumnSpecCreator(zprimeAttributeName, DoubleCell.TYPE).createSpec();
        }

        return new DataTableSpec[]{new DataTableSpec(allColSpecs)};
    }
}