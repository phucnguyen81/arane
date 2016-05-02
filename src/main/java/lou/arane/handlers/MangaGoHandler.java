package lou.arane.handlers;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.core.Context;
import lou.arane.core.Handler;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.script.CopyFiles;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Download manga from mangago site
 *
 * @author LOU
 */
public class MangaGoHandler implements Handler {

    private static final Pattern chapterPattern = Pattern.compile(
        "ch?(?<chapter>\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Pattern to look for image url in javascript handler such as:
     *
     * <pre>javascript:this.src='http://i3.rocaca.net/r/newpiclink/kajika/1/a6150548e03c577efe5924463df969e7.jpeg';</pre>
     */
    private static final Pattern srcPattern = Pattern.compile("src='(.+)'");

    private static final String BASE_URL = "http://www.mangago.me/read-manga/";

	private final Context ctx;

    public MangaGoHandler(Context context) {
    	this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.toString();
		return url.startsWith(BASE_URL);
	}

	@Override
	public void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadPages();
		downloadImages();
		collectImagesIntoChapters();
	}

    /**
     * Download chapter pages by extracting their urls from the master html file
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(ctx.chapterList);
        for (Element chapterAddr : rootFile.select("table[id=chapter_table] a[href]")) {
            Uri chapterUri = new Uri(chapterAddr.attr("href"));
            String chapterName = Util.join(chapterUri.getFilePath(), "_");
            Path chapterPath = ctx.chaptersDir.resolve(chapterName + ".html");
            ctx.add(chapterUri, chapterPath);
        }
        ctx.download();
    }

    /**
     * Download pages for each chapter. Given a chapter html file, the pages can
     * be extracted from the ul element such as:
     *
     * <pre>
        <ul id="dropdown-menu-page">
            <li><a href="/read-manga/kajika/mf/v01/c001/">page 1 of 31</a></li>
            <li><a href="/read-manga/kajika/mf/v01/c001/2/">page 2 of 31</a></li>
            ...
        </ul>
     * </pre>
     */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URL);
            for (Element addr : chapter.select("ul[id=dropdown-menu-page] a[href]")) {
                Uri pageUri = new Uri(addr.absUrl("href"));
                String pageName = Util.join(pageUri.getFilePath(), "_");
                if (!pageName.endsWith(".html")) pageName += ".html";
                Path pagePath = ctx.pagesDir.resolve(pageName);
                ctx.add(pageUri, pagePath);
            }
        }
        ctx.download();
    }

    /** Download the actual images from the html pages */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            Uri imageUri = findImageUri(page);
            String pageName = pageHtml.getFileName().toString().replace(".html", "");
            Path imagePath = ctx.imagesDir.resolve(pageName + "." + imageUri.getFileExtension());
            ctx.add(imageUri, imagePath);
        }
        ctx.download();
    }

    /**
     * Find the uri of the image from corresponding html page.
     * Look for img element such as:
     *
     * <pre>
        <a href="http://www.mangago.me/read-manga/kajika/mf/v01/c001/2/" id="pic_container">
            <img src="http://i3.mangapicgallery.com/r/newpiclink/kajika/1/a6150548e03c577efe5924463df969e7.jpeg"
                border="0"
                onerror="javascript:this.src='http://i3.rocaca.net/r/newpiclink/kajika/1/a6150548e03c577efe5924463df969e7.jpeg';">
            ...
        </a>
     * </pre>
     *
     * Note that the onerror attribute is an alternative url
     * to download the image in case the original url fails.
     */
    private Uri findImageUri(Document page) {
        Element img = page.select("a[id=pic_container] img[border][src]").first();
        Uri imageUri = new Uri(img.attr("src"));
        if (img.hasAttr("onerror")) {
            String onerror = img.attr("onerror");
            Matcher onerrorMatcher = srcPattern.matcher(onerror);
            if (onerrorMatcher.find()) {
                imageUri.addAlternatives(new Uri(onerrorMatcher.group(1)));
            }
        }
        return imageUri;
    }

    /** Copy the downloaded images to output directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir)
            .setDirResolver(fileName -> {
                Matcher matcher = chapterPattern.matcher(fileName);
                return matcher.find() ? matcher.group("chapter") : null;
            })
            .padNumericSequences(3)
            .run();
    }

}
