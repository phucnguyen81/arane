package lou.arane.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.http.HttpFileBatchDownloader;
import lou.arane.util.script.CopyFiles;
import lou.arane.util.script.GenerateImageViewer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Common way to download from mangachrome website
 *
 * @author LOU
 */
public class MangaChromeDownloader extends BaseDownloader {

    private final Pattern chapterPattern = Pattern.compile("\\d+");

    private final HttpFileBatchDownloader downloader = new HttpFileBatchDownloader()
        .setMaxDownloadAttempts(3);

    private final Path baseDir;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path pageDir;
    private final Path imageDir;
    private final Path imageChapterDir;
    private final Path imageOutputDir;

    /**
     * Create a downloader that downloads manga from mangafox. Resources are
     * downloaded into a base directory. Initial resource is an html file in
     * base directory named "chapters.html" that contains the chapter urls.
     *
     * @param baseDir for example "project/mangafox/DGrayMan"
     */
    public MangaChromeDownloader(Path baseDir) {
        this.baseDir = Check.notNull(baseDir, "Null base dir");
        chapterList = baseDir.resolve("chapters.html");
        Check.require(Files.exists(chapterList), "Chapter listing not found at " + chapterList);
        chapterDir = baseDir.resolve("chapters");
        pageDir = baseDir.resolve("pages");
        imageDir = baseDir.resolve("images");
        imageChapterDir = baseDir.resolve("imageChapters");
        imageOutputDir = imageChapterDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapters();
        downloadPages();
        downloadImages();
        collectImagesIntoChapters();
        generateIndexFile();
    }

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList);
        for (Element chapterHref : rootFile.select("ul[class=chp_lst] li a[href]")) {
            Uri chapterUri = new Uri(chapterHref.attr("href"));
            String chapterName = Util.join(chapterUri.getFilePath(), "_");
            Path chapterPath = chapterDir.resolve(chapterName + ".html");
            Util.println(chapterUri + " -> " + chapterPath);
            downloader.add(chapterUri, chapterPath);
        }
        downloader.download();
    }

    /** Download pages for each chapter */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            Document chapter = Util.parseHtml(chapterHtml);
            collectPagesToDownload(chapter);
        }
        downloader.download();
    }

    /**
     * Given a chapter html file, the page urls can be extracted from the select
     * tag such as:
     *
     * <pre>
     * <select class="cbo_wpm_pag" onchange="location.href='http://mangachrome.co/chibisan-date/1/' + this.value + '/'">
     *      <option value="1">1</option>
     * </select>
     * </pre>
     */
    private void collectPagesToDownload(Document chapter) {
        Pattern hrefPattern = Pattern.compile("href='([^']+)'");
        for (Element select : chapter.select("select[class=cbo_wpm_pag][onchange]")) {
            Matcher chapterMatcher = hrefPattern.matcher(select.attr("onchange"));
            if (chapterMatcher.find()) for (Element option : select.select("option[value]")) {
                Uri pageUri = new Uri(chapterMatcher.group(1) + option.attr("value"));
                String pageName = Util.join(pageUri.getFilePath(), "_");
                if (!pageName.endsWith(".html")) pageName += ".html";
                Path pagePath = pageDir.resolve(pageName);
                Util.println(pageUri + " -> " + pagePath);
                downloader.add(pageUri, pagePath);
            }
        }
    }

    /** Download the actual images from the html image files. */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(pageDir)) {
            Document page = Util.parseHtml(pageHtml);
            Element img = page.getElementById("img_mng_enl");
            Uri imageUri = new Uri(img.attr("src"));
            String imageName = pageHtml.getFileName().toString().replace(".html", "");
            imageName = Util.padNumericSequences(imageName, 3);
            Path imagePath = imageDir.resolve(imageName + "." + imageUri.getFileExtension());
            Util.println(imageUri + " -> " + imagePath);
            downloader.add(imageUri, imagePath);
        }
        downloader.download();
    }

    /** Copy the downloaded images to chapter directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(imageDir, imageOutputDir).setDirPattern(chapterPattern).run();
    }

    /** Generate an html index file to read the manga */
    private void generateIndexFile() {
        new GenerateImageViewer(imageOutputDir).setTitle(baseDir.getFileName()).run();
    }
}