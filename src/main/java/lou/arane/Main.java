package lou.arane;

import java.util.List;

import lou.arane.core.Context;
import lou.arane.handlers.MangaLifeHandler;
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
		for (Runnable handler : createHandlers(name, url)) {
			handler.run();
		}
	}

	private static List<Runnable> createHandlers(String name, String url) {
		List<Runnable> handlers = New.list();
		handlers.add(new MangaLifeHandler(new Context(name, new Uri(url), Util.mangaDir("manga.life", name))));
		return handlers;
	}

}
