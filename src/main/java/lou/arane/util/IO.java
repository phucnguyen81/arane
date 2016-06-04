package lou.arane.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * I/O operations for this app.
 *
 * @author Phuc
 */
public class IO {

	/** default buffer size for I/O operations */
	private static final int BUFFER_SIZE = 1024 * 8;

	/** default encoding for I/O operations */
	public static String encoding() {
		return charset().name();
	}

	/** default charset for I/O operations */
	public static Charset charset() {
		return StandardCharsets.UTF_8;
	}

	/**
	 * Copy all bytes from input to output. The streams are buffered before
	 * copying
	 */
	public static void copy(InputStream source, OutputStream sink) throws IOException {
		copy(new BufferedInputStream(source), new BufferedOutputStream(sink), BUFFER_SIZE);
	}

	/** @see Files#copy(InputStream, OutputStream) */
	public static void copy(BufferedInputStream in, BufferedOutputStream out, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
	}

	/**
	 * Copy all chars from a reader to a writer. The reader/writer are buffered
	 * before copying
	 */
	public static void copy(Reader reader, Writer writer) throws IOException {
		copy(new BufferedReader(reader), new BufferedWriter(writer), BUFFER_SIZE);
	}

	/** @see Files#copy(InputStream, OutputStream) */
	public static void copy(BufferedReader reader, BufferedWriter writer, int bufferSize) throws IOException {
		char[] buf = new char[bufferSize];
		int n;
		while ((n = reader.read(buf)) > 0) {
			writer.write(buf, 0, n);
		}
	}

	/**
	 * Limited version of {@link #write(Object, Path, Charset). Encoding
	 * defaults to {@link #charset()}
	 */
	public static void write(Object o, Path file) throws IOException {
		write(o, file, charset());
	}

	/**
	 * Write string reprentation of an object to file. The file is created if
	 * not exists.
	 */
	public static void write(Object o, Path file, Charset charset) throws IOException {
		Util.createFileIfNotExists(file);
		try (Reader reader = new StringReader(o.toString());
				Writer writer = Files.newBufferedWriter(file, charset)) {
			copy(reader, writer);
		}
	}

	/**
	 * Make http GET request
	 *
	 * @return the connection just openned
	 */
	public static HttpURLConnection httpGET(URL url, Charset charset, Duration timeout)
			throws IOException, ProtocolException {
		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept-Charset", charset.name());
		conn.setConnectTimeout((int) timeout.toMillis());
		conn.setReadTimeout((int) timeout.toMillis());
		// pretend to be Mozilla
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
		return conn;
	}

}
