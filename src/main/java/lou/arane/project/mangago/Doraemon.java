package lou.arane.project.mangago;

import java.nio.file.Path;

import lou.arane.project.MangaGoDownloader;
import lou.arane.util.Util;

public class Doraemon {

    public static void main(String[] args) {
        String mangaName = "doraemon";
        Path mangaDir = Util.mangaDir("mangago", "Doraemon");
        new MangaGoDownloader(mangaName, mangaDir).run();
    }

}
