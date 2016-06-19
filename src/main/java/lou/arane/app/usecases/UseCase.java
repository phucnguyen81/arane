package lou.arane.app.usecases;

import java.nio.file.Path;

import org.jsoup.nodes.Document;

import lou.arane.app.Context;
import lou.arane.app.Download;
import lou.arane.core.Cmd;
import lou.arane.util.Log;
import lou.arane.util.ToString;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Common flow for a use case.
 *
 * @author Phuc
 */
public abstract class UseCase implements Cmd {

    private final Context ctx;

    public UseCase(Context context) {
        this.ctx = context;
    }

    @Override
    public String toString() {
        return ToString.of(UseCase.class).add(baseUrl()).nln().add(ctx).str();
    }

    @Override
    public final boolean canRun() {
        String url = ctx.source.externalForm();
        return url.startsWith(baseUrl().externalForm());
    }

    abstract URLResource baseUrl();

    /** Run all the steps of downloading the manga */
    @Override
    public final void doRun() {
        ctx.downloadChapterList();
        downloadChapters();
        downloadPages();
        downloadImages();
    }

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document initial = ctx.chapterList.parseHtml(baseUrl().externalForm());
        Iterable<Download> chapters = getChaptersFromInitialFile(initial);
        ctx.download(chapters);
    }

    /**
     * Get downloads for chapters extracted from the initial chapter listing
     */
    abstract Iterable<Download> getChaptersFromInitialFile(Document initial);

    /**
     * Download pages for each chapter. A page url is built from chapter-index
     * and page-index.
     */
    private void downloadPages() {
        if (Util.notExists(ctx.chaptersDir)) {
            throw new RuntimeException("No chapters downloaded to " + ctx.chaptersDir);
        }
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, baseUrl().externalForm());
            Iterable<Download> pages = getPagesFromChapter(chapter);
            ctx.download(pages);
            Iterable<Download> images = getImagesFromChapter(chapter);
            ctx.download(images);
        }
    }

    /**
     * Get html pages listed in each chapter. Return empty if the chapter does
     * not contain page links (in this case, it should contains direct image
     * link).
     */
    abstract Iterable<Download> getPagesFromChapter(Document chapter);

    /**
     * Get image links listed in each chapter. Return empty if the chapter does
     * not contain image links (in this case, it should contain the html page
     * link)
     */
    abstract Iterable<Download> getImagesFromChapter(Document chapter);

    /**
     * Download the actual images from the html image files.
     */
    private void downloadImages() {
        if (Util.notExists(ctx.pagesDir)) {
            Log.info(ctx.pagesDir, "does not exist, skip getting pages from", ctx.pagesDir);
        }
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            Iterable<Download> images = getImagesFromPage(page);
            ctx.download(images);
        }
    }

    abstract Iterable<Download> getImagesFromPage(Document page);

}
