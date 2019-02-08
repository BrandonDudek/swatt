package xyz.swatt.soap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.xml.XmlDocumentHelper;

import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * Use this client to send Soap messages to an API.
 * <p>
 * <i>Note:</i> This class uses SOAP version 1.1 protocols.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class SoapClient {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(SoapClient.class);

    //========================= Static Variables ===============================
    public static String soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    public final String URL, USER;
    private final String PASS;

    private final MessageFactory MESSAGE_FACTORY;

    //========================= Variables ======================================

    //========================= Constructors ===================================

    /**
     * Creates this Soap client for use against a given endpoint.
     *
     * @param _url
     *         The full URL of the API endpoint that this client will be hitting.
     *
     * @throws IllegalArgumentException
     *         If thee given URL endpoint is not in a valid URL format.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public SoapClient(String _url) {
        this(_url, null, null);
    }

    /**
     * Creates this Soap client for use against a given endpoint, with basic authentication.
     *
     * @param _url
     *         The full URL of the API endpoint that this client will be hitting.
     * @param _user
     *         The username to use for authentication, or {@code null} to ignore.
     * @param _pass
     *         The password to use for authentication, or {@code null} to ignore.
     *
     * @throws IllegalArgumentException
     *         If the given URL endpoint is not in a valid URL format.
     * @throws IllegalArgumentException
     *         If either the given Username or Password are {@code null} but not both.
     * @throws IllegalArgumentException
     *         If either the given Username or Password are blank strings.
     * @throws RuntimeException
     *         If a {@link MessageFactory} could not be created.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public SoapClient(String _url, String _user, String _pass) {

        super();

        LOGGER.info("SoapClient(String _url) [START]");

        //------------------------ Pre-Checks ----------------------------------
        ArgumentChecks.urlIsValid(_url, "Endpoint");

        if(_user == null || _pass == null) {
            //noinspection StringEquality
            if(_user != _pass) {
                throw new IllegalArgumentException("Either both Username and Password have to be NULL or neither!");
            }
        }
        else {
            ArgumentChecks.stringNotWhitespaceOnly(_user, "Username");
            ArgumentChecks.stringNotWhitespaceOnly(_pass, "Password");
        }

        //-------------------------CONSTANTS------------------------------------

        //-------------------------Variables------------------------------------

        //-------------------------Code-----------------------------------------
        URL = _url;
        USER = _user == null ? null : _user.trim();
        PASS = _pass == null ? null : _pass.trim();

        try { // Construct a default SOAP message factory.
            MESSAGE_FACTORY = MessageFactory.newInstance(soapProtocol);
        }
        catch(SOAPException e) {
            throw new RuntimeException("Could not create a SOAP Message Factory!", e);
        }

        LOGGER.debug("SoapClient(String _url) [END]");
    }

    //========================= Methods for External Use =======================

    /**
     * Will send a single SOAP Message with the given body.
     *
     * @param _action
     *         The SOAPAction that this message is requesting be performed, or {@code null} to not send a "SOAPAction".
     * @param _xml
     *         The XML {@link Document} to send as the body in a SOAP envelope.
     *
     * @return The response as a {@link SOAPMessage}.
     *
     * @throws IllegalArgumentException
     *         If the given SOAPAction is a blank string.
     * @throws IllegalArgumentException
     *         If the given XML {@link Document} is {@code null}.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public SOAPMessage sendMessage(String _action, Document _xml) {

        LOGGER.info("sendMessage(_action: {}, Document) [START]", _action);

        //------------------------ Pre-Checks ----------------------------------
        if(_action != null) {
            ArgumentChecks.stringNotWhitespaceOnly(_action, "SOAPAction");
        }

        ArgumentChecks.notNull(_xml, "XML Document");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        SOAPMessage soapMessage, soapResponse;
        SOAPConnection soapConnection = null;

        //------------------------ Code ----------------------------------------
        try { // Create Message.
            soapMessage = MESSAGE_FACTORY.createMessage();

            MimeHeaders headers = soapMessage.getMimeHeaders();
            if(USER != null) {
                String credentials = USER + ":" + PASS;
                credentials = "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes()));
                headers.addHeader("Authorization", credentials);
            }
            if(_action != null) {
                soapMessage.getMimeHeaders().addHeader("SOAPAction", _action);
            }

            SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
            soapBody.addDocument((Document) _xml.cloneNode(true));

            soapMessage.saveChanges();

            if(LOGGER.isDebugEnabled()) {
                try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    soapMessage.writeTo(baos);
                    LOGGER.debug("SOAP Request Message: " + baos.toString());
                }
                catch(IOException e) {
                    LOGGER.warn(e);
                }
            }
        }
        catch(SOAPException e) {
            throw new RuntimeException("Could not create the SOAP message!", e);
        }

        try { // Send Connection.
            soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            soapResponse = soapConnection.call(soapMessage, URL);
        }
        catch(SOAPException e) {
            throw new RuntimeException("Could not send SOAP message!", e);
        }
        finally {
            if(soapConnection != null) {
                try {
                    soapConnection.close();
                }
                catch(SOAPException e) {
                    LOGGER.warn(e);
                }
            }
        }

        LOGGER.debug("sendMessage(_action: {}, Document) [END]: {}", _action, soapResponse);

        return soapResponse;
    }

    /**
     * Will send a single SOAP Message with the given body.
     *
     * @param _action
     *         The SOAPAction that this message is requesting be performed, or {@code null} to not send a "SOAPAction".
     * @param _xml
     *         The XML {@link String} to send as the body in a SOAP envelope.
     *
     * @return The response as a {@link SOAPMessage}.
     *
     * @throws IllegalArgumentException
     *         If the given SOAPAction is a blank string.
     * @throws IllegalArgumentException
     *         If the given XML {@link File} does not exist.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public SOAPMessage sendMessage(String _action, File _xml) {

        LOGGER.info("sendMessage(_action: {}, File) [START]", _action);

        //------------------------ Pre-Checks ----------------------------------
        // Actions validated in nested call.

        ArgumentChecks.fileExists(_xml, "XML File");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        SOAPMessage soapResponse;

        //------------------------ Code ----------------------------------------
        Document xmlDocument = XmlDocumentHelper.getDocumentFrom(_xml);
        soapResponse = sendMessage(_action, xmlDocument);

        LOGGER.debug("sendMessage(_action: {}, File) [END]: {}", _action, soapResponse);

        return soapResponse;
    }

    /**
     * Will send a single SOAP Message with the given body.
     *
     * @param _action
     *         The SOAPAction that this message is requesting be performed, or {@code null} to not send a "SOAPAction".
     * @param _xml
     *         The XML {@link String} to send as the body in a SOAP envelope.
     *
     * @return The response as a {@link SOAPMessage}.
     *
     * @throws IllegalArgumentException
     *         If the given SOAPAction is a blank string.
     * @throws IllegalArgumentException
     *         If the given XML {@link String} is blank.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public SOAPMessage sendMessage(String _action, String _xml) {

        LOGGER.info("sendMessage(_action: {}, String) [START]", _action);

        //------------------------ Pre-Checks ----------------------------------
        // Actions validated in nested call.

        ArgumentChecks.stringNotWhitespaceOnly(_xml, "XML String");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        SOAPMessage soapResponse;

        //------------------------ Code ----------------------------------------
        Document xmlDocument = XmlDocumentHelper.getDocumentFrom(_xml);
        soapResponse = sendMessage(_action, xmlDocument);

        LOGGER.debug("sendMessage(_action: {}, String) [END]: {}", _action, soapResponse);

        return soapResponse;
    }

    //========================= Methods for Internal Use =======================

    //========================= Classes ========================================
}
