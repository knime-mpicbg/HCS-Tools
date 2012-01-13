package de.mpicbg.tds.knime.hcstools.qualitycontrol;


import de.mpicbg.tds.knime.hcstools.HCSSettingsFactory;
import de.mpicbg.tds.knime.knutils.*;
import org.apache.commons.math.linear.*;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.correlation.Covariance;
import org.knime.core.data.*;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.*;

import static de.mpicbg.tds.knime.hcstools.normalization.AbstractScreenTrafoModel.*;
import static de.mpicbg.tds.knime.hcstools.utils.Table2Matrix.extractMatrix;


/**
 * Caluclates z-primes but for sets of positive and negative controls
 *
 * @author Felix Meyenhofer
 *         <p/>
 *         TO DO - add the column classification error. - add a column to show which parameter were removed from the
 *         initial set.
 */

public class MultivariateZPrimes extends AbstractNodeModel {


    public static final String POS_CTRL_MULTIPLE = "multi.pos.ctrls";
    public static final String NEG_CTRL_MULTIPLE = "multi.neg.ctrls";

    public SettingsModelStringArray multiPosCtrls;
    public SettingsModelStringArray multiNegCtrls;
    private SettingsModelFilterString propReadouts = createPropReadoutSelection();
    public SettingsModelString treatmentAttribute = createTreatmentAttributeSelector();
    public SettingsModelString groupBy = HCSSettingsFactory.createGroupBy();


    public MultivariateZPrimes() {
        addSetting(groupBy);
        addSetting(treatmentAttribute);
        addSetting(propReadouts);
        addControlSettings();
    }


