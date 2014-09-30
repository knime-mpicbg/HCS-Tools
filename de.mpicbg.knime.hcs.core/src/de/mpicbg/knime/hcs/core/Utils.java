package de.mpicbg.knime.hcs.core;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;


/**
 * Some small static utility methods used spread over the azubi codebase.
 *
 * @author Holger Brandl
 */
public class Utils {


    /**
     * Returns a logger for the class-namespace. Just a kind of code-shortener.
     */
    public static Logger getLogger(Object o) {
        return Logger.getLogger(o.getClass().getName());
    }


    /**
     * Try to determine whether this application is running under Windows or some other platform by examing the
     * "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith("Windows");
    }


    /**
     * Try to determine whether this application is running under macos
     *
     * @return true if this application is running under a mac OS
     */
    public static boolean isMacOSPlatform() {
        return (System.getProperty("mrj.version") != null);
    }


    /**
     * Tries to extract a resource from the default loactions and a set of optional locations. Additional search dirs
     * will be searched first.
     *
     * @return <code>null</code> if the ressource was not found.
     */
    public static URL getRessource(String resourceName, File... additionalSearchDirs) {
        URL ressourceURL = null;


        for (File file : additionalSearchDirs) {
            assert file.isDirectory();

            File resFile = new File(file.getAbsolutePath() + File.separator + resourceName);
            if (resFile.isFile()) {
                try {
                    ressourceURL = resFile.toURI().toURL();
                    break;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }


        if (ressourceURL == null)
            ressourceURL = getRessource(resourceName);

        return ressourceURL;
    }


    /**
     * Tries to extract a resource by applying the following search order : the current directory, the parent directory
     * or from a the classpath .
     *
     * @return <code>null</code> if the ressource was not found.
     */
    public static URL getRessource(String resourceName) {
        URL ressourceURL = null;

        try {
            if (new File(resourceName).isFile()) {
                ressourceURL = new File(resourceName).toURI().toURL();
            } else if (new File("../" + resourceName).isFile()) {
                ressourceURL = new File("../" + resourceName).toURI().toURL();
            } else {
                URL resource = Utils.class.getResource("/" + resourceName);

                if (resource != null && resource.getContent() != null)
                    ressourceURL = resource;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ressourceURL;
    }


    public static void copyFile(File inFile, File outFile) {
        try {
            assert inFile.isFile();
            assert outFile.getParentFile().isDirectory();

            InputStream is = new FileInputStream(inFile);
            OutputStream os = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyToDir(File inFile, File outDirectory) {
        copyFile(inFile, new File(outDirectory.getAbsolutePath() + File.separator + inFile.getName()));
    }


    public static void moveFile(File inFile, File targetFile) {
        copyFile(inFile, targetFile);
        inFile.delete();
    }


    public static URL getURL(String fileName) {
        File f = new File(fileName);
        assert f.isFile();
        return getURL(f);
    }


    public static URL getURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

//  we need to add PrivilegedAccessor to the classpath in order to use this ugly (but sometimes necessary
//  bytcode hacking)
//
//    public static Object getField(Object o, String fieldName) {
//        try {
//            return PA.getValue(o, fieldName);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//
//        throw new RuntimeException();
//    }
//
//
//    public static void setField(Object o, String fieldName, Object targetValue) {
//        try {
//            PA.setValue(o, fieldName, targetValue);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//    }


    public static Window getOwnerDialog(Container awtOwner) {
        while (awtOwner != null && !(awtOwner instanceof JDialog || awtOwner instanceof JFrame)) {
            awtOwner = awtOwner.getParent();
        }

        return (Window) awtOwner;
    }


    /**
     * Redirects standard out into file
     *
     * @param destFile File which should be used for all stdout(put)
     * @return The old stdout stream
     */
    private PrintStream redirectStdout(File destFile) throws IOException {
        File resultFile = File.createTempFile(destFile.getAbsolutePath(), "");

        FileOutputStream fos = new FileOutputStream(resultFile);
        PrintStream ps = new PrintStream(fos);
        PrintStream oldOutStream = System.out;
        System.setOut(ps);

        return oldOutStream;
    }


    public static List<String> execAndOutexec(String cmd, boolean waitFor, boolean showOutput) {
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // any output?
        assert proc != null;
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), showOutput ? "ERROR" : null);
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), showOutput ? "OUTPUT" : null);

        // kick them off
        errorGobbler.start();
        outputGobbler.start();

        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("exec failed for cmd: " + cmd);
            e.printStackTrace();
        }

        return outputGobbler.getOutput();
    }


    /**
     * Wraps the ugly invocation of a system command using Runtime.exec().
     */
    public static int exec(String cmd, boolean waitFor, boolean showOutput) {
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // any output?
        assert proc != null;
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), showOutput ? "ERROR" : null);
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), showOutput ? "OUTPUT" : null);

        // kick them off
        errorGobbler.start();
        outputGobbler.start();

        // any error???
        if (waitFor)
            try {
                return proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        else
            return 0;

        throw new RuntimeException("exec failed for cmd: " + cmd);
    }


    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }


    // we need to add xstream to use this utility-functions
//
//    /** Serializes an object with XStream. */
//    public static void save2XML(File dumpFile, Object o) {
//        assert o != null;
//        try {
//            new XStream().toXML(o, new FileWriter(dumpFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /** Derializes an object with XStream. */
//    public static Object fromXML(File serialzedObjectFile) {
//        try {
//            return new XStream().fromXML(new FileReader(serialzedObjectFile));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }


    public static void saveBinary(File dumpFile, Object object) {
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(dumpFile));
            objStream.writeObject(object);
            objStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Object loadBinary(File dumpFile) {
        try {
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(dumpFile));
            return inStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    public static <O> Collection<O> randSubSubset(Collection<O> c) {
        Random r = new Random();
        int size = r.nextInt(c.size() - 1) + 1; // ensure that not empty
        List<O> wrappedCol = new ArrayList<O>(c);
        HashSet<O> subco = new HashSet<O>();

        while (size > 0) {
            size--;
            subco.add(wrappedCol.remove(r.nextInt(wrappedCol.size())));
        }

        return subco;
    }


    /**
     * Converts a string array into a space separated string.
     */
    public static String makeStringFromArgs(String[] args) {
        StringBuffer sb = new StringBuffer();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }

        return sb.toString().trim();
    }


    public static void saveText2File(String s, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String readTextFile(File file) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            sb.append(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * If the given {@code value} is  an instance of {@code Long} or {@code Integer} or {@code Float }it becomes
     * converted to double.
     */
    public static Object convertNumbers2Double(Object value) {
        if (value instanceof Integer)
            value = ((Integer) value).doubleValue();

        if (value instanceof Float)
            value = ((Float) value).doubleValue();

        if (value instanceof Long)
            value = ((Long) value).doubleValue();

        return value;
    }
}


class StreamGobbler extends Thread {

    InputStream is;
    String type;
    List<String> output = new ArrayList<String>();


    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }


    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);

                if (type != null)
                    System.out.println(type + ">" + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public List<String> getOutput() {
        return output;
    }
}

