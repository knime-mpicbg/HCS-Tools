package de.mpicbg.knime.hcs.core;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Some little function which are required from times to times for file-handling purposes
 *
 * @author Holger Brandl
 */
public class FileUtils {

    /**
     * Reads and verifies a driver file.
     */
    public static List<File> readDriver(String fileName) {

        File inputFile = new File(fileName);
        assert inputFile.isFile();

        List<File> driverFiles = null;

        try {
            if (!inputFile.isFile() || !inputFile.canRead())
                throw new IllegalArgumentException("file to read is not valid");

            BufferedReader bf = new BufferedReader(new FileReader(inputFile));

            driverFiles = new ArrayList<File>();

            String line;
            while ((line = bf.readLine()) != null && line.trim().length() != 0) {
                File file = new File(line);

                assert file.isFile() : "file " + file + " does not exist!";
                driverFiles.add(file);
            }

            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert driverFiles != null;
        return driverFiles;

    }


    /**
     * Given an array of files this method creates a temporal driver which will be removed as the application finishs.
     */
    public static File createTmpDriver(File[] files) {
        ArrayList<File> listedFiles = new ArrayList<File>();
        for (File file : files) {
            listedFiles.add(file);
        }

        return createTmpDriver(listedFiles);
    }


    /**
     * Given a list of files this method creates a temporal driver which will be removed as the application finishs.
     */
    public static File createTmpDriver(List<File> audioFiles) {
        File tmpDriverFile = null;

        try {
            tmpDriverFile = File.createTempFile("azubiTmpDriver", ".drv");
            writeDriver(tmpDriverFile, audioFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert tmpDriverFile != null;
        return tmpDriverFile;
    }


    public static void writeDriver(File driverFile, List<File> audioFiles) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(driverFile));

            for (File file : audioFiles) {
                bw.write(file.getPath());
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns all files which contains the string <code>s</code> in their filename.
     */
    public static List<File> driverSubSet(List<File> files, String s) {
        assert s != null;
        List<File> filteredFiles = new ArrayList<File>();
        for (File file : files) {
            if (file.getPath().contains(s))
                filteredFiles.add(file);
        }

        return filteredFiles;
    }


    /**
     * Attempts to find all wav-files within a directory. This also includes sub-directories.
     */
    public static List<File> findFilesBySuffix(File directory, String suffix) {
        if (!directory.isDirectory()) {
            throw new RuntimeException("the given directory name '" + directory.getAbsolutePath() + "' does not point to a directory");
        }

        return findFiles(directory, suffix);
    }


    /**
     * Recursively parses a directory and put all files in the return list which match ALL given filters.
     */
    public static List<File> findFiles(File directory, String... filters) {
        return findFiles(directory, true, filters);
    }


    /**
     * Collects all files list which match ALL given filters.
     *
     * @param directory   the base directory for the search
     * @param beRecursive
     * @param filters     strings that must match to the files to be found
     * @return
     */
    public static List<File> findFiles(File directory, boolean beRecursive, String... filters) {
        assert directory.isDirectory();

        List<File> allFiles = new ArrayList<File>();

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (beRecursive)
                    allFiles.addAll(findFiles(file, filters));
            } else {
                if (!file.isHidden()) {
                    allFiles.add(file);
                }
            }
        }

        List<File> foundFiles = filters.length == 0 ? allFiles : filterFiles(allFiles, filters);
        Collections.sort(foundFiles);

        return foundFiles;
    }


    /**
     * Returns the list of files which match ALL of the given set of filters.
     */
    public static List<File> filterFiles(List<File> files, String... filters) {
        List<File> filteredFiles = new ArrayList<File>();
        for (File file : files) {
            boolean isValid = true;
            for (String filter : filters) {
                if (!file.getPath().contains(filter)) {
                    isValid = false;
                    break;
                }

            }

            if (isValid)
                filteredFiles.add(file);
        }

        return filteredFiles;
    }


    /**
     * Splits a list of files into two subsets given a ratio of size (first subset)/size(secnd subset).
     */
    public static List<File>[] splitList(List<File> files, double ratio) {
        int fstSubSize = (int) Math.ceil(files.size() * ratio);
        List<File>[] splitList = new List[2];

        splitList[0] = files.subList(0, fstSubSize);
        splitList[1] = files.subList(fstSubSize, files.size());

        return splitList;
    }

}
