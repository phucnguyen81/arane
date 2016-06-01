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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

	/** Copy all bytes from input to output.
	 * @return the number of bytes copied */
	public static long copy(InputStream source, OutputStream sink) {
		return Try.toGet(() -> tryCopy(
			new BufferedInputStream(source)
			, new BufferedOutputStream(sink)));
	}

	/** @see Files#copy(InputStream, OutputStream) */
	public static long tryCopy(InputStream source, OutputStream sink)
		throws Exception
	{
		long nread = 0L;
		byte[] buf = new byte[BUFFER_SIZE];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}

	/** Limited version of {@link #write(Object, Path, Charset).
	 * Encoding defaults to {@link #charset()} */
	public static void write(Object o, Path file) {
		write(o, file, charset());
	}

	/** Write string reprentation of an object to file.
	 * The file is created if not exists. */
	public static void write(Object o, Path file, Charset charset) {
		Util.createFileIfNotExists(file);
	    Try.toDo(() -> {
	    	try ( Reader reader = new StringReader(o.toString())
	    		; Writer writer = Files.newBufferedWriter(file, charset)
	    	){
	    		copy(reader, writer);
	    	}
	    });
	}

	/** Copy all chars from a reader to a writer.
	 * @return the number of chars copied */
	public static long copy(Reader reader, Writer writer) {
		return Try.toGet(() -> tryCopy(
			new BufferedReader(reader)
			, new BufferedWriter(writer)));
	}

	/** @see Files#copy(InputStream, OutputStream) */
	public static long tryCopy(Reader reader, Writer writer)
		throws Exception
	{
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
