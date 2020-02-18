package xyz.swatt.data_mapping_validator;

import xyz.swatt.log.LogMethods;

import java.util.Set;

/**
 * This interface is for all Data Mapping Classes that compare 2 values of the same type.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public interface DataMappingValidator<T> {
	
	//========================= Static Enums ===================================
	
	/**
	 * Every Implementing Class should have a "Column" enum than implements this interface.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static interface DataMappingFlagEnum {}
	
	//========================= STATIC CONSTANTS ===============================

    //========================= Static Variables ===============================

    //========================= Static Methods =================================
    /**
     * Will Create a formatted Error {@link String} for a given set of values, to be used if the validation fails.
     *
     * @param _sourceValue
     *         The Source Value that is being compared against.
     * @param _destinationValue
     *         The Source Value that is being compared to.
     *
     * @return The created Error {@link String}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public static String createFormattedErrorString(Object _sourceValue, Object _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        return "Source does not Equal Destination!\n\tSource      : " + _sourceValue + "\n\tDestination : " + _destinationValue;
    }
	
	/**
	 * Will determine how many of the given Arguments are {@code null}.
	 *
	 * @param _values
	 * 		The Arguments to check.
	 *
	 * @return A number that indecates how many of the given Arguments are {@code null}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static int nullCount(Object... _values) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		int count = 0;
		
		//------------------------ Code ----------------------------------------
		if(_values != null) {
			for(Object value : _values) {
				if(value == null) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	//========================= CONSTANTS ======================================
	
	//========================= Methods ========================================
	
	/**
	 * @return All Flags on this Mapping or {@code null} / empty list, if there are no mappings.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public Set<? extends DataMappingFlagEnum> getDataMappingFlags();
	
	/**
	 * @return The Set or Generated Name of this {@link DataMappingValidator}; or {@code null}, if not set.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String getMappingName();
	
	/**
	 * This will set a custom Comparator, to be used instead of {@code .equals(...)} when compareing a Source and Destination Value.
	 *
	 * @param _Comparator
	 * 		The {@link DataMappingComparator} to use instead of {@code .equals}.
	 *
	 * @return A reference to this {@link DataMappingValidator}, for method call chaining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DataMappingValidator setComparator(DataMappingComparator<T> _Comparator);
	
	/**
	 * Will give this {@link DataMappingValidator} a custom name.
	 *
	 * @param _name
	 * 		The Name to set, for this {@link DataMappingValidator}.
	 *
	 * @return A reference to self is returned for method call chaining.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public DataMappingValidator setMappingName(String _name);
	
	/**
	 * This method compares the two, previously set, values.
	 *
	 * @return A String with any differences found, or {@code NULL}, if there were no differences.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public String validate();
	
	//========================= Classes ========================================
	
	/**
	 * Every Implementing Class should have a Builder, than implements this interface.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static interface DataMappingBuilder {}
	
	/**
	 * Provides a means for custom data comparison.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public static abstract class DataMappingComparator<T> {
		
		/**
		 * A custom Comparator that will be called by the {@link DataMappingValidator}.
		 * <p>
		 *     <i>Note:</i> Not all {@link DataMappingValidator} Flags may be applied, before this method is called.
		 * </p>
		 *
		 * @param _dataMappingValidator The {@link DataMappingValidator} that the Source and Distionation values are from.
		 * @param _source The Source value that is being compared.
		 * @param _destination The Distionation value that is being compared against.
		 *
		 * @return {@code true}, if the 2 values should be considered equal; otherwise, {@code false}.
		 */
		public abstract boolean compare(DataMappingValidator _dataMappingValidator, T _source, T _destination);
	}
}
