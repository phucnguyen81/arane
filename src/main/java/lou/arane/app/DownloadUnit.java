package lou.arane.app;

import java.io.IOException;

import lou.arane.core.Cmd;
import lou.arane.util.FileResource;
import lou.arane.util.HttpResponse;
import lou.arane.util.New;
import lou.arane.util.URLResource;

/**
 * Unit of download is downloading from url to file.
 *
 * @author Phuc
 */
public class DownloadUnit implements Cmd {

	private final URLResource source;
	private final FileResource target;

	public DownloadUnit(URLResource source, FileResource target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public boolean canRun() {
	    return target.notExists();
	}

	@Override
	public void doRun() {
		try {
			download();
		}
		catch (IOException e) {
			throw New.unchecked(e);
		}
	}

	private void download() throws IOException {
    	try (HttpResponse res = source.httpGET()) {
    		if (res.hasErrorStatus()) {
    			throw new IOException(String.format(
    				"Downloading: %s gives error status: %s", source, res));
    		}
		    res.copyTo(target);
    	}
	}

}
