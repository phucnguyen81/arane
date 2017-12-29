package lou.arane.app.usecases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lou.arane.app.Context;
import lou.arane.core.Cmd;
import lou.arane.util.FileResource;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Download comics from blogtruyen site
 *
 * @author LOU
 */
public class BlogTruyen implements Cmd {

    private static final String BASE_URL = "http://blogtruyen.com/";

    private final Context ctx;

    public BlogTruyen(Context context) {
        this.ctx = context;
    }

    @Override
    public boolean canRun() {
        String url = ctx.source.externalForm();
        return url.startsWith(BASE_URL);
    }

    @Override
    public void doRun() {
        ctx.downloadChapterList();
        downloadChapters();
        downloadImages();
    }

    /**
     * Download chapter pages by extracting their urls from the master html
     * file.
     */
    private void downloadChapters() {
        Document root = ctx.chapterList.parseHtml(BASE_URL);
        Elements chapters = root.select("div[id=list-chapters] a[href]");
        for (Element chapter : chapters) {
            String href = chapter.absUrl("href");
            URLResource.of(href).ifPresent(chapterUri -> {
                String name = chapterUri.fileName().toString();
                if (!name.endsWith(".html")) name += ".html";
                Path path = ctx.chaptersDir.resolve(name);
                ctx.add(chapterUri, new FileResource(path));
            });
        }
        ctx.download();
    }

    /**
     * Download the actual images for each chapter.
     */
    private void downloadImages() {
        for (Path chapter : Util.findHtmlFiles(ctx.chaptersDir)) {
            String chapterName = chapter.getFileName().toString();
            String imageDir = Util.removeFileExtension(chapterName);
            Document page = Util.parseHtml(chapter);
            page.select("article[id=content] img[src]")
                .stream()
                .map(image -> image.absUrl("src"))
                .map(URLResource::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(imageUri -> {
                    String imageName = imageUri.fileName().toString();
                    Path imagePath = Paths.get(imageDir, imageName);
                    imagePath = ctx.imagesDir.resolve(imagePath);
                    ctx.add(imageUri, new FileResource(imagePath));
                });
            ctx.download();
        }
    }

}
