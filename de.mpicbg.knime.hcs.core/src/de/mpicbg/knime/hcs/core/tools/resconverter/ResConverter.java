package de.mpicbg.knime.hcs.core.tools.resconverter;


import de.mpicbg.knime.hcs.core.FileUtils;

import java.io.File;
import java.util.List;


/**
 * Scans a directory structure for opera res-files and converts them into csv.
 *
 * @author Holger Brandl
 */
public class ResConverter {

    public static final String RES_SUFFIX = "res";


    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ResConverter <inputDir>");
            return;
        }

        String inputDir = args[0];

//        File outputdir = new File(args[1]);

        List<File> operaResFiles = FileUtils.findFilesBySuffix(new File(inputDir), RES_SUFFIX);

        for (File operaResFile : operaResFiles) {
            convertFileToCsv(operaResFile.getAbsolutePath());
        }
    }


    public static void convertFileToCsv(String resFile) {
        if (resFile == null) {
            throw new RuntimeException("file should never be null");
        }

        try {
            OperaResFileReader data = new OperaResFileReader(resFile);

            OperaDataCsvWriter writer = new OperaDataCsvWriter(data);

            String outputFileName = resFile.replace("." + RES_SUFFIX, ".txt");

            try {
                writer.setOutputFile(outputFileName, true);
            } catch (Exception e) {
                System.out.println("File can't be created");
            }
            writer.writeRawData();

        } catch (Throwable e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
