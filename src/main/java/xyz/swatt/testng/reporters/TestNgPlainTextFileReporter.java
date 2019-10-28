package xyz.swatt.testng.reporters;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.internal.Utils.isStringNotBlank;

/**
 * Will generate a Plain Text report, using code pulled from {@link org.testng.reporters.TextReporter}.
 * <p>
 * <i>Note:</i> File name is set by {@link #reportFileName}.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 * @see org.testng.reporters.TextReporter
 */
@LogMethods
public class TestNgPlainTextFileReporter implements IReporter {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(TestNgPlainTextFileReporter.class);
	
	private static final String LINE = "\n===============================================\n";
	
	//========================= Static Variables ===============================
	/**
	 * Set to {@code false}, successful tests will not be reported;
	 */
	public static boolean reportSuccesses = true;
	/**
	 * Set to {@code false}, skips tests will not be reported;
	 */
	public static boolean reportSkips = false;
	/**
	 * Set to {@code false}, to shorten the Stacktraces;
	 */
	public static boolean verbose = true;
	
	/**
	 * If set, the Report File will be placed in this directory, instead of the default Test Output directory.
	 */
	public static String outputDirectoryOverride = null;
	
	/**
	 * The name of the Report File to create (minus the ".txt" extension).
	 */
	public static String reportFileName = "report";
	
	//========================= Static Constructor =============================
	static {}
	
	//========================= Static Methods =================================
	
	//========================= CONSTANTS ======================================
	public final LocalDateTime START_TIME;
	
	//========================= Variables ======================================
	StringBuilder stringBuilder = new StringBuilder();
	
	//========================= Constructors ===================================
	
	/**
	 * Captures the Test Suite Start Time.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 */
	public TestNgPlainTextFileReporter() {
		
		super();
		
		//------------------------ Pre-Checks ----------------------------------
		
		//-------------------------CONSTANTS------------------------------------
		
		//-------------------------Variables------------------------------------
		
		//-------------------------Code-----------------------------------------
		START_TIME = LocalDateTime.now();
	}
	
	//========================= Public Methods =================================
	
