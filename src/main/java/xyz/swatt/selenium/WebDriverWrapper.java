package xyz.swatt.selenium;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Quotes;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.exceptions.TooManyResultsException;
import xyz.swatt.exceptions.WebDriverWrapperException;
import xyz.swatt.exceptions.WebPageException;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * This class is a wrapper for the Selenium {@link WebDriver} class.
 * <p>
 *     It's purpose is to make common tasks easier and catch/fix common problems.
 * </p>
 * <p>
 *     All {@link WebDriver} interactions are {@code Synchronized} on the {@link #LOCK} object.
 *     (The {@link WebDriver} / Web Browser can only do one thing at a time, just like a real person.)
 *     This allows Multi-Threading to be used with Selenium.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebDriverWrapper implements Comparable {

	//========================= Static Interfaces ==============================
	/**
	 * A common Interface for the Browser Enums.
	 */
    private interface SpecificBrowser {}

	//========================= Static Enums ===================================
	/**
	 * The different Web Browsers supported by {@link WebDriverWrapper}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
    public enum BrowserType implements SpecificBrowser {

		CHROME("Chrome"),
		FIREFOX("Firefox"),
		IE("Internet Explorer"),
		;

		private final String VALUE;

        BrowserType(String _value) {
			VALUE = _value;
		}

		@Override
		public String toString() {
			return VALUE;
		}
	}

	/**
	 * A list of supported Google Chrome Web Browsers.
	 * <ul>
	 *     <li>{@link #CHROME}</li>
	 *     <li>{@link #CHROME_WIN_32}</li>
	 *     <li>{@link #CHROME_MAC_64}</li>
	 * </ul>
	 *
	 * @see <a href="https://sites.google.com/a/chromium.org/chromedriver/">https://sites.google.com/a/chromium.org/chromedriver</a>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
    public enum ChromeBrowser implements SpecificBrowser {
		
		/**
		 * Will attempt to pick Windows, Mac, or Linux; based on your Operating System.
		 */
		CHROME("chrome"),
		CHROME_LINUX_64("chrome-linux-64"),
		CHROME_MAC_64("chrome-mac-64"),
		CHROME_WIN_32("chrome-win-32.exe"),
		;
		
		private final String DRIVER_NAME;

		@SuppressWarnings("unused")
        ChromeBrowser(String _driverName) {
			DRIVER_NAME = _driverName;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		public String getDriverName() {
			return DRIVER_NAME;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		@Override
		public String toString() {
			return DRIVER_NAME;
		}
	}

	/**
	 * A list of supported Firefox Web Browsers.
	 * <ul>
	 *     <li>{@link #FIREFOX} - Will attempt to pick Windows or Mac, based on your computer. (Preferring 64 over 32 bit for windows.)
	 *     (Cannot be used with {@link #firefoxOverridePath}.)</li>
	 *	   <li>{@link #FIREFOX_MAC}</li>
	 *     <li>{@link #FIREFOX_WIN} - Will attempt to use 64 bit windows, and fall back on 32 bit windows. (Cannot be used with {@link #firefoxOverridePath}.)</li>
	 *     <li>{@link #FIREFOX_WIN_32}</li>
	 *     <li>{@link #FIREFOX_WIN_64}</li>
	 * </ul>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
    public enum FirefoxBrowser implements SpecificBrowser {
		
		/**
		 * Will attempt to pick Windows or Mac, based on your computer. (Preferring 64 over 32 bit for windows.)
		 * <p>
		 *     (Cannot be used with {@link #firefoxOverridePath}.)
		 * </p>
		 */
		FIREFOX("gecko", "gecko"),

		/**
		 * Have to specify the {@link #firefoxOverridePath}.
		 */
		FIREFOX_LINUX_32("gecko-linux-32", null),
		/**
		 * Have to specify the {@link #firefoxOverridePath}.
		 */
		FIREFOX_LINUX_64("gecko-linux-64", null),

		FIREFOX_MAC("gecko-mac", "/Applications/Firefox.app/Contents/MacOS/firefox-bin"),

		/**
		 * Will attempt to use 64 bit Windows, and fall back on 32 bit Windows.
		 * <p>
		 *     (Cannot be used with {@link #firefoxOverridePath}.)
		 * </p>
		 */
		FIREFOX_WIN("gecko-win", "gecko-win"),
		FIREFOX_WIN_32("gecko-win-32.exe", "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"),
		FIREFOX_WIN_64("gecko-win-64.exe", "C:\\Program Files\\Mozilla Firefox\\firefox.exe"),
		;
		
		private final String DRIVER_NAME, BROWSER_PATH;

		@SuppressWarnings("unused")
        FirefoxBrowser(String _driverName, String _browserPath) {
			DRIVER_NAME = _driverName;
			BROWSER_PATH = _browserPath;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		public String getDriverName() {
			return DRIVER_NAME;
		}

		/**
		 * @return {@link #BROWSER_PATH}.
		 */
		public String getBrowserPath() {
			return BROWSER_PATH;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		@Override
		public String toString() {
			return DRIVER_NAME;
		}
	}

	/**
	 * A list of supported Internet Explored (IE) Web Browsers.
	 * <p>
	 * <b>Note:</b> IE only supports Windows.
	 * </p>
	 * <ul>
	 *     <li>{@link #IE_WIN} - Will attempt to use 32 bit windows, and fall back on 64 bit windows. (64 bit is slow.)</li>
	 *     <li>{@link #IE_WIN_32}</li>
	 *     <li>{@link #IE_WIN_64}</li>
	 * </ul>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
    public enum IEBrowser implements SpecificBrowser {
		
		/**
		 * Will attempt to use 32 bit windows, and fall back on 64 bit windows.
		 * <p>
		 *     <b>Note: </b> IE 64 bit has slow typing, due to bug:
		 *     <a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116">Selenium Bug 5116</a>
		 * </p>
		 */
		// TODO: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116.
		IE_WIN("ie-win", ""),

		/**
		 * <p>
		 *     <b>Note: </b> IE 64 bit has slow typing,
		 *     due to bug: <a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116">Selenium Bug 5116</a>
		 * </p>
		 */
		// TODO: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116.
		IE_WIN_64("ie-win-64.exe", "C:\\Program Files\\Internet Explorer\\iexplore.exe"),

		IE_WIN_32("ie-win-32.exe", "C:\\Program Files (x86)\\Internet Explorer\\iexplore.exe"),
		;

		private final String DRIVER_NAME, BROWSER_PATH;

		@SuppressWarnings("unused")
        IEBrowser(String _driverName, String _browserPath) {
			DRIVER_NAME = _driverName;
			BROWSER_PATH = _browserPath;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		public String getDriverName() {
			return DRIVER_NAME;
		}

		/**
		 * @return {@link #BROWSER_PATH}.
		 */
		public String getBrowserPath() {
			return BROWSER_PATH;
		}

		/**
		 * @return {@link #DRIVER_NAME}.
		 */
		@Override
		public String toString() {
			return DRIVER_NAME;
		}
	}

	/**
	 * All known Firefox Extensions.
	 * <ul>
	 *     <li>{@link #FIRE_BUG}</li>
	 *     <li>{@link #FIRE_PATH}</li>
	 * </ul>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
    public enum FirefoxExtension {

		FIRE_BUG("firebug@software.joehewitt.com.xpi"),
		FIRE_PATH("FireXPath@pierre.tholence.com.xpi");

		private final String s;

		@SuppressWarnings("unused")
		FirefoxExtension(String _s) { s = _s; }

		@Override
		public String toString() {
			return s;
		}
	}

	//========================= STATIC CONSTANTS ===============================
	private static final AtomicBoolean NEEDS_RETILING = new AtomicBoolean(false);

	/**
	 * This is the smallest amount of time it takes for a Web Browser to update the DOM.
	 * (Actually somewhere between 100-200ms, so I am picking the lower end for speed.)
	 */
	public static final Duration POLLING_INTERVAL = Duration.ofMillis(100);

	/**
	 * The smallest recommended time to wait for any {@link WebElement} to appear, disappear, or change.
	 * (Twice the {@link #POLLING_INTERVAL}. [This will cause the wait to check 2 times for the requirement.])
	 */
	public static final Duration RECOMMENDED_MIN_POLLING_TIME = POLLING_INTERVAL.multipliedBy(2);

	/**
	 * This is the recommended value for the {@link #userThinkingWaitTime}.
	 * <p>
	 * It was calculated from a sampling of people running this
	 * <a href='https://www.gskstemeducation.com/activities/reaction-test/index.html'>Reaction Test</a>.
	 * </p>
	 */
	public static final Duration RECOMMENDED_USER_THINKING_WAIT_TIME = Duration.ofMillis(800);
	/**
	 * This is the recommended <a href='https://en.wikipedia.org/wiki/Standard_deviation'>Standard Deviation</a> for the {@link
	 * #userThinkingWaitTimeDeviation}.
	 * <p>
	 * It was calculated from a sampling of people running this
	 * <a href='https://www.gskstemeducation.com/activities/reaction-test/index.html'>Reaction Test</a>.
	 * </p>
	 */
	public static final Duration RECOMMENDED_USER_THINKING_WAIT_TIME_DEVIATION = Duration.ofMillis(125);

	/**
	 * Control Key, if on Windows. Command Key, if on Mac.
	 */
	public static final Keys CTRL_CMD_KEY = SystemUtils.IS_OS_MAC ? Keys.COMMAND : Keys.CONTROL;

	private static final Logger LOGGER = LogManager.getLogger(WebDriverWrapper.class);

	/**
	 * Used to make sure that we only create/launch one browser at a time,
	 * so that System Property "webdriver.x.driver" does not conflict between 32 and 64 bit versions.
	 */
	public static final Object CHROME_LOCK = new Object(), FIREFOX_LOCK = new Object(), IE_LOCK = new Object();

	/**
	 * Downloads go here by default.
	 * <p>
	 * (Constant if for external users to use as path.)
	 * </p>
	 */
	public static final String DEFAULT_DOWNLOAD_PATH = System.getProperty("user.home") + "/Downloads/";

	/**
	 * Where screenshots are stored, relative to the Build Path.
	 */
	public static final String SCREENSHOT_LOCATION = "test-output/screenshots/";

	/**
	 * &lt; Driver File Name, Driver File in Temp Directory &gt;
	 */
	private static final ConcurrentHashMap<String, File> DRIVER_FILES = new ConcurrentHashMap<>();

	private static final ConcurrentSkipListSet<WebDriverWrapper> KNOWN_WEB_DRIVER_WRAPPERS = new ConcurrentSkipListSet();

	//========================= Static Variables ===============================
	/**
	 * If {@code true}, when an exception happens, a screenshot will automatically be taken and added to the Exception's Message.
	 * <p><i>(default: {@code true})</i></p>
	 */
	public static boolean autoTakeScreenshotsOnErrors = true;

	/**
	 * If set to {@code true}, the the Browser Driver's logs will be turned on.
	 * <p>
	 *     <i>Note:</i> Only working for the ChromeDriver, right now.
	 * </p>
	 * <p>
	 *     <i>Note:</i> Logs go to the Console and cannot be redirected, without redirecting all of Console OUT/ERROR.
	 * </p>
	 */
	public static boolean enableDriverLogs = false;

	/**
	 * The default maximum amount of time to wait for a {@link WebElement} to appear, disappear, or change. (Default: {@code 0.5} seconds.)
	 * <p>
	 *     (In general a user will expect an action result to happen "immediately".)
	 * </p>
	 */
	public static Duration maxElementLoadTime = Duration.ofMillis(500);

	/**
	 * The maximum amount of time to wait for a Web Page to load. (Default: {@code 3} seconds.)
	 * <p>
	 *     (According to Goggle's Web Master Trend Analyst, John Mu, web pages h load in
	 *     <a href="https://productforums.google.com/forum/#!topic/webmasters/x-tAmtvK9iA/discussion">2</a>-<a
	 *     href="https://productforums.google.com/forum/#!topic/webmasters/x-tAmtvK9iA/discussion">3</a> seconds.)
	 * </p>
	 */
	public static Duration maxPageLoadTime = Duration.ofSeconds(3);

	/**
	 * Set this to simulate the time it would take an actual person, between actions. Delays will happen after these actions: Page Load, Changing Inputs,
	 * Clicks, and Drags.
	 * <p>
	 *     <i>Default:</i> {@code 0 (null)}.
	 * </p>
	 * <p>
	 *     <i>Recommended:</i> {@link #RECOMMENDED_USER_THINKING_WAIT_TIME}.
	 * </p>
	 * <p>
	 *     See Also: {@link #userThinkingWaitTimeDeviation}.
	 * </p>
	 */
	public static Duration userThinkingWaitTime = null;
	/**
	 * Set this to add a <a href='https://en.wikipedia.org/wiki/Normal_distribution'>Normal Distribution</a> to the {@link #userThinkingWaitTime}. (<i>Note:</i>
	 * The distribution will be limited to {@code 2 * } {@link #userThinkingWaitTimeDeviation}, to prevent outliers.)
	 * <p>
	 * <i>Default:</i> {@code 0 (null)}.
	 * </p>
	 * <p>
	 * <i>Recommended:</i> {@link #RECOMMENDED_USER_THINKING_WAIT_TIME_DEVIATION}.
	 * </p>
	 */
	@SuppressWarnings("JavaDoc")
	public static Duration userThinkingWaitTimeDeviation = null;

	/**
	 * This is the default window size for the Chrome Headless Browser.
	 */
	public static Dimension chromeHeadlessDefaultWindowSize = new Dimension(1920, 1080);

	/**
	 * A way to override the absolute path to Chrome Browser executable.
	 */
	public static String chromeBinaryOverridePath;

	/**
	 * A way to override the absolute path to ChromeDriver file.
	 */
	public static String chromeDriverOverridePath;

	/**
	 * A way to override the absolute path to Firefox app. (Required for Linux.)
	 */
	public static String firefoxOverridePath;

	/**
	 * The Absolute Path where Screenshots are saved.
	 * <p>Default: {@link #SCREENSHOT_LOCATION}.</p>
	 */
	public static String screenshotPath = new File(SCREENSHOT_LOCATION).getAbsolutePath();

	private static long screenHeightAvailable = -1;
	private static long screenWidthAvailable = -1;
	private static List<WebDriverWrapper> previousTileWindows = new ArrayList<>(0);
	//////////////////////////////////

	//========================= Static Constructor =============================
	static { }

	//========================= Static Methods =================================
	/**
	 * Will create a Browser of the given Type, with defaults options, and return it.
	 *
	 * @param _browserType
	 *         The Type of browser to create.
	 *         (You can pass in any of the {@link WebDriverWrapper} Browser Enums.)
	 *
	 * @return The newly created {@link WebDriverWrapper}.
	 *
	 * @throws IllegalArgumentException
	 *         If the given Browser Type is {@code null}.
	 *         <p>Or if the given Browser Type is Unknown.</p>
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static WebDriverWrapper getWebDriverWrapper(SpecificBrowser _browserType) {

		LOGGER.info("getWebDriverWrapper(_browserType: {}) [START]", _browserType);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_browserType, "Browser Type");

		//-------------------------CONSTANTS------------------------------------

		//-------------------------Variables------------------------------------
		WebDriverWrapper wdw;

		//-------------------------Code-----------------------------------------
		if(_browserType instanceof ChromeBrowser) {
			wdw = new WebDriverWrapper((ChromeBrowser) _browserType);
		}
		else if(_browserType instanceof FirefoxBrowser) {
			wdw = new WebDriverWrapper((FirefoxBrowser) _browserType);
		}
		else if(_browserType instanceof IEBrowser) {
			wdw = new WebDriverWrapper((IEBrowser) _browserType);
		}
		else {
			throw new IllegalArgumentException("Unknown Browser Type: " + _browserType + "!");
		}

		LOGGER.debug("getWebDriverWrapper(_browserType: {}) - WebDriverWrapper - [END]", _browserType);

		return wdw;
	}

	/**
	 * This method will kill any Browser Driver processes that were started in previous runs.
	 * <p>
	 *     Should only be used at the beginning off execution (like in an {@code @BeforeSuite} method).
	 * </p>
	 * <p>
	 *     <b>Note:</b> Will only execute on windows machine.
	 * </p>
	 * <p>
	 *     <b>Warning:</b> This will kill any ECT-QED Tools' Selenium Drivers from other executions!
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void killPreviousBrowserDriverProcesses() {

		LOGGER.info("killPreviousBrowserDriverProcesses() [START]");

		//------------------------ Pre-Checks ----------------------------------
		if(SystemUtils.IS_OS_MAC) {
			LOGGER.debug("killPreviousBrowserDriverProcesses() - IS_MACK - [END]");
			return;
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<String> allBrowserNames = new LinkedList<>();

		//------------------------ Code ----------------------------------------
		allBrowserNames.add(ChromeBrowser.CHROME_WIN_32.DRIVER_NAME);
		allBrowserNames.add(FirefoxBrowser.FIREFOX_WIN_32.DRIVER_NAME);
		allBrowserNames.add(FirefoxBrowser.FIREFOX_WIN_64.DRIVER_NAME);
		allBrowserNames.add(IEBrowser.IE_WIN_32.DRIVER_NAME);
		allBrowserNames.add(IEBrowser.IE_WIN_64.DRIVER_NAME);

		for(String driverName :allBrowserNames) {

			String baseName = FilenameUtils.getBaseName(driverName);
			try {
				// * is for the random number that is added when copying to the temp directory.
				String command = "taskkill /T /F /IM " + baseName + "-*";
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				LOGGER.warn("Driver " + Quotes.escape(driverName) + " could not be killed!", e);
			}
		}

		DRIVER_FILES.clear();

		LOGGER.debug("killPreviousBrowserDriverProcesses() [END]");
	}

	/**
	 * This method will kill all of the Browser Driver processes that were created during this run.
	 * <p>
	 *     Should only be used at the end off all execution (like in an {@code @AfterSuite} method).
	 * </p>
	 * <p>
	 *     <b>Note:</b> Will only execute on windows machine.
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void killUsedBrowserDriverProcesses() {

		LOGGER.info("killUsedBrowserDriverProcesses() [START]");

		//------------------------ Pre-Checks ----------------------------------
		if(SystemUtils.IS_OS_MAC) {
			LOGGER.debug("killUsedBrowserDriverProcesses() - IS_MACK - [END]");
			return;
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		for(String driverFileName : DRIVER_FILES.keySet()) {

			String baseName = FilenameUtils.getBaseName(driverFileName);
			try {
				// * is for the random number that is added when copying to the temp directory.
				String command = "taskkill /T /F /IM " + baseName + "-*";
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				LOGGER.warn("Driver " + Quotes.escape(driverFileName) + " could not be killed!", e);
			}
		}

		DRIVER_FILES.clear();

		LOGGER.debug("killUsedBrowserDriverProcesses() [END]");
	}

	/**
	 * Will tile all of the windows of all of the {@link WebDriverWrapper}s that have been created and not quit. (If only 1 {@link WebDriverWrapper} is still
	 * active, then it will be maximized.)
	 * <p>
	 * <b>Note:</b> For Chrome on a Mac, "maximise" may not fill the entire screen.
	 * It just expand the window to as large as it needs to be, to not have scroll bars (up to the size of the available screen space).
	 * </p>
	 *
	 * @param _zoomOut
	 *         If true, the windows will get zoomed out to the percentage that they are resided.
	 *         <p>
	 *         <b>Note:</b> Firefox does not support the use of Control keys on the browser.
	 *         (see: <a href="https://github.com/mozilla/geckodriver/issues/786">https://github.com/mozilla/geckodriver/issues/786</a>.)
	 *         </p>
	 *         <p>
	 *         <b>Note:</b> Chrome does not support the use of Control keys on the browser.
	 *         (see: <a href="https://bugs.chromium.org/p/chromedriver/issues/detail?id=2265">https://bugs.chromium.org/p/chromedriver/issues/detail?id=2265</a>.)
	 *         </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void tileWindows(boolean _zoomOut) {
		synchronized(KNOWN_WEB_DRIVER_WRAPPERS) {
			if(NEEDS_RETILING.get()) {
				NEEDS_RETILING.set(false);
				if(!KNOWN_WEB_DRIVER_WRAPPERS.isEmpty()) {
					tileWindows(KNOWN_WEB_DRIVER_WRAPPERS, _zoomOut);
				}
			}
		}
	}

	/**
	 * Will tile all of the given {@link WebDriver}s' active windows.
	 * <p>
	 *     (If only 1 {@link WebDriver} is given, it will be maximized.)
	 * </p>
	 * <p>
	 *     <b>Note:</b> For Chrome on a Mac, "maximise" may not fill the entire screen.
	 *     It just expand the window to as large as it needs to be, to not have scroll bars
	 *     (up to the size of the available screen space).
	 * </p>
	 *
	 * @param _driverWrappers
	 * 		The windows to tile, in a thread safe, unique collection. (see: {@link ConcurrentSkipListSet})
	 * 		
	 * @param _zoomOut
	 * 		If true, the windows will get zoomed out to the percentage that they are resided.
	 * 		<p>
	 * 			<b>Note:</b> Firefox does not support the use of Control keys on the browser.
	 * 		    (see: <a href="https://github.com/mozilla/geckodriver/issues/786">https://github.com/mozilla/geckodriver/issues/786</a>.)
	 *	    </p>
	 * 		<p>
	 * 			<b>Note:</b> Chrome does not support the use of Control keys on the browser.
	 * 		    (see: <a href="https://bugs.chromium.org/p/chromedriver/issues/detail?id=2265">https://bugs.chromium.org/p/chromedriver/issues/detail?id=2265</a>.)
	 *	    </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static void tileWindows(ConcurrentSkipListSet<WebDriverWrapper> _driverWrappers, boolean _zoomOut) {

		LOGGER.info("tileWindows(_driverWrappers: ({}), _zoomOut: {}) [START]", (_driverWrappers == null ? "NULL" : _driverWrappers.size()), _zoomOut);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_driverWrappers, "WebDriver Collection");

		if(_driverWrappers.isEmpty()) {

			//throw new IllegalArgumentException("Given WebDrivers cannot be Empty!");

			// Collection might have changes since call;

			return;
		}
		
		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		//noinspection UnusedAssignment
		int columns = 0, rows = 0, windowHeight = 0, windowWidth = 0;

		//------------------------ Code ----------------------------------------

		////////// Do Calculations on First WebDriverWrapper //////////
		boolean someStillExist = false;
		int windowCount = _driverWrappers.size();
		for(WebDriverWrapper driverWrapper : _driverWrappers) {

			boolean zoomOut = _zoomOut;
			if(!(driverWrapper.DRIVER instanceof InternetExplorerDriver)) {
				zoomOut = false;
			}

			synchronized(driverWrapper.LOCK) {

				if(driverWrapper.hasQuit()) { // WebDriverWrapper quit before lock was acquired.
					windowCount--;
					continue;
				}
				else {
					someStillExist = true;
				}

				// Check for easy out.
				if(windowCount == 1) {

					driverWrapper.maximize();

					if(zoomOut) {
						WebElement body = getBodyElement(driverWrapper);
						if(body != null) {
							body.sendKeys(Keys.chord(CTRL_CMD_KEY, "0")); // Un-zoom.
						}
					}

					LOGGER.debug("tileWindows(_driverWrappers: ({}), _zoomOut: {}) - All Quit - [END]", _driverWrappers.size(), _zoomOut);

					return;
				}

				if(screenWidthAvailable <= 0 || screenHeightAvailable <= 0) { // First Screen Calculation.

					// Have to reset zoom, so that the pixels given are not scaled.
					if(zoomOut) {
						WebElement body = getBodyElement(driverWrapper);
						if(body != null) { // Un-zoom.
							body.sendKeys(Keys.chord(CTRL_CMD_KEY, "0"));
						}
					}

					// Not using Toolkit, because we want to take the taskbar into account.
					screenWidthAvailable = (long) ((JavascriptExecutor) driverWrapper.DRIVER).executeScript("return screen.availWidth;");
					screenHeightAvailable = (long) ((JavascriptExecutor) driverWrapper.DRIVER).executeScript("return screen.availHeight;");
				}

				columns = (int) Math.ceil(Math.sqrt(windowCount));
				rows = (int) Math.ceil(windowCount / (columns * 1.0));
				windowWidth = (int) (screenWidthAvailable / columns);
				windowHeight = (int) (screenHeightAvailable / rows);

				// "Table" dimensions haven't changed, and no new windows were added.
				///// Tile Windows Variables /////
				int previousTileWindowsColumns = -1;
				int previousTileWindowsRows = -1;
				if(previousTileWindowsColumns == columns && previousTileWindowsRows == rows && previousTileWindows.containsAll(_driverWrappers)) {
					LOGGER.debug("tileWindows(_driverWrappers: ({}), _zoomOut: {}) - Subset - [END]", _driverWrappers.size(), _zoomOut);
					return;
				}
			} // END Synchronize on First non-quit Wrapper.

			break;
			
		} // END Loop Through Wrappers to get first non-quit one.

		// Check for easy out.
		if(!someStillExist) {
			LOGGER.debug("tileWindows(_driverWrappers: ({}), _zoomOut: {}) - All Quit - [END]", _driverWrappers.size(), _zoomOut);
			return; // All WebDriverWrappers had quit.
		}

		////////// Loop Through All WebDriverWrappers //////////
		//noinspection UnusedAssignment
		int i = 0, x = 0, y = 0;
		for(WebDriverWrapper driverWrapper : _driverWrappers) {

			boolean zoomOut = _zoomOut;
			if(!(driverWrapper.DRIVER instanceof InternetExplorerDriver)) {
				zoomOut = false;
			}

			synchronized(driverWrapper.LOCK) {
				
				if(driverWrapper.hasQuit()) { // WebDriverWrapper quit before lock was acquired.
					continue;
				}
				
				WebDriver.Window window = driverWrapper.DRIVER.manage().window();
				WebElement body = null;
				if(zoomOut) {
					body = getBodyElement(driverWrapper);
				}
				
				///// Resize ///// (Zoom has to be reset first or the new dimensions will be effected by the percentage of the zoom.)
				if(zoomOut && body != null) { // Un-zoom.
					body.sendKeys(Keys.chord(CTRL_CMD_KEY, "0"));
				}
				Dimension windowSize = window.getSize();
				if(windowSize.width != windowWidth || windowSize.height != windowHeight) {
                    window.setSize(new Dimension(windowWidth, windowHeight));
                    windowSize = window.getSize(); // Validate.
                }
				if(windowSize.width != windowWidth || windowSize.height != windowHeight) {
					LOGGER.warn("Window Resize Failed!\n\tExpected: " + windowWidth + ", " + windowHeight + "\n\tFound: " + windowSize);
				}
				
				///// Re-Position /////
				Point windowPosition = window.getPosition();
                if(windowPosition.x != x || windowPosition.y != y) {
                    window.setPosition(new Point(x, y));
                    windowPosition = window.getPosition(); // Validate.
                }
				if(windowPosition.x != x || windowPosition.y != y) {
					LOGGER.warn("Window Re-Position Failed!\n\tExpected: " + x + ", " + y + "\n\tFound: " + windowPosition);
				}
				
				///// Increment Position /////
				x += windowWidth;
				if(x >= screenWidthAvailable) { // Wrap around.
					x = 0;
					y += windowHeight;
				}
				
				if(zoomOut && body != null) {
					
					// Start at 100%.
					body.sendKeys(Keys.chord(CTRL_CMD_KEY, "0"));  // Does not work for IE.
					
					// Zoom Out.
					if(columns >= 2) {
						
						// Zoom out to 50% to start. (Appropriate for 2 columns.)
						body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 90% (Firefox); 90% (Chrome); 75% IE.
						body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 80% (Firefox); 80% (Chrome); 50% IE.

                        if(driverWrapper.DRIVER instanceof FirefoxDriver || driverWrapper.DRIVER instanceof ChromeDriver) {
							body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 67% (Firefox); 75% (Chrome).
							body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 50% (Firefox); 67% (Chrome).

							if(driverWrapper.DRIVER instanceof ChromeDriver) {
								body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 50% (Chrome).
							}
						}
						
						for(i = 2; i < columns; i++) { // columns >= rows
							body.sendKeys(Keys.chord(CTRL_CMD_KEY, "-")); // 30% (Firefox); 33%, 25% (Chrome); 25%, 10% IE.
						}
					}
				}
				
				// TODO: If window size would be too small.
				/* Tile so that all of the Title bars are visible. {
				driverWrapper.DRIVER.manage().window().setPosition( new Point( x, y ) );
				x += 100;
				y += 50;
				if( x >= screenWidthAvailable || y >= screenHeightAvailable ) { // Wrap around.
				x = 0;
				y = 0;
				}
				}*/
			}
		} // END-Loop Through Web Drivers.
		
		previousTileWindows = new ArrayList<>(_driverWrappers);

		LOGGER.debug("tileWindows(_driverWrappers: ({}), _zoomOut: {}) [END]", _driverWrappers.size(), _zoomOut);
	}

	/**
	 * Will wait for the User "Think" Time, if {@link #userThinkingWaitTime} is set and will add a Normal Deviation if {@link #userThinkingWaitTimeDeviation} is
	 * set.
	 *
	 * @return How long it waited.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	public static Duration waitForUserThinkTime() {

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		long waitTimeMs = 0;

		//------------------------ Code ----------------------------------------
		if(userThinkingWaitTime != null && userThinkingWaitTime.toMillis() > 0) {
			try {
				if(userThinkingWaitTimeDeviation != null && !userThinkingWaitTime.equals(Duration.ZERO)) {

					NormalDistribution normalDistributionGenerator = new NormalDistribution(userThinkingWaitTime.toMillis(),
							userThinkingWaitTimeDeviation.toMillis());

					do {
						waitTimeMs = (long) normalDistributionGenerator.sample();
					} while(waitTimeMs < (userThinkingWaitTime.toMillis() - userThinkingWaitTimeDeviation.multipliedBy(2).toMillis()) ||
							waitTimeMs > (userThinkingWaitTime.toMillis() + userThinkingWaitTimeDeviation.multipliedBy(2).toMillis()));
				}
				else {
					waitTimeMs = userThinkingWaitTime.toMillis();
				}

				Thread.sleep(waitTimeMs);
			}
			catch(InterruptedException e) {
				LOGGER.warn("Failed to Wait for User \"Think\" time.", e);
			}
		}

		return Duration.ofMillis(waitTimeMs);
	}

	//-------------------- Helper Methods --------------------

	/**
	 * Will create an empty Driver File in the User's Temporary Directory.
	 *
	 * @param _prefix
	 *         File Name Prefix.
	 * @param _extension
	 *         File Name Extension, <b>without</b> period.
	 *
	 * @throws WebDriverWrapperException
	 *         If the Temp File cannot be created or set as an executable.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static File createDriverFile(String _prefix, String _extension) {

		LOGGER.debug("createDriverFile(_prefix: {}, _extension: {}) [START]", _prefix, _extension);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		File toRet;

		//------------------------ Code ----------------------------------------
		try {
			toRet = File.createTempFile(_prefix + "-", "." + _extension);
		}
		catch(IOException e) {
			throw new WebDriverWrapperException("Unable to create Temporary Driver File!", e);
		}

		toRet.deleteOnExit();
		// deleteOnExit() WILL delete if:
		// - The program ends on its own.
		// deleteOnExit() will NOT delete if:
		// - The program is terminated externally.
		// -- TODO: Find a way to solve this.
		// --- Possibly by using the same file name and path each time, and just overwriting.

		if(!toRet.setExecutable(true, false)) { // Needed for Mac and Linux.
			throw new WebDriverWrapperException("Could not set the driver file to executable!");
		}

		LOGGER.trace("createDriverFile(_prefix: {}, _extension: {}) - {} - [END]", _prefix, _extension, toRet.getAbsolutePath());

		return toRet;
	}

	/**
	 * @param _driver
	 *         The Browser to look in.
	 *
	 * @return The Body Element, or Null, if a "real" HTML Page is NOT loaded.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static WebElement getBodyElement(WebDriverWrapper _driver) {

		LOGGER.debug("getBodyElement(WebDriverWrapper _driver) [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String url = _driver.getCurrentUrl();

		//noinspection UnusedAssignment
		WebElement body = null;

		//------------------------ Code ----------------------------------------
		if(url == null || url.trim().isEmpty() || url.trim().equalsIgnoreCase("about:blank")) { // No page loaded.
			return null;
		}
		else {
			body = _driver.DRIVER.findElement(By.xpath("/html/body"));
		}

		LOGGER.debug("getBodyElement(WebDriverWrapper _driver) [END]");

		return body;
	}

	//========================= CONSTANTS ======================================
	/**
	 * Used as the lock to all {@link WebDriver} interactions.
	 */
	public final Object LOCK = new Object();

	public final BrowserType BROWSER_TYPE;
	final protected String DRIVER_NAME;
	final protected WebDriver DRIVER;

	//========================= Variables ======================================

	//========================= Constructors =================================== // TODO: Split WebDriverWrapper creation into it's own class.
	/**
	 * <p>
	 *     Instantiates this WebDriverWrapper to use the given {@link ChromeBrowser}.
	 * </p>
	 * <p>&nbsp;</p>
	 * <p>
	 *     <i>Notes:</i>
	 * </p>
	 * <ul>
	 *     <li>The Window will be maximized</li>
	 *     <li>You can use a custom Chrome install patrh by first setting {@link #chromeBinaryOverridePath}</li>
	 *	   <li>You can use a different ChromeDriver by first setting {@link #chromeDriverOverridePath}</li>
	 * </ul>
	 *
	 * @param _browser
	 * 		The Web {@link ChromeBrowser} to use.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link ChromeBrowser} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(ChromeBrowser _browser) {
		this(_browser, false, null);
	}

	/**
	 * <p>
	 *     Instantiates this WebDriverWrapper to use the given {@link ChromeBrowser}.
	 * </p>
	 * <p>&nbsp;</p>
	 * <p>
	 *     <i>Notes:</i>
	 * </p>
	 * <ul>
	 *     <li>The Window will be maximized (unless it is headless, then window size will be set to {@link #chromeHeadlessDefaultWindowSize})</li>
	 *     <li>You can use a custom Chrome install patrh by first setting {@link #chromeBinaryOverridePath}</li>
	 *	   <li>You can use a different ChromeDriver by first setting {@link #chromeDriverOverridePath}</li>
	 * </ul>
	 *
	 * @param _browser
	 * 		The Web {@link ChromeBrowser} to use.
	 * @param _headless
	 *         If {@code true} than a headless (GUI-less) version of Chrome will be used; otherwise the normal Chrome GUI will be used. (Needs Chrome version 59
	 *         or higher.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link ChromeBrowser} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(ChromeBrowser _browser, boolean _headless) {
		this(_browser, _headless, null);
	}

	/**
	 * <p>
	 * Instantiates this WebDriverWrapper to use the given {@link ChromeBrowser}.
	 * </p>
	 * <p>&nbsp;</p>
	 * <p>
	 * <i>Notes:</i>
	 * </p>
	 * <ul>
	 *     <li>The Window will be maximized</li>
	 *     <li>You can use a custom Chrome install patrh by first setting {@link #chromeBinaryOverridePath}</li>
	 *	   <li>You can use a different ChromeDriver by first setting {@link #chromeDriverOverridePath}</li>
	 * </ul>
	 *
	 * @param _browser
	 *         The Web {@link ChromeBrowser} to use.
	 * @param _capabilities
	 *         Allows you to send in custom Capabilities to the created {@link WebDriver}. (<b>Warning:</b> This will overwrite any default {@link
	 *         WebDriverWrapper} settings.)
	 *
	 * @throws IllegalArgumentException
	 *         If the given {@link ChromeBrowser} is {@code null}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(ChromeBrowser _browser, Capabilities _capabilities) {
		this(_browser, false, _capabilities);
	}

	/**
	 * Instantiates this WebDriverWrapper to use the given {@link ChromeBrowser}.
	 * <p>&nbsp;</p>
	 * <p>
	 * <i>Notes:</i>
	 * </p>
	 * <ul>
	 *     <li>The Window will be maximized (unless it is headless, then window size will be set to {@link #chromeHeadlessDefaultWindowSize})</li>
	 *     <li>You can use a custom Chrome install patrh by first setting {@link #chromeBinaryOverridePath}</li>
	 *	   <li>You can use a different ChromeDriver by first setting {@link #chromeDriverOverridePath}</li>
	 * </ul>
	 *
	 * @param _browser
	 *         The Web {@link ChromeBrowser} to use.
	 * @param _headless
	 *         If {@code true} than a headless (GUI-less) version of Chrome will be used; otherwise the normal Chrome GUI will be used.
	 *         (Needs Chrome version 59 or higher.)
	 * @param _capabilities
	 *         Allows you to send in custom Capabilities to the created {@link WebDriver}. (<b>Warning:</b> This will overwrite any default {@link
	 *         WebDriverWrapper} settings.)
	 *
	 * @throws IllegalArgumentException
	 *         If the given {@link ChromeBrowser} is {@code null}.
	 * @throws WebDriverException
	 *         If the current Operating System is unknown or unsupported.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(ChromeBrowser _browser, boolean _headless, Capabilities _capabilities) {

		LOGGER.info("WebDriverWrapper(_browser: {}, _headless: {}, _capabilities: {}) [START]", _browser, _headless,
				(_capabilities == null ? "(NULL)" : _capabilities));

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_browser, "Browser");

		/* Not Needed, because we are doing a a FileExists test below.
		if(chromeDriverOverridePath != null) {
			ArgumentChecks.pathIsAbsoluteFile(chromeDriverOverridePath, "ChromeDriver");
		}*/

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		File driverFile;

		//------------------------ Code ----------------------------------------
		if(chromeDriverOverridePath == null) {
			if(_browser == ChromeBrowser.CHROME) {
				if(SystemUtils.IS_OS_WINDOWS) {
					_browser = ChromeBrowser.CHROME_WIN_32;
				}
				else if(SystemUtils.IS_OS_MAC) {
					_browser = ChromeBrowser.CHROME_MAC_64;
				}
				else if(SystemUtils.IS_OS_LINUX) {
					_browser = ChromeBrowser.CHROME_LINUX_64;
				}
				else {
					throw new WebDriverWrapperException("Unknown or Unsupported Operating System: " + System.getProperty("os.name") + "!");
				}
			}

			////////// Setup ChromeDriver ////////// Chrome must use a different Driver each time, to support multi-threading.
			String[] driverNameParts = _browser.DRIVER_NAME.split("\\.");
			driverFile = createDriverFile(driverNameParts[0], driverNameParts.length > 1 ? driverNameParts[1] : "");
			InputStream resourceInStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("chrome-drivers/" + _browser);
			try {
				FileUtils.copyToFile(resourceInStream, driverFile);
			}
			catch(IOException e) {
				throw new WebDriverWrapperException("Unable to copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!", e);
			}

			// Validate Copy.
			if(driverFile.length() <= 0) {
				throw new WebDriverWrapperException("Could not copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!");
			}
		}
		else {
			File givenDriverFile = new File(chromeDriverOverridePath);
			ArgumentChecks.fileExists(givenDriverFile, "ChromeDriver");

			////////// Setup ChromeDriver ////////// Chrome must use a different Driver each time, to support multi-threading.
			String[] driverNameParts = givenDriverFile.getName().split("\\.");
			driverFile = createDriverFile(driverNameParts[0], driverNameParts.length > 1 ? driverNameParts[1] : "");
			try {
				FileUtils.copyFile(givenDriverFile, driverFile);
			}
			catch(IOException e) {
				throw new WebDriverWrapperException("Unable to copy given Driver File [" + givenDriverFile.getAbsolutePath() + "] to: " +
						driverFile.getAbsolutePath() + "!", e);
			}
		}

		DRIVER_FILES.put(_browser.DRIVER_NAME + System.currentTimeMillis(), driverFile); // Chrome must use a different Driver each time, to support multi-threading.

		////////// Browser Options //////////
        ChromeOptions options;
		if(_capabilities instanceof ChromeOptions) { // ChromeOptions.merge(Capabilities), puts Arguments under Capabilities, and they are never read again.
            options = (ChromeOptions) _capabilities; // TODO: Report or Find ChromeDriver bug.
        }
        else {
            options = new ChromeOptions();
        }
		//options.addArguments("--start-maximized"); // Doesn't work with Mac.
		//noinspection SpellCheckingInspection
		options.addArguments("disable-infobars");
		options.setHeadless(_headless);
		if(chromeBinaryOverridePath != null) {
			// TODO: Check that chromeBinaryOverridePath is a valid path.
			options.setBinary(chromeBinaryOverridePath);
		}
        if(_capabilities != null && !(_capabilities instanceof ChromeOptions)) {
            options.merge(_capabilities); // TODO: Extend ChromeOptions and fix their .merge(Capabilities) method, to copy Arguments to the correct place.
		}

		////////// Launch Browser //////////
		// Using ChromeDriverService so we don't have to set the "webdriver.chrome.driver" System Property.
		// - So that we can create multiple drives in parallel.
		ChromeDriverService.Builder chromeDriverServiceBuilder = new ChromeDriverService.Builder().usingDriverExecutable(driverFile);
		if(enableDriverLogs) {
			chromeDriverServiceBuilder.withVerbose(true).withSilent(false);
			// TODO: Add Support for specifying Log File Location. (--log-path=chromedriver.log)
		}
        else {
			chromeDriverServiceBuilder.withVerbose(false).withSilent(true);
		}
		ChromeDriverService chromeDriverService = chromeDriverServiceBuilder.build();
		ChromeDriver chromeDriver = null;
		while(chromeDriver == null) {
			try {
				chromeDriver = new ChromeDriver(chromeDriverService, options);
			}
			catch(WebDriverException e) { // Can happen when heavily multi-threading.
				String errorMessage = e.getMessage().trim().toLowerCase();
				if(!errorMessage.startsWith("timed out waiting for driver server to start.") &&
						!errorMessage.startsWith("java.net.connectexception: failed to connect to localhost/0:0:0:0:0:0:0:1:")) {
					LOGGER.error(e);
					throw e;
				}
			}
		}
		DRIVER = chromeDriver;
		BROWSER_TYPE = BrowserType.CHROME;
		DRIVER_NAME = _browser.toString();

		// TODO: BUG: Makes all logs visible and gives them the level of what was passed in.
		//( (RemoteWebDriver) DRIVER ).setLogLevel( Level.OFF );

		if(_headless) {
			setWindowSize(chromeHeadlessDefaultWindowSize);
		}
		else {
			maximize();

			KNOWN_WEB_DRIVER_WRAPPERS.add(this);
			NEEDS_RETILING.set(true);
		}


		LOGGER.debug("WebDriverWrapper(_browser: {}, _headless: {}, _capabilities: {}) [END]", _browser, _headless,
				(_capabilities == null ? "(NULL)" : _capabilities));
	}

	/**
	 * Instantiates this WebDriverWrapper to use the given {@link FirefoxBrowser}.
	 * <p>
	 *     (Designed to work with <a href="https://ftp.mozilla.org/pub/firefox/releases/56.0.2/">Firefox version 56.0.2</a>.)
	 * </p>
	 * <p>
	 *     <b>Notes:</b>
	 * </p>
	 * <p>- Will look at the default install path for the Firefox app. (Can be overridden by setting {@link #firefoxOverridePath}.)</p>
	 * <p>- The Window will be maximized.</p>
	 * <p>- Downloads will automatically start and the files will be saved to FireFox's default download location.
	 * (Stored in {@link #DEFAULT_DOWNLOAD_PATH}.)</p>
	 *
	 * @param _browser
	 * 		The {@link FirefoxBrowser} to use.
	 * @param _firefoxExtensionNames
	 * 		A list of Firefox Add-On names. (Known names can be found in the {@link FirefoxExtension} enum.) [Only works on Windows and Mac.]
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link FirefoxBrowser} is {@code null}.
	 * 		<p>Or if {@link #firefoxOverridePath} is mismatched with given {@link FirefoxBrowser}.</p>
	 * 		<p>Or if Extensions are provided when the given {@link FirefoxBrowser} is Linux.</p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(FirefoxBrowser _browser, String... _firefoxExtensionNames) {
		this(_browser, null, _firefoxExtensionNames);
	}

	/**
	 * Instantiates this WebDriverWrapper to use the given {@link FirefoxBrowser}.
	 * <p>
	 * (Designed to work with <a href="https://ftp.mozilla.org/pub/firefox/releases/56.0.2/">Firefox version 56.0.2</a>.)
	 * </p>
	 * <p>
	 * <b>Notes:</b>
	 * </p>
	 * <p>- Will look at the default install path for the Firefox app. (Can be overridden by setting {@link #firefoxOverridePath}.)</p>
	 * <p>- The Window will be maximized.</p>
	 * <p>- Downloads will automatically start and the files will be saved to FireFox's default download location.
	 * (Stored in {@link #DEFAULT_DOWNLOAD_PATH}.)</p>
	 *
	 * @param _browser
	 *         The {@link FirefoxBrowser} to use.
	 * @param _capabilities
	 *         Allows you to send in custom Capabilities to the created {@link WebDriver}. (<b>Warning:</b> This will overwrite any default {@link
	 *         WebDriverWrapper} settings.)
	 * @param _firefoxExtensionNames
	 *         A list of Firefox Add-On names. (Known names can be found in the {@link FirefoxExtension} enum.) [Only works on Windows and Mac.]
	 *
	 * @throws IllegalArgumentException
	 *         If the given {@link FirefoxBrowser} is {@code null}.
	 *         <p>Or if {@link #firefoxOverridePath} is mismatched with given {@link FirefoxBrowser}.</p>
	 *         <p>Or if Extensions are provided when the given {@link FirefoxBrowser} is Linux.</p>
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(FirefoxBrowser _browser, Capabilities _capabilities, String... _firefoxExtensionNames) {

		LOGGER.info("WebDriverWrapper(_browser: {}, _capabilities: {}, _firefoxExtensionNames: ({}) ) [START]", _browser,
				(_capabilities == null ? "(NULL)" : _capabilities),
				(_firefoxExtensionNames == null ? "NULL" : _firefoxExtensionNames.length) );

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_browser, "Browser");

		if(firefoxOverridePath != null && (_browser == FirefoxBrowser.FIREFOX || _browser == FirefoxBrowser.FIREFOX_WIN)) {
			throw new IllegalArgumentException("firefoxOverridePath cannot be used with FirefoxBrowser enum: " + _browser + "!");
		}

		if(firefoxOverridePath != null && (_browser == FirefoxBrowser.FIREFOX_LINUX_32 || _browser == FirefoxBrowser.FIREFOX_LINUX_64)) {
			throw new IllegalArgumentException("firefoxOverridePath must be used with FirefoxBrowser enum: " + _browser + "!");
		}

		if(_firefoxExtensionNames != null && _firefoxExtensionNames.length > 0 && (_browser == FirefoxBrowser.FIREFOX_LINUX_32 || _browser == FirefoxBrowser.FIREFOX_LINUX_64)) {
			throw new IllegalArgumentException("Firefox extensions are not currently supported with FirefoxBrowser enum: " + _browser + "!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean extensionsWereLoaded = false;

		String userHome = System.getProperty("user.home");
		//noinspection SpellCheckingInspection
		File firefoxUserProfileDirectory = new File(userHome + (SystemUtils.IS_OS_MAC ? "/Library/Application Support" : "/AppData/Roaming/Mozilla")
				+ "/Firefox/Profiles/"); // (System.getenv( "USERPROFILE" ) only works on Windows.)
		FirefoxOptions options = new FirefoxOptions();
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		
		List<String> firefoxExtensionNames = _firefoxExtensionNames == null ? new ArrayList<>(0) : Arrays.asList(_firefoxExtensionNames);
		
		//------------------------ Code ----------------------------------------
		////////// Determine Gecko Version and Browser Path //////////
		if(_browser == FirefoxBrowser.FIREFOX || _browser == FirefoxBrowser.FIREFOX_WIN) {
			if(SystemUtils.IS_OS_WINDOWS) {
				if(new File(FirefoxBrowser.FIREFOX_WIN_64.BROWSER_PATH).exists()) {
					_browser = FirefoxBrowser.FIREFOX_WIN_64;
				}
				else {
					_browser = FirefoxBrowser.FIREFOX_WIN_32;
				}
			}
			else if(SystemUtils.IS_OS_MAC && _browser != FirefoxBrowser.FIREFOX_WIN) {
				_browser = FirefoxBrowser.FIREFOX_MAC;
			}
			else {
				throw new WebDriverWrapperException("Unknown or Unsupported Operating System: " + System.getProperty("os.name") + "!");
			}
		}

		synchronized(FIREFOX_LOCK) {

			////////// Set System Property "webdriver.gecko.driver" //////////
			File driverFile = DRIVER_FILES.get(_browser.DRIVER_NAME);
			if(driverFile == null) { // New Browser type or has not been copied out of JAR yet.

				String[] driverNameParts = _browser.DRIVER_NAME.split("\\.");
				driverFile = createDriverFile(driverNameParts[0], driverNameParts.length > 1 ? driverNameParts[1] : "");
				InputStream resourceInStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("gecko-drivers/" + _browser);
				try {
					FileUtils.copyToFile(resourceInStream, driverFile);
				}
				catch(IOException e) {
					throw new WebDriverWrapperException("Unable to copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!", e);
				}

				// Validate Copy.
				if(driverFile.length() <= 0) {
					throw new WebDriverWrapperException("Could not copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!\n\tFile size is 0!");
				}

				DRIVER_FILES.put(_browser.DRIVER_NAME, driverFile);
			}
			System.setProperty("webdriver.gecko.driver", driverFile.getAbsolutePath()); // Have to always set in case 32/64 version changed.

			options.setBinary(firefoxOverridePath != null ? firefoxOverridePath : _browser.getBrowserPath());

			////////// Install Existing Extensions //////////
			if(userHome.trim().matches("^[A-Z]:\\\\windows\\\\system32\\\\config\\\\systemprofile\\\\?$")) {
				LOGGER.warn("Program is running under a System Profile (most likely via a remote machine)."
						+ "Firefox Extensions will not be loaded, as the System Profile does not not contain any installed apps.");
			}
			else { // Local Machine.
				for(String firefoxExtensionName : firefoxExtensionNames) {

					// List only folders. (Avoids Mac ".DS_Store" file, as well as any other files.)
					File extension = new File(firefoxUserProfileDirectory + "/" + Objects.requireNonNull(firefoxUserProfileDirectory.list(
							(current, childName) -> new File(current, childName).isDirectory()))[0] + "/extensions/" + firefoxExtensionName);

					if(!extension.exists()) {
						throw new WebDriverWrapperException("Cannot find Extension " + Quotes.escape(firefoxExtensionName) + "!");
					}

					try {
						firefoxProfile.addExtension(extension);
						extensionsWereLoaded = true;
					}
					catch(Exception e) {
						throw new WebDriverWrapperException("Cannot find Extension " + Quotes.escape(firefoxExtensionName) + "!", e);
					}
				} //END-Loop (Firefox Extensions.)
			} // END-Else (Local Machine)

			////////// Auto Download //////////
			// This will force files of the following known types, to be auto downloaded.
			// (We do this to avoid the OS Download Dialog that Selenium cannot interact with.)
			// -----
			// Pulled from: http://www.iana.org/assignments/media-types/media-types.xhtml
			// Last Updated: 016-08-17
			//noinspection SpellCheckingInspection
			firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "" //
							// Applications:
							+ "application/1d-interleaved-parityfec;application/3gpdash-qoe-report+xml;application/3gpp-ims+xml;application/A2L;application/activemessage;application/activemessage;application/alto-costmap+json;application/alto-costmapfilter+json;application/alto-directory+json;application/alto-endpointcost+json;application/alto-endpointcostparams+json;application/alto-endpointprop+json;application/alto-endpointpropparams+json;application/alto-error+json;application/alto-networkmap+json;application/alto-networkmapfilter+json;application/AML;application/andrew-inset;application/applefile;application/ATF;application/ATFX;application/atom+xml;application/atomcat+xml;application/atomdeleted+xml;application/atomicmail;application/atomsvc+xml;application/ATXML;application/auth-policy+xml;application/bacnet-xdd+zip;application/batch-SMTP;application/beep+xml;application/calendar+json;application/calendar+xml;application/call-completion;application/CALS-1840;application/cbor;application/ccmp+xml;application/ccxml+xml;application/CDFX+XML;application/cdmi-capability;application/cdmi-container;application/cdmi-domain;application/cdmi-object;application/cdmi-queue;application/cdni;application/CEA;application/cea-2018+xml;application/cellml+xml;application/cfw;application/cms;application/cnrp+xml;application/coap-group+json;application/commonground;application/conference-info+xml;application/cpl+xml;application/csrattrs;application/csta+xml;application/CSTAdata+xml;application/csvm+json;application/cybercash;application/dash+xml;application/dashdelta;application/davmount+xml;application/dca-rft;application/DCD;application/dec-dx;application/dialog-info+xml;application/dicom;application/DII;application/DIT;application/dns;application/dskpp+xml;application/dssc+der;application/dssc+xml;application/dvcs;application/ecmascript;application/EDI-consent;application/EDIFACT;application/EDI-X12;application/efi;application/EmergencyCallData.Comment+xml;application/EmergencyCallData.DeviceInfo+xml;application/EmergencyCallData.ProviderInfo+xml;application/EmergencyCallData.ServiceInfo+xml;application/EmergencyCallData.SubscriberInfo+xml;application/emotionml+xml;application/encaprtp;application/epp+xml;application/epub+zip;application/eshop;application/example;application/fastinfoset;application/fastsoap;application/fdt+xml;application/fits;application/font-sfnt;application/font-tdpfr;application/font-woff;application/framework-attributes+xml;application/geo+json;application/gzip;application/H224;application/held+xml;application/http;application/hyperstudio;application/ibe-key-request+xml;application/ibe-pkg-reply+xml;application/ibe-pp-data;application/iges;application/im-iscomposing+xml;application/index;application/index.cmd;application/index.response;application/index.vnd;application/index-obj;application/inkml+xml;application/IOTP;application/ipfix;application/ipp;application/ISUP;application/its+xml;application/javascript;application/jose;application/jose+json;application/jrd+json;application/json;application/json-patch+json;application/json-seq;application/jwk+json;application/jwk-set+json;application/jwt;application/kpml-request+xml;application/kpml-response+xml;application/ld+json;application/lgr+xml;application/link-format;application/load-control+xml;application/lost+xml;application/lostsync+xml;application/LXF;application/mac-binhex40;application/macwriteii;application/mads+xml;application/marc;application/marcxml+xml;application/mathematica;application/mbms-associated-procedure-description+xml;application/mbms-deregister+xml;application/mbms-envelope+xml;application/mbms-msk+xml;application/mbms-msk-response+xml;application/mbms-protection-description+xml;application/mbms-reception-report+xml;application/mbms-register+xml;application/mbms-register-response+xml;application/mbms-schedule+xml;application/mbms-user-service-description+xml;application/mbox;application/media_control+xml;application/media-policy-dataset+xml;application/mediaservercontrol+xml;application/merge-patch+json;application/metalink4+xml;application/mets+xml;application/MF4;application/mikey;application/mods+xml;application/mosskey-data;application/mosskey-request;application/moss-keys;application/moss-signature;application/mp21;application/mp4;application/mpeg4-generic;application/mpeg4-iod;application/mpeg4-iod-xmt;application/mrb-consumer+xml;application/mrb-publish+xml;application/msc-ivr+xml;application/msc-mixer+xml;application/msword;application/mxf;application/nasdata;application/news-checkgroups;application/news-groupinfo;application/news-transmission;application/nlsml+xml;application/nss;application/ocsp-request;application/ocsp-response;application/octet-stream;application/ODA;application/ODX;application/oebps-package+xml;application/ogg;application/oxps;application/p2p-overlay+xml;application/patch-ops-error+xml;application/pdf;application/PDX;application/pgp-encrypted;application/pgp-signature;application/pidf+xml;application/pidf-diff+xml;application/pkcs10;application/pkcs12;application/pkcs7-mime;application/pkcs7-signature;application/pkcs8;application/pkix-attr-cert;application/pkix-cert;application/pkixcmp;application/pkix-crl;application/pkix-pkipath;application/pls+xml;application/poc-settings+xml;application/postscript;application/ppsp-tracker+json;application/problem+json;application/problem+xml;application/provenance+xml;application/prs.alvestrand.titrax-sheet;application/prs.cww;application/prs.hpub+zip;application/prs.nprend;application/prs.plucker;application/prs.rdf-xml-crypt;application/prs.xsf+xml;application/pskc+xml;application/QSIG;application/raptorfec;application/rdap+json;application/rdf+xml;application/reginfo+xml;application/relax-ng-compact-syntax;application/remote-printing;application/reputon+json;application/resource-lists+xml;application/resource-lists-diff+xml;application/rfc+xml;application/riscos;application/rlmi+xml;application/rls-services+xml;application/rpki-ghostbusters;application/rpki-manifest;application/rpki-roa;application/rpki-updown;application/rtf;application/rtploopback;application/rtx;application/samlassertion+xml;application/samlmetadata+xml;application/sbml+xml;application/scaip+xml;application/scim+json;application/scvp-cv-request;application/scvp-cv-response;application/scvp-vp-request;application/scvp-vp-response;application/sdp;application/sep+xml;application/sep-exi;application/session-info;application/set-payment;application/set-payment-initiation;application/set-registration;application/set-registration-initiation;application/SGML;application/sgml-open-catalog;application/shf+xml;application/sieve;application/simple-filter+xml;application/simple-message-summary;application/simpleSymbolContainer;application/slate;application/smil;application/smil+xml;application/smpte336m;application/soap+fastinfoset;application/soap+xml;application/spirits-event+xml;application/sql;application/srgs;application/srgs+xml;application/sru+xml;application/ssml+xml;application/tamp-apex-update;application/tamp-apex-update-confirm;application/tamp-community-update;application/tamp-community-update-confirm;application/tamp-error;application/tamp-sequence-adjust;application/tamp-sequence-adjust-confirm;application/tamp-status-query;application/tamp-status-response;application/tamp-update;application/tamp-update-confirm;application/tei+xml;application/thraud+xml;application/timestamped-data;application/timestamp-query;application/timestamp-reply;application/ttml+xml;application/tve-trigger;application/ulpfec;application/urc-grpsheet+xml;application/urc-ressheet+xml;application/urc-targetdesc+xml;application/urc-uisocketdesc+xml;application/vcard+json;application/vcard+xml;application/vemmi;application/vnd.3gpp.access-transfer-events+xml;application/vnd.3gpp.bsf+xml;application/vnd.3gpp.mid-call+xml;application/vnd.3gpp.pic-bw-large;application/vnd.3gpp.pic-bw-small;application/vnd.3gpp.pic-bw-var;application/vnd.3gpp.sms;application/vnd.3gpp.sms+xml;application/vnd.3gpp.srvcc-ext+xml;application/vnd.3gpp.SRVCC-info+xml;application/vnd.3gpp.state-and-event-info+xml;application/vnd.3gpp.ussd+xml;application/vnd.3gpp2.bcmcsinfo+xml;application/vnd.3gpp2.sms;application/vnd.3gpp2.tcap;application/vnd.3gpp-prose+xml;application/vnd.3gpp-prose-pc3ch+xml;application/vnd.3lightssoftware.imagescal;application/vnd.3M.Post-it-Notes;application/vnd.accpac.simply.aso;application/vnd.accpac.simply.imp;application/vnd.acucorp;application/vnd.adobe.flash-movie;application/vnd.adobe.formscentral.fcdt;application/vnd.adobe.fxp;application/vnd.adobe.partial-upload;application/vnd.adobe.xdp+xml;application/vnd.adobe.xfdf;application/vnd.aether.imp;application/vnd.ah-barcode;application/vnd.ahead.space;application/vnd.airzip.filesecure.azf;application/vnd.airzip.filesecure.azs;application/vnd.amazon.mobi8-ebook;application/vnd.americandynamics.acc;application/vnd.amiga.ami;application/vnd.amundsen.maze+xml;application/vnd.anki;application/vnd.anser-web-certificate-issue-initiation;application/vnd.antix.game-component;application/vnd.apache.thrift.binary;application/vnd.apache.thrift.compact;application/vnd.apache.thrift.json;application/vnd.api+json;application/vnd.apple.installer+xml;application/vnd.apple.mpegurl;application/vnd.arastra.swi;application/vnd.aristanetworks.swi;application/vnd.artsquare;application/vnd.astraea-software.iota;application/vnd.audiograph;application/vnd.autopackage;application/vnd.avistar+xml;application/vnd.balsamiq.bmml+xml;application/vnd.balsamiq.bmpr;application/vnd.bekitzur-stech+json;application/vnd.biopax.rdf+xml;application/vnd.blueice.multipass;application/vnd.bluetooth.ep.oob;application/vnd.bluetooth.le.oob;application/vnd.bmi;application/vnd.businessobjects;application/vnd.cab-jscript;application/vnd.canon-cpdl;application/vnd.canon-lips;application/vnd.cendio.thinlinc.clientconf;application/vnd.century-systems.tcp_stream;application/vnd.chemdraw+xml;application/vnd.chess-pgn;application/vnd.chipnuts.karaoke-mmd;application/vnd.cinderella;application/vnd.cirpack.isdn-ext;application/vnd.citationstyles.style+xml;application/vnd.claymore;application/vnd.cloanto.rp9;application/vnd.clonk.c4group;application/vnd.cluetrust.cartomobile-config;application/vnd.cluetrust.cartomobile-config-pkg;application/vnd.coffeescript;application/vnd.collection.doc+json;application/vnd.collection.next+json;application/vnd.collection+json;application/vnd.comicbook+zip;application/vnd.commerce-battelle;application/vnd.commonspace;application/vnd.contact.cmsg;application/vnd.coreos.ignition+json;application/vnd.cosmocaller;application/vnd.crick.clicker;application/vnd.crick.clicker.keyboard;application/vnd.crick.clicker.palette;application/vnd.crick.clicker.template;application/vnd.crick.clicker.wordbank;application/vnd.criticaltools.wbs+xml;application/vnd.ctc-posml;application/vnd.ctct.ws+xml;application/vnd.cups-pdf;application/vnd.cups-postscript;application/vnd.cups-ppd;application/vnd.cups-raster;application/vnd.cups-raw;application/vnd.cyan.dean.root+xml;application/vnd.cybank;application/vnd.data-vision.rdz;application/vnd.debian.binary-package;application/vnd.dece.data;application/vnd.dece.ttml+xml;application/vnd.dece.unspecified;application/vnd.dece-zip;application/vnd.denovo.fcselayout-link;application/vnd.desmume-movie;application/vnd.dir-bi.plate-dl-nosuffix;application/vnd.dm.delegation+xml;application/vnd.dna;application/vnd.document+json;application/vnd.dolby.mobile.1;application/vnd.dolby.mobile.2;application/vnd.doremir.scorecloud-binary-document;application/vnd.dpgraph;application/vnd.dreamfactory;application/vnd.drive+json;application/vnd.dtg.local;application/vnd.dtg.local.flash;application/vnd.dtg.local-html;application/vnd.dvb.ait;application/vnd.dvb.dvbj;application/vnd.dvb.esgcontainer;application/vnd.dvb.ipdcdftnotifaccess;application/vnd.dvb.ipdcesgaccess;application/vnd.dvb.ipdcesgaccess2;application/vnd.dvb.ipdcesgpdd;application/vnd.dvb.ipdcroaming;application/vnd.dvb.iptv.alfec-base;application/vnd.dvb.iptv.alfec-enhancement;application/vnd.dvb.notif-aggregate-root+xml;application/vnd.dvb.notif-container+xml;application/vnd.dvb.notif-generic+xml;application/vnd.dvb.notif-ia-msglist+xml;application/vnd.dvb.notif-ia-registration-request+xml;application/vnd.dvb.notif-ia-registration-response+xml;application/vnd.dvb.notif-init+xml;application/vnd.dvb.pfr;application/vnd.dvb_service;application/vnd.dynageo;application/vnd.dzr;application/vnd.easykaraoke.cdgdownload;application/vnd.ecdis-update;application/vnd.ecowin.chart;application/vnd.ecowin.filerequest;application/vnd.ecowin.fileupdate;application/vnd.ecowin.series;application/vnd.ecowin.seriesrequest;application/vnd.ecowin.seriesupdate;application/vnd.emclient.accessrequest+xml;application/vnd.enliven;application/vnd.enphase.envoy;application/vnd.eprints.data+xml;application/vnd.epson.esf;application/vnd.epson.msf;application/vnd.epson.quickanime;application/vnd.epson.salt;application/vnd.epson.ssf;application/vnd.ericsson.quickcall;application/vnd.espass-espass+zip;application/vnd.eszigno3+xml;application/vnd.etsi.aoc+xml;application/vnd.etsi.asic-e+zip;application/vnd.etsi.asic-s+zip;application/vnd.etsi.cug+xml;application/vnd.etsi.iptvcommand+xml;application/vnd.etsi.iptvdiscovery+xml;application/vnd.etsi.iptvprofile+xml;application/vnd.etsi.iptvsad-bc+xml;application/vnd.etsi.iptvsad-cod+xml;application/vnd.etsi.iptvsad-npvr+xml;application/vnd.etsi.iptvservice+xml;application/vnd.etsi.iptvsync+xml;application/vnd.etsi.iptvueprofile+xml;application/vnd.etsi.mcid+xml;application/vnd.etsi.mheg5;application/vnd.etsi.overload-control-policy-dataset+xml;application/vnd.etsi.pstn+xml;application/vnd.etsi.sci+xml;application/vnd.etsi.simservs+xml;application/vnd.etsi.timestamp-token;application/vnd.etsi.tsl.der;application/vnd.etsi.tsl+xml;application/vnd.eudora.data;application/vnd.ezpix-album;application/vnd.ezpix-package;application/vnd.fastcopy-disk-image;application/vnd.fdsn.mseed;application/vnd.fdsn.seed;application/vnd.ffsns;application/vnd.filmit.zfc;application/vnd.fints;application/vnd.firemonkeys.cloudcell;application/vnd.FloGraphIt;application/vnd.fluxtime.clip;application/vnd.font-fontforge-sfd;application/vnd.framemaker;application/vnd.frogans.fnc;application/vnd.frogans.ltf;application/vnd.fsc.weblaunch;application/vnd.f-secure.mobile;application/vnd.fujitsu.oasys;application/vnd.fujitsu.oasys2;application/vnd.fujitsu.oasys3;application/vnd.fujitsu.oasysgp;application/vnd.fujitsu.oasysprs;application/vnd.fujixerox.ART4;application/vnd.fujixerox.ART-EX;application/vnd.fujixerox.ddd;application/vnd.fujixerox.docuworks;application/vnd.fujixerox.docuworks.binder;application/vnd.fujixerox.docuworks.container;application/vnd.fujixerox.HBPL;application/vnd.fut-misnet;application/vnd.fuzzysheet;application/vnd.genomatix.tuxedo;application/vnd.geo+json;application/vnd.geocube+xml;application/vnd.geogebra.file;application/vnd.geogebra.tool;application/vnd.geometry-explorer;application/vnd.geonext;application/vnd.geoplan;application/vnd.geospace;application/vnd.gerber;application/vnd.globalplatform.card-content-mgt;application/vnd.globalplatform.card-content-mgt-response;application/vnd.gmx;application/vnd.google-earth.kml+xml;application/vnd.google-earth.kmz;application/vnd.gov.sk.e-form+xml;application/vnd.gov.sk.e-form+zip;application/vnd.gov.sk.xmldatacontainer+xml;application/vnd.grafeq;application/vnd.gridmp;application/vnd.groove-account;application/vnd.groove-help;application/vnd.groove-identity-message;application/vnd.groove-injector;application/vnd.groove-tool-message;application/vnd.groove-tool-template;application/vnd.groove-vcard;application/vnd.hal+json;application/vnd.hal+xml;application/vnd.HandHeld-Entertainment+xml;application/vnd.hbci;application/vnd.hcl-bireports;application/vnd.hdt;application/vnd.heroku+json;application/vnd.hhe.lesson-player;application/vnd.hp-HPGL;application/vnd.hp-hpid;application/vnd.hp-hps;application/vnd.hp-jlyt;application/vnd.hp-PCL;application/vnd.hp-PCLXL;application/vnd.httphone;application/vnd.hydrostatix.sof-data;application/vnd.hyperdrive+json;application/vnd.hzn-3d-crossword;application/vnd.ibm.afplinedata;application/vnd.ibm.electronic-media;application/vnd.ibm.MiniPay;application/vnd.ibm.modcap;application/vnd.ibm.rights-management;application/vnd.ibm.secure-container;application/vnd.iccprofile;application/vnd.ieee.1905;application/vnd.igloader;application/vnd.immervision-ivp;application/vnd.immervision-ivu;application/vnd.ims.imsccv1p1;application/vnd.ims.imsccv1p2;application/vnd.ims.imsccv1p3;application/vnd.ims.lis.v2.result+json;application/vnd.ims.lti.v2.toolconsumerprofile+json;application/vnd.ims.lti.v2.toolproxy.id+json;application/vnd.ims.lti.v2.toolproxy+json;application/vnd.ims.lti.v2.toolsettings.simple+json;application/vnd.ims.lti.v2.toolsettings+json;application/vnd.informedcontrol.rms+xml;application/vnd.informix-visionary;application/vnd.infotech.project;application/vnd.infotech.project+xml;application/vnd.innopath.wamp.notification;application/vnd.insors.igm;application/vnd.intercon.formnet;application/vnd.intergeo;application/vnd.intertrust.digibox;application/vnd.intertrust.nncp;application/vnd.intu.qbo;application/vnd.intu.qfx;application/vnd.iptc.g2.catalogitem+xml;application/vnd.iptc.g2.conceptitem+xml;application/vnd.iptc.g2.knowledgeitem+xml;application/vnd.iptc.g2.newsitem+xml;application/vnd.iptc.g2.newsmessage+xml;application/vnd.iptc.g2.packageitem+xml;application/vnd.iptc.g2.planningitem+xml;application/vnd.ipunplugged.rcprofile;application/vnd.irepository.package+xml;application/vnd.isac.fcs;application/vnd.is-xpr;application/vnd.jam;application/vnd.japannet-directory-service;application/vnd.japannet-jpnstore-wakeup;application/vnd.japannet-payment-wakeup;application/vnd.japannet-registration;application/vnd.japannet-registration-wakeup;application/vnd.japannet-setstore-wakeup;application/vnd.japannet-verification;application/vnd.japannet-verification-wakeup;application/vnd.jcp.javame.midlet-rms;application/vnd.jisp;application/vnd.joost.joda-archive;application/vnd.jsk.isdn-ngn;application/vnd.kahootz;application/vnd.kde.karbon;application/vnd.kde.kchart;application/vnd.kde.kformula;application/vnd.kde.kivio;application/vnd.kde.kontour;application/vnd.kde.kpresenter;application/vnd.kde.kspread;application/vnd.kde.kword;application/vnd.kenameaapp;application/vnd.kidspiration;application/vnd.Kinar;application/vnd.koan;application/vnd.kodak-descriptor;application/vnd.las.las+xml;application/vnd.liberty-request+xml;application/vnd.llamagraphics.life-balance.desktop;application/vnd.llamagraphics.life-balance.exchange+xml;application/vnd.lotus-1-2-3;application/vnd.lotus-approach;application/vnd.lotus-freelance;application/vnd.lotus-notes;application/vnd.lotus-organizer;application/vnd.lotus-screencam;application/vnd.lotus-wordpro;application/vnd.macports.portpkg;application/vnd.macports.portpkg;application/vnd.mapbox-vector-tile;application/vnd.marlin.drm.actiontoken+xml;application/vnd.marlin.drm.conftoken+xml;application/vnd.marlin.drm.license+xml;application/vnd.marlin.drm.mdcf;application/vnd.mason+json;application/vnd.maxmind.maxmind-db;application/vnd.mcd;application/vnd.medcalcdata;application/vnd.mediastation.cdkey;application/vnd.meridian-slingshot;application/vnd.MFER;application/vnd.mfmp;application/vnd.micro+json;application/vnd.micrografx.flo;application/vnd.micrografx-igx;application/vnd.microsoft.portable-executable;application/vnd.miele+json;application/vnd.minisoft-hp3000-save;application/vnd.mitsubishi.misty-guard.trustweb;application/vnd.Mobius.DAF;application/vnd.Mobius.DIS;application/vnd.Mobius.MBK;application/vnd.Mobius.MQY;application/vnd.Mobius.MSL;application/vnd.Mobius.PLC;application/vnd.Mobius.TXF;application/vnd.mophun.application;application/vnd.mophun.certificate;application/vnd.motorola.flexsuite;application/vnd.motorola.flexsuite.adsi;application/vnd.motorola.flexsuite.fis;application/vnd.motorola.flexsuite.gotap;application/vnd.motorola.flexsuite.kmr;application/vnd.motorola.flexsuite.ttc;application/vnd.motorola.flexsuite.wem;application/vnd.motorola.iprm;application/vnd.mozilla.xul+xml;application/vnd.ms-3mfdocument;application/vnd.msa-disk-image;application/vnd.ms-artgalry;application/vnd.ms-asf;application/vnd.ms-cab-compressed;application/vnd.mseq;application/vnd.ms-excel;application/vnd.ms-excel.addin.macroEnabled.12;application/vnd.ms-excel.sheet.binary.macroEnabled.12;application/vnd.ms-excel.sheet.macroEnabled.12;application/vnd.ms-excel.template.macroEnabled.12;application/vnd.ms-fontobject;application/vnd.ms-htmlhelp;application/vnd.msign;application/vnd.ms-ims;application/vnd.ms-lrm;application/vnd.ms-office.activeX+xml;application/vnd.ms-officetheme;application/vnd.ms-playready.initiator+xml;application/vnd.ms-powerpoint;application/vnd.ms-powerpoint.addin.macroEnabled.12;application/vnd.ms-powerpoint.presentation.macroEnabled.12;application/vnd.ms-powerpoint.slide.macroEnabled.12;application/vnd.ms-powerpoint.slideshow.macroEnabled.12;application/vnd.ms-powerpoint.template.macroEnabled.12;application/vnd.ms-PrintDeviceCapabilities+xml;application/vnd.ms-PrintSchemaTicket+xml;application/vnd.ms-project;application/vnd.ms-tnef;application/vnd.ms-windows.devicepairing;application/vnd.ms-windows.nwprinting.oob;application/vnd.ms-windows.printerpairing;application/vnd.ms-windows.wsd.oob;application/vnd.ms-wmdrm.lic-chlg-req;application/vnd.ms-wmdrm.lic-resp;application/vnd.ms-wmdrm.meter-chlg-req;application/vnd.ms-wmdrm.meter-resp;application/vnd.ms-word.document.macroEnabled.12;application/vnd.ms-word.template.macroEnabled.12;application/vnd.ms-works;application/vnd.ms-wpl;application/vnd.ms-xpsdocument;application/vnd.multiad.creator;application/vnd.multiad.creator.cif;application/vnd.musician;application/vnd.music-niff;application/vnd.muvee.style;application/vnd.mynfc;application/vnd.ncd.control;application/vnd.ncd.reference;application/vnd.nearst.inv+json;application/vnd.nervana;application/vnd.netfpx;application/vnd.neurolanguage.nlu;application/vnd.nintendo.nitro.rom;application/vnd.nintendo.snes.rom;application/vnd.nitf;application/vnd.noblenet-directory;application/vnd.noblenet-sealer;application/vnd.noblenet-web;application/vnd.nokia.catalogs;application/vnd.nokia.conml+wbxml;application/vnd.nokia.conml+xml;application/vnd.nokia.iptv.config+xml;application/vnd.nokia.iSDS-radio-presets;application/vnd.nokia.landmark+wbxml;application/vnd.nokia.landmark+xml;application/vnd.nokia.landmarkcollection+xml;application/vnd.nokia.ncd;application/vnd.nokia.n-gage.ac+xml;application/vnd.nokia.n-gage.data;application/vnd.nokia.n-gage.symbian.install;application/vnd.nokia.pcd+wbxml;application/vnd.nokia.pcd+xml;application/vnd.nokia.radio-preset;application/vnd.nokia.radio-presets;application/vnd.novadigm.EDM;application/vnd.novadigm.EDX;application/vnd.novadigm.EXT;application/vnd.ntt-local.content-share;application/vnd.ntt-local.file-transfer;application/vnd.ntt-local.ogw_remote-access;application/vnd.ntt-local.sip-ta_remote;application/vnd.ntt-local.sip-ta_tcp_stream;application/vnd.oasis.opendocument.chart;application/vnd.oasis.opendocument.chart-template;application/vnd.oasis.opendocument.database;application/vnd.oasis.opendocument.formula;application/vnd.oasis.opendocument.formula-template;application/vnd.oasis.opendocument.graphics;application/vnd.oasis.opendocument.graphics-template;application/vnd.oasis.opendocument.image;application/vnd.oasis.opendocument.image-template;application/vnd.oasis.opendocument.presentation;application/vnd.oasis.opendocument.presentation-template;application/vnd.oasis.opendocument.spreadsheet;application/vnd.oasis.opendocument.spreadsheet-template;application/vnd.oasis.opendocument.text;application/vnd.oasis.opendocument.text-master;application/vnd.oasis.opendocument.text-template;application/vnd.oasis.opendocument.text-web;application/vnd.obn;application/vnd.oftn.l10n+json;application/vnd.oipf.contentaccessdownload+xml;application/vnd.oipf.contentaccessstreaming+xml;application/vnd.oipf.cspg-hexbinary;application/vnd.oipf.dae.svg+xml;application/vnd.oipf.dae.xhtml+xml;application/vnd.oipf.mippvcontrolmessage+xml;application/vnd.oipf.pae.gem;application/vnd.oipf.spdiscovery+xml;application/vnd.oipf.spdlist+xml;application/vnd.oipf.ueprofile+xml;application/vnd.oipf.userprofile+xml;application/vnd.olpc-sugar;application/vnd.oma.bcast.associated-procedure-parameter+xml;application/vnd.oma.bcast.drm-trigger+xml;application/vnd.oma.bcast.imd+xml;application/vnd.oma.bcast.ltkm;application/vnd.oma.bcast.notification+xml;application/vnd.oma.bcast.provisioningtrigger;application/vnd.oma.bcast.sgboot;application/vnd.oma.bcast.sgdd+xml;application/vnd.oma.bcast.sgdu;application/vnd.oma.bcast.simple-symbol-container;application/vnd.oma.bcast.smartcard-trigger+xml;application/vnd.oma.bcast.sprov+xml;application/vnd.oma.bcast.stkm;application/vnd.oma.cab-address-book+xml;application/vnd.oma.cab-feature-handler+xml;application/vnd.oma.cab-pcc+xml;application/vnd.oma.cab-subs-invite+xml;application/vnd.oma.cab-user-prefs+xml;application/vnd.oma.dcd;application/vnd.oma.dcdc;application/vnd.oma.dd2+xml;application/vnd.oma.drm.risd+xml;application/vnd.oma.group-usage-list+xml;application/vnd.oma.lwm2m+json;application/vnd.oma.lwm2m+tlv;application/vnd.oma.pal+xml;application/vnd.oma.poc.detailed-progress-report+xml;application/vnd.oma.poc.final-report+xml;application/vnd.oma.poc.groups+xml;application/vnd.oma.poc.invocation-descriptor+xml;application/vnd.oma.poc.optimized-progress-report+xml;application/vnd.oma.push;application/vnd.oma.scidm.messages+xml;application/vnd.oma.xcap-directory+xml;application/vnd.omads-email+xml;application/vnd.omads-file+xml;application/vnd.omads-folder+xml;application/vnd.omaloc-supl-init;application/vnd.oma-scws-config;application/vnd.oma-scws-http-request;application/vnd.oma-scws-http-response;application/vnd.onepager;application/vnd.openblox.game+xml;application/vnd.openblox.game-binary;application/vnd.openeye.oeb;application/vnd.openxmlformats-officedocument.custom-properties+xml;application/vnd.openxmlformats-officedocument.customXmlProperties+xml;application/vnd.openxmlformats-officedocument.drawing+xml;application/vnd.openxmlformats-officedocument.drawingml.chart+xml;application/vnd.openxmlformats-officedocument.drawingml.chartshapes+xml;application/vnd.openxmlformats-officedocument.drawingml.diagramColors+xml;application/vnd.openxmlformats-officedocument.drawingml.diagramData+xml;application/vnd.openxmlformats-officedocument.drawingml.diagramLayout+xml;application/vnd.openxmlformats-officedocument.drawingml.diagramStyle+xml;application/vnd.openxmlformats-officedocument.extended-properties+xml;application/vnd.openxmlformats-officedocument.presentationml.commentAuthors+xml;application/vnd.openxmlformats-officedocument.presentationml.comments+xml;application/vnd.openxmlformats-officedocument.presentationml.handoutMaster+xml;application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml;application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml;application/vnd.openxmlformats-officedocument.presentationml.presentation;application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml;application/vnd.openxmlformats-officedocument.presentationml.presProps+xml;application/vnd.openxmlformats-officedocument.presentationml.slide;application/vnd.openxmlformats-officedocument.presentationml.slide+xml;application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml;application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml;application/vnd.openxmlformats-officedocument.presentationml.slideshow;application/vnd.openxmlformats-officedocument.presentationml.slideshow.main+xml;application/vnd.openxmlformats-officedocument.presentationml.slideUpdateInfo+xml;application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml;application/vnd.openxmlformats-officedocument.presentationml.tags+xml;application/vnd.openxmlformats-officedocument.presentationml.template.main+xml;application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml;application/vnd.openxmlformats-officedocument.presentationml-template;application/vnd.openxmlformats-officedocument.spreadsheetml.calcChain+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.chartsheet+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.connections+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.dialogsheet+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.externalLink+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheDefinition+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheRecords+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.pivotTable+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.queryTable+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.revisionHeaders+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.revisionLog+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.sheetMetadata+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.table+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.tableSingleCells+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.template.main+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.userNames+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.volatileDependencies+xml;application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml;application/vnd.openxmlformats-officedocument.spreadsheetml-template;application/vnd.openxmlformats-officedocument.theme+xml;application/vnd.openxmlformats-officedocument.themeOverride+xml;application/vnd.openxmlformats-officedocument.vmlDrawing;application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.document;application/vnd.openxmlformats-officedocument.wordprocessingml.document.glossary+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.endnotes+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml;application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml;application/vnd.openxmlformats-officedocument.wordprocessingml-template;application/vnd.openxmlformats-package.core-properties+xml;application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml;application/vnd.openxmlformats-package.relationships+xml;application/vnd.oracle.resource+json;application/vnd.orange.indata;application/vnd.osa.netdeploy;application/vnd.osgeo.mapguide.package;application/vnd.osgi.bundle;application/vnd.osgi.dp;application/vnd.osgi.subsystem;application/vnd.otps.ct-kip+xml;application/vnd.oxli.countgraph;application/vnd.pagerduty+json;application/vnd.palm;application/vnd.panoply;application/vnd.paos+xml;application/vnd.pawaafile;application/vnd.pcos;application/vnd.pg.format;application/vnd.pg.osasli;application/vnd.piaccess.application-licence;application/vnd.picsel;application/vnd.pmi.widget;application/vnd.poc.group-advertisement+xml;application/vnd.pocketlearn;application/vnd.powerbuilder6;application/vnd.powerbuilder6-s;application/vnd.powerbuilder7;application/vnd.powerbuilder75;application/vnd.powerbuilder75-s;application/vnd.powerbuilder7-s;application/vnd.preminet;application/vnd.previewsystems.box;application/vnd.proteus.magazine;application/vnd.publishare-delta-tree;application/vnd.pvi.ptid1;application/vnd.pwg-multiplexed;application/vnd.pwg-xhtml-print+xml;application/vnd.qualcomm.brew-app-res;application/vnd.quarantainenet;application/vnd.Quark.QuarkXPress;application/vnd.quobject-quoxdocument;application/vnd.radisys.moml+xml;application/vnd.radisys.msml+xml;application/vnd.radisys.msml-audit+xml;application/vnd.radisys.msml-audit-conf+xml;application/vnd.radisys.msml-audit-conn+xml;application/vnd.radisys.msml-audit-dialog+xml;application/vnd.radisys.msml-audit-stream+xml;application/vnd.radisys.msml-conf+xml;application/vnd.radisys.msml-dialog+xml;application/vnd.radisys.msml-dialog-base+xml;application/vnd.radisys.msml-dialog-fax-detect+xml;application/vnd.radisys.msml-dialog-fax-sendrecv+xml;application/vnd.radisys.msml-dialog-group+xml;application/vnd.radisys.msml-dialog-speech+xml;application/vnd.radisys.msml-dialog-transform+xml;application/vnd.rainstor.data;application/vnd.rapid;application/vnd.rar;application/vnd.realvnc.bed;application/vnd.recordare.musicxml;application/vnd.recordare.musicxml+xml;application/vnd.renlearn.rlprint;application/vnd.rig.cryptonote;application/vnd.route66.link66+xml;application/vnd.rs-274x;application/vnd.ruckus.download;application/vnd.s3sms;application/vnd.sailingtracker.track;application/vnd.sbm.cid;application/vnd.sbm.mid2;application/vnd.scribus;application/vnd.sealed.3df;application/vnd.sealed.csf;application/vnd.sealed.net;application/vnd.sealed-doc;application/vnd.sealed-eml;application/vnd.sealedmedia.softseal-html;application/vnd.sealedmedia.softseal-pdf;application/vnd.sealed-mht;application/vnd.sealed-ppt;application/vnd.sealed-tiff;application/vnd.sealed-xls;application/vnd.seemail;application/vnd.semd;application/vnd.semf;application/vnd.shana.informed.formdata;application/vnd.shana.informed.formtemplate;application/vnd.shana.informed.interchange;application/vnd.shana.informed.package;application/vnd.SimTech-MindMapper;application/vnd.siren+json;application/vnd.smaf;application/vnd.smart.notebook;application/vnd.smart.teacher;application/vnd.software602.filler.form+xml;application/vnd.software602.filler.form-xml-zip;application/vnd.solent.sdkm+xml;application/vnd.spotfire.dxp;application/vnd.spotfire.sfs;application/vnd.sss-cod;application/vnd.sss-dtf;application/vnd.sss-ntf;application/vnd.stepmania.package;application/vnd.stepmania.stepchart;application/vnd.street-stream;application/vnd.sun.wadl+xml;application/vnd.sus-calendar;application/vnd.svd;application/vnd.swiftview-ics;application/vnd.syncml.dm.notification;application/vnd.syncml.dm+wbxml;application/vnd.syncml.dm+xml;application/vnd.syncml.dmddf+wbxml;application/vnd.syncml.dmddf+xml;application/vnd.syncml.dmtnds+wbxml;application/vnd.syncml.dmtnds+xml;application/vnd.syncml.ds.notification;application/vnd.syncml+xml;application/vnd.tao.intent-module-archive;application/vnd.tcpdump.pcap;application/vnd.tmd.mediaflex.api+xml;application/vnd.tml;application/vnd.tmobile-livetv;application/vnd.trid.tpt;application/vnd.triscape.mxs;application/vnd.trueapp;application/vnd.truedoc;application/vnd.ubisoft.webplayer;application/vnd.ufdl;application/vnd.uiq.theme;application/vnd.umajin;application/vnd.unity;application/vnd.uoml+xml;application/vnd.uplanet.alert;application/vnd.uplanet.alert-wbxml;application/vnd.uplanet.bearer-choice;application/vnd.uplanet.bearer-choice-wbxml;application/vnd.uplanet.cacheop;application/vnd.uplanet.cacheop-wbxml;application/vnd.uplanet.channel;application/vnd.uplanet.channel-wbxml;application/vnd.uplanet.list;application/vnd.uplanet.listcmd;application/vnd.uplanet.listcmd-wbxml;application/vnd.uplanet.list-wbxml;application/vnd.uplanet.signal;application/vnd.uri-map;application/vnd.valve.source.material;application/vnd.vcx;application/vnd.vd-study;application/vnd.vectorworks;application/vnd.vel+json;application/vnd.verimatrix.vcas;application/vnd.vidsoft.vidconference;application/vnd.visio;application/vnd.visionary;application/vnd.vividence.scriptfile;application/vnd.vsf;application/vnd.wap.sic;application/vnd.wap.wmlscriptc;application/vnd.wap-slc;application/vnd.wap-wbxml;application/vnd.webturbo;application/vnd.wfa.p2p;application/vnd.wfa.wsc;application/vnd.windows.devicepairing;application/vnd.wmc;application/vnd.wmf.bootstrap;application/vnd.wolfram.mathematica;application/vnd.wolfram.mathematica.package;application/vnd.wolfram.player;application/vnd.wordperfect;application/vnd.wqd;application/vnd.wrq-hp3000-labelled;application/vnd.wt.stf;application/vnd.wv.csp+wbxml;application/vnd.wv.csp+xml;application/vnd.wv.ssp+xml;application/vnd.xacml+json;application/vnd.xara;application/vnd.xfdl;application/vnd.xfdl.webform;application/vnd.xmi+xml;application/vnd.xmpie.cpkg;application/vnd.xmpie.dpkg;application/vnd.xmpie.plan;application/vnd.xmpie.ppkg;application/vnd.xmpie.xlim;application/vnd.yamaha.hv-dic;application/vnd.yamaha.hv-script;application/vnd.yamaha.hv-voice;application/vnd.yamaha.openscoreformat;application/vnd.yamaha.openscoreformat.osfpvg+xml;application/vnd.yamaha.remote-setup;application/vnd.yamaha.smaf-audio;application/vnd.yamaha.smaf-phrase;application/vnd.yamaha.through-ngn;application/vnd.yamaha.tunnel-udpencap;application/vnd.yaoweme;application/vnd.yellowriver-custom-menu;application/vnd.zul;application/vnd.zzazz.deck+xml;application/vnd-acucobol;application/vnd-curl;application/vnd-dart;application/vnd-dxr;application/vnd-fdf;application/vnd-mif;application/vnd-sema;application/vnd-wap-wmlc;application/voicexml+xml;application/vq-rtcpxr;application/watcherinfo+xml;application/whoispp-query;application/whoispp-response;application/wita;application/wordperfect5.1;application/wsdl+xml;application/wspolicy+xml;application/x400-bp;application/xacml+xml;application/xcap-att+xml;application/xcap-caps+xml;application/xcap-diff+xml;application/xcap-el+xml;application/xcap-error+xml;application/xcap-ns+xml;application/xcon-conference-info+xml;application/xcon-conference-info-diff+xml;application/xenc+xml;application/xhtml+xml;application/xml;application/xml-dtd;application/xml-external-parsed-entity;application/xml-patch+xml;application/xmpp+xml;application/xop+xml;application/xv+xml;application/x-www-form-urlencoded;application/yang;application/yin+xml;application/zip;application/zlib;"
							// Audio:
							+ "audio/1d-interleaved-parityfec;audio/32kadpcm;audio/3gpp;audio/3gpp2;audio/ac3;audio/AMR;audio/AMR-WB;audio/amr-wb+;audio/aptx;audio/asc;audio/ATRAC3;audio/ATRAC-ADVANCED-LOSSLESS;audio/ATRAC-X;audio/basic;audio/BV16;audio/BV32;audio/clearmode;audio/CN;audio/DAT12;audio/dls;audio/dsr-es201108;audio/dsr-es202050;audio/dsr-es202211;audio/dsr-es202212;audio/DV;audio/DVI4;audio/eac3;audio/encaprtp;audio/EVRC;audio/EVRC0;audio/EVRC1;audio/EVRCB;audio/EVRCB0;audio/EVRCB1;audio/EVRCNW;audio/EVRCNW0;audio/EVRCNW1;audio/EVRC-QCP;audio/EVRCWB;audio/EVRCWB0;audio/EVRCWB1;audio/EVS;audio/example;audio/fwdred;audio/G711-0;audio/G719;audio/G722;audio/G7221;audio/G723;audio/G726-16;audio/G726-24;audio/G726-32;audio/G726-40;audio/G728;audio/G729;audio/G729D;audio/G729E;audio/GSM;audio/GSM-EFR;audio/GSM-HR-08;audio/iLBC;audio/ip-mr_v2.5;audio/L16;audio/L20;audio/L24;audio/L8;audio/LPC;audio/mobile-xmf;audio/mp4;audio/MP4A-LATM;audio/MPA;audio/mpa-robust;audio/mpeg;audio/mpeg4-generic;audio/ogg;audio/opus;audio/PCMA;audio/PCMA-WB;audio/PCMU;audio/PCMU-WB;audio/prs.sid;audio/raptorfec;audio/RED;audio/rtp-enc-aescm128;audio/rtploopback;audio/rtp-midi;audio/rtx;audio/SMV;audio/SMV0;audio/SMV-QCP;audio/speex;audio/sp-midi;audio/t140c;audio/t38;audio/telephone-event;audio/tone;audio/UEMCLIP;audio/ulpfec;audio/VDVI;audio/VMR-WB;audio/vnd.3gpp.iufp;audio/vnd.4SB;audio/vnd.audiokoz;audio/vnd.CELP;audio/vnd.cisco.nse;audio/vnd.cmles.radio-events;audio/vnd.cns.anp1;audio/vnd.cns.inf1;audio/vnd.dece.audio;audio/vnd.digital-winds;audio/vnd.dlna.adts;audio/vnd.dolby.heaac.1;audio/vnd.dolby.heaac.2;audio/vnd.dolby.mlp;audio/vnd.dolby.mps;audio/vnd.dolby.pl2;audio/vnd.dolby.pl2x;audio/vnd.dolby.pl2z;audio/vnd.dolby.pulse.1;audio/vnd.dra;audio/vnd.dts;audio/vnd.dts.hd;audio/vnd.dvb.file;audio/vnd.everad.plj;audio/vnd.hns.audio;audio/vnd.lucent.voice;audio/vnd.ms-playready.media.pya;audio/vnd.nokia.mobile-xmf;audio/vnd.nortel.vbk;audio/vnd.nuera.ecelp4800;audio/vnd.nuera.ecelp7470;audio/vnd.nuera.ecelp9600;audio/vnd.octel.sbc;audio/vnd.qcelp;audio/vnd.rhetorex.32kadpcm;audio/vnd.rip;audio/vnd.sealedmedia.softseal-mpeg;audio/vnd.vmx.cvsd;audio/vorbis;audio/vorbis-config;"
							// Image:
							+ "image/bmp;image/cgm;image/dicom-rle;image/emf;image/emf;image/example;image/fits;image/g3fax;image/jls;image/jp2;image/jpm;image/jpx;image/naplps;image/png;image/prs.btif;image/prs.pti;image/pwg-raster;image/t38;image/tiff;image/tiff-fx;image/vnd.adobe.photoshop;image/vnd.airzip.accelerator.azv;image/vnd.cns.inf2;image/vnd.dece.graphic;image/vnd.dvb.subtitle;image/vnd.dwg;image/vnd.dxf;image/vnd.fastbidsheet;image/vnd.fpx;image/vnd.fst;image/vnd.fujixerox.edmics-mmr;image/vnd.fujixerox.edmics-rlc;image/vnd.globalgraphics.pgb;image/vnd.microsoft.icon;image/vnd.mix;image/vnd.mozilla.apng;image/vnd.ms-modi;image/vnd.net-fpx;image/vnd.radiance;image/vnd.sealedmedia.softseal-gif;image/vnd.sealedmedia.softseal-jpg;image/vnd.sealed-png;image/vnd.tencent.tap;image/vnd.valve.source.texture;image/vnd.xiff;image/vnd.zbrush.pcx;image/vnd-djvu;image/vnd-svf;image/vnd-wap-wbmp;image/wmf;image/wmf;"
							// Text:
							+ "text/1d-interleaved-parityfec;text/cache-manifest;text/calendar;text/css;text/csv;text/csv-schema;text/directory;text/dns;text/ecmascript;text/encaprtp;text/example;text/fwdred;text/grammar-ref-list;text/html;text/javascript;text/jcr-cnd;text/markdown;text/mizar;text/n3;text/parameters;text/provenance-notation;text/prs.fallenstein.rst;text/prs.lines.tag;text/prs.prop.logic;text/raptorfec;text/RED;text/rfc822-headers;text/rtf;text/rtp-enc-aescm128;text/rtploopback;text/rtx;text/SGML;text/t140;text/tab-separated-values;text/troff;text/turtle;text/ulpfec;text/uri-list;text/vcard;text/vnd.abc;text/vnd.ascii-art;text/vnd.debian.copyright;text/vnd.DMClientScript;text/vnd.dvb.subtitle;text/vnd.esmertec.theme-descriptor;text/vnd.fly;text/vnd.fmi.flexstor;text/vnd.graphviz;text/vnd.in3d.3dml;text/vnd.in3d.spot;text/vnd.IPTC.NewsML;text/vnd.IPTC.NITF;text/vnd.latex-z;text/vnd.motorola.reflex;text/vnd.ms-mediapackage;text/vnd.net2phone.commcenter.command;text/vnd.radisys.msml-basic-layout;text/vnd.si.uricatalogue;text/vnd.sun.j2me.app-descriptor;text/vnd.trolltech.linguist;text/vnd.wap.si;text/vnd.wap.sl;text/vnd.wap.wmlscript;text/vnd.wap-wml;text/vnd-a;text/vnd-curl;text/xml;text/xml-external-parsed-entity;"
							// Video:
							+ "video/1d-interleaved-parityfec;video/3gpp;video/3gpp2;video/3gpp-tt;video/BMPEG;video/BT656;video/CelB;video/DV;video/encaprtp;video/example;video/H261;video/H263;video/H263-1998;video/H263-2000;video/H264;video/H264-RCDO;video/H264-SVC;video/H265;video/iso.segment;video/JPEG;video/jpeg2000;video/mj2;video/MP1S;video/MP2P;video/MP2T;video/mp4;video/MP4V-ES;video/mpeg4-generic;video/MPV;video/nv;video/ogg;video/pointer;video/quicktime;video/raptorfec;video/rtp-enc-aescm128;video/rtploopback;video/rtx;video/SMPTE292M;video/ulpfec;video/vc1;video/vnd.CCTV;video/vnd.dece.hd;video/vnd.dece.mobile;video/vnd.dece.pd;video/vnd.dece.sd;video/vnd.dece.video;video/vnd.dece-mp4;video/vnd.directv.mpeg-tts;video/vnd.directv-mpeg;video/vnd.dlna.mpeg-tts;video/vnd.dvb.file;video/vnd.fvt;video/vnd.hns.video;video/vnd.iptvforum.1dparityfec-1010;video/vnd.iptvforum.1dparityfec-2005;video/vnd.iptvforum.2dparityfec-1010;video/vnd.iptvforum.2dparityfec-2005;video/vnd.iptvforum.ttsavc;video/vnd.iptvforum.ttsmpeg2;video/vnd.motorola.video;video/vnd.motorola.videop;video/vnd.ms-playready.media.pyv;video/vnd.nokia.interleaved-multimedia;video/vnd.nokia.videovoip;video/vnd.objectvideo;video/vnd.radgamettools.bink;video/vnd.radgamettools.smacker;video/vnd.sealed.mpeg1;video/vnd.sealed.mpeg4;video/vnd.sealedmedia.softseal-mov;video/vnd.sealed-swf;video/vnd.uvvu-mp4;video/vnd-mpegurl;video/vnd-vivo;video/VP8;"
					// message, model, & multipart types have not been added because of their unknown usage.
			);

			options.setProfile(firefoxProfile);

			////////// Launch Browser //////////
			///// One of the 2 should work. (TODO)
			// (There is an separate Extension Log bug.)
			options.setCapability("log", "{\"level\": \"error\"}");
			options.setLogLevel(FirefoxDriverLogLevel.WARN);
			/////

			if(_capabilities != null) { // Merge has to happen after all other options are set.
                options.merge(_capabilities);
			}

			DRIVER = new FirefoxDriver(options);
			BROWSER_TYPE = BrowserType.FIREFOX;
			DRIVER_NAME = _browser.toString();
		}

		//( (RemoteWebDriver) DRIVER ).setLogLevel( Level.OFF ); // TODO: BUG: Makes all logs visible and gives them the level of what was padded in.

		maximize();

		// Close any Add-On Windows. (TODO)
		if(extensionsWereLoaded) {
			LOGGER.trace("Extensions were loaded.");
		}

		KNOWN_WEB_DRIVER_WRAPPERS.add(this);
		NEEDS_RETILING.set(true);

		LOGGER.debug("WebDriverWrapper(_browser: {}, _capabilities: {}, _firefoxExtensionNames: ({}) ) [END]", _browser,
				(_capabilities == null ? "(NULL)" : _capabilities),
				(_firefoxExtensionNames == null ? "NULL" : _firefoxExtensionNames.length) );
	}

	/**
	 * Instantiates this WebDriverWrapper to use the given {@link IEBrowser}.
	 * <p>
	 *     <b>Note:</b> IE requires that the "Enable Protected Mode" Security Settings be set to the same value for all Security Zones.
	 *     (see: <a href='http://automate-apps.com/unexpected-error-launching-internet-explorer-protected-mode-settings-are-not-the-same-for-all-zones/'>http://automate-apps.com/unexpected-error-launching-internet-explorer-protected-mode-settings-are-not-the-same-for-all-zones/</a>)
	 * </p>
	 * <p>
	 *     <b>Note:</b> IE requires that the "Pop-up Blocking Level" to be set to "Low", in order to use the {@link #openNewWindow(boolean)} functionality.
	 *     (see: <a href='https://turbofuture.com/internet/How-to-Turn-Off-Pop-Up-Blocker-in-Internet-Explorer-10'>How to Turn Off Pop-Up Blocker in Internet Explorer </a>)
	 * </p>
	 * <p>
	 *     <b>Note:</b> The window will be maximized.
	 * </p>
	 *
	 * @param _browser
	 * 		The Web {@link IEBrowser} to use.
	 * 		<p>
	 *     		<b>Note: </b> IE 64 bit has slow typing,
	 *     		due to bug: <a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116">Selenium Bug 5116</a>
	 * 		</p>
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link IEBrowser} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(IEBrowser _browser) {
		this(_browser, null);
	}

	/**
	 * Instantiates this WebDriverWrapper to use the given {@link IEBrowser}.
	 * <p>
	 *     <b>Note:</b> IE requires that the "Enable Protected Mode" Security Settings be set to the same value for all Security Zones.
	 *     (see: <a href='http://automate-apps.com/unexpected-error-launching-internet-explorer-protected-mode-settings-are-not-the-same-for-all-zones/'>http://automate-apps.com/unexpected-error-launching-internet-explorer-protected-mode-settings-are-not-the-same-for-all-zones/</a>)
	 * </p>
	 * <p>
	 *     <b>Note:</b> IE requires that the "Pop-up Blocking Level" to be set to "Low", in order to use the {@link #openNewWindow(boolean)} functionality.
	 *     (see: <a href='https://turbofuture.com/internet/How-to-Turn-Off-Pop-Up-Blocker-in-Internet-Explorer-10'>How to Turn Off Pop-Up Blocker in Internet Explorer </a>)
	 * </p>
	 * <p>
	 *     <b>Note:</b> The window will be maximized.
	 * </p>
	 *
	 * @param _browser
	 * 		The Web {@link IEBrowser} to use.
	 * 		<p>
	 *     		<b>Note: </b> IE 64 bit has slow typing,
	 *     		due to bug: <a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5116">Selenium Bug 5116</a>
	 * 		</p>
	 * @param _capabilities Allows you to send in custom Capabilities to the created {@link WebDriver}.
	 *                      (<b>Warning:</b> This will overwrite any default {@link WebDriverWrapper} settings.)
	 *
	 * @throws IllegalArgumentException
	 * 		If the given {@link IEBrowser} is {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper(IEBrowser _browser, Capabilities _capabilities) {

		LOGGER.info("WebDriverWrapper(_browser: {}, _capabilities: {}) [START]", _browser,
				(_capabilities == null ? "(NULL)" : _capabilities));

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_browser, "Browser");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		if(_browser == IEBrowser.IE_WIN) {
			
			if(new File(IEBrowser.IE_WIN_32.BROWSER_PATH).exists()) {
				_browser = IEBrowser.IE_WIN_32;
			}
			else {
				_browser = IEBrowser.IE_WIN_64;
			}
		}

		synchronized(IE_LOCK) {

			////////// Set System Property "webdriver.gecko.driver" ////////// IE must use a different Driver each time, so support multithreading.
			String[] driverNameParts = _browser.DRIVER_NAME.split("\\.");
			File driverFile = createDriverFile(driverNameParts[0], driverNameParts.length > 1 ? driverNameParts[1] : "");
			InputStream resourceInStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ie-drivers/" + _browser);
			try {
				FileUtils.copyToFile(resourceInStream, driverFile);
			}
			catch(IOException e) {
				throw new WebDriverWrapperException("Unable to copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!", e);
			}

			// Validate Copy.
			if(driverFile.length() <= 0) {
				throw new WebDriverWrapperException("Could not copy " + _browser + " to: " + driverFile.getAbsolutePath() + "!");
			}

			DRIVER_FILES.put(_browser.DRIVER_NAME, driverFile);
			System.setProperty("webdriver.ie.driver", driverFile.getAbsolutePath()); // Have to always set in case 32/64 version changed.

			////////// Launch Browser //////////
			InternetExplorerOptions options = new InternetExplorerOptions();

			// So that IE's Security "Enable Protected Mode" settings do not have to all be the same.
			//InternetExplorerOptions.introduceFlakinessByIgnoringSecurityDomains(); // Cannot use.
			// See: https://stackoverflow.com/questions/51106754/selenium-ie-upload-file-not-working/51106755#51106755.
			// IE's Security "Enable Protected Mode" settings will have to all be set to the same value.

			// Clears Browser's Cache.
			options.destructivelyEnsureCleanSession();
			// TODO: Stop using because it will clear the cache of browsers already running.
			// Use "Private" browsing instead.
			// TODO: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/6544
			/*options.useCreateProcessApiToLaunchIe();
			options.addCommandSwitches("-private");*/
			// OR.
			/*options.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
			options.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");*/

			// Use a private session to ensure a clean cache.
			// TODO: Blocked by Bug: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/7572.
			//options.useCreateProcessApiToLaunchIe();
			//options.addCommandSwitches("-private");

			// Fixes bug where a hover command only holds for an instant.
			//options.enablePersistentHovering();

			// If the Zoom was last set to something other that 100%, IE Driver will throw an error.
			// This ignores that error.
			options.ignoreZoomSettings();

			// IE uses "simulated" (javascript) events by default. (Because IE can sometimes miss commands if a user changes the focus.)
			// To force "native" events, uncomment these two calls, and remove the "enablePersistentHovering()" call.
			//options.enableNativeEvents(); // Using "native" events because "simulated" (javascript) events have been causing IE to randomly throw errors.
			// (It will also randomly miss click events, when in debug mode.) [TODO: Find a way to call out and report this bug.]

			// Will attempt to ensure that the IE browser window is in focus, before performing a command on it.
			//options.requireWindowFocus(); // Cannot use because it will randomly cause an "IE Command Line" error. (TODO: Report Bug to Selenium / IE Driver.)
			// (Note: IE will sometimes miss commands if the window is not in focus.)

			options.setCapability("logLevel", "ERROR"); // https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities#ie-specific

			if(_capabilities != null) { // Merge has to happen after all other options are set.
                options.merge(_capabilities);
			}

			DRIVER = new InternetExplorerDriver(options);
			BROWSER_TYPE = BrowserType.IE;
			DRIVER_NAME = _browser.toString();

			//((RemoteWebDriver) WEB_DRIVER).setLogLevel(Level.ALL); // Does not work.
		}

		// Maximize and rest Zoom.
		tileWindows(new ConcurrentSkipListSet<>(Collections.singleton(this)), true);
		//maximize();

		KNOWN_WEB_DRIVER_WRAPPERS.add(this);
		NEEDS_RETILING.set(true);

		LOGGER.debug("WebDriverWrapper(_browser: {}, _capabilities: {}) [END]", _browser,
				(_capabilities == null ? "(NULL)" : _capabilities));
	}

	//========================= Methods ========================================
	/**
	 * Close the current window.
	 * <ul>
	 *     <li>If this is the last window, the browser will quit.</li>
	 *     <li>If this is <b>NOT</b> the last window, we will attempt to switch to the previous window.</li>
	 * </ul>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void closeWindow() {

		LOGGER.info("closeWindow() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			boolean isLastWindow = DRIVER.getWindowHandles().size() == 1;

			if(isLastWindow) {
				KNOWN_WEB_DRIVER_WRAPPERS.remove(this);
				NEEDS_RETILING.set(true);
			}

			DRIVER.close();

			if(!isLastWindow) {
				switchToLastWindow();
			}
		}

		LOGGER.debug("closeWindow() [END]");
	}

	/**
	 * Will Execute the given JavaScript code.
	 *
	 * @param _javascriptCode
	 *         The JavaScript Code to be Executed.
	 *
	 * @return Whatever was returned by the JavaScript Code Execution, as a {@link String};
	 * or {@code null}, if nothing was returned by the JavaScript Code Execution.
	 *
	 * @throws IllegalArgumentException
	 *         If the given JavaScript Code is blank.
	 * @throws JavascriptException
	 *         If the JavaScript Code Execution returned something other than a {@link String}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String executeJavascript(String _javascriptCode) {

		LOGGER.info("executeJavascript(_javascriptCode: {}) [START]", _javascriptCode);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.stringNotWhitespaceOnly(_javascriptCode, "JavaScript Code");

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		JavascriptExecutor jsExecutor = (JavascriptExecutor) DRIVER;
		String toRet;

		//------------------------ Code ----------------------------------------
		Object jsReturnObject = jsExecutor.executeScript(_javascriptCode);

		if(jsReturnObject == null) {
			toRet = null;
		}
		else if(!(jsReturnObject instanceof String)) {
			throw new JavascriptException("Unknown Return Type: " + jsReturnObject.getClass().getName() + "!" + getUrlAndScreenshotExceptionMessagePart());
		}
		else {
			toRet = (String) jsReturnObject;
		}

		LOGGER.debug("executeJavascript(_javascriptCode: {}) [END]", _javascriptCode);

		return toRet;
	}

	/**
	 * Waits {@link #maxPageLoadTime} for an alert to pop-up, then returns the {@link Alert}.
	 *
	 * @return The {@link Alert} that is present, or {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public Alert getAlert() {
		return getAlert(maxPageLoadTime);
	}

	/**
	 * Waits up to the given Wait Time for an alert to pop-up, then returns the {@link Alert}.
	 *
	 * @param _waitTime
	 * 		How long to wait (in seconds) for the {@link Alert} to appear.
	 *
	 * @return The {@link Alert} that is present, or {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public Alert getAlert(Duration _waitTime) {

		LOGGER.info("getAlert(_waitTime: {}) [START]", _waitTime);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Alert alert;
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			fluentWait = new FluentWait<>(DRIVER);

			// Fluent Wait Settings.
			fluentWait.withTimeout(_waitTime).pollingEvery(POLLING_INTERVAL)
					.ignoring(NoSuchElementException.class);

			try {
				alert = fluentWait.until(ExpectedConditions.alertIsPresent());
			}
			catch(TimeoutException e) {
				alert = null;
			}
		}

		LOGGER.debug("getAlert(_waitTime: {}) [END]", _waitTime);

		return alert;
	}

	/**
	 * @return The URL of the page currently loaded in the browser.
	 *
	 * @see WebDriver#getCurrentUrl()
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getCurrentUrl() {

		LOGGER.info("getCurrentUrl() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String url = DRIVER.getCurrentUrl();
		
		//------------------------ Code ----------------------------------------
		LOGGER.debug("getCurrentUrl() [END]");

		return url;
	}

	/**
	 * @return The Title of the current page.
	 *
	 * @see WebDriver#getTitle()
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getPageTitle() {

		LOGGER.info("getPageTitle() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String title = DRIVER.getTitle();

		//------------------------ Code ----------------------------------------
		LOGGER.debug("getPageTitle() [END]: {}", title);

		return title;
	}

	/**
	 * @return The Source code of the current page.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see WebDriver#getPageSource()
	 */
	public String getPageSource() {

		LOGGER.info("getPageSource() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String toRet = DRIVER.getPageSource();

		//------------------------ Code ----------------------------------------
		LOGGER.debug("getPageSource() [END]");

		return toRet;
	}

	//////////////////// Get Web Element Wrapper(s) Functions [START] ////////////////////
	/**
	 * Waits around {@link #maxElementLoadTime} for the {@link WebElement} to exist and contain the correct visibility.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of Web Elements found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the Web Element.
	 *
	 * @return The Web Element Wrapper, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by) {
		return getWebElementWrapper(_by, maxElementLoadTime);
	}

	/**
	 * Waits around {@link #maxElementLoadTime} for the {@link WebElement} to exist and contain the correct visibility.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _visibility
	 *         If {@code true} only returns a visible {@link WebElement}, else if {@code false}, only returns a hidden {@link WebElement}.
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, boolean _visibility) {
		return getWebElementWrapper(_by, _visibility, maxElementLoadTime, null);
	}

	/**
	 * Waits the given amount of time for the {@link WebElement} to exist and contain the correct visibility, then grabs it.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, Duration _waitTime) {
		return getWebElementWrapper(_by, null, _waitTime, null);
	}

	/**
	 * Waits around {@link #maxElementLoadTime} for the {@link WebElement} to exist and contain the correct visibility.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _noElementExceptionMessage
	 *         If <b>not</b> {@code NULL}, a {@link NoSuchElementException} will be thrown with this message, if no Element is found.
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 *         If no {@link WebElement} is found, and if the {@code _noElementExceptionMessage} argument is <b>not</b> {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, String _noElementExceptionMessage) {
		return getWebElementWrapper(_by, null, maxElementLoadTime, _noElementExceptionMessage);
	}

	/**
	 * Waits the given amount of time for the {@link WebElement} to exist and contain the correct visibility, then grabs it.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _visibility
	 *         If {@code true} only returns a visible {@link WebElement}, else if {@code false}, only returns a hidden {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, boolean _visibility, Duration _waitTime) {
		return getWebElementWrapper(_by, _visibility, _waitTime, null);
	}

	/**
	 * Waits {@link #maxElementLoadTime} for the {@link WebElement} to exist and contain the correct visibility, then grabs it.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _visibility
	 *         If {@code true} only returns a visible {@link WebElement}, else if {@code false}, only returns a hidden {@link WebElement}.
	 * @param _noElementExceptionMessage
	 *         If <b>not</b> {@code NULL}, a {@link NoSuchElementException} will be thrown with this message, if no Element is found.
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code NULL}.
	 * @throws NoSuchElementException
	 *         If no {@link WebElement} is found, and if the {@code _noElementExceptionMessage} argument is <b>not</b> {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, boolean _visibility, String _noElementExceptionMessage) {
		return getWebElementWrapper(_by, _visibility, maxElementLoadTime, _noElementExceptionMessage);
	}

	/**
	 * Waits the given amount of time for the {@link WebElement} to exist and contain the correct visibility, then grabs it.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 * @param _noElementExceptionMessage
	 *         If <b>not</b> {@code NULL}, a {@link NoSuchElementException} will be thrown with this message, if no Element is found.
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @throws NoSuchElementException
	 *         If no {@link WebElement} is found, and if the {@code _noElementExceptionMessage} argument is <b>not</b> {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, Duration _waitTime, String _noElementExceptionMessage) {
		return getWebElementWrapper(_by, null, _waitTime, _noElementExceptionMessage);
	}

	/**
	 * Waits the given amount of time for the {@link WebElement} to exist and contain the correct visibility, then grabs it.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _visibility
	 *         If {@code true} only returns a visible {@link WebElement}, else if {@code false}, only returns a hidden {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 * @param _noElementExceptionMessage
	 *         If <b>not</b> {@code NULL}, a {@link NoSuchElementException} will be thrown with this message, if no Element is found.
	 *
	 * @return The {@link WebElementWrapper}, if successful; or {@code NULL}, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @throws NoSuchElementException
	 *         If no {@link WebElement} is found, and if the {@code _noElementExceptionMessage} argument is <b>not</b> {@code NULL}.
	 * @throws TooManyResultsException
	 *         If more than 1 {@link WebElement} is found.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebElementWrapper getWebElementWrapper(By _by, Boolean _visibility, Duration _waitTime, String _noElementExceptionMessage) {

		LOGGER.info("getWebElement(_by: {}, _visibility: {}, _waitTime: {}, _noElementExceptionMessage: {}) [START]",
				_by, _visibility, _waitTime, (_noElementExceptionMessage == null ? "(NULL)" : Quotes.escape(_noElementExceptionMessage)));

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		WebElementWrapper webElementWrapper = null;
		List<WebElementWrapper> webElementWrappers;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			webElementWrappers = getWebElementWrappers(null, _by, _waitTime, 1, _visibility);

			switch(webElementWrappers.size()) {
				case 0:
					if(_noElementExceptionMessage != null) {
						throw new NoSuchElementException(_noElementExceptionMessage + getUrlAndScreenshotExceptionMessagePart());
					}
					else {
						break; // Return NULL.
					}
				case 1:
					webElementWrapper = webElementWrappers.get(0);
					break;
				default:
					throw new TooManyResultsException("ERROR! Only 1 WebElement expected, but " + webElementWrappers.size() + " were found!\n\tBy: " + _by +
							getUrlAndScreenshotExceptionMessagePart());
			}
		}

		LOGGER.debug("getWebElement(_by: {}, _visibility: {}, _waitTime: {}, _noElementExceptionMessage: {}) [END]",
				_by, _visibility, _waitTime, (_noElementExceptionMessage == null ? "(NULL)" : Quotes.escape(_noElementExceptionMessage)));

		return webElementWrapper;
	}

	/**
	 * Waits {@link #maxElementLoadTime} for at least one {@link WebElement} to exist and contain the correct visibility; then grabs all of the available {@link
	 * WebElement}s.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}s.
	 *
	 * @return A List of {@link WebElementWrapper}(s), if successful; or an empty List, if not.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getWebElementWrappers(By _by) {
		return getWebElementWrappers(_by, maxElementLoadTime);
	}

	/**
	 * Waits {@link #maxElementLoadTime} for at least one {@link WebElement} to exist and contain the correct visibility; then grabs all of the available {@link
	 * WebElement}s.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.)
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _visibility
	 *         If {@code true} only returns visible {@link WebElement}s, else if {@code false}, only returns hidden {@link WebElement}s.
	 *
	 * @return A List of {@link WebElementWrapper}(s), if successful; or an empty List, if not.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getWebElementWrappers(By _by, boolean _visibility) {
		return getWebElementWrappers(null, _by, maxElementLoadTime, -1, _visibility);
	}

	/**
	 * Waits the given amount of time for at least one {@link WebElement} to exist and contain the correct visibility; then grabs all of the available {@link
	 * WebElement}s.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 *
	 * @return A List of {@link WebElementWrapper}(s), if successful; or an empty List, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getWebElementWrappers(By _by, Duration _waitTime) {
		return getWebElementWrappers(null, _by, _waitTime, -1, null);
	}

	/**
	 * Waits the given amount of time for at least one {@link WebElement} to exist and contain the correct visibility; then grabs all of the available {@link
	 * WebElement}s.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 * @param _visibility
	 *         If {@code true} only returns visible {@link WebElement}s, else if {@code false}, only returns hidden {@link WebElement}s.
	 *
	 * @return A List of {@link WebElementWrapper}(s), if successful; or an empty List, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>Or if the given Wait Time is negative.</p>
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public List<WebElementWrapper> getWebElementWrappers(By _by, boolean _visibility, Duration _waitTime) {
		return getWebElementWrappers(null, _by, _waitTime, -1, _visibility);
	}

	/**
	 * Waits the given amount of time for at least one {@link WebElement} to exist and contain the correct visibility; then grabs all of the available {@link
	 * WebElement}s.
	 * <p>
	 * <b>Note:</b> Wait time can be a little longer (proportional to number of {@link WebElement}s found),
	 * because of the time it takes to construct {@link WebElementWrapper}s.
	 * </p>
	 *
	 * @param _webElement
	 *         If present, the search will be at this {@link WebElement} level (a descendant search); else, It will be at the {@link WebDriver} (page) level.
	 * @param _by
	 *         How to search for the {@link WebElement}.
	 * @param _waitTime
	 *         How long to wait for the {@link WebElement} to exists and contain the correct visibility.
	 *         <p>Pass in {@code 0} to only try to get the Elements once.</p>
	 * @param _numOfElementsToGet
	 *         Number of {@link WebElement} we are looking for, or {@code -1} if we are looking for "all".
	 * @param _visibility
	 *         If set to {@code true}, grabs only visible {@link WebElement}s.
	 *         <p>If set to {@code false}, grabs only hidden {@link WebElement}s.</p>
	 *         <p>If {@code null}, all {@link WebElement}s are returned.</p>
	 *
	 * @return A List of {@link WebElementWrapper}(s), if successful; or an empty List, if not.
	 *
	 * @throws IllegalArgumentException
	 *         If the given By object is {@code null}.
	 *         <p>If the given Wait Time is negative.</p>
	 *         <p>Or if _numOfElementsToGet is not {@code 1} or {@code -1}.</p>
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	List<WebElementWrapper> getWebElementWrappers(WebElement _webElement, By _by, Duration _waitTime, int _numOfElementsToGet, Boolean _visibility) {

		synchronized(LOCK) {

			//------------------------ START Log ----------------------------------
			String webElementString = null;
			if(LOGGER.isInfoEnabled()) {

				webElementString = _webElement == null ? "NULL" : new WebElementWrapper(this, _webElement, null).toString();

				LOGGER.info("getWebElements(_webElement: {}, _by: {}, _waitTime: {}, _numOfElementToGet: {}, _visibility: {}) [START]",
						webElementString, _by, _waitTime, _numOfElementsToGet, _visibility);
			}

			//------------------------ Pre-Checks ----------------------------------
			ArgumentChecks.notNull(_by, "By");

			if(_waitTime.isNegative()) {
				throw new IllegalArgumentException("Given Wait Time cannot be negative! (" + _waitTime + ")");
			}

			if(_numOfElementsToGet != -1 && _numOfElementsToGet < 1) {
				throw new IllegalArgumentException("_numOfElementsToGet is < 1 but not -1! (" + _numOfElementsToGet + ")");
			}

			//------------------------ CONSTANTS -----------------------------------

			//------------------------ Variables -----------------------------------
			long startTime, endTime, msWaited;

			@SuppressWarnings("Convert2Diamond")
			FluentWait fluentWait = _webElement == null ? new FluentWait<WebDriver>(DRIVER) : new FluentWait<WebElement>(_webElement);

			List<WebElement> webElements;
			List<WebElementWrapper> webElementWrappers = new LinkedList<>();

			//------------------------ Code ----------------------------------------
			// Fluent Wait Settings.
			fluentWait.withTimeout(_waitTime).pollingEvery(POLLING_INTERVAL)
					.ignoreAll(Arrays.asList(new Class[]{NoSuchElementException.class, ElementNotVisibleException.class,
							InvalidElementStateException.class}));

			if(BROWSER_TYPE == BrowserType.IE) { // TODO: https://github.com/SeleniumHQ/selenium/issues/4555
				fluentWait.ignoring(WebDriverException.class); // TODO: Null pointer exception when calling webDriver.findElements #4555.
			}

			startTime = System.currentTimeMillis();

			//////////////////// Fluent Wait [START] ////////////////////
			try {
				//noinspection unchecked
				webElements = (List<WebElement>) fluentWait.until((Function<Object, List<WebElement>>) object -> {

					LOGGER.trace("FluentWait.until(Object) [START]");

					List<WebElement> elements;
					if(object instanceof WebDriver) {
						elements = ((WebDriver) object).findElements(_by);
					}
					else if(object instanceof WebElement) {
						elements = ((WebElement) object).findElements(_by);
					}
					else {
						throw new WebDriverWrapperException("Unknown argument type " + object.getClass() + " expecting WebDriver or WebElement!");
					}

					if(elements == null || elements.isEmpty()) {
						throw new NoSuchElementException("No Element found " + _by + "!");
					}

					LOGGER.trace("Found {} Elements.", elements.size());

					// Filter out Web Elements that do not match required visibility.
					if(_visibility != null) {

						LOGGER.trace("Validating all Web Elements visibility...");

						for(int i = 0; i < elements.size(); i++) {

							WebElement element = elements.get(i);
							boolean isDisplayed;
							try {
								isDisplayed = element.isDisplayed();
							}
							catch(StaleElementReferenceException e) {
								continue; // Ignore. Element will not be returned.
							}

							if(isDisplayed != _visibility) {

								// Remove Unwanted Element.
								elements.remove(i);
								i--;
							}
						} // END-Loop Through elements.

						if(elements.isEmpty()) {
							if(_visibility) {
								throw new ElementNotVisibleException("No Elements are Visible!");
							}
							else {
								throw new InvalidElementStateException("No Elements are Hidden!");
							}
						}
					} // END: Validating all Web Elements visibility.

					LOGGER.trace("FluentWait.until(Object) [END]");

					return elements;
				});
			}
			catch(TimeoutException e) {
				return webElementWrappers; // Return Empty List.
			}
			//////////////////// Fluent Wait [END] ////////////////////

			endTime = System.currentTimeMillis();
			msWaited = endTime - startTime;
			LOGGER.trace("Waited {} ms.", msWaited);

			if(webElements != null) { // (For Each loops blow up if collection is NULL.)

				By byUsed = webElements.size() == 1 && _webElement == null ? _by : null; // Does not count if it was a descendant search.

				// Create WebElementWrappers.
				int i = 0;
				for(WebElement webElement : webElements) {

					WebElementWrapper wew;
					try {
						wew = new WebElementWrapper(this, webElement, byUsed);
					}
					catch(NoSuchElementException/*Sometimes thrown by IE*/ | StaleElementReferenceException e) {
						
						LOGGER.trace(e);
						
						if(msWaited <= _waitTime.toMillis()) {
							return getWebElementWrappers(_webElement, _by, _waitTime.minusMillis(msWaited), _numOfElementsToGet, _visibility); // Try Again.
						}
						else {
							continue; // Ignore.
						}
					}

					webElementWrappers.add(wew);
					i++;

					if(_numOfElementsToGet > 0 && i >= _numOfElementsToGet) {
						break;
					}
				}
			}
			LOGGER.debug("getWebElements(_webElement: {}, _by: {}, _waitTime: {}, _numOfElementToGet: {}, _visibility: {}) [END]",
					webElementString, _by, _waitTime, _numOfElementsToGet, _visibility);

			return webElementWrappers;
		}
	}
	//////////////////// Get Web Element Wrapper(s) Functions [END] ////////////////////

	/**
	 * Loads a URL into the current Web Driver window and waits for the page to finish loading.
	 * <p>
	 *     <b>Note:</b> If an {@link Alert} is generated, it will be returned.
	 * </p>
	 * <p>
	 *     <b>Also:</b> This can load a file into the browser, if the "URL" is in the format <i>"file:ABSOLUTE_PATH"</i>.
	 * </p>
	 *
	 * @param _url
	 * 		The URL to load.
	 *
	 * @return The {@link Alert} that was generated as a result of the navigation; or {@code null}, if there is no {@link Alert}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #waitForPageLoad()
	 */
	public Alert goToUrl(String _url) {

		LOGGER.info("goToUrl(_url: {}) [START]", _url);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Alert alert;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

            DRIVER.navigate().to(_url); // Same thing as WebDriver.get(String).

			alert = waitForPageLoadOrAlert(); // User Wait/Think time happens here.
		}

		LOGGER.debug("goToUrl(_url: {}) - ({}) - [END]", _url, (alert == null ? "NULL" : "Alert") );

		return alert;
	}

	/**
	 * This method will tell you if the {@link WebDriver} has quit.
	 *
	 * @return true if the Browser has Quit or is unreachable.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public boolean hasQuit() {

		LOGGER.info("hasQuit() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		boolean hasQuit, hasSessionId;

		//------------------------ Code ----------------------------------------
		try {
			if(DRIVER instanceof RemoteWebDriver) {
				hasSessionId = ((RemoteWebDriver) DRIVER).getSessionId() != null;
			}
			else {
				hasSessionId = true; // Assume `true` if cannot be found.
			}

			hasQuit = DRIVER == null || DRIVER.getWindowHandles().isEmpty() || !hasSessionId;
		}
		catch(NoSuchSessionException | UnreachableBrowserException e) {
			hasQuit = true;
		}

		LOGGER.debug("hasQuit() [END]");

		return hasQuit;
	}

	/**
	 * Will Maximize the current browser window.
	 *
	 * @return This {@link WebDriverWrapper} for method call linking.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper maximize() {

		LOGGER.info("maximize() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		if(SystemUtils.IS_OS_MAC && DRIVER instanceof ChromeDriver) { // Chrome on Mac don't maximize, just expand as far as needed.

			// TODO: Walk through.

			int screenWidthAvailable = (int) (long) ((JavascriptExecutor) DRIVER).executeScript("return screen.availWidth;");
			int screenHeightAvailable = (int) (long) ((JavascriptExecutor) DRIVER).executeScript("return screen.availHeight;");

			WebDriver.Window window = DRIVER.manage().window();

			window.setSize(new Dimension(screenWidthAvailable, screenHeightAvailable));
			window.setPosition(new Point(0, 0));
		}
		else {
			DRIVER.manage().window().maximize();
		}

		LOGGER.debug("maximize() [END]");

		return this;
	}

	/**
	 * Opens a new Window and switches the given Web Driver to point to it.
	 * <p>
	 *     <b>Note:</b> IE requires that the "Pop-up Blocking Level" to be set to "Low", in order for Selenium to open a new window.
	 *     (see: <a href='https://turbofuture.com/internet/How-to-Turn-Off-Pop-Up-Blocker-in-Internet-Explorer-10'
	 *     >How to Turn Off Pop-Up Blocker in Internet Explorer </a>)
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void openNewWindow() {
		openNewWindow(false);
	}

	/**
	 * Opens a new Window and switches the given Web Driver to point to it.
	 * <p>
	 *     <b>Note:</b> IE requires that the "Pop-up Blocking Level" to be set to "Low", in order for Selenium to open a new window.
	 *     (see: <a href='https://turbofuture.com/internet/How-to-Turn-Off-Pop-Up-Blocker-in-Internet-Explorer-10'
	 *     >How to Turn Off Pop-Up Blocker in Internet Explorer </a>)
	 * </p>
	 *
	 * @param _closeOthers
	 * 		If {@code true}, all other Windows will be closed.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void openNewWindow(boolean _closeOthers) {

		LOGGER.info("openNewWindow(_closeOthers: {}) [START]", _closeOthers);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		((JavascriptExecutor) DRIVER).executeScript("window.open();");

		switchToLastWindow();

		if(_closeOthers) {

			String newHandle = DRIVER.getWindowHandle();

			for(String windowHandle : DRIVER.getWindowHandles()) {

				if(!windowHandle.equals(newHandle)) {

					DRIVER.switchTo().window(windowHandle);
					DRIVER.close();
				}
			}

			DRIVER.switchTo().window(newHandle);
		}

		LOGGER.debug("openNewWindow(_closeOthers: {}) [END]", _closeOthers);
	}

	/**
	 * Will put the given string into the Operating System's Clipboard, then executes the paste keyboard shortcut.
	 * <p>
	 *     <i>Note:</i> The Paste Command is sent to wherever the cursor currently is.
	 * </p>
	 *
	 * @param _clipboard
	 * 		The String to Paste.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void paste(String _clipboard) {

		LOGGER.info("paste(_clipboard: {}) [START]", _clipboard);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Actions actions = new Actions(DRIVER);

		//------------------------ Code ----------------------------------------
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(_clipboard), null);

		actions.sendKeys(Keys.chord(WebDriverWrapper.CTRL_CMD_KEY, "v")).perform();

		WebDriverWrapper.waitForUserThinkTime();

		LOGGER.debug("paste(_clipboard: {}) [END]", _clipboard);
	}

	/**
	 * Quits this {@link WebDriver}, closing every associated window.
	 * <p>
	 *     <b>Warning:</b> Do NOT try to use this object after closing it. This will have unexpected results.
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see WebDriver#quit()
	 */
	public void quit() {

		LOGGER.info("quit() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			KNOWN_WEB_DRIVER_WRAPPERS.remove(this);
			NEEDS_RETILING.set(true);

			while(true) {
				try {
					DRIVER.quit();
					break;
				}
				catch(WebDriverException e) { // Can happen when heavily multi-threading.
					String errorMessage = e.getMessage().trim().toLowerCase();
					if(!errorMessage.startsWith("java.net.connectexception: failed to connect to localhost/0:0:0:0:0:0:0:1:")) {
						throw e;
					}
				}
			}

			// IE Driver does not quit. (TODO)
			/*if(DRIVER instanceof InternetExplorerDriver) {
				try {
					Runtime.getRuntime().exec("taskkill /T /F /IM IEDriverServer.exe");
				} catch (IOException e) {
					LOGGER.warn("IE Driver could not be killed!", e);
				}
			}*/

			// TODO: https://github.com/teodesian/Selenium-Remote-Driver/issues/280
			// - When WebDriver.quit() is not called.

			// Need to validate that no other Object/Thread is using a Driver of the same name.
		}

		LOGGER.debug("quit() [END]");
	}

	/**
	 * Refreshes the current page and waits for the DOM to finish loading.
	 *
	 * @return The {@link Alert} that was generated as a result of the refresh; or {@code null}, if there is no {@link Alert}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #waitForPageLoad()
	 */
	public Alert refresh() {

		LOGGER.info("refresh() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Alert alert;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			DRIVER.navigate().refresh();

			alert = waitForPageLoadOrAlert(); // User Wait/Think time happens here.
		}

		LOGGER.debug("refresh() - {} - [END]", (alert == null ? "NULL" : "Alert") );

		return alert;
	}

	/**
	 * Will send keys to the &lt;body&gt; element, if it exists.
	 * <p>
	 *     <b>Note:</b> is the &lt;body&gt; element does not exist, then the keys will be sent to the root element.
	 * </p>
     *
     * @param _keys
     *            What characters to send to the &lt;body&gt; element / root.
	 *
	 * @return A reference to {@code this} {@link WebElementWrapper}, for method call chaining purposes.
	 *
	 * @throws IllegalArgumentException If the given Keys are {@code null} or an Empty String.
	 * @throws WebPageException If there is no root element.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper sendKeys(String _keys) {

		LOGGER.info("sendKeys(_keys: {}) [START]", _keys);

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_keys, "Keys");
		if(_keys.isEmpty()) {
			throw new IllegalArgumentException("Given Keys cannot be an Empty String!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		WebElementWrapper element;

		//------------------------ Code ----------------------------------------
		List<WebElementWrapper> bodyElements = getWebElementWrappers(By.tagName("body"));
		if(bodyElements.size() == 1) {
			element = bodyElements.get(0);
		}
		else {
			element = getWebElementWrapper(By.xpath("/*"));
		}

		if(element == null) {
			throw new WebDriverWrapperException("There is no page loaded in the browser!" + getUrlAndScreenshotExceptionMessagePart());
		}

		element.sendKeys(_keys); // User Wait/Think time happens here.

		LOGGER.debug("sendKeys(_keys: {}) [END]", _keys);

		return this;
	}

	/**
	 * Will set the size of the Browser Window.
	 *
	 * @param _x
	 *         Horizontal Size.
	 * @param _y
	 *         Vertical Size.
	 *
	 * @return A reference to {@code this} {@link WebElementWrapper}, for method call chaining purposes.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper setWindowSize(int _x, int _y) {

		LOGGER.info("setWindowSize(_x: {}, _y: {}) [START]", _x, _y);

		//------------------------ Pre-Checks ----------------------------------
		if(_x < 0 || _y < 0) {
			throw new IllegalArgumentException("Given coordinates (" + _x + ", " + _y + ") cannot be less than 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		setWindowSize(new Dimension(_x, _y));

		LOGGER.debug("setWindowSize(_x: {}, _y: {}) [END]", _x, _y);

		return this;
	}

	/**
	 * Will set the size of the Browser Window.
	 *
	 * @param _dimension
	 *         The Horizontal and Vertical Size, to set the window to.
	 *
	 * @return A reference to {@code this} {@link WebElementWrapper}, for method call chaining purposes.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public WebDriverWrapper setWindowSize(Dimension _dimension) {

		LOGGER.info("setWindowSize(_dimension: {}) [START]", _dimension);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		DRIVER.manage().window().setSize(_dimension);

		LOGGER.debug("setWindowSize(_dimension: {}) [END]", _dimension);

		return this;
	}

	/**
	 * Will scroll the current page so that the X/Y coordinates are at the top/left of the browser's window.
	 *
	 * @param _x
	 * 		Horizontal Position.
	 * @param _y
	 * 		Vertical Position.
	 *
	 * @throws IllegalArgumentException
	 * 		If either of the given coordinates are less than 0.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void scrollTo(int _x, int _y) {

		LOGGER.info("scrollTo(_x: {}, _y: {}) [START]", _x, _y);

		//------------------------ Pre-Checks ----------------------------------
		if(_x < 0 || _y < 0) {
			throw new IllegalArgumentException("Given coordinates (" + _x + ", " + _y + ") cannot be less than 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {
			((JavascriptExecutor) DRIVER).executeScript("scroll(" + _x + "," + _y + ")");
		}

		LOGGER.debug("scrollTo(_x: {}, _y: {}) [END]", _x, _y);
	}

	/**
	 * Switches to the one and only visible Frame/IFrame on the page.
	 * <p>
	 *     Waits {@link #maxPageLoadTime} for the Frame/IFrame to be available.
	 * </p>
	 * <p>
	 *     Same as calling {@link #switchToFrame(By)} and sending {@code By.xpath( "//frame | //iframe" )}.
	 * </p>
	 *
	 * @throws NoSuchElementException
	 * 		If no Frame/IFrame is visible after {@link #maxPageLoadTime} seconds of waiting.
	 * @throws TooManyResultsException
	 * 		If multiple Frames/IFrames are visible.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void switchToFrame() {
		switchToFrame(By.xpath("//frame | //iframe"));
	}

	/**
	 * Switches to the given Frame/IFrame.
	 * <p>
	 *     Waits {@link #maxPageLoadTime} for the Frame/IFrame to be available.
	 * </p>
	 *
	 * @param _by
	 * 		How to search for the Frame/IFrame.
	 *
	 * @throws NoSuchElementException
	 * 		If the Frame/IFrame cannot be found or is not visible after {@link #maxPageLoadTime} seconds of waiting.
	 * @throws TooManyResultsException
	 * 		If the search returns multiple, visible Frames/IFrames.
	 * @throws NoSuchFrameException
	 * 		If the found Element is not a Frame/IFrame.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see #getWebElementWrapper(By, Duration)
	 */
	public void switchToFrame(By _by) {

		LOGGER.info("switchToFrame(_by: {}) [START]", _by);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		List<WebElementWrapper> frames;
		
		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			frames = getWebElementWrappers(_by, maxPageLoadTime);

			/*
			 * Do not use ExpectedConditions.frameToBeAvailableAndSwitchToIt methods,
			 * because they will switch to invisible Frames.
			 */
			if(frames.isEmpty()) {
				throw new NoSuchElementException("ERROR! " + _by + " returns no visible Frame/IFrame!" + getUrlAndScreenshotExceptionMessagePart());
			}
			else if(frames.size() > 1) {
				throw new TooManyResultsException("ERROR! " + _by + " returns more than one visible Frame/IFrame!" + getUrlAndScreenshotExceptionMessagePart());
			}

			DRIVER.switchTo().frame(frames.get(0).getWebElement());

		}

		LOGGER.debug("switchToFrame(_by: {}) [END]", _by);
	}

	/**
	 * Switches to the last (last) opened Window.
	 * <p>
	 *     <b>Note:</b> May need to add a small wait, before calling this method, for the Window to load.
	 * </p>
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void switchToLastWindow() {

		LOGGER.info("switchToWindow() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			String[] windowHandles = DRIVER.getWindowHandles().toArray(new String[]{});
			DRIVER.switchTo().window(windowHandles[windowHandles.length - 1]);
		}

		LOGGER.debug("switchToWindow() [END]");
	}

	/**
	 * Switches to the Window at the given index.
	 * <p>
	 *     Waiting up to {@link #maxPageLoadTime} for the Window to load.
	 * </p>
	 * <p>
	 *     <b>Note:</b> Window indexes are in the order that the windows were opened and only include currently opened windows.
	 * </p>
	 *
	 * @param _index
	 * 		The 0-based index of the Window to switch to.
	 *
	 * @throws IndexOutOfBoundsException
	 * 		If the given Index is less than 0 or greater than the number of open windows,
	 * 		after waiting {@link #maxPageLoadTime} for the Window to load.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void switchToWindow(int _index) {

		LOGGER.info("switchToWindow(_index: {}) [START]", _index);

		//------------------------ Pre-Checks ----------------------------------
		if(_index < 0) {
			throw new IndexOutOfBoundsException("ERROR! Given Index (" + _index + ") cannot be less than 0!");
		}

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		long startTime_ms;
		
		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			startTime_ms = System.currentTimeMillis();

			////////// Wait For Window to Load //////////
			while(DRIVER.getWindowHandles().size() <= _index && System.currentTimeMillis() < startTime_ms + maxPageLoadTime.toMillis()) {

				try { // Wait for window to load.
					Thread.sleep(100);
				}
				catch(InterruptedException e) { /*Ignore*/ }
			}

			////////// Post-Checks //////////
			if(DRIVER.getWindowHandles().size() <= _index) {
				throw new IndexOutOfBoundsException("ERROR! Given Index (" + _index + ") is greater than the number of open windows, after waiting "
						+ maxPageLoadTime + " seconds!");
			}

			////////// Switch to Window //////////
			DRIVER.switchTo().window(DRIVER.getWindowHandles().toArray(new String[]{})[_index]);
		}

		LOGGER.debug("switchToWindow(_index: {}) [END]", _index);
	}

	/**
	 * Switches to the window with the given Page Title.
	 * <p>
	 *     Waiting up to {@link #maxPageLoadTime} for the Window to load.
	 * </p>
	 * <p>
	 *     <b>Note:</b> Page Title is compared, ignoring the case, after trimming and normalization have been applied to the Title(s) and compare string.
	 * </p>
	 *
	 * @param _pageTitle
	 * 		The Title of the Web Page's Browser Window to switch to.
	 *
	 * @throws NotFoundException
	 * 		If there is no Browser Window with the given Web Page Title.
	 * @throws TooManyResultsException
	 * 		If there is more than 1 Browser Window with the given Web Page Title.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void switchToWindow(String _pageTitle) {

		LOGGER.info("switchToWindow(_pageTitle: {}) [START]", _pageTitle);

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		int pageCount = 0;
		long startTime_ms;
		String desiredWindow = null, startingWindow;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			_pageTitle = StringHelper.trim(StringHelper.normalize(_pageTitle));

			startingWindow = DRIVER.getWindowHandle();
			startTime_ms = System.currentTimeMillis();
			do {
				// Try to find window.
				for(String window : DRIVER.getWindowHandles()) {

					DRIVER.switchTo().window(window);

					if(StringHelper.trim(StringHelper.normalize(DRIVER.getTitle())).equalsIgnoreCase(_pageTitle)) {
						desiredWindow = window;
						pageCount++;
					}
				}

				if(pageCount <= 0) {

					try { // Wait for window to load.
						Thread.sleep(POLLING_INTERVAL.toMillis());
					}
					catch(InterruptedException e) { /*Do Nothing*/ }
				}

			} while(pageCount <= 0 && System.currentTimeMillis() < startTime_ms + maxPageLoadTime.toMillis());

			if(pageCount <= 0) {

				DRIVER.switchTo().window(startingWindow);

				throw new NotFoundException("ERROR! No Window with the Title " + Quotes.escape(_pageTitle) + " was found!");
			}
			else if(pageCount == 1) {
				DRIVER.switchTo().window(desiredWindow);
			}
			else {
				DRIVER.switchTo().window(startingWindow);

				throw new TooManyResultsException("ERROR! Only 1 Window with the Title " + Quotes.escape(_pageTitle) + " expected but " + pageCount + " found!");
			}
		}

		LOGGER.debug("switchToWindow(_pageTitle: {}) [END]", _pageTitle);
	}

	/**
	 * Takes a screenshot of the current Window/Tab and saves it to {@link #screenshotPath} ({@value #SCREENSHOT_LOCATION}).
	 *
	 * @return The screenshot File that was saved.
	 *
	 * @throws WebDriverException If the Screenshot cannot be taken.
	 * @throws WebDriverWrapperException If the taken Screenshot cannot be saved.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public File takeScreenshot() {

		LOGGER.info("takeScreenshot() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String rootPath = screenshotPath.endsWith("/") || screenshotPath.endsWith("\\") ? screenshotPath : screenshotPath + "/";

		//------------------------ Code ----------------------------------------
		// Take Screenshot first. (We want go get it as soon as possible before something can change.)
		File tempScreenshot = null;
		while(tempScreenshot == null) {
			try {
				tempScreenshot = ((TakesScreenshot) DRIVER).getScreenshotAs(OutputType.FILE);
			}
			catch(WebDriverException e) { // Can happen when heavily multi-threading.
				String errorMessage = e.getMessage().trim().toLowerCase();
				if(!errorMessage.startsWith("java.net.connectexception: failed to connect to localhost/0:0:0:0:0:0:0:1:")) {
					throw e;
				}
			}
		}

		File screenshot = new File(rootPath + tempScreenshot.getName());

		try {
			FileUtils.copyFile(tempScreenshot, screenshot);
		}
		catch(IOException e) {
			throw new WebDriverWrapperException("Unable to save Screenshot!", e);
		}

		LOGGER.debug("takeScreenshot() [END]");

		return screenshot;
	}

	/**
	 * Will take a screenshot and throw a {@link WebPageException} with the given message and the screenshot's full path.
	 * <p>
	 *     <i>Note:</i> If there is an error taking the Screenshot, then the exception will just contain the given message.
	 * </p>
	 *
	 * @param _message
	 *         The Error message for the {@link WebPageException}.
	 *
	 * @throws WebPageException With the given message.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void throwWebPageException(String _message) throws WebPageException {
		throwWebPageException(_message, null);
	}
	
	/**
	 * Will take a screenshot and throw a {@link WebPageException} with the given message and the screenshot's full path.
	 * <p>
	 * <i>Note:</i> If there is an error taking the Screenshot, then the exception will just contain the given message.
	 * </p>
	 *
	 * @param _message
	 * 		The Error message for the {@link WebPageException}.
	 * @param _cause
	 * 		What other Exception triggered this call.
	 *
	 * @throws WebPageException
	 * 		With the given message.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	public void throwWebPageException(String _message, Throwable _cause) throws WebPageException {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		String errorMessage = _message + getUrlAndScreenshotExceptionMessagePart();
		
		if(_cause == null) {
			throw new WebPageException(errorMessage);
		}
		else {
			throw new WebPageException(errorMessage, _cause);
		}
	}

	/**
	 * This method will wait {@link #maxPageLoadTime} for the given page to load.
	 * <p>
	 *     <b>Note:</b> This will only wait for the DOM to be loaded. Any JavaScript may take longer.
	 * </p>
	 *
	 * @throws TimeoutException
	 * 		If the Page does not finish loading in {@link #maxPageLoadTime}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForPageLoad() {

		LOGGER.info("waitForPageLoad() [START]");

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------
		final String REQUIRED_DOCUMENT_READY_STATE = "complete";

		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;

		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {

			fluentWait = new FluentWait<>(DRIVER);

			// Fluent Wait Settings..
			fluentWait.withTimeout(maxPageLoadTime).pollingEvery(POLLING_INTERVAL)
					.ignoring(JavascriptException.class) /*IE throws this if the call is made to early in the page load.*/;

			try {
				fluentWait.until(driver ->
						((JavascriptExecutor) driver).executeScript("return document.readyState;").equals(REQUIRED_DOCUMENT_READY_STATE)
				);
			}
			catch(TimeoutException e) {

				///// Check if state is what we want (not sure why this happens). /////
				String documentReadyState = (String) ((JavascriptExecutor) DRIVER).executeScript("return document.readyState;");

				if(!documentReadyState.equals(REQUIRED_DOCUMENT_READY_STATE)) {
					throw new TimeoutException("document.readyState=" + documentReadyState + getUrlAndScreenshotExceptionMessagePart(), e);
				}
			}

			WebDriverWrapper.waitForUserThinkTime();
		}

		LOGGER.debug("waitForPageLoad() [END]");
	}

	/**
	 * This method will wait up to {@link #maxPageLoadTime}, for the Page's Title to equal the given value.
	 * <p>
	 * <i>(This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.)</i>
	 * </p>
	 * <p>
	 * <b>Note:</b> The Tile and Expected values will be trimmed, and the case will be ignored.
	 * </p>
	 *
	 * @param _value
	 *         What the Page's Title should be.
	 *
	 * @throws TimeoutException
	 *         If the Page's Title does not equal the given after {@link #maxPageLoadTime}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForTitle(String _value) {
		waitForTitle(_value, true, false, maxPageLoadTime);
	}

	/**
	 * This method will wait up to {@link #maxPageLoadTime}, for the Page's Title to equal the given value.
	 * <p>
	 * <i>Note:</i> This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.
	 * </p>
	 *
	 * @param _value
	 *         What the Page's Title should be.
	 * @param _trim
	 *         {@code true}, to trim both values before comparing.
	 * @param _caseMatters
	 *         {@code false}, to ignore case.
	 *
	 * @throws TimeoutException
	 *         If the Page's Title does not equal the given after {@link #maxPageLoadTime}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForTitle(String _value, boolean _trim, boolean _caseMatters) {
		waitForTitle(_value, _trim, _caseMatters, maxPageLoadTime);
	}

	/**
	 * This method will wait for the given amount of time, for the Page's Title to equal the given value.
	 * <p>
	 * <i>(This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.)</i>
	 * </p>
	 * <p>
	 * <b>Note:</b> The Tile and Expected values will be trimmed, and the case will be ignored.
	 * </p>
	 *
	 * @param _value
	 *         What the Page's Title should be.
	 * @param _maxWaitTime
	 *         The maximum amount of time to wait for the Title to equal the given value.
	 *
	 * @throws IllegalArgumentException
	 *         If the given Wait Time is {@code null}.
	 * @throws TimeoutException
	 *         If the Page's Title does not equal the given after the given Wait Time.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public void waitForTitle(String _value, Duration _maxWaitTime) {
		waitForTitle(_value, true, false, _maxWaitTime);
	}

	/**
	 * This method will wait for the given amount of time, for the Page's Title to equal the given value.
	 * <p>
	 * <i>Note:</i> This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.
	 * </p>
	 *
	 * @param _value
	 *         What the Page's Title should be.
	 * @param _trim
	 *         {@code true}, to trim both values before comparing.
	 * @param _caseMatters
	 *         {@code false}, to ignore case.
	 * @param _maxWaitTime
	 *         The maximum amount of time to wait for the Title to equal the given value.
	 *
	 * @throws IllegalArgumentException
	 *         If the given Value or Wait Time are {@code null}.
	 * @throws TimeoutException
	 *         If the Page's Title does not equal the given after the given Wait Time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	public void waitForTitle(String _value, boolean _trim, boolean _caseMatters, Duration _maxWaitTime) {

		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_value, "Expected Page Title Value");
		//ArgumentChecks.notNull(_maxWaitTime, "Max Wait Time"); // Will be validated below.

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------

		//------------------------ Code ----------------------------------------
		waitForTitle(Arrays.asList(_value), _trim, _caseMatters, _maxWaitTime);
	}
	
	/**
	 * This method will wait up to {@link #maxPageLoadTime}, for the Page's Title to equal one of the given values.
	 * <p>
	 * <i>(This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.)</i>
	 * </p>
	 * <p>
	 * <b>Note:</b> The Tile and Expected values will be trimmed, and the case will be ignored.
	 * </p>
	 *
	 * @param _values
	 * 		Collection of what the Page's Title could be.
	 *
	 * @return The Title that was found, if successful.
	 *
	 * @throws TimeoutException
	 * 		If the Page's Title does not equal the given after {@link #maxPageLoadTime}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String waitForTitle(Collection<String> _values) {
		return waitForTitle(_values, true, false, maxPageLoadTime);
	}
	
	/**
	 * This method will wait up to {@link #maxPageLoadTime}, for the Page's Title to equal one of the given values.
	 * <p>
	 * <i>Note:</i> This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.
	 * </p>
	 *
	 * @param _values
	 * 		Collection of what the Page's Title could be.
	 * @param _trim
	 *        {@code true}, to trim both values before comparing.
	 * @param _caseMatters
	 *        {@code false}, to ignore case.
	 *
	 * @return The Title that was found, if successful.
	 *
	 * @throws TimeoutException
	 * 		If the Page's Title does not equal the given after {@link #maxPageLoadTime}.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String waitForTitle(Collection<String> _values, boolean _trim, boolean _caseMatters) {
		return waitForTitle(_values, _trim, _caseMatters, maxPageLoadTime);
	}
	
	/**
	 * This method will wait for the given amount of time, for the Page's Title to equal one of the given values.
	 * <p>
	 * <i>(This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.)</i>
	 * </p>
	 * <p>
	 * <b>Note:</b> The Tile and Expected values will be trimmed, and the case will be ignored.
	 * </p>
	 *
	 * @param _values
	 * 		Collection of what the Page's Title could be.
	 * @param _maxWaitTime
	 * 		The maximum amount of time to wait for the Title to equal the given value.
	 *
	 * @return The Title that was found, if successful.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Wait Time is {@code null}.
	 * @throws TimeoutException
	 * 		If the Page's Title does not equal the given after the given Wait Time.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String waitForTitle(Collection<String> _values, Duration _maxWaitTime) {
		return waitForTitle(_values, true, false, _maxWaitTime);
	}
	
	/**
	 * This method will wait for the given amount of time, for the Page's Title to equal one of the given values.
	 * <p>
	 * <i>Note:</i> This can be useful to wait for redirects to finish, because {@link #waitForPageLoad()} will not handle redirects.
	 * </p>
	 *
	 * @param _values
	 * 		Collection of what the Page's Title could be.
	 * @param _trim
	 *        {@code true}, to trim both values before comparing.
	 * @param _caseMatters
	 *        {@code false}, to ignore case.
	 * @param _maxWaitTime
	 * 		The maximum amount of time to wait for the Title to equal the given value.
	 *
	 * @return The Title that was found, if successful.
	 *
	 * @throws IllegalArgumentException
	 * 		If the given Value or Wait Time are {@code null}.
	 * @throws TimeoutException
	 * 		If the Page's Title does not equal the given after the given Wait Time.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	public String waitForTitle(Collection<String> _values, boolean _trim, boolean _caseMatters, Duration _maxWaitTime) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notEmpty(_values, "Possible Page Title Values");
		ArgumentChecks.notNull(_maxWaitTime, "Max Wait Time");
		
		//------------------------ CONSTANTS -----------------------------------
		final Set<String> foundTitle = new HashSet(1);
		
		//------------------------ Variables -----------------------------------
		FluentWait<WebDriver> fluentWait;
		LocalDateTime startTime, endTime;
		
		//------------------------ Code ----------------------------------------
		synchronized(LOCK) {
			
			fluentWait = new FluentWait<>(DRIVER);
			
			// Fluent Wait Settings..
			fluentWait.withTimeout(_maxWaitTime).pollingEvery(POLLING_INTERVAL);
			
			startTime = LocalDateTime.now();
			
			try {
				fluentWait.until(driver -> {
					
					String currentTitle = _trim ? DRIVER.getTitle().trim() : DRIVER.getTitle();
					
					for(String expectedTitle : _values) {
						
						expectedTitle = _trim ? expectedTitle.trim() : expectedTitle;
						
						if(_caseMatters && currentTitle.equals(expectedTitle)) {
							foundTitle.add(expectedTitle);
							return true;
						}
						else if(currentTitle.equalsIgnoreCase(expectedTitle)) {
							foundTitle.add(expectedTitle);
							return true;
						}
					}
					
					return false;
				});
			}
			catch(TimeoutException e) {
				endTime = LocalDateTime.now();
				Duration timeSpent = Duration.between(startTime, endTime);
				String currentTitle = getPageTitle();
				throw new TimeoutException("Page title never equaled expected!\n\t\t  Actual: " + currentTitle +
						"\n\t\tExpected: " + _values +
						"\n\tReal Time Waited: " + timeSpent +
						getUrlAndScreenshotExceptionMessagePart(), e);
			}
			
			return foundTitle.iterator().next();
		}
	}

	//-------------------- Override Methods --------------------
	@Override
	public int compareTo(Object o) {

		if(!(o instanceof WebDriverWrapper)) {
			return -1;
		}
		WebDriverWrapper that = (WebDriverWrapper) o;
		return DRIVER.toString().compareTo(that.DRIVER.toString());
	}

	@Override
	public boolean equals(Object o) {

		if(this == o) {
			return true;
		}
		if(!(o instanceof WebDriverWrapper)) {
			return false;
		}
		WebDriverWrapper that = (WebDriverWrapper) o;
		return Objects.equals(DRIVER, that.DRIVER);
	}

	@Override
	public int hashCode() {
		return Objects.hash(DRIVER);
	}

	//-------------------- Helper Methods --------------------
	/**
	 * Starts with "\n\t".
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@LogMethods
	String getUrlAndScreenshotExceptionMessagePart() {

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		String screenshotExceptionMessagePart = "\n\tURL: " + getCurrentUrl();

		//------------------------ Code ----------------------------------------
		if(!autoTakeScreenshotsOnErrors) {
			return screenshotExceptionMessagePart;
		}
		
		screenshotExceptionMessagePart += "\n\tScreenshot: ";
		try {
			File screenshot = takeScreenshot();
			screenshotExceptionMessagePart += screenshot.getAbsolutePath();
		}
		catch(Exception e) {
			screenshotExceptionMessagePart += StringUtils.normalizeSpace(e.getLocalizedMessage());
		}

		return screenshotExceptionMessagePart;
	}

	/**
	 * Waits for the page to finish loading.
	 *
	 * @return The {@link Alert} that appeared; or {@code null}, if there is no {@link Alert}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private Alert waitForPageLoadOrAlert() {

		LOGGER.info( "waitForPageLoadOrAlert() [START]" );

		//------------------------ Pre-Checks ----------------------------------

		//------------------------ CONSTANTS -----------------------------------

		//------------------------ Variables -----------------------------------
		Alert alert;

		//------------------------ Code ----------------------------------------
		// Try to get alert first.
		alert = getAlert(Duration.ZERO);

		if(alert == null) {
			try { // Assume no Alert.
				waitForPageLoad(); // User Wait/Think time happens here.
			}
			catch(UnhandledAlertException e2) { // Catch the alert that loaded late.
				alert = DRIVER.switchTo().alert();
			}
		}

		LOGGER.debug( "waitForPageLoadOrAlert() - ({}) - [END]", alert == null ? "NULL" : "Alert" );

		return alert;
	}
}
