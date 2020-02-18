package xyz.swatt.asserts;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Tests for method Arguments.
 * <p>
 *     If Test fails, it will throw an {@link IllegalArgumentException}!
 * </p>
 */
@SuppressWarnings("Duplicates")
@LogMethods
public class ArgumentChecks {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	private static final Logger LOGGER = LogManager.getLogger(ArgumentChecks.class);
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Public Static Methods ==========================
	/**
	 * Check a given {@link File} to ensure that is it Exists.
	 *
	 * @param _file
	 * 		The {@link File} to Check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@link File} is {@code null}, Does Not Exist, or is a Folder.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void fileExists(File _file, String _argumentName) throws IllegalArgumentException {

		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		if(_file == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "File cannot be NULL!");
		}
		else if(!_file.exists()) {
			throw new IllegalArgumentException("Given " + _argumentName + "File Does Not Exist!\n\tPath: " + _file.getAbsolutePath());
		}
		else if(!_file.isFile()) {
			throw new IllegalArgumentException("Given " + _argumentName + "File is actually a Folder!\n\tPath: " + _file.getAbsolutePath());
		}
	}

	/**
	 * Check a given Folder to ensure that is it Exists.
	 *
	 * @param _folder
	 * 		The Folder to Check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the Folder is {@code null}, Does Not Exist, or is a File.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void folderExists(File _folder, String _argumentName) throws IllegalArgumentException {

		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		if(_folder == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "Folder cannot be NULL!");
		}
		else if(!_folder.exists()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Folder Does Not Exist!\n\tPath: " + _folder.getAbsolutePath());
		}
		else if(!_folder.isDirectory()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Folder is actually a File!\n\tPath: " + _folder.getAbsolutePath());
		}
	}
	
	/**
	 * Check a given number to ensure that it is not {@code null} and greater than {@code 0}.
	 *
	 * @param _number
	 * 		The number to check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given number is {@code <= 0}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void greaterThanZero(Number _number, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		notNull(_number, _argumentName);
		
		if(_number.doubleValue() <= 0) {
			throw new IllegalArgumentException("Given " + _argumentName + "Number (" + _number + ") must be greater than 0!");
		}
	}
	
	/**
	 * Check a given {@link Collection} to ensure that is it exists and has entries.
	 *
	 * @param _arg
	 * 		The {@link Collection} to check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@link Collection} is {@code null} oe empty.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	public static void notEmpty(Collection _arg, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ Code ----------------------------------------
		if(CollectionUtils.isEmpty(_arg)) {
			throw new IllegalArgumentException("Given " + _argumentName + "Collection cannot be Empty!");
		}
	}
	
	/**
	 * Check a given number to ensure that it is not {@code null} and not negative.
	 *
	 * @param _number
	 * 		The number to check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given number is {@code < 0}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void notNegative(Number _number, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		notNull(_number, _argumentName);
		
		if(_number.doubleValue() < 0) {
			throw new IllegalArgumentException("Given " + _argumentName + "Number (" + _number + ") cannot be Negative!");
		}
	}
	
	/**
	 * Check a given Object to ensure that is it exists.
	 *
	 * @param _arg
	 * 		The Object to Check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the Object is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void notNull(Object _arg, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		if(_arg == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "Object cannot be NULL!");
		}
	}
	
	/**
	 * Checks that a given {@link Path} is Absolute and points to a File, without looking at the file system.
	 * <p>
	 *     <b>Note:</b> Absolute does not take System into account.
	 *     (So paths that start with "C:/" or "/" are both considered absolute.)
	 * </p>
	 * <p>
	 *     <b>Note:</b> File check will return false negative, if it points to a file without an extension.
	 * </p>
	 *
	 * @param _path
	 * 		The {@link Path} to Check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @return A {@link Path} representing the given {@link String}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@link Path} is {@code null}, Empty, Whitespace Only, not valid, not Absolute, or points to a Folder (not always caught).
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Path pathIsAbsoluteFile(String _path, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Path path;
		
		//------------------------ Code ----------------------------------------
		if(_path == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be NULL!");
		}
		else if(_path.isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be an Empty String!");
		}
		else if(_path.trim().isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be a Whitespace Only String!");
		}
		
		try {
			path = Paths.get(_path);
		}
		catch(InvalidPathException e) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path is not valid!\n\tPath: " + _path, e);
		}
		
		if(!_path.startsWith("\\") && /*Unix*/ !_path.startsWith("/") /*Unix*/ &&  !_path.matches("[A-Z]:[\\\\/].*") /*Windows*/) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path must Absolute!");
		}
		
		if(FilenameUtils.getName(_path).isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path points to a Folder!");
		}
		
		return path;
	}

	/**
	 * Checks that a given {@link Path} is Absolute and points to a Folder, without looking at the file system.
	 * <p>
	 *     <b>Note:</b> Absolute does not take System into account.
	 *     (So paths that start with "C:/" or "/" are both considered absolute.)
	 * </p>
	 * <p>
	 *     <b>Note:</b> Folders with dots (.) in them, must end with a slash.
	 * </p>
	 *
	 * @param _path
	 * 		The {@link Path} to Check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @return A {@link Path} representing the given {@link String}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@link Path} is {@code null}, Empty, Whitespace Only, not valid, not Absolute, or points to a File (not always caught).
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Path pathIsAbsoluteFolder(String _path, String _argumentName) throws IllegalArgumentException {

		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Path path;

		//------------------------ Code ----------------------------------------
		if(_path == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be NULL!");
		}
		else if(_path.isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be an Empty String!");
		}
		else if(_path.trim().isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path cannot be a Whitespace Only String!");
		}

		try {
			path = Paths.get(_path);
		}
		catch(InvalidPathException e) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path is not valid!\n\tPath: " + _path, e);
		}

		if(!_path.startsWith("\\") && /*Unix*/ !_path.startsWith("/") /*Unix*/ && !_path.matches("[A-Z]:[\\\\/].*") /*Windows*/) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path must Absolute!\n\tPath: " + _path);
		}

		String extension = FilenameUtils.getExtension(_path);
		if(!extension.isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "Path points to a File!\n\tPath: " + _path);
		}
		
		return path;
	}
	
	/**
	 * Check a given {@link String} to ensure that it is not NULL, Empty, or only Whitespace.
	 *
	 * @param _string
	 * 		The {@link String} to check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link String} is {@code null}, Empty, or Whitespace only.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void stringNotBlank(String _string, String _argumentName) throws IllegalArgumentException {
		
		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		if(_string == null) {
			throw new IllegalArgumentException("Given " + _argumentName + "String cannot be NULL!");
		}
		else if(_string.isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "String cannot be an Empty String!");
		}
		else if(_string.trim().isEmpty()) {
			throw new IllegalArgumentException("Given " + _argumentName + "String cannot only contain Whitespace!");
		}
		// Same effect as: StringUtils.isBlank(CharSequence).
	}
	
	/**
	 * Check a given {@link String} to ensure that it is not NULL, Empty, or only Whitespace.
	 *
	 * @param _string
	 * 		The {@link String} to check.
	 * @param _argumentName
	 * 		The Name of the Argument being checked. (Can be {@code null} or an Empty String to ignore.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link String} is {@code null}, Empty, or Whitespace only.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @deprecated Use {@link #stringNotBlank(String, String)} instead.
	 */
	@Deprecated
	public static void stringNotWhitespaceOnly(String _string, String _argumentName) throws IllegalArgumentException {
		stringNotBlank(_string, _argumentName);
	}
	
	/**
	 * Check a given {@link String} to ensure that it is a validly formatted URL.
	 *
	 * @param _url
	 *         The {@link String} to Check.
	 * @param _argumentName
	 *         The Name of the Argument being checked. (Can be NULL or Empty String, to ignore.)
     *
     * @throws IllegalArgumentException
     *         If the given {@link String} is not a validly formatted XPath.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public static void urlIsValid(String _url, String _argumentName) throws IllegalArgumentException {

        //------------------------ Pre-Checks ----------------------------------
        _argumentName = formatArgumentName(_argumentName);

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        stringNotWhitespaceOnly(_url, _argumentName);

        try {
            new URL(_url).toURI();
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Given " + _argumentName + "URL is invalid!", e);
        }
    }

    /**
     * Check a given {@link String} to ensure that it is a validly formatted XPath.
	 *
	 * @param _xPath
	 *         The {@link String} to Check.
	 * @param _argumentName
	 *         The Name of the Argument being checked. (Can be NULL or Empty String, to ignore.)
	 *
	 * @throws IllegalArgumentException
     *         If the given {@link String} is not a validly formatted XPath.
     *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void xpathIsValid(String _xPath, String _argumentName) throws IllegalArgumentException {

		//------------------------ Pre-Checks ----------------------------------
		_argumentName = formatArgumentName(_argumentName);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
        stringNotWhitespaceOnly(_xPath, _argumentName);

		try {
			new Processor(false).newXPathCompiler().compile(_xPath).load();
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Given " + _argumentName + "XPath is invalid!", e);
		}
	}
	
	//========================= Helper Static Methods ==========================
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static String formatArgumentName(String _argumentName) {
		return (_argumentName == null || _argumentName.trim().isEmpty()) ? "" : _argumentName.trim() + " ";
	}

	//========================= CONSTANTS ======================================
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	//========================= Methods ========================================
}
