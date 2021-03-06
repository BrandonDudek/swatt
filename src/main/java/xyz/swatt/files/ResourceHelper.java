/*
 * Created on 2019-07-26 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>) for {swatt}.
 */
package xyz.swatt.files;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helps get Resource Files our of a Build Directory or an Executing Jar.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class ResourceHelper {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	public static final boolean RESOURCES_ARE_IN_JAR;
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ResourceHelper.class);
	
	public static final String PATH_SEPARATOR = "/";
	
	/*
	 * class.getResource() is used, because it is the default way to load resources.
	 * - class.getClassLoader().getResource() is used to force full paths when loading classes
	 * - Thread.getContextClassLoader().getResource() is used when you want to share a class instance between clasesses in a single thread
	 * Because this class is just to load resources files, it doesn't need anythinig other than the default Resource Loader.
	 */
	/*
	 * Passing in "/" to class.getResource() is the only way to get the root Resource location both a build Directory and an Executing Jar.
	 * - "" returns current class path for Build Directory and Executing Jar.
	 * - "." returns current class path for Build Directory  but `null` for Executing Jar.
	 */
	/**
	 * The path to the default, root Resources location.
	 */
	public static final URL RESOURCES_URL = ResourceHelper.class.getResource(PATH_SEPARATOR); // TODO: Ensure that this works of there is a space in the path.
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {
		switch(RESOURCES_URL.getProtocol().toLowerCase()) {
			case "jar":
				RESOURCES_ARE_IN_JAR = true;
				break;
			case "file":
				RESOURCES_ARE_IN_JAR = false;
				break;
			default:
				throw new RuntimeException("Resources are in unknown location type: " + RESOURCES_URL);
		}
	}
	
	//========================= Static Methods =================================
	//-------------------- File --------------------
	
	/**
	 * Gets a single Resource file, as an {@link InputStream}.
	 *
	 * @param _path
	 * 		The path to the Resource file to return.
	 * 		<p>(The path must start at the Resource folder [exclusive], use only forward slashes, and be prefixed with a forward slash.)</p>
	 * 		<p>(i.e. If the resource is <i>src\main\resources\resource_folder\reource_file.txt</i>,
	 * 		then you will pass in "/resource_folder/reource_file.txt".)</p>
	 * 		<p>(<i>Note:</i> the passed in path will be trimmed, prefixed with a forward slash if needed,
	 * 		and all backslashes will be replaced with forward slashes.)</p>
	 *
	 * @return The requested resource as an {@link InputStream}, because the resource my be in a Jar;
	 * or {@code null}, if the resource was not found.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Path is blank.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static InputStream getResource(String _path) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		InputStream is;
		
		//------------------------ Code ----------------------------------------
		_path = fixPath(_path, false); // Validation done here.
		
		// Getting a single Resource file, as a stream, is the same for both a build Directory and an Executing Jar.
		is = ResourceHelper.class.getResourceAsStream(_path);
		
		return is;
	}
	
	/**
	 * Gets a single Resource file, as a {@link String}.
	 *
	 * @param _path
	 * 		The path to the Resource file to return.
	 * 		<p>(The path must start at the Resource folder [exclusive], use only forward slashes, and be prefixed with a forward slash.)</p>
	 * 		<p>(i.e. If the resource is <i>src\main\resources\resource_folder\reource_file.txt</i>,
	 * 		then you will pass in "/resource_folder/reource_file.txt".)</p>
	 * 		<p>(<i>Note:</i> the passed in path will be trimmed, prefixed with a forward slash if needed,
	 * 		and all backslashes will be replaced with forward slashes.)</p>
	 * @param _charset
	 * 		The encoding if the file to return, as a string.
	 *
	 * @return The requested resource as a {@link String}; or {@code null}, if the resource was not found.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Path is blank or if the given Charset is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String getResourceAsString(String _path, Charset _charset) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		InputStream is = getResource(_path); // Argument validation is done inside.
		String toRet = streamToString(is, _charset); // Argument validation is done inside.
		
		//------------------------ Code ----------------------------------------
		return toRet;
	}
	
	// TODO: public static String getResourceAsFile(String _path) // Returns a copy of the file, in the temp directory.
	
	//-------------------- Files --------------------
	
	/**
	 * Gets a concurrent collection of Resource files, as {@link InputStream}s, from a given Resource folder Path.
	 * <p>
	 * <i>Note:</i> This will only return files that are diretly under the given Resource folder, but <b>no</b> files in subfolders.
	 * </p>
	 *
	 * @param _path
	 * 		The path to a Resource folder, to parse for files.
	 * 		<p>(The path must start at the Resource folder [exclusive], use only forward slashes, and be prefixed ans suffixed with a forward slash.)</p>
	 * 		<p>(i.e. If the resource is <i>src\main\resources\resource_folder</i>,
	 * 		then you will pass in "/resource_folder/".)</p>
	 * 		<p>(<i>Note:</i> the passed in path will be trimmed, prefixed/suffixed with a forward slash if needed,
	 * 		and all backslashes will be replaced with forward slashes.)</p>
	 *
	 * @return All files directly under the given Resource folder, as a concurrent collection of {@link InputStream}s (because the resource my be in a Jar);
	 * or {@code null}, if the Resource folder was not found, or an empty collection, if the Resource folder had no files.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Path is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Set<InputStream> getResources(String _path) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Set<Path> paths = getResourcesAsPaths(_path); // Validation done here.
		Set<InputStream> streams;
		
		//------------------------ Code ----------------------------------------
		if(paths == null) {
			streams = null;
		}
		else {
			streams = new ConcurrentHashMap<>().newKeySet(paths.size());
			
			paths.parallelStream().forEach(path -> {
				streams.add(getResource(path.toString()));
			});
		}
		
		return streams;
	}
	
	/**
	 * Gets a concurrent collection of Resource files, as {@link String}s, from a given Resource folder Path.
	 * <p>
	 * <i>Note:</i> This will only return files that are diretly under the given Resource folder, but <b>no</b> files in subfolders.
	 * </p>
	 *
	 * @param _path
	 * 		The path to a Resource folder, to parse for files.
	 * 		<p>(The path must start at the Resource folder [exclusive], use only forward slashes, and be prefixed ans suffixed with a forward slash.)</p>
	 * 		<p>(i.e. If the resource is <i>src\main\resources\resource_folder</i>,
	 * 		then you will pass in "/resource_folder/".)</p>
	 * 		<p>(<i>Note:</i> the passed in path will be trimmed, prefixed/suffixed with a forward slash if needed,
	 * 		and all backslashes will be replaced with forward slashes.)</p>
	 * @param _charset
	 * 		The encoding if the file to return, as a string.
	 *
	 * @return All files directly under the given Resource folder, as a concurrent collection of {@link String}s;
	 * or {@code null}, if the Resource folder was not found, or an empty collection, if the Resource folder had no files.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Path is blank or if the given Charset is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Set<String> getResourcesAsStrings(String _path, Charset _charset) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Set<InputStream> streams = getResources(_path); // Validation done here.
		Set<String> strings;
		
		//------------------------ Code ----------------------------------------
		if(streams == null) {
			strings = null;
		}
		else {
			strings = new ConcurrentHashMap<>().newKeySet(streams.size());
			
			streams.parallelStream().forEach(stream -> {
				
				String str = streamToString(stream, _charset); // Validation done here.
				strings.add(str);
			});
		}
		
		return strings;
	}
	
	// TODO: public static Set<Files> getResourcesAsFiles(String _path) // Returns a copy of the files, from a folder in the temp directory.
	
	/**
	 * Gets a concurrent collection of Resource file {@link Path}s, from a given Resource folder Path.
	 * <p>
	 * <i>Note:</i> This will only return files that are diretly under the given Resource folder, but <b>no</b> files in subfolders.
	 * </p>
	 *
	 * @param _path
	 * 		The path to a Resource folder, to parse for files.
	 * 		<p>(The path must start at the Resource folder [exclusive], use only forward slashes, and be prefixed ans suffixed with a forward slash.)</p>
	 * 		<p>(i.e. If the resource is <i>src\main\resources\resource_folder</i>,
	 * 		then you will pass in "/resource_folder/".)</p>
	 * 		<p>(<i>Note:</i> the passed in path will be trimmed, prefixed/suffixed with a forward slash if needed,
	 * 		and all backslashes will be replaced with forward slashes.)</p>
	 *
	 * @return All files directly under the given Resource folder, as a concurrent collection of {@link Path}s;
	 * or {@code null}, if the Resource folder was not found, or an empty collection, if the Resource folder had no files.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Path is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Set<Path> getResourcesAsPaths(String _path) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Set<Path> paths = null;
		
		//------------------------ Code ----------------------------------------
		_path = fixPath(_path, true); // Validation done here.
		
		if(RESOURCES_ARE_IN_JAR) {
			
			String pathBase = StringUtils.substringBetween(RESOURCES_URL.getPath(), "!", "!").substring(1);
			String fullPath = pathBase + _path;
			
			CodeSource codeSrc = ResourceHelper.class.getProtectionDomain().getCodeSource();
			if(codeSrc == null) {
				throw new RuntimeException("Could not find Execuing Jar!");
			}
			
			try(ZipInputStream jar = new ZipInputStream(RESOURCES_URL.openStream())) {
				
				while(true) {
					
					ZipEntry entry;
					try {
						entry = jar.getNextEntry();
					}
					catch(IOException e) {
						throw new RuntimeException("Could not parse Executing Jar!");
					}
					if(entry == null) {
						break;
					}
					
					String name = entry.getName();
					if(name.startsWith(fullPath)) {
						
						String fileName = name.substring(fullPath.length());
						if(fileName.isEmpty()) { // Folder found.
							paths = new ConcurrentHashMap<>().newKeySet();
						}
						else if(!fileName.contains(PATH_SEPARATOR)) {
							
							String path = name.substring(pathBase.length());
							paths.add(Paths.get(path));
						} // Else, entry is a sub-folder or file in a sub-folder.
					}
				}
			}
			catch(IOException e) {
				throw new RuntimeException("Could not open Executing Jar!");
			}
		}
		else { // Rewource not in JAR.
			URL url = ResourceHelper.class.getResource(_path);
			String fullPath;
			try {
				fullPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.toString());
			}
			catch(UnsupportedEncodingException e) {
				throw new RuntimeException("Invalid Path: `" + _path + "` !", e);
			}
			
			File folder = new File(fullPath);
			if(folder.exists()) {
				
				File[] files = folder.listFiles(pathname -> pathname.isFile());
				paths = new ConcurrentHashMap<>().newKeySet(files.length);
				
				for(File file : files) {
					paths.add(Paths.get(_path, file.getName()));
				}
			}
		}
		
		return paths;
	}
	
	//========================= Helper Static Methods ==========================
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static String fixPath(String _path, boolean _isFolder) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotBlank(_path, "Path");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		_path = _path.trim().replace("\\", PATH_SEPARATOR);
		if(!_path.startsWith(PATH_SEPARATOR)) {
			_path = PATH_SEPARATOR + _path;
		}
		if(_isFolder && !_path.endsWith(PATH_SEPARATOR)) {
			_path += PATH_SEPARATOR;
		}
		
		return _path;
	}
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static String streamToString(InputStream _stream, Charset _charset) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_charset, "Charset Encoding");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		String toRet = null;
		
		//------------------------ Code ----------------------------------------
		if(_stream != null) {
			try {
				toRet = IOUtils.toString(_stream, _charset);
			}
			catch(IOException e) {
				throw new RuntimeException("Could not parse Resouce into a String with " + _charset + " encoding.", e);
			}
			finally {
				try {
					_stream.close();
				}
				catch(IOException e) {
					// Ignore.
				}
			}
		}
		
		return toRet;
	}
	
	//========================= CONSTANTS ======================================
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	//========================= Public Methods =================================
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
}
