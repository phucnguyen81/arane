package lou.arane.project.blogtruyen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Download comics from blogtruyen site
 *
 * @author LOU
 */
public class BlogTruyenDownloader extends BaseDownloader {

    /** Scheme of this site */
    private static final String BASE_URI = "http://blogtruyen.com/";

    /** Name of the manga used in its uri */
    private final String story;

    private final Uri mangaUri;

    private final Path chapterList;

    private final Path chapterDir;
    private final Path imagesDir;

    private final Path outputDir;

    /**
     * Create a downloader to download a story to a directory
     *
     * @param story = name of the story, e.g. "vo-than"
     * @param baseDir = dir to download to, e.g. "/mangas/Vo Than"
     */
    public BlogTruyenDownloader(String story, Path baseDir) {
        this.story = Check.notNull(story, "Null story");
        Check.notNull(baseDir, "Null base dir");
        this.mangaUri = new Uri(BASE_URI + "/truyen/" + story);
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imagesDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadImages();
        copyImagesToOutputDir();
    }

    /** Download the newest page that lists all chapters */
    private void downloadChapterList() {
        Util.deleteIfExists(chapterList);
        add(mangaUri, chapterList);
        download();
        Check.postCond(Files.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     *  <div class="list-wrap" id="list-chapters">
     *      <a href="/truyen/hiep-khach-giang-ho/chap-473" >
     *      <a href="https://www.mediafire.com/?i4fd23z6a8r25i8" rel="nofollow">
     * ...
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList, BASE_URI);
        Elements chapterAddresses = rootFile.select("a[href]");
        for (Element chapterAddr : chapterAddresses) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            chapterUri.getPath().filter(p -> p.contains(story)).ifPresent(p -> {
                String chapterName = chapterUri.getFileName().toString();
                if (!chapterName.endsWith(".html")) {
                    chapterName += ".html";
                }
                Path chapterPath = chapterDir.resolve(chapterName);
                add(chapterUri, chapterPath);
            });
        }
        download();
    }

    /**
     * Download the actual images for each chapter. The images can be found in:
     *
     * <pre>
     *   <article id="content">
     *       <img src="http://4.bp.blogspot.com/.../Vol8-Chap48-P00.jpg?imgmax=3000" />
     *       ...
     *   </article>
     * </pre>
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            String imageDir = chapterHtml.getFileName().toString();
            imageDir = Util.removeFileExtension(imageDir);
            Document page = Util.parseHtml(chapterHtml);
            Elements images = page.select("article[id=content] img[src]");
            for (Element image : images) {
                Uri imageUri = new Uri(image.absUrl("src"));
                String imageName = imageUri.getFileName().toString();
                Path imagePath = Paths.get(imageDir, imageName);
                imagePath = imagesDir.resolve(imagePath);
                add(imageUri, imagePath);
            }
            download();
        }
    }

    /** Make output chapters from downloaded images */
    private void copyImagesToOutputDir() {
        Util
        .list(imagesDir)
        .filter(Util::isNotEmpty)
        .forEach(src -> {
            Path rel = imagesDir.relativize(src);
            Path dst = outputDir.resolve(rel);
            Util.copy(src, dst);
        });
    }

}
