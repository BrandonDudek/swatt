package xyz.swatt.exceptions;

/**
 * A generic Exception that should be thrown when more results are returned than expected.
 *
 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
 */
public class TooManyResultsException extends AbstractSwattException {

	//========================= Static Enums ===================================

	//========================= STATIC CONSTANTS ===============================
	private static final long serialVersionUID = -4997983256521195706L;

	//========================= Static Variables ===============================

	//========================= Static Constructor =============================
	static {

	}

	//========================= Static Methods =================================

	//========================= CONSTANTS ======================================

	//========================= Variables ======================================

	//========================= Constructors ===================================
	/**
	 * Constructs a new runtime exception with {@code null} as its
	 * detail message.  The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link RuntimeException#initCause}.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see AbstractSwattException#AbstractSwattException()
	 */
	public TooManyResultsException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param   _message   the detail message. The detail message is saved for
	 *          later retrieval by the {@link RuntimeException#getMessage()} method.
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see AbstractSwattException#AbstractSwattException(String)
	 */
	public TooManyResultsException(String _message) {
		super(_message);
	}

	/**
	 * Constructs a new runtime exception with the specified cause and a
	 * detail message of <tt>(cause==null ? null : cause.toString())</tt>
	 * (which typically contains the class and detail message of
	 * <tt>cause</tt>).  This constructor is useful for runtime exceptions
	 * that are little more than wrappers for other throwables.
	 *
	 * @param  _cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see AbstractSwattException#AbstractSwattException(Throwable)
	 */
	public TooManyResultsException(Throwable _cause) {
		super(_cause);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this runtime exception's detail message.
	 *
	 * @param  _message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param  _cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 *
	 * @author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)
	 *
	 * @see AbstractSwattException#AbstractSwattException(String, Throwable)
	 */
	public TooManyResultsException(String _message, Throwable _cause) {
		super(_message, _cause);
	}

	//========================= Methods ========================================
}
