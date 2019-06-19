package xyz.swatt.data_mapping;

/**
 * This interface is for all Data Mapping Classes that compare 2 values of the same type.
 */
public interface DataMapping {

    //========================= Static Enums ===================================
	
	/**
	 * Every Implementing Class should have a "Column" enum than implements this interface.
	 */
	public static interface DataMappingFlagEnum {
	}

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
     * Will Create a formatted Error {@link String} for a given set of values, to be used if the validation fails.
     *
     * @param _sourceValue
     *         The Source Value that is being compared against.
     * @param _destinationValue
     *         The Source Value that is being compared to.
     *
     * @return {@code false} if both are {@code null}, {@code false} if just one is {@code null}, and {@code null} if neither are {@code null}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    static Boolean isOneNullButNotBoth(Object _sourceValue, Object _destinationValue) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        if(_sourceValue == null || _destinationValue == null) {
            // Not Equal.
            return _sourceValue != _destinationValue; // Equal.
        }

        return null; // Neither are NULL.
    }

    //========================= Methods ========================================

    /**
     * This method compares the two, previously set, values.
     *
     * @return A String with any differences found, or {@code NULL}, if there were no differences.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public String validate();
}
