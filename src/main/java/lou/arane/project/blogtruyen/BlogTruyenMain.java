package lou.arane.project.blogtruyen;

import java.nio.file.Path;

import lou.arane.util.Util;

public class BlogTruyenMain {

    public static void main(String[] args) {
        download("the-breaker-new-waves", "The Breaker New Waves");
    }

    static void download(String title, String name) {
        Path baseDir = Util.mangaDir("blogtruyen", name);
        new BlogTruyenDownloader(title, baseDir).run();
    }

}
