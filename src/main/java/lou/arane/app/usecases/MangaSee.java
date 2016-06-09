package lou.arane.app.usecases;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.app.Context;
import lou.arane.core.Cmd;
import lou.arane.scripts.CopyFiles;
import lou.arane.util.FileResource;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Download manga from site mangasee.
 *
 * @author LOU
 */
public class MangaSee implements Cmd {

    private static final Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?");

    private static final Pattern chapterPattern = numberPattern;

    private static final String baseUri = "http://mangasee.co/manga/";

	private final Context ctx;

    public MangaSee(Context context) {
    	this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.externalForm();
		return url.startsWith(baseUri);
	}

	@Override
	public void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadPages();
		downloadImages();
		collectImagesIntoChapters();
	}

    private void downloadChapters() {
        FileResource indexPath = ctx.chapterList;
        Document indexDoc = indexPath.parseHtml(baseUri);
        for (Element chapterAddr : indexDoc.getElementsByClass("chapter_link")) {
            String chapterName = chapterAddr.text().trim();
            String href = chapterAddr.absUrl("href");
            URLResource.of(href).ifPresent(chapterUrl -> {
            	Path chapterPath = ctx.chaptersDir.resolve(chapterName + ".html");
            	ctx.add(chapterUrl, new FileResource(chapterPath));
            });
        }
        ctx.download();
    }

    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            Matcher chapterIndMatcher = numberPattern.matcher(chapterHtml.getFileName().toString());
            String chapterIdx = "";
            if (chapterIndMatcher.find()) {
                chapterIdx = chapterIndMatcher.group();
            }
            else {
                throw new IllegalArgumentException("Cannot detect chapter index for " + chapterHtml);
            }
            Document chapter = Util.parseHtml(chapterHtml);
            Element pageForm = chapter.getElementById("pages");
            int pageNo = pageForm.getElementsByTag("option").size();
            for (String pageIdx : Util.rangeClosed(1, pageNo)) {
                Optional<URLResource> pageUrl = URLResource.of(
                    baseUri + String.format(
                    	"?series=%s&chapter=%s&index=1&page=%s"
                    	, ctx.sourceName
                    	, chapterIdx
                    	, pageIdx));
                if (pageUrl.isPresent()) {
	                Path pagePath = ctx.pagesDir.resolve(
	                    String.format("c%s_p%s.html",
	                    Util.padStart(chapterIdx, 3, '0'),
	                    Util.padStart(pageIdx, 3, '0')));
	                ctx.add(pageUrl.get(), new FileResource(pagePath));
                }
            }
        }
        ctx.download();
    }

    /**
     * For each page, look for image under:
        <a href="../manga/?series=DaaDaaDaa&chapter=1&index=1&page=2">
            <img src="http://2.bp.blogspot.com/-WtXeDS7A2cQ/VkP1rrLBqiI/AAAAAAAB07M/gj0W0bYxLD8/s16000/0001-001.jpg" />
        </a>
     */
    private void downloadImages() {
    	Util
    	.findHtmlFiles(ctx.pagesDir)
    	.stream()
    	.map(pages -> Util.parseHtml(pages, baseUri))
    	.forEach(this::downloadImages);
        ctx.download();
    }

	private void downloadImages(Document pages) {
		pages
		.select("a[href] img[src]")
		.stream()
        .map(img -> img.absUrl("src"))
        .map(URLResource::of)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(imageUrl -> {
        	String pageName = imageUrl.fileName();
        	Path imagePath = ctx.imagesDir.resolve(pageName);
        	ctx.add(imageUrl, new FileResource(imagePath));
        });
	}

    /** Organize the downloaded images into chapters */
    private void collectImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir).setDirPattern(chapterPattern).run();
    }
}
