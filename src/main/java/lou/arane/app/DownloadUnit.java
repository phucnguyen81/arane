package lou.arane.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import lou.arane.core.Cmd;
import lou.arane.util.HttpResponse;
import lou.arane.util.IO;
import lou.arane.util.New;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Unit of download is from a url to a file.
 *
 * @author Phuc
 */
public class DownloadUnit implements Cmd {

	private final URLResource source;
	private final Path target;
	private final Duration timeout;

	public DownloadUnit(URLResource source, Path target, Duration timeout) {
		this.source = source;
		this.target = target;
		this.timeout = timeout;
	}

	@Override
	public boolean canRun() {
		return Util.notExists(target);
	}

	@Override
	public void doRun() {
		try {
			getFromUrl();
		}
		catch (IOException e) {
			throw New.unchecked(e);
		}
	}

	private void getFromUrl() throws IOException {
    	try (HttpResponse res = source.httpGET(timeout)) {
    		if (res.hasErrorStatus()) {
    			throw new IOException(String.format(
    				"Downloading: %s gives error status: %s", source, res));
    		}
    		/* Copy in 2 stages to reduce the change of incomplete download
    		 * being written to file */
    		copyToFile(copyToBuffer(res));
    	}
	}

    private InputStream copyToBuffer(HttpResponse r) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        r.copyTo(output);
        return new ByteArrayInputStream(output.toByteArray());
    }

    private void copyToFile(InputStream input) throws IOException {
        try (OutputStream output = Files.newOutputStream(target)) {
            Util.createFileIfNotExists(target);
            IO.copy(input, output);
        }
    }

}
