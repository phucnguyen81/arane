package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

public class MangaReaderDownloaderTest {

    @Test
    public void nausicaa_of_the_valley_of_the_wind() {
        download("nausicaa-of-the-valley-of-the-wind", "Nausicaa of the Valley of the Wind");
    }

    /**
     * Download a manga series to a base directory
     *
     * @param seriesName = e.g. nausicaa-of-the-valley-of-the-wind
     * @param dirName = e.g. Nausicaa of the Valley of the Wind
     */
    private static void download(String seriesName, String dirName) {
        Path baseDir = Util.mangaDir("mangareader", dirName);
        new MangaReaderDownloader(seriesName, baseDir).run();
    }

}
