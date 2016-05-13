package lou.arane.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import lou.arane.util.http.HttpResponse;

/**
 * I/O operations for this app.
 *
 * @author Phuc
 */
public class IO {

	/** default charset for I/O operations */
	public static final Charset CHARSET = StandardCharsets.UTF_8;

	/** default encoding for I/O operation */
	public static final String ENCODING = CHARSET.name();

	/** buffer size used for I/O operations */
	public static final int BUFFER_SIZE = 1024 * 8;

	public static void createFileIfNotExists(Path file) {
		try {
			if (Files.notExists(file)) {
				Files.createDirectories(file.getParent());
				Files.createFile(file);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Download from a url to a file.
	 * If there is no exception, the right content should be downloaded to the file. */
	public static void download(String url, Path file, Duration timeout) {
		createFileIfNotExists(file);
		try ( HttpResponse response = get(url, timeout)
	    	; OutputStream output = Files.newOutputStream(file)
	    ){
			if (response.hasErrorCode()) {
				throw new RuntimeException(String.format("Error code is %s for getting %s", response.code, url));
	    	} else {
	    		copy(response.input, output);
	    	}
	    }
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpResponse get(String aUrl, Duration timeout) {
		try {
			URL url = new URL(aUrl);
			return get(url, timeout);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpResponse get(URL url, Duration timeout) {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			return get(conn, timeout);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpResponse get(HttpURLConnection conn, Duration timeout) {
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			throw new RuntimeException(e);
		}
		conn.setRequestProperty("Accept-Charset", ENCODING);
		// pretend to be Mozilla since some server might check it
		conn.setRequestProperty("User-Agent",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
		conn.setConnectTimeout((int) timeout.toMillis());
		conn.setReadTimeout((int) timeout.toMillis());
		return new HttpResponse(conn);
	}

	/** Copy all bytes from input to output.
	 * @return the number of bytes copied */
	public static long copy(InputStream source, OutputStream sink) {
		source = new BufferedInputStream(source);
		sink = new BufferedOutputStream(sink);
		try {
			return tryCopy(source, sink);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** @see Files#copy(InputStream, OutputStream) */
	private static long tryCopy(InputStream source, OutputStream sink) throws Exception {
		long nread = 0L;
		byte[] buf = new byte[BUFFER_SIZE];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}

	/** Write string reprentation of an object to file.
	 * The file is created if not exists.
	 * Encoding is set to {@link #CHARSET}. */
	public static void write(Object o, Path file) {
		write(o, file, CHARSET);
	}

	/** Extended version of {@link #write(Object, Path)} with explicit charset */
	public static void write(Object o, Path file, Charset charset) {
		createFileIfNotExists(file);
	    try ( Reader reader = new StringReader(o.toString())
	    	; Writer writer = Files.newBufferedWriter(file, charset)
	    ){
	    	copy(reader, writer);
	    }
	    catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	}

	/** Copy all chars from a reader to a writer.
	 * @return the number of chars copied
	 * @see #copy(InputStream, OutputStream) */
	public static long copy(Reader reader, Writer writer) {
		reader = new BufferedReader(reader);
		writer = new BufferedWriter(writer);
		try {
			return tryCopy(reader, writer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static long tryCopy(Reader reader, Writer writer) throws Exception {
		long nread = 0L;
		char[] buf = new char[BUFFER_SIZE];
		int n;
		while ((n = reader.read(buf)) > 0) {
			writer.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}

}
