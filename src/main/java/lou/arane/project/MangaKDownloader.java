package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.BaseDownloader;
import lou.arane.util.Check;
import lou.arane.util.Uri;
import lou.arane.util.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Download comics from mangak site
 *
 * @author LOU
 */
public class MangaKDownloader extends BaseDownloader {

    /** Story list: dragon-ball-bay-vien-ngoc-rong; ushio-and-tora */
	public static void main(String[] args) {
	    String story = "ushio-and-tora";
		Path outputDir = Util.mangaDir("mangak", "Ushio and Tora");
        new MangaKDownloader(story, outputDir).run();
	}

    /* scheme of this site */
    private static final String BASE_URI = "http://mangak.net/";

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
    public MangaKDownloader(String story, Path baseDir) {
        this.mangaUri = new Uri(BASE_URI + story + "/");
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
     * <div class="chapter-list">
     *   <div class="row">
     *       <span><a href="http://mangak.net/dragon-ball-bay-vien-ngoc-rong-chap-minus/">Dragon Ball – chap 1</a></span>
     *   </div>
     *   ...
     * </pre>
     */
    private void downloadChapters() {
        Document rootFile = parseHtml(chapterList, BASE_URI);
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
     * The image links are found in:
     * <pre>
     *  <div class="vung_doc">
            <img src="http://2.bp.blogspot.com/ ..." alt="Dragon Ball ..." title="Dragon Ball ..."/>
            <img class="caucav1" src="http://4.bp.blogspot.com/ ..." title="Truy cap ngay ...">
        ...
     *  </pre>
     */
    private void downloadImages() {
        for (Path chapterHtml : findHtmlFiles(chapterDir)) {
            String chapterFileName = chapterHtml.getFileName().toString();
            Path chapterPath = imageDir.resolve(Util.removeFileExtension(chapterFileName));
            Document page = parseHtml(chapterHtml);
            int idx = 0;
            for (Element img: page.select("div[class=vung_doc] img[src][alt]")) {
                idx += 1;
                String imageSrc = img.attr("src");
                if (Uri.isValidUri(imageSrc)) {
                    Uri imageUri = new Uri(imageSrc);
                    String imageName = idx + "_" + imageUri.getFileName().toString();
                    Path imagePath = chapterPath.resolve(imageName);
                    add(imageUri, imagePath);
                } else {
                    //TODO track errors or have error listener
                    Util.printlnErr("Invalid image uri: " + imageSrc);
                }
            }
            download();
        }
    }
}
