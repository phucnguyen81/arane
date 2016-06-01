package lou.arane.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import lou.arane.base.IOFactory;

/**
 * {@link IOFactory} that knows the {@link Charset} that should be used for
 * reading and writing.
 *
 * @author Phuc
 */
public final class IOText implements IOFactory {

	private final IOFactory origin;
	private final Charset forRead;
	private final Charset forWrite;

	public IOText(IOFactory origin, Charset cs) {
		this(origin, cs, cs);
	}

	public IOText(IOFactory origin, Charset forRead, Charset forWrite) {
		this.origin = origin;
		this.forRead = forRead;
		this.forWrite = forWrite;
	}

	@Override
	public InputStream input() throws IOException {
		return origin.input();
	}

	@Override
	public OutputStream output() throws IOException {
		return origin.output();
	}

	@Override
	public Reader reader() throws IOException {
		return new InputStreamReader(origin.input(), forRead);
	}

	@Override
	public Writer writer() throws IOException {
		return new OutputStreamWriter(origin.output(), forWrite);
	}

	@Override
	public String toString() {
		return String.format("Text:%n  origin:%s%n  %read:%s%n  write:%s"
			, origin, forRead, forWrite);
	}

}
