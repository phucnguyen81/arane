package lou.arane.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
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
 * Download manga from mangaeden site
 * <p>
 * TODO normalize chapter names before downloading their pages
 *
 * @author LOU
 */
public class MangaEdenDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://www.mangaeden.com/";

    /**
     * Pattern for image url such as
     * "src='//static.mangaeden.com/mangasimg//arale123.jpg'"
     */
    private static final Pattern SRC_PATTERN = Pattern.compile("src='(.+)'");

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

    private final String mangaName;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path pageDir;

    private final Path imageDir;

    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader given the base directory to write downloaded content
     * to.
     *
     * @param baseDir = e.g "project/mangaeden/Claymore"
     */
    public MangaEdenDownloader(String mangaName, Path baseDir) {
        this.mangaName = Check.notNull(mangaName, "Null name");
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
        downloadChaperList();
        downloadChapters();
        downloadPages();
        downloadImages();
        collectImagesIntoChapters();
        generateIndexFile();
    }

    /** Download the initial chapter listing */
    private void downloadChaperList() {
        Util.deleteIfExists(chapterList);
        add(new Uri(BASE_URI + "en-manga/" + mangaName + "/"), chapterList);
        download();
        Check.postCond(Files.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     * <a href="/en-manga/soul-eater/110/1/" class="chapterLink">
     * </pre>
     */
    private void downloadChapters() {
        Document baseFile = Util.parseHtml(chapterList, BASE_URI);
        for (Element chapterAddr : baseFile.select("a[href][class=chapterLink]")) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String chapterName = Util.join(chapterUri.getFilePath(), "_");
            if (!chapterName.endsWith(".html")) chapterName += ".html";
            Path chapterPath = chapterDir.resolve(chapterName);
            add(chapterUri, chapterPath);
        }
        download();
    }

    /** Download pages for each chapter */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
            collectPagesToDownload(chapter);
        }
        download();
    }

    /**
     * Given a chapter html file, the page locations can be extracted from the
     * page:
     *
     * <pre>
     * <div class="pagination ">
     * ...
     * <a class="ui-state-default" href="/en-manga/soul-eater/0/13/">
     * ..
     * </div>
     * </pre>
     *
     * Note that the first page is from:
     *
     * <pre>
     * <meta property="og:url" content="http://www.mangaeden.com/en-manga/soul-eater/0/1/" />
     * </pre>
     */
    private void collectPagesToDownload(Document chapter) {
        String firstPageQuery = "meta[property=og:url][content]";
        Element firstPage = chapter.select(firstPageQuery).first();
        collectPageToDownload(new Uri(firstPage.absUrl("content")));

        String pageQuery = "div[class*=pagination] a[class=ui-state-default][href]";
        for (Element page : chapter.select(pageQuery)) {
            collectPageToDownload(new Uri(page.absUrl("href")));
        }
    }

    private void collectPageToDownload(Uri pageUri) {
        String pageName = Util.join(pageUri.getFilePath(), "_");
        pageName = normalizePageName(pageName) + ".html";
        Path pagePath = pageDir.resolve(pageName);
        add(pageUri, pagePath);
    }

    /**
     * @param pageName = for example, en-manga_soul-eater_0_133
     * @return for example, soul-eater_000_133
     */
    private String normalizePageName(String pageName) {
        if (pageName.startsWith("en-manga_")) {
            pageName = pageName.replaceFirst("en-manga_", "");
        }
        pageName = Util.padNumericSequences(pageName, 3);
        return pageName;
    }

    /**
     * Download the actual images from the html image files
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pageDir)) {
            Document page = Util.parseHtml(pageHtml, BASE_URI);
            Uri imageUri = extractImageUri(page);
            String imageName = pageHtml.getFileName().toString().replace(".html", "");
            Path imagePath = imageDir.resolve(imageName + "." + imageUri.getFileExtension());
            add(imageUri, imagePath);
        }
        download();
    }

    /**
     * Get image url from a page, the img element looks like:
     *
     * <pre>
     *  <img id="mainImg" src="//cdn.mangaeden.com/mangasimg/be/be14ef8372e8d8e907d372e4c48b76d3956861fde497f2b8131f74be.jpg"
     *      onerror="this.src='//static.mangaeden.com/mangasimg/be/be14ef8372e8d8e907d372e4c48b76d3956861fde497f2b8131f74be.jpg';"/>
     * </pre>
     */
    private Uri extractImageUri(Document page) {
        Element img = page.select("img[id=mainImg][src]").first();
        Uri imageUri = Uri.http(img.attr("src"));
        if (img.hasAttr("onerror")) {
            String onerror = img.attr("onerror");
            Matcher onerrorMatcher = SRC_PATTERN.matcher(onerror);
            if (onerrorMatcher.find()) {
                imageUri.addAlternatives(Uri.http(onerrorMatcher.group(1)));
            }
        }
        return imageUri;
    }

    /** Organize the downloaded images into chapter sub-directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(imageDir, imageOutputDir).setDirPattern(CHAPTER_PATTERN).run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imageOutputDir).setTitle(baseDir.getFileName()).run();
    }

}
