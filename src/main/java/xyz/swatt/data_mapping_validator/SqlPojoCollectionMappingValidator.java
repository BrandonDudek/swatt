/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping_validator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.asserts.ArgumentChecks;
import xyz.swatt.log.LogMethods;
import xyz.swatt.pojo.SqlPojo;

import java.util.*;

/**
 * Use this class to validate that 2 different {@link Collection}s of {@link SqlPojo}s are equal, compairing their Column Values.
 * <p>
 * <b>Note:</b> The Column Value Types are defined by {@link T}.
 * </p>
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class SqlPojoCollectionMappingValidator<T> extends AbstractDataMapping<T> {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(SqlPojoCollectionMappingValidator.class);
	
	/**
	 * Sets {@link CollectionMappingValidator.MappingFlag}s to be used for all {@link SqlPojoCollectionMappingValidator}s.
	 */
	public static final EnumSet<CollectionMappingValidator.MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(CollectionMappingValidator.MappingFlag.class);
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Static Methods =================================
	// TODO: Add a builder() method.
	
	/**
	 * Will combine Mappings that have the same Destination Row(s) and Column.
	 * <p>
	 * Or, for the mappings that do not have a Destination Row/Column, they will be combined on Destination Value(s).
	 * </p>
	 * <p>
	 * <i>Note:</i> Mapping Names will also be combined.
	 * </p>
	 *
	 * @param _mappings
	 * 		The Mappings to consolidate.
	 *
	 * @return All Mappings, after combining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Collection<SqlPojoCollectionMappingValidator> consolidateOnDestination(Collection<SqlPojoCollectionMappingValidator> _mappings) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notEmpty(_mappings, "Mappings");
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		Map<SqlPojoCollectionMappingValidator.TablesColumnPair, SqlPojoCollectionMappingValidator> combinedTableMappings = new HashMap<>();
		Map<List, SqlPojoCollectionMappingValidator> combinedValuesMappings = new HashMap<>();
		
		//------------------------ Code ----------------------------------------
		for(SqlPojoCollectionMappingValidator mapping : _mappings) {
			
			SqlPojoCollectionMappingValidator otherMapping;
			
			if(mapping.DESTINATION_TABLE_ROWS == null || mapping.DESTINATION_TABLE_ROWS.isEmpty() || mapping.DESTINATION_COLUMN == null) {
				otherMapping = combinedValuesMappings.put(mapping.DESTINATION_VALUES, mapping);
			}
			else {
				TablesColumnPair tablesColumnPair = new TablesColumnPair(mapping.DESTINATION_TABLE_ROWS, mapping.DESTINATION_COLUMN);
				otherMapping = combinedTableMappings.put(tablesColumnPair, mapping);
			}
			
			if(otherMapping != null) { // Combine.
				mapping.SOURCE_VALUES.addAll(otherMapping.SOURCE_VALUES);
				mapping.mappingName += " & " + otherMapping.mappingName;
			}
		}
		
		return CollectionUtils.union(combinedTableMappings.values(), combinedValuesMappings.values());
	}
	
	/**
	 * Will combine Mappings that have the same Destination Row(s) and Column.
	 * <p>
	 * Or, for the mappings that do not have a Destination Row/Column, they will be combined on Destination Value(s).
	 * </p>
	 * <p>
	 * <i>Note:</i> Mapping Names will also be combined.
	 * </p>
	 *
	 * @param _mappings
	 * 		The Mappings to consolidate.
	 *
	 * @return All Mappings, after combining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static Collection<SqlPojoCollectionMappingValidator> consolidateOnDestination(SqlPojoCollectionMappingValidator... _mappings) {
		
		//------------------------ Pre-Checks ----------------------------------
		ArgumentChecks.notNull(_mappings, "Mappings");
		if(_mappings.length < 1) {
			throw new RuntimeException("No Mappings given!");
		}
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		return consolidateOnDestination(Arrays.asList(_mappings));
	}
	
	//========================= CONSTANTS ======================================
	public T typeObject;
	
	/**
	 * The {@link CollectionMappingValidator} object that this one is backed by.
	 */
	public final CollectionMappingValidator<T> COLLECTION_MAPPING_BACKER;
	
	/**
	 * The values being compared.
	 */
	public final List SOURCE_VALUES, DESTINATION_VALUES;
	
	/**
	 * The {@link CollectionMappingValidator.MappingFlag}s that are applied to this {@link SqlPojoCollectionMappingValidator} object. (read only)
	 */
	public final Set<CollectionMappingValidator.MappingFlag> MAPPING_FLAGS;
	
	/**
	 * Just used for reference.
	 */
	private final Collection<SqlPojo> DESTINATION_TABLE_ROWS;
	/**
	 * Just used for reference.
	 */
	private final SqlPojo.RowMapperColumnEnum DESTINATION_COLUMN;
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceValue
	 * 		The value from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(T _sourceValue,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceValue), null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceValues
	 * 		The values from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(List<T> _sourceValues,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, _sourceValues, null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
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
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceTable), _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceTable), _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValue
	 * 		The mapped value found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         T _destinationValue,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, null, Arrays.asList(_destinationValue), _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValues
	 * 		The mapped values found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         List<T> _destinationValues,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, null, _destinationValues, _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, _sourceTables, _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(null, _sourceTables, _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceValue
	 * 		The value from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         T _sourceValue,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, Arrays.asList(_sourceValue), null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceValues
	 * 		The values from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         List<T> _sourceValues,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, _sourceValues, null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, Arrays.asList(_sourceTable), _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, Arrays.asList(_sourceTable), _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValue
	 * 		The mapped value found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         T _destinationValue,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, null, Arrays.asList(_destinationValue), _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValues
	 * 		The mapped values found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         List<T> _destinationValues,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, null, _destinationValues, _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, _sourceTables, _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMappingValidator} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMappingValidator}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMappingValidator.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMappingValidator}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator(String _mappingName,
	                                         Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                         Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                         CollectionMappingValidator.MappingFlag... _flags) {
		this(_mappingName, null, null, _sourceTables, _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private SqlPojoCollectionMappingValidator(String _mappingName, Collection<T> _sourceValues, Collection<T> _destinationValues,
	                                          Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                          Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                          CollectionMappingValidator.MappingFlag... _flags) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		DESTINATION_COLUMN = _destinationColumn;
		
		//------------------------ Variables -----------------------------------
		String mappingName = "";
		EnumSet<CollectionMappingValidator.MappingFlag> flags = EnumSet.noneOf(CollectionMappingValidator.MappingFlag.class);
		
		//------------------------ Code ----------------------------------------
		///// Name /////
		if(_mappingName == null) {
			
			if(CollectionUtils.isEmpty(_sourceTables)) {
				if(CollectionUtils.size(_sourceValues) == 1) {
					mappingName += "\"" + _sourceValues.iterator().next() + "\"";
				}
				else {
					mappingName += "UNKNOWN";
				}
			}
			else {
				mappingName += _sourceTables.iterator().next().getFullTableName();
			}
			
			if(_sourceColumn != null) {
				mappingName += "#" + _sourceColumn;
			}
			
			mappingName += " -> ";
			
			if(CollectionUtils.isEmpty(_destinationTables)) {
				if(CollectionUtils.size(_destinationValues) == 1) {
					mappingName += "\"" + _destinationValues.iterator().next() + "\"";
				}
				else {
					mappingName += "UNKNOWN";
				}
			}
			else {
				mappingName += _destinationTables.iterator().next().getFullTableName();
			}
			
			if(_destinationColumn != null) {
				mappingName += "#" + _destinationColumn;
			}
			
			this.mappingName = mappingName;
		}
		else {
			setMappingName(_mappingName); // Allowing empty string, because when NULL is passes in, default name generation is used.
		}
		
		///// Values /////
		if(_sourceValues == null) {
			_sourceValues = new ArrayList(_sourceTables.size());
			for(SqlPojo sourceRow : _sourceTables) {
				_sourceValues.add((T) sourceRow.getColumnValue(_sourceColumn));
			}
		}
		SOURCE_VALUES = new ArrayList(_sourceValues); // Copying values to a new Collection, so that changes made to the passed in collection, will be seen.
		
		if(_destinationValues == null) {
			DESTINATION_TABLE_ROWS = new ArrayList<>(_destinationTables.size());
			_destinationValues = new ArrayList(_destinationTables.size());
			for(SqlPojo destinationRow : _destinationTables) {
				DESTINATION_TABLE_ROWS.add(destinationRow);
				_destinationValues.add((T) destinationRow.getColumnValue(_destinationColumn));
			}
		}
		else {
			DESTINATION_TABLE_ROWS = new ArrayList<>();
		}
		DESTINATION_VALUES = new ArrayList(_destinationValues); // Copying values to a new Collection, so that changes made to the passed in collection, will be seen.
		
		///// Flages /////
		flags.addAll(GLOBAL_MAPPING_FLAGS);
		if(_flags != null && _flags.length > 0) {
			flags.addAll(Arrays.asList(_flags));
		}
		MAPPING_FLAGS = Collections.unmodifiableSet(flags);
		// Check for Mapping Flag Conflicts. (Done in CollectionMapping.)
		
		///// Backer /////
		COLLECTION_MAPPING_BACKER = new CollectionMappingValidator(mappingName, SOURCE_VALUES, DESTINATION_VALUES,
				MAPPING_FLAGS.toArray(new CollectionMappingValidator.MappingFlag[] {}));
	}
	
	// TODO: Allow Source/Destination to be a REGEX.
	
	//========================= Public Methods =================================
	
	/**
	 * Will convert all of the Source Values to {@code String}s.
	 *
	 * @return A reference to this object, for method call caining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator convertSourceValuesToString() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		for(int i = 0; i < SOURCE_VALUES.size(); i++) {
			SOURCE_VALUES.set(i, String.valueOf(SOURCE_VALUES.get(i)));
		}
		
		return this;
	}
	
	/**
	 * Will convert all of the Destination Values to {@code String}s.
	 *
	 * @return A reference to this object, for method call caining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMappingValidator convertDestinationValuesToString() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		for(int i = 0; i < DESTINATION_VALUES.size(); i++) {
			DESTINATION_VALUES.set(i, String.valueOf(DESTINATION_VALUES.get(i)));
		}
		
		return this;
	}
	
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
	 * This will set a custom Comparator, to be used instead of {@code .equals(...)} when compairing a Source and Destination Value.
	 *
	 * @param _Comparator The {@link DataMappingComparator} to use instead of {@code .equals}.
	 *
	 * @return A reference to this {@link DataMappingValidator}, for method call chaining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public DataMappingValidator setComparator(DataMappingComparator<T> _Comparator) {
		customComparator = _Comparator;
		COLLECTION_MAPPING_BACKER.setComparator(_Comparator);
		return this;
	}
	
	@Override
	public String validate() {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		return COLLECTION_MAPPING_BACKER.validate();
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private static class TablesColumnPair {
		
		private final Collection<? extends SqlPojo> TABLE_ROWS;
		private final SqlPojo.RowMapperColumnEnum COLUMN;
		
		/**
		 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
		 */
		private TablesColumnPair(Collection<? extends SqlPojo> _tables, SqlPojo.RowMapperColumnEnum _column) {
			TABLE_ROWS = _tables;
			COLUMN = _column;
		}
		
		@Override
		public boolean equals(Object obj) {
			
			if(obj == null) {
				return false;
			}
			
			if(!(obj instanceof SqlPojoCollectionMappingValidator.TablesColumnPair)) {
				return false;
			}
			
			TablesColumnPair other = (TablesColumnPair) obj;
			
			return COLUMN == other.COLUMN && Objects.equals(TABLE_ROWS, other.TABLE_ROWS);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(TABLE_ROWS, COLUMN);
		}
	}
	
	// TODO: Create a Builder class.
}
