package de.mpicbg.knime.hcs.base.nodes.reader;

import de.mpicbg.knime.knutils.AbstractNodeModel;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.AttributeUtils;
import de.mpicbg.knime.knutils.BufTableUtils;
import de.mpicbg.knime.knutils.ui.DefaultMicroscopeReaderDialog;
import de.mpicbg.knime.knutils.ui.FileSelectPanel;
import de.mpicbg.knime.hcs.core.TdsUtils;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the model implementation of ExcelReader.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class OperettaFileReader extends AbstractNodeModel {

    public SettingsModelString propInputDir = DefaultMicroscopeReaderDialog.createFileChooser();


    public OperettaFileReader() {
        super(0, 1);

        addSetting(propInputDir);
    }


    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), "txt");
        List<Attribute> colAttributes = getOperettaColModel(inputFiles.get(0));


        DataTableSpec outputSpec = AttributeUtils.compileTableSpecs(colAttributes);
        BufferedDataContainer container = exec.createDataContainer(outputSpec);


        int rowCounter = 0;
        int fileCounter = 0;

        for (File inputFile : inputFiles) {

            logger.info("reading file " + inputFile);

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;

            String barcode = null;
            while (reader.ready()) {
                line = reader.readLine().trim();
                if (line.trim().startsWith("Plate Name")) {
                    barcode = line.split("\t")[1];
                    break;
                }
            }

            if (barcode == null) {
                throw new RuntimeException("Could not plate-name in file " + inputFile);
            }

            // find start of data-section

            while (reader.ready()) {
                line = reader.readLine().trim();
                if (line.trim().equals("[Data]")) {
                    break;
                }
            }

            // skip the column header
            reader.readLine();

            while (reader.ready()) {

                // read the data
                line = reader.readLine();
                if (line == null || line.trim().length() == 0) {
                    break;
                }

                // caution: file contains one additional tab at the end of each line 
                String[] colData = line.split("\t", colAttributes.size());


                DataCell[] knimeRow = new DataCell[colAttributes.size()];


                // first add the barcode-column
                knimeRow[0] = new StringCell(barcode);


                // now add the actual contents
                knimeRow[1] = colAttributes.get(1).createCell(colData[0]);
                knimeRow[2] = colAttributes.get(2).createCell(colData[1]);

                if (colData.length != colAttributes.size()) {
                    throw new RuntimeException("Column model not consistent in input-files: Number of expected columns " + colAttributes.size() + " not present in file " + inputFile);
                }

                for (int i = 2; i < colData.length - 1; i++) {
                    String colValue = colData[i];

                    String value = colValue.trim();
                    double doubleValue;
                    
                    try {
                    	// handle inf
                    	if(value.equals("INF"))
                    		doubleValue = Double.MAX_VALUE;
                    	else
                    		doubleValue = Double.parseDouble(value);
                    } catch (NumberFormatException nfe) {
                        throw new Exception(nfe.getMessage() + "\n" + "Operetta result files may only contain numerical data. Please re-export the result file: " + inputFile.getAbsolutePath());
                    }
                    
                    knimeRow[i + 1] = colAttributes.get(i + 1).createCell(doubleValue);
                }

                DataRow tableRow = new DefaultRow(new RowKey("" + rowCounter++), knimeRow);
                container.addRowToTable(tableRow);
            }

            BufTableUtils.updateProgress(exec, fileCounter++, inputFiles.size());
        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};

    }


    private List<Attribute> getOperettaColModel(File inputFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while (reader.ready()) {
                line = reader.readLine().trim();

                if (line.trim().equals("[Data]")) {
                    String header = reader.readLine();
                    String[] columns = header.split("\t");

                    List<Attribute> colAttributes = new ArrayList<Attribute>();

                    colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_BARCODE, StringCell.TYPE));
                    colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_ROW, IntCell.TYPE));
                    colAttributes.add(new Attribute(TdsUtils.SCREEN_MODEL_WELL_COLUMN, IntCell.TYPE));

                    for (int i = 2, columnsLength = columns.length; i < columnsLength; i++) {
                        String colName = columns[i];
                        colAttributes.add(new Attribute(colName, DoubleCell.TYPE));
                    }

                    return colAttributes;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {

        List<File> inputFiles = FileSelectPanel.getInputFiles(propInputDir.getStringValue(), "txt");
        if (inputFiles.isEmpty()) {
            return new DataTableSpec[]{new DataTableSpec()};
        }

        List<Attribute> colAttributes = getOperettaColModel(inputFiles.get(0));

        return new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
    }
}