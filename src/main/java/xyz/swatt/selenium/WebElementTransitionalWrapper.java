package xyz.swatt.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This Class extends {@link WebElementWrapper} and contains all of it's functionality.
 * <p>
 *     The only difference is that this class has the {@link #getWebElement()} method.
 * </p>
 * <p>
 *     The purpose of this class is to be used <b><i>temporarily</i></b>, while transitioning an existing project to SWATT.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class WebElementTransitionalWrapper extends WebElementWrapper {

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
    /**
     * Creates this WebElementWrapper for use with a give Selenium WebElement.
     *
     * @param _driver The WebDriverWrapper that created this object.
     * @param _webElement The WebElement to interact with.
     *
     * @throws IllegalArgumentException If either of the parameters are {@code null}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public WebElementTransitionalWrapper(WebDriverWrapper _driver, WebElement _webElement) {
        super(_driver, _webElement, null);
    }


    //========================= Methods ========================================
    /**
     * @return A reference to the {@link WebDriver} that this wrapper is based on.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public WebElement getWebElement() {
        return webElement;
    }

    //========================= Classes ========================================
}
