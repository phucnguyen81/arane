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
 * Common way to download manga from kissmanga site.
 *
 * @author LOU
 */
public class KissMangaDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://kissmanga.com/";

    private static final Pattern IMAGE_PATTERN = Pattern.compile("lstImages\\.push\\(\"(.+)\"\\);");

    private static final Pattern CHAPTER_PATTERN = Pattern
        .compile("(Vol-\\d+-)?(Ch-)?(\\d+(\\.\\d+)?)");

    private final Uri mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;

    private final Path imageDir;
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader to download a manga series to an output directory.
     *
     * @param seriesName = e.g. Prison-School
     * @param baseDir = e.g. C:/mangas/kissmanga/Prison School
     */
    public KissMangaDownloader(String seriesName, Path baseDir) {
        Check.notNull(seriesName, "Null name");
        this.mangaUri = new Uri(BASE_URI + "Manga/" + seriesName);
        this.baseDir = Check.notNull(baseDir, "Null dir");
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imageDir = baseDir.resolve("images");
        imageChapterDir = baseDir.resolve("imageChapters");
        imageOutputDir = imageChapterDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
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
        Document chapters = parseHtml(chapterList, BASE_URI);
        for (Element chapterAddr : chapters.select("table[class=listing] a[href]")) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String chapterName = Util.join(chapterUri.getFilePath(), "_");
            if (!chapterName.endsWith(".html")) chapterName += ".html";
            Path chapterPath = chapterDir.resolve(chapterName);
            add(chapterUri, chapterPath);
        }
        download();
    }

    /**
     * Download images for each chapter. The image urls are extracted from
     * javascript's content using regex.
     */
    private void downloadImages() {
        for (Path chapterHtml : findHtmlFiles(chapterDir)) {
            String chapterName = chapterHtml.getFileName().toString();
            chapterName = Util.removeFileExtension(chapterName);
            Document chapter = parseHtml(chapterHtml);
            for (Element script : chapter.select("script[type=text/javascript]")) {
                Matcher matcher = IMAGE_PATTERN.matcher(script.html());
                while (matcher.find()) {
                    Uri imageUri = new Uri(matcher.group(1));
                    String imageName = chapterName + "_" + imageUri.getFileName();
                    imageName = Util.padNumericSequences(imageName, 3);
                    Path imagePath = imageDir.resolve(imageName);
                    add(imageUri, imagePath);
                }
            }
        }
        download();
    }

    /** Organize the downloaded images into chapter sub-directories */
    private void organizeImagesIntoChapters() {
        new CopyFiles(imageDir, imageOutputDir)
            .setDirResolver(this::getDirName)
            .run();
    }

    /** Detect directory name from file name */
    private String getDirName(String fileName) {
        String dirName = null;
        Matcher chapterMatcher = CHAPTER_PATTERN.matcher(fileName);
        if (chapterMatcher.find()) {
            dirName = chapterMatcher.group(3);
        }
        return dirName;
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imageOutputDir).setTitle(baseDir.getFileName()).run();
    }
}
