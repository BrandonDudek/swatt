/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.pojo.SqlPojo;
import xyz.swatt.string.StringHelper;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Use this class to validate that two different Columns in two different {@link SqlPojo}s are equal.
 * </p>
 * <p>
 * <b>Note:</b> {@code T} is the {@link DataMapping} class to use to back this one. Supported Options:
 * </p>
 * <ul>
 * <li>BigDecimalMapping - To be used if the {@link SqlPojo}'s Column's Values are in {@link BigDecimal} format</li>
 * <li>LocalDateTimeMapping</li>
 * <li>LongMapping</li>
 * <li>StringMapping</li>
 * <li>ObjectMapping</li>
 * </ul>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class SqlPojoMapping<T extends DataMapping> implements DataMapping {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(SqlPojoMapping.class);
	
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
	 * The Tables being compared.
	 */
	public final SqlPojo SOURCE_TABLE, DESTINATION_TABLE;
	
	/**
	 * The Columns being compared.
	 */
	public final SqlPojo.RowMapperColumnEnum SOURCE_COLUMN, DESTINATION_COLUMN;
	
	/**
	 * The {@link DataMapping} object that this one is backed by.
	 */
	public final T DATA_MAPPING_BACKER;
	
	/**
	 * The {@link DataMappingFlagEnum}s that are applied to this {@link SqlPojoMapping} object.
	 */
	public final Set<DataMappingFlagEnum> MAPPING_FLAGS;
	
	private Class<T> DATA_MAPPING_CLASS;
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * Creates a new {@link SqlPojo}-to-{@link SqlPojo} {@link DataMapping} object.
	 * <p>
	 * <i>Note:</i> {@link #MAPPING_NAME} is generated automatically.
	 * </p>
	 *
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link DataMapping.DataMappingFlagEnum}s that should be applied to this {@link SqlPojoMapping}.
	 * 		(Enum Type must match {@link T} class's implementation of {@link DataMapping.DataMappingFlagEnum}.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoMapping(SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                      SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                      DataMappingFlagEnum... _flags) {
		this(null, _sourceTable, _sourceColumn, _destinationTable, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link SqlPojo}-to-{@link SqlPojo} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoMapping}.
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link DataMapping.DataMappingFlagEnum}s that should be applied to this {@link SqlPojoMapping}.
	 * 		(Enum Type must match {@link T} class's implementation of {@link DataMapping.DataMappingFlagEnum}.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoMapping(String _mappingName,
	                      SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                      SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                      DataMappingFlagEnum... _flags) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Object sourceValueObject, destinationValueObject;
		
		Set<DataMappingFlagEnum> flags = new HashSet<>();
		
		//------------------------ Code ----------------------------------------
		DATA_MAPPING_CLASS = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		SOURCE_TABLE = _sourceTable;
		DESTINATION_TABLE = _destinationTable;
		SOURCE_COLUMN = _sourceColumn;
		DESTINATION_COLUMN = _destinationColumn;
		
		if(_mappingName == null) {
			MAPPING_NAME = _sourceTable.getFullTableName() + "#" + _sourceColumn + " -> " + _destinationTable.getFullTableName() + "#" + _destinationColumn;
		}
		else {
			MAPPING_NAME = StringHelper.trim(_mappingName); // Allowing empty string, because when NULL is passes in, default name generation is used.
		}
		
		
		if(_flags != null && _flags.length > 0) {
			flags.addAll(Arrays.asList(_flags));
		}
		MAPPING_FLAGS = Collections.unmodifiableSet(flags);
		
		sourceValueObject = _sourceTable.getColumnValue(_sourceColumn);
		destinationValueObject = _destinationTable.getColumnValue(_destinationColumn);
		
		if(DATA_MAPPING_CLASS == BigDecimalMapping.class) {
			DATA_MAPPING_BACKER = (T) new BigDecimalMapping(MAPPING_NAME, (BigDecimal) sourceValueObject, (BigDecimal) destinationValueObject,
					MAPPING_FLAGS.toArray(new BigDecimalMapping.MappingFlag[] {}));
		}
		else if(DATA_MAPPING_CLASS == LocalDateTimeMapping.class) {
			
			DATA_MAPPING_BACKER = (T) new LocalDateTimeMapping(MAPPING_NAME,
					MAPPING_FLAGS.toArray(new LocalDateTimeMapping.MappingFlag[] {}));
			
			if(sourceValueObject instanceof LocalDateTime) {
				((LocalDateTimeMapping) DATA_MAPPING_BACKER).setSourceValue((LocalDateTime) sourceValueObject);
			}
			else if(sourceValueObject instanceof LocalTime) {
				((LocalDateTimeMapping) DATA_MAPPING_BACKER).setSourceValue((LocalTime) sourceValueObject);
			}
			else {
				throw new RuntimeException("Source Value is in an unknown format: " + sourceValueObject.getClass().getName() + "!");
			}
			
			if(destinationValueObject instanceof LocalDateTime) {
				((LocalDateTimeMapping) DATA_MAPPING_BACKER).setDestinationValue((LocalDateTime) destinationValueObject);
			}
			else if(destinationValueObject instanceof LocalTime) {
				((LocalDateTimeMapping) DATA_MAPPING_BACKER).setDestinationValue((LocalTime) destinationValueObject);
			}
			else {
				throw new RuntimeException("Destination Value is in an unknown format: " + destinationValueObject.getClass().getName() + "!");
			}
		}
		else if(DATA_MAPPING_CLASS == LongMapping.class) {
			DATA_MAPPING_BACKER = (T) new LongMapping(MAPPING_NAME, (Long) sourceValueObject, (Long) destinationValueObject);
		}
		else if(DATA_MAPPING_CLASS == StringMapping.class) {
			String sourceValueString = sourceValueObject == null ? null : sourceValueObject.toString(),
					destinationValueString = destinationValueObject == null ? null : destinationValueObject.toString();
			DATA_MAPPING_BACKER = (T) new StringMapping(MAPPING_NAME, sourceValueString, destinationValueString,
					MAPPING_FLAGS.toArray(new StringMapping.MappingFlag[] {}));
		}
		else if(DATA_MAPPING_CLASS == ObjectMapping.class) {
			DATA_MAPPING_BACKER = (T) new ObjectMapping(MAPPING_NAME, sourceValueObject, destinationValueObject);
		}
		else {
			throw new RuntimeException("Unknown DataMapping type Provided: " + DATA_MAPPING_CLASS + "!");
		}
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
		return DATA_MAPPING_BACKER.validate();
	}
	
	@Override
	public String toString() {
		return MAPPING_NAME;
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
}
