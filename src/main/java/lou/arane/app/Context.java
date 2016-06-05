package lou.arane.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.core.Cmd;
import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Common data/methods for all {@link Cmd}.
 * There are no static elements, everything is found on the instance.
 *
 * @author Phuc
 */
public class Context {

	public final String sourceName;

	/* initial location to download from */
	public final URLResource source;

	/* base location to download to */
	public final Path target;

	public final Path chapterList;
	public final Path chaptersDir;

	public final Path pagesDir;

	public final Path imagesDir;
	public final Path outputDir;

	/** Re-try limit if a download fails */
	public int maxDownloadAttempts = 3;

    /** Pattern for extracting urls from text such as:
     * src="mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg" */
    public Pattern srcPattern = Pattern.compile("src=['\"]([^'\"]+)['\"]");

    private final List<Entry<URLResource, Path>> items = new ArrayList<>();

	public Context(String sourceName, URLResource source, Path baseDir) {
		this.source = source;
		this.target = baseDir;
		this.sourceName = sourceName;

		chapterList = baseDir.resolve("chapters.html");
		chaptersDir = baseDir.resolve("chapters");
		pagesDir = baseDir.resolve("pages");
		imagesDir = baseDir.resolve("images");
		outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
	}

	@Override
	public String toString() {
		return String.format("%s: %s -> %s"
			, Context.class.getSimpleName(), source, target
		);
	}

	/** It is common to many manga sites that the initial source url lists the chapters.
	 * This downloads the intial url that contains the chapter locations */
	public void downloadChapterList() {
		Util.deleteIfExists(chapterList);
		add(source, chapterList);
		download();
		Check.postCond(Util.exists(chapterList),
			"Failed to download chapter listing to " + chapterList);
	}

	/** Add a pair of source-target to download later */
	public void add(URLResource fromUri, Path toPath) {
		items.add(New.entry(fromUri, toPath));
	}

	/** Download items added so far.
	 * All items are cleared after this returns. */
	public void download() {
		try {
			new Downloads(itemsSorted(), maxDownloadAttempts).run();
		}
		finally {
			items.clear();
		}
	}

	/** Sort by the target path */
	private List<Entry<URLResource, Path>> itemsSorted() {
		List<Entry<URLResource, Path>> sorted = new ArrayList<>(items);
		sorted.sort((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
		return sorted;
	}

    /** Find urls enclosed in text pattern "src='url'".
     * This is needed because if the url is embedded in javascript
     * elemements then we cannot search for it in html tags. */
	public List<String> findSourceUrls(String str) {
		List<String> urls = new ArrayList<>();
        Matcher matcher = srcPattern.matcher(str);
        while (matcher.find()) {
        	urls.add(matcher.group(1));
        }
        return urls;
	}

}
