package lou.arane.usecases;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lou.arane.base.Cmd;
import lou.arane.base.Context;
import lou.arane.io.URLResource;
import lou.arane.scripts.CopyFiles;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Download manga from mangago site
 *
 * @author LOU
 */
public class MangaGo implements Cmd {

    private static final String BASE_URL = "http://www.mangago.me/read-manga/";

	private final Context ctx;

    public MangaGo(Context context) {
    	this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.externalForm();
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
            Optional<URLResource> chapterUrl = URLResource.of(chapterAddr.attr("href"));
            if (chapterUrl.isPresent())	{
	            String chapterName = chapterUrl.get().fileName().toString();
	            Path chapterPath = ctx.chaptersDir.resolve(chapterName + ".html");
	            ctx.add(chapterUrl.get(), chapterPath);
            }
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
        	String chapterName = Util.removeFileExtension(chapterHtml.getFileName().toString());
            Document chapter = Util.parseHtml(chapterHtml, BASE_URL);
            for (Element addr : chapter.select("ul[id=dropdown-menu-page] a[href]")) {
                Optional<URLResource> pageUrl = URLResource.of(addr.absUrl("href"));
                if (pageUrl.isPresent()) {
	                String pageName = chapterName + "_" + addr.ownText();
	                if (!pageName.endsWith(".html")) pageName += ".html";
	                Path pagePath = ctx.pagesDir.resolve(pageName);
	                ctx.add(pageUrl.get(), pagePath);
                }
            }
        }
        ctx.download();
    }

    /** Download the actual images from the html pages */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            Optional<URLResource> imageUri = findImageUri(page);
            if (imageUri.isPresent()) {
	            String pageName = pageHtml.getFileName().toString().replace(".html", "");
	            Path imagePath = ctx.imagesDir.resolve(pageName + "." + imageUri.get().fileExtension());
	            ctx.add(imageUri.get(), imagePath);
            }
        }
        ctx.download();
    }

    /**
     * Find the source of the image from corresponding html page.
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
    private Optional<URLResource> findImageUri(Document page) {
        Element img = page.select("a[id=pic_container] img[border][src]").first();
        Optional<URLResource> imageUrl = URLResource.of(img.attr("src"));
        if (!imageUrl.isPresent()) {
        	return Optional.empty();
        }
        else if (img.hasAttr("onerror")) {
        	return ctx.findSourceUrls(img.attr("onerror"))
				.stream()
				.map(URLResource::of)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.collectingAndThen(
					Collectors.toList()
					, onerrorUrls -> Optional.of(
						new URLResource(imageUrl.get(), onerrorUrls))));
        } else {
        	return imageUrl;
        }
    }

    /** Copy the downloaded images to output directories */
    private void collectImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir)
        .setDirPattern(Pattern.compile("\\d+"))
        .padNumericSequences(3)
        .run();
    }

}
