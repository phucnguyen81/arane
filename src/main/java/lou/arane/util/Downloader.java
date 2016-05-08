package lou.arane.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloader {

	/** Data for a single download operation */
	static class Item {
		final Uri uri;
		final Path path;

		Duration timeout = Duration.of(2, ChronoUnit.MINUTES);

		final LinkedList<IOException> exceptions = New.linkedList();

		Item(Uri uri, Path path) {
			this.uri = uri;
			this.path = path;
		}

		@Override
		public String toString() {
			return String.format("[%s -> %s]", uri, path);
		}
	}

	private final LinkedList<Item> items = New.linkedList();

    private int maxDownloadAttempts = 1;

    public Downloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }


    /** Add a pair of uri-path to download */
    public void add(Uri uri, Path path) {
        items.add(new Item(uri, path));
    }

    /** Sort download order by the target path */
    public void sortByPath() {
        items.sort((d1, d2) -> d1.path.compareTo(d2.path));
    }

    /** Download the pairs of uri-path that were added */
    public void download() {
        while (!items.isEmpty()) {
            Item item = items.removeFirst();
            if (Util.exists(item.path)) continue;
            try {
            	Log.info("Start " + item);
                download(item);
            }
            catch (IOException e) {
                Log.error(e);
                handleError(item, e);
            }
        }
    }

    /** Download from item uri to item path */
	private void download(Item item) throws IOException {
		Path parentDir = item.path.toAbsolutePath().getParent();
		Files.createDirectories(parentDir);
		byte[] bytes = getBytes(item);
		Files.write(item.path, bytes);
	}

    /**
     * Get uri content as bytes. Try alternate uris if the original one
     * fails. Throw only the last error.
     */
    private static byte[] getBytes(Item item) throws IOException {
        List<Uri> uris = New.list();
        uris.add(item.uri);
        uris.addAll(item.uri.getAlternatives());

        DownloadResponse r = null;
        for (Uri uri : uris) {
        	r = Util.request(uri.toUriString(), item.timeout);
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

	private void handleError(Item item, IOException e) {
		item.exceptions.add(e);
		int downloadErrors = item.exceptions.size();
		if (downloadErrors < maxDownloadAttempts) {
		    items.addLast(item);
		}
	}

    @Override
    public String toString() {
    	return String.format("%s[%n%s%n]",
    		getClass().getSimpleName(),
    		Util.join(items, Util.LINE_BREAK));
    }
}
