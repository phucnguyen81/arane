package lou.arane.base.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import lou.arane.base.IOFactory;

/**
 * Enhance a {@link IOFactory} to copy bytes/chars from
 * the byte/char streams the factory produces.
 *
 * @author Phuc
 */
public final class IOCopy extends IOWrap {

	private static final int DEFAULT_BUFFER_SIZE = 1024* 8;

	private final int bufferSize;

	public IOCopy(IOFactory origin) {
		this(origin, DEFAULT_BUFFER_SIZE);
	}

	public IOCopy(IOFactory origin, int bufferSize) {
		super(origin);
		if (bufferSize <= 0) {
			throw new AssertionError(bufferSize + " is not positive size");
		}
		this.bufferSize = bufferSize;
	}

	/** Copy bytes from input to output */
	public void copyBytes() throws Exception {
		try ( InputStream source = new BufferedInputStream(input())
			; OutputStream sink = new BufferedOutputStream(output())
		){
			copy(source, sink);
		}
	}

	/** Copy chars from reader to writer */
	public void copyChars() throws Exception {
		try ( Reader source = new BufferedReader(reader())
			; Writer sink = new BufferedWriter(writer())
		){
			copy(source, sink);
		}
	}

	/** @see Files#copy(InputStream, OutputStream) */
	private void copy(InputStream source, OutputStream sink)
		throws Exception
	{
		byte[] buf = new byte[bufferSize];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
		}
	}

	/** @see Files#copy(InputStream, OutputStream) */
	private void copy(Reader source, Writer sink)
		throws Exception
	{
		char[] buf = new char[bufferSize];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
		}
	}

	@Override
	public String toString() {
		return String.format("Copy:%n  bufferSize:%s%n  origin:%s"
			, bufferSize, super.toString());
	}

}
