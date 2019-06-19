/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>
 * Use this class to validate that 2 different {@link BigDecimal}s, {@link Double}s, or {@link Float}s are equivalent.
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class BigDecimalMapping implements DataMapping {

    //========================= Static Enums ===================================

    /**
     * <p>
     * List of possible flags for a Data Mapping.
     * </p>
     * <p>
     * <i>Note:</i> All flags are {@code false} by default.
     * </p>
     * <p>
     * <i>Note:</i> Flags can be globally turned on by using the {@link #GLOBAL_MAPPING_FLAGS} collection.
     * </p>
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public static enum MappingFlag implements DataMappingFlagEnum {

        /**
         * Will ignoring trailing 0s after the decimal place.
         * <p><i>(By default, BigDecimal takes precision into account.)</i></p>
         */
        IGNORE_PRECISION
    }

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(BigDecimalMapping.class);

    /**
     * Sets {@link MappingFlag}s to be used for all {@link BigDecimalMapping}s.
     */
    public static final EnumSet<MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(MappingFlag.class);

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
    public final BigDecimal SOURCE_VALUE, DESTINATION_VALUE;

    /**
     * The {@link MappingFlag}s that are applied to this {@link BigDecimalMapping} object.
     */
    public final Set<MappingFlag> MAPPING_FLAGS;

    //========================= Variables ======================================

    //========================= Constructors ===================================

    /**
     * Creates a new {@link BigDecimal}-to-{@link BigDecimal} {@link DataMapping} object.
     *
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link BigDecimalMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public BigDecimalMapping(BigDecimal _sourceValue, BigDecimal _destinationValue, MappingFlag... _flags) {
        this(null, _sourceValue, _destinationValue, _flags);
    }

    /**
     * Creates a new {@link BigDecimal}-to-{@link BigDecimal} {@link DataMapping} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link BigDecimalMapping}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link BigDecimalMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public BigDecimalMapping(String _mappingName, BigDecimal _sourceValue, BigDecimal _destinationValue, MappingFlag... _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        EnumSet<MappingFlag> flags = EnumSet.noneOf(MappingFlag.class);

        //------------------------ Code ----------------------------------------
        MAPPING_NAME = _mappingName == null || StringHelper.removeWhitespace(_mappingName).isEmpty() ? null : StringHelper.trim(_mappingName);
        SOURCE_VALUE = _sourceValue;
        DESTINATION_VALUE = _destinationValue;

        flags.addAll(GLOBAL_MAPPING_FLAGS);
        if(_flags != null && _flags.length > 0) {
            flags.addAll(Arrays.asList(_flags));
        }
        MAPPING_FLAGS = Collections.unmodifiableSet(flags);
    }

    //========================= Public Methods =================================
    @Override
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMapping.createFormattedErrorString(SOURCE_VALUE, DESTINATION_VALUE);

        //------------------------ Variables -----------------------------------
        BigDecimal source = SOURCE_VALUE, destination = DESTINATION_VALUE; // Not work with class variables, so that `validated()` can be called, idempotently.

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

        ////// Handle Flags /////

        ////// Compare /////
        boolean isEqual;
        if(MAPPING_FLAGS.contains(MappingFlag.IGNORE_PRECISION)) {
            isEqual = source.compareTo(destination) == 0;
        }
        else {
            isEqual = source.equals(destination);
        }

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
