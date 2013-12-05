package de.mpicbg.tds.knime.helpers.exdata;

import de.mpicbg.tds.knime.knutils.AbstractNodeModel;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This is the model implementation of MatlabSnippet. Matlab integration for Knime.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class ExampleDataNodeModel extends AbstractNodeModel {


    public SettingsModelString selectedExample = ExampleDataNodeFactory.createSelectedExampleProperty();


    protected ExampleDataNodeModel() {
        super(0, 1);
        addSetting(selectedExample);
    }


    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
                                          final ExecutionContext exec) throws Exception {
        InputStream inputStream = getExDataInputStream();
        DataTable table = DataContainer.readFromStream(inputStream);
        BufferedDataTable out = exec.createBufferedDataTable(table, exec);

        return new BufferedDataTable[]{out};
    }


    public InputStream getExDataInputStream() {
        try {
            URL exampleFileURL = new URL(selectedExample.getStringValue());

            return exampleFileURL.openStream();
        } catch (Throwable e) {
            return null;
        }
    }


    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        InputStream exDataInputStream = getExDataInputStream();
        if (exDataInputStream == null) {
            throw new InvalidSettingsException("No example data selected");
        }
        try {

            DataTableSpec spec = peekDataTableSpec(exDataInputStream);
            if (spec == null) { // if written with 1.3.x and before
                logger.debug("Table spec is not first entry in input file, "
                        + "need to deflate entire file");
                DataTable outTable = DataContainer.readFromStream(exDataInputStream);
                spec = outTable.getDataTableSpec();
            }
            return new DataTableSpec[]{spec};
        } catch (IOException ioe) {
            String message = ioe.getMessage();
            if (message == null) {
                message = "Unable to read spec from file, "
                        + "no detailed message available.";
            }
            throw new InvalidSettingsException(message);

        }
    }


    /**
     * Opens the zip file and checks whether the first entry is the spec. If so, the spec is parsed and returned.
     * Otherwise null is returned.
     * <p/>
     * <p> This method is used to fix bug #1141: Dialog closes very slowly.
     *
     * @param inputStream To read from.
     * @return The spec or null (null will be returned when the file was written with a version prior 2.0)
     * @throws IOException If that fails for any reason.
     */
    private DataTableSpec peekDataTableSpec(final InputStream inputStream)
            throws IOException {
        // must not use ZipFile here as it is known to have memory problems
        // on large files, see e.g.
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5077277
        ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(inputStream));
        ZipEntry entry = zipIn.getNextEntry();

        try {
            // hardcoded constants here as we do not want additional
            // functionality to DataContainer ... at least not yet.
            if ("spec.xml".equals(entry != null ? entry.getName() : "")) {
                NodeSettingsRO settings = NodeSettings.loadFromXML(
                        new NonClosableInputStream.Zip(zipIn));
                try {
                    NodeSettingsRO specSettings = settings.getNodeSettings("table.spec");
                    return DataTableSpec.load(specSettings);

                } catch (InvalidSettingsException ise) {
                    IOException ioe = new IOException(
                            "Unable to read spec from file");
                    ioe.initCause(ise);
                    throw ioe;
                }
            } else {
                return null;
            }
        } finally {
            zipIn.close();
        }
    }

}

