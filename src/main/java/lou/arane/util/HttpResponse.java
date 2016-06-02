package lou.arane.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/** Results from making http connection */
public class HttpResponse implements Closeable {

	private final HttpURLConnection conn;

	private final InputStream input;

	private final int status;

	public HttpResponse(HttpURLConnection conn) {
		this.conn = conn;
		this.input = Unchecked.tryGet(() -> conn.getInputStream());
		this.status = Unchecked.tryGet(() -> conn.getResponseCode());
	}

	/** Whether response code indicates error */
	public boolean hasErrorStatus() {
		return status < 200 || status > 299;
	}

	/** Write content to output */
	public void copyTo(OutputStream output) throws IOException {
		IO.copy(input, output);
	}

	@Override
	public void close() throws IOException {
		input.close();
		conn.disconnect();
	}

	@Override
	public String toString() {
		return String.format("Response:%n  conn:%s%n  status:%s", conn, status);
	}

}