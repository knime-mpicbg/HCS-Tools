package de.mpicbg.knime.hcs.base.echofilereader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.xml.sax.helpers.DefaultHandler;

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
	public static final int IDsourceColumn = 2;
	public static final int IDdestinationColumn = 5;
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

	private SettingsModel createFileURL() {
		return new SettingsModelString(CFG_FILE_URL, null);
	}
	
	private SettingsModel createSplitSourceCol() {
		return new SettingsModelBoolean(CFG_splitSourceCol, false);
	}

	private SettingsModel createSplitDestinationCol() {
		return new SettingsModelBoolean(CFG_splitDestinationCol, false);
	}

	public static SettingsModelString createFileChooser() {
		return new SettingsModelString("input.files", "");
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
		if (getModelSetting(CFG_FILE_URL) == null) {
			throw new InvalidSettingsException("No input file selected");
		}
		//String loc = getModelSetting(CFG_FILE_URL).toString();
		if (xml_file.isEmpty() || xml_file.length() == 0) {
			throw new InvalidSettingsException("No location provided");
		}
		/**
		 * SAX library implementation
		 */
		DefaultHandler handler = new ParseXML();
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
		}
		 else if (((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
				.getBooleanValue() == true) {
			nrColumns = 15;
		} else if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
				.getBooleanValue() == true) {
			nrColumns = 15;
		} 
		else
		{
			nrColumns = 13;
		}

		
		DataTableSpec colAttributes = getEchoColumnModel(); // call the list of names form getEchoColumnModel

		BufferedDataContainer buf = exec.createDataContainer(colAttributes);
		//create data container and take the attributes
			DataCell[] cells = new DataCell[nrColumns]; //create table with specify number of columns
			setWarningMessage("size: " + EchoReportRecords.records.size());
			int counter =0;
			for (EchoReportRecords r : EchoReportRecords.records) {
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
				
				int index =12;
				// number of columns depends on user settings - add 2 or 4 columns
				if (((SettingsModelBoolean) getModelSetting(CFG_splitSourceCol))
						.getBooleanValue() == true) {
					String[] parts = splitPosition(r.getSrcWell());
					cells[index +1] = new StringCell(parts[0]);
					cells[index +2] = new StringCell(parts[1]);
					index = index + 2;
				}
				if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
						.getBooleanValue() == true) {
					String[] parts = splitPosition(r.getDestWell());
					cells[index +1] = new StringCell(parts[0]);
					cells[index +2] = new StringCell(parts[1]);
				}
			
		    	DataRow row = new DefaultRow("RowKey_"+counter, cells);
		    	buf.addRowToTable(row);
		    	counter++;
			}
			buf.close();
			BufferedDataTable table = buf.getTable();

			
			/**
			 * Creation of Second Table with Echo METADATA
			 */
			int meta_nrColumns = 10;
			DataTableSpec colAttributes1 = getMetaDataColumnModel();
			BufferedDataContainer buf1 = exec.createDataContainer(colAttributes1);
			DataCell[] cells1 = new DataCell[meta_nrColumns];
			
			setWarningMessage("size: " + EchoReportHeader.headers.size());
			
			int counter1= 0;
				for (EchoReportHeader rh : EchoReportHeader.headers) {
					
					cells1[0] = new StringCell(rh.getRunID());
					cells1[1] = new StringCell(rh.getRunDateTime());
					cells1[2] = new StringCell(rh.getAppName());
					cells1[3] = new StringCell(rh.getAppVersion());
					cells1[4] = new StringCell(rh.getProtocolName());
					cells1[5] = new StringCell(rh.getUserName());
					
				}
				for (EchoReportFooter rf : EchoReportFooter.footers) {
					
					cells1[6] = new StringCell(rf.getInstrName());
					cells1[7] = new StringCell(rf.getInstrModel());
					cells1[8] = new StringCell(rf.getInstrSN());
					cells1[9] = new StringCell(rf.getInstrSWVersion());
					DataRow row = new DefaultRow("RowKey_" + counter1, cells1);
			    	buf1.addRowToTable(row);
			    	counter1++;
				}
				
				buf1.close();
				BufferedDataTable table1 = buf1.getTable();
				
				
			return new BufferedDataTable[]{table, table1}; 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		
		


		DataTableSpec colAttributes = getEchoColumnModel();
		DataTableSpec colAttributes1 = getMetaDataColumnModel();
		
		/*DataTableSpec[] spec = new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)};
		DataTableSpec[] spec1 = new DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes1)};*/
		
		return new DataTableSpec[]{colAttributes,colAttributes1};// DataTableSpec[]{AttributeUtils.compileTableSpecs(colAttributes)}; //create the table
		
	}
	

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
			cspecCreator = new DataColumnSpecCreator("plateRow (Source Well)", StringCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
			cspecCreator = new DataColumnSpecCreator("plateColumn (Source Well)", StringCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
		
		}
		if(((SettingsModelBoolean) getModelSetting(CFG_splitDestinationCol))
				.getBooleanValue() == true) {
			cspecCreator = new DataColumnSpecCreator("plateRow (Destination Well)", StringCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
			cspecCreator = new DataColumnSpecCreator("plateColumn (Destination Well)", StringCell.TYPE);
			specCreator.addColumns(cspecCreator.createSpec());
			
		}
		
		return specCreator.createSpec();
	}
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
	
private static String[] splitPosition(String wellPosition) {
				
				//String regex = "([a-zA-Z])";
				String regex = "(?<=\\d)(?=\\p{L})|(?<=\\p{L})(?)"; 
				String[] parts = wellPosition.split(regex);
				
				
				return parts;
				}
}