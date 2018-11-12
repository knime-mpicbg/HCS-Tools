package de.mpicbg.knime.hcs.base.node.port.binning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.config.base.ConfigBase;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.workflow.ModelContentOutPortView;

import de.mpicbg.knime.hcs.core.math.BinningAnalysisModel;
import de.mpicbg.knime.hcs.core.math.Interval;

/**
 * Port-Object for Binning Analysis model
 * @author Antje Janosch
 *
 */
public class BinningPortObject implements PortObject {
	
	// keys to create @see ModelContent
	public static final String PORT_MAIN_KEY = "binning.calculate";
	public static final String PORT_COLUMNS_KEY = "selected.columns";
	public static final String PORT_BINS_KEY = "bin.size";
	
	public static final String PORT_INCL_KEY = "iv.include";
	public static final String PORT_BOUNDS_KEY = "iv.bounds";
	
	// important for port registration
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(BinningPortObject.class);
	
	// binning analysis model
	private BinningAnalysisModel m_model;

	/*
	 * constructor
	 */
	public BinningPortObject(BinningAnalysisModel binningModel) {
		this.m_model = binningModel;
	}

	/*
	 * constructor
	 * model set from model file
	 */
	public BinningPortObject(File binningSettingsFile) {
		
		BinningAnalysisModel model = new BinningAnalysisModel();
		
		try {
			FileInputStream fis = new FileInputStream(binningSettingsFile);
			ModelContentRO settings = ModelContent.loadFromXML(fis);
			List<String> selectedColumns = Arrays.asList(settings.getStringArray(PORT_COLUMNS_KEY));
			int nBins = settings.getInt(PORT_BINS_KEY);
			
			HashMap<String, LinkedList<Interval>> binningMap = new LinkedHashMap<String, LinkedList<Interval>>();
			
			try {
				for(String col : selectedColumns) {

					ConfigBase cfg = settings.getConfigBase(col);
					List<Interval> ivList = new LinkedList<Interval>();
					for(String k : cfg.keySet()) {
						ConfigBase cfgIv = cfg.getConfigBase(k);
						boolean[] incl = cfgIv.getBooleanArray(PORT_INCL_KEY);
						double[] bounds = cfgIv.getDoubleArray(PORT_BOUNDS_KEY);
						Interval iv = new Interval(bounds[0], bounds[1], k, Interval.getMode(incl[0], incl[1]));
						ivList.add(iv);
					}

					binningMap.put(col, (LinkedList<Interval>) ivList);
				}
			// in case of empty input table => binning map stays empty in the model
			} catch (InvalidSettingsException e) {}
			
			model = new BinningAnalysisModel(selectedColumns, nBins, binningMap);
					
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
		
		this.m_model = model;
	}

	@Override
	public String getSummary() {
		return "Binning Parameters";
	}

	@Override
	public PortObjectSpec getSpec() {
		final BinningPortObjectSpec spec =
                new BinningPortObjectSpec(m_model.getColumns().toArray(new String[m_model.getColumns().size()]));

        return spec;
	}

	@Override
	public JComponent[] getViews() {
		ModelContent model = createModelContentFromBinningModel();
        return new JComponent[] {new ModelContentOutPortView(model)};
	}
	
	/*
	 * converts binning analysis model to @see ModelContent
	 */
	private ModelContent createModelContentFromBinningModel() {
		
		assert m_model != null;
		List<String> selectedCols = m_model.getColumns();
		
		// populate node settings
		ModelContent settings = new ModelContent(BinningPortObject.PORT_MAIN_KEY);
		settings.addStringArray(BinningPortObject.PORT_COLUMNS_KEY, selectedCols.toArray(new String[selectedCols.size()]));
		settings.addInt(BinningPortObject.PORT_BINS_KEY, m_model.getNBins());

		// for each parameter
		for(Entry<String, LinkedList<Interval>> entry : m_model.getModel().entrySet()) {
			String col = entry.getKey();
			LinkedList<Interval> ivList = entry.getValue();

			ConfigBase cfg = settings.addConfigBase(col);
			// for each interval
			for(Interval iv : ivList) {
				ConfigBase cfg_bin = cfg.addConfigBase(iv.getLabel());
				boolean[] incl = {iv.checkModeLowerBound(),iv.checkModeUpperBound()};
				double[] bounds = {iv.getLowerBound(),iv.getUpperBound()};
				cfg_bin.addBooleanArray(BinningPortObject.PORT_INCL_KEY, incl);
				cfg_bin.addDoubleArray(BinningPortObject.PORT_BOUNDS_KEY, bounds);
			}
		}
		
		return settings;
	}

	/*
	 * save model to temporary file
	 */
	public Path writeModelToTmpFile() throws IOException {
		
		File binningSettingsFile = null;
		binningSettingsFile = File.createTempFile("binningSettings", ".xml");
		
		ModelContent model = createModelContentFromBinningModel();
		
		model.saveToXML(new FileOutputStream(binningSettingsFile));
		
		return binningSettingsFile.toPath();
	}

	/**
	 * @return {@link BinningAnalysisModel}
	 */
	public BinningAnalysisModel getBinningModel() {
		return m_model;
	}

}
