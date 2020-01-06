/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * <p>
 * Use this class to validate that 2 different {@link Temporal}s representing Date and/or Time are equal.
 * </p>
 * <p>
 * <i>Note:</i> All {@link Temporal}s are converted to {@link LocalDateTimeTuple}s, before comparison.
 * </p>
 * <p>
 * <b>Warning:</b> Be careful when comparing Date/Times both with and without Time Zones.
 * Date/Times without Time Zones will be treated as having come from the Local (System Default) Time Zone.
 * </p>
 * <p>&nbsp;</p>
 * <p>
 * This {@link DataMappingValidator} has been tested with:
 * </p>
 * <ul>
 *     <li>{@link LocalDateTime}</li>
 *     <li>{@link LocalDate}</li>
 *     <li>{@link LocalTime}</li>
 *     <li>{@link java.util.Date}</li>
 *     <li>{@link java.sql.Date}</li>
 *     <li>{@link java.sql.Timestamp}</li>
 *     <li>{@link Instant}</li>
 *     <li>{@link ZonedDateTime}</li>
 *     <li>{@link OffsetDateTime}</li>
 *     <li>{@link OffsetTime}</li>
 * </ul>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class DateTimeMappingValidator extends AbstractDataMapping<DateTimeMappingValidator.LocalDateTimeTuple> {
	
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
	private static final Logger LOGGER = LogManager.getLogger(DateTimeMappingValidator.class);
	
	/**
	 * Sets {@link MappingFlag}s to be used for all {@link DateTimeMappingValidator}s.
	 */
	public static final EnumSet<MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(MappingFlag.class);
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Static Methods =================================
	
	/**
	 * Will convert a {@link Temporal} to a {@link LocalDateTimeTuple}.
	 *
	 * @param _temporal
	 * 		The {@link Temporal} to convert.
	 *
	 * @return The resulting {@link LocalDateTimeTuple} or {@code null}, if null is provided.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static LocalDateTimeTuple temporalToLocalDateTimeTuple(Temporal _temporal) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		LocalDate date = null;
		LocalTime time = null;
		//------------------------ Code ----------------------------------------
		if(_temporal == null) {
			return null;
		}
		
		try { // Try to process as a Zoned Date/Time first.
			ZonedDateTime zonedDateTime; // Using ZonedDateTime instead of OffsetDateTime, so that Offset is calculated and not given.
			if(_temporal instanceof Instant) { // Instants have to be treated differently.
				zonedDateTime = ((Instant) _temporal).atZone(ZoneId.systemDefault());
			}
			else {
				zonedDateTime = ZonedDateTime.from(_temporal);
			}
			
			zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
			date = zonedDateTime.toLocalDate();
			time = zonedDateTime.toLocalTime();
		}
		catch(DateTimeException dte) {  // Process as Local Date/Time.
			
			LOGGER.trace(dte);
			
			if(_temporal.isSupported(ChronoUnit.DAYS)) {
				date = LocalDate.from(_temporal);
			}
			if(_temporal.isSupported(ChronoUnit.HOURS)) {
				
				try { // Try to process as a Zoned Date/Time first.
					time = OffsetTime.from(_temporal).withOffsetSameInstant(OffsetTime.now().getOffset()).toLocalTime();
				}
				catch(DateTimeException dte2) {  // Process as Local Date/Time.
					
					LOGGER.trace(dte2);
					
					time = LocalTime.from(_temporal);
				}
			}
		}
		
		return new LocalDateTimeTuple(date, time);
	}
	
	//========================= CONSTANTS ======================================
	public final LocalDateTimeTuple SOURCE, DESTINATION;
	
	/**
	 * The {@link MappingFlag}s that are applied to this {@link DateTimeMappingValidator} object.
	 */
	public final Set<MappingFlag> MAPPING_FLAGS;
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * Creates a new blank {@link Date}-to-{@link Date} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Date}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(Date _source, Date _destination, MappingFlag... _flags) {
		this(null, _source, _destination, _flags);
	}
	
	/**
	 * Creates a new blank {@link Date}-to-{@link Temporal} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Date}s/{@link Temporal}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(Date _source, Temporal _destination, MappingFlag... _flags) {
		this(null, _source, _destination, _flags);
	}
	
	/**
	 * Creates a new blank {@link Temporal}-to-{@link Date} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Temporal}s/{@link Date}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(Temporal _source, Date _destination, MappingFlag... _flags) {
		this(null, _source, _destination, _flags);
	}
	
	/**
	 * Creates a new blank {@link Temporal}-to-{@link Temporal} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Temporal}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(Temporal _source, Temporal _destination, MappingFlag... _flags) {
		this(null, _source, _destination, _flags);
	}
	
	/**
	 * Creates a new blank {@link Date}-to-{@link Date} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Date}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link DateTimeMappingValidator}.
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(String _mappingName, Date _source, Date _destination, MappingFlag... _flags) {
		this(_mappingName,
				_source == null ? null : Instant.ofEpochMilli(_source.getTime()),
				_destination == null ? null : Instant.ofEpochMilli(_destination.getTime()),
				_flags);
	}
	
	/**
	 * Creates a new blank {@link Date}-to-{@link Temporal} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Date}s/{@link Temporal}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link DateTimeMappingValidator}.
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(String _mappingName, Date _source, Temporal _destination, MappingFlag... _flags) {
		this(_mappingName, _source == null ? null : Instant.ofEpochMilli(_source.getTime()), _destination, _flags);
	}
	
	/**
	 * Creates a new blank {@link Temporal}-to-{@link Date} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Temporal}s/{@link Date}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link DateTimeMappingValidator}.
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(String _mappingName, Temporal _source, Date _destination, MappingFlag... _flags) {
		this(_mappingName, _source, _destination == null ? null : Instant.ofEpochMilli(_destination.getTime()), _flags);
	}
	
	/**
	 * Creates a new blank {@link Temporal}-to-{@link Temporal} {@link DataMappingValidator} object.
	 * <p>
	 * <i>Note:</i> All {@link Temporal}s are converted to {@link LocalDateTime}s, before comparison.
	 * </p>
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link DateTimeMappingValidator}.
	 * @param _source
	 * 		The Source Date/Time.
	 * @param _destination
	 * 		The Destination Date/Time.
	 * @param _flags
	 * 		Any {@link MappingFlag}s that should be applied to this {@link DateTimeMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DateTimeMappingValidator(String _mappingName, Temporal _source, Temporal _destination, MappingFlag... _flags) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		EnumSet<MappingFlag> flags = EnumSet.noneOf(MappingFlag.class);
		
		//------------------------ Code ----------------------------------------
		///// Set Name /////
		setMappingName(_mappingName);
		
		///// Create LocalDate/Time /////
		SOURCE = temporalToLocalDateTimeTuple(_source);
		DESTINATION = temporalToLocalDateTimeTuple(_destination);
		
		///// Gather Flags /////
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
	
	// TODO: Add Support for java.util.Date (must also pass in timezone and/or assume one).
	// - This will also implicitly add support for java.sql.Date and java.sql.Timestamp
	// - Update Unit Tests
	
	// TODO: Add Support for String Date/Time (must also pass in formatt and timezone and/or assume one).
	// - Update Unit Tests
	
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
	
	/**
	 * This method compares the two, previously set, values.
	 *
	 * @return A String with any differences found, or {@code NULL}, if there were no differences.
	 *
	 * @throws NullPointerException
	 * 		If either the Source or Destination Values have not been set.
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public String validate() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		String ERROR_MESSAGE = DataMappingValidator.createFormattedErrorString(SOURCE, DESTINATION);
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		///// Custom Comparator /////
		if(customComparator != null) {
			if(customComparator.compair(this, SOURCE, DESTINATION)) {
				return null;
			}
			else {
				return ERROR_MESSAGE;
			}
		}
		
		///// Handle NULLs /////
		switch(DataMappingValidator.nullCount(SOURCE, DESTINATION)) {
			case 1:
				return ERROR_MESSAGE;
			case 2:
				return null;
		}
		
		///// Compare with Flags /////
		boolean isEqual = true;
		
		if(!MAPPING_FLAGS.contains(MappingFlag.IGNORE_DATE)) {
			isEqual &= Objects.equals(SOURCE.DATE, DESTINATION.DATE);
		}
		
		if(isEqual && !MAPPING_FLAGS.contains(MappingFlag.IGNORE_TIME)) {
			isEqual &= Objects.equals(SOURCE.TIME, DESTINATION.TIME);
		}
		
		////// Return /////
		return isEqual ? null : ERROR_MESSAGE;
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
	
	/**
	 * This is how the Date/Time data is stored for comparison.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static class LocalDateTimeTuple {
		
		public final LocalDate DATE;
		public final LocalTime TIME;
		
		/**
		 * Creates a Date/Time Tuple, for data comparison.
		 *
		 * @param _date
		 * 		The Date Component of this {@link LocalDateTimeTuple}; or {@code null} if there is no Date.
		 * @param _time
		 * 		The Time Component of this {@link LocalDateTimeTuple}; or {@code null} if there is no Time.
		 *
		 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
		 */
		public LocalDateTimeTuple(LocalDate _date, LocalTime _time) {
			DATE = _date;
			TIME = _time;
		}
		
		@Override
		public String toString() {
			return DATE + " " + TIME;
		}
	}
}
