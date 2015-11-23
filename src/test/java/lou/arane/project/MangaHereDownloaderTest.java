package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangahere.
 *
 * @author LOU
 */
public class MangaHereDownloaderTest {

    @Test
    public void OreToKawazuSanNoIsekaiHourouki() {
        download("ore_to_kawazu_san_no_isekai_hourouki", "Ore to Kawazu-san no Isekai Hourouki");
    }

    /** Base directory for downloading a manga on mangahere */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("mangahere", dirName);
        new MangaHereDownloader(mangaName, baseDir).run();
    }

}
