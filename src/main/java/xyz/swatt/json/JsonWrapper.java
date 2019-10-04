/*
 * Created on 2019-09-06 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.exceptions.JsonException;
import xyz.swatt.exceptions.TooManyResultsException;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A Helper Class for working with JSON.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class JsonWrapper {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(JsonWrapper.class);
	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule());
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static { }
	
	//========================= Static Methods =================================
	
	/**
	 * Will convert a given String into a Jaway {@link JsonPath}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static JsonPath toJsonPath(String _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath;
		
		//------------------------ Code ----------------------------------------
		try {
			jsonPath = JsonPath.compile(_jsonPath);
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Given JSON Path is invalid!", e);
		}
		
		return jsonPath;
	}
	
	//========================= CONSTANTS ======================================
	/**
	 * The Jaway {@link DocumentContext} that his JSON Helper is built around.
	 * You can use this directly to perform all Jayway JsonPath operations.
	 */
	public final DocumentContext JSON_DOCUMENT;
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * Will instantiate this object, for working with given JSON data.
	 *
	 * @param _jsonDocument
	 * 		The JSON {@link DocumentContext} to work with.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given JSON {@link DocumentContext} is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonWrapper(DocumentContext _jsonDocument) {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonDocument, "JSON Document");
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		JSON_DOCUMENT = _jsonDocument;
	}
	
	/**
	 * Will instantiate this object, for working with given JSON data.
	 *
	 * @param _jsonFile
	 * 		The JSON data to work with.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given JSON {@link File} is {@code null} or does not exist.
	 * @throws JsonException
	 * 		If the given JSON {@link File} could not be parsed.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonWrapper(File _jsonFile) {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.fileExists(_jsonFile, "JSON");
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		try {
			JSON_DOCUMENT = JsonPath.parse(_jsonFile);
		}
		catch(Exception e) {
			throw new JsonException("Could not parse JSON file: " + _jsonFile.getAbsolutePath() + "!");
		}
	}
	
	/**
	 * Will instantiate this object, for working with given JSON data.
	 * <p><b>Note:</b></p> Uses the defined Jackson {@link #OBJECT_MAPPER}.
	 *
	 * @param _jsonNode
	 * 		The JSON data to work with.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link JsonNode} is null.
	 * @throws JsonException
	 * 		If the given JSON Node could not be parsed or converted.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonWrapper(JsonNode _jsonNode) {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonNode, "JSON Node");
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		try {
			JSON_DOCUMENT = JsonPath.parse(OBJECT_MAPPER.writeValueAsString(_jsonNode));
		}
		catch(Exception e) {
			throw new JsonException("Could not convert from Jackson JsonNode to Jaway DocumentContext!", e);
		}
	}
	
	/**
	 * Will instantiate this object, for working with given JSON data.
	 *
	 * @param _jsonObject
	 * 		The JSON data to work with.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link JsonNode} is null.
	 * @throws JsonException
	 * 		If the given JSON Object could not be parsed or converted.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonWrapper(JsonObject _jsonObject) {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonObject, "JSON Object");
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		try {
			JSON_DOCUMENT = JsonPath.parse(_jsonObject.toString());
		}
		catch(Exception e) {
			throw new JsonException("Could not convert from Google's Gson, JsonObject to Jaway DocumentContext!", e);
		}
	}
	
	/**
	 * Will instantiate this object, for working with given JSON data.
	 *
	 * @param _jsonString
	 * 		The JSON data to work with.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given JSON {@link String} is blank.
	 * @throws JsonException
	 * 		If the given JSON {@link String} could not be parsed or converted.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonWrapper(String _jsonString) {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_jsonString, "JSON");
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		try {
			JSON_DOCUMENT = JsonPath.parse(_jsonString);
		}
		catch(Exception e) {
			throw new JsonException("Could not convert from JSON String to Jaway DocumentContext!", e);
		}
	}
	
	//========================= Public Methods =================================
	
	/**
	 * Will add the given object to the array specified by the JSON Path.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Entry in the Array.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void addToArray(JsonPath _jsonPath, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonPath, "JSON Path");
		// Goven Value can be null.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		JSON_DOCUMENT.add(_jsonPath, _value);
	}
	
	/**
	 * Will add the given object to the array specified by the JSON Path.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Entry in the Array.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is blank or invalid.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void addToArray(String _jsonPath, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		
		//------------------------ Code ----------------------------------------
		addToArray(jsonPath, _value);
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and delete all nodes found.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void deleteNodes(JsonPath _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonPath, "JSON Path");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		JSON_DOCUMENT.delete(_jsonPath);
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and delete all nodes found.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is blank or invalid.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void deleteNodes(String _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		
		//------------------------ Code ----------------------------------------
		deleteNodes(jsonPath);
	}
	
	/**
	 * @return The JSON Document that this {@link JsonWrapper} is build around, as a Jaway {@link DocumentContext}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DocumentContext getJsonAsDocumentContext() {
		return JSON_DOCUMENT;
	}
	
	/**
	 * @return The JSON Document that this {@link JsonWrapper} is build around, as a Jackson {@link JsonNode}.
	 * <p><b>Note:</b></p> Uses the defined Jackson {@link #OBJECT_MAPPER}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonNode getJsonAsJsonNode() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonNode jsonNode;
		String jsonString = getJsonAsString();
		
		//------------------------ Code ----------------------------------------
		try {
			jsonNode = OBJECT_MAPPER.readTree(jsonString);
		}
		catch(Exception e) {
			throw new JsonException("Cound not convert JSON Document to a JsonNode!", e);
		}
		
		return jsonNode;
	}
	
	/**
	 * @return The JSON Document that this {@link JsonWrapper} is build around, as a Google, Gson {@link JsonObject}.
	 * <p><b>Note:</b></p> Used the default Jackson {@link JsonParser}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public JsonObject getJsonAsJsonObject() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonObject jsonObject;
		String jsonString = getJsonAsString();
		
		//------------------------ Code ----------------------------------------
		try {
			jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
		}
		catch(Exception e) {
			throw new JsonException("Cound not convert JSON Document to a JsonObject!", e);
		}
		
		return jsonObject;
	}
	
	/**
	 * @return The JSON Document that this {@link JsonWrapper} is build around, as a {@link String}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getJsonAsString() {
		return JSON_DOCUMENT.jsonString();
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and return the result.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @return The result, as {@link String}s; or {@code null}, if no result was found.
	 * <p><b>Note:</b> If an Object or Array is found, it will be returned as a JSON String.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than 1 result is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getString(JsonPath _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.notNull(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		String toRet;
		List<String> results;
		
		//------------------------ Code ----------------------------------------
		results = getStrings(_jsonPath); // Argument Validation done inside.
		
		if(results.isEmpty()) {
			toRet = null;
		}
		else if(results.size() > 1) {
			throw new TooManyResultsException("Expect only 1 result, but " + results.size() + " were found!");
		}
		else {
			toRet = results.get(0);
		}
		
		return toRet;
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and return the result.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @return The result, as {@link String}s; or {@code null}, if no result was found.
	 * <p><b>Note:</b> If an Object or Array is found, it will be returned as a JSON String.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than 1 result is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getString(String _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		String toRet;
		
		//------------------------ Code ----------------------------------------
		toRet = getString(jsonPath);
		
		return toRet;
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and return the results.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @return A {@link List} will all of the results, as {@link String}s.
	 * <p><b>Note:</b> If an Object is found, it will be returned as a JSON String.
	 * If an Array is found, each of it's elements will be parsed and added to the returned {@link List}.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @throws JsonException
	 * 		If the given Path found an unknown JSON Object.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<String> getStrings(JsonPath _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.notNull(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Object jsonQueryResults;
		
		List<String> toRet = new LinkedList<>();
		
		//------------------------ Code ----------------------------------------
		try {
			jsonQueryResults = getObject(_jsonPath); // Argument Validation done inside.
			if(jsonQueryResults instanceof Integer || jsonQueryResults instanceof String || jsonQueryResults instanceof LinkedHashMap /*JSON Object*/) {
				toRet.add(jsonQueryResults.toString());
			}
			else if(jsonQueryResults instanceof JSONArray) {
				
				for(Object entry : ((JSONArray) jsonQueryResults)) {
					
					String entryAsString = entry.toString();
					toRet.add(entryAsString);
				}
			}
			else {
				throw new JsonException("JSON Path returned unknown JSON Object: " + jsonQueryResults.getClass());
			}
		}
		catch(PathNotFoundException e) {
			toRet.clear();
		}
		
		return toRet;
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and return the results.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @return A {@link List} will all of the results, as {@link String}s.
	 * <p><b>Note:</b> If an Object is found, it will be returned as a JSON String.
	 * If an Array is found, each of it's elements will be parsed and added to the returned {@link List}.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is blank or invalid.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<String> getStrings(String _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		
		List<String> toRet;
		
		//------------------------ Code ----------------------------------------
		toRet = getStrings(jsonPath);
		
		return toRet;
	}
	
	/**
	 * Will put the given &lt;key, object&gt; pair into the Map/Object specified by the JSON Path.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _key
	 * 		The Variable Name / Map Key to use for the new Value.
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Entry in the Map.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void putInMapOrObject(JsonPath _jsonPath, String _key, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonPath, "JSON Path");
		// Goven Value can be null.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		JSON_DOCUMENT.put(_jsonPath, _key, _value);
	}
	
	/**
	 * Will put the given &lt;key, object&gt; pair into the Map/Object specified by the JSON Path.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _key
	 * 		The Variable Name / Map Key to use for the new Value.
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Entry in the Map.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is blank or invalid.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void putInMapOrObject(String _jsonPath, String _key, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		
		//------------------------ Code ----------------------------------------
		putInMapOrObject(jsonPath, _key, _value);
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and delete all nodes found.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Node.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void setNode(JsonPath _jsonPath, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonPath, "JSON Path");
		// Goven Value can be null.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		JSON_DOCUMENT.set(_jsonPath, _value);
	}
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and delete all nodes found.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 * @param _value
	 * 		The Object to Serialize as the Value for the new Node.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is blank or invalid.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void setNode(String _jsonPath, Object _value) {
		
		//------------------------ Pre-Checks ----------------------------------
		//ArgumentChecks.stringNotWhitespaceOnly(_jsonPath, "JSON Path"); // Validation done in sub-method call.
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		JsonPath jsonPath = toJsonPath(_jsonPath); // Argument Validation done inside.
		
		//------------------------ Code ----------------------------------------
		setNode(jsonPath, _value);
	}
	
	//========================= Helper Methods =================================
	
	/**
	 * Will query this JSON Document, with the given JSON Path, and return the results.
	 *
	 * @param _jsonPath
	 * 		The {@link JsonPath} query to use.
	 * 		(See: <a href="https://github.com/json-path/JsonPath">JsonPath's Readme on GitHub</a> for details.)
	 *
	 * @return A {@link List} will all of the results, as an {@link Object}.
	 *
	 * @throws IllegalArgumentException
	 * 		The Json Path is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private Object getObject(JsonPath _jsonPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_jsonPath, "JSON Path");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Object toRet;
		
		//------------------------ Code ----------------------------------------
		try {
			toRet = JSON_DOCUMENT.read(_jsonPath);
		}
		catch(PathNotFoundException e) {
			toRet = null;
		}
		
		return toRet;
	}
	
	//========================= Classes ========================================
}
