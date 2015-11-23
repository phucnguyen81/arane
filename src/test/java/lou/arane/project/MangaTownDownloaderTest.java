package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangatown
 *
 * @author LOU
 */
public class MangaTownDownloaderTest {

    @Test
    public void Berserk() {
        download("berserk", "Berserk");
    }

    public void Dungeon() {
        download("dungeon_ni_deai_o_motomeru_no_wa_machigatte_iru_darou_ka", "Dungeon");
    }

    /** Download a manga to a base directory */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("mangatown", dirName);
        new MangaTownDownloader(mangaName, baseDir).run();
    }

}
