package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.knutils.AbstractNodeModel;


/**
 * This is the model implementation of EchoFileReader.
 * 
 *
 * @author Magda Rucinska
 * 
 */
public class EchoFileReaderNodeModel extends AbstractNodeModel {

	// NODE SETTINGS KEYS + DEFAULTS
	public static final String CFG_FILE_URL = "fileUrl";
	public static final String CFG_splitSourceCol = "split.source.column";
	public static final String CFG_splitDestinationCol = "split.destination.column";

	/**
	 * Constructor for the node model.
	 */
	protected EchoFileReaderNodeModel() {

		// TODO one incoming port and one outgoing port is assumed
		super(0, 2, true);
		addModelSetting(EchoFileReaderNodeModel.CFG_FILE_URL, createFileURL());
		addModelSetting(EchoFileReaderNodeModel.CFG_splitDestinationCol, createSplitDestinationCol());
		addModelSetting(EchoFileReaderNodeModel.CFG_splitSourceCol, createSplitSourceCol());
	}

	/**
	 * @return SettingsModel for Location
	 */
	private SettingsModel createFileURL() {
		return new SettingsModelString(CFG_FILE_URL, null);
	}

	/**
	 * @return SettingsModel if source column should be split
	 */
	private SettingsModel createSplitSourceCol() {
		return new SettingsModelBoolean(CFG_splitSourceCol, false);
	}

