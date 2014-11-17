/**
 * 
 */
package de.mpicbg.knime.hcs.base.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author niederle
 * 
 * these methods are meant to support the KNIME-protocol for URLs
 * it's important to place this class within KNIME, as the KNIME-protocol is unknown otherwise
 * and will take care of interpreting local style as file://
 *
 */
public class URLSupport {
	
	/**
	 * URL connection
	 */
	private URLConnection connection;
	
	/**
	 * default constructor
	 */
	public URLSupport() {
		this.connection = null;		
	}
	
	/**
	 * tries to create a URL connection by a given file name
	 * it adds file:// if the protocol is not given to treat it as local
	 * @param filename
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public URLSupport(String filename) throws IOException {
		this.connection = null;    		
		URL fileURL = textToURL(filename);
		this.connection = fileURL.openConnection();
	}
	
    /**
     * Tries to create an URL from the passed string. (from KNIME - FileReader node, extended by test of absolute path)
     *
     * @param urlString the string to transform into an URL
     * @return URL if entered value could be properly transformed, or
     * @throws MalformedURLException if the value passed was invalid
     */
    static URL textToURL(final String urlString) throws MalformedURLException {

        if ((urlString == null) || (urlString.equals(""))) {
            throw new MalformedURLException("Specify a not empty valid URL");
        }

        URL newURL;
        try {
            newURL = new URL(urlString);
        } catch (MalformedURLException e) {
            // see if they specified a file without giving the protocol (only absolute path possible)
            File tmp = new File(urlString);
            // if not absolute, raise given exception again
            if(!tmp.isAbsolute()) throw new MalformedURLException(e.getMessage());
            // if that blows off we let the exception go up the stack.
            newURL = tmp.getAbsoluteFile().toURI().toURL();
        }
        System.out.println("given: " + urlString + "\nurl:" + newURL.toString());
        return newURL;
    }
	
	/**
	 * @return timestamp of the last modification time
	 */
    public long getTimestamp() {
    	return this.connection.getLastModified();
    }
    
    /**
     * 
     * @return InputStream of this URL connection
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
    	if(this.connection == null) return null;
    	return this.connection.getInputStream();
    }

    public static void main(String[] args) {
    	String path1 = "C:\\test\\test.csv";	//Windows (non-url-style)
    	String path2 = "file:/C:/test/test.csv"; //Windows (url-style)
    	String path3 = "knime://knime.workflow/test.csv";	// knime protocol
    	String path4 = "something://idontknow/file.txt";	// non-valid protocol
    	String path5 = "/Users/itsme/test.txt";		//MacOS (non-url-style)
    	String path6 = "file:/Users/itsme/test.txt";	//MacOS (url-style)
    	
    	try {
			new URLSupport(path1);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			new URLSupport(path2);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			new URLSupport(path3);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			new URLSupport(path4);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			new URLSupport(path5);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	try {
			new URLSupport(path6);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
}
