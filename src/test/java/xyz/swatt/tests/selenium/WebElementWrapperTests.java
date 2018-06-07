package xyz.swatt.tests.selenium;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import xyz.swatt.selenium.WebDriverWrapper;
import xyz.swatt.selenium.WebElementWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Quotes;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Objects;

/**
 * This class will test the {@link WebElementWrapper} class.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebElementWrapperTests {

    //========================= STATIC CONSTANTS ===============================
    private static final WebDriverWrapper DRIVER = new WebDriverWrapper(BrowserVersion.FIREFOX_52);
    private static final Logger LOGGER = LogManager.getLogger(WebElementWrapperTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {
        DRIVER.goToUrl("file:" + new File("src/test/resources/Selenium Test Files/Test Web Page.html").getAbsolutePath());
    }

    //========================= Static Methods =================================
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public static Object[][] getAttributeTestsData() {

        return new Object[][] {
                {"Has Attribute", "#visible1", "visible", "true"},
                {"Does NOT have Attribute", "#visible1", "walla", null},
        };
    }
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "getAttributeTestsData")
    public static void getAttributeTests(String _testName, String _cssSelector, String _attribute, String _value) {

        LOGGER.info("getAttributeTests(_testName: {}, _cssSelector: {}, _attribute: {}, _value: {}) [START]",
                _testName, _cssSelector, _attribute, _value);

        //------------------------ Parameter Checks ----------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String attributeValue;
        WebElementWrapper webElementWrapper = DRIVER.getWebElementWrapper(By.cssSelector(_cssSelector));

        //------------------------ Code ----------------------------------------
        attributeValue = webElementWrapper.getAttribute(_attribute);
        if(!Objects.equals(attributeValue, _value)) {
            throw new RuntimeException("@" + _attribute + " attribute does not equal " + Quotes.escape(_value)
                    + "!\nInstead it is " + Quotes.escape(attributeValue) + ".");
        }

        LOGGER.debug("getAttributeTests(_testName: {}, _cssSelector: {}, _attribute: {}, _value: {}) [END]",
                _testName, _cssSelector, _attribute, _value);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public static Object[][] WebElementToStringToXpathData() {

        return new Object[][] {

                ////////// General Rules //////////
                {"Element Name", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p]", //
                        "//p"}, //

                {"Child", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p > p]", //
                        "//p/p"}, //

                {"Child (no space)", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p>p]", //
                        "//p/p"}, //

                {"Descendant", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p p]", //
                        "//p//p"}, //

                {"ID", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: #id1]", //
                        "//*[@id='id1']"}, //

                {"ID after Element Name", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p#id1]", //
                        "//p[@id='id1']"}, //

                {"Class", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: .class1]", //
                        "//*[contains(concat(' ',normalize-space(@class),' '),' class1 ')]"}, //

                {"Class after Element Name", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p.class1]", //
                        "//p[contains(concat(' ',normalize-space(@class),' '),' class1 ')]"}, //

                {"Child Selector after Element Name", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p:nth-child(2)]", //
                        "//p[2]"}, //

                {"Child Selector after Element Name and before Class", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p:nth-child(2).class1]", //
                        "//p[2][contains(concat(' ',normalize-space(@class),' '),' class1 ')]"}, //

                {"Child Selector after Class after Element Name", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: p.class1:nth-child(2)]", //
                        "//p[2][contains(concat(' ',normalize-space(@class),' '),' class1 ')]"}, //

                {"Child Selector after Class", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: .class1:nth-child(2)]", //
                        "//*[2][contains(concat(' ',normalize-space(@class),' '),' class1 ')]"}, //

                {"Child Selector after Class after Class", "[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: .class1.class2:nth-child(2)]", //
                        "//*[2][contains(concat(' ',normalize-space(@class),' '),' class1 ')][contains(concat(' ',normalize-space(@class),' '),' class2 ')]"}, //

                ////////// Specific Examples //////////
                {"example1", "[[[[[[FirefoxDriver: firefox on WINDOWS (87401bc9-3074-453a-9bf8-9fde4a708087)] -> css selector: html > body > div > div > div > div > div#doclist > div#templates-tabDatatable_wrapper > div > div > div > table#templates-tabDatatable > tbody > tr#rowid_63486]] -> id: fw_publish_status_63486]] -> xpath: ..]",
                        "//html/body/div/div/div/div/div[@id='doclist']/div[@id='templates-tabDatatable_wrapper']/div/div/div/table[@id='templates-tabDatatable']/tbody/tr[@id='rowid_63486']//*[@id='fw_publish_status_63486']/.."}, //

                {"example2", "[[[[FirefoxDriver: firefox on WINDOWS (be5480ca-20da-455b-88f9-0c3f9aa54088)] -> xpath: //*[@id='documents-tabDatatable_wrapper' or @id='templates-tabDatatable_wrapper']//table[ @id='documents-tabDatatable' or @id='templates-tabDatatable' ]/tbody/tr[ td[4][normalize-space()=\"Melrose Industries PLC disposal of its Elster business\"] ]]] -> css selector:  .doc-statuses div]",
                        "//*[@id='documents-tabDatatable_wrapper' or @id='templates-tabDatatable_wrapper']//table[ @id='documents-tabDatatable' or @id='templates-tabDatatable' ]/tbody/tr[ td[4][normalize-space()=\"Melrose Industries PLC disposal of its Elster business\"] ]//*[contains(concat(' ',normalize-space(@class),' '),' doc-statuses ')]//div"}, //

                {"example3", "[[[[ChromeDriver: chrome on XP (7c010e3b6a9171d75f1ee1e5be337041)] -> css selector: .docket-table tbody]] -> css selector: tr.docket-row:nth-child(2)]",
                        "//*[contains(concat(' ',normalize-space(@class),' '),' docket-table ')]//tbody//tr[2][contains(concat(' ',normalize-space(@class),' '),' docket-row ')]"}, //
        };
    }
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "WebElementToStringToXpathData")
    public static void WebElementToStringToXpathTests(String _testName, String _webElementToString, String _expectedXpath) {

        LOGGER.info("WebElementToStringToXpathTests(_testName: {}, _webElementToString: {}, _expectedXpath: {}) [START]",
                _testName, _webElementToString, _expectedXpath);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String xpathOutput = WebElementWrapper.webElementToStringToXpath(_webElementToString);

        //------------------------ Code ----------------------------------------
        if(!xpathOutput.equals(_expectedXpath)) {
            throw new RuntimeException("Error in Outputted XPath!\n\t Expecting: " + _expectedXpath + "\n\t But Found: " + xpathOutput);
        }

        LOGGER.debug("WebElementToStringToXpathTests(_testName: {}, _webElementToString: {}, _expectedXpath: {}) [END]",
                _testName, _webElementToString, _expectedXpath);
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test
    public static void descendantSearchWithGlobalXpathTest() {

        LOGGER.info("descendantSearchWithGlobalXpathTest() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        WebElementWrapper wrapper = DRIVER.getWebElementWrapper(By.id("parent"));
        java.util.List<WebElementWrapper> class1Elements = wrapper.getDescendants(By.xpath("//p"));
        if(class1Elements.size() != 1) {
            throw new RuntimeException("Only 1 child expected but " + class1Elements.size() + " were found!");
        }

        LOGGER.debug("descendantSearchWithGlobalXpathTest() [END]");
    }

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods ========================================

    //========================= Classes ========================================
}