    protected void addControlSettings() {
        multiPosCtrls = MultivariateZPrimesFactory.createMultiCtls(POS_CTRL_MULTIPLE);
        multiNegCtrls = MultivariateZPrimesFactory.createMultiCtls(NEG_CTRL_MULTIPLE);
        addSetting(multiPosCtrls);
        addSetting(multiNegCtrls);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable input = inData[0];

        Attribute groupingAttribute = new InputTableAttribute(groupBy.getStringValue(), input);
        Attribute treatmentAttribute = new InputTableAttribute(this.treatmentAttribute.getStringValue(), input);

        List<String> posCtrls = getPosControls();
        List<String> negCtrls = getNegControls();

        // make sure that both sets are disjunct
        HashSet<String> intersection = new HashSet<String>(posCtrls);
        intersection.retainAll(negCtrls);
        if (!intersection.isEmpty()) {
            throw new RuntimeException("Selected positive and negative control(s) are identical (not disjunct), which is unlikely to have meaningful semantics");
        }

        // get the parameter
//        List<String> readouts = propReadouts.getIncludeList();
        List<Attribute> readouts = new ArrayList<Attribute>();
        for (String item : propReadouts.getIncludeList()) {
            Attribute attribute = new InputTableAttribute(item, input);
            if (attribute.getType().isCompatible(DoubleValue.class)) {
                readouts.add(attribute);
            } else {
                logger.warn("The parameter '" + attribute.getName() + "' will not be considered for outlier removal, since it is not a DoubleCell type.");
            }
        }

        // get the grouping of the measurements
        Map<String, List<DataRow>> groups = AttributeUtils.splitRows(input, groupingAttribute);

        // create a simple table with just one column which contains the barcodes of the groups
        BufferedDataTable barcodeTable = prepareZPrimeTable(exec, new ArrayList<String>(groups.keySet()));
        TableUpdateCache updateCache = new TableUpdateCache(barcodeTable.getDataTableSpec());

        Attribute barcodeAttr = new InputTableAttribute(groupBy.getStringValue(), barcodeTable);
        Attribute positiveAttr = new InputTableAttribute(POSITIVE_CONTROL_DESC, barcodeTable);
        Attribute negativeAttr = new InputTableAttribute(NEGATIVE_CONTROL_DESC, barcodeTable);

        // group loop
        int counter = 0;
        for (DataRow dataRow : barcodeTable) {

            String barcode = barcodeAttr.getNominalAttribute(dataRow);
            String positiveControl = positiveAttr.getNominalAttribute(dataRow);
            String negativeControl = negativeAttr.getNominalAttribute(dataRow);
            logger.info("Calculating z-prime for group '" + barcode + "'");

            // inspired by ValueSubgroupIteration
            List<DataRow> plate = groups.get(barcode);
            List<DataRow> posCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, positiveControl);
            List<DataRow> negCtrlWells = AttributeUtils.filterByAttributeValue(plate, treatmentAttribute, negativeControl);

            // assemble the matrices
            RealMatrix posMatrix = extractMatrix(posCtrlWells, readouts);
            RealMatrix negMatrix = extractMatrix(negCtrlWells, readouts);

            Double zPrime = Double.NaN;
            Double classificationError = Double.NaN;
            if ((posMatrix != null) && (negMatrix != null)) {

                // remove zero columns
                int[] columnIndex = checkColumns(posMatrix, negMatrix);
                int[] rowIndex = getRowIndices(posMatrix);
                posMatrix.getSubMatrix(rowIndex, columnIndex);
                rowIndex = getRowIndices(negMatrix);
                negMatrix.getSubMatrix(rowIndex, columnIndex);

                // bootstrapping
                posMatrix = bootstrapMatrix(posMatrix);
                negMatrix = bootstrapMatrix(negMatrix);

                if ((posMatrix != null) && (negMatrix != null)) {
                    // mean vecotrs
                    RealVector posMeanVect = computeColumnMeans(posMatrix);
                    RealVector negMeanVect = computeColumnMeans(negMatrix);

                    // Covairiance within classes
                    Covariance posCov = new Covariance(posMatrix);
                    Covariance negCov = new Covariance(negMatrix);

                    // compute the weights
                    RealVector meanVect = posMeanVect.subtract(negMeanVect);
                    RealMatrix cov = posCov.getCovarianceMatrix();
                    cov.add(negCov.getCovarianceMatrix());

                    try {
                        DecompositionSolver solver = new SingularValueDecompositionImpl(cov).getSolver();
                        //                    boolean flag = solver.isNonSingular();
                        RealMatrix inv = solver.getInverse();
                        RealVector weights = inv.preMultiply(meanVect);

                        // calcualte the projected values
                        double[] posProj = computeProjectedValues(posMatrix, weights);
                        double[] negProj = computeProjectedValues(negMatrix, weights);

                        // calculate the missclassification
                        classificationError = calculateClassificationError(weights, posMeanVect, negMeanVect, posProj, negProj);

                        // calculate the z-prime factor for the current plate
                        double posCtrlMean = StatUtils.mean(posProj);
                        double posCtrlSD = Math.sqrt(StatUtils.variance(posProj));
                        double negCtrlMean = StatUtils.mean(negProj);
                        double negCtrlSD = Math.sqrt(StatUtils.variance(negProj));
                        zPrime = 1 - 3 * ((posCtrlSD + negCtrlSD) / Math.abs(posCtrlMean - negCtrlMean));

                    } catch (InvalidMatrixException e) {

                        zPrime = Double.NaN;
                    }
                }
            }

            String posStatus = getSampilingStatus(posMatrix, posCtrlWells.size());
            String negStatus = getSampilingStatus(negMatrix, negCtrlWells.size());

            // parse the values in the ouput table.
            Attribute attribute;
            attribute = new Attribute(POSITIVE_CONTROL_DESC + "_Samples", DoubleCell.TYPE);
            updateCache.add(dataRow, attribute, new DoubleCell(posCtrlWells.size()));
            attribute = new Attribute(POSITIVE_CONTROL_DESC + "_Status", StringCell.TYPE);
            updateCache.add(dataRow, attribute, new StringCell(posStatus));
            attribute = new Attribute(NEGATIVE_CONTROL_DESC + "_Samples", DoubleCell.TYPE);
            updateCache.add(dataRow, attribute, new DoubleCell(negCtrlWells.size()));
            attribute = new Attribute(NEGATIVE_CONTROL_DESC + "_Status", StringCell.TYPE);
            updateCache.add(dataRow, attribute, new StringCell(negStatus));
            attribute = new Attribute("Multivariate_Z-Prime", DoubleCell.TYPE);
            updateCache.add(dataRow, attribute, isValidNumber(zPrime) ? DataType.getMissingCell() : new DoubleCell(zPrime));
            attribute = new Attribute("ClassificationError", DoubleCell.TYPE);
            updateCache.add(dataRow, attribute, isValidNumber(classificationError) ? DataType.getMissingCell() : new DoubleCell(classificationError));

            BufTableUtils.updateProgress(exec, counter++, groups.size());
        }

