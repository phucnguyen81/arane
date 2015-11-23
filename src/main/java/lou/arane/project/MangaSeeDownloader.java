package lou.arane.project;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.script.CopyFiles;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Download manga from site mangasee.
 *
 * @author LOU
 */
public class MangaSeeDownloader extends BaseDownloader {

    private static final Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?");

    private static final Pattern chapterPattern = numberPattern;

    private static final String rootUri = "http://mangasee.co/manga/";

    private final String mangaName;
    private final Uri mangaUri;
    private final Path chapterList;

    private final Path chapterDir;
    private final Path pageDir;
    private final Path imageDir;

    private final Path outputDir;

    public MangaSeeDownloader(String mangaName, Path baseDir) {
        this.mangaName = mangaName;
        mangaUri = Uri.of(rootUri + "?series=" + mangaName);
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        pageDir = baseDir.resolve("pages");
        imageDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
    }

    public void run() {
        downloadChaperList();
        downloadChapters();
        downloadPages();
        downloadImages();
        collectImagesIntoChapters();
    }

    /** Download the initial chapter listing */
    private void downloadChaperList() {
        Util.deleteIfExists(chapterList);
        add(mangaUri, chapterList);
        download();
        Check.postCond(Util.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    private void downloadChapters() {
        Path indexPath = chapterList;
        Document indexDoc = parseHtml(indexPath);
        indexDoc.setBaseUri(rootUri);
        for (Element chapterAddr : indexDoc.getElementsByClass("chapter_link")) {
            String chapterName = chapterAddr.text().trim();
            String href = chapterAddr.absUrl("href");
            Uri chapterUri = Uri.of(href);
            Path chapterPath = chapterDir.resolve(chapterName + ".html");
            add(chapterUri, chapterPath);
        }
        download();
    }

    private void downloadPages() {
        for (Path chapterHtml : findHtmlFiles(chapterDir)) {
            Matcher chapterIndMatcher = numberPattern.matcher(chapterHtml.getFileName().toString());
            String chapterIdx = "";
            if (chapterIndMatcher.find())
                chapterIdx = chapterIndMatcher.group();
            else
                throw new IllegalArgumentException("Cannot detect chapter index for " + chapterHtml);
            Document chapter = parseHtml(chapterHtml);
            Element pageForm = chapter.getElementById("pages");
            int pageNo = pageForm.getElementsByTag("option").size();
            for (String pageIdx : Util.rangeClosed(1, pageNo)) {
                Uri pageUri = Uri.of(
                    rootUri + String.format(
                        "?series=%s&chapter=%s&index=1&page=%s",
                        mangaName, chapterIdx, pageIdx));
                Path pagePath = pageDir.resolve(
                    String.format("c%s_p%s.html",
                    Util.padStart(chapterIdx, 3, '0'),
                    Util.padStart(pageIdx, 3, '0')));
                add(pageUri, pagePath);
            }
        }
        download();
    }

    /**
     * For each page, look for image under:
        <a href="../manga/?series=DaaDaaDaa&chapter=1&index=1&page=2">
            <img src="http://2.bp.blogspot.com/-WtXeDS7A2cQ/VkP1rrLBqiI/AAAAAAAB07M/gj0W0bYxLD8/s16000/0001-001.jpg" />
        </a>
     */
    private void downloadImages() {
        for (Path pageHtml : findHtmlFiles(pageDir)) {
            Document page = parseHtml(pageHtml);
            page.setBaseUri(rootUri);
            for (Element img : page.select("a[href] img[src]")) {
                Uri imageUri = Uri.of(img.absUrl("src"));
                Path pageName = imageUri.getFileName();
                Path imagePath = imageDir.resolve(pageName);
                add(imageUri, imagePath);
            }
        }
        download();
    }

    /** Organize the downloaded images into chapters */
    private void collectImagesIntoChapters() {
        new CopyFiles(imageDir, outputDir).setDirPattern(chapterPattern).run();
    }

}
