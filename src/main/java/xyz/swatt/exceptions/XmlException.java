package xyz.swatt.exceptions;

/**
 * This Exception represents failures in XML parsing / manipulation.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class XmlException extends RuntimeException {

	//========================= Static Enums ===================================

	//========================= STATIC CONSTANTS ===============================
	private static final long serialVersionUID = -4759721633736849807L;

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
	 *
	 * @see RuntimeException#RuntimeException()
	 */
	public XmlException() {
		super();
	}

	/**
	 * @param message
	 *            the detail message. The detail message is saved for
	 *            later retrieval by the {@link RuntimeException#getMessage()}
	 *            method.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see RuntimeException#RuntimeException(String)
	 */
	public XmlException(String message) {
		super( message );
	}

	/**
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link RuntimeException#getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent
	 *            or unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see RuntimeException#RuntimeException(String)
	 */
	public XmlException(Throwable cause) {
		super( cause );
	}

	/**
	 * @param message
	 *            the detail message. The detail message is saved for
	 *            later retrieval by the {@link RuntimeException#getMessage()}
	 *            method.
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link RuntimeException#getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent
	 *            or unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public XmlException(String message, Throwable cause) {
		super( message, cause );
	}

	//========================= Methods ========================================
}
