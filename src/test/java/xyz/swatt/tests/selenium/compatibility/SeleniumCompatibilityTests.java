package xyz.swatt.tests.selenium.compatibility;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import xyz.swatt.selenium.WebDriverWrapper;
import xyz.swatt.selenium.WebDriverWrapper.ChromeBrowser;
import xyz.swatt.selenium.WebDriverWrapper.FirefoxBrowser;
import xyz.swatt.selenium.WebElementWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Quotes;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
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

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static { }

    //========================= Static Methods =================================
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public static Object[][] factoryDataProvider() {

        if(WebDriverWrapper.IS_MAC) {

            return new Object[][]{
                    {ChromeBrowser.CHROME},
                    {ChromeBrowser.CHROME_MAC_64},
                    {FirefoxBrowser.FIREFOX},
                    {FirefoxBrowser.FIREFOX_MAC},
                    {BrowserVersion.FIREFOX_52}, // HTML Unit Driver. (UI-less)
            };
        }
        else { // Windows.
            return new Object[][]{
                    {ChromeBrowser.CHROME},
                    {ChromeBrowser.CHROME_WIN_32},
                    {FirefoxBrowser.FIREFOX}, // Automatically Chooses OS and 32/64 bit.
                    {FirefoxBrowser.FIREFOX_WIN}, // Automatically Chooses 32/64 bit.
                    {FirefoxBrowser.FIREFOX_WIN_64},
                    {FirefoxBrowser.FIREFOX_WIN_32},
                    {BrowserVersion.FIREFOX_52}, // HTML Unit Driver. (UI-less)
            };
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
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test
    public void startBrowser() {

        LOGGER.info("startBrowser() - {} - [START]", BROWSER_TYPE);

        loadBrowser();

        LOGGER.debug("startBrowser() - {} - [END]", BROWSER_TYPE);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dependsOnMethods = {"startBrowser"})
    public void loadPage() {

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
    @Test(dependsOnMethods = {"loadPage"})
    public void tileWindows() {

        LOGGER.info("tileWindows() [START]");

        //------------------------ Pre-Checks ----------------------------------
        if(BROWSER_TYPE instanceof BrowserVersion) {
            return; // Cannot manipulate the window of a GUI-less browser.
        }

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebDriverWrapper driver2 = loadBrowser();
        CopyOnWriteArrayList<WebDriverWrapper> drivers = new CopyOnWriteArrayList<>(Arrays.asList(driver, driver2));
        WebDriverWrapper.tileWindows(drivers, true);

        // TODO: Validate window size, window positions, and zoom percent. (using visual inspection now)
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        catch(InterruptedException e) { /*Do Nothing*/ }

        driver2.quit();
        drivers.remove(driver2);
        WebDriverWrapper.tileWindows(drivers, true);

        // TODO: Validate window size, window positions, and zoom percent. (using visual inspection now)
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        catch(InterruptedException e) { /*Do Nothing*/ }

        LOGGER.debug("tileWindows() [END]");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dependsOnMethods = {"loadPage"})
    public void sendKeys() {

        LOGGER.info("sendKeys() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        final String keysToSend = "Walla";

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebElementWrapper input = driver.getWebElementWrapper(By.id("lst-ib"), true,
                "Could not find Google's search input element!");
        input.sendKeys(keysToSend);

        ///// Validate /////
        String inputValue = input.getValue();
        if(!inputValue.equals(keysToSend)) {
            throw new RuntimeException(Quotes.escape(keysToSend) + " was sent to input, but " + Quotes.escape(inputValue) + " was found!");
        }

        LOGGER.debug("sendKeys() [END]");
    }

    /**
     * Opens a new Tab/Window and then closes it.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dependsOnMethods = {"loadPage"})
    public void openCloseWindow() {

        LOGGER.info("openCloseTab() [START]");

        //------------------------ Pre-Checks ----------------------------------
        if(BROWSER_TYPE instanceof BrowserVersion) {
            return; // Cannot manipulate the window of a GUI-less browser.
        }

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
            throw new RuntimeException("Returned to old Window. Expecting " + Quotes.escape(oldWindowTitle) + " but found " + Quotes.escape(oldWindowTitleAgain) + "!");
        }

        // TODO: Validate Window Title and Count. (using visual inspection now)

        LOGGER.debug("openCloseWindow() [END]");
    }

    //////////////////// After ////////////////////
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
        else if(BROWSER_TYPE instanceof BrowserVersion) {
            newDriver = new WebDriverWrapper((BrowserVersion) BROWSER_TYPE);
        }
        else {
            throw new RuntimeException("Unknown Browser Type: " + BROWSER_TYPE + "!");
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
