package lou.arane.usecases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import lou.arane.base.Cmd;
import lou.arane.base.Context;
import lou.arane.base.URLResource;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** FIXME
 * results seem broken, see example of downloading 'paladin'
 */

/**
 * Download comics from blogtruyen site
 *
 * @author LOU
 */
public class BlogTruyen implements Cmd {

    /** Scheme of this site */
    private static final String BASE_URL = "http://blogtruyen.com/";

	private final Context ctx;

    /**
     * Create a downloader to download a story to a directory
     *
     * @param story = name of the story, e.g. "vo-than"
     * @param baseDir = dir to download to, e.g. "/mangas/Vo Than"
     */
    public BlogTruyen(Context context) {
    	this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.urlString();
		return url.startsWith(BASE_URL);
	}

	@Override
	public void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadImages();
		copyImagesToOutputDir();
	}

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     *  <div class="list-wrap" id="list-chapters">
     *      <a href="/truyen/hiep-khach-giang-ho/chap-473" >
     *      <a href="https://www.mediafire.com/?i4fd23z6a8r25i8" rel="nofollow">
     * ...
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(ctx.chapterList, BASE_URL);
        Elements chapterAddresses = rootFile.select("a[href]");
        for (Element chapterAddr : chapterAddresses) {
            String href = chapterAddr.absUrl("href");
			Optional<URLResource> chapterUri = URLResource.of(href);
            if (chapterUri.isPresent()) {
	            String path = chapterUri.get().filePath();
	            if (path.contains(ctx.sourceName)) {
	            	String chapterName = chapterUri.get().fileName().toString();
	            	if (!chapterName.endsWith(".html")) {
	            		chapterName += ".html";
	            	}
	            	Path chapterPath = ctx.chaptersDir.resolve(chapterName);
	            	ctx.add(chapterUri.get(), chapterPath);
	            }
            }
        }
        ctx.download();
    }

    /**
     * Download the actual images for each chapter. The images can be found in:
     *
     * <pre>
     *   <article id="content">
     *       <img src="http://4.bp.blogspot.com/.../Vol8-Chap48-P00.jpg?imgmax=3000" />
     *       ...
     *   </article>
     * </pre>
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            String imageDir = Util.removeFileExtension(chapterHtml.getFileName().toString());
            Document page = Util.parseHtml(chapterHtml);
            Elements images = page.select("article[id=content] img[src]");
            for (Element image : images) {
            	URLResource.of(image.absUrl("src")).ifPresent(imageUri -> {
            		String imageName = imageUri.fileName().toString();
            		Path imagePath = Paths.get(imageDir, imageName);
            		imagePath = ctx.imagesDir.resolve(imagePath);
            		ctx.add(imageUri, imagePath);
            	});
            }
            ctx.download();
        }
    }

    /** Make output chapters from downloaded images */
    private void copyImagesToOutputDir() {
        Util
        .list(ctx.imagesDir)
        .filter(Util::isNotEmpty)
        .forEach(src -> {
            Path rel = ctx.imagesDir.relativize(src);
            Path dst = ctx.outputDir.resolve(rel);
            Util.copy(src, dst);
        });
    }

}
