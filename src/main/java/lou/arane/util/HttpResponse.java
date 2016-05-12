package lou.arane.util;

import java.io.InputStream;
import java.net.HttpURLConnection;

/** Results from making http connection */
public class HttpResponse implements AutoCloseable {

	public final HttpURLConnection conn;

	public final InputStream input;

	public final int code;

	public HttpResponse(HttpURLConnection conn) throws Exception {
		this.conn = conn;
		this.input = conn.getInputStream();
		this.code = conn.getResponseCode();
	}

	/** Whether response code indicates error */
	public boolean hasErrorCode() throws Exception {
		int code = conn.getResponseCode();
		return code < 200 || code > 299;
	}

	@Override
	public void close() throws Exception {
		input.close();
		conn.disconnect();
	}
}