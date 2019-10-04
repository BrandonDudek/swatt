package xyz.swatt.tests.selenium.compatibility;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Quotes;
import org.testng.ITestContext;
import org.testng.annotations.*;
import xyz.swatt.selenium.WebDriverWrapper;
import xyz.swatt.selenium.WebDriverWrapper.ChromeBrowser;
import xyz.swatt.selenium.WebDriverWrapper.FirefoxBrowser;
import xyz.swatt.selenium.WebDriverWrapper.IEBrowser;
import xyz.swatt.selenium.WebElementWrapper;

import java.util.concurrent.TimeUnit;

/**
 * This class will test new versions of Selenium, Browsers, and Drivers.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class SeleniumCompatibilityTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(SeleniumCompatibilityTests.class);
    private static final String SEARCH_TERM = "Walla";

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static { }

    //========================= Static Methods =================================
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public static Object[][] factoryDataProvider() {

        if(SystemUtils.IS_OS_MAC) {

            return new Object[][]{
                    {ChromeBrowser.CHROME},  // Automatically Chooses OS.
                    {ChromeBrowser.CHROME_MAC_64},
                    {FirefoxBrowser.FIREFOX},  // Automatically Chooses OS.
                    {FirefoxBrowser.FIREFOX_MAC},

                    ///// HTML Unit Driver (GUI-less) /////
                    {"CHROME_HEADLESS"},
            };
        } else if (SystemUtils.IS_OS_WINDOWS) { // Windows.
            return new Object[][]{
                    {ChromeBrowser.CHROME}, // Automatically Chooses OS.
                    {ChromeBrowser.CHROME_WIN_32},
                    {FirefoxBrowser.FIREFOX}, // Automatically Chooses OS and 32/64 bit.
                    {FirefoxBrowser.FIREFOX_WIN}, // Automatically Chooses 32/64 bit.
                    {FirefoxBrowser.FIREFOX_WIN_64},
                    {FirefoxBrowser.FIREFOX_WIN_32},
                    {IEBrowser.IE_WIN}, // Automatically Chooses 32/64 bit.
                    {IEBrowser.IE_WIN_32},
                    {IEBrowser.IE_WIN_64},

                    ///// HTML Unit Driver (GUI-less) /////
                    {"CHROME_HEADLESS"},
            };
        } else if (SystemUtils.IS_OS_LINUX) { // Windows.
            return new Object[][]{
                    {ChromeBrowser.CHROME}, // Automatically Chooses OS.
                    {ChromeBrowser.CHROME_LINUX_64},

                    ///// HTML Unit Driver (GUI-less) /////
                    {"CHROME_HEADLESS"},
            };
        } else {
            throw new RuntimeException("OS could not be determined or is not supported!");
        }
    }

    //========================= CONSTANTS ======================================
    private final Object BROWSER_TYPE;

    //========================= Variables ======================================
    private WebDriverWrapper driver;

    //========================= Constructors ===================================
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Factory(dataProvider = "factoryDataProvider")
    public SeleniumCompatibilityTests(Object _browserType) {

        LOGGER.info("SeleniumCompatibilityTests( _browserType: {} ) [START]", _browserType);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        BROWSER_TYPE = _browserType;

        //------------------------ Code ----------------------------------------

        LOGGER.debug("SeleniumCompatibilityTests( _browserType: {} ) [END]", _browserType);
    }

    //========================= Methods ========================================
    //-------------------- DataProviders --------------------

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] browserDriverNameData(ITestContext _iTestContext) {
        return new Object[][]{
                {BROWSER_TYPE.toString()},
        };
    }

    //-------------------- Before --------------------

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @BeforeSuite
    public void closeOldDriverProcesses() {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebDriverWrapper.killPreviousBrowserDriverProcesses();
    }

    //-------------------- Tests --------------------
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData")
    public void startBrowser(String _browserDriverName) {

        LOGGER.info("startBrowser() - {} - [START]", BROWSER_TYPE);

        loadBrowser();

        // TODO: Validate Browser Type.

        LOGGER.debug("startBrowser() - {} - [END]", BROWSER_TYPE);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData", dependsOnMethods = {"startBrowser"})
    public void loadPage(String _browserDriverName) {

        LOGGER.info("loadPage() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        driver.goToUrl("http://google.com/");
        String pageTitle = driver.getPageTitle();
        if(!pageTitle.equals("Google")) {
            throw new RuntimeException("Google failed to load! (" + pageTitle + ")");
        }

        LOGGER.debug("loadPage() [END]");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData", dependsOnMethods = {"loadPage"})
    public void sendKeys(String _browserDriverName) {

        LOGGER.info("sendKeys() [START] - {}", BROWSER_TYPE);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebElementWrapper input = driver.getWebElementWrapper(By.cssSelector("input[name=q]"), true,
                "Could not find Google's search input element!");
        input.sendKeys(SEARCH_TERM + Keys.ESCAPE);

        ///// Validate /////
        String inputValue = input.getValue();
        if(!inputValue.equals(SEARCH_TERM)) {
            throw new RuntimeException(Quotes.escape(SEARCH_TERM) + " was sent to input, but " + Quotes.escape(inputValue) + " was found!");
        }

        LOGGER.debug("sendKeys() [END]");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData", dependsOnMethods = {"sendKeys"})
    public void click(String _browserDriverName) {

        LOGGER.info("click() [START] - {}", BROWSER_TYPE);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebElementWrapper button = driver.getWebElementWrapper(By.cssSelector("#tsf input[aria-label='Google Search']"), true,
                "Cannot find the \"Google Search\" button!");
        button.click(true);

        ///// Validate /////
        driver.getWebElementWrapper(By.id("resultStats"), true, WebDriverWrapper.maxPageLoadTime,
                "Search Results failed to load!");
        String pageTitle = driver.getPageTitle();
        if(!pageTitle.equals(SEARCH_TERM + " - Google Search")) {
            throw new RuntimeException("Google failed to load! (\"" + pageTitle + "\" loaded instead.)");
        }

        LOGGER.debug("click() [END]");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData", dependsOnMethods = {"click"})
    public void tileWindows(String _browserDriverName) {

        LOGGER.info("tileWindows() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebDriverWrapper driver2 = loadBrowser();
        WebDriverWrapper.tileWindows(true);

        // TODO: Validate window size, window positions, and zoom percent. (using visual inspection now)
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        catch(InterruptedException e) { /*Do Nothing*/ }

        driver2.quit();
        WebDriverWrapper.tileWindows(true);

        // TODO: Validate window size, window positions, and zoom percent. (using visual inspection now)
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        catch(InterruptedException e) { /*Do Nothing*/ }

        LOGGER.debug("tileWindows() [END]");
    }

    /**
     * Opens a new Tab/Window and then closes it.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "browserDriverNameData", dependsOnMethods = {"tileWindows"})
    public void openCloseWindow(String _browserDriverName) {

        LOGGER.info("openCloseTab() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        String oldWindowTitle = driver.getPageTitle();

        driver.openNewWindow();
        String newWindowTitle = driver.getPageTitle();
        if(newWindowTitle.equals(oldWindowTitle)) {
            throw new RuntimeException("Opened new window, but found old window title: " + Quotes.escape(oldWindowTitle) + "!");
        }

        // TODO: Validate Window Title and Count. (using visual inspection now)

        driver.closeWindow();
        String oldWindowTitleAgain = driver.getPageTitle();
        if(!oldWindowTitleAgain.equals(oldWindowTitle)) {
            throw new RuntimeException("Returned to old Window. Expecting " + Quotes.escape(oldWindowTitle) + " but found " +
                    Quotes.escape(oldWindowTitleAgain) + "!");
        }

        // TODO: Validate Window Title and Count. (using visual inspection now)

        LOGGER.debug("openCloseWindow() [END]");
    }

    //-------------------- After --------------------
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @AfterClass
    public void afterClass() {

        LOGGER.info("afterClass() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        driver.quit();

        WebDriverWrapper.killUsedBrowserDriverProcesses();

        LOGGER.debug("afterClass() [END]");
    }

    ///////////////////////// Helper Methods /////////////////////////
    /**
     * Will create a new {@link WebDriverWrapper} instance.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    private WebDriverWrapper loadBrowser() {

        LOGGER.info("loadBrowser() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        WebDriverWrapper newDriver;

        //------------------------ Code ----------------------------------------
        if(BROWSER_TYPE instanceof ChromeBrowser) {
            newDriver = new WebDriverWrapper((ChromeBrowser) BROWSER_TYPE);
        }
        else if(BROWSER_TYPE instanceof FirefoxBrowser) {
            newDriver = new WebDriverWrapper((FirefoxBrowser) BROWSER_TYPE);
        }
        else if(BROWSER_TYPE instanceof IEBrowser) {
            newDriver = new WebDriverWrapper((IEBrowser) BROWSER_TYPE);
        }
        else {
            newDriver = new WebDriverWrapper(ChromeBrowser.CHROME, true);
        }

        if(driver == null) {
            driver = newDriver;
        }

        LOGGER.debug("loadBrowser() [END]");

        return newDriver;
    }

    /**
     * @return The {@link #BROWSER_TYPE}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return BROWSER_TYPE.toString();
    }

    //========================= Classes ========================================
}
