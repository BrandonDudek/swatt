package xyz.swatt.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.dom.ElementNSImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This class handles the logic for the @{@link LogMethods} annotation.
 */
@Aspect
public class LogMethodsAspect {

    //========================= Static Enums ===================================

    //========================= STATIC CONSTANTS ===============================
    private static final Logger LOGGER = LogManager.getLogger(LogMethodsAspect.class);

    //========================= Static Variables ===============================

    //========================= Static Constructor =============================
    static {}

    //========================= Static Methods =================================

    //========================= CONSTANTS ======================================

    //========================= Variables ======================================

    //========================= Constructors ===================================

    //========================= Methods for External Use =======================
    @Around("(execution(*.new(..)) || execution(* *(..))) && (@within(LogMethods) || @annotation(LogMethods))")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        LOGGER.info("around(ProceedingJoinPoint: {}) [START]", proceedingJoinPoint.getKind());

        //------------------------ Pre-Checks ----------------------------------

        //-------------------------CONSTANTS------------------------------------

        //-------------------------Variables------------------------------------
        ///// Tier 1 /////
        boolean logParams, logResults;
        int modifiers;

        Class<?> classObj = proceedingJoinPoint.getSignature().getDeclaringType(), // Target is null for static methods.
                methodReturnType;
        Executable executable;
        LogMethods classAnnotation, constructorMethodAnnotation;
        Signature signature = proceedingJoinPoint.getSignature();
        Object toRet;
        String constructorMethodName;
        StringBuilder logString;

        String[] parameterNames;

        ///// Tier 2 /////
        Logger logger = LogManager.getLogger(classObj);

        //-------------------------Code-----------------------------------------
        ////////// Get Annotation(s), Modifiers, Name, & Return Type //////////
        classAnnotation = classObj.getAnnotation(LogMethods.class);
        if(signature instanceof ConstructorSignature) {
            ConstructorSignature constructorSignature = ((ConstructorSignature) signature);
            Constructor constructor = constructorSignature.getConstructor();
            constructorMethodName = signature.getDeclaringType().getSimpleName();
            parameterNames = constructorSignature.getParameterNames();
            methodReturnType = null;

            executable = constructor;
        }
        else {
            MethodSignature methodSignature = ((MethodSignature) signature);
            Method method = methodSignature.getMethod();
            constructorMethodName = method.getName();
            parameterNames = methodSignature.getParameterNames();
            methodReturnType = method.getReturnType();

            // Early Exit Check //
            if(constructorMethodName.startsWith("$SWITCH_TABLE$")) {
                toRet = proceedingJoinPoint.proceed();
                LOGGER.debug("around(ProceedingJoinPoint: {}) [END]: $SWITCH_TABLE$", proceedingJoinPoint.getKind());
                return toRet;
            }

            executable = method;
        }
        constructorMethodAnnotation = executable.getAnnotation(LogMethods.class);
        modifiers = executable.getModifiers();

        ///// Get Annotation(s) Values /////
        logParams = constructorMethodAnnotation != null ? constructorMethodAnnotation.arguments() : classAnnotation.arguments();
        logResults = constructorMethodAnnotation != null ? constructorMethodAnnotation.returns() : classAnnotation.returns();

        ////////// Log Method Start //////////
        ///// Construct Log String /////
        logString = new StringBuilder(constructorMethodName + "(");
        for(int i = 0; i < proceedingJoinPoint.getArgs().length; i++) {

            logString.append(parameterNames[i]);
            if(logParams) {
                logString.append(": ").append(toLogString(proceedingJoinPoint.getArgs()[i])); // Log Value.
            }
            else {
                logString.append(" (").append(executable.getParameterTypes()[i].getSimpleName()).append(")"); // Log Class/Type.
            }
            if(i < proceedingJoinPoint.getArgs().length - 1) {
                logString.append(", ");
            }
        }
        logString.append(") ");

        ///// Perform Logging /////
        if(Modifier.isPublic(modifiers)) {
            logger.info(logString + "[START]");
        }
        else {
            logger.debug(logString + "[START]");
        }

        ////////// Run Method //////////
        toRet = proceedingJoinPoint.proceed();

        ////////// Log Method End //////////
        ///// Construct Log String /////
        logString.append("[END]");
        if(methodReturnType != null && methodReturnType != Void.TYPE && logResults) {
            logString.append(": ").append(toLogString(toRet));
        }

        ///// Perform Logging /////
        if(Modifier.isPublic(modifiers)) {
            logger.info(logString.toString());
        }
        else {
            logger.debug(logString.toString());
        }

        LOGGER.debug("around(ProceedingJoinPoint: {}) [END]: Object", proceedingJoinPoint.getKind());

        return toRet;
    }

    //========================= Methods for Internal Use =======================

    /**
     * @return The given {@code Object} as a formatted string, based on type.
     *
     * @author Brandon Dudek &lt;bdudek@paychex.com&gt;
     */
    private String toLogString(Object _object) {

        LOGGER.debug("toLogString(_object: {}) [START]", _object.getClass().getTypeName());

        //------------------------ Pre-Checks ----------------------------------
        if(_object == null) {
            LOGGER.trace("toLogString(_object: {}) [END]: (NULL)", _object.getClass().getTypeName());
            return "(NULL)";
        }

        //------------------------ CONSTANTS -----------------------------------

        //------------------------ Variables -----------------------------------
        String logString;

        //------------------------ Code ----------------------------------------
        if(_object instanceof Object[]) { // Also evaluates to true, for multidimensional primitive arrays.
            logString = Arrays.deepToString((Object[]) _object);
        }
        else {
            switch(_object.getClass().getTypeName()) {
                case "org.apache.xerces.dom.ElementNSImpl":
                    ElementNSImpl element = ((ElementNSImpl) _object);
                    synchronized(element.getOwnerDocument()) { // ElementNSImpl/org.w3c.dom.Document are not thread safe.
                        logString = "[" + element.getNodeName() + ": "
                                + (element.getNodeValue() != null ? element.getNodeValue() : "(" + element.getLength() + ")") + "]";
                    }
                    break;
                case "java.io.File":
                    logString = ((File) _object).getAbsolutePath();
                    break;
                case "byte[]":
                    logString = Arrays.toString((byte[]) _object);
                    break;
                case "boolean[]":
                    logString = Arrays.toString((boolean[]) _object);
                    break;
                case "int[]":
                    logString = Arrays.toString((int[]) _object);
                    break;
                case "short[]":
                    logString = Arrays.toString((short[]) _object);
                    break;
                case "long[]":
                    logString = Arrays.toString((long[]) _object);
                    break;
                case "float[]":
                    logString = Arrays.toString((float[]) _object);
                    break;
                case "double[]":
                    logString = Arrays.toString((double[]) _object);
                    break;
                case "char[]":
                    logString = Arrays.toString((char[]) _object);
                    break;
                default:
                    logString = _object.toString();
            }
        }

        LOGGER.trace("toLogString(_object: {}) [END]: String ({})", _object.getClass().getTypeName(), logString.length());

        return logString;
    }

    //========================= Classes ========================================
}
