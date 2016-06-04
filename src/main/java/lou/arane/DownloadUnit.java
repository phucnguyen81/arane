package lou.arane;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		Util.createFileIfNotExists(target);
    	try (OutputStream output = Files.newOutputStream(target);
    			HttpResponse res = source.httpGET(timeout);) {
    		if (res.hasErrorStatus()) {
    			throw new RuntimeException(String.format(
    				"Downloading: %s gives error status: %s", source, res));
    		}
    		/* copy in 2 phases to reduce the chance of incomplete download */
    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    		res.copyTo(buffer);
    		IO.copy(new ByteArrayInputStream(buffer.toByteArray()), output);
    	}
    	catch (IOException e) {
    		throw New.unchecked(e);
		}
	}

}
