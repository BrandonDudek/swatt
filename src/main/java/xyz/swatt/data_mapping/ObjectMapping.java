/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

/**
 * Use this class to validate that 2 different {@link T}s are equal.
 * <p>
 * (This will just use the {@link T}'s {@code equals(T)} method, and is the fallback class for the {@link DataMapping} interface.)
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class ObjectMapping<T> implements DataMapping {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(ObjectMapping.class);

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
    public final T SOURCE_VALUE, DESTINATION_VALUE;

    //========================= Variables ======================================

    //========================= Constructors ===================================

    /**
     * Creates a new {@link T}-to-{@link T} {@link DataMapping} object.
     *
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public ObjectMapping(T _sourceValue, T _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }

    /**
     * Creates a new {@link T}-to-{@link T} {@link DataMapping} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link ObjectMapping}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public ObjectMapping(String _mappingName, T _sourceValue, T _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        MAPPING_NAME = _mappingName == null || StringHelper.removeWhitespace(_mappingName).isEmpty() ? null : StringHelper.trim(_mappingName);
        SOURCE_VALUE = _sourceValue;
        DESTINATION_VALUE = _destinationValue;
    }

    //========================= Public Methods =================================
    
    /**
     * @return The Set or Generated Name of this Mapping; or {@code null}, if not set.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Override
    public String getMappingName() {
        return MAPPING_NAME;
    }
    
    @Override
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMapping.createFormattedErrorString(SOURCE_VALUE, DESTINATION_VALUE);

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        ////////// Handle NULLs /////
        if(SOURCE_VALUE == null || DESTINATION_VALUE == null) {
            if(SOURCE_VALUE == DESTINATION_VALUE) {
                return null; // Equal.
            }
            else {
                return ERROR_MESSAGE;
            }
        }

        ////// Compare /////
        boolean isEqual = SOURCE_VALUE.equals(DESTINATION_VALUE);

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
