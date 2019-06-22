/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.pojo.SqlPojo;
import xyz.swatt.string.StringHelper;

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
public class SqlPojoCollectionMapping<T> implements DataMapping {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(SqlPojoCollectionMapping.class);
	
	/**
	 * Sets {@link CollectionMapping.MappingFlag}s to be used for all {@link SqlPojoCollectionMapping}s.
	 */
	public static final EnumSet<CollectionMapping.MappingFlag> GLOBAL_MAPPING_FLAGS = EnumSet.noneOf(CollectionMapping.MappingFlag.class);
	
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
	
	public T tTypeObject;
	
	/**
	 * The {@link CollectionMapping} object that this one is backed by.
	 */
	public final CollectionMapping<T> COLLECTION_MAPPING_BACKER;
	
	/**
	 * The values being compared.
	 */
	public final List<T> SOURCE_VALUES, DESTINATION_VALUES;
	
	/**
	 * The {@link CollectionMapping.MappingFlag}s that are applied to this {@link SqlPojoCollectionMapping} object. (read only)
	 */
	public final Set<CollectionMapping.MappingFlag> MAPPING_FLAGS;
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _sourceValue
	 * 		The value from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(T _sourceValue,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceValue), null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _sourceValues
	 * 		The values from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(List<T> _sourceValues,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, _sourceValues, null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
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
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceTable), _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValue
	 * 		The mapped value found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                T _destinationValue,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, null, Arrays.asList(_destinationValue), _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValues
	 * 		The mapped values found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                List<T> _destinationValues,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, null, _destinationValues, _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
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
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, _sourceTables, _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
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
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, _sourceTables, _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceValue
	 * 		The value from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                T _sourceValue,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, Arrays.asList(_sourceValue), null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceValues
	 * 		The values from the Source Data.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                List<T> _sourceValues,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, _sourceValues, null, null, null, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceTable
	 * 		The Source Table to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                SqlPojo _sourceTable, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(_mappingName, Arrays.asList(_sourceTable), _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValue
	 * 		The mapped value found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                T _destinationValue,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, null, Arrays.asList(_destinationValue), _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationValues
	 * 		The mapped values found in the Destination Data.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                List<T> _destinationValues,
	                                CollectionMapping.MappingFlag... _flags) {
		this(null, null, _destinationValues, _sourceTables, _sourceColumn, null, null, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTable
	 * 		The Destination Table to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                SqlPojo _destinationTable, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(_mappingName, _sourceTables, _sourceColumn, Arrays.asList(_destinationTable), _destinationColumn, _flags);
	}
	
	/**
	 * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
	 *
	 * @param _mappingName
	 * 		An optional, unique name to give this {@link SqlPojoCollectionMapping}.
	 * @param _sourceTables
	 * 		The Source Tables to pull the Column Value from.
	 * @param _sourceColumn
	 * 		The Source Column to pull the Value from.
	 * @param _destinationTables
	 * 		The Destination Tables to pull the Column Value from.
	 * @param _destinationColumn
	 * 		The Destination Column to pull the Value from.
	 * @param _flags
	 * 		Any {@link CollectionMapping.MappingFlag}s that should be applied to this {@link SqlPojoCollectionMapping}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public SqlPojoCollectionMapping(String _mappingName,
	                                Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                CollectionMapping.MappingFlag... _flags) {
		this(_mappingName, null, null, _sourceTables, _sourceColumn, _destinationTables, _destinationColumn, _flags);
	}
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	private SqlPojoCollectionMapping(String _mappingName, Collection<T> _sourceValues, Collection<T> _destinationValues,
	                                 Collection<? extends SqlPojo> _sourceTables, SqlPojo.RowMapperColumnEnum _sourceColumn,
	                                 Collection<? extends SqlPojo> _destinationTables, SqlPojo.RowMapperColumnEnum _destinationColumn,
	                                 CollectionMapping.MappingFlag... _flags) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		String mappingName = "";
		EnumSet<CollectionMapping.MappingFlag> flags = EnumSet.noneOf(CollectionMapping.MappingFlag.class);
		
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
			
			MAPPING_NAME = mappingName;
		}
		else {
			MAPPING_NAME = StringHelper.trim(_mappingName); // Allowing empty string, because when NULL is passes in, default name generation is used.
		}
		
		///// Values /////
		if(_sourceValues == null) {
			_sourceValues = new ArrayList(_sourceTables.size());
			for(SqlPojo sourceRow : _sourceTables) {
				_sourceValues.add((T) sourceRow.getColumnValue(_sourceColumn));
			}
		}
		SOURCE_VALUES = Collections.unmodifiableList(new ArrayList(_sourceValues));
		
		if(_destinationValues == null) {
			_destinationValues = new ArrayList(_destinationTables.size());
			for(SqlPojo destinationRow : _destinationTables) {
				_destinationValues.add((T) destinationRow.getColumnValue(_destinationColumn));
			}
		}
		DESTINATION_VALUES = Collections.unmodifiableList(new ArrayList(_destinationValues));
		
		///// Flages /////
		flags.addAll(GLOBAL_MAPPING_FLAGS);
		if(_flags != null && _flags.length > 0) {
			flags.addAll(Arrays.asList(_flags));
		}
		MAPPING_FLAGS = Collections.unmodifiableSet(flags);
		// Check for Mapping Flag Conflicts. (Done in CollectionMapping.)
		
		///// Backer /////
		COLLECTION_MAPPING_BACKER = new CollectionMapping(MAPPING_NAME, SOURCE_VALUES, DESTINATION_VALUES,
				MAPPING_FLAGS.toArray(new CollectionMapping.MappingFlag[] {}));
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
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		return COLLECTION_MAPPING_BACKER.validate();
	}
	
	@Override
	public String toString() {
		return MAPPING_NAME != null ? MAPPING_NAME : super.toString();
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
}
