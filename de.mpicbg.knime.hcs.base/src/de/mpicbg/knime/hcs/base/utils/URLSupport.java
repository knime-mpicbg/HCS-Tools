/**
 * 
 */
package de.mpicbg.knime.hcs.base.utils;

import java.io.IOException;
import java.io.InputStream;
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
	public URLSupport(String filename) throws URISyntaxException, IOException {
		this.connection = null;
    	//check if URL can be resolved
    	// retrieve protocol
    	String protocol = (new URI(filename)).getScheme();
    	// try to add file-protocol to treat it as a local file
    	if(protocol == null) filename = "file://" + filename;
    		
		URL fileURL = new URL(filename);
		this.connection = fileURL.openConnection();
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

}
