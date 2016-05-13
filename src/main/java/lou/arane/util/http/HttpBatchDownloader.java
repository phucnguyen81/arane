package lou.arane.util.http;

import java.nio.file.Path;
import java.util.LinkedList;

import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.Uri;
import lou.arane.util.Util;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class HttpBatchDownloader {

	private final LinkedList<HttpDownloader> items = New.linkedList();

    private int maxDownloadAttempts = 1;

    public HttpBatchDownloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of uri-path to download */
    public void add(Uri uri, Path path) {
        HttpDownloader item = new HttpDownloader(uri, path);
        item.downloadAttempts = maxDownloadAttempts;
		items.add(item);
    }

    /** Download the pairs of uri-path that were added */
    public void download() {
        while (!items.isEmpty()) {
            HttpDownloader item = items.removeFirst();
            if (item.canTryDownload()) {
            	Log.info("Start " + item);
	        	try {
	        		item.tryDownload();
	        	}
	        	catch (RuntimeException e) {
	        		Log.error(e);
	        		// re-try later
	        		if (item.canTryDownload()) {
	        			items.addLast(item);
	        		}
	        	}
            }
        }
    }

    /** Sort download by the target path */
    public void sortByPath() {
        items.sort((d1, d2) -> d1.path.compareTo(d2.path));
    }

	@Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(items, Util.LINE_BREAK);
		return String.format("%s[%n%s%n]", className, joinedItems);
    }
}
