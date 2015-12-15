package lou.arane.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.script.CopyFiles;
import lou.arane.util.script.GenerateImageViewer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Download manga from mangahere
 *
 * @author LOU
 */
public class MangaHereDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://www.mangahere.co/manga/";

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("c\\d+(\\.\\d+)?");

    private final Uri mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path pageDir;
    private final Path imageDir;
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader to download into a base directory. Initial resource
     * is an html file in base directory named "chapters.html" that contains the
     * chapter urls.
     *
     * @param baseDir = for example "project/mangahere/DrSlump"
     */
    public MangaHereDownloader(String mangaName, Path baseDir) {
        Check.notNull(mangaName, "Null uri");
        this.mangaUri = new Uri(BASE_URI + mangaName + "/");
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        pageDir = baseDir.resolve("pages");
        imageDir = baseDir.resolve("images");
        imageChapterDir = baseDir.resolve("imageChapters");
        imageOutputDir = imageChapterDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadPages();
        downloadImages();
        organizeImagesIntoChapters();
        generateIndexFile();
    }

    /** Download the intial file that contains the chapter locations */
    private void downloadChapterList() {
        Util.deleteIfExists(chapterList);
        add(mangaUri, chapterList);
        download();
        Check.postCond(Files.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList);
        rootFile.select("div[class=detail_list] a[class=color_0077]")
            .stream()
            .map(chapterAddr -> new Uri(chapterAddr.attr("href")))
            .sorted()
            .forEach(chapterUri -> {
                String chapterName = Util.join(chapterUri.getFilePath(), "_");
                Path chapterPath = chapterDir.resolve(chapterName + ".html");
                add(chapterUri, chapterPath);
            });
        download();
    }

    /**
     * Download pages for each chapter. Given a chapter html file, the pages can
     * be extracted from the select element such as:
     *
     * <pre>
    * <select class="wid60">
    *      <option value="http://www.mangahere.co/manga/dr_slump/v01/c001/2.html">2</option>
    * </select>
    * </pre>
     */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            Document chapter = Util.parseHtml(chapterHtml);
            for (Element opt : chapter.select("select[class=wid60] option[value]")) {
                Uri pageUri = new Uri(opt.attr("value"));
                String pageName = Util.join(pageUri.getFilePath(), "_");
                if (!pageName.endsWith(".html")) pageName += ".html";
                Path pagePath = pageDir.resolve(pageName);
                add(pageUri, pagePath);
            }
        }
        download();
    }

    /**
     * Download the actual images from the html pages by looking for the img
     * elements such as:
     *
     * <pre>
     *   <img id="image" src="http://z.mhcdn.net/store/manga/213/01-001.0/compressed/dr_slump_v01_c01.dr.slump_vol01_ch01_pg004.jpg?v=11211294742">
     * </pre>
     * */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pageDir)) {
            Document page = Util.parseHtml(pageHtml);
            Element img = page.select("img[id=image][src]").first();
            Uri imageUri = new Uri(img.attr("src"));
            String pageName = imageName(pageHtml.getFileName().toString().replace(".html", ""));
            Path imagePath = imageDir.resolve(pageName + "." + imageUri.getFileExtension());
            add(imageUri, imagePath);
        }
        download();
    }

    /**
     * Get image name from page name. For example, if a page name is
     * "manga_tiger_books_v02_c001_43", image name can be
     * "tiger_books_v002_c001_043"
     */
    private String imageName(String pageName) {
        if (pageName.startsWith("manga_")) {
            pageName = pageName.replaceFirst("manga_", "");
        }
        pageName = Util.padNumericSequences(pageName, 3);
        return pageName;
    }

    /** Copy the downloaded images to chapter directories */
    private void organizeImagesIntoChapters() {
        new CopyFiles(imageDir, imageOutputDir).setDirPattern(CHAPTER_PATTERN).run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imageOutputDir).setTitle(baseDir.getFileName()).run();
    }

}
