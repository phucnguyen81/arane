package lou.arane.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.Uri;
import lou.arane.util.Util;
import lou.arane.util.http.HttpFileBatchDownloader;

/**
 * Common data/methods made global.
 *
 * @author Phuc
 */
public class Context {

    /**
     * Pattern for extracting urls from text such as:
     * src="mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg"
     */
    public static final Pattern SRC_PATTERN = Pattern.compile("src=['\"]([^'\"]+)['\"]");

    /** Find urls enclosed in "src='url'" */
	public static List<String> findSourceUrls(String str) {
		if (str == null) return Collections.emptyList();
		List<String> urls = new ArrayList<>();
        Matcher matcher = SRC_PATTERN.matcher(str);
        while (matcher.find()) {
        	urls.add(matcher.group(1));
        }
        return urls;
	}

	public final String sourceName;

	/* initial location to download from */
	public final Uri source;

	/* base location to download to */
	public final Path target;

	public final Path chapterList;
	public final Path chaptersDir;

	public final Path pagesDir;

	public final Path imagesDir;
	public final Path outputDir;

	private final HttpFileBatchDownloader downloader = new HttpFileBatchDownloader()
			.setMaxDownloadAttempts(3);

	public Context(String sourceName, Uri source, Path baseDir) {
		this.source = source;
		this.target = baseDir;
		this.sourceName = sourceName;

		chapterList = baseDir.resolve("chapters.html");
		chaptersDir = baseDir.resolve("chapters");
		pagesDir = baseDir.resolve("pages");
		imagesDir = baseDir.resolve("images");
		outputDir = baseDir.resolve("output").resolve(baseDir.getFileName());
	}

	/** It is common to many manga sites that the initial source url lists the chapters.
	 * This downloads the intial url that contains the chapter locations */
	public void downloadChapterList() {
		Util.deleteIfExists(chapterList);
		add(source, chapterList);
		download();
		Check.postCond(Files.exists(chapterList),
				"Failed to download chapter listing to " + chapterList);
	}

	/** Register a pair of uri-path to download later */
	public void add(Uri fromUri, Path toPath) {
		downloader.add(fromUri, toPath);
	}

	/** Download what been added so far */
	public void download() {
		downloader.sortByPath();
		Log.info("Start download: " + downloader);
		downloader.download();
	}

}
