package xyz.swatt.exceptions;

/**
 * A generic Runtime Exception, to be used when an error occures in any conversion operation.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class ConversionException extends RuntimeException {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	private static final long serialVersionUID = 8433606127617483980L;
	
	//========================= Static Variables ===============================
	
	//========================= Static Constructor =============================
	static {
	
	}
	
	//========================= Static Methods =================================
	
	//========================= CONSTANTS ======================================
	
	//========================= Variables ======================================
	
	//========================= Constructors ===================================
	
	/**
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see RuntimeException#RuntimeException()
	 */
	public ConversionException() {
		super();
	}
	
	/**
	 * @param message
	 * 		the detail message. The detail message is saved for
	 * 		later retrieval by the {@link RuntimeException#getMessage()}
	 * 		method.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see RuntimeException#RuntimeException(String)
	 */
	public ConversionException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 * 		the cause (which is saved for later retrieval by the
	 *        {@link RuntimeException#getCause()} method). (A <tt>null</tt>
	 * 		value is permitted, and indicates that the cause is nonexistent
	 * 		or unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see RuntimeException#RuntimeException(String)
	 */
	public ConversionException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message
	 * 		the detail message. The detail message is saved for
	 * 		later retrieval by the {@link RuntimeException#getMessage()}
	 * 		method.
	 * @param cause
	 * 		the cause (which is saved for later retrieval by the
	 *        {@link RuntimeException#getCause()} method). (A <tt>null</tt>
	 * 		value is permitted, and indicates that the cause is nonexistent
	 * 		or unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	//========================= Methods ========================================
}
