package xyz.swatt.tests.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.swatt.log.LogMethods;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 */
@LogMethods
public class SampleClass {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(SampleClass.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {
        LOGGER.debug("Static Constructor");
    }

    //========================= Static Methods =================================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @LogMethods
    public static void publicStaticVoidMethod() {
        LOGGER.debug("\t\tInside publicStaticVoidMethod.");
    }

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @LogMethods(arguments = false, returns = false)
    protected SampleClass() {
        LOGGER.debug("\t\tInside Protected Constructor.");
    }

    //========================= Methods for External Use =======================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @LogMethods
    public void callPrivateMethods() {
        privateMethodWithReturnString();
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    @LogMethods(skip = true)
    public void skipMethod() {
        LOGGER.debug("\t\tInside skipMethod.");
    }

    //========================= Methods for Internal Use =======================

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    void packagePrivateMethodWithPrimitiveArguments(byte _byte, boolean _boolean, int _int, double _double, char _char) {
        LOGGER.trace("\t\tInside packagePrivateMethodWithPrimitiveArguments.");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    void packagePrivateMethodWithObjectArguments(String _string, File _file) {
        LOGGER.trace("\t\tInside packagePrivateMethodWithObjectArguments.");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    void packagePrivateMethodWithCollectionArguments(int[] _primitiveArray, String[] _objectArray, List _list, Map _map) {
        LOGGER.trace("\t\tInside packagePrivateMethodWithCollectionArguments.");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    void packagePrivateMethodWithMultidimensionalCollectionArguments(char[][] _primitiveDoubleArray, String[][] _objectDoubleArray) {
        LOGGER.trace("\t\tInside packagePrivateMethodWithMultidimensionalCollectionArguments.");
    }

    /**
     * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
     */
    //@LogMethods // Inherit from class.
    private String privateMethodWithReturnString() {
        LOGGER.trace("\t\tInside privateMethod.");
        return "Walla Walla Walla";
    }

    //========================= Classes ========================================
}
