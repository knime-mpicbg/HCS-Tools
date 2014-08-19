package de.mpicbg.knime.hcs.base.nodes.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ExDataSet {

    private final URL rootURL;

    private final String exampleName;

    private final String description;

    private final String fileName;


    public ExDataSet(String exampleName, URL rootURL, String description, String fileName) {
        super();
        this.rootURL = rootURL;
        this.description = description;
        this.fileName = fileName;
        this.exampleName = exampleName;
    }


    public String getExampleName() {
        return exampleName;
    }


    public String getDescription() {
        return description;
    }


    public String getFileName() {
        return fileName;
    }


    public String getFileURL() {
        String rootLocation = rootURL.toString().substring(0, rootURL.toString().lastIndexOf("/") + 1);

        return rootLocation + getFileName().trim();
    }


    @Override
    public String toString() {
        return getExampleName();
    }


    public static void main(String[] args) throws MalformedURLException {
        List<ExDataSet> exDataSets = parseExampleList(new URL("file:///Users/brandl/projects/knime/knimehelpers/testdata/somedata.csv"));
        System.err.println(exDataSets);
    }


    public static List<ExDataSet> parseExampleList(URL listLocationURL) {
        List<ExDataSet> exDataSets = new ArrayList<ExDataSet>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(listLocationURL.openStream()));

            StringBuffer templateBuffer = new StringBuffer();

            for (String line = reader.readLine() ; line != null ; line = reader.readLine()) {
            	String[] splitLine = line.split(";");

                if (splitLine.length != 3)
                    continue;

                exDataSets.add(new ExDataSet(splitLine[0], listLocationURL, splitLine[1], splitLine[2]));
            }
            
            
            /*while (reader.ready()) {
                String line = reader.readLine();
                String[] splitLine = line.split(";");

                if (splitLine.length != 3)
                    continue;

                exDataSets.add(new ExDataSet(splitLine[0], listLocationURL, splitLine[1], splitLine[2]));
            }*/

        } catch (IOException e) {
            throw new RuntimeException("Could not read exListLocation");
        }

        return exDataSets;
    }
}
