package lou.arane.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.Check;
import lou.arane.util.FileResource;
import lou.arane.util.ToString;
import lou.arane.util.URLResource;

/**
 * Encapsulated Context?, or trying to be one.
 * <p>
 * TODO this is mutable, which is troublesome for being a Context. Maybe it can
 * be a common internal part of the usecases?
 *
 * @author Phuc
 */
public class Context {

    public final String sourceName;

    /* initial location to download from */
    public final URLResource source;

    /* base location to download to */
    public final Path target;

    public final FileResource chapterList;
    public final Path chaptersDir;

    public final Path pagesDir;

    public final Path imagesDir;
    public final Path outputDir;

    /** Re-try limit if a download fails */
    private final int maxDownloadAttempts = 3;

    /**
     * Pattern for extracting urls from text such as: src=
     * "mangas/Feng Shen Ji/Chapter 001/Feng_Shen_Ji_ch01_p00.jpg"
     */
    private final Pattern srcPattern = Pattern.compile("src=['\"]([^'\"]+)['\"]");

    private final List<Download> items = new ArrayList<>();

    public Context(String sourceName, URLResource source, Path baseDir) {
        this.source = source;
        this.target = baseDir;
        this.sourceName = sourceName;

        chapterList = new FileResource(baseDir.resolve("chapters.html"));
        chaptersDir = baseDir.resolve("chapters");
        pagesDir = baseDir.resolve("pages");
        imagesDir = baseDir.resolve("images");
        outputDir = baseDir.resolve("output");
    }

    @Override
    public String toString() {
        return ToString.of(Context.class).add("source=", source).add("target=", target).str();
    }

    /**
     * It is common to many manga sites that the initial source url lists the
     * chapters. This downloads the intial url that contains the chapter
     * locations
     */
    public void downloadChapterList() {
        chapterList.deleteIfExists();
        add(source, chapterList);
        download();
        Check.postCond(chapterList.exists(),
                "Failed to download chapter listing to " + chapterList);
    }

    /**
     * Find urls enclosed in text pattern "src='url'". This is needed because if
     * the url is embedded in javascript elemements then we cannot search for it
     * in html tags.
     */
    public List<String> findSourceUrls(String str) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = srcPattern.matcher(str);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    /**
     * Add a pair of source-target to download later
     */
    public void add(URLResource src, FileResource dst) {
        items.add(new Download(src, dst));
    }

    /**
     * Download items added so far. All items are cleared after this returns.
     */
    public void download() {
        Downloads d = new Downloads(items, maxDownloadAttempts);
        items.clear();
        d.run();
    }

}
