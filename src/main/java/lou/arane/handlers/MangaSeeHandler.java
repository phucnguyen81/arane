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
 * Download manga from site mangasee.
 *
 * @author LOU
 */
public class MangaSeeHandler implements Handler {

    private static final Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?");

    private static final Pattern chapterPattern = numberPattern;

    private static final String rootUri = "http://mangasee.co/manga/";

	private final Context ctx;

    public MangaSeeHandler(Context context) {
    	this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.toString();
		return url.startsWith(rootUri);
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
        Path indexPath = ctx.chapterList;
        Document indexDoc = Util.parseHtml(indexPath);
        indexDoc.setBaseUri(rootUri);
        for (Element chapterAddr : indexDoc.getElementsByClass("chapter_link")) {
            String chapterName = chapterAddr.text().trim();
            String href = chapterAddr.absUrl("href");
            Uri chapterUri = Uri.of(href);
            Path chapterPath = ctx.chaptersDir.resolve(chapterName + ".html");
            ctx.add(chapterUri, chapterPath);
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
                Uri pageUri = Uri.of(
                    rootUri + String.format(
                        "?series=%s&chapter=%s&index=1&page=%s",
                        ctx.sourceName, chapterIdx, pageIdx));
                Path pagePath = ctx.pagesDir.resolve(
                    String.format("c%s_p%s.html",
                    Util.padStart(chapterIdx, 3, '0'),
                    Util.padStart(pageIdx, 3, '0')));
                ctx.add(pageUri, pagePath);
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
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            page.setBaseUri(rootUri);
            for (Element img : page.select("a[href] img[src]")) {
                Uri imageUri = Uri.of(img.absUrl("src"));
                Path pageName = imageUri.getFileName();
                Path imagePath = ctx.imagesDir.resolve(pageName);
                ctx.add(imageUri, imagePath);
            }
        }
        ctx.download();
    }

    /** Organize the downloaded images into chapters */
    private void collectImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir).setDirPattern(chapterPattern).run();
    }
}
