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
 * Common way to download manga from mangatown site.
 *
 * @author LOU
 */
public class MangaTownDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://www.mangatown.com/manga/";
    private final Uri mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;

    private final Path pageDir;

    private final Path imageDir;
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("(v\\d+[^c]*)?c\\d+");
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader to download a manga given the manga name and an
     * output directory.
     *
     * @param mangaName = Berserk
     * @param baseDir = e.g. /mangas/mangatown/Berserk
     */
    public MangaTownDownloader(String mangaName, Path baseDir) {
        Check.notNull(mangaName, "Null name");
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
        collectImagesIntoChapters();
        generateIndexFile();
    }

    /** Download the intial file that contains the chapter locations */
    private void downloadChapterList() {
        add(mangaUri, chapterList);
        Util.deleteIfExists(chapterList);
        download();
        Check.postCond(Files.exists(chapterList), "Chapter listing must be downloaded");
    }

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList);
        for (Element chapterAddr : rootFile.select("ul[class=chapter_list] a[href]")) {
            Uri chapterUri = new Uri(chapterAddr.attr("href"));
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
            Document chapter = Util.parseHtml(chapterHtml);
            collectPagesToDownload(chapter);
        }
        download();
    }

    /**
     * Given a chapter html file, the pages can be extracted from the select tag
     * such as:
     *
     * <pre>
        <div class="page_select">
            <select onchange="javascript:location.href=this.value;">
                <option selected="selected" value="http://www.mangatown.com/manga/anatolia_story/v01/c001/">01</option>
                <option value="http://www.mangatown.com/manga/anatolia_story/v01/c001/2.html">02</option>
            ...
            </select>
     * </pre>
     */
    private void collectPagesToDownload(Document chapter) {
        for (Element pageOption : chapter.select("div[class=page_select] select option[value]")) {
            Uri pageUri = new Uri(pageOption.attr("value"));
            String pageName = Util.join(pageUri.getFilePath(), "_");
            if (pageName.startsWith("manga_")) pageName = pageName.replaceFirst("manga_", "");
            if (!pageName.endsWith(".html")) pageName += ".html";
            pageName = Util.padNumericSequences(pageName, 3);
            Path pagePath = pageDir.resolve(pageName);
            add(pageUri, pagePath);
        }
    }

    /**
     * Download the actual images from the html image files such as:
     *
     * <pre>
        <img id="image"
            src="http://cdn.mangatown.com/store/manga/132/01-001.0/compressed/anatolia_story_v01_c001_p003.jpg?v=51193244323" />
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pageDir)) {
            Document page = Util.parseHtml(pageHtml);
            Element img = page.select("img[id=image][src]").first();
            Uri imageUri = new Uri(img.attr("src"));
            String pageName = pageHtml.getFileName().toString();
            pageName = Util.removeFileExtension(pageName);
            pageName = pageName + "." + imageUri.getFileExtension();
            pageName = pageName.toLowerCase();
            Path imagePath = imageDir.resolve(pageName);
            add(imageUri, imagePath);
        }
        download();
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
