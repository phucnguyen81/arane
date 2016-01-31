package lou.arane.util;

import java.nio.file.Path;

import lou.arane.util.http.HttpFileBatchDownloader;

/**
 * Core class for downloading a set of uris to paths
 *
 * @author LOU
 */
public class BaseDownloader {

    /** Indentation for building html */
    protected static final Object __ = TreeBuilder.__;

    private final HttpFileBatchDownloader downloader = new HttpFileBatchDownloader()
        .setMaxDownloadAttempts(3);

    /** Register a pair of uri-path to download later */
    public void add(Uri fromUri, Path toPath) {
        downloader.add(fromUri, toPath);
    }

    /** Download all what been added so far */
    public void download() {
        downloader.sortByPath();
        //TODO replace with log/reporting
        Util.println("Start download: " + downloader);
        downloader.download();
    }

}
