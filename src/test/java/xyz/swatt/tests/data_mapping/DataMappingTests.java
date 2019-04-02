/*
 * Created on 2019-03-25 by Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>); for {swatt}.
 */
package xyz.swatt.tests.data_mapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xyz.swatt.data_mapping.*;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Will test all of the {@link xyz.swatt.data_mapping.DataMapping} classes.
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
        return new Object[][]{
                {null, null, new BigDecimalMapping.MappingFlag[]{}},
                {new BigDecimal("123.45"), BigDecimal.valueOf(123.45), new BigDecimalMapping.MappingFlag[]{}},
                {new BigDecimal("123.4"), BigDecimal.valueOf(123.4F).setScale(1, BigDecimal.ROUND_HALF_UP), new BigDecimalMapping.MappingFlag[]{}},
                {new BigDecimal("123.450"), new BigDecimal("123.450"), new BigDecimalMapping.MappingFlag[]{}},
                {new BigDecimal("123.450"), new BigDecimal("123.45"), new BigDecimalMapping.MappingFlag[]{BigDecimalMapping.MappingFlag.IGNORE_PRECISION}},
                {new BigDecimal("123"), new BigDecimal("123.000"), new BigDecimalMapping.MappingFlag[]{BigDecimalMapping.MappingFlag.IGNORE_PRECISION}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "bigDecimalData")
    public void bigDecimalTest(BigDecimal _source, BigDecimal _destination, BigDecimalMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        BigDecimalMapping mapper = new BigDecimalMapping(_source, _destination, _flags);
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
        return new Object[][]{

                {null, BigDecimal.valueOf(1), new BigDecimalMapping.MappingFlag[]{}},
                {new BigDecimal("123.450"), new BigDecimal("123.45"), new BigDecimalMapping.MappingFlag[]{}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "bigDecimalNegativeData")
    public void bigDecimalNegativeTest(BigDecimal _source, BigDecimal _destination, BigDecimalMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String expectedError = DataMapping.createFormattedErrorString(_source, _destination);

        //------------------------ Code ----------------------------------------
        BigDecimalMapping mapper = new BigDecimalMapping(_source, _destination, _flags);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("No Differences Found!");
        }
        else if(!differences.equals(expectedError)) {
            throw new RuntimeException("Differences:\n" + differences + "\n\nExpected Error:\n" + expectedError);
        }
    }

    ////////// LocalDateTime //////////

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @DataProvider
    public Object[][] localDateTimeData(ITestContext _iTestContext) {

        LocalDateTime localDateTimeNow = LocalDateTime.now();
        LocalDate localDateNow = LocalDate.now();
        LocalTime localTimeNow = localDateTimeNow.toLocalTime();

        DateTimeFormatter zonedDateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss.SSSxxx'['VV']'");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        String DateTimeString = dateTimeFormatter.format(localDateTimeNow);
        String DateString = dateFormatter.format(localDateTimeNow);
        String TimeString = timeFormatter.format(localDateTimeNow);

        ZonedDateTime localZonedDateTime = localDateTimeNow.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneOffset.UTC);

        String localZonedDateTimeString = localZonedDateTime.format(zonedDateTimeFormatter);
        String utcZonedDateTimeString = utcZonedDateTime.format(zonedDateTimeFormatter);

        return new Object[][]{

                ///// LocalDateTime to LocalDateTime /////
                {"NULL to NULL", new LocalDateTimeMapping().setSourceValue((LocalDateTime) null).setDestinationValue((LocalDateTime) null)},
                {"LocalDateTime to LocalDateTime", new LocalDateTimeMapping().setSourceValue(localDateTimeNow).setDestinationValue(LocalDateTime.from(localDateTimeNow))},

                ///// X to X /////
                {"LocalDate to LocalDate", new LocalDateTimeMapping().setSourceValue(localDateNow).setDestinationValue(LocalDate.from(localDateNow))},
                {"LocalTime to LocalTime", new LocalDateTimeMapping().setSourceValue(localTimeNow).setDestinationValue(LocalTime.from(localTimeNow))},
                {"String DateTime to String DateTime", new LocalDateTimeMapping().setSourceValue(DateTimeString, dateTimeFormatter).setDestinationValue(DateTimeString, dateTimeFormatter)},
                {"String Date to String Date", new LocalDateTimeMapping().setSourceValue(DateString, dateFormatter).setDestinationValue(DateString, dateFormatter)},
                {"String Time to String Time", new LocalDateTimeMapping().setSourceValue(TimeString, timeFormatter).setDestinationValue(TimeString, timeFormatter)},

                ///// X to LocalDateTime /////
                {"LocalDate to LocalDateTime", new LocalDateTimeMapping(LocalDateTimeMapping.MappingFlag.IGNORE_TIME).setSourceValue(localDateNow).setDestinationValue(localDateTimeNow)},
                {"LocalTime to LocalDateTime", new LocalDateTimeMapping(LocalDateTimeMapping.MappingFlag.IGNORE_DATE).setSourceValue(localTimeNow).setDestinationValue(localDateTimeNow)},
                {"String DateTime to LocalDateTime", new LocalDateTimeMapping().setSourceValue(DateTimeString, dateTimeFormatter).setDestinationValue(localDateTimeNow)},
                {"String Date to LocalDateTime", new LocalDateTimeMapping(LocalDateTimeMapping.MappingFlag.IGNORE_TIME).setSourceValue(DateString, dateFormatter).setDestinationValue(localDateTimeNow)},
                {"String Time to LocalDateTime", new LocalDateTimeMapping(LocalDateTimeMapping.MappingFlag.IGNORE_DATE).setSourceValue(TimeString, timeFormatter).setDestinationValue(localDateTimeNow)},

                ///// Time Zone Differences /////
                {"ZonedDateTime to ZonedDateTime", new LocalDateTimeMapping().setSourceValue(localZonedDateTime).setDestinationValue(utcZonedDateTime)},
                {"ZonedDateTime to ZonedDateTime", new LocalDateTimeMapping().setSourceValue(localZonedDateTime).setDestinationValue(localDateTimeNow)},
                {"ZonedDateTime to ZonedDateTime", new LocalDateTimeMapping().setSourceValue(utcZonedDateTime).setDestinationValue(localDateTimeNow)},

                {"String localZonedDateTimeString to String utcZonedDateTimeString", new LocalDateTimeMapping()
                        .setSourceValue(localZonedDateTimeString, zonedDateTimeFormatter).setDestinationValue(utcZonedDateTimeString, zonedDateTimeFormatter)},

                {"String localZonedDateTimeString to LocalDateTime", new LocalDateTimeMapping()
                        .setSourceValue(localZonedDateTimeString, zonedDateTimeFormatter).setDestinationValue(localDateTimeNow)},

                {"String utcZonedDateTimeString to LocalDateTime", new LocalDateTimeMapping()
                        .setSourceValue(utcZonedDateTimeString, zonedDateTimeFormatter).setDestinationValue(localDateTimeNow)},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "localDateTimeData")
    public void localDateTimeTest(String _testName, LocalDateTimeMapping _mapping) {

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
    public Object[][] localDateTimeNegativeData(ITestContext _iTestContext) {

        LocalDateTime localDateTimeNow = LocalDateTime.now();
        LocalDate localDateNow = LocalDate.now();
        LocalTime localTimeNow = localDateTimeNow.toLocalTime();

        DateTimeFormatter zonedDateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss.SSSxxx'['VV']'");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        String DateTimeString = dateTimeFormatter.format(localDateTimeNow);
        String DateString = dateFormatter.format(localDateTimeNow);
        String TimeString = timeFormatter.format(localDateTimeNow);

        ZonedDateTime localZonedDateTime = localDateTimeNow.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneOffset.UTC);

        String localZonedDateTimeString = localZonedDateTime.format(zonedDateTimeFormatter);
        String utcZonedDateTimeString = utcZonedDateTime.format(dateTimeFormatter);

        return new Object[][]{

                ///// LocalDateTime to LocalDateTime /////
                {"LocalDateTime to NULL", new LocalDateTimeMapping().setSourceValue(localDateTimeNow).setDestinationValue((LocalDateTime) null)},
                {"LocalDateTime to LocalDateTime - 1 day", new LocalDateTimeMapping().setSourceValue(localDateTimeNow).setDestinationValue(localDateTimeNow.minusDays(1))},

                ///// X to X /////
                {"String DateTime to String Date", new LocalDateTimeMapping().setSourceValue(DateTimeString, dateTimeFormatter).setDestinationValue(DateString, dateFormatter)},
                {"String DateTime to String Time", new LocalDateTimeMapping().setSourceValue(DateTimeString, dateTimeFormatter).setDestinationValue(TimeString, timeFormatter)},

                ///// X to LocalDateTime /////
                {"String Date to LocalDateTime", new LocalDateTimeMapping().setSourceValue(DateString, dateFormatter).setDestinationValue(localDateTimeNow)},
                {"String Time to LocalDateTime", new LocalDateTimeMapping().setSourceValue(TimeString, timeFormatter).setDestinationValue(localDateTimeNow)},

                ///// Time Zone Differences /////
                {"String localDateTime to String localZonedDateTime", new LocalDateTimeMapping()
                        .setSourceValue(DateTimeString, dateTimeFormatter).setDestinationValue(utcZonedDateTimeString, dateTimeFormatter)},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "localDateTimeNegativeData")
    public void localDateTimeNegativeTest(String _testName, LocalDateTimeMapping _mapping) {

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
        LongMapping mapper = new LongMapping(_source, _destination);
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
        LongMapping mapper = new LongMapping(_source, _destination);
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
        String expectedError = DataMapping.createFormattedErrorString(_source, _destination);

        //------------------------ Code ----------------------------------------
        LongMapping mapper = new LongMapping(_source, _destination);
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
        return new Object[][]{
                {null, null, new StringMapping.MappingFlag[]{}},
                {"equal strings", "equal strings", new StringMapping.MappingFlag[]{}},
                {"Ignore Case", "ignore case", new StringMapping.MappingFlag[]{StringMapping.MappingFlag.IGNORE_CASE}},
                {" test  normalize ", "  test normalize  ", new StringMapping.MappingFlag[]{StringMapping.MappingFlag.NORMALIZE_SOURCE, StringMapping.MappingFlag.NORMALIZE_DESTINATION}},
                {" trim  ", "trim", new StringMapping.MappingFlag[]{StringMapping.MappingFlag.TRIM_SOURCE, StringMapping.MappingFlag.TRIM_SOURCE}},
                {"XML Escape $emsp; Source &", "XML Escape $emsp; Source &amp;", new StringMapping.MappingFlag[]{StringMapping.MappingFlag.XML_ESCAPE_SOURCE}},
                {"XML Escape $nbsp; Destination &amp;", "XML Escape $nbsp; Destination &", new StringMapping.MappingFlag[]{StringMapping.MappingFlag.XML_ESCAPE_DESTINATION}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "stringData")
    public void stringTest(String _source, String _destination, StringMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        StringMapping mapper = new StringMapping(_source, _destination, _flags);
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
        return new Object[][]{
                {null, "", new StringMapping.MappingFlag[]{}},
                {"string1", "string2", new StringMapping.MappingFlag[]{}},
        };
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dataProvider = "stringNegativeData")
    public void stringNegativeTest(String _source, String _destination, StringMapping.MappingFlag[] _flags) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String expectedError = DataMapping.createFormattedErrorString(_source, _destination);

        //------------------------ Code ----------------------------------------
        StringMapping mapper = new StringMapping(_source, _destination, _flags);
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
        ObjectMapping mapper = new ObjectMapping(_source, _destination);
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
        ObjectMapping mapper = new ObjectMapping(_source, _destination);
        String differences = mapper.validate();
        if(differences == null) {
            throw new RuntimeException("There should have been differences!\n- " + _source + "\n- " + _destination);
        }
    }

    //========================= Helper Methods =================================

    //========================= Classes ========================================
}
