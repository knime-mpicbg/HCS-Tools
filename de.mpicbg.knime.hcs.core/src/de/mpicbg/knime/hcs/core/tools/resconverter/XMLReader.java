package de.mpicbg.knime.hcs.core.tools.resconverter;



//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

/* Test-Application */
/* --------------------------------------------------------------------------- */
/*class TestApplication
{
	public static void main (String[] args)
	{
		if(args.length > 0)
		{
		    System.out.println("Dateiname: " + args[0]);
            try
            {   XMLReader myReader = new XMLReader();
            	try
            	{
            		FileInputStream file = new FileInputStream(args[0]);
            		myReader.Read(file);
                	file.close();
            	}
            	catch(IOException e){return;}

                myReader.ShowTree();
                //System.out.println(myReader.getNumberOfElement("answer", true));
                //System.out.println(myReader.getNumberOfElement("atext"));
                //myReader.setCurrentChildNode("answer",0, false);
                //System.out.println(myReader.getNumberOfElement("atext", true));
                //System.out.println(myReader.getNumberOfElement("text"));
                //System.out.println(">" + myReader.getValueOfElement("question", 0, false) + "<");
                //System.out.println(">" + myReader.getValueOfElement("answer", 2, true) + "<");
                //System.out.println(">" + myReader.getValueOfAttribute("answer", "value", 1, false) + "<");
                //System.out.println("<<< " + myReader.getValueOfElement("text", 1, false));
            }
            catch(XMLReaderException e)
            {
                System.out.println(e.toString());
                System.out.println(e.getMessage());
            }
        }
	}
}*/


/**
 * Instances of the class XMLReader are able to read in XML-files.<br> The parsing process is done by Document Object
 * Model (DOM). The information are provided as a document tree. There are different methods available to extract
 * information from the tree
 *
 * @author Antje Niederlein
 * @version 1.0.0
 */
