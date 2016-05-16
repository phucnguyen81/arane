package lou.arane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lou.arane.base.Command;
import lou.arane.base.Context;
import lou.arane.commands.BlogTruyen;
import lou.arane.commands.EgScans;
import lou.arane.commands.IzTruyenTranh;
import lou.arane.commands.KissManga;
import lou.arane.commands.MangaGo;
import lou.arane.commands.MangaLife;
import lou.arane.commands.MangaSee;
import lou.arane.commands.decor.ReplaceCommand;
import lou.arane.util.Log;
import lou.arane.util.Url;
import lou.arane.util.Util;

/**
 * Entry point of the system.
 *
 * @author Phuc
 */
public class Arane {

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
		System.err.println("------------------------------------------");
		System.err.println("Failed to handle arguments: " + Arrays.asList(args));
		System.err.println("------------------------------------------");
		System.err.println("Usage: arane name url");
		System.err.println("name = name to identify the content");
		System.err.println("url = the url you want to download from");
		System.err.println("e.g. arane WorldTrigger http://manga.life/read-online/WorldTrigger");
		System.err.println("------------------------------------------");
		System.err.println("Current supported sites are:");
		System.err.println("blogtruyen, egscans, iztruyentranh, kissmanga, mangago, manga.life, mangasee");
		System.err.println("------------------------------------------");
	}

	/** Run the first command that can handle the given name and url */
	private static void run(String name, String url) {
		List<Command> commands = createCommands(name, url);
		Optional<Command> h = findFirstRunnable(commands);
		if (h.isPresent()) {
			h.get().doRun();
		} else {
			Log.info("No supports for: " + name + ", " + url);
		}
	}

	/** Create all available handlers */
	private static List<Command> createCommands(String name, String url) {
		List<Command> commands = new ArrayList<>();
		Context ctx;

		ctx = new Context(name, new Url(url), mangaDir("blogtruyen", name));
		commands.add(attachLog(new BlogTruyen(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("egscans", name));
		commands.add(attachLog(new EgScans(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("izmanga", name));
		commands.add(attachLog(new IzTruyenTranh(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("kissmanga", name));
		commands.add(attachLog(new KissManga(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("mangago", name));
		commands.add(attachLog(new MangaGo(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("manga.life", name));
		commands.add(attachLog(new MangaLife(ctx), ctx));

		ctx = new Context(name, new Url(url), mangaDir("mangasee", name));
		commands.add(attachLog(new MangaSee(ctx), ctx));

		return commands;
	}

	/** Attach logging before and after a run */
	private static Command attachLog(Command cmd, Context ctx) {
		return new ReplaceCommand(cmd, () -> {
			Log.info(String.format("Start downloading %s into %s", ctx.sourceName, ctx.target));
			cmd.doRun();
			Log.info(String.format("Finished downloading %s into %s", ctx.sourceName, ctx.target));
		});
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
