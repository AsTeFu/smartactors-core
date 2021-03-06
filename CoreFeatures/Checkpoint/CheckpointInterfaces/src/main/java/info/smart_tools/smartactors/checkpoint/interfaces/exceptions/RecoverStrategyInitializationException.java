package info.smart_tools.smartactors.checkpoint.interfaces.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.checkpoint.interfaces.IRecoverStrategy recover strateggy} when it cannot be
 * initialized.
 */
public class RecoverStrategyInitializationException extends Exception {
    /**
     * The constructor.
     *
     * @param msg      the message
     * @param cause    the cause
     */
    public RecoverStrategyInitializationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
