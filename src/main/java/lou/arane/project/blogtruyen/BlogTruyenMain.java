package lou.arane.project.blogtruyen;

import java.nio.file.Path;

import lou.arane.util.Util;

public class BlogTruyenMain {

    public static void main(String[] args) {
        download("dragon-ball-original", "7 Vien Ngoc Rong");
    }

    static void download(String title, String name) {
        Path baseDir = Util.mangaDir("blogtruyen", name);
        new BlogTruyenDownloader(title, baseDir).run();
    }

}
