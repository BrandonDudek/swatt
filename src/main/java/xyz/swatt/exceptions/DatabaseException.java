package xyz.swatt.exceptions;

/**
 * A generic Runtime Exception for working with Databases.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class DatabaseException extends RuntimeException {
	
	//========================= Static Enums ===================================
	
	//========================= STATIC CONSTANTS ===============================
	private static final long serialVersionUID = -4642528140232850324L;
	
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
	public DatabaseException() {
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
	public DatabaseException(String message) {
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
	public DatabaseException(Throwable cause) {
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
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	//========================= Methods ========================================
}
