package de.mpicbg.tds.knime.hcstools.normalization;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.knime.HCSAttributeUtils;
import de.mpicbg.tds.knime.hcstools.utils.AttributeStatistics;
import de.mpicbg.tds.knime.hcstools.visualization.ScreenExplorer;
import de.mpicbg.tds.knime.knutils.*;
import org.apache.commons.math.linear.RealMatrix;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * This is the model implementation of POCNormalizer. Some nodes to ease the handling and mining of HCS-data.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class BScoreNormalizer extends AbstractScreenTrafoModel {


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute plateAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute wellColAttribute = HCSAttributeUtils.getPlateColumnAttribute(input);
        Attribute wellRowAttribute = HCSAttributeUtils.getPlateRowAttribute(input);

        List<String> readouts = propReadouts.getIncludeList();

        int counter = 0;

        Map<String, List<DataRow>> plates = AttributeUtils.splitRows(input, plateAttribute);


        List<Attribute> attributeModel = AttributeUtils.convert(input.getDataTableSpec());

        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        for (String barcode : plates.keySet()) {
            List<DataRow> plateWells = plates.get(barcode);

            Plate plate = ScreenExplorer.parseIntoPlates(null, readouts, new ArrayList<String>(), Collections.singletonMap(barcode, plateWells), attributeModel, wellRowAttribute, wellColAttribute).get(0);

            logger.info("Normalizing plate " + plate.getBarcode() + " (" + counter++ + " of " + plates.size() + ")");


            for (String readout : readouts) {
                RealMatrix m = TdsUtils.getReadoutGrid(plate, readout);
                Attribute readoutAttribute = new InputTableAttribute(readout, input);
                Attribute normAttribute = new Attribute(readout + getAttributeNameSuffix(), DoubleCell.TYPE);


                // run the median-polish
                double madFactor = AttributeStatistics.getMadScalingFromPrefs();
                BScore bScore = new BScore(m, madFactor);


                for (DataRow dataRow : plateWells) {
                    Double readoutValue = readoutAttribute.getDoubleAttribute(dataRow);
                    int plateColumn = wellColAttribute.getIntAttribute(dataRow);
                    int plateRow = wellRowAttribute.getIntAttribute(dataRow);

                    if (readoutValue == null) {
                        updateCache.add(dataRow, normAttribute, DataType.getMissingCell());

                    } else {
                        double newValue = bScore.get(plateRow - 1, plateColumn - 1);
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
        return ".bscore";
    }
}