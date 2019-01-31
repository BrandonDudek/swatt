package xyz.swatt.xml;

import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.*;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.dom.AttrImpl;
import org.apache.xerces.dom.CommentImpl;
import org.apache.xerces.dom.ElementNSImpl;
import org.apache.xerces.dom.TextImpl;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.exceptions.TooManyResultsException;
import xyz.swatt.exceptions.XmlException;
import xyz.swatt.string.StringHelper;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Helper Class to deal with XML {@link Document}s.
 * <ul>
 *     <li><b>XML:</b> <a href="https://docs.oracle.com/javase/8/docs/api/org/w3c/dom/package-summary.html" target="_blank">W3C</a> is used for all XML Classes.
 *     <li><b>XPath:</b> <a href="http://www.saxonica.com" target="_blank">Saxon</a>
 *     <a href="http://www.saxonica.com/html/documentation/javadoc/net/sf/saxon/s9api/package-summary.html" target="_blank"><i>s9api</i></a>
 *     is used to support XPath v3.1.
 * </ul>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public final class XmlDocumentHelper {

	//========================= Static Enums ===================================
	public enum XmlEntityFormat {
		DECIMAL, HEX
	}

	//========================= STATIC CONSTANTS ===============================
	private static final Logger LOGGER = LogManager.getLogger(XmlDocumentHelper.class);

	//========================= Static Variables ===============================

	//========================= Static Constructor =============================
	static {

	}

	//========================= Static Methods =================================
	/**
	 * Creates an Element {@link Node}, that belongs to the given XML {@link Document}, with the given name and value.
	 *
	 * @param _xmlDocument The XML Document to be the owner of this new {@link Node}.
	 * @param _name The name to call the new Node.
	 * @param _value The value to give the new Node.
	 *
	 * @return The Node that was just created.
	 *
	 * @throws IllegalArgumentException If the given XML Document is {@code null}, or if the given Name or Value is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Node createElementNode( Document _xmlDocument, String _name, String _value ) {

		LOGGER.info( "createElementNode( Document, name: {}, value: {} ) [START]", _name, _value );

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_xmlDocument, "XML Document");

		ArgumentChecks.stringNotWhitespaceOnly(_name, "Node Name");
		ArgumentChecks.stringNotWhitespaceOnly(_value, "Node Value");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Node newNode;

		//------------------------ Initialize ----------------------------------
		newNode = _xmlDocument.createElement( _name );

		//------------------------ Code ----------------------------------------
		newNode.appendChild( _xmlDocument.createTextNode( _value ) );

		LOGGER.debug( "createElementNode( Document, name: {}, value: {} ) [END]", _name, _value );

		return newNode;
	}

	/**
	 * Finds all Decimal Entities with the equivalent Hexadecimal Entity.
	 *
	 * @param _s
	 *            The String to perform the find and replace on.
	 *
	 * @return The given String with the replacements made.
	 *
	 * @throws IllegalArgumentException If the given string is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String decimalEscapedToHexEscaped(String _s) {

		LOGGER.info("decimalEscapedToHexEscaped(_s: {}) [START]", _s);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_s, null);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Matcher matcher = Pattern.compile("&#([0-9]{2,4});").matcher(_s);

		HashMap<String, String> replacements = new HashMap<>();

		//------------------------ Code ----------------------------------------
		////////// Determine Replacements //////////
		while(matcher.find()) {

			String dec = matcher.group(1);
			String replacementString = "&#x" + Integer.toHexString(Integer.parseInt(dec)) + ";";

			replacements.put(matcher.group(), replacementString);
		}

		////////// Make Replacements //////////
		for(String find : replacements.keySet()) {
			_s = _s.replaceAll(Matcher.quoteReplacement(find), replacements.get(find));
		}

		LOGGER.debug("decimalEscapedToHexEscaped(_s: {}) [END]", _s);

		return _s;
	}

	/**
	 * Will create a new XML {@link Document}.
	 *
	 * @return A brand new, empty XML {@link Document}, with no root.
	 *
	 * @throws XmlException If the {@link Document} cannot be created.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document createNewDocument() {

		LOGGER.info("createNewDocument() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Document doc;

		//------------------------ Code ----------------------------------------
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch(ParserConfigurationException e) {
			throw new XmlException("Error creating XML Document!", e);
		}

		LOGGER.info("createNewDocument() [END]");

		return doc;
	}

	/**
	 * Will XML Escape any character outside of the range of Basic Latin printable characters (\x09-\x0D|\x21-\x7E).
	 * (Note: Same as only keeping ASCII printable characters.)
	 *
	 * @param _inputString The {@link String} to parse for escaping.
	 * @param _format  Whether the XML Entities should be in Decimal or HEX format.
	 *
	 * @return The escaped {@link String}; or {@code null}, if {@code null} is given.
	 *
	 * @throws IllegalArgumentException If the given {@link XmlEntityFormat} is {@code null} or an unknown format.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String escapeExtendedCharacters(String _inputString, XmlEntityFormat _format) {

		LOGGER.info( "escapeExtendedCharacters(_inputString: {}, _format: {}) [START]", _inputString, _format );

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_format, "XML Entity Format");

		if( _inputString == null ) {
			return null;
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		StringBuilder stringBuilder = new StringBuilder();

		//------------------------ Code ----------------------------------------
		for( char c : _inputString.toCharArray() ) {

			if( ( (int) c >= 9 && (int) c <= 13 ) || ( (int) c >= 32 && (int) c <= 126 ) ) { // Using Decimal values here (note: the javadoc shows Hex values).
				stringBuilder.append( c );
			}
			else {

				switch( _format ) {

					case DECIMAL:
						stringBuilder.append( "&#" ).append((int) c).append( ";" );
						break;

					case HEX:
						stringBuilder.append( "&#x" ).append( Integer.toHexString((int) c) ).append( ";" );
						break;

					default:
						throw new IllegalArgumentException( "Unknown XmlEntityFormat: " + _format + "!" );
				}
			}
		}

		LOGGER.debug( "escapeExtendedCharacters(_inputString: {}, _format: {}) [END]", _inputString, _format );

		return stringBuilder.toString();
	}

	/**
	 * Creates a Namespace aware XML Document from an XML {@link File}.
	 *
	 * @param _xmlFile
	 *            An XML {@link File} to parse into an XML {@link Document}.
	 *
	 * @return An XML {@link Document} representing the given File.
	 *
	 * @throws IllegalArgumentException
	 *             <ul>
	 *                 <li>If the given File is {@code null}.</li>
	 *                 <li>The given {@link File} does not exist.</li>
	 *                 <li>The given {@link File} is a directory.</li>
	 *                 <li>Or the given {@link File} is unreadable.</li>
	 *             </ul>
	 *
	 * @throws XmlException If there was a problem with conversion.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document getDocumentFrom(File _xmlFile) {

		LOGGER.debug( "getDocumentFrom(_xmlFile: {}) [START]", _xmlFile == null ? "(NULL)" : _xmlFile.getPath() );

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.fileExists(_xmlFile, "XML");

		//noinspection ConstantConditions
		if(!_xmlFile.canRead()) {
			throw new IllegalArgumentException( "Given File " + _xmlFile.getAbsolutePath() + " is unreadable!" );
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Document xmlDocument;
		String xmlString  = StringHelper.toString( _xmlFile );

		//------------------------ Code ----------------------------------------
		// TransformerFactory un-escapes all XML Entities by default.
		// getDocumentFrom( String ) will first double escape them to preserve them.
		xmlDocument = getDocumentFrom( xmlString );

		LOGGER.debug( "getDocumentFrom(_xmlFile: {}) [END]", _xmlFile.getPath() );

		return xmlDocument;
	}

	/**
	 * Creates a Namespace aware XML Document from a String representation.
	 *
	 * @param _xmlString
	 * 		String representation of an XML {@link Document}.
	 *
	 * @return An XML {@link Document} representing the given String.
	 *
	 * @throws IllegalArgumentException
	 * 		<ul>
	 * 		    <li>If the given String is {@code null}.</li>
	 * 		    <li>Or if the given String is Empty or just Whitespace.</li>
	 * 		</ul>
	 * @throws XmlException If there was a problem with conversion.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document getDocumentFrom(String _xmlString) {

		LOGGER.info("getDocumentFrom(_xmlString: {}) [START]", _xmlString);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_xmlString, "XML String");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Document xmlDocument;
		DOMResult domResult = new DOMResult();

		//------------------------ Code ----------------------------------------
		// Trailing white space is not meaningful to XML, and can throw error if
		// XML declaration comes after whitespace.
		_xmlString = StringHelper.replace(_xmlString, "", StringHelper.CharacterPosition.END_OF_STRING, true,
				StringHelper.CharacterSet.ALL_WHITESPACE);

		// TransformerFactory un-escapes all XML Entities by default.
		// So we are doubly escaping them as to not lose them.
		_xmlString = _xmlString.replaceAll("&", "&amp;");

		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer(); // Saxon TransformerFactory is Namespace Aware by default.
			//transformer.setOutputProperty(SaxonOutputKeys.REQUIRE_WELL_FORMED, "yes"); // No longer available in Saxon-HE.
			transformer.transform(new StreamSource(new StringReader(_xmlString)), domResult);
		}
		catch(TransformerException e) {
			throw new XmlException("Error creating XML Document!", e);
		}

		xmlDocument = (Document) domResult.getNode();

		// DocumentBuilderFactory is not used because it does not load the docElement and firstChild at initialization for all Nodes in the document.
		// TransformerFactory does, and this is helpful for debugging.
		// Notes:
		// - DocumentBuilderFactory is not Namespace Aware by default.
		// -- To enable it, use DocumentBuilderFactory.setNamespaceAware( true ).
		// - DocumentBuilderFactory un-escapes all XML Entities by default.
		// -- DocumentBuilderFactory.setExpandEntityReferences( false ) does not work.

		LOGGER.debug("getDocumentFrom(_xmlString: {}) [END]", _xmlString);

		return xmlDocument;
	}

	/**
	 * Gets a {@link Node} from a given XML {@link Document} that match the given XPath. (Expects only 1 {@link Node} to be found.)
	 * <p>
	 *     If an XPath boolean (<i>xs:boolean</i>) is given as the XPath, it will be wrapped in an {@code #TEXT} {@link Node} and returned.
	 * </p>
	 * <p>
	 *     <i>Note:</i> All Root level Namespaces are automatically accounted for and do not have to be manually specified.
	 * </p>
	 *
	 * @param _xmlNode
	 * 		The Document or Element to do the XPath search on.
	 * 		<p>Even if an {@link Node} is passed, the search could still be at the {@link Document} level depending on the {@code _xPath} value.</p>
	 * @param _xPath
	 * 		The XPath to search for.
	 * 		<p>If the XPath starts with "/" or "//" the search will be done at the Document level.</p>
	 * 		<p>If the XPath starts with no backslash, "./", or ".//" the search will be done at the Element level.
	 * 		(When searching from the Element level, do <b>not</b> include the given element's name in the XPath.)</p>
	 *
	 * @return A {@link Node} that was the result of this XPath lookup, or {@code null}, if no {@link Node} matches the XPath expression.
	 *
	 * @throws IllegalArgumentException If the given Node is {@code null} or the given XPath is blank.
	 * @throws XmlException If there is an error with the XPath lookup.
	 * @throws TooManyResultsException If more than 1 Node was found by the given XPath
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Node getNodeForXPath(Node _xmlNode, String _xPath) {

		LOGGER.debug("getNodesForXPath(xmlDocument, xPath: {}) [START]", _xPath);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<Node> nodes = getNodesForXPath(_xmlNode, _xPath); // Argument Checks done here.

		//------------------------ Code ----------------------------------------
		if(nodes.size() > 1) {
			throw new TooManyResultsException("Only 1 result Node expected, but " + nodes.size() + " were found!");
		}

		LOGGER.debug("getNodesForXPath(xmlDocument, xPath: {}) [END]", _xPath);

		return nodes.isEmpty() ? null : nodes.get(0);
	}

	/**
	 * Gets a list of {@link Node}s from a given XML {@link Document} that match the given XPath.
	 * <p>
	 *     If an XPath boolean (<i>xs:boolean</i>) is given as the XPath, it will be wrapped in an {@code #TEXT} {@link Node} and returned.
	 * </p>
	 * <p>
	 *     <i>Note:</i> All Root level Namespaces are automatically accounted for and do not have to be manually specified.
	 * </p>
	 *
	 * @param _xmlNode
	 * 		The Document or Element to do the XPath search on.
	 * 		<p>Even if an {@link Node} is passed, the search could still be at the {@link Document} level depending on the {@code _xPath} value.</p>
	 * @param _xPath
	 * 		The XPath to search for.
	 * 		<p>If the XPath starts with "/" or "//" the search will be done at the Document level.</p>
	 * 		<p>If the XPath starts with no backslash, "./", or ".//" the search will be done at the Element level.
	 * 		(When searching from the Element level, do <b>not</b> include the given element's name in the XPath.)</p>
	 *
	 * @return A collection of {@link Node}s that was the result of this XPath lookup, or an empty collection, if no {@link Node}s matched the XPath expression.
	 *
	 * @throws IllegalArgumentException If the given Node is {@code null} or the given XPath is blank.
	 * @throws XmlException If there is an error with the XPath lookup.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static List<Node> getNodesForXPath(Node _xmlNode, String _xPath) {

		LOGGER.debug("getNodesForXPath(_xmlDocument, _xPath: {}) [START]", _xPath);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_xmlNode, "XML Node");
		ArgumentChecks.stringNotWhitespaceOnly(_xPath, "XPath");

		//------------------------ CONSTANTS -----------------------------------
		// Creating a new processor each time to better support multi-threading.
		// TODO: Really only need 1 Processor Per Document, because Processors synchronize on Document.
		Processor PROCESSOR = new Processor(false);

		//------------------------ Variables -----------------------------------
		Document document = _xmlNode.getOwnerDocument() == null ? (Document) _xmlNode : _xmlNode.getOwnerDocument();
		Node rootNode;
		XPathCompiler xPathCompiler = PROCESSOR.newXPathCompiler();
		XPathSelector xPathselector;

		LinkedList<Node> nodes = new LinkedList<>();

		//------------------------ Code ----------------------------------------
		// Set Namespaces of the Root Element, for XPath Compiler to use.
		// That way the XPath does not have to specify the Default Namespace.
		if((rootNode = document.getFirstChild()) != null) {

			NamedNodeMap rootAttributes = rootNode.getAttributes();
			if(rootAttributes != null) {
				for(int i = 0; i < rootAttributes.getLength(); i++) {

					Node rootAttribute = rootAttributes.item(i);

					String prefix = rootAttribute.getPrefix();
					String localName = rootAttribute.getLocalName();
					if(prefix == null) { // May be Default Namespace.
						if(localName != null && localName.equalsIgnoreCase("xmlns")) { // Is Default Namespace?
							xPathCompiler.declareNamespace("", rootAttribute.getNodeValue());
						}
					}
					else if(prefix.equalsIgnoreCase("xmlns")) {
						xPathCompiler.declareNamespace(localName, rootAttribute.getNodeValue());
					}
				}
			}
		}

		try {
			xPathselector = xPathCompiler.compile(_xPath).load();
		}
		catch(Exception e) {
			throw new XmlException("Invalid XPath: " + _xPath, e);
		}

		XdmValue xdmItems;
		try {
			xPathselector.setContextItem(PROCESSOR.newDocumentBuilder().wrap(_xmlNode));
			xdmItems = xPathselector.evaluate();
		}
		catch(SaxonApiException e) {
			throw new XmlException("Error executing XPath: " + _xPath, e);
		}

		for(XdmItem xdmItem : xdmItems) {

			Sequence value = xdmItem.getUnderlyingValue();

			Node node;
			if(value instanceof BooleanValue) { // xs:boolean
				node = document.createTextNode(((BooleanValue) value).getStringValue());
			}
			else if(value instanceof Int64Value) { // xs:integer
				node = document.createTextNode(((Int64Value) value).getStringValue());
			}
			else if(value instanceof NamespaceNode) {
				node = document.createTextNode(((NamespaceNode) value).getStringValue());
			}
			else if(value instanceof StringValue) { // xs:string
				node = document.createTextNode(((StringValue) value).getStringValue());
			}
			else if(value instanceof DOMNodeWrapper) { // Node / Element.

				Object realNode = ((DOMNodeWrapper) value).getRealNode();

				if(realNode instanceof AttrImpl) {
					node = (AttrImpl) realNode;
				}
				else if(realNode instanceof CommentImpl) {
					node = (CommentImpl) realNode;
				}
				else if(realNode instanceof ElementNSImpl) {
					node = (ElementNSImpl) realNode;
				}
				else if(realNode instanceof com.sun.org.apache.xerces.internal.dom.ElementNSImpl) {
					node = (com.sun.org.apache.xerces.internal.dom.ElementNSImpl) realNode;
				}
				else if(realNode instanceof TextImpl) {
					node = (TextImpl) realNode;
				}
				else {
					String className = realNode.getClass().toString();
					throw new XmlException("The given XPath returned a DOMNodeWrapper of an Unknown Type! (" + className + ")");
				}
			}
			else {
				String className = value.getClass().toString();
				throw new XmlException("The given XPath returned a Node of an Unknown Type! (" + className + ")");
			}

			nodes.add(node);
		}

		LOGGER.debug("getNodesForXPath(_xmlDocument, _xPath: {}) [END]", _xPath);

		return nodes;
	}

	/**
	 * Gets a String result for the given xPath, by calling getTextContent() on the returned element.
	 * <p>
	 *     <i>Note:</i> All Root level Namespaces are automatically accounted for and do not have to be manually specified.
	 * </p>
	 *
	 * @param _xmlNode
	 *            The Document or Element to do the XPath search on.
	 *            <ul>
	 *            <li>Even if an Element is passed, the search
	 *            could still be at the Document level depending on the
	 *            {@code _xPath} value.
	 *            </ul>
	 * @param _xPath
	 *            The XPath to search for.
	 *            <ul>
	 *            <li>If the XPath Starts with "/" or "//" the search will be
	 *            done at the Document level.
	 *            <li>If the XPath Starts with "./" or no backslash the search
	 *            will be done at the Element level.
	 *            <ul>
	 *            <li>When Searching from the Element Level, do <b>not</b>
	 *            include the given element's name in the XPath.
	 *            </ul>
	 *            </ul>
	 *
	 * @return A String with the result or NULL, if there is no result.
	 *
	 * @throws XmlException If there is an error with the XPath lookup.
	 * @throws TooManyResultsException If more than 1 Node was found by the given XPath
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String getStringForXPath(Node _xmlNode, String _xPath) {

		LOGGER.debug( "getStringForXPath(Node, _xPath: {}) [START]", _xPath );

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Node node;
		String result = null;

		//------------------------ Code ----------------------------------------
		node = getNodeForXPath(_xmlNode, _xPath);
		if(node != null) {
			result = node.getTextContent();
		}

		LOGGER.debug( "getStringForXPath(Node, _xPath: {}) [END]", _xPath );

		return result;
	}

	/**
	 * Gets a List of String results for the given xPath, by calling getTextContent() on all of the returned Elements.
	 * <p>
	 *     <i>Note:</i> All Root level Namespaces are automatically accounted for and do not have to be manually specified.
	 * </p>
	 *
	 * @param _xmlNode The Document or Element to do the XPath search on.
	 *            <ul>
	 *            <li>Even if an Element is passed, the search
	 *            could still be at the Document level depending on the
	 *            {@code _xPath} value.
	 *            </ul>
	 * @param _xPath The XPath to search for.
	 *            <ul>
	 *            <li>If the XPath Starts with "/" or "//" the search will be
	 *            done at the Document level.
	 *            <li>If the XPath Starts with "./" or no backslash the search
	 *            will be done at the Element level.
	 *            <ul>
	 *            <li>When Searching from the Element Level, do <b>not</b>
	 *            include the given element's name in the XPath.
	 *            </ul>
	 *            </ul>
	 *
	 * @return A List of Strings with the result or an Empty List, if there is no result.
	 *
	 * @throws IllegalArgumentException If the given Node is {@code null} or the given XPath is blank.
	 * @throws XmlException If there is an error with the XPath lookup.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static List< String > getStringsForXPath(Node _xmlNode, String _xPath) {

		LOGGER.debug( "getStringsForXPath( Node, _xPath: {} ) [START]", _xPath );

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List< Node > nodes = getNodesForXPath( _xmlNode, _xPath ); // Argument Checks done here.
		List< String > results = new ArrayList<>( nodes.size() );

		//------------------------ Initialize ----------------------------------

		//------------------------ Code ----------------------------------------
		for( Node node : nodes ) {
			results.add( node.getTextContent() );
		}

		LOGGER.debug( "getStringsForXPath(Node, _xPath: {}) [END]", _xPath );

		return results;
	}

	/**
	 * Finds all Hexadecimal Entities with the equivalent Decimal Entity.
	 *
	 * @param _s
	 *            The String to perform the find and replace on.
	 *
	 * @return The given String with the replacements made.
	 *
	 * @throws IllegalArgumentException If the given string is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String hexEscapedToDecimalEscaped(String _s) {

		LOGGER.info("hexEscapedToDecimalEscaped(_s: {}) [START]", _s);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_s, null);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Matcher matcher = Pattern.compile("&#x([0-9a-zA-z]{2,4});").matcher(_s);

		HashMap<String, String> replacements = new HashMap<>();

		//------------------------ Code ----------------------------------------
		////////// Determine Replacements //////////
		while(matcher.find()) {

			String hex = matcher.group(1);
			String replacementString = "&#" + Integer.valueOf(hex, 16) + ";";

			replacements.put(matcher.group(), replacementString);
		}

		////////// Make Replacements //////////
		for(String find : replacements.keySet()) {
			_s = _s.replaceAll(Matcher.quoteReplacement(find), replacements.get(find));
		}

		LOGGER.debug("hexEscapedToDecimalEscaped(_s: {}) [END]", _s);

		return _s;
	}

	/**
	 * Will take in an HTML {@link String} and try to parse it to an XML {@link Document}, with UTF-8 encoding.
	 * <p>(The parsing will try to resolve the differences between HTML and XML.)</p>
	 * <p><b>Note:</b> All tags and attributes are converted to lower case, for consistency.</p>
	 * <p><b>Warning:</b> If the given text does not have a root element, then only the first element is parsed..</p>
	 *
	 * @param _html The HTML {@link String} to convert to an XML {@link Document}.
	 *
	 * @return The given HTML as an XML {@link Document}.
	 *
	 * @throws IllegalArgumentException If the given HTML {@link String} is blank.
	 * @throws XmlException If the given HTML {@link String} cannot be parsed or has multiple root elements.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document htmlToXml(String _html) {

		LOGGER.info("htmlToXml(_html: {}) [START]", _html);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_html, "HTML String");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		org.jsoup.nodes.Document jsoupDocument;
		Document w3cDocument;

		//------------------------ Code ----------------------------------------
		jsoupDocument = Jsoup.parse(_html);
		if(jsoupDocument.body().childNodeSize() < 1) {
			throw new XmlException("The given HTML String could not be parsed!\n\tHTML String: " + _html);
		}
		else if(jsoupDocument.body().childNodeSize() > 1) {
			throw new XmlException("The given HTML String has multiple root elements! (It is required to have only 1.)!\n\tHTML String: " + _html);
		}

		try {
			jsoupDocument = Jsoup.parse(_html, "", Parser.xmlParser().settings(new ParseSettings(false, false)));
		}
		catch(Exception e) {
			throw new XmlException("The given HTML String could not be parsed!", e);
		}

		w3cDocument = new W3CDom().fromJsoup(jsoupDocument);

		LOGGER.debug("htmlToXml(_html: {}) [END]", _html);

		return w3cDocument;
	}

	/**
	 * Takes in an XML {@link Node} and returns a String XML representation of it, with no formatting.
	 *
	 * @param _node
	 * 		The Node/Document to get the String representation of.
	 *
	 * @return The XML {@link Node}/{@link Document} in String format
	 *
	 * @throws IllegalArgumentException If the given {@code _node} is {@code null}.
	 * @throws XmlException If the {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String prettyPrint(Node _node) {
		return toString( _node, true );
	}

	/**
	 * Takes in an XML {@link Node} and returns a String XML representation of it, with no formatting.
	 *
	 * @param _node
	 * 		The Node/Document to get the String representation of.
	 *
	 * @return The XML {@link Node}/{@link Document} in String format
	 *
	 * @throws IllegalArgumentException If the given {@code _node} is {@code null}.
	 * @throws XmlException If the {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(Node _node) {
		return toString( _node, false );
	}

	/**
	 * Takes in an XML {@link Node} and returns a String XML representation of it.
	 *
	 * @param _node
	 * 		The Node/Document to get the String representation of.
	 * @param _prettyPrint
	 * 		Whether or not to apply "pretty print" to the String.
	 * 		<i>(The indent value will be 2 spaces.)</i>
	 *
	 * @return The XML {@link Node}/{@link Document} in String format.
	 *
	 * @throws IllegalArgumentException If the given {@code _node} is {@code null}.
	 * @throws XmlException If the {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(Node _node, boolean _prettyPrint) {

		LOGGER.info("toString(_node, _prettyPrint: {}) [START]", _prettyPrint);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_node, "Node");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		StreamResult result = new StreamResult(new StringWriter());
		String toString;
		Transformer transformer;

		//------------------------ Code ----------------------------------------
		try {
			transformer = TransformerFactory.newInstance().newTransformer();

			//transformer.setOutputProperty( OutputKeys.METHOD, "xml" ); // Automatically inferred by TransformerFactory.

			if(_node.getOwnerDocument() != null) {

				// Omit XML Declaration, because the given node is not a Document.
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

			if(_prettyPrint) {

				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				//transformer.setOutputProperty( SaxonOutputKeys.INDENT_SPACES, "2" ); // Cannot be used as it requires a Saxon License.
			}

			transformer.transform(new DOMSource(_node), result);
		}
		catch(TransformerException e) {
			throw new XmlException("Error parsing XML Document to String!", e);
		}

		toString = result.getWriter().toString();

		// TransformerFactory escapes &s by default.
		// But we want to defeat this as to display exactly what is in the XML file/String.
		// Note: We already preserve XML Entities on Document creation.
		toString = toString.replaceAll("&amp;", "&");

		LOGGER.debug("toString(_node: {}, _prettyPrint: {}) [END]", toString, _prettyPrint);

		return toString;
	}

	//========================= CONSTANTS ======================================

	//========================= Variables ======================================

	//========================= Constructors ===================================

	//========================= Methods ========================================

	//========================= Classes ========================================
}
