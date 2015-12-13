package lou.arane.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lou.arane.util.http.HttpFileBatchDownloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Common methods to help with downloading.
 *
 * @author LOU
 */
public class BaseDownloader {

    /** Indentation for building html */
    protected static final Object __ = TreeBuilder.__;

    /** Batch downloader */
    private final HttpFileBatchDownloader downloader = new HttpFileBatchDownloader()
        .setMaxDownloadAttempts(3);

    /** Register a pair of uri-path to download later */
    public void add(Uri fromUri, Path toPath) {
        downloader.add(fromUri, toPath);
    }

    /** Download all what been added so far */
    public void download() {
        downloader.sortByPath();
        Util.println(downloader);
        downloader.download();
    }

	/** @see #parseHtml(Path, String) */
	public static Document parseHtml(Path path, Uri baseUri) {
		return parseHtml(path, baseUri.toString());
	}

	/**
	 * Parse html file and return its html model. The base uri is set on the
	 * model to resolve urls
	 */
	public static Document parseHtml(Path path, String baseUri) {
		Document html = parseHtml(path);
		html.setBaseUri(baseUri);
		return html;
	}

	/**
	 * Parse html file and return its html model. Throw an unchecked exception
	 * if parsing fails.
	 */
	public static Document parseHtml(Path path) {
		String defaultCharsetName = null;
		try {
			return Jsoup.parse(path.toFile(), defaultCharsetName);
		} catch (IOException e) {
			throw new Unchecked("Failed to parse html file " + path, e);
		}
	}

	/** Find html files of a given directory */
	public static List<Path> findHtmlFiles(Path dir) {
		return Util.list(dir)
		        .filter(Util::isRegularFile)
				.filter(file -> file.toString().endsWith(".html"))
				.collect(Collectors.toList());
	}

	/** Find all html files in the directory tree rooted at a given directory */
    public static List<Path> findAllHtmlFiles(Path dir) {
        return Util.walk(dir)
                .filter(Util::isRegularFile)
                .filter(file -> file.toString().endsWith(".html"))
                .collect(Collectors.toList());
    }

}
