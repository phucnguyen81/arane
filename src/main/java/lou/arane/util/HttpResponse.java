package lou.arane.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/** Results from making http connection */
public class HttpResponse implements Closeable {

	private final HttpURLConnection conn;

	private final InputStream content;

	private final int status;

	public HttpResponse(HttpURLConnection conn) {
		this.conn = conn;
		try {
			this.content = conn.getInputStream();
			this.status = conn.getResponseCode();
		} catch (IOException e) {
			throw New.unchecked(e);
		}
	}

	/** Whether response code indicates error */
	public boolean hasErrorStatus() {
		return status < 200 || status > 299;
	}

	/** Write content to output */
	public void copyTo(OutputStream output) {
		try {
			IO.copy(content, output);
		} catch (IOException e) {
			throw New.unchecked(e);
		}
	}

	@Override
	public void close() {
		try {
			content.close();
		} catch (IOException e) {
			throw New.unchecked(e);
		} finally {
			conn.disconnect();
		}
	}

	@Override
	public String toString() {
	    return new ToString(HttpResponse.class).join("conn", conn).join("status", status).render();
	}

}