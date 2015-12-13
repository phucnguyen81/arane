package lou.arane.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.StrBuilder;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.html.HtmlMatcher;
import lou.arane.util.html.HtmlPattern;
import lou.arane.util.script.CopyFiles;
import lou.arane.util.script.GenerateImageViewer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Common way to download manga from mangatako site
 *
 * @author LOU
 */
public class MangaTakoDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://mangatako.com/manga/";

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("chapter_\\d+(\\.\\d+)?");

    private final Uri mangaUri;

    private final Path baseDir;

    private final Path chapterList;
    private Predicate<String> chapterFilter = chapter -> true;
    private final Path chaptersDir;

    private final Path pagesDir;

    private final Path imagesDir;
    private final Path outputDir;
    private final Path outputImagesDir;

    /**
     * Create a downloader to download a manga series to an output directory.
     *
     * @param seriesName = e.g. TerraForMars
     * @param baseDir = e.g. mangas/mangatako/Terra ForMars
     */
    public MangaTakoDownloader(String seriesName, Path baseDir) {
        Check.notNull(seriesName, "Null name");
        this.mangaUri = new Uri(BASE_URI + "?series=" + seriesName);
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        chapterList = baseDir.resolve("chapters.html");
        chaptersDir = baseDir.resolve("chapters");
        pagesDir = baseDir.resolve("pages");
        imagesDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
        outputImagesDir = outputDir.resolve("images");
    }

    public MangaTakoDownloader includeChapters(String first, String... more) {
        Set<String> chapters = New.set(first, more);
        return setChapterFilter(chapter -> chapters.contains(chapter));
    }

    public MangaTakoDownloader setChapterFilter(Predicate<String> filter) {
        chapterFilter = Check.notNull(filter, "Null chapter filter");
        return this;
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
     * Download chapter pages by extracting their urls from the master html
     * file:
     *
     * <pre>
     * <a class='chapter_link' href='../manga/?series=TerraForMars&chapter=115&index=2&page=1'>Terra ForMars 115</a>
     * </pre>
     */
    private void downloadChapters() {
        Document chapters = parseHtml(chapterList, BASE_URI);
        for (Element chapterAddr : chapters.select("a[class=chapter_link][href]")) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String series = chapterUri.getQueryParm("series");
            String chapter = chapterUri.getQueryParm("chapter");
            if (chapterFilter.test(chapter)) {
                chapter = Util.padNumericSequences(chapter, 3);
                String chapterName = String.format("%s_chapter_%s.html", series, chapter);
                Path chapterPath = chaptersDir.resolve(chapterName);
                add(chapterUri, chapterPath);
            }
        }
        download();
    }

    /** Download pages for each chapter */
    private void downloadPages() {
        for (Path chapterHtml : findHtmlFiles(chaptersDir)) {
            Document chapter = parseHtml(chapterHtml, BASE_URI);
            addPages(chapter);
        }
        download();
    }

    /**
     * Find pages for a chapter by searching for page urls in the chapter
     * document
     */
    private void addPages(Document chapterDoc) {
        HtmlMatcher series = new HtmlMatcher();
        HtmlMatcher chapter = new HtmlMatcher();
        HtmlMatcher index = new HtmlMatcher();
        HtmlMatcher page = new HtmlMatcher();
        Runnable addPage = () -> {
            Matcher pageMatcher = NUMBER_PATTERN.matcher(page.text());
            if (pageMatcher.find()) {
                String pageVal = pageMatcher.group();
                addPage(series.val(), chapter.val(), index.val(), pageVal);
            }
        };

        HtmlPattern p = new HtmlPattern();
        p.add("form", "id=pages");
        p.add(__, "input", "name=series", series);
        p.add(__, "input", "name=chapter", chapter);
        p.add(__, "input", "name=index", index);
        p.add(__, "select", "name=page");
        p.add(__, __, "option", page, addPage);
        p.matchAll(chapterDoc);
    }

    /**
     * Add a page given sufficient parameters to determine its uri: what series,
     * what chapter, ect.
     */
    private void addPage(String series, String chapter, String index, String page) {
        Uri pageUri = new Uri(BASE_URI)
            .queryParam("series", series)
            .queryParam("chapter", chapter)
            .queryParam("index", index)
            .queryParam("page", page);
        String pageName = new StrBuilder("$series$_chapter_$chapter$_page_$page$.html")
            .attr("series", series)
            .attr("chapter", Util.padNumericSequences(chapter, 3))
            .attr("page", Util.padNumericSequences(page, 3))
            .toString();
        Path pagePath = pagesDir.resolve(pageName);
        add(pageUri, pagePath);
    }

    /**
     * Download the actual images from the html image files such as:
     *
     * <pre>
     * <img class='img-responsive' src='http://img3.mangasee.co/s/TerraForMars/Part1/0000-001.png' />
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : findHtmlFiles(pagesDir)) {
            addImageToDownload(pageHtml);
        }
        download();
    }

    private void addImageToDownload(Path pageHtml) {
        Document page = parseHtml(pageHtml);
        Element img = page.select("img[class=img-responsive][src]").first();
        Uri imageUri = new Uri(img.attr("src"));
        String imageName = pageHtml.getFileName().toString();
        imageName = Util.removeFileExtension(imageName);
        imageName = imageName + "." + imageUri.getFileExtension();
        imageName = imageName.toLowerCase();
        Path imagePath = imagesDir.resolve(imageName);
        add(imageUri, imagePath);
    }

    /** Organize the downloaded images into sub-directories.
     * A sub-directory corresponds to a chapter. */
    private void collectImagesIntoChapters() {
        Util.createDirectories(outputImagesDir);
        new CopyFiles(imagesDir, outputImagesDir).setDirPattern(CHAPTER_PATTERN).run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(outputImagesDir).setTitle(baseDir.getFileName()).run();
    }
}
