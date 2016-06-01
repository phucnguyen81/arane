package lou.arane.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import lou.arane.base.IOFactory;

/**
 * Decorator of {@link IOFactory}.
 *
 * @author Phuc
 */
public abstract class IOWrap implements IOFactory {

	private final IOFactory origin;

	public IOWrap(IOFactory origin) {
		this.origin = origin;
	}

	@Override
	public final InputStream input() throws IOException {
		return origin.input();
	}

	@Override
	public final OutputStream output() throws IOException {
		return origin.output();
	}

	@Override
	public final Reader reader() throws IOException {
		return origin.reader();
	}

	@Override
	public final Writer writer() throws IOException {
		return origin.writer();
	}

	@Override
	public String toString() {
		return String.format("Wrap:%n  %s%n)", origin);
	}

}
