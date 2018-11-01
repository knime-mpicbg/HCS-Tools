package de.mpicbg.knime.hcs.base.node.port.binning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

public class BinningPortObjectSerializer extends PortObjectSerializer<BinningPortObject> {

	private static final String ZIP_ENTRY_PORT = "BinningPort.xml";
	
	@Override
	public void savePortObject(BinningPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_PORT));
		//Files.copy(portObject.getFile().toPath(), out);
		Files.copy(portObject.writeModelToTmpFile(), out);
		out.flush();
		out.closeEntry();
		out.close();
	}

	@Override
	public BinningPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		ZipEntry nextEntry = in.getNextEntry();
		if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_PORT)) {
			throw new IOException("Expected zip entry '" + ZIP_ENTRY_PORT + "' not present");
		}
		
		File binningSettingsFile = File.createTempFile("binningSettings", ".xml");
		Files.copy(in, binningSettingsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		in.close();
		return new BinningPortObject(binningSettingsFile);
	}



}
