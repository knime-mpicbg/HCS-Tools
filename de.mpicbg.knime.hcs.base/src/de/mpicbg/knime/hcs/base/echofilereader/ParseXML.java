package de.mpicbg.knime.hcs.base.echofilereader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mpicbg.knime.hcs.base.echofilereader.EchoReportRecords;
import de.mpicbg.knime.hcs.base.echofilereader.EchoReportFooter;
import de.mpicbg.knime.hcs.base.echofilereader.EchoReportHeader;



public class ParseXML extends DefaultHandler{

	private StringBuffer buffer = new StringBuffer();
	private EchoReportRecords report;
	private EchoReportHeader reportheader;
	public EchoReportRecords record;
	public EchoReportFooter reportfooter;
	private  String value = "test";
	
	
	
	
	
	 
	 /*public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
   

    SAXParserFactory factory = SAXParserFactory.newInstance();

factory.setValidating(false);

SAXParser parser = factory.newSAXParser();
parser.parse("/Users/rucinska/Documents/echo files/E5XX-1107_Print_1314172473.xml", handler);
//parser.parse(xml_file, handler);
		
	    for (EchoReportHeader rh : EchoReportHeader.headers) {

			System.out.println(rh.getRunID());
	        System.out.println(rh.getAppVersion());
	        System.out.println(rh.getProtocolName());
	        System.out.println(rh.getRunDateTime());
	        System.out.println(rh.getAppName());

		 }
		for (EchoReportRecords r : EchoReportRecords.records) {

				System.out.println(r.getSrcPlateName());
	            System.out.println(r.getSrcPlateBarcode());
	            System.out.println(r.getSrcWell());
	            System.out.println(r.getDestPlateName());
	            System.out.println(r.getDestPlateBarcode());
	            System.out.println(r.getDestWell());
	            System.out.println(r.getXferVol());
	            System.out.println(r.getActualVol());
	            System.out.println(r.getCurrentFluidVolume());
	            System.out.println(r.getFluidComposition());
	            System.out.println(r.getFluidUnits());
	            System.out.println(r.getFluidType());
	            System.out.println(r.getXferStatus());

				}
		
			for (EchoReportFooter rf : EchoReportFooter.footers) {

				System.out.println(rf.getInstrName());
				System.out.println(rf.getInstrModel());
				System.out.println(rf.getInstrSN());
				System.out.println(rf.getInstrSWVersion());

				}
}*/

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		int length = attributes.getLength();
		
		for (int i=0; i<length; i++) {	
			value = attributes.getValue(i);
			//System.out.println( value );

		}		

		buffer.setLength(0);

		if (localName.equals("report")) {

			report = new EchoReportRecords();
		}
		else if (qName.equals("reportheader")) {

			reportheader = new EchoReportHeader();
		}
		else if (qName.equals("record")) {

			record = new EchoReportRecords();
		}
		else if (qName.equals("reportfooter")) {

			reportfooter = new EchoReportFooter();

		}


	}
	@Override

	public void endElement(String uri, String localName, String qName)throws SAXException {


		if (qName.equals("report")) {
			EchoReportHeader.headers.add(reportheader);
			EchoReportFooter.footers.add(reportfooter);

		}
	


		else if (qName.equals("RunID")) {

			reportheader.setRunID(buffer.toString());
		}
		else if (qName.equals("RunDateTime")) {
			reportheader.setRunDateTime(buffer.toString());

		}
		else if (qName.equals("AppName")) {
			reportheader.setAppName(buffer.toString());

		}
		else if (qName.equals("AppVersion")) {
			reportheader.setAppVersion(buffer.toString());

		}
		else if (qName.equals("ProtocolName")) {
			reportheader.setProtocolName(buffer.toString());

		}
		else if (qName.equals("UserName")) {

			reportheader.setUserName(buffer.toString());
		}
		else if (qName.equals("SrcPlateName")) {

			record.setSrcPlateName(buffer.toString());
		}
		else if (qName.equals("SrcPlateName")) {

			record.setSrcPlateName(buffer.toString());
		}
		else if (qName.equals("SrcPlateBarcode")) {

			record.setSrcPlateBarcode(buffer.toString());
		}
		else if (qName.equals("SrcWell")) {

			record.setSrcWell(buffer.toString());
		} 
		else if (qName.equals("DestPlateName")) {

			record.setDestPlateName(buffer.toString());
		}
		else if (qName.equals("DestPlateBarcode")) {

			record.setDestPlateBarcode(buffer.toString());
		}
		else if (qName.equals("DestWell")) {

			record.setDestWell(buffer.toString());
		}
		else if (qName.equals("XferVol")) {

			record.setXferVol(buffer.toString());
		}
		else if (qName.equals("ActualVol")) {

			record.setActualVol(buffer.toString());
		}
		else if (qName.equals("CurrentFluidVolume")) {

			record.setCurrentFluidVolume(buffer.toString());
		}
		else if (qName.equals("FluidComposition")) {

			record.setFluidComposition(buffer.toString());
		}
		else if (qName.equals("FluidUnits")) {

			record.setFluidUnits(buffer.toString());
		}
		else if (qName.equals("FluidType")) {

			record.setFluidType(buffer.toString());
		}
		else if (qName.equals("XferStatus")) {

			record.setXferStatus(buffer.toString());
		}

		else if (qName.equals("InstrName")) {
			reportfooter.setInstrName(buffer.toString());

		}
		else if (qName.equals("InstrModel")) {
			reportfooter.setInstrModel(buffer.toString());

		}
		else if (qName.equals("InstrSN")) {
			reportfooter.setInstrSN(buffer.toString());

		}
		else if (qName.equals("InstrSWVersion")) {
			reportfooter.setInstrSWVersion(buffer.toString());

		}
		else if (qName.equals("record")) {
			EchoReportRecords.records.add(record);

		}

	}
	@Override
	public void characters(char[] ch, int start, int length) {
		buffer.append(ch, start, length);

	}
	
	 public String getValue(){
			return value;
		}

}
