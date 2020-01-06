/*
 * Created on 2019-03-25 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.tests.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xyz.swatt.data_mapping_validator.*;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Will test all of the {@link DataMappingValidator} classes.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class DataMappingTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(DataMappingTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Public Methods =================================
    ////////// BigDecimal //////////
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] bigDecimalData(ITestContext _iTestContext) {
        return new Object[][] {
                {null, null, new BigDecimalMappingValidator.MappingFlag[] {}},
                {new BigDecimal("123.45"), BigDecimal.valueOf(123.45), new BigDecimalMappingValidator.MappingFlag[] {}},
                {new BigDecimal("123.4"), BigDecimal.valueOf(123.4F).setScale(1, BigDecimal.ROUND_HALF_UP), new BigDecimalMappingValidator.MappingFlag[] {}},
                {new BigDecimal("123.450"), new BigDecimal("123.450"), new BigDecimalMappingValidator.MappingFlag[] {}},
                {new BigDecimal("123.450"), new BigDecimal("123.45"), new BigDecimalMappingValidator.MappingFlag[] {BigDecimalMappingValidator.MappingFlag.IGNORE_PRECISION}},
                {new BigDecimal("123"), new BigDecimal("123.000"), new BigDecimalMappingValidator.MappingFlag[] {BigDecimalMappingValidator.MappingFlag.IGNORE_PRECISION}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "bigDecimalData")
    public void bigDecimalTest(BigDecimal _source, BigDecimal _destination, BigDecimalMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        BigDecimalMappingValidator mapper = new BigDecimalMappingValidator(_source, _destination, _flags);
        String differences = mapper.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] bigDecimalNegativeData(ITestContext _iTestContext) {
        return new Object[][] {
        
                {null, BigDecimal.valueOf(1), new BigDecimalMappingValidator.MappingFlag[] {}},
                {new BigDecimal("123.450"), new BigDecimal("123.45"), new BigDecimalMappingValidator.MappingFlag[] {}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "bigDecimalNegativeData")
    public void bigDecimalNegativeTest(BigDecimal _source, BigDecimal _destination, BigDecimalMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        String expectedError = DataMappingValidator.createFormattedErrorString(_source, _destination);
        
        //------------------------ Code ----------------------------------------
        BigDecimalMappingValidator mapper = new BigDecimalMappingValidator(_source, _destination, _flags);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("No Differences Found!");
        }
        else if(!differences.equals(expectedError)) {
            throw new RuntimeException("Differences:\n" + differences + "\n\nExpected Error:\n" + expectedError);
        }
    }
    
    ////////// DateTime //////////
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] dateTimeData(ITestContext _iTestContext) {
        
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = localDateTime.toLocalTime();
        
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        
        Date javaDate = Date.from(instant);
        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        
        ZonedDateTime localZonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
        
        OffsetDateTime localOffsetDateTime = localZonedDateTime.toOffsetDateTime();
        OffsetDateTime utcOffsetDateTime = utcZonedDateTime.toOffsetDateTime();
        
        OffsetTime localOffsetTime = localOffsetDateTime.toOffsetTime();
        OffsetTime utcOffsetTime = utcOffsetDateTime.toOffsetTime();
        
        return new Object[][] {
                
                ///// NULL to NULL /////
                {"NULL (Temporal) to NULL (Temporal)", new DateTimeMappingValidator((Temporal) null, (Temporal) null)},
                {"NULL (Date) to NULL (Date)", new DateTimeMappingValidator((Date) null, (Date) null)},
                {"NULL (Date) to NULL (Temporal)", new DateTimeMappingValidator((Date) null, (Temporal) null)},
                {"NULL (Temporal) to NULL (Date)", new DateTimeMappingValidator((Temporal) null, (Date) null)},
                
                ///// LocalDateTime to X /////
                {"LocalDateTime to LocalDateTime", new DateTimeMappingValidator(localDateTime, LocalDateTime.from(localDateTime))},
                {"LocalDateTime to LocalDate", new DateTimeMappingValidator(localDateTime, localDate, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"LocalDateTime to JavaDate", new DateTimeMappingValidator(localDateTime, javaDate)},
                {"LocalDateTime to SqlDate", new DateTimeMappingValidator(localDateTime, sqlDate, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"LocalDateTime to LocalTime", new DateTimeMappingValidator(localDateTime, localTime, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                {"LocalDateTime to Timestamp", new DateTimeMappingValidator(localDateTime, timestamp)},
                {"LocalDateTime to Instant", new DateTimeMappingValidator(localDateTime, instant)},
                
                ///// LocalDate to X /////
                {"LocalDate to LocalDate", new DateTimeMappingValidator(localDate, LocalDate.from(localDate))},
                {"LocalDate to JavaDate", new DateTimeMappingValidator(localDate, javaDate, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"LocalDate to SqlDate", new DateTimeMappingValidator(localDate, sqlDate, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"LocalDate to Timestamp", new DateTimeMappingValidator(localDate, timestamp, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"LocalDate to Instant", new DateTimeMappingValidator(localDate, instant, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                
                ///// LocalTime to X /////
                {"LocalTime to LocalTime", new DateTimeMappingValidator(localTime, LocalTime.from(localTime))},
                {"LocalTime to JavaDate", new DateTimeMappingValidator(localTime, javaDate, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                {"LocalTime to Timestamp", new DateTimeMappingValidator(localTime, timestamp, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                {"LocalTime to Instant", new DateTimeMappingValidator(localTime, instant, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                
                ///// JavaDate to X ///// (JavaDate contains Time)
                {"JavaDate to JavaDate", new DateTimeMappingValidator(javaDate, Date.from(javaDate.toInstant()))},
                {"JavaDate to SqlDate", new DateTimeMappingValidator(javaDate, sqlDate, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"JavaDate to Timestamp", new DateTimeMappingValidator(javaDate, timestamp)},
                {"JavaDate to Instant", new DateTimeMappingValidator(javaDate, instant)},
                
                ///// SqlDate to X ///// (JavaDate contains Time, but it is always 00:00:00.000)
                {"SqlDate to SqlDate", new DateTimeMappingValidator(sqlDate, java.sql.Date.valueOf(localDate))},
                {"SqlDate to Timestamp", new DateTimeMappingValidator(sqlDate, timestamp, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                {"SqlDate to Instant", new DateTimeMappingValidator(sqlDate, instant, DateTimeMappingValidator.MappingFlag.IGNORE_TIME)},
                
                ///// Timestamp to X /////
                {"Timestamp to Timestamp", new DateTimeMappingValidator(timestamp, Timestamp.from(timestamp.toInstant()))},
                {"Timestamp to Instant", new DateTimeMappingValidator(timestamp, instant)},
                
                ///// ZonedDateTime to X /////
                {"ZonedDateTime: LOCAL to UTC", new DateTimeMappingValidator(localZonedDateTime, utcZonedDateTime)},
                {"ZonedDateTime: LOCAL to LocalDateTime", new DateTimeMappingValidator(localZonedDateTime, localDateTime)},
                {"ZonedDateTime: UTC to LocalDateTime", new DateTimeMappingValidator(utcZonedDateTime, localDateTime)},
                
                ///// OffsetDateTime to X /////
                {"OffsetDateTime: LOCAL to UTC", new DateTimeMappingValidator(localOffsetDateTime, utcOffsetDateTime)},
                {"OffsetDateTime: LOCAL to LocalDateTime", new DateTimeMappingValidator(localOffsetDateTime, localDateTime)},
                {"OffsetDateTime: UTC to LocalDateTime", new DateTimeMappingValidator(utcOffsetDateTime, localDateTime)},
                
                ///// OffsetTime to X /////
                {"OffsetTime: LOCAL to UTC", new DateTimeMappingValidator(localOffsetTime, utcOffsetTime, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                {"OffsetTime: LOCAL to LocalDateTime", new DateTimeMappingValidator(localOffsetTime, localDateTime, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
                {"OffsetTime: UTC to LocalDateTime", new DateTimeMappingValidator(utcOffsetTime, localDateTime, DateTimeMappingValidator.MappingFlag.IGNORE_DATE)},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "dateTimeData")
    public void dateTimeTest(String _testName, DateTimeMappingValidator _mapping) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        String differences = _mapping.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] dateTimeNegativeData(ITestContext _iTestContext) {
        
        LocalDateTime localDateTime = LocalDateTime.now();
        
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = localDateTime.toLocalTime();
        
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        
        Date javaDate = Date.from(instant);
        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
        
        return new Object[][] {
                
                ///// LocalDateTime to LocalDateTime /////
                {"LocalDateTime to NULL", new DateTimeMappingValidator(localDateTime, (Temporal) null)},
                {"LocalDateTime to LocalDateTime - 1 day", new DateTimeMappingValidator(localDateTime, localDateTime.minusDays(1))},
                
                ///// Percision Differences /////
                {"LocalDateTime to LocalDate", new DateTimeMappingValidator(localDateTime, localDate)},
                {"LocalDateTime to LocalTime", new DateTimeMappingValidator(localDateTime, localTime)},
                {"LocalDateTime to SqlDate", new DateTimeMappingValidator(localDateTime, sqlDate)},
                {"localDate to LocalTime", new DateTimeMappingValidator(localDate, localTime)},
                {"JavaDate to SqlDate", new DateTimeMappingValidator(javaDate, sqlDate)},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "dateTimeNegativeData")
    public void dateTimeNegativeTest(String _testName, DateTimeMappingValidator _mapping) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        String differences = _mapping.validate();
        if(differences == null) {
            throw new RuntimeException("There should have been differences! " + _testName + "\n" + _mapping);
        }
    }

    ////////// Long //////////

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] longData(ITestContext _iTestContext) {
        return new Object[][]{
                {(short) 123, 123L},
                {123, 123L},
                {(byte) 1, 1L},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "longData")
    public void longTest(long _source, long _destination) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        LongMappingValidator mapper = new LongMappingValidator(_source, _destination);
        String differences = mapper.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] longObjectData(ITestContext _iTestContext) {
        return new Object[][]{
                {null, null},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "longObjectData")
    public void longObjectTest(Long _source, Long _destination) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        LongMappingValidator mapper = new LongMappingValidator(_source, _destination);
        String differences = mapper.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] longNegativeData(ITestContext _iTestContext) {
        return new Object[][]{
                {null, 1L},
                {1L, 2L},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "longNegativeData")
    public void longNegativeTest(Long _source, Long _destination) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String expectedError = DataMappingValidator.createFormattedErrorString(_source, _destination);

        //------------------------ Code ----------------------------------------
        LongMappingValidator mapper = new LongMappingValidator(_source, _destination);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("No Differences Found!");
        }
        else if(!differences.equals(expectedError)) {
            throw new RuntimeException("Differences:\n" + differences + "\n\nExpected Error:\n" + expectedError);
        }
    }

    ////////// String //////////

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] stringData(ITestContext _iTestContext) {
        return new Object[][] {
                {null, null, new StringMappingValidator.MappingFlag[] {}},
                {"equal strings", "equal strings", new StringMappingValidator.MappingFlag[] {}},
                {"Ignore Case", "ignore case", new StringMappingValidator.MappingFlag[] {StringMappingValidator.MappingFlag.IGNORE_CASE}},
                {" test  normalize ", "  test normalize  ", new StringMappingValidator.MappingFlag[] {StringMappingValidator.MappingFlag.NORMALIZE_SOURCE, StringMappingValidator.MappingFlag.NORMALIZE_DESTINATION}},
                {" trim  ", "trim", new StringMappingValidator.MappingFlag[] {StringMappingValidator.MappingFlag.TRIM_SOURCE, StringMappingValidator.MappingFlag.TRIM_SOURCE}},
                {"XML Escape $emsp; Source &", "XML Escape $emsp; Source &amp;", new StringMappingValidator.MappingFlag[] {StringMappingValidator.MappingFlag.XML_ESCAPE_SOURCE}},
                {"XML Escape $nbsp; Destination &amp;", "XML Escape $nbsp; Destination &", new StringMappingValidator.MappingFlag[] {StringMappingValidator.MappingFlag.XML_ESCAPE_DESTINATION}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "stringData")
    public void stringTest(String _source, String _destination, StringMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        
        //------------------------ Code ----------------------------------------
        StringMappingValidator mapper = new StringMappingValidator(_source, _destination, _flags);
        String differences = mapper.validate();
        if(differences != null) {
            throw new RuntimeException(differences);
        }
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] stringNegativeData(ITestContext _iTestContext) {
        return new Object[][] {
                {null, "", new StringMappingValidator.MappingFlag[] {}},
                {"string1", "string2", new StringMappingValidator.MappingFlag[] {}},
        };
    }
    
    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "stringNegativeData")
    public void stringNegativeTest(String _source, String _destination, StringMappingValidator.MappingFlag[] _flags) {
        
        //------------------------ Pre-Checks ----------------------------------
        
        //------------------------ CONSTANTS -----------------------------------
        
        //------------------------ Variables -----------------------------------
        String expectedError = DataMappingValidator.createFormattedErrorString(_source, _destination);
        
        //------------------------ Code ----------------------------------------
        StringMappingValidator mapper = new StringMappingValidator(_source, _destination, _flags);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("No Differences Found!");
        }
        else if(!differences.equals(expectedError)) {
            throw new RuntimeException("Differences:\n" + differences + "\n\nExpected Error:\n" + expectedError);
        }
    }

    ////////// Object //////////

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] objectData(ITestContext _iTestContext) {
        return new Object[][]{
                {null, null},
                {BigInteger.ONE, BigInteger.valueOf(1)},
                {new BigInteger("2"), BigInteger.valueOf(2)},
                {new File("./walla.tmp"), new File("./walla.tmp")},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectData")
    public void objectTest(Object _source, Object _destination) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        ObjectMappingValidator mapper = new ObjectMappingValidator(_source, _destination);
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
                {null, BigInteger.ONE},
                {BigInteger.ZERO, null},
                {new BigInteger("1"), BigInteger.valueOf(2)},
                {new File("./walla.tmp"), new File("./walla.txt")},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "objectNegativeData")
    public void objectNegativeTest(Object _source, Object _destination) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        ObjectMappingValidator mapper = new ObjectMappingValidator(_source, _destination);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("There should have been differences!\n- " + _source + "\n- " + _destination);
        }
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
