package xyz.swatt.data_mapping_validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

/**
 * This is an Abstract Implementation of the DataMapping Interface. All Mapping Validators sould extent this.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public abstract class AbstractDataMapping<T> implements DataMappingValidator<T> {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(AbstractDataMapping.class);
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Public Static Methods ==========================
	
	//========================= Helper Static Methods ==========================
	
	//========================= CONSTANTS ======================================
	
	//========================= Variables ======================================
	DataMappingComparator customComparator;
	
	/**
	 * The name that was given to this mapping.
	 * <p>
	 * <i>Note:</i> This name is optional and may be {@code null}.
	 * </p>
	 */
	String mappingName;
	
	//========================= Constructors ===================================
	
	//========================= Public Methods =================================
	
	/**
	 * @return The Set or Generated Name of this Mapping; or {@code null}, if not set.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public String getMappingName() {
		return mappingName;
	}
	
	/**
	 * This will set a custom Comparator, to be used instead of {@code .equals(...)} when compairing a Source and Destination Value.
	 *
	 * @param _Comparator
	 * 		The {@link DataMappingComparator} to use instead of {@code .equals}.
	 *
	 * @return A reference to this {@link DataMappingValidator}, for method call chaining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public DataMappingValidator setComparator(DataMappingComparator<T> _Comparator) {
		customComparator = _Comparator;
		return this;
	}
	
	/**
	 * @param _name
	 * 		The Name to set, for this Mapping.
	 *
	 * @return A reference to self is returned for method call chaining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Override
	public DataMappingValidator setMappingName(String _name) {
		mappingName = StringUtils.isBlank(_name) ? null : StringHelper.trim(_name);
		return this;
	}
	
	@Override
	public String toString() {
		return mappingName != null ? mappingName : super.toString();
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
}
