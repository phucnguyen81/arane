package lou.arane.util.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.Util.Response;

/**
 * Downloads content from url via http protocol. Checked-exceptions are
 * re-thrown as {@link HttpIOException}.
 */
public class HttpDownloader {

	private final Duration timeout;

	/** Create a downloader with default timeout */
	public HttpDownloader() {
		this(Duration.of(2, ChronoUnit.MINUTES));
	}

	/** Create a downloader with a given timeout */
	public HttpDownloader(Duration timeout) {
		Check.notNull(timeout, "Null timeout");
		Check.prevent(timeout.isNegative() || timeout.isZero(),
			"Expect positive timeout, actual value: " + timeout);
		this.timeout = timeout;
	}

	public byte[] getBytes(Uri uri) throws IOException {
		return getBytes(uri.uri);
	}

	public byte[] getBytes(URI uri) throws IOException {
		try {
			return getBytes(uri.toURL());
		} catch (MalformedURLException urlError) {
			throw new IOException(urlError);
		}
	}

	public byte[] getBytes(URL url) throws IOException {
		return getBytes(url.toString());
	}

	public byte[] getBytes(String url) throws IOException {
		return getBytesInternal(url);
	}

	/**
	 * Core method for downloading content at url.
	 * <p>
	 * FIXME no need to make a class for this single method.
	 */
	private byte[] getBytesInternal(String urlStr) throws IOException {
		Response r = Util.getUrl(urlStr, timeout);
		if (r.exception != null) throw r.exception;
		else return r.content;
	}

}
