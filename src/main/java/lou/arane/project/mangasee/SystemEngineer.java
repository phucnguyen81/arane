package lou.arane.project.mangasee;

import java.nio.file.Path;

import lou.arane.project.MangaSeeDownloader;
import lou.arane.util.Util;

/**
 * @author LOU
 */
public class SystemEngineer {

    public static void main(String[] args) {
        String mangaName = "SESystemEngineer";
        Path mangaDir = Util.mangaDir("mangasee", "System Engineer");
        new MangaSeeDownloader(mangaName, mangaDir).run();
    }

}
