/*
 * Created on Jan 16, 2006
 */
package de.mpicbg.knime.hcs.core.tools.resconverter;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.File;
import java.util.Arrays;
import java.util.List;


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


    public static void main(String[] args) {
        OperaResFileReader reader = new OperaResFileReader("/Users/brandl/Desktop/An_01_Me01_006GW100706B-KPIHgws(2010-07-30_18-43-54).res");
        System.err.println("barcode" + reader.getBarcode());
    }


    public OperaResFileReader(String resFile) {
        this.resFile = resFile;


        try {
            Builder parser = new Builder();
            Document doc = parser.build(new File(resFile));
//            System.out.println(doc.toXML());

            Element rootElement = doc.getRootElement();
            rootElement.getAttribute("Barcode");

            this.barcode = rootElement.getChildElements("Barcode").get(0).getValue();

            String measFile = rootElement.getChildElements("MeasurementFile").get(0).getValue();
            this.acquisitionDate = measFile.split("meas_0[\\d]*[(]")[1].trim().replace(").mea", "").replaceAll("$^", "");

            plate = new Plate();

            // read the well data
            Elements areaTags = rootElement.getChildElements("Areas").get(0).getChildElements("Area");
            for (int i = 0; i < areaTags.size(); i++) {
                Elements wellsTag = areaTags.get(i).getChildElements("Wells");

                // add all wells
                if (wellsTag.size() == 0) {
                    continue;
                }


                Elements wells = wellsTag.get(0).getChildElements("Well");

                for (int j = 0; j < wells.size(); j++) {
                    Element wellElement = wells.get(j);

                    int rowValue = Integer.parseInt(wellElement.getAttributeValue("row"));
                    int colValue = Integer.parseInt(wellElement.getAttributeValue("col"));

                    Well well = new Well(rowValue, colValue);
                    if (plate.getWell(colValue, rowValue) != null) {
                        throw new RuntimeException("plate exists" + rowValue + "," + colValue);
                    }

                    plate.addWell(well);

                    // add all its features
                    Elements wellResults = wellElement.getChildElements("Result");
                    for (int k = 0; k < wellResults.size(); k++) {
                        Element element = wellResults.get(k);
                        String readoutName = element.getAttributeValue("name");

                        String rawValue = element.getValue();
                        double readout = rawValue.equals("NAN") ? Double.NaN : Double.parseDouble(rawValue);

                        well.getWellStatistics().put(readoutName, readout);
                    }
                }
            }

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
}
