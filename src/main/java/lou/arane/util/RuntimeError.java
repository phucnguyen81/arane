package lou.arane.util;

/**
 * Exceptions that cannot be handled or simply should not happen.
 * For example: UTF-8 encoding exception, local files io exception, etc.
 * Not meant to wrap unchecked exeption.
 *
 * @author LOU
 */
@SuppressWarnings("serial")
public class RuntimeError extends RuntimeException {

    public RuntimeError(String message) {
        super(message);
    }

    public RuntimeError(String message, Exception cause) {
        super(message, cause);
    }

    public RuntimeError(Exception cause) {
        super(cause);
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
