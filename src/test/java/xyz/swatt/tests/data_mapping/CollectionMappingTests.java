/*
 * Created on 2019-03-29 by bdudek for {swatt}.
 */
package xyz.swatt.tests.data_mapping;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xyz.swatt.data_mapping_validator.CollectionMappingValidator;
import xyz.swatt.data_mapping_validator.DataMappingValidator;
import xyz.swatt.log.LogMethods;

import java.util.*;

/**
 * Will test all of the {@link DataMappingValidator} classes.
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
    public Iterator<Object[]> customComparatorData(ITestContext _iTestContext) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        List<Object[]> data = new LinkedList<>();
        
        //------------------------ Code ----------------------------------------
        final String mappingName = "Custom BigDecimal";
        final List<String> list1 = Arrays.asList("A");
        final List<String> list2 = Arrays.asList("B");
        final CollectionMappingValidator.MappingFlag flag = CollectionMappingValidator.MappingFlag.ORDER_MATTERS;
        data.add(new Object[] {new CollectionMappingValidator(mappingName, list1, list2, flag)
                .setComparator(new DataMappingValidator.DataMappingComparator<String>() {
            @Override
            public boolean compare(DataMappingValidator _dataMappingValidator, String _source, String _destination) {
                boolean isEqual = _dataMappingValidator.getMappingName().equals(mappingName);
                isEqual &= _source.equals(list1.get(0));
                isEqual &= _destination.equals(list2.get(0));
                isEqual &= _dataMappingValidator.getDataMappingFlags().contains(flag);
                return isEqual;
            }
        })
        });
        
        return data.iterator();
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "customComparatorData")
    public void customComparatorTest(DataMappingValidator _dataMappingValidator) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        String errors;
        
        //------------------------ Code ----------------------------------------
        if(StringUtils.isNotBlank(errors = _dataMappingValidator.validate())) {
            Assert.fail(errors);
        }
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] objectData(ITestContext _iTestContext) {
        return new Object[][] {
                {null, null, new CollectionMappingValidator.MappingFlag[] {}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4), new CollectionMappingValidator.MappingFlag[] {}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(3, 4, 2, 1), new CollectionMappingValidator.MappingFlag[] {}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, 1), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.IGNORE_DUPLICATES}},
                {Arrays.asList(1, 1, 2, 3, 4), Arrays.asList(1, 2, 3, 4), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.IGNORE_DUPLICATES}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, null), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.IGNORE_NULLS}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.SOURCE_CONTAINS_DESTINATION}},
                {Arrays.asList(1, 2), Arrays.asList(1, 2, 3, 4), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.DESTINATION_CONTAINS_SOURCE}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectData")
    public void objectTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues, CollectionMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        CollectionMappingValidator mapper = new CollectionMappingValidator(_sourceValues, _destinationValues, _flags);
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
        return new Object[][] {
        
                {null, Arrays.asList(1), new CollectionMappingValidator.MappingFlag[] {}},
                {Arrays.asList(0), null, new CollectionMappingValidator.MappingFlag[] {}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, 1), new CollectionMappingValidator.MappingFlag[] {/*CollectionMapping.MappingFlag.IGNORE_DUPLICATES*/}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3, 4, null), new CollectionMappingValidator.MappingFlag[] {/*CollectionMapping.MappingFlag.IGNORE_NULLS*/}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(3, 4, 2, 1), new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.ORDER_MATTERS}},
                {Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 3), new CollectionMappingValidator.MappingFlag[] {/*CollectionMapping.MappingFlag.SOURCE_CONTAINS_DESTINATION*/}},
                {Arrays.asList(1, 2), Arrays.asList(1, 2, 3, 4), new CollectionMappingValidator.MappingFlag[] {/*CollectionMapping.MappingFlag.DESTINATION_CONTAINS_SOURCE*/}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectNegativeData")
    public void objectNegativeTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues, CollectionMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        CollectionMappingValidator mapper = new CollectionMappingValidator(_sourceValues, _destinationValues, _flags);
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
        return new Object[][] {
                {Arrays.asList(1, 2), Arrays.asList(1, 2),
                        new CollectionMappingValidator.MappingFlag[] {CollectionMappingValidator.MappingFlag.SOURCE_CONTAINS_DESTINATION, CollectionMappingValidator.MappingFlag.DESTINATION_CONTAINS_SOURCE}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectNegativeArgumentsData", expectedExceptions = IllegalArgumentException.class)
    public void objectNegativeArgumentsTest(Collection<Object> _sourceValues, Collection<Object> _destinationValues,
                                            CollectionMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        CollectionMappingValidator mapper = new CollectionMappingValidator(_sourceValues, _destinationValues, _flags);
    }
    
    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
