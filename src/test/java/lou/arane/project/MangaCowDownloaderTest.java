package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangacow
 *
 * @author LOU
 */
public class MangaCowDownloaderTest {

    @Test
    public void BlindFaithDescent() {
        download("Blind-Faith-Descent", "Blind Faith Descent");
    }

    /**
     * Download a manga series to a base directory
     *
     * @param seriesName = e.g. Blind-Faith-Descent
     * @param dirName = e.g. Blind Faith Descent
     */
    private static void download(String seriesName, String dirName) {
        downloader(seriesName, dirName).run();
    }

    /**
     * Create a downloader to download a manga series to a base directory
     *
     * @param seriesName = e.g. Blind-Faith-Descent
     * @param dirName = e.g. Blind Faith Descent
     */
    private static MangaCowDownloader downloader(String seriesName, String dirName) {
        Path baseDir = Util.mangaDir("mangacow", dirName);
        return new MangaCowDownloader(seriesName, baseDir);
    }
}
