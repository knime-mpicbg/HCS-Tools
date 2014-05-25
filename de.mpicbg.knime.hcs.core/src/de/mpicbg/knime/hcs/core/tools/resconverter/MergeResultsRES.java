/*
 * Created on Jan 16, 2006
 */
package de.mpicbg.knime.hcs.core.tools.resconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author niederle
 */
public class MergeResultsRES {

    XMLReader xmlFile;
    String summaryRes;
    OperaAnalysisDataMerge data;


    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println("---------------------------------------");
        ResConverter.convertFileToCsv(args[0]);
    }


    public MergeResultsRES(String[] args, String newBarcode) {
        super();

        try {
            xmlFile = new XMLReader();
            xmlFile.Read(new FileInputStream(args[0]));
            summaryRes = args[0].substring(0, args[0].lastIndexOf(File.separator));
            summaryRes = summaryRes.substring(0, summaryRes.lastIndexOf(File.separator));
            summaryRes = summaryRes.substring(0, summaryRes.lastIndexOf(File.separator)) + File.separator + "summaryRes.txt";
            if (args[0] != null) {
                data = new OperaAnalysisDataMerge(xmlFile, args[0], newBarcode);
            }
            String fn = data.getResFileName();
            System.out.println("Merge file: " + fn);
            if (data.getMacroscript() != null && data.getMacroscript().length() > 1) {
                data.createFile(summaryRes, true);
                data.writeRawData();
            }
        } catch (XMLReaderException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
