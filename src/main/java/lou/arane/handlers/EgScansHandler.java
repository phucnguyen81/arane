package lou.arane.handlers;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lou.arane.core.Context;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.script.CopyFiles;

/**
 * Download mangas from manga.life site
 *
 * @author Phuc
 */
public class EgScansHandler implements Runnable {

    private static final String BASE_URI = "http://read.egscans.com/";

    private static final Pattern CHAPTER_PATTERN = Pattern.compile("chapter_\\d+[a-zA-Z]?");

	private final Context ctx;

	public EgScansHandler(Context context) {
		this.ctx = context;
    }

    @Override
	public void run() {
		if (canRun()) {
			doRun();
		}
    }

	private boolean canRun() {
		//domain must match
		String url = ctx.source.toString();
		return url.startsWith(BASE_URI);
	}

	/** Run all the steps of downloading the manga */
	private void doRun() {
		ctx.downloadChapterList();
		downloadChapters();
		downloadImagesForAllChapters();
		organizeImagesIntoChapters();
	}

    /**
     * Download chapter pages; their urls are from the master html file:
     *
     * <pre>
        <select onchange="change_chapter('Feng_Shen_Ji', this.value)" name="chapter">
            <option selected="selected" value="Chapter_001">Chapter 001</option>
            <option value="Chapter_002">Chapter 002</option>
            ...
        </select>
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(ctx.chapterList, BASE_URI);
        for (Element chapterOption : rootFile.select("select[name=chapter] option[value]")) {
            String chapterName = chapterOption.attr("value");
            Uri chapterUri = new Uri(BASE_URI + ctx.sourceName + "/" + chapterName);
            Path chapterPath = ctx.chaptersDir.resolve(chapterName + ".html");
            ctx.add(chapterUri, chapterPath);
        }
        ctx.download();
    }

    private void downloadImagesForAllChapters() {
        for (Path chapterHtml : Util.findHtmlFiles(ctx.chaptersDir)) {
            addImages(chapterHtml);
        }
        ctx.download();
    }

    /**
     * Find the images of a given chapter. The image urls are searched for in
     * javascript text such as:
     *
     * <pre>
        <script type="text/javascript">
            ...
            src="mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg"
            ...
     * </pre>
     */
    private void addImages(Path chapterHtml) {
        String chapterName = chapterHtml.getFileName().toString();
        chapterName = Util.removeFileExtension(chapterName);
        Document chapter = Util.parseHtml(chapterHtml, BASE_URI);
        for (Element script : chapter.select("script[type=text/javascript]")) {
            addImages(script, chapterName);
        }
    }

    /** Find images from text of a script element */
    private void addImages(Element script, String chapterName) {
    	for (String srcUrl : ctx.findSourceUrls(script.html())) {
            Uri imageUri = new Uri(BASE_URI + srcUrl);
            String imageName = chapterName + "_" + imageUri.getFileName();
            imageName = Util.padNumericSequences(imageName.toLowerCase(), 3);
            Path imagePath = ctx.imagesDir.resolve(imageName);
            ctx.add(imageUri, imagePath);
    	}
    }

    /**
     * Images downloaded from previous step are organized into chapter
     * directories
     */
    private void organizeImagesIntoChapters() {
        new CopyFiles(ctx.imagesDir, ctx.outputDir).setDirPattern(CHAPTER_PATTERN).run();
    }

}
