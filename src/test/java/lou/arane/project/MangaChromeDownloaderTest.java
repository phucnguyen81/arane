package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangachrome
 *
 * @author LOU
 */
public class MangaChromeDownloaderTest {

    @Test
    public void Ability() {
        new MangaChromeDownloader(baseDir("Ability")).run();
    }

    /** Get a base directory for downloading a manga on mangachrome */
    private static Path baseDir(String dirName) {
        return Util.mangaDir("mangachrome", dirName);
    }

}
