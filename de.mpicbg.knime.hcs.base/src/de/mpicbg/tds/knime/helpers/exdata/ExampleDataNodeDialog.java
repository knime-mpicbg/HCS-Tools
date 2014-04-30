package de.mpicbg.tds.knime.helpers.exdata;

import de.mpicbg.tds.knime.hcstools.HCSToolsBundleActivator;
import de.mpicbg.tds.knime.hcstools.prefs.HCSToolsPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ExampleDataNodeDialog extends DefaultNodeSettingsPane {

    public ExDataSelectPanel exDataSelectPanel;


    public ExampleDataNodeDialog() {
        List<URL> exListURLs = getExListUrls();

        exDataSelectPanel = new ExDataSelectPanel(exListURLs);
        addTab("Script Output", exDataSelectPanel);

        removeTab("Options");
    }


    private List<URL> getExListUrls() {
        IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
        String templateResources = prefStore.getString(HCSToolsPreferenceInitializer.EXDATA_LIST_LOCATIONS);
        List<URL> exListURLs = new ArrayList<URL>();
        for (String s : templateResources.split(";")) {
            try {
                exListURLs.add(new URL(s));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return exListURLs;
    }


    @Override
    public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        try {
            String fileURL = settings.getString("sel.ex");
            exDataSelectPanel.setSelectedExample(fileURL);

        } catch (InvalidSettingsException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);

        settings.addString("sel.ex", exDataSelectPanel.getCurDataSet().getFileURL());
    }
}
