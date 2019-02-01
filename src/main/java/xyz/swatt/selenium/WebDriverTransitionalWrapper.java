package xyz.swatt.selenium;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.openqa.selenium.WebDriver;

/**
 * This Class extends {@link WebDriverWrapper} and contains all of it's functionality.
 * <p>
 *     The only difference is that this class has the {@link #getWebDriver()} method.
 * </p>
 * <p>
 *     The purpose of this class is to be used <b><i>temporarily</i></b>, while transitioning an existing project to SWATT.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebDriverTransitionalWrapper extends WebDriverWrapper {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {
    }

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================
    public WebDriverTransitionalWrapper(ChromeBrowser _browser) {
        super(_browser);
    }

    public WebDriverTransitionalWrapper(FirefoxBrowser _browser) {
        super(_browser);
    }

    public WebDriverTransitionalWrapper(IEBrowser _browser) {
        super(_browser);
    }

    public WebDriverTransitionalWrapper(BrowserVersion _browser) {
        super(_browser);
    }

    //========================= Methods ========================================
    /**
     * @return A reference to the {@link WebDriver} that this wrapper is based on.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public WebDriver getWebDriver() {
        return DRIVER;
    }

    //========================= Classes ========================================
}