//public class XMLReader {
//
//    private DocumentBuilderFactory factory;            // needed to parse the XML-file
//    private DocumentBuilder builder;                // needed to parse the XML-file
//    private Document doc;                            // document tree consisting of Nodes (Class Node)
//    private int depth;                                // depth of current element (for ShowTree)
//    private ArrayList nodelist = new ArrayList();    // current NodeList (set by FillNodeList)
//    private Node currChild;                            // parent Node of nodelist
//
//
//    /**
//     * Class constructor
//     *
//     * @throws XMLReaderException if the XML-parser could not be initialized
//     * @see XMLReaderException
//     */
//    public XMLReader() throws XMLReaderException {
//        // Abruf eines Factory-Exemplars
//        factory = DocumentBuilderFactory.newInstance();
//        depth = 0;
//        currChild = null;
//        // Erzeugung des Parsers ueber diese Factory
//        try {
//            builder = factory.newDocumentBuilder();
//        }
//        catch (ParserConfigurationException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//    }
//
//
//    /**
//     * Creates the document tree of a given XML-file
//     *
//     * @param file the XML-file to be parsed
//     * @throws XMLReaderException If the file could not be parsed
//     * @see XMLReaderException
//     */
//    public void Read(InputStream file) throws XMLReaderException {
//        // DOM-Baum erzeugen
//        try {
//            doc = builder.parse(file);
//        }
//        catch (SAXException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//        catch (IOException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//    }
//
//
//    /**
//     * Shows the information contained by the document tree on StdOut. Values and attributes are omitted.
//     */
//    public void ShowTree() {
//        ShowElement(doc);
//    }
//
//
//    /**
//     * Shows the information a special node of the document tree contains. Recursive Method.
//     *
//     * @param n node, from which the information should be extracted
//     */
//    private void ShowElement(Node n) {
//        String indent = "";
//        int i;
//        // Make some indent signs
//        for (i = 0; i < depth; i++) indent = indent + '\t';
//
//        // Extract node name and if node has any attributes
//        System.out.println(indent + n.getNodeName());
//        System.out.println(indent + "Attributes: " + n.hasAttributes());
//
//        // If there are any child nodes, show them
//        if (n.hasChildNodes()) {
//            NodeList nl = n.getChildNodes();
//            int j = nl.getLength();
//            for (i = 0; i < j; i++) {
//                depth++;
//                ShowElement(nl.item(i));
//            }
//        }
//        depth--;
//    }
//
//
//    /**
//     * Check wether the string has any printable characters.
//     *
//     * @param t String to be checked
//     * @return returns false if the string only consists of whitespaces.
//     */
//    private boolean IsPrintable(String t) {
//        int i;
//        for (i = 0; i < t.length(); i++) {
//            if (Character.isWhitespace(t.charAt(i)) == false) return true;
//        }
//        return false;
//    }
//
//
//    /**
//     * Searchs and stores a specific element of the document tree for extraction further information of its children. Be
//     * careful: If there is no child matching the position the old current child will remain.
//     *
//     * @param name   name of the element
//     * @param idx    index of the element with the specific name beginning with index 0 (example: second element named
//     *               "MyElement" --> idx = 1)
//     * @param parent do you want to search under the root (true) or under the current child (set by a further call of
//     *               setCurrentChildNode
//     */
//    public void setCurrentChildNode(String name, int idx, boolean parent) {
//        Node pNode = null;
//        if (!parent || currChild == null) pNode = doc;
//        else pNode = currChild;
//
//        // fills the list with all children named as searched of the chosen parent-node (parameter parent)
//        FillNodeList(name, pNode);
//
//        // sets the current child to the child specified by idx (does it exist?)
//        currChild = null;
//        try {
//            currChild = (Node) nodelist.get(idx);
//        }
//        catch (IndexOutOfBoundsException e) {
//        }
//    }
//
//
//    /**
//     * Set the parent element of the document tree for extraction further information of its children.
//     */
//    public void setParentAsChildNode() {
//        Node pNode = null;
//        if (currChild == null) pNode = doc;
//        else pNode = currChild.getParentNode();
//        if (pNode == null) pNode = doc;
//
//        currChild = pNode;
//    }
//
//
//    public String getCurrentChildNode() {
//        if (currChild == null) return "null";
//        return currChild.getNodeName();
//    }
//
//
//    /**
//     * Creates a list with all nodes of the same node name
//     *
//     * @param name   name of the element
//     * @param parent parent node which should contain children named by the specified name
//     */
//    private void FillNodeList(String name, Node parent) {
//        // fill node list
//        if (nodelist.isEmpty()) {
//            getNode(name, parent);
//        } else {
//            nodelist.clear();
//            getNode(name, parent);
//        }
//    }
//
//
//    /**
//     * Counts the elements with a given name under the root node
//     *
//     * @param name name of the element
//     */
//    public int getNumberOfElement(String name) {
//        return getNumberOfElement(name, false);
//    }
//
//
//    /**
//     * Counts the elements with a given name under the current child node or under the root node
//     *
//     * @param name   name of the element
//     * @param parent true, if the number of elements under the current child node should be determined, false, if the
//     *               number of elements under the root node is needed.
//     * @see #setCurrentChildNode(String name, int idx, boolean parent)
//     */
//    public int getNumberOfElement(String name, boolean parent) {
//        int number = 0;
//        if (!parent || currChild == null) {
//            FillNodeList(name, doc);
//            number = nodelist.size();
//        } else {
//            FillNodeList(name, currChild);
//            number = nodelist.size();
//        }
//        return number;
//    }
//
//
//    /**
//     * Returns the value of an attribute of a specific element
//     *
//     * @param name   name of the element which contains the attribute
//     * @param attr   name of the attribute
//     * @param idx    index of the element with the specific element name beginning with index 0
//     * @param parent true, if the element with the attribute should be found under current child node should be
//     *               determined, false, if the element with the attribute should be found under the root node is
//     *               needed.
//     * @return the value of the attribute
//     * @throws XMLReaderException if the attribute could not be found or readed
//     * @see #setCurrentChildNode(String name, int idx, boolean parent)
//     * @see XMLReaderException
//     */
//    public String getValueOfAttribute(String name, String attr, int idx, boolean parent) throws XMLReaderException {
//        String value = "";
//
//        if (!parent || currChild == null) FillNodeList(name, doc);
//        else FillNodeList(name, currChild);
//
//        try {
//            Node n = ((Node) nodelist.get(idx));
//            //if(n.hasChildNodes())		// changed 16.01.2006
//            if (n.hasAttributes()) {
//                NamedNodeMap nm = n.getAttributes();
//                int j = nm.getLength();
//                int i;
//                for (i = 0; i < j; i++) {
//                    Node child = nm.item(i);
//                    if (child.getNodeType() == Node.ATTRIBUTE_NODE && child.getNodeName().equals(attr)) {
//                        value += child.getNodeValue();
//                    }
//                }
//            }
//        }
//        catch (IndexOutOfBoundsException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//        catch (DOMException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//
//        return value;
//    }
//
//
//    /**
//     * Returns the text content of an element which contains text
//     *
//     * @param name   name of the element which contains the text
//     * @param idx    index of the element with the specific element name beginning with index 0
//     * @param parent true, if the element should be found under current child node should be determined, false, if the
//     *               elemen should be found under the root node is needed.
//     * @return the text of the element
//     * @throws XMLReaderException if the element could not be found or readed
//     * @see #setCurrentChildNode(String name, int idx, boolean parent)
//     * @see XMLReaderException
//     */
//    public String getValueOfElement(String name, int idx, boolean parent) throws XMLReaderException {
//        String value = "";
//
//        if (!parent || currChild == null) FillNodeList(name, doc);
//        else FillNodeList(name, currChild);
//
//        try {
//            Node n = ((Node) nodelist.get(idx));
//            if (n.hasChildNodes()) {
//                NodeList nl = n.getChildNodes();
//                int j = nl.getLength();
//                int i;
//                for (i = 0; i < j; i++) {
//                    Node child = nl.item(i);
//                    if (child.getNodeType() == Node.TEXT_NODE && IsPrintable(child.getNodeValue()))
//                        value = value + child.getNodeValue();
//                }
//            }
//        }
//        catch (IndexOutOfBoundsException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//        catch (DOMException e) {
//            throw new XMLReaderException("> " + e.getMessage());
//        }
//
//        return value;
//    }
//
//
//    /**
//     * Adds all elements with a given name to the nodelist (searching under a given parent node)
//     *
//     * @param name name of the element
//     * @param n    parent Node
//     */
//    private void getNode(String name, Node n) {
//        if (n.getNodeName().equals(name)) {
//            nodelist.add(n);
//        }
//        if (n.hasChildNodes()) {
//            NodeList nl = n.getChildNodes();
//            int j = nl.getLength();
//            int i;
//
//            for (i = 0; i < j; i++) {
//                getNode(name, nl.item(i));
//            }
//        }
//    }
//
//}