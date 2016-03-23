package lou.arane;

import java.nio.file.Path;

import lou.arane.project.mangalife.MangaLifeDownloader;
import lou.arane.util.Uri;
import lou.arane.util.Util;

/**
 * Main class for downloading mangas from command line
 * 
 * @author Phuc
 */
public class Arane {
	
	public static void main(String[] args) {
		if (args.length == 0) {
			showHelp();
		}
		else {
			download(args[0]);
		}
	}

	private static void showHelp() {
		Util.println("Usage: arane url");
		Util.println("e.g: arane http://manga.life/read-online/Berserk");
	}

	private static void download(String url) {
		Uri uri = new Uri(url);
		String mangaName = uri.getFileName().toString();
		Path mangaDir = Util.mangaDir("mangalife", mangaName);
		Util.println("Prepare to download to " + mangaDir);
		if (url.startsWith("http://manga.life/read-online/")) {
			new MangaLifeDownloader(mangaName, mangaDir).run();
		}
		else {
			Util.printlnErr("Does not support url " + url);
		}
	}

}
