package lou.arane.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.html.HtmlMatcher;
import lou.arane.util.html.HtmlPattern;
import lou.arane.util.script.GenerateImageViewer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Common way to download manga from mangacow site
 *
 * @author LOU
 */
public class MangaCowDownloader extends BaseDownloader {

    private static final String BASE_URI = "http://mangacow.co/";

    private final Uri mangaUri;

    private final Path baseDir;

    private final Path chapterList;
    private Predicate<String> chapterFilter = chapter -> true;
    private final Path chaptersDir;

    private final Path pagesDir;

    private final Path imagesDir;

    /**
     * Create a downloader to download a manga series to an output directory.
     *
     * @param seriesName = e.g. terra_formars
     * @param baseDir = e.g. mangas/mangacow/Terra ForMars
     */
    public MangaCowDownloader(String seriesName, Path baseDir) {
        Check.notNull(seriesName, "Null uri");
        this.mangaUri = new Uri(BASE_URI + seriesName + "/");
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        chapterList = baseDir.resolve("chapters.html");
        chaptersDir = baseDir.resolve("chapters");
        pagesDir = baseDir.resolve("pages");
        imagesDir = baseDir.resolve("output/images");
    }

    public MangaCowDownloader setChapters(String first, String... more) {
        Set<String> chapters = New.set(first, more);
        return setChapterFilter(chapter -> chapters.contains(chapter));
    }

    public MangaCowDownloader setChapterFilter(Predicate<String> filter) {
        chapterFilter = Check.notNull(filter, "Null chapter filter");
        return this;
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadPages();
        downloadImages();
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
        <ul class="lst mng_chp">
            <a href="http://mangacow.co/the_legendary_moonlight_sculptor/14/" class="lst"></a>
            ...
        </ul>
     * </pre>
     */
    private void downloadChapters() {
        Document chapters = Util.parseHtml(chapterList, BASE_URI);
        for (Element chapterAddr : chapters.select("ul[class=lst mng_chp] a[href][class=lst]")) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String chapter = chapterUri.getFileName().toString();
            if (chapterFilter.test(chapter)) {
                Path chapterPath = chaptersDir.resolve(chapter + ".html");
                add(chapterUri, chapterPath);
            }
        }
        download();
    }

    /** Download pages for each chapter */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
            String chapterIdx = chapterHtml.getFileName().toString();
            chapterIdx = Util.removeFileExtension(chapterIdx);
            addPages(chapter, chapterIdx);
        }
        download();
    }

    /**
     * Find pages for a chapter by searching for page urls in the chapter
     * document
     *
     * <pre>
        <select onchange="location.href='http://mangacow.co/the_legendary_moonlight_sculptor/01/' + this.value + '/'" class="cbo_wpm_pag">
            <option selected="selected" value="1">1</option>
            <option value="2">2</option>
            ...
        </select>
     * </pre>
     */
    private void addPages(Document chapterDoc, String chapter) {
        HtmlMatcher select = new HtmlMatcher();
        HtmlMatcher option = new HtmlMatcher();
        Runnable addPage = () -> {
            String pageName = chapter + "/" + option.text();
            Uri pageUri = mangaUri.resolve(pageName);
            Path pagePath = pagesDir.resolve(pageName + ".html");
            add(pageUri, pagePath);
        };

        HtmlPattern p = new HtmlPattern();
        p.add("select", "class=cbo_wpm_pag", select);
        p.add(__, "option", option, addPage);
        p.matchAll(chapterDoc);
    }

    /**
     * Download the actual images from the html image files such as:
     *
     * <pre>
        <div class="prw" style="display: block;">
            <a href="http://mangacow.co/the_legendary_moonlight_sculptor/01/2/">
                <img src="http://mangacow.co/wp-content/manga/8/2/0000.jpg">
            </a>
        </div>
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findAllHtmlFiles(pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            Element img = page.select("div[class=prw] img[src]").first();
            Uri imageUri = new Uri(img.attr("src"));
            String imageName =  pagesDir.relativize(pageHtml).toString();
            imageName = Util.removeFileExtension(imageName);
            imageName = Util.padNumericSequences(imageName, 3);
            imageName += "." + imageUri.getFileExtension();
            Path imagePath = imagesDir.resolve(imageName);
            add(imageUri, imagePath);
        }
        download();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imagesDir).setTitle(baseDir.getFileName()).run();
    }
}