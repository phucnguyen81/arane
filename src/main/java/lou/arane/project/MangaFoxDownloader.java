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
import org.jsoup.select.Elements;

/**
 * Common way to download manga from mangafox site.
 *
 * @author LOU
 */
public class MangaFoxDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://mangafox.me/";

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("c\\d+");

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
     * @param mangaName = e.g. sun_ken_rock
     * @param baseDir = e.g. /mangas/mangafox/Sun-ken Rock
     */
    public MangaFoxDownloader(String mangaName, Path baseDir) {
        Check.notNull(mangaName, "Null name");
        mangaUri = new Uri(BASE_URI + "manga/" + mangaName + "/");
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
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList);
        Elements chapterAddresses = rootFile.select("a[href][class=tips]");
        for (Element chapterAddr : chapterAddresses) {
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
            Uri chapterUri = findChapterUri(chapter);
            collectPagesToDownload(chapter, chapterUri);
        }
        download();
    }

    /**
     * Given a chapter html file, the chapter uri can be extracted from the link
     * element such as:
     *
     * <pre>
	 * <link rel="canonical" href="http://mangafox.me/manga/hi_no_tori/v01/c001/1.html"/>
	 * </pre>
     */
    private static Uri findChapterUri(Document chapter) {
        Element chapterLocation = chapter.select("link[rel=canonical][href]").first();
        return new Uri(chapterLocation.attr("href"));
    }

    /**
     * Given a chapter html file, the pages can be extracted from the select tag
     * such as:
     *
     * <pre>
	 * <select class="m">
	 *      <option value="1">1</option>
	 * </select>
	 * </pre>
     */
    private void collectPagesToDownload(Document chapter, Uri chapterUri) {
        for (Element pageOption : chapter.select("select[class=m] option[value]")) {
            String pageIdx = pageOption.attr("value");
            if (!pageIdx.equals("0")) {
                Uri pageUri = chapterUri.resolve(pageIdx + ".html");
                String pageName = Util.join(pageUri.getFilePath(), "_");
                pageName = cleanPageName(pageName);
                Path pagePath = pageDir.resolve(pageName);
                add(pageUri, pagePath);
            }
        }
    }

    /**
     * For example, if a name is "manga_tiger_books_v02_c001_43", we want to
     * convert it to "tiger_books_v002_c001_043"
     */
    private String cleanPageName(String pageName) {
        if (pageName.startsWith("manga_")) {
            pageName = pageName.replaceFirst("manga_", "");
        }
        pageName = Util.padNumericSequences(pageName, 3);
        return pageName;
    }

    /** Download the actual images from the html image files. */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pageDir)) {
            Document page = Util.parseHtml(pageHtml);
            Element img = page.select("img[src][id=image]").first();
            Uri imageUri = new Uri(img.attr("src"));
            String imageName = pageHtml.getFileName().toString();
            imageName = Util.removeFileExtension(imageName);
            imageName += "." + imageUri.getFileExtension();
            imageName = imageName.toLowerCase();
            Path imagePath = imageDir.resolve(imageName);
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
