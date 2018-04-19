/**
 * This package will contain all of the custom <b>SWATT</b> {@link java.lang.Exception}s.
 * <p>
 *     These {@link java.lang.Exception}s will be kept generic, so that they will work in multiple situations.
 * </p>
 * <p>
 *     Every {@link java.lang.Exception} will extend the {@link xyz.swatt.exceptions.AbstractSwattException},
 *     so that they can all be caught under 1 ancestor class.
 * </p>
 * <p>
 *     Every {@link java.lang.Exception} will extend {@link java.lang.RuntimeException}, so that it does not need to be declared as {@code thrown}.
 * </p>
 */
package xyz.swatt.exceptions;