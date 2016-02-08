package lou.arane.project.mangalife;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.script.CopyFiles;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Common way to download manga from manga.life site
 *
 * @author LOU
 */
public class MangaLifeDownloader extends BaseDownloader {

    private static final Pattern ONERROR_SRC_PATTERN = Pattern.compile("src=['\"]([^'\"]+)['\"]");

    /** base location of all mangas for this site */
    private static final String BASE_URI = "http://manga.life/";

    private final Uri mangaUri;

    private final Path chapterList;
    private final Path chaptersDir;

    private final Path pagesDir;

    private final Path imagesDir;
    private final Path outputDir;

    private final String mangaName;

    /**
     * Create a downloader to download a manga series to an output directory.
     *
     * @param mangaName = e.g. GateJietaiKareNoChiNiteKakuTatakeri
     * @param baseDir = e.g. mangas/manga-life/Gate
     */
    public MangaLifeDownloader(String mangaName, Path baseDir) {
        this.mangaName = Check.notNull(mangaName, "Null name");
        this.mangaUri = new Uri(BASE_URI + "read-online/" + mangaName);
        Check.notNull(baseDir, "Null base dir");
        chapterList = baseDir.resolve("chapters.html");
        chaptersDir = baseDir.resolve("chapters");
        pagesDir = baseDir.resolve("pages");
        imagesDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadPages();
        downloadImages();
        collectImagesIntoChapters();
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
     * Download chapter pages by extracting their urls from the master html
     * file:
     *
     * <pre>
     *   <a href="/read-online/GateJietaiKareNoChiNiteKakuTatakeri/chapter-35/index-1/page-1">Chapter 35</a>
     * </pre>
     */
    private void downloadChapters() {
        Document chapters = Util.parseHtml(chapterList, BASE_URI);
        chapters
        .select("a[href]")
        .stream()
        .map(addr -> addr.absUrl("href"))
        .filter(href -> href.contains(mangaName))
        .map(href -> new Uri(href))
        .forEach(chapterUri -> {
            String chapterPath = Util.join(chapterUri.getFilePath(), "_");
            add(chapterUri, chaptersDir.resolve(chapterPath + ".html"));
        });
        download();
    }

    /** Download pages for each chapter.
     * A page url is built from chapter-index and page-index. */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
            addPages(chapter);
            download();
        }
    }

    /**
     * Find pages for a chapter by searching for page urls in the chapter
     * document
     */
    private void addPages(Document chapterDoc) {
        String selectChapter = "select[class*=changeChapterSelect] option[value][selected]";
        for (Element chapterOpt : chapterDoc.select(selectChapter)) {
            String[] chapterSpec = chapterOpt.attr("value").split(";");
            if (chapterSpec.length == 2) {
                String chapter = "chapter-" + chapterSpec[0];
                String index = "index-" + chapterSpec[1];
                addPages(chapterDoc, chapter, index);
            }
        }
    }

    private void addPages(Document chapterDoc, String chapter, String index) {
        String selectPage = "select[class*=changePageSelect] option[value]";
        for (Element pageOption : chapterDoc.select(selectPage)) {
            String page = pageOption.attr("value");
            addPage(chapter, index, page);
        }
    }

    private void addPage(String chapter, String index, String page) {
        String base = mangaUri.toUri().toString();
        String pageUriStr = New.joiner("/", base + "/")
            .add(chapter).add(index).add(page).toString();
        Uri pageUri = new Uri(pageUriStr);
        Path pagePath = pagesDir.resolve(chapter + "_" + page + ".html");
        add(pageUri, pagePath);
    }

    /**
     * Download the actual images from the html image files such as:
     *
     * <pre>
     * <img src="http://.../0010-001.jpg" onerror="this.onerror=null;this.src='http://.../0010-001.jpg'"/>
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            addImageToDownload(page);
        }
        download();
    }

    private void addImageToDownload(Document page) {
        for (Element img : page.select("a[href] img[src]")) {
            Uri imgUri = new Uri(img.absUrl("src"));
            addOnErrorUri(imgUri, img.attr("onerror"));
            Path imgPath = imagesDir.resolve(imgUri.getFileName());
            add(imgUri, imgPath);
        }
    }

    private static void addOnErrorUri(Uri imgUri, String onerrorAttr) {
        findOnErrorSrc(onerrorAttr)
            .map(onerror -> new Uri(onerror))
            .ifPresent(onerror -> imgUri.addAlternatives(onerror));
    }

    private static Optional<String> findOnErrorSrc(String onerror) {
        if (onerror == null) {
            return Optional.empty();
        }
        Matcher srcMatcher = ONERROR_SRC_PATTERN.matcher(onerror);
        if (srcMatcher.find()) {
            return Optional.of(srcMatcher.group(1));
        }
        return Optional.empty();
    }

    /** Organize the downloaded images into sub-directories.
     * A sub-directory corresponds to a chapter. */
    private void collectImagesIntoChapters() {
        Util.createDirectories(outputDir);
        new CopyFiles(imagesDir, outputDir)
            .setDirPattern("\\d+(\\.\\d+)?")
            .run();
    }
}
