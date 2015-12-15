package lou.arane.project;

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
 * Common way to download from egscans website
 *
 * @author LOU
 */
public class EgScansDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://read.egscans.com/";

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("chapter_\\d+[a-zA-Z]?");

    /**
     * Pattern for extracting urls from text such as:
     * src="mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg"
     */
    private static final Pattern SRC_PATTERN = Pattern.compile("src=['\"]([^'\"]+)['\"]");

    private final Uri mangaUri;
    private final String mangaName;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path imageDir;
    private final Path outputDir;
    private final Path outputImagesDir;

    /**
     * Create a downloader that downloads manga from egscans to a base directory
     *
     * @parem mangaName e.g. "Feng_Shen_Ji"
     * @param baseDir e.g. "C:/mangas/egscans/Feng Shen Ji"
     */
    public EgScansDownloader(String mangaName, Path baseDir) {
        this.mangaName = Check.notNull(mangaName, "Null name");
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        mangaUri = new Uri(BASE_URI + mangaName + "/");
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imageDir = baseDir.resolve("images");
        outputDir = baseDir.resolve(baseDir.getFileName());
        outputImagesDir = outputDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChaperList();
        downloadChapters();
        downloadImagesForAllChapters();
        organizeImagesIntoChapters();
        generateImageViewer();
    }

    /** Download the initial chapter listing */
    private void downloadChaperList() {
        Util.deleteIfExists(chapterList);
        add(mangaUri, chapterList);
        download();
        Check.postCond(Util.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages; their urls are from the master html file:
     *
     * <pre>
        <select onchange="change_chapter('Feng_Shen_Ji', this.value)" name="chapter">
            <option selected="selected" value="Chapter_001">Chapter 001</option>
            <option value="Chapter_002">Chapter 002</option>
            ...
        </select>
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList, BASE_URI);
        for (Element chapterOption : rootFile.select("select[name=chapter] option[value]")) {
            String chapterName = chapterOption.attr("value");
            Uri chapterUri = new Uri(BASE_URI + mangaName + "/" + chapterName);
            Path chapterPath = chapterDir.resolve(chapterName + ".html");
            add(chapterUri, chapterPath);
        }
        download();
    }

    private void downloadImagesForAllChapters() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            addImages(chapterHtml);
        }
        download();
    }

    /**
     * Find the images of a given chapter. The image urls are searched for in
     * javascript text such as:
     *
     * <pre>
        <script type="text/javascript">
            ...
            src="mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg"
            ...
     * </pre>
     */
    private void addImages(Path chapterHtml) {
        String chapterName = chapterHtml.getFileName().toString();
        chapterName = Util.removeFileExtension(chapterName);
        Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
        for (Element script : chapter.select("script[type=text/javascript]")) {
            addImages(script, chapterName);
        }
    }

    /** Find images from text of a script element */
    private void addImages(Element script, String chapterName) {
        Matcher matcher = SRC_PATTERN.matcher(script.html());
        while (matcher.find()) {
            Uri imageUri = new Uri(BASE_URI + matcher.group(1));
            String imageName = chapterName + "_" + imageUri.getFileName();
            imageName = Util.padNumericSequences(imageName.toLowerCase(), 3);
            Path imagePath = imageDir.resolve(imageName);
            add(imageUri, imagePath);
        }
    }

    /**
     * Images downloaded from previous step are organized into chapter
     * directories
     */
    private void organizeImagesIntoChapters() {
        new CopyFiles(imageDir, outputImagesDir).setDirPattern(CHAPTER_PATTERN).run();
    }

    /** Generate an html file to read the manga */
    private void generateImageViewer() {
        new GenerateImageViewer(outputImagesDir).setTitle(baseDir.getFileName()).run();
    }
}