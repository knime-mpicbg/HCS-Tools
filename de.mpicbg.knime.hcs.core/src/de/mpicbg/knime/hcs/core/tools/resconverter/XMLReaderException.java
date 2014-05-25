package de.mpicbg.knime.hcs.core.tools.resconverter;

/**
 * Exceptions raised by XMLReader will be instances of this class.
 *
 * @author Antje Niederlein
 * @version 1.0.0
 */

public class XMLReaderException extends Exception {

    /**
     * Class constructor
     */
    public XMLReaderException() {
    }


    /**
     * Class constructor setting a user defined exception message
     *
     * @param error exception message
     */
    public XMLReaderException(String error) {
        super(error);
    }


    /**
     * Method returns the information, that this exception ist an XML Reader Error
     *
     * @return the information "XML Reader Error"
     */
    public String getLocalizedMessage() {
        return "XML Reader Error";
    }
}