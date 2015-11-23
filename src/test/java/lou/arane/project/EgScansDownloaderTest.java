package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from egscans
 *
 * @author LOU
 */
public class EgScansDownloaderTest {

    @Test
    public void FengSengJi() {
        download("Feng_Shen_Ji", "Feng Shen Ji");
    }

    @Test
    public void Jin() {
        download("Jin", "Jin");
    }

    /** Download a manga from egscans to a directory */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("egscans", dirName);
        new EgScansDownloader(mangaName, baseDir).run();
    }

}
