package lou.arane.project.mangalife;

import java.nio.file.Path;

import lou.arane.util.Util;

/**
 * Download mangas from manga-life site
 *
 * @author LOU
 */
public class MangaLifeMain {

    public static void main(String[] args) {
        String mangaName = "GateJietaiKareNoChiNiteKakuTatakeri";
        Path mangaDir = Util.mangaDir("mangalife", "Gate");
        new MangaLifeDownloader(mangaName, mangaDir).run();
    }

}
