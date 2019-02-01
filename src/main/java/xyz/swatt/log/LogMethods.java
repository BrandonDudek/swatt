package xyz.swatt.log;

import org.apache.logging.log4j.Level;
import org.apache.xerces.dom.ElementNSImpl;

import java.io.File;
import java.lang.annotation.*;

/**
 * Will log [START] and [END] logs for all methods and constructors (except for the static constructor). Argument and Return values are included in the logs, by
 * default.
 * <p>&nbsp;</p>
 * <p>
 * <b>Notes:</b>
 * </p>
 * <ul>
 * <li>[START] Logs are {@link Level#INFO} for {@code public} methods and {@link Level#DEBUG} for non-{@code public} methods</li>
 * <li>[END] Logs are {@link Level#DEBUG} for {@code public} methods and {@link Level#TRACE} for non-{@code public} methods</li>
 * <li>(Line Numbers will not be correct for these logs)</li>
 * <li>{@link ElementNSImpl}s are printed as [Name: Value], or [Name: (child_count)] if Value is {@code null}</li>
 * <li>{@link File}s are printed as Absolute Paths</li>
 * <li>Array Values are printed out</li>
 * <li>Multidimensional Array Values are printed out, at each level</li>
 * <li>.toString is used for everything else</li>
 * </ul>
 * <p>
 * <sub>(Work done in {@link LogMethodsAspect}.)</sub>
 * </p>
 * <p>&nbsp;</p>
 * <p>
 * <b>See:</b> <a href="https://www.mojohaus.org/aspectj-maven-plugin/examples/libraryJars.html">Using Aspect Libraries</a> on the MojoHaus website,
 * to use this AOP Annotation in your code.
 * </p>
 */
@Documented // This adds this annotation to Javadocs.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface LogMethods {

    /**
     * If {@code false}, method argument values will not be logged.
     * <p>(default = {@code true})</p>
     *
     * @return If method argument values will be logged.
     */
    boolean arguments() default true;

    /**
     * If {@code false}, method returned values will not be logged.
     * <p>(default = {@code true})</p>
     *
     * @return If method returned values will be logged.
     */
    boolean returns() default true;

    /**
     * If {@code true}, then this method will not be logged.
     * <p>(default = {@code false})</p>
     * <p>
     * <i>This is ignored at the class level.</i>
     * </p>
     *
     * @return If this method logging will be skipped.
     */
    boolean skip() default false;
}
