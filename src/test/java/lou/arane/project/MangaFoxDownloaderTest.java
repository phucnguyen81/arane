package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangafox
 *
 * @author LOU
 */
public class MangaFoxDownloaderTest {

    @Test
    public void the_legendary_moonlight_sculptor() {
        download("the_legendary_moonlight_sculptor", "The Legendary Moonlight Sculptor");
    }

    public void doraemon() {
        download("doraemon", "Doraemon");
    }

    public void yu_gi_oh_duelist() {
        download("yu_gi_oh_duelist", "Yu-Gi-Oh! Duelist");
    }

    public void yu_gi_oh_millennium_world() {
        download("yu_gi_oh_millennium_world", "Yu-Gi-Oh! Millenium World");
    }

    /**
     * Download a manga series to a base directory
     *
     * @param seriesName = e.g. sun_ken_rock
     * @param dirName = e.g. Sun-ken Rock
     */
    private static void download(String seriesName, String dirName) {
        Path baseDir = Util.mangaDir("mangafox", dirName);
        new MangaFoxDownloader(seriesName, baseDir).run();
    }

}
