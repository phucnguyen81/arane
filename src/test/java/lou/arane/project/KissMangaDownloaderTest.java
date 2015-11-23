package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from kissmanga
 *
 * @author LOU
 */
public class KissMangaDownloaderTest {

    @Test
    public void PrisonSchool() {
        download("Prison-School", "Prison School");
    }

    /**
     * Download a particular manga given a base directory name of the project
     */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("kissmanga", dirName);
        new KissMangaDownloader(mangaName, baseDir).run();
    }

}
