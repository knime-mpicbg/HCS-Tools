package de.mpicbg.tds.knime.hcstools.normalization.byrow;

import de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel;
import de.mpicbg.tds.knime.knutils.*;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

import java.util.List;


/**
 * This is the model implementation of ZScoreNormalizer.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class VectorLengthNormalizer extends AbstractNodeModel {

    private SettingsModelFilterString vectorSpace = VectorLengthNormalizerFactory.createVectorSpaceSelector();
    private SettingsModelBoolean replaceExistingValues = AbstractScreenTrafoModel.createPropReplaceValues();
    private SettingsModelBoolean addVectNormCol = VectorLengthNormalizerFactory.createAppendLengthCol();


    public VectorLengthNormalizer() {
        addSetting(vectorSpace);
        addSetting(replaceExistingValues);
        addSetting(addVectNormCol);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        List<String> readouts = vectorSpace.getIncludeList();

        List<Attribute> vectorSpaceAtrributes = AttributeUtils.compileSpecs(readouts, input);

        TableUpdateCache updateCache = new TableUpdateCache(input.getDataTableSpec());

        int rowCounter = 0;
        for (DataRow dataRow : input) {

            // calculate length
            Double squaredLength = 0.;
            for (Attribute vectorSpaceAtrribute : vectorSpaceAtrributes) {
                Double value = vectorSpaceAtrribute.getDoubleAttribute(dataRow);

                if (value == null) {
                    squaredLength = null;
                    break;
                }

                squaredLength += value * value;
            }

            // calculate the length
            Double length = null;
            if (squaredLength != null) {
                length = Math.sqrt(squaredLength);
            }

            // Parse the vector norm column
            if (addVectNormCol.getBooleanValue()) {
                Attribute normColumn = new Attribute("vector-norm", DoubleCell.TYPE);
                if (squaredLength == null) {
                    updateCache.add(dataRow, normColumn, DataType.getMissingCell());

                } else {
                    updateCache.add(dataRow, normColumn, new DoubleCell(length));
                }
            }


            // Parse the normalized values
            for (Attribute vectorSpaceAtrribute : vectorSpaceAtrributes) {
                String attributeName = vectorSpaceAtrribute.getName();
                Attribute normAttribute = new Attribute(attributeName + getAttributeNameSuffix(), DoubleCell.TYPE);

                if (squaredLength == null) {
                    updateCache.add(dataRow, normAttribute, DataType.getMissingCell());

                } else {
                    double nonnormValue = vectorSpaceAtrribute.getDoubleAttribute(dataRow);
                    double normValue = nonnormValue / length;
                    updateCache.add(dataRow, normAttribute, new DoubleCell(normValue));
                }
            }

            BufTableUtils.updateProgress(exec, rowCounter++, input.getRowCount());
        }

        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(input, c, exec);

        return new BufferedDataTable[]{out};
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        TableUpdateCache updateCache = new TableUpdateCache(inSpecs[0]);
        String suffix = getAttributeNameSuffix();

        if (addVectNormCol.getBooleanValue()) {
            updateCache.registerAttribute(new Attribute("vector-norm", DoubleCell.TYPE));
        }
        for (String columnName : vectorSpace.getIncludeList()) {
            updateCache.registerAttribute(new Attribute(columnName + suffix, DoubleCell.TYPE));
        }

        return new DataTableSpec[]{updateCache.createColRearranger().createSpec()};
    }


    public String getAttributeNameSuffix() {
        return replaceExistingValues.getBooleanValue() ? "" : ".lennorm";
    }


}