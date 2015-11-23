package lou.arane.util.http;

import java.nio.file.Path;
import java.util.LinkedList;

import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.Uri;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class HttpFileBatchDownloader {

    private final LinkedList<HttpFileDownloader> downloaders = New.linkedList();

    private Integer maxDownloadAttempts = null;

    public HttpFileBatchDownloader setMaxDownloadAttempts(Integer maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of uri-path to download */
    public void add(Uri uri, Path path) {
        downloaders.add(new HttpFileDownloader(uri, path));
    }

    /** Sort download order by the target path */
    public void sortByPath() {
        downloaders.sort((d1, d2) -> d1.getPath().compareTo(d2.getPath()));
    }

    /** Download the pairs of uri-path that were added */
    public void download() {
        while (!downloaders.isEmpty()) {
            HttpFileDownloader downloader = downloaders.removeFirst();
            if (!downloader.pathExists()) {
                try {
                    downloader.download();
                }
                catch (HttpIOException e) {
                    // FIXME show stack trace here is too ugly, at least use log
                    e.printStackTrace();
                    handleDownloadErrors(downloader);
                }
            }
        }
    }

    /** Retry download later if there are not too many errors */
    private void handleDownloadErrors(HttpFileDownloader downloader) {
        int downloadErrors = downloader.getDownloadExceptions().size();
        if (maxDownloadAttempts == null || downloadErrors < maxDownloadAttempts) {
            downloaders.addLast(downloader);
        }
    }

    @Override
    public String toString() {
        return downloaders.toString();
    }
}
