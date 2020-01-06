/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

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
public class BigDecimalMappingValidator extends AbstractDataMapping<BigDecimal> {
    
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
    private static final Logger LOGGER = LogManager.getLogger(BigDecimalMappingValidator.class);
    
    /**
     * Sets {@link MappingFlag}s to be used for all {@link BigDecimalMappingValidator}s.
     */
    public static final EnumSet<MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(MappingFlag.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    /**
     * The values being compared.
     */
    public final BigDecimal SOURCE_VALUE, DESTINATION_VALUE;
    
    /**
     * The {@link MappingFlag}s that are applied to this {@link BigDecimalMappingValidator} object.
     */
    public final Set<MappingFlag> MAPPING_FLAGS;

    //========================= Variables ======================================
    
    //========================= Constructors ===================================
    
    /**
     * Creates a new {@link BigDecimal}-to-{@link BigDecimal} {@link DataMappingValidator} object.
     *
     * @param _sourceValue
     * 		The value from the Source Data.
     * @param _destinationValue
     * 		The mapped value found in the Destination Data.
     * @param _flags
     * 		Any {@link MappingFlag}s that should be applied to this {@link BigDecimalMappingValidator}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public BigDecimalMappingValidator(BigDecimal _sourceValue, BigDecimal _destinationValue, MappingFlag... _flags) {
        this(null, _sourceValue, _destinationValue, _flags);
    }

    /**
     * Creates a new {@link BigDecimal}-to-{@link BigDecimal} {@link DataMappingValidator} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link BigDecimalMappingValidator}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link BigDecimalMappingValidator}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public BigDecimalMappingValidator(String _mappingName, BigDecimal _sourceValue, BigDecimal _destinationValue, MappingFlag... _flags) {
    
        //------------------------ Pre-Checks ----------------------------------
    
        //------------------------ CONSTANTS -----------------------------------
    
        //------------------------ Variables -----------------------------------
        EnumSet<MappingFlag> flags = EnumSet.noneOf(MappingFlag.class);
    
        //------------------------ Code ----------------------------------------
        setMappingName(_mappingName);
    
        SOURCE_VALUE = _sourceValue;
        DESTINATION_VALUE = _destinationValue;
    
        flags.addAll(GLOBAL_MAPPING_FLAGS);
        if(_flags != null && _flags.length > 0) {
            flags.addAll(Arrays.asList(_flags));
        }
        MAPPING_FLAGS = Collections.unmodifiableSet(flags);
    }
    
    //========================= Public Methods =================================
    /**
     * @return All Flags on this Mapping or {@code null} / empty list, if there are no mappings.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Override
    public Set<? extends DataMappingFlagEnum> getDataMappingFlags() {
        return MAPPING_FLAGS;
    }
    
    @Override
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMappingValidator.createFormattedErrorString(SOURCE_VALUE, DESTINATION_VALUE);
        
        //------------------------ Variables -----------------------------------
        boolean isEqual;
        
        //------------------------ Code ----------------------------------------
        ///// Custom Comparator /////
        if(customComparator != null) {
            if(customComparator.compair(this, SOURCE_VALUE, DESTINATION_VALUE)) {
                return null;
            }
            else {
                return ERROR_MESSAGE;
            }
        }
        
        ///// Handle NULLs /////
        switch(DataMappingValidator.nullCount(SOURCE_VALUE, DESTINATION_VALUE)) {
            case 1:
                return ERROR_MESSAGE;
            case 2:
                return null;
        }
        
        ////// Compare /////
        if(MAPPING_FLAGS.contains(MappingFlag.IGNORE_PRECISION)) {
            isEqual = SOURCE_VALUE.compareTo(DESTINATION_VALUE) == 0;
        }
        else {
            isEqual = SOURCE_VALUE.equals(DESTINATION_VALUE);
        }
        
        ////// Return /////
        return isEqual ? null : ERROR_MESSAGE;
    }
    
    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
