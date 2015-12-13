package lou.arane.util;

/**
 * Exceptions that cannot be handled or simply should not happen.
 * For example: UTF-8 encoding exception, local files io exception, etc.
 * Not meant to wrap unchecked exeption.
 *
 * @author LOU
 */
@SuppressWarnings("serial")
public class Unchecked extends RuntimeException {

    public Unchecked(String message) {
        super(message);
    }

    public Unchecked(String message, Exception cause) {
        super(message, cause);
    }

    public Unchecked(Exception cause) {
        super(cause);
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
