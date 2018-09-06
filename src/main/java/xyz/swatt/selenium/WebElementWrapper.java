package xyz.swatt.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.w3c.dom.Document;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.exceptions.InvalidTypeException;
import xyz.swatt.exceptions.TooManyResultsException;
import xyz.swatt.exceptions.XmlException;
import xyz.swatt.string.StringHelper;
import xyz.swatt.xml.XmlDocumentHelper;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class is a wrapper for the Selenium {@link WebElement} class.
 * <p>
 *     It's purpose is to make common tasks easier and catch/fix common problems.
 * </p>
 * <p>
 *     All {@link WebDriver} interactions are {@code Synchronized} on the {@link WebDriverWrapper#LOCK} object.
 *     (The {@link WebDriver} / Web Browser can only do one thing at a time, just like a real person.)
 *     This allows Multi-Threading to be used with Selenium.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebElementWrapper {

	//========================= Static Enums ===================================

	//========================= STATIC CONSTANTS ===============================
	private static final Logger LOGGER = LogManager.getLogger(WebElementWrapper.class);

	//========================= Static Variables ===============================

	//========================= Static Constructor =============================
	static { }

	//========================= Static Methods =================================
	/**
	 * This method will take in a {@link WebElement}'s "toString" and come up with an XPath to locate the {@link WebElement} in the DOM.
	 * 
	 * @param _webElementToString
	 * 		The result of calling a {@link WebElement}'s "toString" function.
	 * 
	 * @return An XPath to locate the {@link WebElement} in the DOM.
	 * 		<p>(<i>Note:</i> XPath is "best guess".)</p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static String webElementToStringToXpath(String _webElementToString) {

		LOGGER.debug("webElementToStringToXpath( String: {}) [START]", _webElementToString);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------
		final String CSS_SELECTOR_STRING = "css selector: ";
		final String ID_SELECTOR_STRING = "id: ";
		final String XPATH_SELECTOR_STRING = "xpath: ";
		final String CSS_EXCLUDE_CHARACTERS_REGEX = "\\s#\\.:/";
		
		//------------------------ Variables -----------------------------------
		StringBuilder xpath = new StringBuilder();
		String[] selectorParts;
		
		//------------------------ Code ----------------------------------------
		_webElementToString = "]" + _webElementToString.substring(_webElementToString.indexOf("] -> "), _webElementToString.length() - 1);
		selectorParts = _webElementToString.split(Pattern.quote("]] -> "));
		
		for(String selectorPart2 : selectorParts) {
			
			if(selectorPart2.isEmpty()) {
				continue;
			}
			
			String selectorPart = StringHelper.normalize(selectorPart2).trim();
			
			if(selectorPart.startsWith(CSS_SELECTOR_STRING)) {
				
				selectorPart = "//" + selectorPart.substring(CSS_SELECTOR_STRING.length()).trim();
				selectorPart = selectorPart.replaceAll("[\\s]*>[\\s]*", "/"); // Note: This will cause problems if there is any quotes with ">" in them.
				selectorPart = selectorPart.replaceAll("[\\s]+", "//"); // Note: This will cause problems if there is any quotes with Whitespace in them.
				selectorPart = selectorPart.replaceAll("(/)#([^" + CSS_EXCLUDE_CHARACTERS_REGEX + "]+)", "$1*[@id='$2']");
				selectorPart = selectorPart.replaceAll("([^/])#([^" + CSS_EXCLUDE_CHARACTERS_REGEX + "]+)", "$1[@id='$2']");
				
				// :nth-child( has to be converted before @class.
				String tempString = "";
				while(!selectorPart.equals(tempString)) { // Supports multiple classes.
					
					if(!tempString.isEmpty()) {
						selectorPart = tempString;
					}

					// Have to switch child selector before class selector, because that is what CSS does internally.
					tempString = selectorPart.replaceAll("([.][^" + CSS_EXCLUDE_CHARACTERS_REGEX + "]+)(:nth-child\\([0-9]+\\))", "$2$1");
				}
				
				selectorPart = selectorPart.replaceAll("([/]):nth-child\\(([0-9]+)\\)", "$1*[$2]");
				selectorPart = selectorPart.replaceAll("([^/]):nth-child\\(([0-9]+)\\)", "$1[$2]");
				selectorPart = selectorPart.replaceAll("([/])\\.([^" + CSS_EXCLUDE_CHARACTERS_REGEX + "]+)", "$1*[contains(concat(' ',normalize-space(@class),' '),' $2 ')]");
				
				tempString = "";
				while(!selectorPart.equals(tempString)) { // Supports multiple classes.
					
					if(!tempString.isEmpty()) {
						selectorPart = tempString;
					}
					
					tempString = selectorPart.replaceAll("([^/])\\.([^" + CSS_EXCLUDE_CHARACTERS_REGEX + "]+)", "$1[contains(concat(' ',normalize-space(@class),' '),' $2 ')]");
				}
			}
			else if(selectorPart.startsWith(ID_SELECTOR_STRING)) {
				
				selectorPart = selectorPart.substring(ID_SELECTOR_STRING.length());
				selectorPart = "//*[@id='" + selectorPart + "']";
			}
			else if(selectorPart.startsWith(XPATH_SELECTOR_STRING)) {
				
				selectorPart = selectorPart.substring(XPATH_SELECTOR_STRING.length());
				
				if(selectorPart.startsWith(".")) {
					selectorPart = "/" + selectorPart;
				}
				else if(!selectorPart.startsWith("/")) {
					selectorPart = "//" + selectorPart;
				}
			}
			else {
				LOGGER.warn("Unknown Selector: " + selectorPart);
				
				// TODO: Handle more cases. (In the meantime, attempt to skip this part.)
				selectorPart = "";
			}
			
			xpath.append(selectorPart);
		}
		
		LOGGER.trace("webElementToStringToXpath(_webElementToString: {}) [END]", _webElementToString);
		
		return xpath.toString();
	}
	
	//========================= CONSTANTS ======================================
	private final String XPATH_IDS_SELECTOR;
	final WebDriverWrapper WEB_DRIVER_WRAPPER;

	//========================= Variables ======================================
	By originalBy;
	private String name, webElementToStringSelectorXpath;
	WebElement webElement;

	//========================= Constructors ===================================
	/**
	 * Creates this {@link WebElementWrapper} for use with a give Selenium {@link WebElement}.
	 *
	 * @param _driverWrapper
	 * 		The WebDriverWrapper that created this object.
	 * @param _webElement
	 * 		The {@link WebElement} to interact with.
	 * @param _byUsed
	 * 		Is passed in if the {@link By} that was used, resulted in only 1 {@link WebElement} being returned; otherwise, {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given WebDriverWrapper or {@link WebElement} are {@code null}.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@SuppressWarnings("unused")
	WebElementWrapper(WebDriverWrapper _driverWrapper, WebElement _webElement, By _byUsed) {

		LOGGER.info("WebElementWrapper(_driverWrapper: {}, _webElement: {}, _byUsed: {}) [START]",
				(_driverWrapper == null ? "(NULL)" : _driverWrapper.DRIVER.getTitle()), _webElement, _byUsed);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_driverWrapper, "WebDriverWrapper");
		ArgumentChecks.notNull(_webElement, "WebElement");

		//-------------------------CONSTANTS------------------------------------

		//-------------------------Variables------------------------------------

		//-------------------------Code-----------------------------------------
		WEB_DRIVER_WRAPPER = _driverWrapper;
		webElement = _webElement;
		originalBy = _byUsed;
		
		XPATH_IDS_SELECTOR = getXpathIdsSelector();
		
		// Don't allow reacquireWebElement() to be called from Constructor.
		// This could cause an infinite loop. [reacquireWebElement() used Constructor to create a new one.]

		//noinspection ConstantConditions
		LOGGER.debug("WebElementWrapper(_driverWrapper: {}, _webElement: {}, _byUsed: {}) [END]",
				_driverWrapper.DRIVER.getTitle(), _webElement, _byUsed);
	}
	
	//========================= Public Methods =================================
	/**
	 * If this {@link WebElement} is in Focus, Focus will be lost.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper blur() {

		LOGGER.info("blur() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			
			while(true) {
				
				try {
					((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("arguments[0].blur();", webElement);
					break;
				}
				catch(StaleElementReferenceException e) {
					
					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}
		
		LOGGER.debug("blur() [END]");
		
		return this;
	}

	/**
	 * Determines this {@link WebElement}'s "class" Attribute contains the given {@code _token}.
	 * <p>
	 *     Uses Word Breaks to ensure that it doesn't match other (super-string) tokens.
	 * </p>
	 *
	 * @param _token
	 * 		The word to match.
	 *
	 * @return {@code true}, if the {@code _token} was found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public boolean classContains(String _token) {

		LOGGER.info("classContains(_token: {}) [START]", _token);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		boolean contains = getAttribute("class").matches(".*\\b" + _token + "\\b.*");
		
		LOGGER.debug("classContains(_token: {}) - {} - [END]", _token, contains);
		
		return contains;
	}

	/**
	 * Calls {@link WebElement#clear()}.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * 
	 * @see WebElement#clear()
	 */
	public WebElementWrapper clearInput() {

		LOGGER.info("clearInput() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			while(true) {
				try {
					webElement.clear();
					break;
				}
				catch(StaleElementReferenceException e) {
					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}
		
		LOGGER.debug("clearInput() [END]");
		
		return this;
	}

	//////////////////// Click Functions [START] ////////////////////
	/**
	 * Hovers over the {@link WebElement}, then clicks it.
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the browser's viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     Same as calling {@link #click(boolean)} with a value of {@code false}.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with Selenium's built-in <i>click()</i> function.
	 *     In those cases, use {@link #javascriptClick()}.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s that need a Javascript Click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws WebDriverException
	 * 		If the {@link WebElement} is covered, and another {@link WebElement} would receive the click.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper click() {
		return click(false);
	}

	/**
	 * Hovers over the {@link WebElement}, then clicks it, and optionally waits for a page refresh.
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the browser's viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with Selenium's built-in <i>click()</i> function.
	 *     In those cases, use {@link #javascriptClick()}.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s that need a Javascript Click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * @param _waitForRefresh
	 * 		Whether to wait for a page reload, after the click.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws TimeoutException
	 * 		If the page never unloads, or if the page does not finish loading in {@link WebDriverWrapper#maxPageLoadTimeInSeconds}
	 * 		({@value WebDriverWrapper#DEFAULT_MAX_PAGE_LOAD_TIME_S} seconds).
	 * @throws WebDriverException
	 * 		If the {@link WebElement} is covered, and another {@link WebElement} would receive the click.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * 
	 * @see WebDriverWrapper#waitForPageLoad()
	 */
	public WebElementWrapper click(boolean _waitForRefresh) {
		return click(_waitForRefresh, true, false, null);
	}

	/**
	 * Waits for this {@link WebElement} to be click-able, hovers over it, then clicks it.
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the browser's viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with Selenium's built-in <i>click()</i> function.
	 *     In those cases, use {@link #javascriptClick()}.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s that need a Javascript Click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * @param _waitTimeInSeconds
	 * 		The maximum amount of time to wait for this {@link WebElement} to be click-able (<i>truncated to a {@code long}</i>).
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException
	 * 		If {@code _waitTimeInSeconds < 0}.
	 * @throws TimeoutException
	 * 		If this {@link WebElement} is not click-able after {@code _waitTimeInSeconds} seconds.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * 
	 * @see #waitForClickability(double)
	 */
	public WebElementWrapper click(double _waitTimeInSeconds) {
		return click(_waitTimeInSeconds, false);
	}

	/**
	 * Waits for this {@link WebElement} to be click-able, hovers over it, clicks it, and optionally waits for a page refresh.
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the browser's viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with Selenium's built-in <i>click()</i> function.
	 *     In those cases, use {@link #javascriptClick()}.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s that need a Javascript Click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * @param _waitTimeInSeconds
	 * 		The Maximum amount of time to wait for this {@link WebElement} to be click-able (<i>truncated to a {@code long}</i>).
	 * @param _waitForRefresh
	 * 		Whether to wait for a page reload, after the click.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException
	 * 		If {@code _waitTimeInSeconds < 0}.
	 * @throws TimeoutException
	 * 		If this {@link WebElement} is not click-able after {@code _waitTimeInSeconds}.
	 * 	 	<p>Or if the page never unloads, or if the page does not finish loading in {@link WebDriverWrapper#maxPageLoadTimeInSeconds} seconds.</p>
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * 
	 * @see #waitForClickability(double)
	 */
	public WebElementWrapper click(double _waitTimeInSeconds, boolean _waitForRefresh) {

		LOGGER.info("click(_waitTimeInSeconds: {}, _waitForRefresh: {}) [START]", _waitTimeInSeconds, _waitForRefresh);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) { // Synchronizing Chained Actions.
			
			waitForClickability(_waitTimeInSeconds);
			click(_waitForRefresh);
		}
		
		LOGGER.debug("click(_waitTimeInSeconds: {}, _waitForRefresh: {}) [END]", _waitTimeInSeconds, _waitForRefresh);
		
		return this;
	}

	/**
	 * Will hold down the {@link Keys#CONTROL} or {@link Keys#COMMAND} Key, while performing a click.
	 * <p>
	 *     Note: {@link Keys#COMMAND} for Mac and {@link Keys#CONTROL} for everything else.
	 * </p>
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #keyClick(CharSequence)
	 */
	public WebElementWrapper controlCommandClick() {
		return keyClick(WEB_DRIVER_WRAPPER.CTRL_CMD_KEY);
	}

	/**
	 * Will hold down Key(s) while performing a click.
	 *
	 * @param _keys The {@link Keys} or {@link Keys#chord(CharSequence...)} to hold down, while performing the click.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException If the given Keys are {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #click()
	 */
	public WebElementWrapper keyClick(CharSequence _keys) {

		LOGGER.info("keyClick(_keys: {}) [START]", _keys);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_keys, "Keys");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		click(false, true, false, _keys);

		LOGGER.debug("keyClick(_keys: {}) [END]", _keys);

		return this;
	}
	//////////////////// Click Functions [END] ////////////////////

	/**
	 * Will Un-Select all Option for a Multi-Select {@link WebElement}.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws UnexpectedTagNameException
	 * 		If this {@link WebElement} is not a Select.
	 * @throws UnsupportedOperationException
	 * 		If the Select does not support multiple selections
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper deselectAll() {

		LOGGER.info("deselectAll() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			
			Select select = new Select(webElement);
			select.deselectAll();
		}
		
		LOGGER.debug("deselectAll() [END]");
		
		return this;
	}

	/**
	 * Will perform a "double click" on this Element.
	 *
	 * @return This {@link WebElementWrapper}, for method call chaining purposes.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper doubleClick() {

		LOGGER.info("doubleClick() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions;

		//------------------------ Code ----------------------------------------
		actions = new Actions(WEB_DRIVER_WRAPPER.DRIVER);

		actions.doubleClick(webElement);

		performAction(actions);

		LOGGER.debug("doubleClick() [END]");

		return this;
	}

	/**
	 * Drags this {@link WebElement} to the middle of the given {@link WebElement}.
	 *
	 * @param _destinationElement
	 * 		Where to drag this {@link WebElement} to.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper dragTo(WebElementWrapper _destinationElement) {

		LOGGER.info("dragTo(WebElementWrapper _destinationElement) [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions clickAndHoldActions = new Actions(WEB_DRIVER_WRAPPER.DRIVER).clickAndHold(webElement);
		Actions releasedActions = new Actions(WEB_DRIVER_WRAPPER.DRIVER).release(webElement);
		
		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			
			hoverOver();

			performAction(clickAndHoldActions);

			try {
				Thread.sleep(300);// Can update, up to 0.5s.
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			_destinationElement.hoverOver();
			
			try {
				Thread.sleep(300); // Can update, up to 0.5s.
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			performAction(releasedActions);
		}
		
		LOGGER.debug("dragTo(WebElementWrapper _destinationElement) [END]");
		
		return this;
	}

	/**
	 * Will get the value of one of this Element's XML Attributes.
	 * <p>
	 *     <b>Note:</b> For CSS Properties use {@link #getCssValue(String)}.
	 * </p>
	 *
	 * @param _attributeName
	 *         The name of the Attribute value to return.
	 *
	 * @return The Attribute's current value or {@code null}, if the Attribute does not exist.
	 *
	 * @throws IllegalArgumentException If the given Attribute Name is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebElement#getAttribute(String)
	 * @see #getCssValue(String)
	 */
	public String getAttribute(String _attributeName) {

		LOGGER.info("getAttribute(_attributeName: {}) [START]", _attributeName);

		//------------------------ Pre-Checks ----------------------------------
		if(_attributeName == null) {
			throw new IllegalArgumentException("Given Attribute Name cannot be NULL!");
		}
		if(_attributeName.trim().isEmpty()) {
			throw new IllegalArgumentException("Given Attribute Name cannot be an Empty String or just Whitespace!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String value;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			while(true) {
				try {
					value = webElement.getAttribute(_attributeName);
					break;
				}
				catch(StaleElementReferenceException e) {
					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}

		LOGGER.debug("getAttribute(_attributeName: {}) - {} - [END]", _attributeName, value == null ? "(NULL)" : Quotes.escape(value));

		return value;
	}

	/**
	 * Will get the value of one of this Element's CSS Properties.
	 * <p>
	 *     <b>Note:</b> For XML Attributes use {@link #getAttribute(String)}.
	 * </p>
	 *
	 * @param _propertyName
	 *         The name of the Properties value to return.
	 *
	 * @return The Properties's current value or {@code null}, if the Properties is not set.
	 *
	 * @throws IllegalArgumentException If the given Properties Name is blank.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebElement#getCssValue(String)
	 * @see #getAttribute(String)
	 */
	public String getCssValue(String _propertyName) {

		LOGGER.info("getCssValue(_propertyName: {}) [START]", _propertyName);

		//------------------------ Pre-Checks ----------------------------------
		if(_propertyName == null) {
			throw new IllegalArgumentException("Given Attribute Name cannot be NULL!");
		}
		if(_propertyName.trim().isEmpty()) {
			throw new IllegalArgumentException("Given Attribute Name cannot be an Empty String or just Whitespace!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String value;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			while(true) {
				try {
					value = webElement.getCssValue(_propertyName);
					break;
				}
				catch(StaleElementReferenceException e) {
					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}

		LOGGER.debug("getCssValue( _propertyName: {}) - {} - [END]", _propertyName, value == null ? "(NULL)" : Quotes.escape(value));

		return value;
	}

	//////////////////// Get Descendant(s) Functions [START] ////////////////////
	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 *
	 * @return A {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 * 		
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by) {
		return getDescendant(_by, null, -1, null);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 *
	 * @return A {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, boolean _isVisible) {
		return getDescendant(_by, _isVisible, -1);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 *
	 * @return A {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, double _secondsToWait) {
		return getDescendant(_by, null, _secondsToWait, null);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _notFoundErrorMessage
	 * 		If <b>not</b> {@code null}, a {@link NoSuchElementException} will be thrown with this message, if no {@link WebElement} is found.
	 *
	 * @return a {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 * 		If no {@link WebElement} is found, and if the {@code _notFoundErrorMessage} argument is <b>not</b> {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, String _notFoundErrorMessage) {
		return getDescendant(_by, null, -1, _notFoundErrorMessage);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 *
	 * @return a {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #getDescendants(By _by, Boolean _isVisible, double _secondsToWait)
	 */
	public WebElementWrapper getDescendant(By _by, boolean _isVisible, double _secondsToWait) {
		return getDescendant(_by, _isVisible, _secondsToWait, null);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 * @param _notFoundErrorMessage
	 * 		If <b>not</b> {@code null}, a {@link NoSuchElementException} will be thrown with this message, if no {@link WebElement} is found.
	 *
	 * @return a {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 * 		If no {@link WebElement} is found, and if the {@code _notFoundErrorMessage} argument is <b>not</b> {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, boolean _isVisible, String _notFoundErrorMessage) {
		return getDescendant(_by, _isVisible, -1, _notFoundErrorMessage);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 * @param _notFoundErrorMessage
	 * 		If <b>not</b> {@code null}, a {@link NoSuchElementException} will be thrown with this message, if no {@link WebElement} is found.
	 *
	 * @return a {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 * 		If no {@link WebElement} is found, and if the {@code _notFoundErrorMessage} argument is <b>not</b> {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, double _secondsToWait, String _notFoundErrorMessage) {
		return getDescendant(_by, null, _secondsToWait, _notFoundErrorMessage);
	}

	/**
	 * Gets the descendant of this {@link WebElement} that matches the given search query.
	 *
	 * @param _by
	 * 		How to search for the descendant. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 * @param _notFoundErrorMessage
	 * 		If <b>not</b> {@code null}, a {@link NoSuchElementException} will be thrown with this message, if no {@link WebElement} is found.
	 *
	 * @return a {@link WebElementWrapper} that is the descendant of this {@link WebElement} and matches the given search query, if one exists; else returns {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 * 		If no {@link WebElement} is found, and if the {@code _notFoundErrorMessage} argument is <b>not</b> {@code null}.
	 * @throws TooManyResultsException
	 * 		If more than one descendant is found.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getDescendant(By _by, Boolean _isVisible, double _secondsToWait, String _notFoundErrorMessage) {

		LOGGER.info("getDescendant(_by: {}, _isVisible: {}, _secondsToWait: {}, _notFoundErrorMessage: {}) [START]", _by, _isVisible, _secondsToWait,
				(_notFoundErrorMessage == null ? "(NULL)" : Quotes.escape(_notFoundErrorMessage)));

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		WebElementWrapper descendant = null;
		List<WebElementWrapper> descendants;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			descendants = getDescendants(_by, _isVisible, _secondsToWait);

			switch(descendants.size()) {
				case 0:
					if(_notFoundErrorMessage != null) {
						throw new NoSuchElementException(_notFoundErrorMessage);
					}
					else {
						break;
					}
				case 1:
					descendant = descendants.get(0);
					break;
				default:
					throw new TooManyResultsException("ERROR! Only 1 Descendant expected, but " + descendants.size() + " were found!\nBy: " + _by);
			}
		}

		LOGGER.debug("getDescendant(_by: {}, _isVisible: {}, _secondsToWait: {}, _notFoundErrorMessage: {}) [END]", _by, _isVisible, _secondsToWait,
				(_notFoundErrorMessage == null ? "(NULL)" : Quotes.escape(_notFoundErrorMessage)));

		return descendant;
	}

	/**
	 * Gets all the descendants of this {@link WebElement} that match the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendants. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 *
	 * @return a List of {@link WebElementWrapper}s that are descendants of this {@link WebElement} and match the given search query;
	 * 		or an empty List, if no descendants were found.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getDescendants(By _by) {
		return getDescendants(_by, null, -1);
	}

	/**
	 * Gets all the descendants of this {@link WebElement} that match the given search query.
	 * <p>
	 *     <b>Note:</b> Waits {@code WebDriverWrapper#maxElementLoadTime} ({@link WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 *     for descendant(s) to exist.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the descendants. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 *
	 * @return a List of {@link WebElementWrapper}s that are descendants of this {@link WebElement} and match the given search query;
	 * 		or an empty List, if no descendants were found.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getDescendants(By _by, Boolean _isVisible) {
		return getDescendants(_by, _isVisible, -1);
	}

	/**
	 * Gets all the descendants of this {@link WebElement} that match the given search query.
	 *
	 * @param _by
	 * 		How to search for the descendants. (<i>Note: XPaths starting with "//" will be updated to start with ".//"</i>.)
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 *
	 * @return a List of {@link WebElementWrapper}s that are descendants of this {@link WebElement} and match the given search query;
	 * 		or an empty List, if no descendants were found.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given By object is {@code NULL}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getDescendants(By _by, double _secondsToWait) {
		return getDescendants(_by, null, _secondsToWait);
	}

	/**
	 * Gets all the descendants of this {@link WebElement} that match the given search query.
	 *
	 * @param _by
	 * 		How to search for the descendants.
	 * 		<p>
	 * 		    If looking for a direct decedent, do NOT start with the CSS "child combinator" selector (&gt;).
	 * 		    This CSS selector MUST be preceded by a parent query.
	 * 		    Instead use an XPath starting with "./".
	 * 		</p>
	 * 		<p>
	 * 		    <b>Note:</b> XPaths starting with "//" will be updated to start with ".//".
	 * 		</p>
	 * @param _isVisible
	 * 		If {@code true}, grabs only visible {@link WebElement}s.
	 * 		<p>If {@code false}, grabs only hidden {@link WebElement}s.</p>
	 * 		<p>If {@code null}, all {@link WebElement}s are returned.</p>
	 * @param _secondsToWait
	 * 		How long to wait (in seconds) for the descendant to exist.
	 * 		<p>(Will default to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * 		if {@code < 0} is passed in.</p>
	 *
	 * @return a List of {@link WebElementWrapper}s that are descendants of this {@link WebElement} and match the given search query;
	 * 		or an empty List, if no descendants were found.
	 *
	 * @throws IllegalArgumentException If the given {@link By} object is {@code NULL}.
	 *             <p>
	 *                 Or if the given {@link By} object is a CSS Selector starts with "&gt;".
	 *             </p>

	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getDescendants(By _by, Boolean _isVisible, double _secondsToWait) {

		LOGGER.info("getDescendants(_by: {}, _isVisible: {}, _secondsToWait: {}) [START]", _by, _isVisible, _secondsToWait);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_by, "By");

		if( _by instanceof By.ByCssSelector) { // See: https://github.com/SeleniumHQ/selenium/issues/2091

			String selector = _by.toString();
			if(selector.trim().startsWith( ">" )) {
				throw new IllegalArgumentException("Given CSS Selector cannot start with \">\"!");
			}
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<WebElementWrapper> descendants = null;

		//------------------------ Code ----------------------------------------
		_secondsToWait = _secondsToWait < 0 ? WebDriverWrapper.maxElementLoadTimeInSeconds : _secondsToWait;

		if(_by instanceof ByXPath) {

			// Force ".//".
			String xpath = _by.toString();
			xpath = xpath.substring(xpath.indexOf(" ") + 1).trim();
			if(xpath.startsWith("//") || xpath.startsWith("\\\\")) {
				_by = By.xpath("." + xpath);
			}
		}

		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			do {
				try {
					descendants = WEB_DRIVER_WRAPPER.getWebElementWrappers(webElement, _by, _secondsToWait, -1, _isVisible);
				}
				catch(StaleElementReferenceException e) {
					if(!reacquireWebElement()) {
						throw e;
					}
				}
				catch(JavascriptException e) {

					// Selenium 3 started throwing this exception instead of StaleElementReferenceException, only for get descendant calls.
					if(!e.getMessage().toLowerCase().startsWith("element reference not seen before: ") || !reacquireWebElement()) {
						throw e;
					}
				}
			} while(descendants == null);
		}

		LOGGER.debug("getDescendants(_by: {}, _isVisible: {}, _secondsToWait: {}) - ({}) - [END]", _by, _isVisible, _secondsToWait, descendants.size());

		return descendants;
	}
	//////////////////// Get Descendant(s) Functions [END] ////////////////////

	/**
	 * Gets all of the Select input's Drop-Down's Options.
	 *
	 * @return a List{@code <String>} of all of the Drop-Down's Options' Texts.
	 *
	 * @throws UnexpectedTagNameException
	 * 		If this {@link WebElement} is not a Select / Drop-Down.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<String> getSelectOptionsAvailable() {

		LOGGER.info("getSelectOptionsAvailable() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<String> options;
		List<WebElement> webElementOptions;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			webElementOptions = new Select(webElement).getOptions(); // Throws the UnexpectedTagNameException.
			if(webElementOptions == null || webElementOptions.isEmpty()) {
				return new ArrayList<>(0);
			}

			options = new ArrayList<>(webElementOptions.size());
			for(WebElement webElementOption : webElementOptions) {
				options.add(webElementOption.getText());
			}
		}

		LOGGER.debug("getSelectOptionsAvailable() - ({}) - [END]", options.size());

		return options;
	}

	/**
	 * Gets all of the Select input's Drop-Down's Options that are selected.
	 *
	 * @return a List{@code <String>} of all of the Drop-Down's Options' Texts that are selected..
	 *
	 * @throws UnexpectedTagNameException
	 * 		If this {@link WebElement} is not a Select / Drop-Down.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<String> getSelectOptionsSelected() {

		LOGGER.info("getSelectOptionsSelected() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<String> options = new LinkedList<>();
		List<WebElement> webElementOptions;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			webElementOptions = new Select(webElement).getOptions(); // Throws the UnexpectedTagNameException.
			if(webElementOptions != null) {

				for(WebElement webElementOption : webElementOptions) {

					if(webElementOption.isSelected()) {
						options.add(webElementOption.getText());
					}
				}
			}
		}

		LOGGER.debug("getSelectOptionsSelected() - ({}) - [END]", options.size());

		return options;
	}

	/**
	 * Get the tag name of this {@link WebElement}.
	 * (Not the value of the name attribute. i.e. Will return "input" for the {@link WebElement} {@code <input name="foo" />}.)
	 *
	 * @return The tag name of this {@link WebElement}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getTagName() {

		LOGGER.info("getTagName() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		if(name == null) { // Cashed because tag Name never changes.

			while(true) {

				try {
					name = webElement.getTagName();
					break;
				}
				catch(StaleElementReferenceException e) {

					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}

		LOGGER.debug("getTagName() - {} - [END]", name);

		return name;
	}

	/**
	 * Will return the value of this {@link WebElement} regardless of the type.
	 *
	 * <ul>
	 *     <li><strong>Input Element:</strong> "value" Attribute</li>
	 *     <li><strong>Text Area Element:</strong> "value" Attribute</li>
	 *     <li><strong>Select Element:</strong>
	 *         <ul>
	 *             <li>If no option(s) are selected, {@code null} is returned.</li>
	 *             <li>If only 1 option is selected, the value of the selected option is returned.</li>
	 *             <li>If multiple options are selected, a JSON representation of the selected options in list format is returned.
	 *             (<i>Note:</i> use {@link #getSelectOptionsSelected()} to get all values in List format.)</li>
	 *         </ul>
	 *     </li>
	 *     <li><strong>Links (Anchor Tags):</strong> Visible Text</li>
	 *     <li><strong>Regular Dom Element:</strong> Visible Text</li>
	 * </ul>
	 * <p>
	 *     <b>Note:</b> If returning the <i>Visible Text</i>, the Element will be scrolled to first.
	 * </p>
	 *
	 * @return The value of this {@link WebElement}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getValue() {

		LOGGER.info("getValue() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String tagName = null, value;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			///// Scroll element into view first (as a real user would have to do to get the value) /////
			// isDisplayed() will return false if the Element is in the Overflow of another Element.
			// If the Element is in the Overflow of another Element, getText() will return an empty String.
			// Try scrolling to it.
			if(!isDisplayed()) {
				javascriptScrollIntoView();
			}

			///// Get Element Type /////
			do {
				try {
					tagName = webElement.getTagName().toLowerCase();
				}
				catch(StaleElementReferenceException e) {

					if(!reacquireWebElement()) {
						throw e;
					}
				}
			} while(tagName == null);

			///// Get Value /////
			switch(tagName) {
				case "input":
				case "textarea":
					while(true) {
						try {
							value = getAttribute("value");
							break;
						}
						catch(StaleElementReferenceException e) {

							if(!reacquireWebElement()) {
								throw e;
							}
						}
					}
					break;
				case "select":
					List<String> selectedOptions = getSelectOptionsSelected();
					if(selectedOptions.isEmpty()) {
						value = null;
					}
					else if(selectedOptions.size() == 1) {
						value = selectedOptions.get(0);
					}
					else {
						try {
							value = new ObjectMapper().writeValueAsString(selectedOptions);
						}
						catch(IOException e) {
							throw new RuntimeException("Could not convert List " + selectedOptions + "to JSON!", e);
						}
					}
					break;
				default:
					while(true) {
						try {
							value = webElement.getText();
							break;
						}
						catch(StaleElementReferenceException e) {

							if(!reacquireWebElement()) {
								throw e;
							}
						}
					}
					break;
			}
		}

		LOGGER.debug("getValue() - {} - [END]", value == null ? "(NULL)" : Quotes.escape(value));

		return value;
	}

	/**
	 * Scrolls this {@link WebElement} into view and hovers the cursor over the middle of it.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see Actions#moveToElement(WebElement)
	 */
	public WebElementWrapper hoverOver() {

		LOGGER.info("hoverOver() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions;
		WebElementWrapper webElementToScrollTo = this;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			actions = new Actions(WEB_DRIVER_WRAPPER.DRIVER);

			// isDisplayed() will return false if the Element is in the Overflow of another Element.
			// Try scrolling to it.
			if(!isDisplayed()) {
				javascriptScrollIntoView();
			}

			// Handle hidden Web Elements, now that WebDriverHelper allows grabbing them.
			while(!webElementToScrollTo.isDisplayed()) {

				try {
					// It may be an Invisible overlay Element, so try to scroll near to it.
					webElementToScrollTo = webElementToScrollTo.getDescendant(By.xpath(".."));
				}
				catch(NoSuchElementException e) {
					// Not accessible.
					LOGGER.debug("hoverOver() [END]");
					return this;
				}

				if(webElementToScrollTo.getTagName().equals("body") || webElementToScrollTo.getTagName().equals("html")) {
					// Not accessible.
					LOGGER.debug("hoverOver() [END]");
					return this;
				}
			}

			if(!isFullyInViewport()) {
				scrollToMiddle();
			}

			actions.moveToElement(webElementToScrollTo.webElement);

			performAction(actions);
		}

		LOGGER.debug("hoverOver() [END]");

		return this;
	}

	/**
	 * Calls {@link WebElement#isDisplayed()}.
	 *
	 * @return {@code true} if this {@link WebElement} is visible in the web page (It may not be scrolled to.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebElement#isDisplayed()
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isDisplayed() {

		LOGGER.info("isDisplayed() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Boolean isVisible = null;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			do {
				try {
					isVisible = webElement.isDisplayed();
				}
				catch(StaleElementReferenceException e) {

					if(!reacquireWebElement()) {
						isVisible = false; // Treat removed as hidden.
					}
				}
			} while(isVisible == null);
		}

		LOGGER.debug("isDisplayed() - {} - [END]", isVisible);

		return isVisible;
	}

	/**
	 * If the {@link WebElement} is enabled and does not have an "disabled" attribute.
	 *
	 * @return {@code true} if the {@link WebElement} is enabled, {@code false} otherwise.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebElement#isEnabled()
	 */
	public boolean isEnabled() {

		LOGGER.info("isEnabled() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isEnabled;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {
			isEnabled = webElement.isEnabled() && webElement.getAttribute("disabled") == null;
		}

		LOGGER.debug("isEnabled() - {} - [END]", isEnabled);

		return isEnabled;
	}

	/**
	 * Will return {@code true} if this {@link WebElement} is fully inside the current, visible Viewport.
	 * (i.e. The part of the page that is currently being displayed by the browser.)
	 *
	 * @return {@code true}, if the {@link WebElement} is fully in view.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public boolean isFullyInViewport() {

		LOGGER.info("isFullyInViewport() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isInViewport = false;
		int endElementX, endElementY, startElementX, startElementY;
		long endViewportX, endViewportY;
		double startViewportX, startViewportY;
		JavascriptExecutor javascriptExecutor;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			javascriptExecutor = (JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER;

			try {
				startElementX = webElement.getLocation().getX();
				startElementY = webElement.getLocation().getY();
				endElementX = startElementX + webElement.getSize().getWidth();
				endElementY = startElementY + webElement.getSize().getHeight();
			}
			catch(StaleElementReferenceException e) {

				if(reacquireWebElement()) {
					return isFullyInViewport();
				}
				else {
					throw e;
				}
			}

			Object xNumber = javascriptExecutor.executeScript("return window.scrollX;"); // IE returns null for 0.
			//noinspection Duplicates
			if(xNumber == null) { // IE returns null for 0.
				startViewportX = 0;
			}
			else if(xNumber instanceof Double) {
				startViewportX = (Double) xNumber;
			}
			else {
				startViewportX = (Long) xNumber;
			}

			Object yNumber = javascriptExecutor.executeScript("return window.scrollY;"); // IE returns null for 0.
			//noinspection Duplicates
			if(yNumber == null) { // IE returns null for 0.
				startViewportY = 0;
			}
			else if(yNumber instanceof Double) {
				startViewportY = (Double) yNumber;
			}
			else {
				startViewportY = (Long) yNumber;
			}

			endViewportX = (long) (startViewportX + (long) javascriptExecutor.executeScript("return window.innerWidth;"));
			endViewportY = (long) (startViewportY + (long) javascriptExecutor.executeScript("return window.innerHeight;"));

			if(startElementX >= startViewportX && startElementX < endViewportX && startElementY >= startViewportY && startElementY < endViewportY
					&& endElementX > startViewportX && endElementX <= endViewportX && endElementY > startViewportY && endElementY <= endViewportY) {
				isInViewport = true;
			}
		}

		LOGGER.debug("isFullyInViewport() - {} - [END]", isInViewport);

		return isInViewport;
	}

	/**
	 * Determines if this {@link WebElement} is the {@link WebElement} that the browser is focused on.
	 *
	 * @return {@code true}, if this {@link WebElement} is the one in focus.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isInFocus() {

		LOGGER.info("isInFocus() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		boolean isInFocus = webElement.equals(WEB_DRIVER_WRAPPER.DRIVER.switchTo().activeElement());

		LOGGER.debug("isInFocus() - {} - [END]", isInFocus);

		return isInFocus;
	}

	/**
	 * Will return {@code true} if any part of the {@link WebElement} is inside the currently Visible Viewport.
	 * (i.e. The part of the page that is currently being displayed by the browser.)
	 *
	 * @return {@code true}, if the {@link WebElement} is partially in view.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public boolean isPartiallyInViewport() {

		LOGGER.info("isPartiallyInViewport() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isInViewport = false;
		int endElementX, endElementY, startElementX, startElementY;
		long endViewportX, endViewportY, startViewportX, startViewportY;
		JavascriptExecutor javascriptExecutor;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			javascriptExecutor = (JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER;

			startViewportX = (long) javascriptExecutor.executeScript("return window.scrollX;");
			startViewportY = (long) javascriptExecutor.executeScript("return window.scrollY;");

			endViewportX = startViewportX + (long) javascriptExecutor.executeScript("return window.innerWidth;");
			endViewportY = startViewportY + (long) javascriptExecutor.executeScript("return window.innerHeight;");

			startElementX = webElement.getLocation().getX();
			startElementY = webElement.getLocation().getY();

			endElementX = startElementX + webElement.getSize().getWidth();
			endElementY = startElementY + webElement.getSize().getHeight();

			if(startElementX >= startViewportX && startElementX < endViewportX && startElementY >= startViewportY && startElementY < endViewportY) {
				isInViewport = true; // Element Starts inside the viewport.
			}
			else if(endElementX > startViewportX && endElementX <= endViewportX && endElementY > startViewportY && endElementY <= endViewportY) {
				isInViewport = true; // Element End inside the viewport.
			}
		}

		LOGGER.debug("isPartiallyInViewport() - {} - [END]", isInViewport);

		return isInViewport;
	}

	/**
	 * Calls {@link WebElement#isSelected()}.
	 *
	 * @return {@code true} if the {@link WebElement} is currently selected or checked, {@code false} otherwise.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebElement#isSelected()
	 */
	public boolean isSelected() {

		LOGGER.info("isSelected() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isSelected;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			while(true) {

				try {
					isSelected = webElement.isSelected();
					break;
				}
				catch(StaleElementReferenceException e) {

					if(reacquireWebElement()) {
						//noinspection UnnecessaryContinue
						continue;
					}
					else {
						throw e;
					}
				}
			}
		}

		LOGGER.debug("isSelected() - {} - [END]", isSelected);

		return isSelected;
	}

	/**
	 * Determines if this {@link WebElement} has gone stale.
	 *
	 * @return {@code true} if this {@link WebElement} is stale, otherwise returns {@code false}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public boolean isStale() {

		LOGGER.info("isStale() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isStale;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			try {
				webElement.isDisplayed();
				isStale = false;
			}
			catch(StaleElementReferenceException e) {
				isStale = true;
			}
		}

		LOGGER.debug("isStale() - {} - [END]", isStale);

		return isStale;
	}

	/**
	 * Hovers over this {@link WebElement} and clicks it using JavaScript.
	 * <p>
	 *     (This is helpful for {@link WebElement}s that do not work with Selenium's built-in {@link WebElement#click()} function.)
	 * </p>
	 *
	 * <p>
	 *     <b>Known {@link WebElement}s that need a JavaScript click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with a JavaScript click. For those Elements use the normal {@link #click()} function.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s:</b>
	 * </p>
	 * <p>- <a href="https://summernote.org/">Summernote</a></p>
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws WebDriverException
	 * 		If the {@link WebElement} is covered, and another {@link WebElement} would receive the click.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper javascriptClick() {
		return javascriptClick(false);
	}

	/**
	 * Hovers over this {@link WebElement} and clicks it using JavaScript.
	 * <p>
	 *     (This is helpful for {@link WebElement}s that do not work with Selenium's built-in {@link WebElement#click()} function.)
	 * </p>
	 *
	 * <p>
	 *     <b>Known {@link WebElement}s that need a JavaScript click:</b>
	 * </p>
	 * <p>- Bootstrap's Date-Time Picker Widget</p>
	 *
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the viewport, it will be scrolled to the middle of the viewport.
	 * </p>
	 * <p>
	 *     <b>Known Issues:</b> Some {@link WebElement}s do not work with a JavaScript click. For those Elements use the normal {@link #click()} function.
	 * </p>
	 * <p>
	 *     <b>Known {@link WebElement}s:</b>
	 * </p>
	 * <p>- <a href="https://summernote.org/">Summernote</a></p>
	 *
	 * @param _waitForRefresh
	 * 		If {@code true}, this method will wait for a page reload.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws TimeoutException
	 * 		If the page never unloads, or if the page does not finish loading in
	 * 		{@link WebDriverWrapper#maxPageLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_PAGE_LOAD_TIME_S} seconds).
	 * @throws WebDriverException
	 * 		If the {@link WebElement} is covered, and another {@link WebElement} would receive the click.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebDriverWrapper#waitForPageLoad()
	 */
	public WebElementWrapper javascriptClick(boolean _waitForRefresh) {
		return click(_waitForRefresh, true, true, null);
	}

	/**
	 * Will perform a right click on this element.
	 * <ul>
	 *     <li>To navigate through the Context Menu items, send {@link Keys#UP} and {@link Keys#DOWN} to this element.</li>
	 *     <li>To select/execute/perform a highlighted Context Menu item, send {@link Keys#ENTER} to this element.</li>
	 *     <li>And to close the Context Menu, send {@link Keys#ESCAPE} to this element.</li>
	 * </ul>
	 * <p>
	 *     <b>Note:</b> <b>Chrome</b> and <b>Firefox</b> can only work with custom Context Menus. (<b>Not</b> the browser's Context Menu.)
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void rightClick() {

		LOGGER.info("rightClick() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions;

		//------------------------ Code ----------------------------------------
		actions = new Actions(WEB_DRIVER_WRAPPER.DRIVER).contextClick(webElement);

		performAction(actions);

		LOGGER.debug("rightClick() [END]");
	}

	/**
	 * Will perform a right click on this element send {@link Keys#DOWN} a given number of times, and then send {@link Keys#ENTER}.
	 * <p>
	 *     <b>Note:</b> <b>Chrome</b> and <b>Firefox</b> can only work with custom Context Menus. (<b>Not</b> the browser's Context Menu.)
	 * </p>
	 *
	 * @param _downCount
	 *         The number of times to press {@link Keys#DOWN}.
	 *         (<i>Note:</i> The first send of {@link Keys#DOWN} will select the top level item in the Context Menu.
	 *
	 * @throws IllegalArgumentException
	 *         If the given Down Count is &lt; 1!
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void rightClickAndSelectContextMenuItem(int _downCount) {

		LOGGER.info("rightClickAndSelectContextMenuItem(_downCount: {}) [START]", _downCount);

		//------------------------ Pre-Checks ----------------------------------
		if(_downCount < 1) {
			throw new IllegalArgumentException("Given Down Count must be > 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions;

		//------------------------ Code ----------------------------------------
		actions = new Actions(WEB_DRIVER_WRAPPER.DRIVER);

		actions.contextClick(webElement);

		for(int i = 0; i < _downCount; i++) {
			actions.sendKeys(Keys.DOWN);
		}

		actions.sendKeys(Keys.ENTER);

		performAction(actions); // Synchronize done inside.

		LOGGER.debug("rightClickAndSelectContextMenuItem(_downCount: {}) [END]", _downCount);
	}

	/**
	 * Sets the value of this Select / Drop-Down {@link WebElement}, by clicking on the Drop-Down then clicking on the correct option and closing the Drop-Down.
	 * <p>
	 *     <b>Note:</b> If this is a Multi-Select, the Option will be added to those already selected.
	 * </p>
	 * <p>
	 *     <b>Note:</b> If the Drop-Down {@link WebElement} is not visible on the page, it is scrolled to.
	 * </p>
	 *
	 * @param _visibleText
	 * 		The Select / Drop-Down Option's visible text.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _visibleText} is {@code null}.
	 * @throws NoSuchElementException
	 * 		If the Option does not exists.
	 * @throws TooManyResultsException
	 * 		If more than 1 Option has the given text.
	 * @throws UnexpectedTagNameException
	 * 		If this {@link WebElement} is not a Select / Drop-Down.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper select(String _visibleText) {

		LOGGER.info("select( visibleText: {}) [START]", _visibleText);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_visibleText, "Visible Text");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean isOpenSelect = false; // Non-Dropdown.

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			Select select = new Select(webElement);

			//------------------------ Initialize ----------------------------------
			///// isOpenSelect /////
			if(getAttribute("multiple") != null) {
				isOpenSelect = true;
			}
			else {
				String sizeAttribute = getAttribute("size");

				if(sizeAttribute != null) {

					try {

						int size = Integer.parseInt(sizeAttribute);
						if(size >= 2) {
							isOpenSelect = true;
						}
					}
					catch(NumberFormatException e) {
						//noinspection ConstantConditions
						isOpenSelect = false;
					}
				}
			}

			////////// Simulate Selecting an Option on a Drop-Down //////////
			if(!isOpenSelect) {

				// Open Select Drop-Down.
				click();

				// Highlight Option.
				WebElementWrapper option = getDescendant(By.xpath(".//option[normalize-space(.) = " + Quotes.escape(_visibleText) + "]"));
				option.click(false, false, false, null); // Selenium 3 broke manually scrolling Select Options, but does it automatically on click.

				// Close Drop-Down.
				blur();
			}

			////////// Real Work //////////
			// For some reason a Drop-Down's value cannot be set when the Select is open.
			// This even cannot be accomplished with JavaScript.
			select.selectByVisibleText(_visibleText);
		}

		LOGGER.debug("select( visibleText: {}) [END]", _visibleText);

		return this;
	}

	/**
	 * Will scroll the web page so that this {@link WebElement}'s middle, is in the middle of the viewport (browser window).
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper scrollToMiddle() {

		LOGGER.info("scrollToMiddle() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		int elementHeight, elementWidth, startElementX, startElementY;
		long scrollToX, scrollToY, windowHeight, windowWidth;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			///// Scroll into view from overflow Element scroll bars /////
			javascriptScrollIntoView();

			///// Scroll to Middle of Window /////
			startElementX = webElement.getLocation().getX();
			startElementY = webElement.getLocation().getY();
			elementWidth = webElement.getSize().width;
			elementHeight = webElement.getSize().height;
			windowWidth = (long) ((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("return window.innerWidth;");
			windowHeight = (long) ((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("return window.innerHeight;");
			scrollToX = startElementX + (elementWidth / 2) - (windowWidth / 2);
			scrollToY = startElementY + (elementHeight / 2) - (windowHeight / 2);

			// If Element is in the Top and/or Left half of the screen, after scrolling to [0, 0], just scroll as far as possible.
			scrollToX = scrollToX < 0 ? 0 : scrollToX;
			scrollToY = scrollToY < 0 ? 0 : scrollToY;

			((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("window.scrollTo(" + scrollToX + ", " + scrollToY + ")");
		}

		LOGGER.debug("scrollToMiddle() [END]");

		return this;
	}

	/**
	 * Will scroll the web page so that the {@link WebElement} starts at the top, right corner of the viewport.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper scrollToTopLeft() {

		LOGGER.info("scrollToTopLeft() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		int startElementX, startElementY;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			///// Scroll into view from overflow Element scroll bars /////
			javascriptScrollIntoView();

			///// Scroll to Middle of Window /////
			startElementX = webElement.getLocation().getX();
			startElementY = webElement.getLocation().getY();

			((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("window.scrollTo(" + startElementX + ", " + startElementY + ")");
		}

		LOGGER.debug("scrollToTopLeft() [END]");

		return this;
	}

	/**
	 * Sends a sequence of specialized keys to the {@link WebElement}. (Keys are sent one at a time.)
	 * <p>
	 *     <b>Note:</b> To send multiple keys at once, use {@link #sendKeys(String)} and send {@link Keys#chord(CharSequence...)}.
	 * </p>
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the viewport, it will be scrolled to the top, left of the viewport.
	 * 	</p>
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not selected, this will click on it first.
	 * </p>
	 *
	 * @param _keys
	 * 		What characters to send to the {@link WebElement}.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException If the given {@code _keys} is null.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@SuppressWarnings("ConstantConditions")
    public WebElementWrapper sendKeys(Keys... _keys) {

		LOGGER.info("sendKeys(_keys: ({})) [START]", _keys == null ? "NULL" : _keys.length);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_keys, "Keys");

		if( _keys.length < 1 ) {
			throw new RuntimeException( "Keys to send cannot be Empty!" );
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		StringBuilder keysToSend = new StringBuilder();

		//------------------------ Code ----------------------------------------
		for(Keys key : _keys) {
			keysToSend.append(key.toString());
		}

		sendKeys(keysToSend.toString());

		LOGGER.debug("sendKeys(_keys: ({})) [END]", _keys.length);

		return this;
	}

	/**
	 * Sends a sequence of keys to the {@link WebElement}. (Keys are sent one at a time.)
	 * <p>
	 *     <b>Note:</b> To send multiple keys at once, send {@link Keys#chord(CharSequence...)} for {@code _keys}.
	 * </p>
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not visible in the viewport, it will be scrolled to the top, left of the viewport.
	 * 	</p>
	 * <p>
	 *     <b>Note:</b> If the {@link WebElement} is not selected, this will click on it first.
	 * </p>
	 *
	 * @param _keys
	 * 		What characters to send to the {@link WebElement}.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @throws IllegalArgumentException If the given {@code _keys} is null.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper sendKeys(String _keys) {

		LOGGER.info("sendKeys(_keys: {}) [START]", _keys);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_keys, "Keys");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			if(!isInFocus()) {

				// A real user would have to select an input element before typing.
				// Not doing this may cause the first character to be missed.
				click();
			}
			else if(!isFullyInViewport()) { // Click performed above will scroll into view.
				scrollToMiddle();
			}

			if(WEB_DRIVER_WRAPPER.DRIVER_NAME.equals(WebDriverWrapper.IEBrowser.IE_WIN_64.toString())) { // IE 64 is already very slow at sending keys.
				webElement.sendKeys(_keys); // TODO: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116.
			}
			else {
				// By default Selenium puts all of the characters into the web element at once.
				// This can cause problems, especially of there is JavaScript logic around that input.
				for(char c : _keys.toCharArray()) {

					webElement.sendKeys(String.valueOf(c));
					// Not using Action, because in IE it will sometimes not trigger javascript listeners.
					//new Actions(WEB_DRIVER_WRAPPER.DRIVER).sendKeys(String.valueOf(c)).perform();

					// Adding delay to simulate typing.
					// (100ms [professional typing speed] - 300ms [average typing speed].)
					try {
						Thread.sleep(100);
					}
					catch(InterruptedException e) {/*Ignore*/}
				}
			}
		}

		LOGGER.debug("sendKeys(_keys: {}) [END]", _keys);

		return this;
	}

	/**
	 * Get's a String representation of this {@link WebElement}. (not formatted)
	 *
	 * @return This {@link WebElement}'s Outer HTML as a String.
	 *
	 * @throws JavascriptException If the Javascript command to get the "Outer HTML" fails.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Get's a String representation of this {@link WebElement}.
	 *
	 * @param _prettyPrint
	 * 		Whether or not to try and apply "pretty print" format to the returning String.
	 * 		If {@code false}, then the formatting will be an exact copy of the HTML page.
	 *
	 * @return This {@link WebElement}'s Outer HTML as a String.
	 *
	 * @throws JavascriptException If the Javascript command to get the "Outer HTML" fails.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String toString(boolean _prettyPrint) {

		LOGGER.info("toString(_prettyPrint: {}) [START]", _prettyPrint);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------
		final String JAVASCRIPT_COMMAND = "return arguments[0].outerHTML;";

		//------------------------ Variables -----------------------------------
		String toString;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			toString = (String) ((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript(JAVASCRIPT_COMMAND, webElement);

			if(toString.equals("Error executing JavaScript")) {
				if(isStale()) {
					if(reacquireWebElement()) {
						return toString(_prettyPrint);
					}
					else {
						throw new StaleElementReferenceException(webElement.toString());
					}
				}
				else {
					throw new JavascriptException(toString + "\n\t" + JAVASCRIPT_COMMAND);
				}
			}

			if(_prettyPrint) {

				try {
					toString = XmlDocumentHelper.prettyPrint(XmlDocumentHelper.getDocumentFrom(toString));
				}
				catch(XmlException e) { /*Will not "pretty print"*/ }
			}
		}

		LOGGER.debug("toString(_prettyPrint: {}) [END]", _prettyPrint);

		return toString;
	}

	/**
	 * Will convert this {@link WebElement} to an XML {@link Document}, with itself as the root.
	 *
	 * @return The XML representation of this {@link WebElement}.
	 *
	 * @throws XmlException If the HTML could not be converted to an XML.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public Document toXml() {

		LOGGER.info("toXml() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Document xmlDocument;
		String htmlString;

		//------------------------ Code ----------------------------------------
		while(true) {

			try {
				htmlString = toString();
				break;
			}
			catch(StaleElementReferenceException e) {

				if(!reacquireWebElement()) {
					throw e;
				}
			}
		}
		//////////////////// Manually Fix HTML for XML Parsing ////////////////////
		/* Parsers Tried:
		 * - Chilkat Java HTML Conversion Library (Requires License.)
		 * - HotSAX (Not Tried. Last update was 2012, and never got out of Alpha.)
		 * - HTML Cleaner (Removes elements that are in an invalid place for HTML. [i.e. <tr> tags that are not under a <table> tag.] )
		 * - HTML Unit (Has same issues as HTML Cleaner.)
		 * - HTML Parser (Not Tried. Last update was 2006.)
		 * - htmlparser [by doibuon] (Discontinued.)
		 * - Java Mozilla Html Parser (Not Tried. Last update was 2013.)
		 * - Jericho (Not Tried. Could not find HTML to XML functionality.)
		 * - Jsoup (Has same issues as HTML Cleaner.)
		 * - JTidy (Not Tried. Last update was 2012.)
		 * - Neko HTML (Not Tried. Required Xerces.)
		 * - Validator.nu HTML Parser (Not Tried. Last update was 2011.)
		 * - Tag Soup (Not Tried. Homepage was 404.)
		 */

		// Handle Void Elements (https://www.w3.org/TR/html-markup/syntax.html#syntax-elements).
		/* Test Cases:
		 * - Negative: <input id="repositoryId324999" name="repositoryIdAction" value="254134" type="hidden"/>
		 * - Normal: <input id="repositoryId324999" name="repositoryIdAction" value="254134" type="hidden"/>
		 * - Has "/" In Attribute: <input id="filename324999" name="filenameAction" value="British Columbia Investment Management Corporation / Public Sector Pension Investment Board (British Columbia) acquisition of TimberWest Forest Corp. (British Columbia)" type="hidden">
		 */
		htmlString = htmlString.replaceAll("<(area|base|br|col|command|embed|hr|img|input|keygen|link|meta|param|source|track|wbr)\\b([^>]*)(?<!/)>", "<input$2/>");

		xmlDocument = XmlDocumentHelper.getDocumentFrom(htmlString);

		LOGGER.debug("toXml() [END]");

		return xmlDocument;
	}

	/**
	 * Hovers over the {@code input} {@link WebElement} and uploads the given file, without using the Operating System's Dialog.
	 * <p>
	 *     <b>Note:</b> Selenium does not support Multiple File uploads at the same time,
	 *     <i>as of <a href='https://github.com/SeleniumHQ/selenium/blob/master/dotnet/CHANGELOG'>v2.53.0</a></i>.
	 *     You will have to loop over your list of files and upload them one at a time.
	 * </p>
	 * <ul>
	 *     <li>Also, uploading multiple files simultaneously (via multi-threading) can cause the Web Browser to run out of ports.
	 *     It is recommended to synchronize upload sections.</li>
	 * </ul>
	 *
	 * @param _file
	 * 		The {@link File} to upload.
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@code _file} is {@code null}, does not exist, or is a folder.
	 * @throws InvalidTypeException
	 * 		If this {@link WebElement} is not a File Input.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void uploadFile(File _file) {

		LOGGER.info("uploadFile(_file: {}) [START]", (_file == null ? "NULL" : _file.getAbsolutePath()));

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.fileExists(_file, null);

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean correctName, correctType;
		String typeAttribute;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			typeAttribute = webElement.getAttribute("type");

			////////// Pre-Checks //////////
			correctName = webElement.getTagName().equalsIgnoreCase("input");
			correctType = typeAttribute != null && typeAttribute.equalsIgnoreCase("file");

			if(!correctName || !correctType) {

				throw new InvalidTypeException("ERROR! Only input Elements of type \"file\" can be used to upload files! This Element "
						+ (!correctName ? "is a " + webElement.getTagName() + " Element." : "has a type of \""
						+ (typeAttribute == null ? "NULL" : typeAttribute) + "\"."));
			}

			////////// Execute //////////
			hoverOver();
			//noinspection ConstantConditions
			webElement.sendKeys(_file.getAbsolutePath());
		}

		LOGGER.debug("uploadFile(_file: {}) [END]", _file.getAbsolutePath());
	}

	/**
	 * Clicks on this {@link WebElement} to open the Upload Files dialog, goes to the folder, selects all files, and then closes the Upload dialog.
	 * <p>
	 *     <b>Note:</b> Focus must not be changed during this method.
	 *     (Because this uses the <i>java.awt.{@link Robot}</i> class.)
	 * </p>
	 *
	 * @param _folder
	 * 		The Folder to upload all of the files out of.
	 *
	 * @throws IllegalArgumentException
	 * 		If the {@code _folder} is {@code null}, does not exist, or is a file.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void uploadFolder(File _folder) {

		LOGGER.info("uploadFolder(_folder: {}) [START]", _folder == null ? "(NULL)" : _folder.getAbsolutePath());

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.folderExists(_folder, null);

		//------------------------ CONSTANTS -----------------------------------
		int KEY_STROKES_WAIT_TIME_MS = 250; // Minimum Observed.
		int LOADING_WAIT_TIME_MS = 400; // Minimum Observed.

		//------------------------ Variables -----------------------------------
		Robot robot;

		//------------------------ Code ----------------------------------------
		try {
			robot = new Robot();
		}
		catch(AWTException e) {
			throw new RuntimeException("Could not start Java AWT Robot!", e);
		}

		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			// Add folder path to clip-board.
			//noinspection ConstantConditions
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(_folder.getAbsolutePath()), null);

			// Open Dialog.
			click();

			try {
				Thread.sleep(LOADING_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			// Go to folder.
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			try {
				Thread.sleep(KEY_STROKES_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			try {
				Thread.sleep(LOADING_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			// Select all files.
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			try {
				Thread.sleep(KEY_STROKES_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			try {
				Thread.sleep(KEY_STROKES_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			// Open files (closing upload dialog).
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			try {
				Thread.sleep(KEY_STROKES_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			try {
				Thread.sleep(LOADING_WAIT_TIME_MS);
			}
			catch(InterruptedException e) { /*Do Nothing*/ }
		}

		LOGGER.info("uploadFolder(_folder: {}) [START]", _folder.getAbsolutePath());
	}

	/**
	 * Waits, up to the given length of time, for this {@link WebElement} to be removed from the DOM.
	 *
	 * @param _waitTime
	 * 		How long (in seconds) to wait.
	 * 		<p><i>Note:</i> Unit is truncated to milliseconds (3 decimal places).</p>
	 *
	 * @throws TimeoutException
	 * 		If this {@link WebElement} is not removed from the DOM in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForUnload(double _waitTime) {

		LOGGER.info("waitForUnload(_waitTime: {}) [START]", _waitTime);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = initializeFluentWait(_waitTime);
			fluentWait.ignoring(NoSuchElementException.class, StaleElementReferenceException.class).until(ExpectedConditions.stalenessOf(webElement));
		}

		LOGGER.debug("waitForUnload(_waitTime: {}) [END]", _waitTime);
	}

	//////////////////// Wait for Attribute Functions [START] ////////////////////
	/**
	 * Waits for a given Attribute to exist.
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>Or the given _name is an Empty String or just Whitespace.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name) {
		waitForAttribute(_name, null, WebDriverWrapper.maxElementLoadTimeInSeconds, true, true);
	}

	/**
	 * Waits for a given Attribute to exist.
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>The given _name is an Empty String or just Whitespace.</p>
	 * 		<p>Or the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, double _waitTime) {
		waitForAttribute(_name, null, _waitTime, true, true);
	}

	/**
	 * Waits for a given Attribute to exist and/or for the value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _value
	 * 		The value to compare against. If {@code null} this method will just look for the existence/non-existence of the Attribute.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>Or the given _name is an Empty String or just Whitespace.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, String _value) {
		waitForAttribute(_name, _value, WebDriverWrapper.maxElementLoadTimeInSeconds, true, true);
	}

	/**
	 * Waits for a given Attribute to exist and/or for the value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _value
	 * 		The value to compare against. If {@code null} this method will just look for the existence/non-existence of the Attribute.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)}</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>Or the given _name is an Empty String or just Whitespace.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, String _value, boolean _caseSensitive) {
		waitForAttribute(_name, _value, WebDriverWrapper.maxElementLoadTimeInSeconds, _caseSensitive, true);
	}

	/**
	 * Waits for a given Attribute to exist and/or for the value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _value
	 * 		The value to compare against. If {@code null} this method will just look for the existence/non-existence of the Attribute.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>The given _name is an Empty String or just Whitespace.</p>
	 * 		<p>Or the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, String _value, double _waitTime) {
		waitForAttribute(_name, _value, _waitTime, true, true);
	}

	/**
	 * Waits for a given Attribute to exist and/or for the value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _value
	 * 		The value to compare against. If {@code null} this method will just look for the existence/non-existence of the Attribute.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)}</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>The given _name is an Empty String or just Whitespace.</p>
	 * 		<p>Or the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, String _value, double _waitTime, boolean _caseSensitive) {
		waitForAttribute(_name, _value, _waitTime, _caseSensitive, true);
	}

	/**
	 * Waits for a given Attribute to exist / not exist and/or for the value to equal / not equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _name
	 * 		The name of the Attribute to look for.
	 * @param _value
	 * 		The value to compare against. If {@code null} this method will just look for the existence/non-existence of the Attribute.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)}</p>
	 * @param _isEqualTo
	 * 		If {@code true}, wait for the value to equal the given {@code _value} or just existence, if the given {@code _value} is {@code null}.
	 * 		<p>If {@code false}, wait for the value to NOT equal the given {@code _value} or just not existence, if the given {@code _value} is {@code null}.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _name is {@code null}.
	 * 		<p>The given _name is an Empty String or just Whitespace.</p>
	 * 		<p>Or the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s Attribute does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForAttribute(String _name, String _value, double _waitTime, boolean _caseSensitive, boolean _isEqualTo) {

		LOGGER.info("waitForAttribute(_name: {}, _value: {}, _waitTime: {}, _isEqualTo: {}) [START]", _name, _value, _waitTime, _isEqualTo);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_value, "Value");

		if(_waitTime < 0) {
			throw new IllegalArgumentException("Given Wait time must be >= 0!");
		}
		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = initializeFluentWait(_waitTime);
			fluentWait.until((Function<WebDriver, Boolean>) driver -> {

				String value = getAttribute(_name);

				///// NULL Compares /////
				if(_isEqualTo) { // We want them to be equal.

					if(Objects.equals(value, _value)) {
						return true;
					}
					else if(value == null || _value == null) {
						return false;
					}
					else { /*Neither of them are null, so do the Non-NULL Compares*/ }
				}
				else { // We don't want them to be equal.

					if(Objects.equals(value, _value)) {
						return false;
					}
					else if(value == null || _value == null) {
						return true;
					}
					else { /*Neither of them are null, so do the Non-NULL Compares*/ }
				}

				///// Non-NULL Compares /////
				boolean valuesAreEqual;

				if(_caseSensitive) {
					valuesAreEqual = value.trim().equals(_value.trim());
				}
				else {
					valuesAreEqual = value.trim().equalsIgnoreCase(_value.trim());
				}

				return valuesAreEqual == _isEqualTo;
			});
		}

		LOGGER.debug("waitForAttribute(_name: {}, _value: {}, _waitTime: {}, _isEqualTo: {}) [END]", _name, _value, _waitTime, _isEqualTo);
	}
	//////////////////// Wait for Attribute Functions [END] ////////////////////

	//////////////////// Wait for Class Functions [START] ////////////////////
	/**
	 * Waits up to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * for the {@code @class} to contain the given value.
	 *
	 * @param _token
	 * 		The value to compare against.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _token} is {@code null}.
	 * 		<p>Or if the given {@code _token} is an Empty String or just Whitespace.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s {@code @class}v does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForClass(String _token) {
		waitForClass(_token, WebDriverWrapper.maxElementLoadTimeInSeconds, true);
	}

	/**
	 * Waits up to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * for the {@code @class} to contain / not contain the given value.
	 *
	 * @param _token
	 * 		The value to compare against.
	 * @param _isEqualTo
	 * 		If {@code true}, waits for the {@code @class} to contain the value.
	 * 		<p>If {@code false}, waits for the {@code @class} to NOT contain the value.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _token} is {@code null}.
	 * 		<p>Or if the given {@code _token} is an Empty String or just Whitespace.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s {@code @class}v does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForClass(String _token, boolean _isEqualTo) {
		waitForClass(_token, WebDriverWrapper.maxElementLoadTimeInSeconds, _isEqualTo);
	}

	/**
	 * Waits for the {@code @class} to contain the given value.
	 *
	 * @param _token
	 * 		The value to compare against.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _token} is {@code null}.
	 * 		<p>If the given {@code _token} is an Empty String or just Whitespace.</p>
	 * 		<p>Or if the given {@code _waitTime} is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s {@code @class} does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForClass(String _token, double _waitTime) {
		waitForClass(_token, _waitTime, true);
	}

	/**
	 * Waits for the {@code @class} to contain / not contain the given value.
	 *
	 * @param _token
	 * 		The value to compare against.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 * @param _isEqualTo
	 * 		If {@code true}, waits for the {@code @class} to contain the value.
	 * 		<p>If {@code false}, waits for the {@code @class} to NOT contain the value.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@code _token} is {@code null}.
	 * 		<p>If the given {@code _token} is an Empty String or just Whitespace.</p>
	 * 		<p>Or if the given {@code _waitTime} is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s {@code @class}v does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForClass(String _token, double _waitTime, boolean _isEqualTo) {

		LOGGER.info("waitForClass(_token: {}, _waitTime: {}, _isEqualTo: {}) [START]", _token, _waitTime, _isEqualTo);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_token, "Token");

		if(_waitTime < 0) {
			throw new IllegalArgumentException("Given Wait time must be >= 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = initializeFluentWait(_waitTime);
			fluentWait.until((Function<WebDriver, Boolean>) driver -> {

				boolean contains = classContains(_token);

				return contains == _isEqualTo;
			});
		}

		LOGGER.debug("waitForAttribute(_token: {}, _waitTime: {}, _isEqualTo: {}) [END]", _token, _waitTime, _isEqualTo);
	}
	//////////////////// Wait for Class Functions [END] ////////////////////

	/**
	 * Waits up to the given amount of time for this {@link WebElement} to be click-able (i.e. visible, enabled, and un-obscured).
	 *
	 * @param _waitTimeInSeconds
	 * 		The maximum amount of time to wait for this {@link WebElement} to be click-able (<i>truncated to a {@code long}</i>).
	 *
	 * @throws IllegalArgumentException
	 * 		If {@code _waitTimeInSeconds < 0}.
	 * @throws TimeoutException
	 * 		If this {@link WebElement} is not click-able after {@code _waitTimeInSeconds}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForClickability(double _waitTimeInSeconds) {

		LOGGER.info("waitForClickability(_waitTimeInSeconds: {}) [START]", _waitTimeInSeconds);

		//------------------------ Pre-Checks ----------------------------------
		if(_waitTimeInSeconds < 0) {
			throw new IllegalArgumentException("Given Wait time must be >= 0!");
		}
		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		WebDriverWait webDriverWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			webDriverWait = new WebDriverWait(WEB_DRIVER_WRAPPER.DRIVER, (long) _waitTimeInSeconds, WebDriverWrapper.POLLING_INTERVAL_MS);

			try {
				webDriverWait.until(ExpectedConditions.elementToBeClickable(webElement));
			}
			catch(StaleElementReferenceException e) {

				if(reacquireWebElement()) {
					waitForClickability(_waitTimeInSeconds);
				}
				else {
					throw e;
				}
			}
		}

		LOGGER.debug("waitForClickability(_waitTimeInSeconds: {}) [END]", _waitTimeInSeconds);
	}

	//////////////////// Wait for Value Functions [START] ////////////////////
	/**
	 * Waits {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds)
	 * for this {@link WebElement}'s value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _value
	 * 		The value to compare against.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)} is used.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _value is {@code null}.
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s value does not change in
	 * 		{@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds).
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForValue(String _value, boolean _caseSensitive) {
		waitForValue(_value, WebDriverWrapper.maxElementLoadTimeInSeconds, _caseSensitive, true);
	}

	/**
	 * Waits for this {@link WebElement}'s value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _value
	 * 		The value to compare against.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _value is {@code null}.
	 * 		<p>Or if the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s value does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForValue(String _value, double _waitTime) {
		waitForValue(_value, _waitTime, true, true);
	}

	/**
	 * Waits for this {@link WebElement}'s value to equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _value
	 * 		The value to compare against.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)} is used.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _value is {@code null}.
	 * 		<p>Or if the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s value does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForValue(String _value, double _waitTime, boolean _caseSensitive) {
		waitForValue(_value, _waitTime, _caseSensitive, true);
	}

	/**
	 * Waits for this {@link WebElement}'s value to equal / not equal a given value.
	 * <p>
	 *     <b>Note:</b> Both the actual value and the given value are trimmed before comparing.
	 * </p>
	 *
	 * @param _value
	 * 		The value to compare against.
	 * @param _waitTime
	 * 		How long to wait for the change to occur.
	 * @param _caseSensitive
	 * 		If {@code true}, {@link String#equals(Object)} is used.
	 * 		<p>If {@code false}, {@link String#equalsIgnoreCase(String)} is used.</p>
	 * @param _isEqualTo
	 * 		If {@code true}, wait for the value to equal the given value.
	 * 		<p>If {@code false}, wait for the value to NOT equal the given value.</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given _value is {@code null}.
	 * 		<p>Or if the given _waitTime is {@code < 0}.</p>
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s value does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForValue(String _value, double _waitTime, boolean _caseSensitive, boolean _isEqualTo) {

		LOGGER.info("waitForValue(_value: {}, _waitTime: {}, _caseSensitive: {}, _isEqualTo: {}) [START]", _value, _waitTime, _caseSensitive, _isEqualTo);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_value, "Value");

		if(_waitTime < 0) {
			throw new IllegalArgumentException("Given Wait time must be >= 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = initializeFluentWait(_waitTime);
			fluentWait.until((Function<WebDriver, Boolean>) driver -> {

				String value = getValue().trim();

				boolean valuesAreEqual;
				if(_caseSensitive) {
					valuesAreEqual = value.equals(_value.trim());
				}
				else {
					valuesAreEqual = value.equalsIgnoreCase(_value.trim());
				}

				return valuesAreEqual == _isEqualTo;
			});
		}

		LOGGER.debug("waitForValue(_value: {}, _waitTime: {}, _caseSensitive: {}, _isEqualTo: {}) [END]", _value, _waitTime, _caseSensitive, _isEqualTo);
	}
	//////////////////// Wait for Value Functions [END] ////////////////////

	//////////////////// Wait for Visibility Functions [START] ////////////////////
	/**
	 * Waits up to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds),
	 * for this {@link WebElement} to become visible.
	 * <p>
	 *     <b>Note:</b> A Stale {@link WebElement} will be treated as invisible.
	 * </p>
	 *
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s visibility does not change in
	 * 		{@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds).
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForVisibility() {
		waitForVisibility(true, WebDriverWrapper.maxElementLoadTimeInSeconds);
	}

	/**
	 * Waits up to {@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds),
	 * for this {@link WebElement} to become visible/invisible.
	 * <p>
	 *     <b>Note:</b> If waiting for invisibility, a Stale {@link WebElement} will be treated as invisible.
	 * </p>
	 *
	 *
	 * @param _visible
	 * 		{@code true} if we are waiting for {@link WebElement} to be visible, or {@code false} if we are waiting for {@link WebElement} to be invisible.
	 *
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s visibility does not change in
	 * 		{@link WebDriverWrapper#maxElementLoadTimeInSeconds} ({@value WebDriverWrapper#DEFAULT_MAX_ELEMENT_LOAD_TIME_S} seconds).
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForVisibility(boolean _visible) {
		waitForVisibility(_visible, WebDriverWrapper.maxElementLoadTimeInSeconds);
	}

	/**
	 * Waits, up to the given length of time, for this {@link WebElement} to become visible.
	 * <p>
	 *     <b>Note:</b> A Stale {@link WebElement} will be treated as invisible.
	 * </p>
	 *
	 * @param _waitTime
	 * 		How long (in seconds) to wait.
	 * 		<p><i>Note:</i> Unit is truncated to milliseconds (3 decimal places).</p>
	 *
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s visibility does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForVisibility(double _waitTime) {
		waitForVisibility(true, _waitTime);
	}

	/**
	 * Waits, up to the given length of time, for this {@link WebElement} to become visible/invisible.
	 * <p>
	 *     <b>Note:</b> If waiting for invisibility, a Stale {@link WebElement} will be treated as invisible.
	 * </p>
	 *
	 * @param _visible
	 * 		{@code true} if we are waiting for {@link WebElement} to be visible, or {@code false} if we are waiting for {@link WebElement} to be invisible.
	 * @param _waitTime
	 * 		How long (in seconds) to wait.
	 * 		<p><i>Note:</i> Unit is truncated to milliseconds (3 decimal places).</p>
	 *
	 * @throws TimeoutException
	 * 		If this {@link WebElement}'s visibility does not change in the given amount of time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForVisibility(boolean _visible, double _waitTime) {

		LOGGER.info("waitForVisibility(_visible: {}, _waitTime: {}) [START]", _visible, _waitTime);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = initializeFluentWait(_waitTime);

			if(_visible) {

				fluentWait.ignoring( ElementNotVisibleException.class );

				long startTime = System.currentTimeMillis();
				long maxEndWaitTime = startTime + (((long) _waitTime) * 1000);

				while(true) { // START "Try again for Visible on StaleElementReferenceException" loop.
					try {
						fluentWait.until(ExpectedConditions.visibilityOf(webElement));
						break; // Element is visible.
					}
					catch(StaleElementReferenceException e) {

						while(true) { // START "Require if not past the wait time" loop.

							long currentTime = System.currentTimeMillis();
							if(currentTime >= maxEndWaitTime) {
								throw new TimeoutException("Web Element failed to appear after waiting " + _waitTime + " seconds!");
							}
							else if(reacquireWebElement()) {
								break; // Successfully re-acquired.
							}
							else {
								try { // Attempt to wait for element to appear.
									Thread.sleep(WebDriverWrapper.POLLING_INTERVAL_MS);
								}
								catch(InterruptedException e1) { /*Ignore*/ }

								//noinspection UnnecessaryContinue
								continue; // Try to re-acquire again, if not past the wait time.
							}
						} // END "Require if not past the wait time" loop.

						// Try to wait for visibility again, with time left.
						long newWaitTime = maxEndWaitTime - System.currentTimeMillis();
						fluentWait.withTimeout(Duration.ofMillis(newWaitTime));

						//noinspection UnnecessaryContinue
						continue;
					} // END Catch StaleElementReferenceException
				} // END "Try again for Visible on StaleElementReferenceException" loop.
			}
			else {
				fluentWait.until( (Function<WebDriver, Boolean>) driver -> !isDisplayed());
			}
		}

		LOGGER.debug("waitForVisibility(_visible: {}, _waitTime: {}) [END]", _visible, _waitTime);
	}
	//////////////////// Wait for Visibility Functions [END] ////////////////////

	//////////////////// WebElementWrapper & WebDriverWrapper Helper Methods ////////////////////
	/**
	 * @return The {@link WebElement} that this {@link WebElementWrapper} is currently pointing to. <i>(<b>Note:</b> This will change after a re-acquire.)</i>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	WebElement getWebElement() {
		return webElement;
	}

	/**
	 * @return {@code true}, if the {@link WebElement} was able to be re-acquired.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	boolean reacquireWebElement() {

		LOGGER.info("reacquireWebElement() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean success = false;
		List<WebElementWrapper> reacquiredWebElementWrappers = new ArrayList<>(0); // Will get overwritten.

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			try {

				if(originalBy != null) {
					reacquiredWebElementWrappers = WEB_DRIVER_WRAPPER.getWebElementWrappers(null, originalBy,
							WebDriverWrapper.maxElementLoadTimeInSeconds, 2, null);
				}

				if(reacquiredWebElementWrappers.size() != 1) {

					if(webElementToStringSelectorXpath == null) { // Calculate it.
						webElementToStringSelectorXpath = webElementToStringToXpath(webElement.toString());
					}
					if(!webElementToStringSelectorXpath.isEmpty()) { // We were able to figure something out.
						reacquiredWebElementWrappers = WEB_DRIVER_WRAPPER.getWebElementWrappers(null, By.xpath(webElementToStringSelectorXpath),
								WebDriverWrapper.maxElementLoadTimeInSeconds, 2, null);
					}
				}

				if(reacquiredWebElementWrappers.size() != 1 && XPATH_IDS_SELECTOR != null) {
					reacquiredWebElementWrappers = WEB_DRIVER_WRAPPER.getWebElementWrappers(null, By.xpath(XPATH_IDS_SELECTOR),
							WebDriverWrapper.maxElementLoadTimeInSeconds, 2, null);
				}

				if(reacquiredWebElementWrappers.size() == 1) {

					webElement = reacquiredWebElementWrappers.get(0).webElement;
					success = true;
				}
			}
			catch(Exception e) {

				// Could not re-acquire.
				LOGGER.warn(e);
				success = false;
			}
		}

		LOGGER.debug("reacquireWebElement() [END]");

		return success;
	}

	//////////////////// WebElementWrapper only Helper Methods ////////////////////
	/**
	 * @param _waitForRefresh Whether to wait for a page reload, after the click.
	 * @param _scrollIntoView If {@code true}, then the element will be scrolled into view first.
	 * @param _javascriptClick If {@code true}, then a javascript click will be performed.
	 * @param _keys If defined, the key(s) to hold down while doing the click.
	 *
	 * @return This {@link WebElementWrapper} for function call chain-ability.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private WebElementWrapper click( boolean _waitForRefresh, boolean _scrollIntoView, boolean _javascriptClick, CharSequence _keys ) {

		LOGGER.info( "click() [START]" );

		//------------------------ Pre-Checks ----------------------------------
		if(_javascriptClick && _keys != null) {
			throw new RuntimeException("Cannot hold keys with a Javascript Click!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions;

		//------------------------ Code ----------------------------------------
		synchronized( WEB_DRIVER_WRAPPER.LOCK ) {

			///// Scroll To and Hover Over /////
			if( _scrollIntoView || isFullyInViewport() ) {
				hoverOver();
			}

			///// Click /////
			if( _javascriptClick ) {
				( (JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER ).executeScript( "arguments[0].click();", webElement );
			}
			else {

				if(_keys != null) { // Don't need to catch StaleElementReferenceException, because action is being done at browser level.
					new Actions(WEB_DRIVER_WRAPPER.DRIVER).keyDown(_keys).perform();
				}

				while (true) {
					try {
						webElement.click();
						break;
					}
					catch (StaleElementReferenceException e) {
						if( !reacquireWebElement() ) {
							throw e;
						}
					}
				} // END "Catch StaleElementReferenceException" Loop.

				if(_keys != null) { // Don't need to catch StaleElementReferenceException, because action is being done at browser level.
					new Actions(WEB_DRIVER_WRAPPER.DRIVER).keyUp(_keys).perform();
				}
			} // END "Normal Click" Else.

			///// Wait for Refresh /////
			if( _waitForRefresh ) {

				// Page is unloaded.
				waitForUnload( WebDriverWrapper.maxPageLoadTimeInSeconds );

				// Page is loaded.
				WEB_DRIVER_WRAPPER.waitForPageLoad();
			}
		}

		LOGGER.debug( "click() [END]" );

		return this;
	}

	/**
	 * Some Web Pages do not honor the unique id attribute constraint.
	 * So this method tries to create an XPath Selector of {@code @id}s, for a better unique identifier.
	 * <p>
	 *     <b>Note:</b> The {@link #webElement} variable must be set before calling this method.
	 * </p>
	 * <p>
	 *     <b>Note:</b> XPath is used instead of CSS Selector, because XPaths can do more.
	 * </p>
	 *
	 * @return An XPath String that can be used to re-acquire this {@link WebElement}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private String getXpathIdsSelector() {

		LOGGER.info("getXpathIdsSelector() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String id;
		String name;StringBuilder xpath = null;
		WebElement ancestor;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			try {
				id = webElement.getAttribute("id");
				if(id == null || (id = id.trim()).isEmpty()) {
					LOGGER.debug("getXpathIdsSelector() [END]");
					return null;
				}

				xpath = new StringBuilder("/" + webElement.getTagName() + "[@id='" + id + "']");
				ancestor = webElement;

				while(true) {
					try {
						ancestor = ancestor.findElement(By.xpath(".."));
					}
					catch(NoSuchElementException e) {
						LOGGER.debug("getXpathIdsSelector() [END]");
						return xpath.toString();
					}

					name = ancestor.getTagName();
					id = ancestor.getAttribute("id");

					if(id != null && !(id = id.trim()).isEmpty()) {
						name += "[@id='" + id + "']";
					}

					xpath.insert(0, "/" + name);
				}
			}
			catch(StaleElementReferenceException e) {

				// Do not attempt to reacquire as that would cause extremely long loops. (Growth is exponential!.)
				if(xpath != null) {
					return "/" + xpath;
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	// FluentWait cannot be a Object level variable, because the ignore methods are cumulative.
	private FluentWait<WebDriver> initializeFluentWait(double _waitTime) {

		LOGGER.info("initializeFluentWait(_waitTime: {}) [START]", _waitTime);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			fluentWait = new FluentWait<>(WEB_DRIVER_WRAPPER.DRIVER)
					.pollingEvery(Duration.ofMillis(WebDriverWrapper.POLLING_INTERVAL_MS))
					.withTimeout(Duration.ofMillis((long) (_waitTime * 1000)));
		}

		LOGGER.debug("initializeFluentWait(_waitTime: {}) [END]", _waitTime);

		return fluentWait;
	}

	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private void javascriptScrollIntoView() {

		LOGGER.info("javascriptScrollIntoView() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		synchronized(WEB_DRIVER_WRAPPER.LOCK) {

			while(true) {

				try {
					((JavascriptExecutor) WEB_DRIVER_WRAPPER.DRIVER).executeScript("arguments[0].scrollIntoView();", webElement);
					break;
				}
				catch(StaleElementReferenceException e) {

					if(!reacquireWebElement()) {
						throw e;
					}
				}
			}
		}

		LOGGER.debug("javascriptScrollIntoView() [END]");
	}

	/**
	 * Will perform given {@link Actions}, and catch {@link StaleElementReferenceException}s.
	 *
	 * @param _actions
	 *         The {@link Actions} to perform.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private void performAction(Actions _actions) {

		LOGGER.debug("performAction(_actions: {}) [START]", _actions);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_actions, "Action");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		while(true) {
			try {
				_actions.perform();
				break;
			}
			catch(StaleElementReferenceException e) {
				if(!reacquireWebElement()) {
					throw e;
				}
			}
		}

		LOGGER.trace("performAction(_actions: {}) [END]", _actions);
	}

	//========================= Classes ========================================
}