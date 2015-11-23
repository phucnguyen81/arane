package lou.arane.project.mangago;

import java.nio.file.Path;

import lou.arane.project.MangaGoDownloader;
import lou.arane.util.Util;

/**
 * Long ago, in Jushin, special secret agents named "Angyo Onshi" wandered the
 * country and restored the order. But now that Jushin is destroyed, only one is
 * still wandering...
 *
 * @author LOU
 */
public class ShinAngyoOnshi {

    public static void main(String[] args) {
        String mangaName = "ShinAngyoOnshi";
        Path mangaDir = Util.mangaDir("mangago", "Shin Angyo Onshi");
        new MangaGoDownloader(mangaName, mangaDir).run();
    }

}