	/**
	 * Constructs a Plain Text report.
	 *
	 * @param xmlSuites
	 * 		The TestNG XML Test Suites.
	 * @param suites
	 * 		The TestNG XML Test Suites' results.
	 * @param outputDirectoryPath
	 * 		Where to write the output file(s).
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see IReporter#generateReport(List, List, String)
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectoryPath) {
		
		//------------------------ Pre-Checks ----------------------------------
		
		//------------------------ CONSTANTS -----------------------------------
		
		//------------------------ Variables -----------------------------------
		
		//------------------------ Code ----------------------------------------
		outputDirectoryPath = outputDirectoryOverride == null ? outputDirectoryPath : outputDirectoryOverride;
		File outputDirectory = new File(outputDirectoryPath);
		if(!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new RuntimeException("Could not create Output Directory: " + outputDirectoryPath + "!");
		}
		
		for(ISuite suite : suites) {
			
			Map<String, ISuiteResult> suiteResults = suite.getResults();
			for(String testName : suiteResults.keySet()) {
				
				ITestContext testContent = suiteResults.get(testName).getTestContext();
				
				logResults(testContent);
			} // END LOOP - XML Test.
		} // END LOOP - XML Test Suite.
		
		try {
			FileUtils.write(new File(outputDirectoryPath, reportFileName + ".txt"), stringBuilder, StandardCharsets.UTF_8);
		}
		catch(IOException e) {
			throw new RuntimeException("Could not write results to file!\n\n" + stringBuilder);
		}
	}
	
	//========================= Helper Methods =================================
	private void logResults(ITestContext testContent) {
		
		IResultMap failedTestResults = testContent.getFailedTests();
		IResultMap skippedTestResults = testContent.getSkippedTests();
		IResultMap passedTestResults = testContent.getPassedTests();
		
		////////// Log Configurations //////////
		for(ITestResult tr : testContent.getFailedConfigurations().getAllResults()) {
			Throwable ex = tr.getThrowable();
			String stackTrace = "";
			if(ex != null) {
				if(verbose) {
					stackTrace = Utils.longStackTrace(ex, false);
				}
				else {
					stackTrace = Utils.shortStackTrace(ex, false);
				}
			}
			
			logResult("FAILED CONFIGURATION",
					tr.getTestClass().getName(),
					Utils.detailedMethodName(tr.getMethod(), false),
					tr.getMethod().getDescription(),
					stackTrace,
					tr.getParameters(),
					tr.getMethod().getConstructorOrMethod().getParameterTypes());
		}
		
		for(ITestResult tr : testContent.getSkippedConfigurations().getAllResults()) {
			logResult("SKIPPED CONFIGURATION",
					tr.getTestClass().getName(),
					Utils.detailedMethodName(tr.getMethod(), false),
					tr.getMethod().getDescription(),
					null,
					tr.getParameters(),
					tr.getMethod().getConstructorOrMethod().getParameterTypes());
		}
		
		////////// Log Methods ////////// TODO: Log in order of execution.
		for(ITestResult tr : failedTestResults.getAllResults()) {
			Throwable ex = tr.getThrowable();
			String stackTrace = "";
			if(ex != null) {
				if(verbose) {
					stackTrace = Utils.longStackTrace(ex, false);
				}
				else {
					stackTrace = Utils.shortStackTrace(ex, false);
				}
			}
			
			logResult("FAILED", tr, stackTrace);
		}
		
		Set<ITestResult> rawskipped = skippedTestResults.getAllResults();
		List<ITestResult> skippedTests = Lists.newArrayList();
		List<ITestResult> retriedTests = Lists.newArrayList();
		for(ITestResult result : rawskipped) {
			if(result.wasRetried()) {
				retriedTests.add(result);
			}
			else {
				skippedTests.add(result);
			}
		}
		
		if(reportSkips) {
			logExceptions("SKIPPED", skippedTests);
		}
		if(reportSuccesses) {
			logExceptions("RETRIED", retriedTests);
		}
		
		if(reportSuccesses) {
			for(ITestResult tr : passedTestResults.getAllResults()) {
				logResult("PASSED", tr, null);
			}
		}
		
		////////// Generate Header ////////// TODO: Add Header for XML Test Suite (right now just have XML Tests).
		int totalTestMethodsCallsCount = failedTestResults.size() + skippedTestResults.size() + passedTestResults.size();
		StringBuilder logBuf = new StringBuilder(LINE);
		logBuf.append("    ").append(testContent.getName()).append("\n");
		logBuf.append("    Tests run: ").append(totalTestMethodsCallsCount)
				.append(", Failures: ").append(failedTestResults.size())
				.append(", Skips: ").append(skippedTests.size());
		if(!retriedTests.isEmpty()) {
			logBuf.append(", Retries: ").append(retriedTests.size());
		}
		int confFailures = testContent.getFailedConfigurations().getAllResults().size();
		int confSkips = testContent.getSkippedConfigurations().getAllResults().size();
		if(confFailures > 0 || confSkips > 0) {
			logBuf.append("\n")
					.append("    Configuration Failures: ").append(confFailures)
					.append(", Skips: ").append(confSkips);
		}
		
		String timeTaken = Duration.between(START_TIME, LocalDateTime.now()).toString().substring(2);
		logBuf.append("\n")
				.append("    Total Time: ").append(timeTaken);
		
		logBuf.append(LINE);
		
		//logResult("", logBuf.toString());
		
		stringBuilder.insert(0, logBuf); // Add Counts to begining of report.
	}
	
	private void logResult(String status, ITestResult tr, String stackTrace) {
		logResult(status, tr.getTestClass().getName(), tr.getName(), tr.getMethod().getDescription(), stackTrace, tr.getParameters(),
				tr.getMethod().getConstructorOrMethod().getParameterTypes());
	}
	
	private void logResult(String status, String className, String name, String description, String stackTrace, Object[] params, Class<?>[] paramTypes) {
		
		StringBuilder msg = new StringBuilder(className).append("#").append(name);
		
		if(null != params && params.length > 0) {
			msg.append("(");
			
			// The error might be a data provider parameter mismatch, so make
			// a special case here
			if(params.length != paramTypes.length) {
				msg.append(name)
						.append(": Wrong number of arguments were passed by the Data Provider: found ")
						.append(params.length)
						.append(" but expected ")
						.append(paramTypes.length)
						.append(")");
			}
			else {
				for(int i = 0; i < params.length; i++) {
					if(i > 0) {
						msg.append(", ");
					}
					msg.append(Utils.toString(params[i], paramTypes[i]));
				}
				
				msg.append(")");
			}
		}
		if(!Utils.isStringEmpty(description)) {
			msg.append("\n");
			for(int i = 0; i < status.length() + 2; i++) {
				msg.append(" ");
			}
			msg.append(description);
		}
		if(!Utils.isStringEmpty(stackTrace)) {
			msg.append("\n").append(stackTrace);
		}
		
		logResult(status, msg.toString());
	}
	
	private void logResult(String status, String message) {
		if(isStringNotBlank(status)) {
			stringBuilder.append(status).append(": ");
		}
		stringBuilder.append(message);
		if(!message.endsWith("\n") && !message.endsWith("\r")) {
			stringBuilder.append("\n");
		}
	}
	
	private void logExceptions(String status, List<ITestResult> results) {
		results.forEach(
				tr -> {
					Throwable throwable = tr.getThrowable();
					logResult(status, tr, throwable != null ? Utils.shortStackTrace(throwable, false) : null);
				});
	}
	
	//========================= Classes ========================================
}
