package lou.arane.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloader {

	private final LinkedList<DownloadItem> items = New.linkedList();

    private int maxDownloadAttempts = 1;

    public Downloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of uri-path to download */
    public void add(Uri uri, Path path) {
        DownloadItem item = new DownloadItem(uri, path);
        item.downloadAttempts = maxDownloadAttempts;
		items.add(item);
    }

    /** Download the pairs of uri-path that were added */
    public void download() {
        while (!items.isEmpty()) {
            DownloadItem item = items.removeFirst();
            if (item.canTryDownload()) {
            	Log.info("Start " + item);
	        	try {
	        		item.tryDownload();
	        	}
	        	catch (IOException e) {
	        		Log.error(e);
	        		// re-try later
	        		if (item.canTryDownload()) {
	        			items.addLast(item);
	        		}
	        	}
            }
        }
    }

    /** Sort download order by the target path */
    public void sortByPath() {
        items.sort((d1, d2) -> d1.path.compareTo(d2.path));
    }

	@Override
    public String toString() {
    	return String.format("%s[%n%s%n]",
    		getClass().getSimpleName(),
    		Util.join(items, Util.LINE_BREAK));
    }
}
