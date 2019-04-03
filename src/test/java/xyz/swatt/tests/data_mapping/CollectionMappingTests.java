/*
 * Created on 2019-03-29 by bdudek for {swatt}.
 */
package xyz.swatt.tests.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xyz.swatt.data_mapping.CollectionMapping;
import xyz.swatt.log.LogMethods;

import java.util.Arrays;
import java.util.Collection;

/**
 * Will test all of the {@link xyz.swatt.data_mapping.DataMapping} classes.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class CollectionMappingTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(CollectionMappingTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Public Methods =================================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] objectData(ITestContext _iTestContext) {
        return new Object[][]{
                {null, null, new CollectionMapping.MappingFlag[]{}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4), new CollectionMapping.MappingFlag[]{}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(3, 4, 2, 1), new CollectionMapping.MappingFlag[]{}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, 1), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.IGNORE_DUPLICATES}},
                {Arrays.asList(1, 1, 2, 3, 4), Arrays.asList(1, 2, 3, 4), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.IGNORE_DUPLICATES}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, null), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.IGNORE_NULLS}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.SOURCE_CONTAINS_DESTINATION}},
                {Arrays.asList(1, 2), Arrays.asList(1, 2, 3, 4), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.DESTINATION_CONTAINS_SOURCE}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectData")
    public void objectTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues, CollectionMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        CollectionMapping mapper = new CollectionMapping(_sourceValues, _destinationValues, _flags);
        String differences = mapper.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] objectNegativeData(ITestContext _iTestContext) {
        return new Object[][]{

                {null, Arrays.asList(1), new CollectionMapping.MappingFlag[]{}},
                {Arrays.asList(0), null, new CollectionMapping.MappingFlag[]{}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, 1), new CollectionMapping.MappingFlag[]{/*CollectionMapping.MappingFlag.IGNORE_DUPLICATES*/}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, null), new CollectionMapping.MappingFlag[]{/*CollectionMapping.MappingFlag.IGNORE_NULLS*/}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(3, 4, 2, 1), new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.ORDER_MATTERS}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3), new CollectionMapping.MappingFlag[]{/*CollectionMapping.MappingFlag.SOURCE_CONTAINS_DESTINATION*/}},
                {Arrays.asList(1, 2), Arrays.asList(1, 2, 3, 4), new CollectionMapping.MappingFlag[]{/*CollectionMapping.MappingFlag.DESTINATION_CONTAINS_SOURCE*/}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectNegativeData")
    public void objectNegativeTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues, CollectionMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        CollectionMapping mapper = new CollectionMapping(_sourceValues, _destinationValues, _flags);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("There should have been differences!\n- " + _sourceValues + "\n- " + _destinationValues);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] objectNegativeArgumentsData(ITestContext _iTestContext) {
        return new Object[][]{
                {Arrays.asList(1, 2), Arrays.asList(1, 2),
                        new CollectionMapping.MappingFlag[]{CollectionMapping.MappingFlag.SOURCE_CONTAINS_DESTINATION, CollectionMapping.MappingFlag.DESTINATION_CONTAINS_SOURCE}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectNegativeArgumentsData", expectedExceptions = IllegalArgumentException.class)
    public void objectNegativeArgumentsTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues, CollectionMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        CollectionMapping mapper = new CollectionMapping(_sourceValues, _destinationValues, _flags);
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}