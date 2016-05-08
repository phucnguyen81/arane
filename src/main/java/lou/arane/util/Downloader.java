package lou.arane.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloader {

	private final LinkedList<DownloadItem> downloadItems = New.linkedList();

    private int maxDownloadAttempts = 1;

    public Downloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }


    /** Add a pair of uri-path to download */
    public void add(Uri uri, Path path) {
        downloadItems.add(new DownloadItem(uri, path));
    }

    /** Sort download order by the target path */
    public void sortByPath() {
        downloadItems.sort((d1, d2) -> d1.path.compareTo(d2.path));
    }

    /** Download the pairs of uri-path that were added */
    public void download() {
        while (!downloadItems.isEmpty()) {
            DownloadItem downloadItem = downloadItems.removeFirst();
            if (Util.notExists(downloadItem.path)) {
                try {
                	Log.info("Start " + downloadItem);
                    download(downloadItem);
                }
                catch (IOException e) {
                    Log.error(e);
                    handleDownloadErrors(downloadItem);
                }
            }
        }
    }

    /** Download uri content and save to path. Keep track of error. */
    public static void download(DownloadItem downloadItem) throws IOException {
        IOException error = null;
        try {
            Path parentDir = downloadItem.path.toAbsolutePath().getParent();
            Files.createDirectories(parentDir);
            Files.write(downloadItem.path, downloadBytes(downloadItem));
        }
        catch (IOException e) {
            error = e;
        }
        finally {
            if (error != null) {
                downloadItem.exceptions.add(error);
                throw error;
            }
        }
    }

    /**
     * Download uri content as bytes. Try alternate uris if the original one
     * fails. Throw only the last error.
     */
    public static byte[] downloadBytes(DownloadItem downloadItem) throws IOException {
        List<Uri> uris = New.list();
        uris.add(downloadItem.uri);
        uris.addAll(downloadItem.uri.getAlternatives());

        DownloadResponse r = null;
        for (Uri uri : uris) {
        	r = Util.getUrl(uri.uri.toURL().toString(), downloadItem.timeout);
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

    /** Retry download later if there are not too many errors */
    private void handleDownloadErrors(DownloadItem downloadItem) {
        int downloadErrors = downloadItem.exceptions.size();
        if (downloadErrors < maxDownloadAttempts) {
            downloadItems.addLast(downloadItem);
        }
    }

    @Override
    public String toString() {
    	return String.format("%s[%n%s%n]",
    		getClass().getSimpleName(),
    		Util.join(downloadItems, Util.LINE_BREAK));
    }
}
