/*
 * Created on 2019-04-02 by Brandon Dudek &lt;bdudek@paychex.com&gt; for {oac}.
 */
package xyz.swatt.tests.selenium.temp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.ITestContext;
import org.testng.TestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import xyz.swatt.log.LogMethods;
import xyz.swatt.selenium.WebDriverWrapper;
import xyz.swatt.selenium.WebElementWrapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Will run Performance Tests against the Login functionality.
 *
 * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
 */
@LogMethods
public class LogInPerfTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final boolean HEADLESS = false;
    private static final int MAX_TOTAL_INSTANCES_TO_TILE = 20;
    private static final int TOTAL_INSTANCES = 20;

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(LogInPerfTests.class);

    //========================= Static Variables ===============================
    private static final ConcurrentSkipListSet<WebDriverWrapper> drivers = new ConcurrentSkipListSet<>();

    //========================= Static Constructor =============================
    static {
        WebDriverWrapper.maxPageLoadTime = Duration.ofSeconds(30);
        WebDriverWrapper.maxElementLoadTime = Duration.ofSeconds(5);
    }

    //========================= Static Methods =================================

    /**
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    @DataProvider(parallel = true)
    public static Iterator<Object[]> perfData(ITestContext _iTestContext) {

        //------------------------ Pre-Checks ----------------------------------
        WebDriverWrapper.killPreviousBrowserDriverProcesses();

        //------------------------ CONSTANTS -----------------------------------
        final List<String> workingUserNames = Arrays.asList("oactest474", "oactest633", "oactest668", "oactest862", "oactest950", "oactest1242", "oactest1561");

        //------------------------ Variables -----------------------------------
        Runnable runnable = () -> drivers.add(new WebDriverWrapper(WebDriverWrapper.ChromeBrowser.CHROME, HEADLESS));
        List<Object[]> data = new LinkedList<>();

        //------------------------ Code ----------------------------------------
        for(int i = 1; i <= TOTAL_INSTANCES; i++) {
            new Thread(runnable).start();
        }

        while(drivers.size() < TOTAL_INSTANCES) {
            try {
                LOGGER.error("Size: " + drivers.size());
                Thread.sleep(Duration.ofSeconds(1).toMillis());
            }
            catch(InterruptedException e) {
                // Do nothing;
            }
        }

        if(TOTAL_INSTANCES <= MAX_TOTAL_INSTANCES_TO_TILE) {
            WebDriverWrapper.tileWindows(false);
        }

        int i = 0;
        for(WebDriverWrapper driver : drivers) {
            data.add(new Object[]{++i, driver, workingUserNames.get(i % 7)});
        }

        return data.iterator();
    }

    //========================= CONSTANTS ======================================
    final int RUN_NUM;
    final String USERNAME;

    //========================= Variables ======================================
    private boolean allPassed = false;
    private WebDriverWrapper driver;

    //========================= Constructors ===================================

    /**
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    @Factory(dataProvider = "perfData")
    public LogInPerfTests(int _runNum, WebDriverWrapper _driver, String _userName) {

        //------------------------ Pre-Checks ----------------------------------

        //-------------------------CONSTANTS------------------------------------

        //-------------------------Variables------------------------------------

        //-------------------------Code-----------------------------------------
        RUN_NUM = _runNum;
        driver = _driver;
        USERNAME = _userName;
    }

    //========================= Public Methods =================================
    //-------------------- Global DataProviders --------------------

    /**
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    @DataProvider
    public Object[][] globalArguments(ITestContext _iTestContext) {
        return new Object[][]{
                {RUN_NUM},
        };
    }

    //-------------------- Tests --------------------

    /**
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    @Test(dataProvider = "globalArguments")
    public void logInTests(int _runNum) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        try {
            driver.goToUrl("https://paychexoaacn2a-paychexoaac.analytics.ocp.oraclecloud.com/analytics/saw.dll?bieehome");
            driver.waitForTitle("Identity Cloud Service");

            driver.getWebElementWrapper(By.cssSelector("#idcs-signin-idp-signin-form .oj-flex-item.idcs-signin-idp-signin-form-idp-name-container  a"),
                    true, "Could not find SSO Login link!").click(true);
            driver.waitForTitle("Paychex employee services");

            if(true) {
                driver.getWebElementWrapper(By.id("USERNAME"), true, "Could not find the Username input!")
                        .sendKeys(USERNAME /*"oactest" + RUN_NUM*/);
                driver.getWebElementWrapper(By.id("PASSWORD"), true, "Could not find the Password input!")
                        .sendKeys("OIJrbbz6jNAkg3cR");
            }
            driver.getWebElementWrapper(By.cssSelector("button[type=submit]"), true, "Could not find the \"Sign in\" button!")
                    .click();

            ///// Validate /////
            driver.getWebElementWrapper(By.cssSelector("#logout, #unauthorizedError"), (HEADLESS || TOTAL_INSTANCES > MAX_TOTAL_INSTANCES_TO_TILE), WebDriverWrapper.maxPageLoadTime,
                    "OAC failed to load after login!");

            allPassed = true;
        }
        catch(Throwable e) {
            throw new TestException("Screenshot: " + driver.takeScreenshot().getAbsolutePath(), e);
        }
        finally {
            if(allPassed || HEADLESS) {

                driver.quit();

                if(!HEADLESS && TOTAL_INSTANCES <= MAX_TOTAL_INSTANCES_TO_TILE) {
                    WebDriverWrapper.tileWindows(false);
                }

                driver = null; // Free up memory.

                // Could call Garbage Collection here if needed.
            }
        }
    }

    /**
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    //@Test(dataProvider = "globalArguments")
    public void googleTest(int _runNum) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        final String SEARCH_TERM = "Cheese Steak Jimmy's";

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        try {
            driver.goToUrl("http://google.com/");
            driver.waitForTitle("Google");

            WebElementWrapper input = driver.getWebElementWrapper(By.cssSelector("input[name=q]"), true,
                    "Could not find Google's search input element!");
            input.sendKeys(SEARCH_TERM + Keys.ESCAPE); // [ESC] closes suggestions.

            driver.getWebElementWrapper(By.cssSelector("#tsf input[aria-label='Google Search']"), true,
                    "Cannot find the \"Google Search\" button!").click(true);

            ///// Validate /////
            driver.getWebElementWrapper(By.id("resultStats"), true, WebDriverWrapper.maxPageLoadTime,
                    "Search Results failed to load!");
            driver.waitForTitle(SEARCH_TERM + " - Google Search");

            driver.getWebElementWrapper(By.id("resultStats"), true, WebDriverWrapper.maxPageLoadTime, "Search Results failed to load!");

            allPassed = true;
        }
        catch(Throwable e) {
            throw new TestException("Screenshot: " + driver.takeScreenshot().getAbsolutePath(), e);
        }
        finally {
            if(allPassed || HEADLESS) {

                driver.quit();

                if(!HEADLESS && TOTAL_INSTANCES <= MAX_TOTAL_INSTANCES_TO_TILE) {
                    WebDriverWrapper.tileWindows(false);
                }

                driver = null; // Free up memory.

                // Could call Garbage Collection here if needed.
            }
        }
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
