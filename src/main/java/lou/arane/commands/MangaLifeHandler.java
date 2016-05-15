package lou.arane.commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lou.arane.base.Command;
import lou.arane.base.Context;
import lou.arane.scripts.CopyFiles;
import lou.arane.util.Url;
import lou.arane.util.Util;

/**
 * Download mangas from manga.life site
 *
 * @author Phuc
 */
public class MangaLifeHandler implements Command {

    /** base location of all mangas for this site */
    private static final String BASE_URI = "http://manga.life/";

	private final Context ctx;

	public MangaLifeHandler(Context context) {
		this.ctx = context;
    }

	@Override
	public boolean canRun() {
		//domain must match
		String url = ctx.source.toString();
		return url.startsWith(BASE_URI);
	}

	/** Run all the steps of downloading the manga */
	@Override
	public void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadPages();
		downloadImages();
		collectImagesIntoChapters();
	}

    /**
     * Download chapter pages by extracting their urls from the master html
     * file:
     *
     * <pre>
     *   <a href="/read-online/GateJietaiKareNoChiNiteKakuTatakeri/chapter-35/index-1/page-1">Chapter 35</a>
     * </pre>
     */
    private void downloadChapters() {
        Document chapters = Util.parseHtml(ctx.chapterList, BASE_URI);
        chapters
        .select("a[href]")
        .stream()
        .map(addr -> addr.absUrl("href"))
        .filter(href -> href.contains(ctx.sourceName))
        .map(href -> new Url(href))
        .forEach(chapterUri -> {
            String chapterPath = Util.join(Paths.get(chapterUri.filePath()), "_");
            ctx.add(chapterUri, ctx.chaptersDir.resolve(chapterPath + ".html"));
        });
        ctx.download();
    }

    /** Download pages for each chapter.
     * A page url is built from chapter-index and page-index. */
    private void downloadPages() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
            addPages(chapter);
            ctx.download();
        }
    }

    /**
     * Find pages for a chapter by searching for page urls in the chapter
     * document
     */
    private void addPages(Document chapterDoc) {
        String selectChapter = "select[class*=changeChapterSelect] option[value][selected]";
        for (Element chapterOpt : chapterDoc.select(selectChapter)) {
            String[] chapterSpec = chapterOpt.attr("value").split(";");
            if (chapterSpec.length == 2) {
                String chapter = "chapter-" + chapterSpec[0];
                String index = "index-" + chapterSpec[1];
                addPages(chapterDoc, chapter, index);
            }
        }
    }

    private void addPages(Document chapterDoc, String chapter, String index) {
        String selectPage = "select[class*=changePageSelect] option[value]";
        for (Element pageOption : chapterDoc.select(selectPage)) {
            String page = pageOption.attr("value");
            addPage(chapter, index, page);
        }
    }

    /** TODO do source.resolve(chapter, index, page) here, not doing strings */
    private void addPage(String chapter, String index, String page) {
        String base = ctx.source.toString();
        base = Util.removeEnding(base, "/");
        String pageUriStr = Util.join(Arrays.asList(base, chapter, index, page), "/");
        Url pageUri = new Url(pageUriStr);
        Path pagePath = ctx.pagesDir.resolve(chapter + "_" + page + ".html");
        ctx.add(pageUri, pagePath);
    }

	/**
     * Download the actual images from the html image files such as:
     *
     * <pre>
     * <img src="http://.../0010-001.jpg" onerror="this.onerror=null;this.src='http://.../0010-001.jpg'"/>
     * </pre>
     */
    private void downloadImages() {
        for (Path pageHtml : Util.findHtmlFiles(ctx.pagesDir)) {
            Document page = Util.parseHtml(pageHtml);
            addImageToDownload(page);
        }
        ctx.download();
    }

    private void addImageToDownload(Document page) {
        for (Element img : page.select("a[href] img[src]")) {
            Url imgUri = new Url(img.absUrl("src"));
            addOnErrorUri(imgUri, img.attr("onerror"));
            Path imgPath = ctx.imagesDir.resolve(imgUri.fileName());
            ctx.add(imgUri, imgPath);
        }
    }

    private void addOnErrorUri(Url imgUri, String onerrorAttr) {
        for (String onerrorUrl : ctx.findSourceUrls(onerrorAttr)) {
        	Url onerrorUri = new Url(onerrorUrl);
        	imgUri.alternatives.add(onerrorUri);
        }
    }

    /** Organize the downloaded images into sub-directories.
     * A sub-directory corresponds to a chapter. */
    private void collectImagesIntoChapters() {
        Util.createDirectories(ctx.outputDir);
        new CopyFiles(ctx.imagesDir, ctx.outputDir)
            .setDirPattern("\\d+(\\.\\d+)?")
            .run();
    }


}