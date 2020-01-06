/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

import java.util.Set;

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
public class LongMappingValidator extends AbstractDataMapping<Long> {
    
    //========================= Static Enums ===================================
    
    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(LongMappingValidator.class);
    
    //========================= Static Variables ===============================
    
    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    /**
     * The values being compared.
     */
    public final Long SOURCE_VALUE, DESTINATION_VALUE;
    
    //========================= Variables ======================================
    
    //========================= Constructors ===================================
    
    /**
     * Creates a new {@link long}-to-{@link long} {@link DataMappingValidator} object.
     *
     * @param _sourceValue
     * 		The value from the Source Data.
     * @param _destinationValue
     * 		The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMappingValidator(long _sourceValue, long _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }

    /**
     * Creates a new {@link long}-to-{@link long} {@link DataMappingValidator} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link LongMappingValidator}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMappingValidator(String _mappingName, long _sourceValue, long _destinationValue) {
        this(_mappingName, new Long(_sourceValue), new Long(_destinationValue));
    }
    
    /**
     * Creates a new {@link Long}-to-{@link Long} {@link DataMappingValidator} object.
     *
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMappingValidator(Long _sourceValue, Long _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }
    
    /**
     * Creates a new {@link Long}-to-{@link Long} {@link DataMappingValidator} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link LongMappingValidator}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LongMappingValidator(String _mappingName, Long _sourceValue, Long _destinationValue) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        setMappingName(_mappingName);
        
        SOURCE_VALUE = _sourceValue;
        DESTINATION_VALUE = _destinationValue;
    }
    
    //========================= Public Methods =================================
    /**
     * @return All Flags on this Mapping or {@code null} / empty list, if there are no mappings.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Override
    public Set<? extends DataMappingFlagEnum> getDataMappingFlags() {
        return null;
    }
    
    @Override
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMappingValidator.createFormattedErrorString(SOURCE_VALUE, DESTINATION_VALUE);
        
        //------------------------ Variables -----------------------------------
        
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
        
        ////// Compare & Return /////
        return SOURCE_VALUE.equals(DESTINATION_VALUE) ? null : ERROR_MESSAGE;
    }
    
    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
