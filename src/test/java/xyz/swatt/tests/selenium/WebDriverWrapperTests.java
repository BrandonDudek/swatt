package xyz.swatt.tests.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Quotes;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import xyz.swatt.selenium.WebDriverWrapper;
import xyz.swatt.selenium.WebElementWrapper;
import xyz.swatt.xml.XmlDocumentHelper;

import java.io.File;
import java.util.List;

/**
 * This class will test the {@link WebDriverWrapper} class.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebDriverWrapperTests {

    //========================= STATIC CONSTANTS ===============================
    private static final long MIN_ELEMENT_WAIT_TIME_MS = WebDriverWrapper.maxElementLoadTime.toMillis(); // s to ms.

    // Processing time for visibility checks.
    private static final long MAX_ELEMENT_WAIT_TIME_MS = MIN_ELEMENT_WAIT_TIME_MS + WebDriverWrapper.POLLING_INTERVAL.toMillis() + 400;

    private static final Document HTML_AS_XML;
    private static final WebDriverWrapper DRIVER = new WebDriverWrapper(WebDriverWrapper.ChromeBrowser.CHROME, true);
    private static final Logger LOGGER = LogManager.getLogger(WebDriverWrapperTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {
        File htmlFile = new File("src/test/resources/Selenium Test Files/Test Web Page.html");

        HTML_AS_XML = XmlDocumentHelper.getDocumentFrom(htmlFile);

        DRIVER.goToUrl("file:" + htmlFile.getAbsolutePath());
    }

    //========================= Static Methods =================================
    
    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods ========================================
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test
    public void getOneVisibleElementWhenThereAreAlsoHiddenOnes() {

        LOGGER.info("getOneVisibleElementWhenThereAreAlsoHiddenOnes() [START]");

        //------------------------ Parameter Checks ----------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        long startTime, endTime, timeTakenInMs;

        WebElementWrapper webElementWrapper;

        //------------------------ Code ----------------------------------------
        ///// Action /////
        startTime = System.currentTimeMillis();
        webElementWrapper = DRIVER.getWebElementWrapper(By.cssSelector("p.test1visible"), true, WebDriverWrapper.maxElementLoadTime);
        endTime = System.currentTimeMillis();

        ///// Validate /////
        if(webElementWrapper == null) {
            throw new RuntimeException("Unable to get WebElementWrapper!");
        }

        timeTakenInMs = endTime - startTime;
        if(timeTakenInMs >= WebDriverWrapper.maxElementLoadTime.toMillis() * 1000) {

            throw new RuntimeException("WebDriverHelper.getWebElementWrapper(WebDriver _driver, By _by) took too long to get the visible Web Element!"
                    + "\n\tExpected Time < " + (WebDriverWrapper.maxElementLoadTime.toMillis() * 1000) + " ms.\n\tActual Time: " + timeTakenInMs + " ms.");
        }

        LOGGER.debug("getOneVisibleElementWhenThereAreAlsoHiddenOnes() {{} ms} [END]", timeTakenInMs);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test
    public void waitForExistence() {

        LOGGER.info("waitForExistence() [START]");

        //------------------------ Parameter Checks ----------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        long startTime, endTime, timeTakenInMs;

        String cssSelector = "xys";
        WebElementWrapper webElementWrapper;

        //------------------------ Code ----------------------------------------
        ///// Action /////
        startTime = System.currentTimeMillis();
        webElementWrapper = DRIVER.getWebElementWrapper(By.cssSelector(cssSelector));
        endTime = System.currentTimeMillis();

        ///// Validate /////
        if(webElementWrapper != null) {
            throw new RuntimeException("Somehow managed to get non-Existent Element" + Quotes.escape(cssSelector) + "!");
        }

        timeTakenInMs = endTime - startTime;
        if(timeTakenInMs < MIN_ELEMENT_WAIT_TIME_MS) {
            throw new RuntimeException("Did not wait long enough for Web Element to exist!\n\tExpected Time >= " + MIN_ELEMENT_WAIT_TIME_MS
                    + " ms.\n\tActual Time: " + timeTakenInMs + " ms.");
        }
        if(timeTakenInMs > MAX_ELEMENT_WAIT_TIME_MS) {
            throw new RuntimeException("Waited too long for Web Element to exist!\n\tMax Wait Time: " + MIN_ELEMENT_WAIT_TIME_MS
                    + " ms.\n\tActual Time: " + timeTakenInMs + " ms.");
        }

        LOGGER.debug("waitForExistence() - ({} ms) - [END]", timeTakenInMs);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] waitForVisibilityDataProvider() {
        return new Object[][]{
                {"All Hidden", "#waitForVisibilityTest #hiddenp1", 0},
                {"Some Hidden", "#waitForVisibilityTest p",
                        XmlDocumentHelper.getNodesForXPath(HTML_AS_XML, "/html/body//*[@id='waitForVisibilityTest']//p[@visible='true']").size()},
                
        };
    }
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "waitForVisibilityDataProvider")
    public void waitForVisibility(String _name, String _cssSelector, Integer _numShouldBeFound) {

        LOGGER.info("waitForVisibility(_name: {}, _cssSelector: {}, _numShouldBeFound: {}) [START]", _name, _cssSelector, _numShouldBeFound);

        //------------------------ Parameter Checks ----------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        long startTime, endTime, timeTakenInMs;

        List<WebElementWrapper> webElementWrappers;

        //------------------------ Code ----------------------------------------
        ///// Action /////
        startTime = System.currentTimeMillis();
        webElementWrappers = DRIVER.getWebElementWrappers(By.cssSelector(_cssSelector), true, WebDriverWrapper.maxElementLoadTime);
        endTime = System.currentTimeMillis();

        ///// Validate /////
        if(webElementWrappers.size() != _numShouldBeFound) {
            throw new RuntimeException("Found " + webElementWrappers.size() + " Elements when only " + _numShouldBeFound + " are Visible!" );
        }

        timeTakenInMs = endTime - startTime;
        if(timeTakenInMs > MAX_ELEMENT_WAIT_TIME_MS) {
            throw new RuntimeException("Waited too long for Web Element to become visible!\n\tMax Wait Time: " + MIN_ELEMENT_WAIT_TIME_MS
                    + " ms.\n\tActual Time: " + timeTakenInMs + " ms.");
        }

        LOGGER.debug("waitForVisibility(_name: {}, _cssSelector: {}, _numShouldBeFound: {}) {{} ms} [END]", _name, _cssSelector, _numShouldBeFound, timeTakenInMs);
    }

    //========================= Classes ========================================
}
