package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangaway
 *
 * @author LOU
 */
public class MangaWayDownloaderTest {

    @Test
    public void YuGiOh() {
        download("Yu-Gi-Oh-7580", "Yu-Gi Oh!");
    }

    public void ZatchBell() {
        download("Zatch+Bell-7652", "Zatch Bell");
    }

    /** Download a manga given its base directory */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("mangaway", dirName);
        new MangaWayDownloader(mangaName, baseDir).run();
    }

}
