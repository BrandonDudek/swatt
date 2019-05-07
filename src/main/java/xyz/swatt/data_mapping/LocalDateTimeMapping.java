/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>
 * Use this class to validate that 2 different {@link LocalDateTime}s, {@link LocalDate}s, {@link LocalTime}s, {@link ZonedDateTime}s, or {@link String}s
 * Representing Date and/or Time are equal.
 * </p>
 * <p>
 * Other Date and Time Classes Supported: {@link TemporalAccessor} ( {@link LocalDate} ).
 * </p>
 * <p>
 * <i>Note:</i> Because of the large number of supported class, you must use {@code setSource(...)} and {@code setDestination(...)} methods,
 * on top of the constructor.
 * </p>
 * <p>
 * <b>Warning:</b> Be careful when comparing (Date)Times both with and without Time Zones.
 * (Date)Times without Time Zones will be treated as having come from the Local (System Default) Time Zone.
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class LocalDateTimeMapping implements DataMapping {

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
    public static enum MappingFlag {

        /**
         * Will only validate the values' Times.
         * <p>
         * <b>Note:</b> Cannot be used with {@link #IGNORE_TIME}.
         * </p>
         */
        IGNORE_DATE,

        /**
         * Will only validate the values' Dates.
         * <p>
         * <b>Note:</b> Cannot be used with {@link #IGNORE_DATE}.
         * </p>
         */
        IGNORE_TIME
    }

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(LocalDateTimeMapping.class);

    /**
     * Sets {@link MappingFlag}s to be used for all {@link LocalDateTimeMapping}s.
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
     * The {@link MappingFlag}s that are applied to this {@link LocalDateTimeMapping} object.
     */
    public final Set<MappingFlag> MAPPING_FLAGS;

    //========================= Variables ======================================
    public boolean sourceWasSet = false, destinationWasSet = false;
    public LocalDate sourceDateValue, destinationDateValue; // LocalDate is immutable.
    public LocalTime sourceTimeValue, destinationTimeValue; // LocalTime is immutable.

    //========================= Constructors ===================================

    /**
     * Creates a new blank {@link LocalDateTime}-to-{@link LocalDateTime} {@link DataMapping} object.
     * <p>
     * <i>Note:</i> You must call the {@code setSource(...)} and {@code setDestination(...)} methods,
     * before you can call the {@link #validate()} method.
     * </p>
     *
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link LocalDateTimeMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping(MappingFlag... _flags) {
        this(null, _flags);
    }

    /**
     * Creates a new blank {@link LocalDateTime}-to-{@link LocalDateTime} {@link DataMapping} object.
     * <p>
     * <i>Note:</i> You must call the {@code setSource(...)} and {@code setDestination(...)} methods,
     * before you can call the {@link #validate()} method.
     * </p>
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link LocalDateTimeMapping}.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link LocalDateTimeMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping(String _mappingName, MappingFlag... _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        EnumSet<MappingFlag> flags = EnumSet.noneOf(MappingFlag.class);

        //------------------------ Code ----------------------------------------
        MAPPING_NAME = _mappingName == null || StringHelper.removeWhitespace(_mappingName).isEmpty() ? null : StringHelper.trim(_mappingName);

        flags.addAll(GLOBAL_MAPPING_FLAGS);
        if(_flags != null && _flags.length > 0) {
            flags.addAll(Arrays.asList(_flags));
        }
        MAPPING_FLAGS = Collections.unmodifiableSet(flags);

        ///// Check for Mapping Flag Conflicts /////
        if(MAPPING_FLAGS.containsAll(Arrays.asList(MappingFlag.IGNORE_DATE, MappingFlag.IGNORE_TIME))) {
            throw new IllegalArgumentException("Cannot use both IGNORE_DATE & IGNORE_TIME flags in the same LocalDateTimeMapping!");
        }
    }

    //========================= Public Methods =================================
    ////////// Setters //////////

    /**
     * Set's the Source Value to the given argument.
     *
     * @param _sourceValue
     *         The object to use as the Source Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setSourceValue(ZonedDateTime _sourceValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        LocalDateTime localDateTime = null;

        //------------------------ Code ----------------------------------------
        if(_sourceValue != null) {
            localDateTime = _sourceValue.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }

        return setSourceValue(localDateTime);
    }

    /**
     * Set's the Destination Value to the given argument.
     *
     * @param _destinationValue
     *         The object to use as the Destination Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setDestinationValue(ZonedDateTime _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        LocalDateTime localDateTime = null;

        //------------------------ Code ----------------------------------------
        if(_destinationValue != null) {
            localDateTime = _destinationValue.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }

        return setDestinationValue(localDateTime);
    }

    /**
     * Set's the Source Value to the given argument.
     *
     * @param _sourceValue
     *         The object to use as the Source Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setSourceValue(LocalDateTime _sourceValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_sourceValue != null) {
            sourceDateValue = _sourceValue.toLocalDate();
            sourceTimeValue = _sourceValue.toLocalTime();
        }

        sourceWasSet = true;

        return this;
    }

    /**
     * Set's the Destination Value to the given argument.
     *
     * @param _destinationValue
     *         The object to use as the Destination Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setDestinationValue(LocalDateTime _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_destinationValue != null) {
            destinationDateValue = _destinationValue.toLocalDate();
            destinationTimeValue = _destinationValue.toLocalTime();
        }

        destinationWasSet = true;

        return this;
    }

    /**
     * Set's the Source Value to the given argument.
     *
     * @param _sourceValue
     *         The object to use as the Source Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setSourceValue(LocalDate _sourceValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_sourceValue != null) {
            sourceDateValue = _sourceValue;
            sourceTimeValue = null;
        }

        sourceWasSet = true;

        return this;
    }

    /**
     * Set's the Destination Value to the given argument.
     *
     * @param _destinationValue
     *         The object to use as the Destination Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setDestinationValue(LocalDate _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_destinationValue != null) {
            destinationDateValue = _destinationValue;
            destinationTimeValue = null;
        }

        destinationWasSet = true;

        return this;
    }

    /**
     * Set's the Source Value to the given argument.
     *
     * @param _sourceValue
     *         The object to use as the Source Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setSourceValue(LocalTime _sourceValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_sourceValue != null) {
            sourceDateValue = null;
            sourceTimeValue = _sourceValue;
        }

        sourceWasSet = true;

        return this;
    }

    /**
     * Set's the Destination Value to the given argument.
     *
     * @param _destinationValue
     *         The object to use as the Destination Value.
     *
     * @return A reference to itself, for method chaining.
     *
     * @throws DateTimeParseException
     *         If the given Date/Time {@link String} cannot be parsed with the given {@link DateTimeFormatter}.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public LocalDateTimeMapping setDestinationValue(LocalTime _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_destinationValue != null) {
            destinationDateValue = null;
            destinationTimeValue = _destinationValue;
        }

        destinationWasSet = true;

        return this;
    }

    /**
     * Set's the Source Value to the given argument.
     *
     * @param _sourceValue
     *         The object to use as the Source Value.
     * @param _formatter
     *         The formatter to use, not {@code null}.
     *
     * @return A reference to itself, for method chaining.
     *
     * @throws DateTimeParseException
     *         If the given Date/Time {@link String} cannot be parsed with the given {@link DateTimeFormatter}.
     * @throws IllegalArgumentException
     *         If any of the arguments are {@code null}.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @SuppressWarnings("Duplicates")
    public LocalDateTimeMapping setSourceValue(String _sourceValue, DateTimeFormatter _formatter) {

        //------------------------ Pre-Checks ----------------------------------
        ArgumentChecks.stringNotWhitespaceOnly(_sourceValue, "Source Date/Time");
        ArgumentChecks.notNull(_formatter, "DateTimeFormatter");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        // Parse as Local Time Zone, if none is given.
        _formatter = _formatter.getZone() == null ? _formatter.withZone(ZoneId.systemDefault()) : _formatter;

        //------------------------ Code ----------------------------------------
        if(_sourceValue != null) {

            try { // Try to load as a Zoned Date Time.
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(_sourceValue, _formatter);
                zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()); // Convert to Local Time Zone.
                sourceDateValue = zonedDateTime.toLocalDate();
                sourceTimeValue = zonedDateTime.toLocalTime();
            }
            catch(DateTimeParseException dtpe) {

                if(dtpe.getMessage().toLowerCase().contains("unable to obtain zoneddatetime from temporalaccessor")) { // Missing Date or Time.

                    TemporalAccessor temporalAccessor = _formatter.parse(_sourceValue); // Parse without Zone.

                    ///// Extract Date /////
                    try {
                        sourceDateValue = LocalDate.from(temporalAccessor);
                    }
                    catch(DateTimeException dte) {
                        if(dte.getMessage().toLowerCase().contains("unable to obtain localdate from temporalaccessor")) {
                            sourceDateValue = null; // No Date aspect.
                        }
                        else {
                            throw dte;
                        }
                    }

                    ///// Extract Time /////
                    try {
                        sourceTimeValue = LocalTime.from(temporalAccessor);
                    }
                    catch(DateTimeException dte) {
                        if(dte.getMessage().toLowerCase().contains("unable to obtain localtime from temporalaccessor")) {
                            sourceTimeValue = null; // No Time aspect.
                        }
                        else {
                            throw dte;
                        }
                    }
                } // END: No Time Zone block
            } // END: DateTimeParseException
        }

        sourceWasSet = true;

        return this;
    }

    /**
     * Set's the Destination Value to the given argument.
     *
     * @param _destinationValue
     *         The object to use as the Destination Value.
     * @param _formatter
     *         The formatter to use, not {@code null}.
     *
     * @return A reference to itself, for method chaining.
     *
     * @throws DateTimeParseException
     *         If the given Date/Time {@link String} cannot be parsed with the given {@link DateTimeFormatter}.
     * @throws IllegalArgumentException
     *         If any of the arguments are {@code null}.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @SuppressWarnings("Duplicates")
    public LocalDateTimeMapping setDestinationValue(String _destinationValue, DateTimeFormatter _formatter) {

        //------------------------ Pre-Checks ----------------------------------
        ArgumentChecks.stringNotWhitespaceOnly(_destinationValue, "Destination Date/Time");
        ArgumentChecks.notNull(_formatter, "DateTimeFormatter");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        // Parse as Local Time Zone, if none is given.
        _formatter = _formatter.getZone() == null ? _formatter.withZone(ZoneId.systemDefault()) : _formatter;

        //------------------------ Code ----------------------------------------
        if(_destinationValue != null) {

            try { // Try to load as a Zoned Date Time.
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(_destinationValue, _formatter);
                zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()); // Convert to Local Time Zone.
                destinationDateValue = zonedDateTime.toLocalDate();
                destinationTimeValue = zonedDateTime.toLocalTime();
            }
            catch(DateTimeParseException dtpe) {

                if(dtpe.getMessage().toLowerCase().contains("unable to obtain zoneddatetime from temporalaccessor")) { // Missing Date or Time.

                    TemporalAccessor temporalAccessor = _formatter.parse(_destinationValue); // Parse without Zone.

                    ///// Extract Date /////
                    try {
                        destinationDateValue = LocalDate.from(temporalAccessor);
                    }
                    catch(DateTimeException dte) {
                        if(dte.getMessage().toLowerCase().contains("unable to obtain localdate from temporalaccessor")) {
                            destinationDateValue = null; // No Date aspect.
                        }
                        else {
                            throw dte;
                        }
                    }

                    ///// Extract Time /////
                    try {
                        destinationTimeValue = LocalTime.from(temporalAccessor);
                    }
                    catch(DateTimeException dte) {
                        if(dte.getMessage().toLowerCase().contains("unable to obtain localtime from temporalaccessor")) {
                            destinationTimeValue = null; // No Time aspect.
                        }
                        else {
                            throw dte;
                        }
                    }
                } // END: No Time Zone block
            } // END: DateTimeParseException
        }

        destinationWasSet = true;

        return this;
    }

    ////////// Validate //////////

    /**
     * This method compares the two, previously set, values.
     *
     * @return A String with any differences found, or {@code NULL}, if there were no differences.
     *
     * @throws NullPointerException
     *         If either the Source or Destination Values have not been set.
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Override
    public String validate() {

        //------------------------ Pre-Checks ----------------------------------
        if(!sourceWasSet) {
            throw new NullPointerException("The Source Value was never Set!");
        }
        if(!destinationWasSet) {
            throw new NullPointerException("The Destination Value was never Set!");
        }

        //------------------------ CONSTANTS -----------------------------------
        String ERROR_MESSAGE = DataMapping.createFormattedErrorString(sourceDateValue + " " + sourceTimeValue,
                destinationDateValue + " " + destinationTimeValue);

        //------------------------ Variables -----------------------------------
        boolean isEqual = true;

        // LocalDate and LocalTime are both immutable.

        //------------------------ Code ----------------------------------------
        if(!MAPPING_FLAGS.contains(MappingFlag.IGNORE_DATE)) {

            ///// Handle NULLs /////
            Boolean isOneNullButNotBoth = DataMapping.isOneNullButNotBoth(sourceDateValue, destinationDateValue);
            if(isOneNullButNotBoth != null) {
                if(isOneNullButNotBoth) {
                    return ERROR_MESSAGE;
                }
                else {
                    return null;
                }
            }

            ////// Compare /////
            isEqual &= sourceDateValue.equals(destinationDateValue);
        }

        if(!MAPPING_FLAGS.contains(MappingFlag.IGNORE_TIME)) {

            ///// Handle NULLs /////
            Boolean isOneNullButNotBoth = DataMapping.isOneNullButNotBoth(sourceTimeValue, destinationTimeValue);
            if(isOneNullButNotBoth != null) {
                if(isOneNullButNotBoth) {
                    return ERROR_MESSAGE;
                }
                else {
                    return null;
                }
            }

            ////// Compare /////
            isEqual &= sourceTimeValue.equals(destinationTimeValue);
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
