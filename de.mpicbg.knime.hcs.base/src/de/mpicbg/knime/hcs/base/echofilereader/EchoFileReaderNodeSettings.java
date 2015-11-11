package de.mpicbg.knime.hcs.base.echofilereader;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class EchoFileReaderNodeSettings {
	private static final String FILE_URL = "fileUrl";
	
	private String m_fileURL = null;

	 /**
     * @return the fileURL
     */
    String getFileURL() {
        return m_fileURL;
    }

    /**
     * @param fileURL the fileURL to set
     */
    void setFileURL(final String fileURL) {
        m_fileURL = fileURL;
    }

    void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec inSpec) {
        m_fileURL = settings.getString(FILE_URL, null);
    }
    void loadSettingsModel(final NodeSettingsRO settings)
                throws InvalidSettingsException {
                m_fileURL = settings.getString(FILE_URL);
    }
    void saveSettings(final NodeSettingsWO settings) {
                    settings.addString(FILE_URL, m_fileURL);
}
}