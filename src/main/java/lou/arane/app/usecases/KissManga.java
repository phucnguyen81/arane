package lou.arane.app.usecases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.app.Context;
import lou.arane.core.Cmd;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** FIXME
 * Find out why downloading from kissmanga failed.
 * url = http://kissmanga.com/Manga/Lighter
 * <p>
 * The error code is 403 or 503.
 * This is probably some security measure.
 * Need to find out how to bypass this blockage.
 * Maybe making a fake browser request?
 */

/**
 * Download manga from kissmanga site.
 *
 * @author LOU
 */
public class KissManga implements Cmd {

    private static final String BASE_URI = "http://kissmanga.com/";

    /** Patterns of images embeded in javascript element */
    private static final Pattern IMAGE_PATTERN = Pattern.compile(
    		"lstImages\\.push\\(\"(.+)\"\\);"
    		);

	private final Context ctx;

	public KissManga(Context context) {
		this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.externalForm();
		return url.startsWith(BASE_URI);
	}

	@Override
	public void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadImages();
	}

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document chapters = Util.parseHtml(ctx.chapterList, BASE_URI);
        for (Element chapterAddr : chapters.select("table[class=listing] a[href]")) {
            Optional<URLResource> chapterUrl = URLResource.of(chapterAddr.absUrl("href"));
            if (chapterUrl.isPresent()) {
	            String chapterName = Util.join(Paths.get(chapterUrl.get().filePath()), "_");
	            if (!chapterName.endsWith(".html")) chapterName += ".html";
	            Path chapterPath = ctx.chaptersDir.resolve(chapterName);
	            ctx.add(chapterUrl.get(), chapterPath);
            }
        }
        ctx.download();
    }

    /**
     * Download images for each chapter. The image urls are extracted from
     * javascript's content using regex.
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            String chapterName = chapterHtml.getFileName().toString();
            chapterName = Util.removeFileExtension(chapterName);
            Document chapter = Util.parseHtml(chapterHtml);
            for (Element script : chapter.select("script[type=text/javascript]")) {
                Matcher matcher = IMAGE_PATTERN.matcher(script.html());
                while (matcher.find()) {
                    Optional<URLResource> imageUrl = URLResource.of(matcher.group(1));
                    if (imageUrl.isPresent()) {
	                    String imageName = chapterName + "_" + imageUrl.get().fileName();
	                    imageName = Util.padNumericSequences(imageName, 3);
	                    Path imagePath = ctx.imagesDir.resolve(imageName);
	                    ctx.add(imageUrl.get(), imagePath);
                    }
                }
            }
        }
        ctx.download();
    }
}
