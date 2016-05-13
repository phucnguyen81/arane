package lou.arane.util;

import java.io.InputStream;
import java.net.HttpURLConnection;

/** Results from making http connection */
public class HttpResponse implements AutoCloseable {

	public final HttpURLConnection conn;

	public final InputStream input;

	public final int code;

	public HttpResponse(HttpURLConnection conn) {
		this.conn = conn;
		try {
			this.input = conn.getInputStream();
			this.code = conn.getResponseCode();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Whether response code indicates error */
	public boolean hasErrorCode() {
		try {
			int code = conn.getResponseCode();
			return code < 200 || code > 299;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			input.close();
			conn.disconnect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}