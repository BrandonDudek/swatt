package xyz.swatt.testng.reporters;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Will generate a Test Matrix Style report, with Pass Rates.
 */
public class TestNgMatrixReporter implements IReporter {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(TestNgMatrixReporter.class);

    private static final Map<KnownError, Integer> knownErrors = new HashedMap<>();

    //========================= Static Variables ===============================
    /**
     * The name of the Report File to create (minus the ".html" extension).
     */
    public static String reportFileName = "testing-matrix";

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================
    /**
     * Will take in a Bug Id and report it as an error at the end of Regressions.
     * <p>
     *     (This is to be used for known bugs that have workaround in the Test Code.)
     * </p>
     * <p>
     *     <b>Note:</b> Errors that happen below the Test Method (in stack trace), will only be counted once, per Test Method.
     * </p>
     *
     * @param _bugId
     *         The Identifier of the bug to be reported.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    public static synchronized void addKnownError(String _bugId) {

        LOGGER.info("addKnownError(_bugId: {}) [START]", _bugId);

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------
        final int ERROR_LOCATION_LINE_NUMBER;
        final String ERROR_LOCATION_CLASS, ERROR_LOCATION_METHOD;
        String testClass = "", testClassPath = "", testMethod = "";

        @SuppressWarnings("SpellCheckingInspection")
        final List<String> CLASSES_TO_SKIP = Arrays.asList(
                "ForkedBooter",
                "TestNGExecutor",
                "TestNGProvider",
                "TestNGXmlTestSuite"
        );
        final List<String> PATHS_TO_SKIP = Arrays.asList(
                "java.lang.Thread",
                "java.lang.reflect.Method"
        );

        //------------------------ Variables -----------------------------------
        boolean testMethodLevelError = true;

        Integer count;
        KnownError knE = new KnownError();
        String reportedMethodColumn;

        //------------------------ Code ----------------------------------------
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

        StackTraceElement location = stacktrace[2]; // 0: Thread.getStackTrace(); 1: TestNgMatrixReporter.addKnownError().
        ERROR_LOCATION_CLASS = location.getClassName().substring(location.getClassName().lastIndexOf(".") + 1);
        ERROR_LOCATION_METHOD = location.getMethodName();
        ERROR_LOCATION_LINE_NUMBER = location.getLineNumber();

        for(int i = stacktrace.length - 1; i >= 2; i--) { // Start at beginning.

            StackTraceElement stackTraceElement = stacktrace[i];

            // Get Values.
            testClassPath = stackTraceElement.getClassName();
            testClass = testClassPath.substring(testClassPath.lastIndexOf(".") + 1);
            testMethod = stackTraceElement.getMethodName();

            // Skip Setup Classes.
            if(PATHS_TO_SKIP.contains(testClassPath)) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else if(CLASSES_TO_SKIP.contains(testClass)) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else if(testClassPath.startsWith("java.util.concurrent.ThreadPoolExecutor")) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else if(testClassPath.startsWith("org.testng.")) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else if(testClassPath.startsWith("sun.reflect.")) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else {
                break;
            }
        }

        // Format Method Column.
        if(testClass.equals(ERROR_LOCATION_CLASS) && testMethod.equals(ERROR_LOCATION_METHOD)) {
            reportedMethodColumn = testMethod + "()";
        }
        else {
            testMethodLevelError = false;
            reportedMethodColumn = testMethod + "() -> " + ERROR_LOCATION_CLASS
                    + (ERROR_LOCATION_METHOD.isEmpty() ? "" : "." + ERROR_LOCATION_METHOD + "()");
        }

        ////////// Add Known Error //////////
        knE.testClass = testClass;
        knE.testClassPath = testClassPath;
        knE.reportedMethod = reportedMethodColumn;
        knE.lineNum = ERROR_LOCATION_LINE_NUMBER;
        knE.bugId = _bugId;

        count = knownErrors.get(knE);
        count = count == null ? 1 : ++count;

        // Errors that occur below test method, can happen multiple times within a single Test Method run.
        // So we are only counting them as 1, as to not inflate the error count.
        // Note: This will also result in multiple test method call's errors, also only being counted as 1.
        // TODO: Find a way around this. (Maybe using Thread Ids.)
        count = testMethodLevelError ? count : 1;

        knownErrors.put(knE, count);

        LOGGER.debug("addKnownError(_bugId: {}) [END]", _bugId);
    }

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods ========================================
    /**
     * Constructs a Test Matrix style report.
     *
     * @param xmlSuites
     *         The TestNG XML Test Suites.
     * @param suites
     *         The TestNG XML Test Suites' results.
     * @param outputDirectory
     *         Where to write the output file(s).
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     *
     * @see IReporter#generateReport(List, List, String)
     */
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        LOGGER.info("generateReport() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        int allTestsCount = 0, allPassedTestsCount = 0;
        PrintWriter pw;

        //------------------------ Code ----------------------------------------
        if(!new File(outputDirectory).mkdirs()) {
            throw new RuntimeException("Could not create Output Directory: " + outputDirectory + "!");
        }

        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputDirectory, reportFileName + ".html"))));
        }
        catch(IOException e) {
            LOGGER.error("Unable to create Testing Matrix Report File!", e);
            return; // Fail without exception.
        }

        pw.println("<html><head>");
        pw.println("<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css'>");
        /*pw.println( "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css'>" );*/
        pw.println("</head><body style='margin:1%'>");

        //Iterating over each suite included in the test
        boolean firstSuite = true;
        for(ISuite suite : suites) {

            if(!firstSuite) {
                pw.println("<br/><br/>");
            }
            else {
                firstSuite = false;
            }

            String suiteName = suite.getName();
            pw.println("<h1>" + suiteName + "</h1>");

            boolean firstXmlTest = true;
            Map<String, ISuiteResult> suiteResults = suite.getResults();
            for(String testName : suiteResults.keySet()) {

                if(!firstXmlTest) {
                    pw.println("<br/>");
                }
                else {
                    firstXmlTest = false;
                }

                pw.println("<h2>" + testName + "</h2>");  // TestNG XML Test.

                ITestContext testContent = suiteResults.get(testName).getTestContext();
                int numOfMethods = testContent.getAllTestMethods().length;

                LinkedHashMap<String, Integer> successes = new LinkedHashMap<>(numOfMethods),
                        failures = new LinkedHashMap<>(numOfMethods),
                        skips = new LinkedHashMap<>(numOfMethods);

                pw.println("<h3>Test Methods</h3>");  // TestNG XML Test.
                for(ITestNGMethod testMethod : testContent.getAllTestMethods()) {

                    String classMethodName = getMethodPath(testMethod);

                    successes.put(classMethodName, 0);
                    failures.put(classMethodName, 0);
                    skips.put(classMethodName, 0);
                }

                loadResultsMap(successes, testContent.getPassedTests().getAllResults());
                loadResultsMap(failures, testContent.getFailedTests().getAllResults());
                loadResultsMap(skips, testContent.getSkippedTests().getAllResults());

                pw.println("<table class='table table-bordered table-hover' style='width:auto'>");
                pw.println("<tr><th>Class</th><th>Method</th><th>Total</th><th class='success'>Success</th><th class='danger'>Fail</th><th class='warning'>Skip</th></tr>");
                int totalRuns = 0, totalSuccess = 0, totalErrors = 0, totalSkips = 0;
                for(String key : successes.keySet()) {

                    String[] keyParts = key.split(Pattern.quote("."));
                    int success = successes.get(key);
                    int failed = failures.get(key);
                    int skip = skips.get(key);
                    int total = success + failed + skip;

                    pw.println("<tr><td>" + keyParts[0] + "</td><td>" + keyParts[1] + "</td><td>" + total + "</td><td class='success'>" + success
                            + "</td><td class='danger'>" + failed + "</td><td class='warning'>" + skip + "</td></tr>");

                    totalRuns += total;
                    totalSuccess += success;
                    totalErrors += failed;
                    totalSkips += skip;
                }
                pw.println("<tr><th></th><th>Totals:</th><th>" + totalRuns + "</th><th class='success'>" + totalSuccess + "</th><th class='danger'>"
                        + totalErrors + "</th><th class='warning'>" + totalSkips + "</th></tr>");

                int successRate = Math.round(totalSuccess / (float) totalRuns * 1000) / 10;
                pw.println("<tr><td></td><th>Pass Rate:</th><td></td><td>" + successRate + "%</td><td></td><td></td></tr>");

                pw.println("</table>");

                allTestsCount += totalRuns;
                allPassedTestsCount += totalSuccess;
            } // END LOOP - XML Test.
        } // END LOOP - XML Test Suite.

        int allSuccessRate = Math.round(allPassedTestsCount / (float) allTestsCount * 1000) / 10;
        pw.println("<br/><br/><table class='table table-bordered table-hover' style='margin-left: 2em; width:auto'><tr><th>Global Pass Rate:</th><th></th><th>"
                + allSuccessRate + "%</th></tr></table><br/><br/>");

        if(!knownErrors.isEmpty()) {

            pw.println("<h3>Known Bugs</h3>");
            pw.println("<table class='table table-bordered table-hover' style='width:auto'>");
            pw.println("<tr><th>Class</th><th>Method</th><th>Line Number</th><th>Bug Id</th><th class='danger'>Total</th></tr>");

            int totals = 0;
            for(KnownError ke : knownErrors.keySet()) {

                int errorCount = knownErrors.get(ke);

                pw.println("<tr><td title='" + ke.testClassPath + "'>" + ke.testClass + "</td><td>" + ke.reportedMethod
                        + "</td><td>" + ke.lineNum + "</td><td>" + ke.bugId + "</td><td class='danger'>" + errorCount
                        + "</td></tr>");

                totals += errorCount;
            }
            pw.println("<tr><th></th><th></th><th></th><th>Totals:</th><th class='danger'>" + totals + "</th></tr>");

            pw.println("</table>");

            allSuccessRate = Math.round(allPassedTestsCount / (float) (allTestsCount + totals) * 1000) / 10;
            pw.println("<br/><table class='table table-bordered table-hover' style='margin-left: 2em; width:auto'><tr><th>Global Pass Rate (with Known Bugs):</th><th></th><th>"
                    + allSuccessRate + "%</th></tr></table><br/><br/>");
        }

        pw.println("</body></html>");
        pw.flush();
        pw.close();

        LOGGER.debug("generateReport() [END]");
    }

    //////////////////// Helper Methods ////////////////////
    /**
     * Loads ITestResults into a given Map, that keep track of the count of each Result type.
     *
     * @param _map
     *         The Map to load.
     * @param _results
     *         The Results to count.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    private void loadResultsMap(Map<String, Integer> _map, Collection<ITestResult> _results) {

        LOGGER.debug("loadResultsMap( Map< String, Integer > _map, Collection< ITestResult > _results ) [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            for(ITestResult testResult : _results) {

                String key = getMethodPath(testResult.getMethod());
                int count = _map.get(key);
                count++;
                _map.put(key, count);
            }
        }
        catch(Exception e) {
            throw e; // For Debugging.
        }

        LOGGER.debug("loadResultsMap( Map< String, Integer > _map, Collection< ITestResult > _results ) [END]");
    }

    /**
     * Constructs the given Method's full path.
     *
     * @param _testMethod
     *         The method to fo figure out it's path.
     *
     * @return The given Method's full path.
     *
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    private String getMethodPath(ITestNGMethod _testMethod) {

        LOGGER.debug("getMethodPath(ITestNGMethod) [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------

        //------------------------ Code ----------------------------------------
        String path = _testMethod.getRealClass().getSimpleName() + "."

                // Not using ITestNGMethod.getMethodName(), because that name could be changed by custom TestNG Listeners.
                + _testMethod.getConstructorOrMethod().getName() + "()";

        LOGGER.debug("getMethodPath(ITestNGMethod) [END]");

        return path;
    }

    //========================= Classes ========================================
    private static class KnownError {

        int lineNum;

        String testClass;
        String testClassPath;

        String reportedMethod;

        String bugId;

        KnownError() { }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof KnownError)) {
                return false;
            }
            KnownError that = (KnownError) o;
            return Objects.equals(testClass, that.testClass) &&
                    Objects.equals(testClassPath, that.testClassPath) &&
                    Objects.equals(reportedMethod, that.reportedMethod);
        }

        @Override
        public int hashCode() {

            return Objects.hash(testClass, testClassPath, reportedMethod);
        }
    }
}
