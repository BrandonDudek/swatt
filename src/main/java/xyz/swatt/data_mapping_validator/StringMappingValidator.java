/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>
 * Use this class to validate that 2 different {@link String}s or {@link Character}s are equal.
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class StringMappingValidator extends AbstractDataMapping<String> {
    
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
         * Will compare values ignoring the case.
         */
        IGNORE_CASE,

        /**
         * Will normalize the whitespace from all of the Source Values using {@link StringHelper#normalize(String)}.
         */
        NORMALIZE_SOURCE,

        /**
         * Will normalize the whitespace from all of the Destination Values using {@link StringHelper#normalize(String)}.
         */
        NORMALIZE_DESTINATION,

        /**
         * Will trim the whitespace from all of the Source Values using {@link StringHelper#trim(String)}.
         */
        TRIM_SOURCE,

        /**
         * Will trim the whitespace from all of the Destination Values using {@link StringHelper#trim(String)}.
         */
        TRIM_DESTINATION,

        /**
         * Will XML Escape all of the Source's "{@literal &}" to "{@literal &amp;}".
         */
        XML_ESCAPE_SOURCE,

        /**
         * Will XML Escape all of the Destination's "{@literal &}" to "{@literal &amp;}".
         */
        XML_ESCAPE_DESTINATION
    }
    
    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(StringMappingValidator.class);
    
    /**
     * Sets {@link MappingFlag}s to be used for all {@link StringMappingValidator}s.
     */
    public static final EnumSet<MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(MappingFlag.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================
    /**
     * @param _str
     *         The string to escape.
     *
     * @return The escaped string.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    private static String xmlEscape(String _str) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String escapedString;

        //------------------------ Code ----------------------------------------
        escapedString = _str.replaceAll("&(?![#0-9a-zA-Z]+;)", "&amp;"); // Escapes &'s that are not already part of an XML Entity.

        return escapedString;
    }

    //========================= CONSTANTS ======================================
    /**
     * The values being compared.
     */
    public final String SOURCE_VALUE, DESTINATION_VALUE;
    
    /**
     * The {@link MappingFlag}s that are applied to this {@link StringMappingValidator} object.
     */
    public final Set<MappingFlag> MAPPING_FLAGS;

    //========================= Variables ======================================
    
    //========================= Constructors ===================================
    
    /**
     * Creates a new {@link String}-to-{@link String} {@link DataMappingValidator} object.
     *
     * @param _sourceValue
     * 		The value from the Source Data.
     * @param _destinationValue
     * 		The mapped value found in the Destination Data.
     * @param _flags
     * 		Any {@link MappingFlag}s that should be applied to this {@link StringMappingValidator}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public StringMappingValidator(String _sourceValue, String _destinationValue, MappingFlag... _flags) {
        this(null, _sourceValue, _destinationValue, _flags);
    }

    /**
     * Creates a new {@link String}-to-{@link String} {@link DataMappingValidator} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link StringMappingValidator}.
     * @param _sourceValue
     *         The value from the Source Data.
     * @param _destinationValue
     *         The mapped value found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link StringMappingValidator}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public StringMappingValidator(String _mappingName, String _sourceValue, String _destinationValue, MappingFlag... _flags) {
    
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
        
        String source = SOURCE_VALUE, destination = DESTINATION_VALUE; // Not work with class variables, so that `validated()` can be called, idempotently.
        
        //------------------------ Code ----------------------------------------
        ///// Handle NULLs w/o Custom Comparator /////
        if(customComparator == null) {
            switch(DataMappingValidator.nullCount(SOURCE_VALUE, DESTINATION_VALUE)) {
                case 1:
                    return ERROR_MESSAGE;
                case 2:
                    return null;
            }
        }
        
        ////// Handle Flags /////
        if(source != null && MAPPING_FLAGS.contains(MappingFlag.NORMALIZE_SOURCE)) {
            source = StringHelper.normalize(source);
        }
        if(destination != null && MAPPING_FLAGS.contains(MappingFlag.NORMALIZE_DESTINATION)) {
            destination = StringHelper.normalize(destination);
        }
        
        if(source != null && MAPPING_FLAGS.contains(MappingFlag.TRIM_SOURCE)) {
            source = StringHelper.trim(source);
        }
        if(destination != null && MAPPING_FLAGS.contains(MappingFlag.TRIM_DESTINATION)) {
            destination = StringHelper.trim(destination);
        }
        
        if(source != null && MAPPING_FLAGS.contains(MappingFlag.XML_ESCAPE_SOURCE)) {
            source = xmlEscape(source);
        }
        if(destination != null && MAPPING_FLAGS.contains(MappingFlag.XML_ESCAPE_DESTINATION)) {
            destination = xmlEscape(destination);
        }
        
        ////// Compare & Return /////
        if(customComparator != null) {
            if(customComparator.compair(this, source, destination)) {
                return null;
            }
            else {
                return ERROR_MESSAGE;
            }
        }
        else {
            if(MAPPING_FLAGS.contains(MappingFlag.IGNORE_CASE)) {
                isEqual = source.equalsIgnoreCase(destination);
            }
            else {
                isEqual = source.equals(destination);
            }
            
            return isEqual ? null : ERROR_MESSAGE;
        }
    }
    
    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
