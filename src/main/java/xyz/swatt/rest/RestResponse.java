package xyz.swatt.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

/**
 * This class contains all of the data returned from a RESTful service request.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class RestResponse {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(RestResponse.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {
    }

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    public final int STATUS_CODE;
    public final String BODY;
    public final Map<String, String> HEADERS;

    //========================= Variables ======================================

    //========================= Constructors ===================================
    /**
     * Will create a {@link RestResponse} and load the response data to it.
     *
     * @param _statusCode The Response Code that was returned.
     * @param _headers The Response Header Key/Value pairs that were returned.
     * @param _body The Body of the Response message.
     *
     * @throws IllegalArgumentException If the Response Code is invalid (&lt;100 or &gt;599).
     * <p>
     *     If the given Response Headers is {@code null} or empty.
     * </p>
     * <p>
     *     If the given Response Body is {@code null}. (If the Response Body is empty, use an empty String.)
     * </p>
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public RestResponse(int _statusCode, Map<String, String> _headers, String _body) {

        super();

        LOGGER.info("RestResponse(_statusCode: {}, _headers: ({}), _body: {}) [START]", _statusCode, _headers == null ? "NULL" : _headers.size(), _body);

        //------------------------ Pre-Checks ----------------------------------
        if(_statusCode < 100 || _statusCode > 599) {
            throw new IllegalArgumentException("Given Response Code is invalid! (" + _statusCode + ")\n\tMust be >= 100 and < 600.");
        }

        if(_headers == null || _headers.isEmpty()) {
            throw new IllegalArgumentException("No Response Headers were given! (Headers are required.)");
        }

        if(_body == null) {
            throw new IllegalArgumentException("Given Response Body cannot be NULL!\n\t(If the Response Body is empty, use an empty String.)");
        }

        //-------------------------CONSTANTS------------------------------------

        //-------------------------Variables------------------------------------

        //-------------------------Code-----------------------------------------
        STATUS_CODE = _statusCode;
        HEADERS = _headers;
        BODY = _body;

        LOGGER.debug("RestResponse(_statusCode: {}, _headers: ({}), _body: {}) [END]", _statusCode, _headers.size(), _body);
    }

    //========================= Methods ========================================
    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        RestResponse that = (RestResponse) o;
        return STATUS_CODE == that.STATUS_CODE &&
                Objects.equals(BODY, that.BODY) &&
                Objects.equals(HEADERS, that.HEADERS);
    }

    /**
     * @return The Status Code of the Response.
     */
    public int getStatusCode() {
        return STATUS_CODE;
    }

    /**
     * @return The Body of the Response as a String.
     */
    public String getBody() {
        return BODY;
    }

    /**
     * @return The Headers that were returned with the Response.
     */
    public Map<String, String> getHeaders() {
        return HEADERS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(STATUS_CODE, BODY, HEADERS);
    }

    @Override
    public String toString() {
        return STATUS_CODE + ": " + BODY;
    }

    //========================= Classes ========================================
}
