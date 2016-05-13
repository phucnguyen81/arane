package lou.arane.util;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Perform a single download operation */
public class DownloadItem {

	public Uri uri;
	public final Path path;

	public Duration timeout = Duration.of(2, ChronoUnit.MINUTES);

	public int downloadAttempts = 1;

	public DownloadItem(Uri uri, Path path) {
		this.uri = uri;
		this.path = path;
	}

	/** Whether next call to {@link #tryDownload()} has any effect */
	public boolean canTryDownload() {
		return downloadAttempts > 0 && Util.notExists(path);
	}

	/** Download from item uri to item path */
	public void tryDownload() {
		if (canTryDownload()) try {
			download();
		} catch (RuntimeException e) {
			downloadAttempts -= 1;
			throw e;
		}
		else {
			throw new RuntimeException("Download attempts exceeded for " + this);
		}
	}

    /** Download from uri to file.
     * Try alternate uris if the original one fails.
     * Throw only the error of the last uri being tried. */
    private void download() {
        List<Uri> uris = New.list();
        uris.add(uri);
        uris.addAll(uri.getAlternatives());
        while (!uris.isEmpty()) {
        	Uri aUri = uris.remove(0);
        	try {
        		IO.download(aUri.toUriString(), path, timeout);
        		return;
        	}
        	catch (RuntimeException e) {
        		if (uris.isEmpty()) {
        			throw e;
        		}
        	}
        }
        throw new AssertionError("This error should be unreachable!");
    }

    @Override
	public String toString() {
		return String.format("[%s -> %s]", uri, path);
	}
}