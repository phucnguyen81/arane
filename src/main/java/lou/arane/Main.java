package lou.arane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lou.arane.core.Context;
import lou.arane.core.Command;
import lou.arane.handlers.BlogTruyenHandler;
import lou.arane.handlers.EgScansHandler;
import lou.arane.handlers.IzTruyenTranhHandler;
import lou.arane.handlers.KissMangaHandler;
import lou.arane.handlers.MangaGoHandler;
import lou.arane.handlers.MangaLifeHandler;
import lou.arane.handlers.MangaSeeHandler;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.Url;
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
		}
		else try {
			run(args[0], args[1]);
		}
		catch (Throwable t) {
			printHelp(args);
			throw t;
		}
	}

	private static void printHelp(String[] args) {
		System.err.println("*****************************************");
		System.err.println("Failed to handle arguments: " + Arrays.asList(args));
		System.err.println("*****************************************");
		System.err.println("Usage: arane name url");
		System.err.println("name = name to identify the content");
		System.err.println("url = the url you want to download from");
		System.err.println("e.g. arane WorldTrigger http://manga.life/read-online/WorldTrigger");
		System.err.println("*****************************************");
	}

	/** Run the first handler that can handle the given name and url */
	private static void run(String name, String url) {
		List<Command> commands = createHandlers(name, url);
		Optional<Command> h = findFirstRunnable(commands);
		if (h.isPresent()) {
			h.get().doRun();
		} else {
			Log.info("No supports for: " + name + ", " + url);
		}
	}

	/** Create all available handlers */
	private static List<Command> createHandlers(String name, String url) {
		List<Command> commands = New.list();
		Context ctx;

		ctx = new Context(name, new Url(url), mangaDir("blogtruyen", name));
		commands.add(createLogHandler(ctx, new BlogTruyenHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("eggscans", name));
		commands.add(createLogHandler(ctx, new EgScansHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("izmanga", name));
		commands.add(createLogHandler(ctx, new IzTruyenTranhHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("kissmanga", name));
		commands.add(createLogHandler(ctx, new KissMangaHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("mangago", name));
		commands.add(createLogHandler(ctx, new MangaGoHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("manga.life", name));
		commands.add(createLogHandler(ctx, new MangaLifeHandler(ctx)));

		ctx = new Context(name, new Url(url), mangaDir("mangasee", name));
		commands.add(createLogHandler(ctx, new MangaSeeHandler(ctx)));

		return commands;
	}

	/** Decorate a handler to log a message after complete a run */
	private static Command createLogHandler(Context ctx, Command command) {
		return new Command() {
			@Override
			public boolean canRun() {
				return command.canRun();
			}
			@Override
			public void doRun() {
				command.doRun();
				String msg = String.format(
					"Finished downloading %s into %s", ctx.sourceName, ctx.target);
				Log.info(msg);
			}
		};
	}

	private static Optional<Command> findFirstRunnable(Collection<Command> commands) {
		return commands.stream().filter(Command::canRun).findFirst();
	}

	/** Common way to get a base directory for downloading a manga. */
	public static Path mangaDir(String first, String... more) {
	    Path mangasDir = Util.userHomeDir().resolve("mangas");
	    Path baseDir = mangasDir.resolve(Paths.get(first, more));
	    return baseDir;
	}

}
