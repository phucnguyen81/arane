package lou.arane.util.http;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;

/**
 * Downloads content from url via http protocol. Checked-exceptions are
 * re-thrown as {@link HttpIOException}.
 */
public class HttpDownloader {

	private final long timeout;
	private final TimeUnit timeoutUnit;

	/** Create a downloader with default timeout */
	public HttpDownloader() {
		this(2, MINUTES);
	}

	/** Create a downloader with a given timeout */
	public HttpDownloader(long timeout, TimeUnit timeoutUnit) {
		Check.require(timeout > 0, "Expect positive timeout, actual value: " + timeout);
		Check.notNull(timeoutUnit, "Null time unit");
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	public byte[] getBytes(Uri uri) throws HttpIOException {
		return getBytes(uri.uri);
	}

	public byte[] getBytes(URI uri) throws HttpIOException {
		try {
			return getBytes(uri.toURL());
		} catch (MalformedURLException urlError) {
			throw new HttpIOException(urlError);
		}
	}

	public byte[] getBytes(URL url) throws HttpIOException {
		return getBytes(url.toString());
	}

	public byte[] getBytes(String url) throws HttpIOException {
		try {
			return getBytesInternal(url);
		} catch (IOException e) {
			throw new HttpIOException(e);
		}
	}

	/**
	 * Core method for downloading content at url.
	 * <p>
	 * TODO performance seems not good.
	 * Check out apache-http-components or async-http-client for improving performance.
	 * Try to do it with just Java Standard.
	 */
	private byte[] getBytesInternal(String urlStr) throws HttpIOException, IOException {
		Check.notNull(urlStr, "Null url");
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try (Closeable disconnect = () -> conn.disconnect()) {
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept-Charset", Util.ENCODING);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
			conn.setConnectTimeout(getTimeoutInMillis());
			conn.setReadTimeout(getTimeoutInMillis());

			int responseCode = conn.getResponseCode();
			if (responseCode < 200 || responseCode > 299) {
				throw new HttpIOException(String.format(
					"Failed to download from %s %n %s", urlStr, conn.getContent()));
			} else {
				return Util.read(conn.getInputStream());
			}
		}
	}

	private int getTimeoutInMillis() {
		return (int) TimeUnit.MILLISECONDS.convert(timeout, timeoutUnit);
	}
}
