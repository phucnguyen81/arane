package lou.arane.app.usecases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lou.arane.app.Context;
import lou.arane.core.Cmd;
import lou.arane.scripts.CopyFiles;
import lou.arane.util.FileResource;
import lou.arane.util.ToString;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Download mangas from manga.life site
 *
 * @author Phuc
 */
public class MangaLife implements Cmd {

    /** base location of all mangas for this site */
    private static final String BASE_URI = "http://manga.life";

    private final Context ctx;

    public MangaLife(Context context) {
        this.ctx = context;
    }

    @Override
    public String toString() {
        return ToString.of(MangaLife.class).add(BASE_URI).nln()
                .add(ctx).str();
    }

    @Override
    public boolean canRun() {
        // domain must match
        String url = ctx.source.externalForm();
        return url.startsWith(BASE_URI);
    }

    /** Run all the steps of downloading the manga */
    @Override
    public void doRun() {
        ctx.downloadChapterList();
        downloadChapters();
        downloadPages();
        downloadImages();
        collectImagesIntoChapters();
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
        ctx.chapterList
            .parseHtml(BASE_URI)
            .select("a[href]")
            .stream()
            .map(a -> a.absUrl("href"))
            .filter(href -> href.contains(ctx.sourceName))
            .forEach(href -> URLResource.of(href).ifPresent(chapterUrl -> {
                String chapterPath = Util.join(Paths.get(chapterUrl.filePath()), "_");
                Path chapterFile = ctx.chaptersDir.resolve(chapterPath + ".html");
                ctx.add(chapterUrl, new FileResource(chapterFile));
            }));
        ctx.download();
    }

    /**
     * Download pages for each chapter. A page url is built from chapter-index
     * and page-index.
     */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
            addPages(chapter);
            ctx.download();
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

    /** TODO do source.resolve(chapter, index, page) here, not doing strings */
    private void addPage(String chapter, String index, String page) {
        String base = ctx.source.externalForm();
        base = Util.removeEnding(base, "/");
        String pageUriStr = Util.join(Arrays.asList(base, chapter, index, page), "/");
        URLResource.of(pageUriStr).ifPresent(pageUrl -> {
            Path pagePath = ctx.pagesDir.resolve(chapter + "_" + page + ".html");
            ctx.add(pageUrl, new FileResource(pagePath));
        });
    }

    /**
     * Download the actual images from the html image files such as:
     *
     * <pre>
     * <img src="http://.../0010-001.jpg" onerror="this.onerror=null;this.src='http://.../0010-001.jpg'"/>
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            addImageToDownload(page);
        }
        ctx.download();
    }

    private void addImageToDownload(Document page) {
        for (Element img : page.select("a[href] img[src]")) {
            URLResource.of(img.absUrl("src")).ifPresent(imgUrl -> {
                imgUrl = new URLResource(imgUrl, onErrorUrls(img.attr("onerror")));
                Path imgPath = ctx.imagesDir.resolve(imgUrl.fileName());
                ctx.add(imgUrl, new FileResource(imgPath));
            });
        }
    }

    private List<URLResource> onErrorUrls(String onerrorAttr) {
        return ctx.findSourceUrls(onerrorAttr)
                .stream()
                .map(URLResource::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Organize the downloaded images into sub-directories. A sub-directory
     * corresponds to a chapter.
     */
    private void collectImagesIntoChapters() {
        Util.createDirectories(ctx.outputDir);
        new CopyFiles(ctx.imagesDir, ctx.outputDir)
                .setDirPattern("\\d+(\\.\\d+)?")
                .run();
    }

}
