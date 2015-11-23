package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from mangaeden
 *
 * @author LOU
 */
public class MangaEdenDownloaderTest {

    @Test
    public void WorldTrigger() {
        download("world-trigger", "World Trigger");
    }

    /**
     * Download a particular manga given a base directory name of the project
     */
    private static void download(String mangaName, String dirName) {
        Path baseDir = Util.mangaDir("mangaeden", dirName);
        new MangaEdenDownloader(mangaName, baseDir).run();
    }

}
