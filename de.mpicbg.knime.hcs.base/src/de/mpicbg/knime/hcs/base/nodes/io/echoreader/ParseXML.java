package de.mpicbg.knime.hcs.base.nodes.io.echoreader;

import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse echo xml
 * 
 * @author Magda Rucinska
 *
 */
public class ParseXML extends DefaultHandler {

	private StringBuffer buffer = new StringBuffer();
	
	private EchoRecord record;
	
	private EchoReportHeader reportheader;
	private EchoReportFooter reportfooter;
	private LinkedList<EchoRecord> recordList = new LinkedList<EchoRecord>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		buffer = new StringBuffer();

		if (localName.equals("report")) {
			new EchoRecord();
		} else if (qName.equals("reportheader")) {
			reportheader = new EchoReportHeader();
		} else if (qName.equals("record")) {
			record = new EchoRecord();
		} else if (qName.equals("reportfooter")) {
			reportfooter = new EchoReportFooter();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equals("RunID")) {
			 reportheader.setRunID(buffer.toString());
		 } else if (qName.equals("RunDateTime")) {
			 reportheader.setRunDateTime(buffer.toString());
		 } else if (qName.equals("AppName")) {
			 reportheader.setAppName(buffer.toString());
		 } else if (qName.equals("AppVersion")) {
			 reportheader.setAppVersion(buffer.toString());
		 } else if (qName.equals("ProtocolName")) {
			 reportheader.setProtocolName(buffer.toString());
		 } else if (qName.equals("UserName")) {
			 reportheader.setUserName(buffer.toString());
		 } else if (qName.equals("SrcPlateName")) {
			 record.setSrcPlateName(buffer.toString());
		 } else if (qName.equals("SrcPlateName")) {
			 record.setSrcPlateName(buffer.toString());
		 } else if (qName.equals("SrcPlateBarcode")) {
			 record.setSrcPlateBarcode(buffer.toString());
		 } else if (qName.equals("SrcWell")) {
			 record.setSrcWell(buffer.toString());
		 } else if (qName.equals("DestPlateName")) {
			 record.setDestPlateName(buffer.toString());
		 } else if (qName.equals("DestPlateBarcode")) {
			 record.setDestPlateBarcode(buffer.toString());
		 } else if (qName.equals("DestWell")) {
			 record.setDestWell(buffer.toString());
		 } else if (qName.equals("XferVol")) {
			 record.setXferVol(buffer.toString());
		 } else if (qName.equals("ActualVol")) {
			 record.setActualVol(buffer.toString());
		 } else if (qName.equals("CurrentFluidVolume")) {
			 record.setCurrentFluidVolume(buffer.toString());
		 } else if (qName.equals("FluidComposition")) {
			 record.setFluidComposition(buffer.toString());
		 } else if (qName.equals("FluidUnits")) {
			 record.setFluidUnits(buffer.toString());
		 } else if (qName.equals("FluidType")) {
			 record.setFluidType(buffer.toString());
		 } else if (qName.equals("XferStatus")) {
			 record.setXferStatus(buffer.toString());
		 } else if (qName.equals("InstrName")) {
			 reportfooter.setInstrName(buffer.toString());
		 } else if (qName.equals("InstrModel")) {
			 reportfooter.setInstrModel(buffer.toString());
		 } else if (qName.equals("InstrSN")) {
			 reportfooter.setInstrSN(buffer.toString());
		 } else if (qName.equals("InstrSWVersion")) {
			 reportfooter.setInstrSWVersion(buffer.toString());
		 } else if (qName.equals("record")) {
			 recordList.add(record);
		 }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void characters(char[] ch, int start, int length) {
		buffer.append(ch, start, length);
	}

	/**
	 * @return list of echo records
	 */
	public LinkedList<EchoRecord> getRecords() {
		return recordList;
	}

	/**
	 * @return echo header
	 */
	public EchoReportHeader getReportHeader() {
		return reportheader;
	}

	/**
	 * @return echo footer
	 */
	public EchoReportFooter getReportFooter() {
		return reportfooter;
	}

}
