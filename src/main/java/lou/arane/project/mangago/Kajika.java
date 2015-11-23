package lou.arane.project.mangago;

import java.nio.file.Path;

import lou.arane.project.MangaGoDownloader;
import lou.arane.util.Util;

public class Kajika {

    public static void main(String[] args) {
        String mangaName = "Kajika";
        Path mangaDir = Util.mangaDir("mangago", "Kajika");
        new MangaGoDownloader(mangaName, mangaDir).run();
    }

}
