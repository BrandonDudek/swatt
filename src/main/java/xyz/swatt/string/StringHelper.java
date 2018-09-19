package xyz.swatt.string;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.xml.XmlDocumentHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Scanner;

/**
 * A Helper class to preform common and/or complex String manipulations.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class StringHelper {

	//========================= Static Enums ===================================
	public static enum CharacterPosition {

		ANYWHERE("ANYWHERE"),

		BEGINNING_OF_STRING("BEGINNING_OF_STRING"),
		MIDDLE_OF_STRING("MIDDLE_OF_STRING"),
		END_OF_STRING("END_OF_STRING"),

		/**
		 * Fist or Last consecutive characters, but not in the middle.
		 */
		BEGINNING_OR_END("BEGINNING_OR_END"),;

		private final String VALUE;

		@SuppressWarnings("unused")
		private CharacterPosition(String _value) {

			VALUE = _value;
		}

		@Override
		public String toString() {

			return VALUE;
		}
	}

	public static enum CharacterSet {

		/**
		 * All known kinds of ASCII &amp; UTF-8 Whitespace.
		 * <p>
		 *     <b>Java Patterns:</b> (see: <a href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html">Java 8 REGEX Pattern</a>)
		 * </p>
		 * <p>- \h = A horizontal whitespace character: [ \t\xA0&#92;u1680&#92;u180e&#92;u2000-&#92;u200a&#92;u202f&#92;u205f&#92;u3000]</p>
		 * <p>- \s = A whitespace character: [ \t\n\x0B\f\r]</p>
		 * <p>- \v = A vertical whitespace character: [\n\x0B\f\r\x85&#92;u2028&#92;u2029]</p>
		 * <p>- \p{javaWhitespace} = Equivalent to Character.isWhitespace(char)</p>
		 * <p>- \R = Any Unicode line-break sequence: (&#92;u000D&#92;u000A|[&#92;u000A&#92;u000B&#92;u000C&#92;u000D&#92;u0085&#92;u2028&#92;u2029])</p>
		 * <p>-- "\R" Not yet accepted in java as of 1.8.0_152.</p>
		 *
		 * <p>
		 *     <b>Regex Unicode Categories:</b>
		 * </p>
		 * <p>- \p{Z} = Separator (any kind of whitespace or invisible separator.)</p>
		 *
		 * <p>
		 *     <b>HTML:</b>
		 * </p>
		 * <p>- \xA0 = Non-Breaking Space</p>
		 * <p>- \x{2002} = EN Space (half a Monospaced Character width)</p>
		 * <p>- \x{2003} = EM Space (one Monospaced Character width)</p>
		 * <p>- \x{2009} = Thin Space</p>
		 */
		// Javadoc comments use "&#92;u" for unicode escapes, so that is formats correctly.
		ALL_WHITESPACE("[\\h\\s\\v\\p{javaWhitespace}\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029\\p{Z}\\xA0\\x{2002}\\x{2003}\\x{2009}]"),

		/**
		 * All Control Characters.
		 */
		CONTROL("\\p{C}"),

		/**
		 * All Control Characters, except [\t\n\v\f\r].
		 * <p>
		 * <i>(see: <a href="http://www.regular-expressions.info/unicode.html">Regex Tutorial - Unicode</a>)</i>
		 * </p>
		 */
		CONTROL_NON_PRINT("[[\\x00-\\x08][\\x0E-\\x1F][\\x7F-\\x9F]\\p{Cf}\\p{Co}\\p{Cs}\\p{Cn}]"),

		/**
		 * Characters that will not show up in a web browser, even if HTML Escaped.
		 * Different from {@link #CONTROL_NON_PRINT}, because some Control Characters can be displayed when HTML Escaped.
		 * <p>
		 *     <b>Includes:</b>
		 * </p>
		 * <p>- Basic Latin</p>
		 * <p>- Latin-1 Supplement</p>
		 * <p>- Latin Extended-A</p>
		 * <p>- Latin Extended-B</p>
		 *
		 * <p>(Tested in Chrome.)</p>
		 */
		HTML_NON_PRINT("[[\\x00\\x7F][\\x80\\x81\\x8D-\\x90\\x9D\\x9E]\\p{Co}\\p{Cs}\\p{Cn}]"),

		/**
		 * Normal Whitespace.
		 * <ul>
		 * <li>" " - Space</li>
		 * <li>\t - Tab</li>
		 * <li>\r - Carriage Return</li>
		 * <li>\n - New Line</li>
		 * <li>\f - Form Feed</li>
		 * </ul>
		 */
		NORMAL_WHITESPACE("[\\s]"),;

		private final String VALUE;

		@SuppressWarnings("unused")
		private CharacterSet(String _value) {

			VALUE = _value;
		}

		@Override
		public String toString() {

			return VALUE;
		}
	}

	public static enum NumberBase {
		DECIMAL, HEXADECIMAL
	}

	//========================= STATIC CONSTANTS ===============================
	private static final Logger LOGGER = LogManager.getLogger(StringHelper.class);

	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static { }

	//========================= Static Methods =================================
	/**
	 * Will Replace all Non-ASCII (table 1) characters (and non-print ASCII [table 1] characters) with their XML Escaped equivalent.
	 * <p><b>Note:</b> Same as calling {@link #xmlEscapeNonAsciiPrintCharacters(String, NumberBase)}.</p>
	 *
	 * @param _string The String to Escape.
	 * @param _numberBase The Number System to Escape to.
	 *
	 * @return The given String with all the appropriate characters replaced; or {@code null}, if {@code null} is given.
	 *
	 * @throws IllegalArgumentException If the given {@link NumberBase} is {@code null} or unknown.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #xmlEscapeNonAsciiPrintCharacters(String, NumberBase)
	 */
	public static String htmlEscapeNonAsciiPrintCharacters(String _string, NumberBase _numberBase) {
		return xmlEscapeNonAsciiPrintCharacters(_string, _numberBase);
	}

	/**
	 * Will Replace all Non-ASCII (table 1) characters (and non-print ASCII [table 1] characters) with their XML Escaped equivalent.
	 * <p><b>Note:</b> Same as calling {@link XmlDocumentHelper#escapeExtendedCharacters(String, XmlDocumentHelper.XmlEntityFormat)}.</p>
	 *
	 * @param _string The String to Escape.
	 * @param _numberBase The Number System to Escape to.
	 *
	 * @return The given String with all the appropriate characters replaced; or {@code null}, if {@code null} is given.
	 *
	 * @throws IllegalArgumentException If the given {@link NumberBase} is {@code null} or unknown.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see XmlDocumentHelper#escapeExtendedCharacters(String, XmlDocumentHelper.XmlEntityFormat)
	 */
	public static String xmlEscapeNonAsciiPrintCharacters(String _string, NumberBase _numberBase) {
		return XmlDocumentHelper.escapeExtendedCharacters(_string,
				_numberBase == NumberBase.DECIMAL ? XmlDocumentHelper.XmlEntityFormat.DECIMAL : XmlDocumentHelper.XmlEntityFormat.HEX);
	}

	/**
	 * Replaces all contiguous Whitespace characters with one space.
	 * <p>
	 *     <b>Note:</b> Same as calling {@code StringHelper.replace(_string, " ", CharacterPosition.ANYWHERE, true, CharacterSet.ALL_WHITESPACE);}.
	 * </p>
	 *
	 * @param _string
	 * 		The input String to normalize.
	 *
	 * @return A normalized version of the input String.
	 *
	 * @throws IllegalArgumentException
	 * 		If the input string is {@code null}.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String normalize(String _string) {

		LOGGER.info("normalize(_string: {}) [START]", (_string == null ? "(NULL)" : _string));

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_string, "String");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		String result = replace(_string, " ", CharacterPosition.ANYWHERE, true, CharacterSet.ALL_WHITESPACE);

		LOGGER.debug("normalize(_string: {}) - {} - [END]", _string, result);

		return result;
	}

	/**
	 * Removes whitespace, of all kinds, from the given string.
	 * <p>
	 *     <b>Note:</b> Same as calling {@code StringHelper.replace(_string, "", CharacterPosition.ANYWHERE, false|true, CharacterSet.ALL_WHITESPACE);}.
	 * </p>
	 *
	 * @param _string
	 * 		The string to remove the whitespace from.
	 *
	 * @return The input string with all of the whitespace removed.
	 *
	 * @throws IllegalArgumentException
	 * 		If the input string is {@code null}.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String removeWhitespace(String _string) {

		LOGGER.info("removeWhitespace(_string: {}) [START]", (_string == null ? "(NULL)" : _string));

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_string, "String");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		String result = replace(_string, "", CharacterPosition.ANYWHERE, false, CharacterSet.ALL_WHITESPACE);

		LOGGER.debug("removeWhitespace(_string: {}) - {} - [END]", _string, result);

		return result;
	}

	/**
	 * Replaces all characters of the given Character Set with the given replacement String.
	 *
	 * @param _haystack
	 * 		The String to do the Search and Replace on.
	 * @param _replacement
	 * 		The String use as a replacement.
	 * @param _position
	 * 		Where in the _haystack to search for the _characterSets.
	 * @param _normalize
	 * 		If true, every contiguous batch of characters in the given Character Set will be replaced with just one instance of the replacement String;
	 * 		otherwise, all characters get replaced with the replacement String.
	 * @param _characterSet
	 * 		The Character Set to replace.
	 *
	 * @return The input String with the given Character Set replaced by the given replacement String.
	 *
	 * @throws IllegalArgumentException
	 * 		if the haystack string is {@code null} or no {@link CharacterSet} is given.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String replace(String _haystack, String _replacement, CharacterPosition _position, boolean _normalize, CharacterSet... _characterSet) {

		LOGGER.info("replace(_haystack: {}, _replacement: {}, _position: {}, _normalize: {}, _characterSet: {}) [START]",
				_haystack, _characterSet, _replacement, _position, _normalize);

		//------------------------ CONSTANTS -----------------------------------
		if(_haystack == null) {
			throw new IllegalArgumentException("Given Haystack String cannot be NULL!");
		}

		if(_characterSet == null || _characterSet.length < 1) {
			throw new IllegalArgumentException("At least 1 Character set must be given!");
		}

		//------------------------ Variables -----------------------------------
		StringBuilder characterSetsRegex;
		String pattern;
		String toRet;

		//------------------------ Code ----------------------------------------
		if(_characterSet.length > 1) {
			
			characterSetsRegex = new StringBuilder("[");
			for(CharacterSet cs : _characterSet) {
				characterSetsRegex.append(cs);
			}
			characterSetsRegex.append("]");
		}
		else {
			characterSetsRegex = new StringBuilder(_characterSet[0].toString());
		}

		switch(_position) {
			case ANYWHERE:
				pattern = characterSetsRegex + (_normalize ? "+" : "");
				break;
			case BEGINNING_OF_STRING:
				pattern = "^" + characterSetsRegex + (_normalize ? "+" : "");
				break;
			case MIDDLE_OF_STRING:
				pattern = "(?<!^)" + characterSetsRegex + (_normalize ? "+" : "") + "(?!$)";
				break;
			case END_OF_STRING:
				pattern = characterSetsRegex + (_normalize ? "+" : "") + "$";
				break;
			case BEGINNING_OR_END:
				pattern = "^" + characterSetsRegex + (_normalize ? "+" : "") + "|" + characterSetsRegex + (_normalize ? "+" : "") + "$";
				break;
			default:
				throw new RuntimeException("Unknown position \"" + _position + "\"!");
		}

		toRet = _haystack.replaceAll(pattern, _replacement);

		LOGGER.debug("replace(_haystack: {}, _replacement: {}, _position: {}, _normalize: {}, _characterSet: {}) [END]",
				_haystack, _replacement, _position, _normalize, _characterSet);

		return toRet;
	}

	/**
	 * Same as {@link String#replaceFirst(String, String)}, except it replaces the last occurrence and the "_needle" is <b>NOT</b> a REGEX.
	 *
	 * @param _haystack
	 * 		The String to do the Search and Replace on.
	 * @param _needle
	 * 		The substring to search for and replace.
	 * @param _replacement
	 * 		The new VALUE to be put in place of the search String.
	 *
	 * @return The new string, with the replacement made, if the substring was found; or the original String, if the substring was <i>not</i> found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String replaceLast(String _haystack, String _needle, String _replacement) {

		LOGGER.info("replaceLast(_haystack: {}, _needle: {}, _replacement: {}) [START]", _haystack, _needle, _replacement);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		int index = _haystack.lastIndexOf(_needle);
		String toRet = _haystack;

		//------------------------ Code ----------------------------------------
		if(index > -1) {
			toRet = new StringBuilder(_haystack).replace(index, index + _needle.length(), _replacement).toString();
		}

		LOGGER.debug("replaceLast(_haystack: {}, _needle: {}, _replacement: {}) [END]", _haystack, _needle, _replacement);

		return toRet;
	}

    /**
     * Will write the given {@link String} to the given {@link File}.
     * <p>
     * <b>Note:</b>
     * If the file does not exit, it will be created, along with any missing path folders. Or if the File Exists, it will be overwritten.
     * </p>
     *
     * @param _string
     *         The {@link String} to be written to the {@link File}.
     * @param _file
     *         The {@link File} to write the {@link String} into.
     * @param _options
     *         <i>Optional</i> File Open Options to overwrite default behavior (see: {@link java.nio.file.StandardOpenOption}).
     *
     * @return The given {@link File} for method call chaining.
     *
     * @throws IllegalArgumentException
     *         If the given {@link String} or {@link File} are {@code null}.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public File toFile(String _string, File _file, OpenOption... _options) {

        LOGGER.info("toFile(_string: {}, _file: {}) [START]", _string, _file == null ? "(NULL)" : _file.getAbsolutePath());

        //------------------------ Pre-Checks ----------------------------------
        ArgumentChecks.notNull(_string, "String");
        ArgumentChecks.notNull(_file, "File");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        try {
            Files.write(_file.toPath(), _string.getBytes(), _options);
        }
        catch(IOException e) {
            throw new RuntimeException("Could not write to file!\n\tFile: " + _file.getAbsolutePath() + "\n\tString: " + _string, e);
        }

        LOGGER.debug("toFile(_string: {}, _file: {}) [END]", _string, _file.getAbsolutePath());

        return _file;
    }

	/**
	 * Converts a File into a String.
	 *
	 * @param _file
	 * 		The file to pull the data out of and put in a String.
	 *
	 * @return The contents of the File, or {@code null}, if the file could not be found.
	 *
	 * @throws NullPointerException
	 * 		If {@code _file} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(File _file) {

		LOGGER.info("toString(_file: {}) [START]", _file == null ? "(NULL)" : _file.getAbsolutePath());

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_file, "File");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Scanner scanner;
		String string;

		//------------------------ Code ----------------------------------------
		try {
			//noinspection ConstantConditions
			scanner = new Scanner(_file, "UTF-8");
			scanner.useDelimiter("\\Z");
			string = scanner.hasNext() ? scanner.next() : "";
			scanner.close();
		}
		catch(final FileNotFoundException e) {
			return null;
		}

		LOGGER.debug("toString(_file: {}) [END]", _file.getAbsolutePath());

		return string;
	}

	/**
	 * Converts an InputStream into a String.
	 * <p>
	 *     <b>Note:</b> The given Input Stream is closed.
	 * </p>
	 *
	 * @param _stream
	 * 		The Input Stream to pull data from and put in a String.
	 *
	 * @return The converted contents of the InputStream, or an Empty String, if the InputStream is empty.
	 *
	 * @throws NullPointerException
	 * 		If {@code _stream} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String toString(InputStream _stream) {

		LOGGER.info("toString(InputStream) [START]");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Scanner scanner = new Scanner(_stream, "UTF-8");  // NullPointerException is thrown here if _file is NULL.
		String string;

		//------------------------ Code ----------------------------------------
		scanner.useDelimiter("\\Z");
		string = scanner.hasNext() ? scanner.next() : "";
		scanner.close();

		LOGGER.debug("toString(InputStream) [END]");

		return string;
	}

	/**
	 * Will remove all whitespace from the Beginning and End of the given string.
	 * <p>
	 *     <b>Note:</b> Same as calling {@code StringHelper.replace(_string, "", CharacterPosition.BEGINNING_OR_END, true, CharacterSet.ALL_WHITESPACE);}.
	 * </p>
	 *
	 * @param _string
	 * 		The String to trim;
	 *
	 * @return A trimmed version of the given String.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String trim(String _string) {
		return replace(_string, "", CharacterPosition.BEGINNING_OR_END, true, CharacterSet.ALL_WHITESPACE);
	}

	//========================= CONSTANTS ======================================

	//========================= Variables ======================================

	//========================= Constructors ===================================
	/**
	 * Immutable Class.
	 */
	private StringHelper() {

		super();

		LOGGER.error("StringHelper()"); // Should never get instantiated.
	}

	//========================= Methods ========================================

	//========================= Classes ========================================
}
