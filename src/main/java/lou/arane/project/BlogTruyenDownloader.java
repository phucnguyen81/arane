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
import org.jsoup.select.Elements;

/**
 * Download comics from blogtruyen site
 *
 * @author LOU
 */
public class BlogTruyenDownloader extends BaseDownloader {

    /** Scheme of this site */
    private static final String BASE_URI = "http://blogtruyen.com/";

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

    private static final Pattern CHAPTER_PATTERN = NUMBER_PATTERN;

    private final Uri mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path imageDir;
    private final Path outputDir;
    private final Path outputImagesDir;

    /**
     * Create a downloader to download a story to a directory
     *
     * @param story = name of the story, e.g. "vo-than"
     * @param baseDir = dir to download to, e.g. "/mangas/Vo Than"
     */
    public BlogTruyenDownloader(String story, Path baseDir) {
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        this.mangaUri = new Uri(BASE_URI + "/truyen/" + story);
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imageDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
        outputImagesDir = outputDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadImages();
        collectImagesIntoChapters();
        generateIndexFile();
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
        Document rootFile = parseHtml(chapterList, BASE_URI);
        Elements chapterAddresses = rootFile.select("div[id=list-chapters] a[href]:not([rel]), a[href][rel=noreferrer]");
        for (Element chapterAddr : chapterAddresses) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String chapterName = chapterUri.getFileName().toString();
            chapterName = cleanChapterName(chapterName);
            if (!chapterName.endsWith(".html")) {
                chapterName += ".html";
            }
            Path chapterPath = chapterDir.resolve(chapterName);
            add(chapterUri, chapterPath);
        }
        download();
    }

    /**
     * Clean chapter names so that chapter order matches lexicographical order.
     * For example, chapter "chap-1" is converted to "001".
     */
    private static String cleanChapterName(String chapterName) {
        Matcher matcher = NUMBER_PATTERN.matcher(chapterName);
        if (matcher.find()) {
            chapterName = matcher.group();
        }
        chapterName = Util.padNumericSequences(chapterName, 3);
        return chapterName;
    }

    /**
     * Download the actual images for each chapter. The images can be found in:
     *
     * <pre>
     *   <article id="content">
     *       <img src="http://4.bp.blogspot.com/.../Vol8-Chap48-P00.jpg?imgmax=3000" />
     * </pre>
     */
    private void downloadImages() {
        for (Path chapterHtml : findHtmlFiles(chapterDir)) {
            Document page = parseHtml(chapterHtml);
            Elements images = page.select("article[id=content] img[src]");
            for (int imageIdx = 0; imageIdx < images.size(); imageIdx++) {
                Element image = images.get(imageIdx);
                Uri imageUri = new Uri(image.absUrl("src"));
                String imageName = chapterHtml.getFileName().toString();
                imageName = Util.removeFileExtension(imageName);
                imageName += "_" + imageIdx + "." + imageUri.getFileExtension();
                imageName = Util.padNumericSequences(imageName, 3);
                Path imagePath = imageDir.resolve(imageName);
                add(imageUri, imagePath);
            }
            download();
        }
    }

    /** Organize the downloaded images into chapter sub-directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(imageDir, outputImagesDir).setDirPattern(CHAPTER_PATTERN).run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(outputImagesDir).setTitle(baseDir.getFileName()).run();
    }

}
