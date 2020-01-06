package xyz.swatt.tests.data_mapping;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xyz.swatt.data_mapping_validator.*;
import xyz.swatt.log.LogMethods;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Unit Tests for DataMappingValidtor's Custom Comparator.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class CustomComparatorTests {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(CustomComparatorTests.class);
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Public Static Methods ==========================
	
	//========================= Helper Static Methods ==========================
	
	//========================= CONSTANTS ======================================
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	//========================= Public Methods =================================
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@DataProvider
	public Iterator<Object[]> mappingValidatorsData(ITestContext _iTestContext) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		List<Object[]> data = new LinkedList<>();
		
		//------------------------ Code ----------------------------------------
		final String bigDecimalMappingName = "Custom BigDecimal";
		final BigDecimal bigDecimalVal1 = new BigDecimal(2.5);
		final BigDecimal bigDecimalVal2 = new BigDecimal(3.75);
		final BigDecimalMappingValidator.MappingFlag bigDecimalFlag = BigDecimalMappingValidator.MappingFlag.IGNORE_PRECISION;
		data.add(new Object[] {new BigDecimalMappingValidator(bigDecimalMappingName, bigDecimalVal1, bigDecimalVal2, bigDecimalFlag)
				.setComparator(new DataMappingValidator.DataMappingComparator<BigDecimal>() {
			@Override
			public boolean compair(DataMappingValidator _dataMappingValidator, BigDecimal _source, BigDecimal _destination) {
				boolean isEqual = _dataMappingValidator.getMappingName().equals(bigDecimalMappingName);
				isEqual &= _source.equals(bigDecimalVal1);
				isEqual &= _destination.equals(bigDecimalVal2);
				isEqual &= _dataMappingValidator.getDataMappingFlags().contains(bigDecimalFlag);
				return isEqual;
			}
		})});
		
		final String dateTimeMappingName = "Custom DateTime";
		final LocalDate dateTimeVal1 = LocalDate.now();
		final LocalTime dateTimeVal2 = LocalTime.now();
		final DateTimeMappingValidator.MappingFlag dateTimeFlag = DateTimeMappingValidator.MappingFlag.IGNORE_TIME;
		data.add(new Object[] {new DateTimeMappingValidator(dateTimeMappingName, dateTimeVal1, dateTimeVal2, dateTimeFlag)
				.setComparator(new DataMappingValidator.DataMappingComparator<DateTimeMappingValidator.LocalDateTimeTuple>() {
			@Override
			public boolean compair(DataMappingValidator _dataMappingValidator, DateTimeMappingValidator.LocalDateTimeTuple _source,
			                       DateTimeMappingValidator.LocalDateTimeTuple _destination) {
				boolean isEqual = _dataMappingValidator.getMappingName().equals(dateTimeMappingName);
				isEqual &= _source.DATE.equals(dateTimeVal1);
				isEqual &= _destination.TIME.equals(dateTimeVal2);
				isEqual &= _dataMappingValidator.getDataMappingFlags().contains(dateTimeFlag);
				return isEqual;
			}
		})});
		
		final String longMappingName = "Custom Long";
		final Long longVal1 = 1234567890l;
		final Long longVal2 = 9876543210L;
		data.add(new Object[] {new LongMappingValidator(longMappingName, longVal1, longVal2)
				.setComparator(new DataMappingValidator.DataMappingComparator<Long>() {
			@Override
			public boolean compair(DataMappingValidator _dataMappingValidator, Long _source, Long _destination) {
				boolean isEqual = _dataMappingValidator.getMappingName().equals(longMappingName);
				isEqual &= _source.equals(longVal1);
				isEqual &= _destination.equals(longVal2);
				return isEqual;
			}
		})});
		
		final String objectMappingName = "Custom Object";
		final String stringVal1 = "Walla Walla Walla";
		final String stringVal2 = "Cheese Stick Jimmy";
		data.add(new Object[] {new ObjectMappingValidator(objectMappingName, stringVal1, stringVal2)
				.setComparator(new DataMappingValidator.DataMappingComparator<String>() {
			@Override
			public boolean compair(DataMappingValidator _dataMappingValidator, String _source, String _destination) {
				boolean isEqual = _dataMappingValidator.getMappingName().equals(objectMappingName);
				isEqual &= _source.equals(stringVal1);
				isEqual &= _destination.equals(stringVal2);
				return isEqual;
			}
		})});
		
		final String stringMappingName = "Custom String";
		final StringMappingValidator.MappingFlag stringFlag = StringMappingValidator.MappingFlag.IGNORE_CASE;
		data.add(new Object[] {new StringMappingValidator(stringMappingName, stringVal1, stringVal2, stringFlag)
				.setComparator(new DataMappingValidator.DataMappingComparator<String>() {
			@Override
			public boolean compair(DataMappingValidator _dataMappingValidator, String _source, String _destination) {
				boolean isEqual = _dataMappingValidator.getMappingName().equals(stringMappingName);
				isEqual &= _source.equals(stringVal1);
				isEqual &= _destination.equals(stringVal2);
				isEqual &= _dataMappingValidator.getDataMappingFlags().contains(stringFlag);
				return isEqual;
			}
		})});
		
		// CollectionMappingValidator tested in CollectionMappingTests.
		
		return data.iterator();
	}
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	@Test(dataProvider = "mappingValidatorsData")
	public void validateCustomComparator(DataMappingValidator _dataMappingValidator) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		String errors;
		
		//------------------------ Code ----------------------------------------
		if(StringUtils.isNotBlank(errors = _dataMappingValidator.validate())) {
			Assert.fail(errors);
		}
	}
	
	//========================= Helper Methods =================================
	
	//========================= Classes ========================================
}
