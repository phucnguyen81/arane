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
 * Common way to download manga from mangareader site.
 *
 * @author LOU
 */
public class MangaReaderDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://www.mangareader.net/";

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("\\d+");

    private final Uri mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path pageDir;
    private final Path imageDir;
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader given the target directory to write downloaded
     * content to.
     *
     * @param baseDir for example "project/mangareader/MangaName"
     */
    public MangaReaderDownloader(String seriesName, Path baseDir) {
        Check.notNull(seriesName, "Null uri");
        mangaUri = new Uri(BASE_URI + seriesName);
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
        collectImagesIntoChapters();
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
     * Download html chapters by extracting their urls from the master html
     * file. The chapter urls can be found in:
     *
     * <pre>
        <div id="chapterlist">
            ...
            <a href="/jin/1">Jin 1</a>
            ...
        </div>
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = parseHtml(chapterList, BASE_URI);
        for (Element chapterAddr : rootFile.select("div[id=chapterlist] a[href]")) {
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
        for (Path chapterHtml : findHtmlFiles(chapterDir)) {
            Document chapter = parseHtml(chapterHtml, BASE_URI);
            collectPagesToDownload(chapter);
        }
        download();
    }

    /**
     * Given a chapter html file, the pages can be extracted from the select tag
     * such as:
     *
     * <pre>
        <select id="pageMenu">
            <option value="/jin/1" selected="selected">1</option>
            <option value="/jin/1/2">2</option>
            ...
        </select>
     * </pre>
     */
    private void collectPagesToDownload(Document chapter) {
        for (Element pageOption : chapter.select("select[id=pageMenu] option[value]")) {
            Uri pageUri = new Uri(pageOption.absUrl("value"));
            String pageName = Util.join(pageUri.getFilePath(), "_");
            if (!pageName.endsWith(".html")) pageName += ".html";
            Path pagePath = pageDir.resolve(pageName);
            add(pageUri, pagePath);
        }
    }

    /**
     * Download the actual images found in the html image files. The image urls
     * are found in the img elements such as:
     *
     * <pre>
     * <img id="img" src="http://i20.mangareader.net/jin/1/jin-3422517.jpg" />
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : findHtmlFiles(pageDir)) {
            Document page = parseHtml(pageHtml);
            Element img = page.select("img[id=img][src]").first();
            Uri imageUri = new Uri(img.attr("src"));
            String pageName = pageHtml.getFileName().toString().replace(".html", "");
            Path imagePath = imageDir.resolve(pageName + "." + imageUri.getFileExtension());
            add(imageUri, imagePath);
        }
        download();
    }

    /** Copy the downloaded images to chapter directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(imageDir, imageOutputDir)
            .setDirPattern(CHAPTER_PATTERN)
            .padNumericSequences(3)
            .run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imageOutputDir)
            .setTitle(baseDir.getFileName())
            .run();
    }

}
