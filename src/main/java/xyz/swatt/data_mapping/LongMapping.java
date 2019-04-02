/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

/**
 * <p>
 * Use this class to validate that 2 different {@link Long}s, {@link Integer}s, or {@link Short}s are equivalent.
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class LongMapping implements DataMapping {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(LongMapping.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    /**
     * The name that was given to this mapping.
     * <p>
     * <i>Note:</i> This name is optional and may be {@code null}.
     * </p>
     */
    public final String MAPPING_NAME;

    /**
     * The values being compared.
     */
    public final Long SOURCE_VALUE, DESTINATION_VALUE;

    //========================= Variables ======================================

    //========================= Constructors ===================================

    /**
     * Creates a new {@link long}-to-{@link long} {@link DataMapping} object.
     *
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMapping(long _sourceValue, long _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }

    /**
     * Creates a new {@link long}-to-{@link long} {@link DataMapping} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link LongMapping}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMapping(String _mappingName, long _sourceValue, long _destinationValue) {
        this(_mappingName, new Long(_sourceValue), new Long(_destinationValue));
    }

    /**
     * Creates a new {@link Long}-to-{@link Long} {@link DataMapping} object.
     *
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMapping(Long _sourceValue, Long _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }

    /**
     * Creates a new {@link Long}-to-{@link Long} {@link DataMapping} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link LongMapping}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMapping(String _mappingName, Long _sourceValue, Long _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        MAPPING_NAME = _mappingName == null || StringHelper.removeWhitespace(_mappingName).isEmpty() ? null : StringHelper.trim(_mappingName);
        SOURCE_VALUE = _sourceValue;
        DESTINATION_VALUE = _destinationValue;
    }

    //========================= Public Methods =================================
    @Override
    @SuppressWarnings("Duplicates")
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMapping.createFormattedErrorString(SOURCE_VALUE, DESTINATION_VALUE);

        //------------------------ Variables -----------------------------------
        Long source = SOURCE_VALUE, destination = DESTINATION_VALUE; // Not work with class variables, so that `validated()` can be called, idempotently.

        //------------------------ Code ----------------------------------------
        ////////// Handle NULLs /////
        if(source == null || destination == null) {
            if(source == destination) {
                return null; // Equal.
            }
            else {
                return ERROR_MESSAGE;
            }
        }

        ////// Compare /////
        boolean isEqual = source.equals(destination);

        ////// Return /////
        if(isEqual) {
            return null;
        }
        else {
            return ERROR_MESSAGE;
        }
    }

    @Override
    public String toString() {
        return MAPPING_NAME != null ? MAPPING_NAME : super.toString();
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
