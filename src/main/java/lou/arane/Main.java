package lou.arane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import lou.arane.core.Context;
import lou.arane.core.Handler;
import lou.arane.handlers.BlogTruyenHandler;
import lou.arane.handlers.EgScansHandler;
import lou.arane.handlers.IzTruyenTranhHandler;
import lou.arane.handlers.KissMangaHandler;
import lou.arane.handlers.MangaGoHandler;
import lou.arane.handlers.MangaLifeHandler;
import lou.arane.handlers.MangaSeeHandler;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.Uri;
import lou.arane.util.Util;

/**
 * Entry point of the system.
 *
 * @author Phuc
 */
public class Main {

	public static void main(String[] args) {
		if (args.length < 2) {
			printHelp(args);
			return;
		}
		try {
			download(args[0], args[1]);
		}
		catch (Throwable t) {
			printHelp(args);
			throw t;
		}
	}

	private static void printHelp(String[] args) {
		System.err.println("*****************************************");
		System.err.println("Failed to handle arguments: " + New.list(args));
		System.err.println("*****************************************");
		System.err.println("Usage: arane name url");
		System.err.println("name = name to identify the content");
		System.err.println("url = the url you want to download from");
		System.err.println("e.g. arane WorldTrigger http://manga.life/read-online/WorldTrigger");
		System.err.println("*****************************************");
	}

	/** Call each handler to handle the url */
	private static void download(String name, String url) {
		Optional<Handler> handler = createHandlers(name, url)
		.stream()
		.filter(Handler::canRun)
		.findFirst()
		;
		if (handler.isPresent()) {
			handler.get().doRun();
		}
		else {
			Log.error("No handlers defined for handling: " + name + ", " + url);
		}
	}

	private static List<Handler> createHandlers(String name, String url) {
		List<Handler> handlers = New.list();
		handlers.add(new BlogTruyenHandler(new Context(name, Uri.of(url), mangaDir("blogtruyen", name))));
		handlers.add(new EgScansHandler(new Context(name, Uri.of(url), mangaDir("eggscans", name))));
		handlers.add(new IzTruyenTranhHandler(new Context(name, Uri.of(url), mangaDir("izmanga", name))));
		handlers.add(new KissMangaHandler(new Context(name, Uri.of(url), mangaDir("kissmanga", name))));
		handlers.add(new MangaGoHandler(new Context(name, Uri.of(url), mangaDir("mangago", name))));
		handlers.add(new MangaLifeHandler(new Context(name, Uri.of(url), mangaDir("manga.life", name))));
		handlers.add(new MangaSeeHandler(new Context(name, Uri.of(url), mangaDir("mangasee", name))));
		return handlers;
	}

	/** Common way to get a base directory for downloading a manga. */
	public static Path mangaDir(String first, String... more) {
	    Path mangasDir = Util.userHomeDir().resolve("mangas");
	    Path baseDir = mangasDir.resolve(Paths.get(first, more));
	    return baseDir;
	}

}
