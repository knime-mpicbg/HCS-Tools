package de.mpicbg.tds.knime.hcstools.reader;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.tools.resconverter.OperaResFileReader;
import de.mpicbg.tds.core.tools.resconverter.ResConverter;
import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import de.mpicbg.tds.knime.knutils.Attribute;
import de.mpicbg.tds.knime.knutils.AttributeUtils;
import de.mpicbg.tds.knime.knutils.BufTableUtils;
import de.mpicbg.tds.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.tds.knime.knutils.ui.FileSelectPanel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
 * This is the model implementation of ExcelReader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class OperaFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();
    private static String ACQUISITION_DATE = "Acquisition Time";


    public OperaFileReader() {
        super(0, 1);

        addSetting(propInputDir);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        List<File> resFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), ResConverter.RES_SUFFIX);
        List<Attribute> colAttributes = getOperaColumnModel(resFiles.get(0));

        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(colAttributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);


        int tableRowCounter = 0;
        int fileCounter = 0;

        // keep a reference to the first file data which allows to test that the column-model does not change within the files
        OperaResFileReader firstFileData = null;


        for (File inputFile : resFiles) {
            logger.info("reading file " + inputFile);


            OperaResFileReader operaData = new OperaResFileReader(inputFile);

            // if not the first file, make sure that the column models match
            if (firstFileData == null) {
                firstFileData = operaData;
            } else {
                if (!firstFileData.getResultParameters().equals(operaData.getResultParameters())) {
                    throw new RuntimeException("column-definition mismatch between '" + firstFileData.getSourceFile() + "' and '" + operaData.getSourceFile() + "' ");
                }
            }


            // iterate over all wells in the opera-data-strucuture
            List<String> readoutNames = operaData.getResultParameters();
            for (Well well : operaData.getPlate().getWells()) {

                if (well.getWellStatistics().isEmpty()) {
                    continue;
                }

                DataCell[] knimeRow = new DataCell[operaData.getResultParameters().size() + 4]; // +3 because of barcode and row/col positions

                // start with the barcode column
                knimeRow[0] = new StringCell(operaData.getBarcode());

                // now add the actual contents
                knimeRow[1] = colAttributes.get(1).createCell(well.getPlateRow());
                knimeRow[2] = colAttributes.get(2).createCell(well.getPlateColumn());
                knimeRow[3] = colAttributes.get(3).createCell(operaData.getAcquisitionDate());


                // add all readouts to the well
                int colCounter = 0;
                for (String readoutName : readoutNames) {
                    knimeRow[colCounter + 4] = colAttributes.get(colCounter + 4).createCell(well.getReadout(readoutName));
                    colCounter++;
                }

                DataRow tableRow = new DefaultRow(new RowKey("" + tableRowCounter++), knimeRow);
                container.addRowToTable(tableRow);
            }

            BufTableUtils.updateProgress(exec, fileCounter++, resFiles.size());
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }


    private List<Attribute> getOperaColumnModel(File inputFile) {
        OperaResFileReader operaData = new OperaResFileReader(inputFile.getAbsolutePath());
        List<String> readoutNames = operaData.getResultParameters();

        List<Attribute> colAttributes = new ArrayList<Attribute>();


        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
        colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));
        colAttributes.add(new Attribute(OperaFileReader.ACQUISITION_DATE, StringCell.TYPE));


        // create attributes for the other readouts
        for (String readoutName : readoutNames) {
            colAttributes.add(new Attribute(readoutName, DoubleCell.TYPE));
        }

        return colAttributes;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        List<File> resFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), ResConverter.RES_SUFFIX);
//        if (resFiles.isEmpty()) {
//            return new DataTableSpec[]{new DataTableSpec()};
//        }

        List<Attribute> colAttributes = getOperaColumnModel(resFiles.get(0));

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }
}