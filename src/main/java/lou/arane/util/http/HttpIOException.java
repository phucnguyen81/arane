package lou.arane.util.http;

/**
 * Capture an exception related to input-output operations using http protocol.
 *
 * @author pnguyen58
 */
@SuppressWarnings("serial")
public class HttpIOException extends Exception {

    public HttpIOException(String message) {
        super(message);
    }

    public HttpIOException(Exception cause) {
        super(cause);
    }

}
