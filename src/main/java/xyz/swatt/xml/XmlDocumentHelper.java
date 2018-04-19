package xyz.swatt.xml;

import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.lib.SaxonOutputKeys;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.exceptions.TooManyResultsException;
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
import java.util.LinkedList;
import java.util.List;

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
	public static enum XmlEntityFormat {
		DECIMAL, HEX
	}

	//========================= STATIC CONSTANTS ===============================
	private static final Logger LOGGER = LogManager.getLogger(XmlDocumentHelper.class);

	/**
	 * Special Names:
	 * &emsp;	= EM Space (one Monospaced Character width)
	 * &ensp;	= EN Space (half a Monospaced Character width)
	 * &lrm;	= Left-To-Right Mark
	 * &nbsp;	= Non-Breaking Space
	 * &rlm;	= Right-To-Left Mark
	 * &shy;	= Soft Hyphen
	 * &thinsp;	= Thin Space
	 * &zwj;	= Zero-Width Joiner
	 * &zwnj;	= Zero-Width Non-Joiner
	 *
	 * Hex:
	 * &#x20;	= Space
	 * &#x7f;	= Delete
	 * &#xa0;	= Non-Breaking Space
	 * &#xad;	= Soft Hyphen
	 * &#xff;	= Non-Breaking Space
	 * &#x034F;	= Combining Grapheme Joiner
	 * #x2002;	= EN Space (half a Monospaced Character width)
	 * #x2003;	= EM Space (one Monospaced Character width)
	 * #x2009;	= Thin Space
	 * #x200C;	= Zero-Width Non-Joiner
	 * #x200D;	= Zero-Width Joiner
	 * #x200E;	= Left-To-Right Mark
	 * #x200F;	= Right-To-Left Mark
	 *
	 * Decimal:
	 * &#32;	= Space
	 * &#127;	= Delete
	 * &#160;	= Non-Breaking Space
	 * &#173;	= Soft Hyphen
	 * &#255;	= Non-Breaking Space
	 * &#847;	= Combining Grapheme Joiner
	 * &#8194;	= EN Space (half a Monospaced Character width)
	 * &#8195;	= EM Space (one Monospaced Character width)
	 * &#8201;	= Thin Space
	 * &#8204;	= Zero-Width Non-Joiner
	 * &#8205;	= Zero-Width Joiner
	 * &#8206;	= Left-To-Right Mark
	 * &#8207;	= Right-To-Left Mark
	 */
	public static final String HTML_WHITESPACE_ENTITIES = "&emsp;|&ensp;|&lrm;|&nbsp;|&rlm;|&shy;|&thinsp;|&zwj;|&zwnj;"
			+ "|&#x20;|&#x7F;|&#xA0;|&#xAD;|&#xFF;|&&#x034F;|#x2002;|#x2003;|#x2009;|#x200C;|#x200D;|#x200E;|#x200F;"
			+ "|&#32;|&#127;|&#160;|&#173;|&#255;|&#847;|&#8194;|&#8195;|&#8201;|&#8204;|&#8205;|&#8206;|&#8207;";

	//========================= Static Variables ===============================

	//========================= Static Constructor =============================
	static {

	}

	//========================= Static Methods =================================
	/**
	 * Will create a new XML {@link Document}.
	 *
	 * @return A brand new, empty XML {@link Document}, with no root.
	 *
	 * @throws ParserConfigurationException
	 *             If the {@link Document} cannot be created.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document createNewDocument() throws ParserConfigurationException {

		LOGGER.info("createNewDocument() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Document doc;

		//------------------------ Code ----------------------------------------
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		LOGGER.info("createNewDocument() [END]");

		return doc;
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
	 * @throws TransformerException
	 *             If there was a problem with conversion.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document getDocumentFrom(File _xmlFile) throws TransformerException {

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
	 * @throws TransformerException
	 * 		If there was a problem with conversion.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Document getDocumentFrom(String _xmlString) throws TransformerException {

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

		// Saxon TransformerFactory is Namespace Aware by default.
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(SaxonOutputKeys.REQUIRE_WELL_FORMED, "yes");
		transformer.transform(new StreamSource(new StringReader(_xmlString)), domResult);
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
	 * @throws IllegalArgumentException
	 * 		If the given {@code _xmlNode} is {@code null}.
	 * 		<p>Or if the given {@code _xPath} is {@code null}, an Empty String, or Whitespace only.</p>
	 * @throws SaxonApiException
	 * 		If there is an error with the XPath lookup.
	 * @throws TooManyResultsException
	 * 		If more than 1 Node was found by the given XPath
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Node getNodeForXPath(Node _xmlNode, String _xPath) throws SaxonApiException {

		LOGGER.debug("getNodesForXPath(xmlDocument, xPath: {}) [START]", _xPath);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<Node> nodes = getNodesForXPath(_xmlNode, _xPath);

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
	 * @throws IllegalArgumentException
	 * 		If the given {@code _xmlNode} is {@code null}.
	 * 		<p>Or if the given {@code _xPath} is {@code null}, an Empty String, or Whitespace only.</p>
	 * @throws SaxonApiException
	 * 		If there is an error with the XPath lookup.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static List<Node> getNodesForXPath(Node _xmlNode, String _xPath) throws SaxonApiException {

		LOGGER.debug("getNodesForXPath(_xmlDocument, _xPath: {}) [START]", _xPath);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------
		// Creating a new processor each time to better support multi-threading.
		// TODO: Really only need 1 Processor Per Document, because Processors synchronize on Document.
		Processor PROCESSOR = new Processor(false);

		//------------------------ Variables -----------------------------------
		Document document = _xmlNode.getOwnerDocument() == null ? (Document) _xmlNode : _xmlNode.getOwnerDocument();
		String defaultNamespace;
		XPathCompiler xPathCompiler = PROCESSOR.newXPathCompiler();
		XPathSelector xPathselector;

		LinkedList<Node> nodes = new LinkedList<>();

		//------------------------ Code ----------------------------------------
		// Namespace of the Root Element. (Root Element can only have a Default Namespace.)
		defaultNamespace = document.getFirstChild() == null ? null : document.getFirstChild().getNamespaceURI();
		if(defaultNamespace != null) {
			xPathCompiler.declareNamespace("", defaultNamespace);
		}

		try {
			xPathselector = xPathCompiler.compile(_xPath).load();
		}
		catch(Exception e) {
			throw new RuntimeException("Invalid XPath: " + _xPath, e);
		}

		xPathselector.setContextItem(PROCESSOR.newDocumentBuilder().wrap(_xmlNode));

		for(XdmItem xdmItem : xPathselector.evaluate()) {

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
				else if(realNode instanceof TextImpl) {
					node = (TextImpl) realNode;
				}
				else {
					String className = realNode.getClass().toString();
					throw new RuntimeException("The given XPath returned a DOMNodeWrapper of an Unknown Type! (" + className + ")");
				}
			}
			else {
				String className = value.getClass().toString();
				throw new RuntimeException("The given XPath returned a Node of an Unknown Type! (" + className + ")");
			}

			nodes.add(node);
		}

		LOGGER.debug("getNodesForXPath(_xmlDocument, _xPath: {}) [END]", _xPath);

		return nodes;
	}

	/**
	 * Takes in an XML {@link Node} and returns a String XML representation of it, with no formatting.
	 *
	 * @param _node
	 * 		The Node/Document to get the String representation of.
	 *
	 * @return The XML {@link Node}/{@link Document} in String format
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _node} is {@code null}.
	 * @throws TransformerException
	 * 		If {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String prettyPrint(Node _node) throws TransformerException {
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
	 * @throws IllegalArgumentException
	 * 		If the given {@code _node} is {@code null}.
	 * @throws TransformerException
	 * 		If {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(Node _node) throws TransformerException {
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
	 * @throws IllegalArgumentException
	 * 		If the given {@code _node} is {@code null}.
	 * @throws TransformerException
	 * 		If {@link Node}/{@link Document} cannot be converted into a String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(Node _node, boolean _prettyPrint) throws TransformerException {

		LOGGER.info("toString(_node, _prettyPrint: {}) [START]", _prettyPrint);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_node, "Node");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		StreamResult result = new StreamResult(new StringWriter());
		String toString;
		Transformer transformer;

		//------------------------ Code ----------------------------------------
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
}
