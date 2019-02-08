/*
 * Created on 2019-02-05 by Brandon Dudek &lt;bdudek@paychex.com&gt; for {swatt}.
 */
package xyz.swatt.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.jdbc.core.RowMapper;
import xyz.swatt.asserts.ArgumentChecks;

import java.util.*;

/**
 * This interface defines a common structure for working with SQL DB POJOs.
 *
 * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
 */
public interface SqlPojo<T> extends RowMapper<T> {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================

    //========================= Static Methods =================================

    /**
     * Will compare two {@link SqlPojo} Collections and determine the differences between them.
     *
     * @param _sqlPojos1
     *         The First {@link SqlPojo} Collection to compare against the Second.
     * @param _sqlPojos2
     *         The Second {@link SqlPojo} Collection to compare against the First.
     * @param _ignoreColumnIndexes
     *         An array of 0-based column indexes to ignore in this comparison.
     *
     * @return A Collection of differences, as descriptive {@link String}s or an empty collection, if there are no differences.
     *
     * @throws IllegalArgumentException
     *         If either of the given {@link SqlPojo} Collection are {@code null}!
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    public static List<String> determineDifferences(final Collection<SqlPojo> _sqlPojos1, final Collection<SqlPojo> _sqlPojos2, int... _ignoreColumnIndexes) {

        //------------------------ Pre-Checks ----------------------------------
        ArgumentChecks.notNull(_sqlPojos1, "First SqlPojo Collection");
        ArgumentChecks.notNull(_sqlPojos2, "Second SqlPojo Collection");

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        List<SqlPojo> sqlPojos2Copy = new ArrayList<>(_sqlPojos2);
        List<String> diffs = new ArrayList<>();

        //------------------------ Code ----------------------------------------
        ///// Check Sizes /////
        if(_sqlPojos1.size() != _sqlPojos2.size()) {
            diffs.add("Collection Sizes do not match! [Collection1 (" + _sqlPojos1.size() + ") != Collection2 (" + _sqlPojos2.size() + ")]");
        }

        ///// Check Collection 1 /////
        for(SqlPojo row1 : _sqlPojos1) {

            boolean match = false;
            for(SqlPojo row2 : sqlPojos2Copy) {

                if(row1.determineDifferences(row2, _ignoreColumnIndexes).isEmpty()) {

                    sqlPojos2Copy.remove(row2);
                    match = true;
                    break;
                }
            }

            if(!match) {
                diffs.add("Collection 1 row could not be found in Collection 2: " + ToStringBuilder.reflectionToString(row1, ToStringStyle.SHORT_PREFIX_STYLE));
            }
        }

        ///// Report Collection 2 Leftovers /////
        for(SqlPojo row2 : sqlPojos2Copy) {
            diffs.add("Collection 2 row could not be found in Collection 1: " + ToStringBuilder.reflectionToString(row2, ToStringStyle.SHORT_PREFIX_STYLE));
        }

        return diffs;
    }

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================
    /**
     * An array of 0-based column indexes to ignore, that can be set at the object level.
     */
    public HashSet<Integer> differencesSkipColumns = new HashSet<>();

    //========================= Constructors ===================================

    //========================= Public Methods =================================

    /**
     * Will compare this object to two another {@link SqlPojo} objects and determine the differences between them.
     *
     * @param _otherObject
     *         The object to compare this one against.
     * @param _ignoreColumnIndexes
     *         An array of 0-based column indexes to ignore in this comparison.
     *
     * @return A Collection of differences, as descriptive {@link String}s or an empty collection, if there are no differences.
     *
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    public Set<String> determineDifferences(SqlPojo _otherObject, int... _ignoreColumnIndexes);

    /**
     * Will return the column value for the given column index.
     *
     * @param _columnIndex
     *         The 0-based index of the desired column.
     *
     * @return The value of the desired column as an {@link Object}.
     *
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    public Object getColumn(int _columnIndex);

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
