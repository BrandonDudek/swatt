package xyz.swatt.testng.reporters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;
import xyz.swatt.log.LogMethods;

import java.io.*;
import java.util.List;

/**
 * Will generate a Plain Test report, with Pass Percentage.
 * <p>
 * <i>Note:</i> File name is set by {@link #reportFileName}.
 * </p>
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
@LogMethods
public class TestNgPassPercentageReporter implements IReporter {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(TestNgPassPercentageReporter.class);

    //========================= Static Variables ===============================
    private static int knownBugsCount = 0;
    /**
     * If set, the Report File will be placed in this directory, instead of the default Test Output directory.
     */
    public static String outputDirectoryOverride = null;

    /**
     * The name of the Report File to create (minus the ".html" extension).
     */
    public static String reportFileName = "testng-pass-percentage";

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    /**
     * Will allow the Error Count to be incremented by 1.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public static synchronized void addKnownError() {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        knownBugsCount++;
    }

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods ========================================

    /**
     * Constructs a Pass Percentage report.
     *
     * @param _xmlSuites
     *         The TestNG XML Test Suites.
     * @param _suites
     *         The TestNG XML Test Suites' results.
     * @param _outputDirectoryPath
     *         Where to write the output file(s).
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     * @see IReporter#generateReport(List, List, String)
     */
    @Override
    public void generateReport(List<XmlSuite> _xmlSuites, List<ISuite> _suites, String _outputDirectoryPath) {

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        int allTestsCount = 0, allPassedTestsCount = 0;

        File outputDirectory = new File(outputDirectoryOverride == null ? _outputDirectoryPath : outputDirectoryOverride);
        PrintWriter pw;

        //------------------------ Code ----------------------------------------
        if(!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new RuntimeException("Could not create Output Directory: " + _outputDirectoryPath + "!");
        }

        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(_outputDirectoryPath, reportFileName + ".txt"))));
        }
        catch(IOException e) {
            throw new RuntimeException("Unable to create Testing Pass Percentage File! " + e.getMessage());
        }

        for(ISuite suite : _suites) { // Iterating over each XML Test Suite...
            for(ISuiteResult iSuiteResult : suite.getResults().values()) { // Iterating over each XML Test...

                ITestContext testContent = iSuiteResult.getTestContext();

                int passesCount = testContent.getPassedTests().size();
                allPassedTestsCount += passesCount;

                allTestsCount += passesCount;
                allTestsCount += testContent.getFailedTests().size();
                allTestsCount += testContent.getSkippedTests().size();

            } // END LOOP - XML Test.
        } // END LOOP - XML Test Suite.

        allTestsCount += knownBugsCount;

        double allSuccessRate = Math.round((float) allPassedTestsCount / (float) allTestsCount * 1000.0) / 10.0;
        pw.print(allSuccessRate + "%");
        pw.flush();
        pw.close();
    }

    //========================= Classes ========================================
}
