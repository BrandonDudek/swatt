/*
 * Created on 2019-03-08 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;
import xyz.swatt.string.StringHelper;

import java.util.*;

/**
 * Use this class to validate that 2 different {@link Collection}s of {@link Object}s are equal, By using the Object's build in {@code equals(Object)} method.
 * <p>
 * <i>Note:</i> This class is used instead of direct comparison, to simplify large batches of comparisons, and to provide common functionality.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class CollectionMapping<T> implements DataMapping {

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
         * If Source and Destination lists should be made {@code distinct} before comparing them.
         */
        IGNORE_DUPLICATES,

        /**
         * If {@code null} entries in the {@link Collection}s should be skipped.
         */
        IGNORE_NULLS,

        /**
         * Will ensure that the two {@link Collection}s' Values are Equal and in the Same Order.
         */
        ORDER_MATTERS,

        /**
         * If the Source list is expected to be a super-set of the Destination list.
         * <p>
         * <b>Note:</b> Cannot be used with {@link #DESTINATION_CONTAINS_SOURCE}.
         * </p>
         */
        SOURCE_CONTAINS_DESTINATION,

        /**
         * If the Destination list is expected to be a super-set of the Source list.
         * <p>
         * <b>Note:</b> Cannot be used with {@link #SOURCE_CONTAINS_DESTINATION}.
         * </p>
         */
        DESTINATION_CONTAINS_SOURCE,
    }

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(CollectionMapping.class);

    /**
     * Sets {@link MappingFlag}s to be used for all {@link CollectionMapping}s.
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
     * The values being compared.
     */
    public final Collection<T> SOURCE_VALUES, DESTINATION_VALUES;

    /**
     * The {@link MappingFlag}s that are applied to this {@link CollectionMapping} object. (read only)
     */
    public final Set<MappingFlag> MAPPING_FLAGS;

    //========================= Variables ======================================

    //========================= Constructors ===================================
    /**
     * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
     *
     * @param _sourceValues
     *         The values from the Source Data.
     * @param _destinationValues
     *         The mapped values found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link CollectionMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public CollectionMapping(Collection<T> _sourceValues, Collection<T> _destinationValues, MappingFlag... _flags) {
        this(null, _sourceValues, _destinationValues, _flags);
    }

    /**
     * Creates a new {@link Collection}-to-{@link Collection} {@link DataMapping} object.
     *
     * @param _mappingName
     *         An optional, unique name to give this {@link CollectionMapping}.
     * @param _sourceValues
     *         The values from the Source Data.
     * @param _destinationValues
     *         The mapped values found in the Destination Data.
     * @param _flags
     *         Any {@link MappingFlag}s that should be applied to this {@link CollectionMapping}.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public CollectionMapping(String _mappingName, Collection<T> _sourceValues, Collection<T> _destinationValues, MappingFlag... _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        EnumSet<MappingFlag> flags = EnumSet.noneOf(MappingFlag.class);

        //------------------------ Code ----------------------------------------
        MAPPING_NAME = _mappingName == null || StringHelper.removeWhitespace(_mappingName).isEmpty() ? null : StringHelper.trim(_mappingName);
        SOURCE_VALUES = _sourceValues;
        DESTINATION_VALUES = _destinationValues;

        flags.addAll(GLOBAL_MAPPING_FLAGS);
        if(_flags != null && _flags.length > 0) {
            flags.addAll(Arrays.asList(_flags));
        }
        MAPPING_FLAGS = Collections.unmodifiableSet(flags);

        ///// Check for Mapping Flag Conflicts /////
        if(MAPPING_FLAGS.containsAll(Arrays.asList(MappingFlag.SOURCE_CONTAINS_DESTINATION, MappingFlag.DESTINATION_CONTAINS_SOURCE))) {
            throw new IllegalArgumentException("Cannot use both SOURCE_CONTAINS_DESTINATION & DESTINATION_CONTAINS_SOURCE flags in the same CollectionMapping!");
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

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        Collection<T> sourceValues = SOURCE_VALUES, destinationValues = DESTINATION_VALUES;

        //------------------------ Code ----------------------------------------
        ////////// Handle NULLs /////
        if(sourceValues == null || destinationValues == null) {
            if(sourceValues == destinationValues) {
                return null; // Equal.
            }
            else if(sourceValues == null) {
                return "Source collection is NULL, but Destination collection contains " + destinationValues.size() + " entries!";
            }
            else {
                return "Destination collection is NULL, but Source collection contains " + sourceValues.size() + " entries!";
            }
        }

        ////// Handle Flags /////
        if(MAPPING_FLAGS.contains(MappingFlag.IGNORE_DUPLICATES)) {
            sourceValues = new HashSet<>(sourceValues);
            destinationValues = new HashSet<>(destinationValues);
        }
        else { // Make shallow copies fo collections before manipulating them.
            sourceValues = new ArrayList<>(sourceValues);
            destinationValues = new ArrayList<>(destinationValues);
        }

        if(MAPPING_FLAGS.contains(MappingFlag.IGNORE_NULLS)) {
            sourceValues.removeAll(Collections.singleton(null));
            destinationValues.removeAll(Collections.singleton(null));
        }

        ////// Compare /////
        List<String> errors = new LinkedList();

        if(MAPPING_FLAGS.contains(MappingFlag.ORDER_MATTERS)) {

            int index = 0;
            T sourceEntry, destinationEntry;
            Iterator<T> sourceIterator = sourceValues.iterator(), destinationIterator = destinationValues.iterator();

            while(sourceIterator.hasNext() || destinationIterator.hasNext()) {

                sourceEntry = sourceIterator.hasNext() ? sourceIterator.next() : null;
                destinationEntry = destinationIterator.hasNext() ? destinationIterator.next() : null;

                if(!Objects.equals(sourceEntry, destinationEntry)) {

                    errors.add("Entry " + index + " in Source Collection is: " + sourceEntry);
                    errors.add("Entry " + index + " in Destination Collection is: " + destinationEntry);
                }

                if(MAPPING_FLAGS.contains(MappingFlag.SOURCE_CONTAINS_DESTINATION) && !destinationIterator.hasNext()) {
                    break;
                }
                if(MAPPING_FLAGS.contains(MappingFlag.DESTINATION_CONTAINS_SOURCE) && !sourceIterator.hasNext()) {
                    break;
                }

                index++;
            }
        }
        else {
            if(!MAPPING_FLAGS.contains(MappingFlag.SOURCE_CONTAINS_DESTINATION)) {
                for(T sourceEntry : sourceValues) {
                    if(!destinationValues.remove(sourceEntry)) {
                        errors.add("Destination Collection does not contain Source Entry: " + sourceEntry);
                    }
                }
            }

            if(!MAPPING_FLAGS.contains(MappingFlag.DESTINATION_CONTAINS_SOURCE)) {

                for(T destinationEntry : destinationValues) {

                    if(MAPPING_FLAGS.contains(MappingFlag.SOURCE_CONTAINS_DESTINATION)) { // Source Values was never tested.

                        if(!sourceValues.remove(destinationEntry)) { // Test against Source Values now.
                            errors.add("Source Collection does not contain Destination Entry: " + destinationEntry);
                        }
                    }
                    else { // Source Values were already tested, any Values left in Destination are errors.
                        errors.add("Source Collection does not contain Destination Entry: " + destinationEntry);
                    }
                }
            }
        }

        ////// Return /////
        if(errors.isEmpty()) {
            return null;
        }
        else {
            return String.join("\n", errors);
        }
    }

    @Override
    public String toString() {
        return MAPPING_NAME != null ? MAPPING_NAME : super.toString();
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
