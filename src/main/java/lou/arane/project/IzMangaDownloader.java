package lou.arane.project;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Download comics from izmanga site
 *
 * @author LOU
 */
public class IzMangaDownloader extends BaseDownloader {

	public static void main(String[] args) {
	    String story = "ban_long-117";
		Path outputDir = Util.mangaDir("izmanga", "Ban Long");
        new IzMangaDownloader(story, outputDir).run();
	}
	
    /* scheme of this site */
    private static final String BASE_URI = "http://izmanga.com/";

    /* pattern to look for images embeded in the chapter pages */
    private static final Pattern DATA_IMAGES_PATTERN = Pattern.compile("data\\s*=\\s*'(?<imgs>http:.+)'");

    private final Uri mangaUri;

    private final Path chapterList;

    private final Path chapterDir;
    private final Path imageDir;

    /**
     * Create a downloader to download a story to a directory
     *
     * @param story = base name of the story, e.g. "ban_long-117"
     * @param baseDir = dir to download to, e.g. "mangas/Ban Long"
     */
    public IzMangaDownloader(String story, Path baseDir) {
        this.mangaUri = new Uri(BASE_URI + story);
        chapterList = baseDir.resolve("chapters.html");
        chapterDir = baseDir.resolve("chapters");
        imageDir = baseDir.resolve("images");
    }

    /** Run the entire process of downloading the manga */
    public void run() {
        downloadChapterList();
        downloadChapters();
        downloadImages();
    }

    /** Download the newest page that lists all chapters */
    private void downloadChapterList() {
        Util.deleteIfExists(chapterList);
        add(mangaUri, chapterList);
        download();
        Check.postCond(Util.exists(chapterList),
            "Chapter listing must be downloaded to " + chapterList);
    }

    /**
     * Download chapter pages by extracting their urls from the master html
     * file. The page urls are taken from the chapter listing:
     *
     * <pre>
     *   <div class="chapter-list">
     *      <div class="row">
     *          <span>
     *              <a href="http://izmanga.com/chapter/ban_long/117/94716">001</a>
     *          </span>
     * ...
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = Util.parseHtml(chapterList, BASE_URI);
        Elements chapterAddresses = rootFile.select("div[class=chapter-list] a[href]");
        for (Element chapterAddr : chapterAddresses) {
            Uri chapterUri = new Uri(chapterAddr.absUrl("href"));
            String chapterName = chapterAddr.ownText();
            if (!chapterName.endsWith(".html")) {
                chapterName += ".html";
            }
            Path chapterPath = chapterDir.resolve(chapterName);
            add(chapterUri, chapterPath);
        }
        download();
    }
    
    /**
     * Download the actual images for each chapter. 
     * The image links are found in javascript content:
     * <pre>
     *  data = 'http://2.bp.blogspot.com/-A9scOkmQ61Q/UP_zXu7qUbI/AAAAAAAAFFc/exlxGRoLYuw/0%252520copy.jpg?imgmax=2000|http://2.bp.blogspot.com/-8iv32sR7N2k/UP_zZDw3_yI/AAAAAAAAFFg/jXUNe3dRca0/0%252520Credit-ban-long.jpg?imgmax=2000|...
     *  </pre>
     */
    private void downloadImages() {
        for (Path chapterHtml : Util.findHtmlFiles(chapterDir)) {
            String chapterFileName = chapterHtml.getFileName().toString();
            Path chapterPath = imageDir.resolve(Util.removeFileExtension(chapterFileName));
            Document page = Util.parseHtml(chapterHtml);
            Matcher imagesMatcher = DATA_IMAGES_PATTERN.matcher(page.data());
            if (imagesMatcher.find()) {
                int idx = 0;
                for (String img : imagesMatcher.group("imgs").split("\\|")) {
                    idx += 1;
                    Uri imageUri = new Uri(img.trim());
                    String imageName = String.format("%s.%s", idx, imageUri.getFileExtension());
                    Path imagePath = chapterPath.resolve(imageName);
                    add(imageUri, imagePath);
                }
            }
            download();
        }
    }
}
