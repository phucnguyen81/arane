package lou.arane.app.usecases;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lou.arane.app.Context;
import lou.arane.core.Cmd;
import lou.arane.scripts.CopyFiles;
import lou.arane.util.FileResource;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Download mangas from mangakakalot site
 *
 * @author Phuc
 */
public class Manganelo implements Cmd {

    private static final String BASE_URL = "http://manganelo.com/";

    private static final Pattern CHAPTER_PATTERN = Pattern
        .compile("chapter_\\d+[a-zA-Z]?");

    private final Context ctx;

    public Manganelo(Context context) {
        this.ctx = context;
    }

    @Override
    public boolean canRun() {
        String url = ctx.source.externalForm();
        return url.startsWith(BASE_URL);
    }

    /** Run all the steps of downloading the manga */
    @Override
    public void doRun() {
        ctx.downloadChapterList();
        downloadChapters();
        downloadImagesForAllChapters();
        organizeImagesIntoChapters();
    }

    /**
     * Download chapter pages; their urls are from the master html file.
     */
    private void downloadChapters() {
        Document chapters = ctx.chapterList.parseHtml(BASE_URL);
        chapters.select("div[class=chapter-list] a[href]")
            .stream()
            .forEach(chapter -> {
                URLResource.of(chapter.absUrl("href")).ifPresent(url -> {
                    String name = url.fileName();
                    Path path = ctx.chaptersDir.resolve(name + ".html");
                    ctx.add(url, new FileResource(path));
                });
            });
        ctx.download();
    }

    private void downloadImagesForAllChapters() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            addImages(chapterHtml);
        }
        ctx.download();
    }

    /**
     * Find the images of a given chapter
     */
    private void addImages(Path chapterHtml) {
        String chapterName = chapterHtml.getFileName().toString();
        chapterName = Util.removeFileExtension(chapterName);
        Document chapter = Util.parseHtml(chapterHtml, BASE_URL);
        for (Element img : chapter.select("div[id=vungdoc] img[src]")) {
            addImages(img, chapterName);
        }
    }

    /** Find images from text of a img element */
    private void addImages(Element img, String chapterName) {
        URLResource.of(img.absUrl("src")).ifPresent(imgUrl -> {
            String imgName = chapterName + "_" + img.siblingIndex();
            imgName += "." + imgUrl.fileExtension();
            imgName = Util.padNumericSequences(imgName, 3);
            Path imgPath = ctx.imagesDir.resolve(imgName);
            ctx.add(imgUrl, new FileResource(imgPath));
        });
    }

    /**
     * Images downloaded from previous step are organized into chapter
     * directories
     */
    private void organizeImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir)
            .setDirPattern(CHAPTER_PATTERN).run();
    }

}
