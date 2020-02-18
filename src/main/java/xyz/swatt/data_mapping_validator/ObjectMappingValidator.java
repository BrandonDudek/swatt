/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

import java.util.Set;

/**
 * Use this class to validate that 2 different {@link T}s are equal.
 * <p>
 * (This will just use the {@link T}'s {@code equals(T)} method, and is the fallback class for the {@link DataMappingValidator} interface.)
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class ObjectMappingValidator<T> extends AbstractDataMapping<T> {
    
    //========================= Static Enums ===================================
    
    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(ObjectMappingValidator.class);
    
    //========================= Static Variables ===============================
    
    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================
    /**
     * The values being compared.
     */
    public final T SOURCE_VALUE, DESTINATION_VALUE;
    
    //========================= Variables ======================================
    
    //========================= Constructors ===================================
    
    /**
     * Creates a new {@link T}-to-{@link T} {@link DataMappingValidator} object.
     *
     * @param _sourceValue
     * 		The value from the Source Data.
     * @param _destinationValue
     * 		The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public ObjectMappingValidator(T _sourceValue, T _destinationValue) {
        this(null, _sourceValue, _destinationValue);
    }

    /**
     * Creates a new {@link T}-to-{@link T} {@link DataMappingValidator} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link ObjectMappingValidator}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public ObjectMappingValidator(String _mappingName, T _sourceValue, T _destinationValue) {
    
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
            if(customComparator.compare(this, SOURCE_VALUE, DESTINATION_VALUE)) {
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
