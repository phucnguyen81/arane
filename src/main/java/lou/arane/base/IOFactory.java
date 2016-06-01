package lou.arane.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Things that can be read from and written to.
 *
 * @author Phuc
 */
public interface IOFactory {

	default InputStream input() throws IOException {
		throw new UnsupportedOperationException("Reading not supported");
	}

	default OutputStream output() throws IOException {
		throw new UnsupportedOperationException("Writing not supported");
	}

	default Reader reader() throws IOException {
		return new InputStreamReader(input());
	}

	default Writer writer() throws IOException {
		return new OutputStreamWriter(output());
	}

}
