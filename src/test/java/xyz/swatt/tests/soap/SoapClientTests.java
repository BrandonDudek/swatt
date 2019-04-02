/*
 * Created on 2019-01-17 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.tests.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import xyz.swatt.soap.SoapClient;
import xyz.swatt.xml.XmlDocumentHelper;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 *
 */
public class SoapClientTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(SoapClientTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static { }

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods for External Use =======================
    /*
     * https://jansipke.nl/examples-of-public-soap-web-services
     * http://soapclient.com/SoapServices.html
     */

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] soapActions() {
        return new Object[][]{
                {"http://tempuri.org/Add", "<Add xmlns=\"http://tempuri.org/\"><intA>1</intA><intB>2</intB></Add>", "3"},
                {"http://tempuri.org/Subtract", "<Subtract xmlns=\"http://tempuri.org/\"><intA>3</intA><intB>2</intB></Subtract>", "1"},
        };
    }
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "soapActions")
    public void soapActionTest(String _action, String _xmlMessage, String _expectedResponse) throws SOAPException {

        LOGGER.info("soapActionTest(_action: {}, _xmlMessage: {}, _expectedResponse: {}) [START]", _action, _xmlMessage, _expectedResponse);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        SoapClient soapClient = new SoapClient("http://www.dneonline.com/calculator.asmx");

        //------------------------ Code ----------------------------------------
        SOAPMessage response = soapClient.sendMessage(_action, _xmlMessage);

        Document responseXml = response.getSOAPBody().extractContentAsDocument();
        LOGGER.info(XmlDocumentHelper.toString(responseXml));

        String result = XmlDocumentHelper.getStringForXPath(responseXml, "/*");
        Assert.assertEquals(result, _expectedResponse, _action);

        LOGGER.debug("soapActionTest(_action: {}, _xmlMessage: {}, _expectedResponse: {}) [END]", _action, _xmlMessage, _expectedResponse);
    }

    //========================= Methods for Internal Use =======================

    //========================= Classes ========================================
}
