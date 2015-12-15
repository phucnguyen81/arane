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
 * Download manga from mangaway site
 *
 * @author LOU
 */
public class MangaWayDownloader extends BaseDownloader {

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("c\\d+(\\.\\d+)?");

    private static final Pattern IMAGE_PATTERN = Pattern.compile("lstImages\\.push\\(\"(.+)\"\\);");

    private static final String BASE_URI = "http://mangaway.net/manga/";

    private final String mangaUri;

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path imageDir;
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader given the target directory to write downloaded
     * content to.
     *
     * @param baseDir something like "mangas/mangaway/MangaName"
     */
    public MangaWayDownloader(String mangaName, Path baseDir) {
        Check.notNull(mangaName, "Null name");
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        mangaUri = BASE_URI + mangaName + "/";
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imageDir = baseDir.resolve("images");
        imageChapterDir = baseDir.resolve("imageChapters");
        imageOutputDir = imageChapterDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChaperList();
        downloadChapters();
        downloadImages();
        collectImagesIntoChapters();
        generateIndexFile();
    }

    /** Download the initial chapter listing */
    private void downloadChaperList() {
        Util.deleteIfExists(chapterList);
        add(new Uri(mangaUri), chapterList);
        download();
        Check.postCond(Files.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     *  <div id="list" class="list">
     *      <div class="ch">
     *          <a href="http://mangaway.net/manga/Yokohama+Shopping+Log-7530/c140.1.html?id=160821">
     *  ...
     * </pre>
     */
    private void downloadChapters() {
        Document chapterDoc = Util.parseHtml(chapterList);
        for (Element chapterAddr : chapterDoc.select("div[id=list] div[class=ch] a[href]")) {
            Uri chapterUri = new Uri(chapterAddr.attr("href"));
            String chapterName = Util.join(chapterUri.getFilePath(), "_");
            if (!chapterName.endsWith(".html")) chapterName += ".html";
            chapterName = Util.padNumericSequences(chapterName, 3);
            Path chapterPath = chapterDir.resolve(chapterName);
            add(chapterUri, chapterPath);
        }
        download();
    }

    /**
     * Download the actual images from the chapter files. The image urls are
     * extracted using regex.
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            String chapterName = chapterHtml.getFileName().toString().replace(".html", "");
            Document chapter = Util.parseHtml(chapterHtml);
            for (Element script : chapter.select("script[type=text/javascript]")) {
                Matcher matcher = IMAGE_PATTERN.matcher(script.html());
                while (matcher.find()) {
                    Uri imageUri = new Uri(matcher.group(1));
                    String imageName = chapterName + "_" + imageUri.getFileName();
                    if (imageName.startsWith("manga_")) {
                        imageName = imageName.replaceFirst("manga_", "");
                    }
                    imageName = Util.padNumericSequences(imageName, 3);
                    Path imagePath = imageDir.resolve(imageName);
                    add(imageUri, imagePath);
                }
            }
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
