/*
 * Created on Jan 16, 2006
 */
package de.mpicbg.knime.hcs.core.tools.resconverter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.model.Plate;


/**
 * @author niederle
 */
public class OperaResFileReader {


    /**
     * Comment for <code>barcode</code> Barcode
     */
    private String barcode;


    /**
     * Comment for <code>resFile</code> Path of result file
     */
    private String resFile;

    private String acquisitionDate;

    private Plate plate;


    public OperaResFileReader(File inputFile) {
        this(inputFile.getAbsolutePath());
    }

    public OperaResFileReader(String resFile) {
        this.resFile = resFile;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
        	DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(resFile));
        	
/*
            //Element rootElement = doc.getDocumentElement();
            //rootElement.getAttribute("Barcode");

            this.barcode = doc.getElementsByTagName("Barcode").item(0).getTextContent();
            //this.barcode = rootElement.getChildElements("Barcode").get(0).getValue();

            String measFile = doc.getElementsByTagName("MeasurementFile").item(0).getTextContent();
            this.acquisitionDate = measFile.split("meas_0[\\d]*[(]")[1].trim().replace(").mea", "").replaceAll("$^", "");
         

            plate = new Plate();

            // read the well data
            
            NodeList areaTags = doc.getElementsByTagName("Areas").item(0).getChildNodes();
            for(int i = 0; i < areaTags.getLength(); i++) {
            	NodeList wellsTag = ((Element)areaTags.item(i)).getElementsByTagName("Wells");
            	
            	if(wellsTag.getLength() == 0)
            		continue;
            	
            	NodeList wells = ((Element)wellsTag.item(0)).getElementsByTagName("Well");
            	
            	for(int j = 0; j < wells.getLength(); j++) {
            		Element wellElement = (Element)wells.item(j);
            		
            		int rowValue = Integer.parseInt(wellElement.getAttribute("row"));
            		int colValue = Integer.parseInt(wellElement.getAttribute("col"));
            		
            		Well well = new Well(rowValue, colValue);
            		
            		if (plate.getWell(colValue, rowValue) != null) {
                        throw new RuntimeException("well already exists (row:" + rowValue + ", column: " + colValue + ")");
                    }
            		
            		plate.addWell(well);
            		
            		NodeList wellResults = wellElement.getElementsByTagName("Result");
            		
            		for(int k = 0; k < wellResults.getLength(); k++) {
            			Element result = ((Element) wellResults.item(k));
            			String readoutName = result.getAttribute("name");
            			String rawValue = result.getTextContent();
            			double readout = rawValue.equals("NAN") ? Double.NaN : Double.parseDouble(rawValue);
            			
            			well.getWellStatistics().put(readoutName, readout);
            		}
            	}
            }*/

        } catch (Throwable t) {
            throw new RuntimeException("Parsing of opera-file failed: ", t);
        }

    }


    public Plate getPlate() {
        return plate;
    }


    public float[][][] getResultValues() {
        //todo convert this to new appraoch
        return null;
    }


    public String getSourceFile() {
        return resFile;
    }


    public List<String> getResultParameters() {
        return TdsUtils.flattenReadoutNames(Arrays.asList(plate));
    }


    /**
     * @return Returns the barcode.
     */
    public String getBarcode() {
        return barcode;
    }


    public int getNumRows() {
        return plate.getNumRows();
    }


    public int getNumColumns() {
        return plate.getNumColumns();
    }


    public String getAcquisitionDate() {
        return acquisitionDate;
    }
    
    public static void main(String[] args) {
    	OperaResFileReader reader = new OperaResFileReader("/Volumes/tds/software+tools/Knime/Examples/HCS-tools-expample-data/OperaExampleData/An_01_Me01_009GW101105C-KPIHgws(2010-11-13_11-04-28).res");
    	
    }
}