	/**
	 * @return SettingsModel if destination column should be split
	 */
	private SettingsModel createSplitDestinationCol() {
		return new SettingsModelBoolean(CFG_splitDestinationCol, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		/**
		 * read xml file
		 */
		String xml_file = null;
		if (getModelSetting(CFG_FILE_URL) != null){
			xml_file = ((SettingsModelString) getModelSetting(CFG_FILE_URL))
					.getStringValue();
		}
		
		/**
		 * SAX library implementation
		 */
		ParseXML handler = new ParseXML();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(xml_file, handler);

		/**
		 * Creation of First Table with Echo information
		 */	
		int nrColumns = 0;
		//check the settings and specify the number of columns in the table
		if(((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
				.getBooleanValue() && ((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol)).getBooleanValue() == true) {
			nrColumns = 17;
			//if both options of spliting columns are selected
		}
		else if (((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
				.getBooleanValue() == true) {
			nrColumns = 15; //split SourceWell selected
		} else if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
				.getBooleanValue() == true) {
			nrColumns = 15; //split DestinationWell selected
		} 
		else
		{
			nrColumns = 13; //no splitting choice selected
		}


		DataTableSpec colAttributes = getEchoColumnModel(); // call the list of names form getEchoColumnModel

		BufferedDataContainer buf = exec.createDataContainer(colAttributes);
		//create data container and take the attributes
		DataCell[] cells = new DataCell[nrColumns]; //create table with specify number of columns

		int counter =0;
		for (EchoRecord r : handler.getRecords()) {
			//get all values form parsed xml file
			cells[0] = new StringCell(r.getSrcPlateName());
			cells[1] = new StringCell(r.getSrcPlateBarcode());
			cells[2] = new StringCell(r.getSrcWell());
			cells[3] = new StringCell(r.getDestPlateName());
			cells[4] = new StringCell(r.getDestPlateBarcode());
			cells[5] = new StringCell(r.getDestWell());
			cells[6] = new StringCell(r.getXferVol());
			cells[7] = new StringCell(r.getActualVol());
			cells[8] = new StringCell(r.getCurrentFluidVolume());
			cells[9] = new StringCell(r.getFluidComposition());
			cells[10] = new StringCell(r.getFluidUnits());
			cells[11] = new StringCell(r.getFluidType());
			cells[12] = new StringCell(r.getXferStatus());
			/**
			 Addition of columns to the table, depends on the user selection
			 */

			int index =12;
			// number of columns depends on user settings - add 2 or 4 columns
			if (((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
					.getBooleanValue() == true) {
				int[] parts = splitPosition(r.getSrcWell());
				cells[index +1] = new IntCell(parts[0]);
				cells[index +2] = new IntCell(parts[1]);
				index = index + 2;
			}
			if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
					.getBooleanValue() == true) {
				int[] parts = splitPosition(r.getDestWell());
				cells[index +1] = new IntCell(parts[0]);
				cells[index +2] = new IntCell(parts[1]);
			}

			DataRow row = new DefaultRow("RowKey_" + counter, cells);
			buf.addRowToTable(row);
			counter++;
		}
		buf.close();
		BufferedDataTable table = buf.getTable();
		// first table contains data from Echo Reader xml

		/**
		 * Creation of Second Table with Echo METADATA
		 */
		int meta_nrColumns = 10;
		DataTableSpec colAttributes1 = getMetaDataColumnModel();
		BufferedDataContainer buf1 = exec.createDataContainer(colAttributes1);
		DataCell[] cells1 = new DataCell[meta_nrColumns];

		EchoReportHeader rh = handler.getReportHeader();
		EchoReportFooter rf = handler.getReportFooter();

		cells1[0] = new StringCell(rh.getRunID());
		cells1[1] = new StringCell(rh.getRunDateTime());
		cells1[2] = new StringCell(rh.getAppName());
		cells1[3] = new StringCell(rh.getAppVersion());
		cells1[4] = new StringCell(rh.getProtocolName());
		cells1[5] = new StringCell(rh.getUserName());
		cells1[6] = new StringCell(rf.getInstrName());
		cells1[7] = new StringCell(rf.getInstrModel());
		cells1[8] = new StringCell(rf.getInstrSN());
		cells1[9] = new StringCell(rf.getInstrSWVersion());
		DataRow row = new DefaultRow("RowKey_1", cells1);
		buf1.addRowToTable(row);

		buf1.close();
		BufferedDataTable table1 = buf1.getTable();
		// second table contains meta data (just one row)
		return new BufferedDataTable[]{table, table1}; 
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		//check if a file is set otherwise throw InvalidSettingsException (the node will not be executable until a file location is set)		
		String xml_file = null;
		if (getModelSetting(CFG_FILE_URL) != null){
			xml_file = ((SettingsModelString) getModelSetting(CFG_FILE_URL))
					.getStringValue();
		}
		else {
			throw new InvalidSettingsException("No input file selected");
		}
		if (xml_file == null) {
			throw new InvalidSettingsException("No location provided");
		}
		//Load the table with data from Echo xml file
		DataTableSpec colAttributes = getEchoColumnModel();
		DataTableSpec colAttributes1 = getMetaDataColumnModel();

		return new DataTableSpec[]{colAttributes,colAttributes1}; //create the table
	}

	/**
	 * @return output table spec for echo data
	 */
	private DataTableSpec getEchoColumnModel() {
		//create a new table spec and add columns according to xml file
		DataTableSpecCreator specCreator = new DataTableSpecCreator();
		DataColumnSpecCreator cspecCreator = null;

		cspecCreator = new DataColumnSpecCreator("Source Plate Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Source Plate Barcode", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Source Well", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Destination Plate Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Destination Plate Barcode", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Destination Well", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Transfer Volume", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Actual Volume", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Current Fluid Volume", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Fluid Composition", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Fluid Units", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Fluid Type", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Transfer Status", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());

		if (((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
				.getBooleanValue() == true) {
			cspecCreator = new DataColumnSpecCreator("plateRow (Source Well)", IntCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
			cspecCreator = new DataColumnSpecCreator("plateColumn (Source Well)", IntCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
		}
		if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
				.getBooleanValue() == true) {
			cspecCreator = new DataColumnSpecCreator("plateRow (Destination Well)", IntCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
			cspecCreator = new DataColumnSpecCreator("plateColumn (Destination Well)", IntCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());

		}

		return specCreator.createSpec();
	}

	/**
	 * @return output table spec for echo metadata
	 */
	private DataTableSpec getMetaDataColumnModel() {

		DataTableSpecCreator specCreator = new DataTableSpecCreator();
		DataColumnSpecCreator cspecCreator = null;

		cspecCreator = new DataColumnSpecCreator("Run ID", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Run Date/Time", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Application Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Application Version", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Protocol Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("User Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Instrument Name", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Instrument Model", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Instrument Serial Number", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());
		cspecCreator = new DataColumnSpecCreator("Instrument Software Version", StringCell.TYPE);
		specCreator.addColumns(cspecCreator.createSpec());

		return specCreator.createSpec();
	}

	/**
	 * Method to split Destination/Source column into two columns - regular expression split
	 * @param wellPosition
	 * @return vector with plateRow and plateColumn indices
	 * @throws CanceledExecutionException
	 */
	private static int[] splitPosition(String wellPosition) throws CanceledExecutionException {

		String regex = "([a-zA-Z]{1,2})([\\d]{1,2})"; 
		Matcher matcher = Pattern.compile(regex).matcher(wellPosition);
		int plateRow = 0;
		int plateColumn = 0;
		if(matcher.matches()) {
			plateRow = TdsUtils.mapPlateRowStringToNumber(matcher.group(1));
			plateColumn = (int) Double.parseDouble(matcher.group(2));
		}
		else throw new CanceledExecutionException("Not a valid well pattern: " + wellPosition);
		return new int[]{plateRow,plateColumn};
	}
}
