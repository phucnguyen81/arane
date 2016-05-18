package lou.arane.usecases;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lou.arane.base.Cmd;
import lou.arane.base.Context;
import lou.arane.base.URLResource;
import lou.arane.util.Util;

/**
 * Download from iztruyetranh site
 *
 * @author Phuc
 */
public class IzTruyenTranh implements Cmd {

    /* domain */
    private static final String BASE_URI = "http://iztruyentranh.com/";

    /* pattern to look for images embeded in the chapter pages */
    private static final Pattern DATA_IMAGES_PATTERN = Pattern.compile("data\\s*=\\s*'(?<imgs>http:.+)'");

	private final Context ctx;

    /**
     * Create a downloader to download a story to a directory
     *
     * @param story = base name of the story, e.g. "ban_long-117"
     * @param baseDir = dir to download to, e.g. "mangas/Ban Long"
     */
    public IzTruyenTranh(Context context) {
    	this.ctx = context;
    }

    @Override
	public boolean canRun() {
    	// domain must match
    	String url = ctx.source.urlString();
    	return url.startsWith(BASE_URI);
    }

    @Override
	public void doRun() {
    	ctx.downloadChapterList();
    	downloadChapters();
    	downloadImages();
	}

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     *   <div class="chapter-list">
     *      <div class="row">
     *          <span>
     *              <a href="http://izmanga.com/chapter/ban_long/117/94716">001</a>
     *          </span>
     * ...
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(ctx.chapterList, BASE_URI);
        Elements chapterAddresses = rootFile.select("div[class=chapter-list] a[href]");
        for (Element chapterAddr : chapterAddresses) {
            URLResource chapterUri = new URLResource(chapterAddr.absUrl("href"));
            String chapterName = chapterAddr.ownText();
            if (!chapterName.endsWith(".html")) {
                chapterName += ".html";
            }
            Path chapterPath = ctx.chaptersDir.resolve(chapterName);
            ctx.add(chapterUri, chapterPath);
        }
        ctx.download();
    }

    /**
     * Download the actual images for each chapter.
     * The image links are found in javascript content:
     * <pre>
     *  data = 'http://2.bp.blogspot.com/-A9scOkmQ61Q/UP_zXu7qUbI/AAAAAAAAFFc/exlxGRoLYuw/0%252520copy.jpg?imgmax=2000|http://2.bp.blogspot.com/-8iv32sR7N2k/UP_zZDw3_yI/AAAAAAAAFFg/jXUNe3dRca0/0%252520Credit-ban-long.jpg?imgmax=2000|...
     * </pre>
     * So we have to parse the string to get the image links.
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            String chapterFileName = chapterHtml.getFileName().toString();
            chapterFileName = Util.removeFileExtension(chapterFileName);
			Path chapterPath = ctx.imagesDir.resolve(chapterFileName);
            Document page = Util.parseHtml(chapterHtml);
            Matcher imagesMatcher = DATA_IMAGES_PATTERN.matcher(page.data());
            if (imagesMatcher.find()) {
                int idx = 0;
                for (String img : imagesMatcher.group("imgs").split("\\|")) {
                    idx += 1;
                    URLResource imageUri = new URLResource(img.trim());
                    String imageName = idx + "_" + imageUri.fileName();
                    imageName = Util.padNumericSequences(imageName, 3);
                    Path imagePath = chapterPath.resolve(imageName);
                    ctx.add(imageUri, imagePath);
                }
            }
            ctx.download();
        }
    }

}
