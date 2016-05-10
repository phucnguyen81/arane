package lou.arane.util;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Perform a single download operation */
public class DownloadItem {

	public final Uri uri;
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
	public void tryDownload() throws IOException {
		if (canTryDownload()) try {
			Util.tryWrite(path, getBytes());
		} catch (IOException e) {
			downloadAttempts -= 1;
			throw e;
		}
		else {
			throw new IOException("Download attempts exceeded for " + this);
		}
	}

    /**
     * Get uri content as bytes. Try alternate uris if the original one
     * fails. Throw only the last error.
     */
    private byte[] getBytes() throws IOException {
        List<Uri> uris = New.list();
        uris.add(uri);
        uris.addAll(uri.getAlternatives());

        DownloadResponse r = null;
        for (Uri uri : uris) {
        	r = Util.request(uri.toUriString(), timeout);
        	if (r.error == null) return r.content;
        }
        if (r == null) {
        	throw new Unchecked("Downloading fails without response for " + uris);
        } else if (r.error == null) {
        	throw new Unchecked("Downloading fails without error for " + uris);
        } else {
        	throw r.error;
        }
    }

	@Override
	public String toString() {
		return String.format("[%s -> %s]", uri, path);
	}
}