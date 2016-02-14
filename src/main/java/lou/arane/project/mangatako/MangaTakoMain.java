package lou.arane.project.mangatako;

import java.nio.file.Path;

import lou.arane.util.Util;

public class MangaTakoMain {

    public static void main(String[] args) {
        download("TheBreakerNewWaves", "The Breaker New Waves");
    }

    static void download(String seriesName, String dirName) {
        Path baseDir = Util.mangaDir("mangatako", dirName);
        new MangaTakoDownloader(seriesName, baseDir).run();
    }
}
