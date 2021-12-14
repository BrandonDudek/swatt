package xyz.swatt.tests.log;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Will generateLogs the LogMethodsAspect Annotation.
 */
public class LogMethodsAspectTests {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(LogMethodsAspectTests.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods for External Use =======================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test
    public void generateLogs() {

        LOGGER.info("generateLogs() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //-------------------------CONSTANTS------------------------------------

        //-------------------------Variables------------------------------------

        //-------------------------Code-----------------------------------------
        SampleClass.publicStaticVoidMethod();

        SampleClass sampleClass = new SampleClass();

        ////////// Test Argument Types //////////
        sampleClass.packagePrivateMethodWithPrimitiveArguments((byte) 0, false, 12345, 678.90, 'a');

        sampleClass.packagePrivateMethodWithObjectArguments("Cheese Steak Jimmy"
                /*, new File("C:\\sample.file") - Cannot test File logs, because absolute path is different for each OS.*/);
        // TODO: org.apache.xerces.dom.ElementNSImpl.

        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        sampleClass.packagePrivateMethodWithCollectionArguments(
                new int[]{1, 2, 3},
                new String[]{"one", "two", "three"},
                Arrays.asList("X", "Y", "Z"), map);

        sampleClass.packagePrivateMethodWithMultidimensionalCollectionArguments(
                new char[][]{{'a', 'b', 'c'}, {'x', 'y', 'z'}},
                new String[][]{{"Walla1", "Walla2", "Walla3"}, {"Cheese", "Steak", "Jimmy"}});

        ////////// Test Return Types //////////
        sampleClass.callPrivateMethods();

        ////////// @LogMethods Argument Tests //////////
        sampleClass.skipMethod();

        // TODO: Test @LogMethods(arguments = false, returns = false)

        // TODO: Validate Enums.

        LOGGER.debug("generateLogs() [END]");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @Test(dependsOnMethods = "generateLogs")
    public void validateLogs() throws URISyntaxException, IOException {

        LOGGER.info("validateLogs() [START]");

        //------------------------ Pre-Checks ----------------------------------

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        File expectedLog = new File(getClass().getClassLoader().getResource("log-test-expected.log").toURI()),
                actualLogs = new File("test-output/xyz.swatt.tests.log.log");

        //------------------------ Code ----------------------------------------
        Assert.assertTrue(expectedLog.exists(), "Cannot find Expected Log file!");
        Assert.assertTrue(actualLogs.exists(), "Cannot find Actual Log file!");

        if(!FileUtils.contentEquals(expectedLog, actualLogs)) {
            String error = "Actual Log Files does not equal Expected!\n\tExpected: " + expectedLog.getAbsolutePath()
                    + "\n\tActual: " + actualLogs.getAbsolutePath();
            throw new AssertionError(error);
        }

        LOGGER.debug("validateLogs() [END]");
    }

    //========================= Methods for Internal Use =======================

    //========================= Classes ========================================
}