        // build the output-table
        ColumnRearranger c = updateCache.createColRearranger();
        BufferedDataTable out = exec.createColumnRearrangeTable(barcodeTable, c, exec);

        return new BufferedDataTable[]{out};
    }

    protected static double calculateClassificationError(RealVector w, RealVector pmean, RealVector nmean, double[] pproj, double[] nproj) {

        double threshold = pmean.add(nmean).dotProduct(w) / 2;
        double nbsamples = pproj.length + nproj.length;
        double missclassification;
        if (pmean.dotProduct(w) < nmean.dotProduct(w)) {
            missclassification = countMissclassifications(threshold, pproj, nproj);
        } else {
            missclassification = countMissclassifications(threshold, nproj, pproj);
        }
        return missclassification / nbsamples * 100;
    }


    protected static double countMissclassifications(double th, double[] a, double[] b) {
        double missed = 0;
        for (double v : a) {
            if (v > th) {
                missed++;
            }
        }
        for (double v : b) {
            if (v < th) {
                missed++;
            }
        }
        return missed;
    }


    protected int[] checkColumns(RealMatrix pm, RealMatrix nm) {
        ArrayList<Integer> colInd = new ArrayList<Integer>();
        for (int c = 0; c < pm.getColumnDimension(); ++c) {
            if (Math.abs(StatUtils.sum(pm.getColumn(c))) <= Double.MIN_VALUE) {
                continue;
            } else if (Math.abs(StatUtils.sum(pm.getColumn(c))) <= Double.MIN_VALUE) {
                continue;
            }
            colInd.add(c);
        }
        int[] ind = new int[colInd.size()];
        int i = 0;
        for (int c : colInd) {
            ind[i++] = c;
        }
        return ind;
    }


    protected int[] getRowIndices(RealMatrix mat) {
        int[] ind = new int[mat.getRowDimension()];
        for (int i = 0; i < mat.getRowDimension(); ++i) {
            ind[i] = i;
        }
        return ind;
    }


    protected double[] computeProjectedValues(RealMatrix mat, RealVector weig) {
        double[] proj = new double[mat.getRowDimension()];
        for (int r = 0; r < mat.getRowDimension(); ++r) {
            RealVector vec = mat.getRowVector(r);
            RealVector prod = vec.ebeMultiply(weig);
            proj[r] = StatUtils.sum(prod.getData());
        }
        return proj;
    }


    protected RealVector computeColumnMeans(RealMatrix mat) {
        double[] meanVect = new double[mat.getColumnDimension()];
        for (int c = 0; c < mat.getColumnDimension(); ++c) {
            RealVector vect = mat.getColumnVector(c);
            meanVect[c] = StatUtils.mean(vect.getData());
        }
        return new ArrayRealVector(meanVect);
    }


    protected RealMatrix bootstrapMatrix(RealMatrix mat) {
        double[][] bootstrap;
        if ((mat.getRowDimension() < mat.getColumnDimension()) && (mat.getRowDimension() >= 3)) {
//            if (mat.getRowDimension() < 3) {
//                return mat;
//            } else {
            int Nboot;
            if (mat.getRowDimension() < 100) {
                Nboot = 100;
            } else {
                Nboot = mat.getColumnDimension();
            }
            bootstrap = new double[Nboot][mat.getColumnDimension()];
            int R;
            RandomData rand = new RandomDataImpl();
            for (int c = 0; c < mat.getColumnDimension(); ++c) {
                for (int r = 0; r < bootstrap.length; ++r) {
                    R = rand.nextInt(0, mat.getRowDimension() - 1);
                    bootstrap[r][c] = mat.getEntry(R, c);
                }
            }
//            }
            return new Array2DRowRealMatrix(bootstrap);
        } else {
//            bootstrap = mat.getData();
            return mat;
        }

    }


    protected String getSampilingStatus(RealMatrix mat, int origSize) {
        String sampleStatus = "";
        if (origSize < 3) {
            sampleStatus = "Too few samples";
        }
        if (origSize < mat.getColumnDimension()) {
            sampleStatus = "Bootstraped -> " + mat.getRowDimension();
        } else {
            sampleStatus = "OK";
        }
        return sampleStatus;
    }


    private boolean isValidNumber(double nb) {
        return Double.isInfinite(nb) || Double.isNaN(nb);
    }


    protected List<String> getPosControls() {
        List<String> posCtrls = new ArrayList<String>(Arrays.asList(multiPosCtrls.getStringArrayValue()));
        cleanControls(posCtrls);
        return posCtrls;
    }


    protected List<String> getNegControls() {
        List<String> negCtrls = new ArrayList<String>(Arrays.asList(multiNegCtrls.getStringArrayValue()));
        cleanControls(negCtrls);
        return negCtrls;
    }


    private void cleanControls(List<String> ctrls) {
        for (int i = 0; i < ctrls.size(); i++) {
            if (ctrls.get(i).trim().length() < 1) {
                ctrls.remove(i);
                i--;
            }
        }
    }


    private BufferedDataTable prepareZPrimeTable(ExecutionContext exec, List<String> barcodes) {
//        List<String> readouts = propReadouts.getIncludeList();
        List<String> posCtrls = getPosControls();
        List<String> negCtrls = getNegControls();

        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
        allColSpecs[0] = new DataColumnSpecCreator(groupBy.getStringValue(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC, StringCell.TYPE).createSpec();
        allColSpecs[2] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC, StringCell.TYPE).createSpec();

        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);

        // fill in the first three columns.
        int index = 0;
        for (String positiveControl : posCtrls) {
            for (String negativeControl : negCtrls) {
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
        DataColumnSpec[] allColSpecs = new DataColumnSpec[9];

        allColSpecs[0] = new DataColumnSpecCreator(groupBy.getStringValue(), StringCell.TYPE).createSpec();
        allColSpecs[1] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC, StringCell.TYPE).createSpec();
        allColSpecs[2] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC, StringCell.TYPE).createSpec();
        allColSpecs[3] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC + "_Samples", DoubleCell.TYPE).createSpec();
        allColSpecs[4] = new DataColumnSpecCreator(POSITIVE_CONTROL_DESC + "_Status", StringCell.TYPE).createSpec();
        allColSpecs[5] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC + "_Samples", DoubleCell.TYPE).createSpec();
        allColSpecs[6] = new DataColumnSpecCreator(NEGATIVE_CONTROL_DESC + "_Status", StringCell.TYPE).createSpec();
        allColSpecs[7] = new DataColumnSpecCreator("Multivariate_Z-Prime", DoubleCell.TYPE).createSpec();
        allColSpecs[8] = new DataColumnSpecCreator("ClassificationError", DoubleCell.TYPE).createSpec();

        return new DataTableSpec[]{new DataTableSpec(allColSpecs)};
    }


}